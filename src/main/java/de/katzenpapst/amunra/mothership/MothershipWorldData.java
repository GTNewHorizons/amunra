package de.katzenpapst.amunra.mothership;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.DimensionManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.helper.PlayerID;
import de.katzenpapst.amunra.network.packet.PacketSimpleAR;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody.ScalableDistance;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.api.galaxies.Satellite;

public class MothershipWorldData extends WorldSavedData {

    public static final String saveDataID = "ARMothershipData";

    // orbit distances should stay the same
    private final Map<CelestialBody, Float> orbitDistances = new HashMap<>();

    private int highestId = 0;
    private int numTicksWithoutSave = 0;

    // https://github.com/Questology/Questology/blob/d125a9359e50a84ccee0c5100f04464a0d13e072/src/main/java/demonmodders/questology/handlers/event/GenericEventHandler.java
    protected final HashMap<Integer, Mothership> mothershipIdList = new HashMap<>();

    protected final Map<Integer, Mothership> mothershipsByDimension = new HashMap<>();

    public MothershipWorldData(final String id) {
        super(id);
    }

    @SuppressWarnings("unchecked")
    public HashMap<Integer, Mothership> getMotherships() {
        return (HashMap<Integer, Mothership>) this.mothershipIdList.clone();
    }

    protected void updateAllOrbits() {
        final Map<CelestialBody, Integer> bodies = this.getBodiesWithShips();
        for (final CelestialBody b : bodies.keySet()) {
            this.updateOrbitsFor(b);
        }
    }

    protected void updateOrbitsFor(final CelestialBody parent) {
        if (parent == null) return;

        final List<Mothership> list = this.getMothershipsForParent(parent);
        final int numShips = list.size();
        final float twoPi = (float) Math.PI * 2;
        final float angle = twoPi / numShips;
        final Random rand = new Random(parent.getName().hashCode());
        float phaseOffset = rand.nextFloat() * twoPi;
        final float orbitDistance = this.getMothershipOrbitDistanceFor(parent);

        for (final Mothership ms : list) {

            if (phaseOffset > twoPi) {
                phaseOffset -= twoPi;
            }

            ms.setPhaseShift(phaseOffset);
            ms.setRelativeDistanceFromCenter(new ScalableDistance(orbitDistance, orbitDistance));
            phaseOffset += angle;
        }
        this.markDirty();
    }

    public float getMothershipOrbitDistanceFor(final CelestialBody parent) {
        if (this.orbitDistances.get(parent) != null) {
            return this.orbitDistances.get(parent);
        }

        // recalc
        float orbitSize = -1;
        if (parent instanceof Planet) {
            // now try to find out what the closest thing here is
            for (final Moon moon : GalaxyRegistry.getRegisteredMoons().values()) {
                if (moon.getParentPlanet() != parent) continue;
                if (orbitSize == -1 || orbitSize > moon.getRelativeDistanceFromCenter().unScaledDistance) {
                    orbitSize = moon.getRelativeDistanceFromCenter().unScaledDistance;
                }
            }
            for (final Satellite satellite : GalaxyRegistry.getRegisteredSatellites().values()) {
                if (satellite.getParentPlanet() != parent) {
                    continue;
                }
                if (orbitSize == -1 || orbitSize > satellite.getRelativeDistanceFromCenter().unScaledDistance) {
                    orbitSize = satellite.getRelativeDistanceFromCenter().unScaledDistance;
                }

            }
            if (orbitSize == -1) {
                orbitSize = 10.0F;
            } else {
                orbitSize -= 1.0F;
            }
        } else {
            // todo figure out
            orbitSize = 5.0F;

        }
        this.orbitDistances.put(parent, orbitSize);
        return orbitSize;
    }

    /**
     * Creates new mothership for given player and given parentBody, sends it to all clients and returns it.
     */
    public Mothership registerNewMothership(final EntityPlayer player, final CelestialBody currentParent) {
        final int newId = ++this.highestId;

        // failsafe
        if (this.mothershipIdList.get(newId) != null) {
            throw new RuntimeException("Somehow highestID is already used");
        }

        // find dimension ID
        final int newDimensionID = DimensionManager.getNextFreeDimId();

        DimensionManager.registerDimension(newDimensionID, AmunRa.config.mothershipProviderID);

        final Mothership ship = new Mothership(newId, new PlayerID(player));
        ship.setParent(currentParent);
        ship.setDimensionInfo(newDimensionID);

        this.mothershipIdList.put(newId, ship);
        this.mothershipsByDimension.put(newDimensionID, ship);
        this.updateOrbitsFor(currentParent);// Do I even need this on server side?

        this.markDirty();

        final NBTTagCompound data = new NBTTagCompound();
        ship.writeToNBT(data);

        AmunRa.packetPipeline
                .sendToAll(new PacketSimpleAR(PacketSimpleAR.EnumSimplePacket.C_NEW_MOTHERSHIP_CREATED, data));
        return ship;
    }

    /**
     * Add an existing mothership object, usually one which the server sent here
     *
     * @return the definite mothership object as it should be used and stuff
     */
    @SideOnly(Side.CLIENT)
    public Mothership addMothership(final Mothership ship) {

        if (MinecraftServer.getServer() != null && !MinecraftServer.getServer().isDedicatedServer()) {
            // don't do this on an integrated SSP server, because for these, the list is up-to-date already
            this.updateOrbitsFor(ship.getParent());
            // here we have a stupid case where the ship we get is a duplicate of one in the list
            return this.getByMothershipId(ship.getID());
        }
        // probably got from server
        if (ship.getID() > this.highestId) {
            this.highestId = ship.getID();
        }

        if (this.mothershipIdList.get(ship.getID()) != null) {
            throw new RuntimeException(
                    "Mothership " + ship.getID() + " is already registered, this shouldn't happen...");
        }

        this.maybeRegisterDimension(ship.getDimensionID());

        this.mothershipIdList.put(ship.getID(), ship);
        this.mothershipsByDimension.put(ship.getDimensionID(), ship);
        this.updateOrbitsFor(ship.getParent());
        return ship;
    }

    /**
     * Should only be used if only the number of ships around a body is required, otherwise just get the full list
     */
    public int getNumMothershipsForParent(final CelestialBody parent) {
        if (parent == null) {
            return 0;
        }
        return (int) this.mothershipIdList.values().stream().map(Mothership::getParent).filter(parent::equals).count();
    }

    public boolean hasMothershipsInOrbit(final CelestialBody parent) {
        return parent != null
                && this.mothershipIdList.values().stream().map(Mothership::getParent).anyMatch(parent::equals);
    }

    /**
     * Get all motherships for a certain parent
     */
    public List<Mothership> getMothershipsForParent(final CelestialBody parent) {
        if (parent == null) {
            return new ArrayList<>(0);
        }
        return this.mothershipIdList.values().stream().filter(ship -> parent.equals(ship.getParent()))
                .collect(Collectors.toList());
    }

    /**
     * Get all motherships owned by a certain player
     */
    public int getNumMothershipsForPlayer(final PlayerID player) {
        return (int) this.mothershipIdList.values().stream().filter(ship -> ship.isPlayerOwner(player)).count();
    }

    public int getNumMothershipsForPlayer(final EntityPlayer player) {
        return this.getNumMothershipsForPlayer(new PlayerID(player));
    }

    /**
     * Gets a list of CelestialBodies which have motherships.
     *
     * @return a map where the key is the celestial body and the value is the number of motherships around it
     */
    public Map<CelestialBody, Integer> getBodiesWithShips() {
        final Map<CelestialBody, Integer> result = new HashMap<>();

        for (final Mothership curM : this.mothershipIdList.values()) {
            final CelestialBody parent = curM.getParent();
            if (parent == null) continue;

            if (result.containsKey(parent)) {
                result.put(parent, result.get(parent) + 1);
            } else {
                result.put(parent, 1);
            }

        }

        return result;
    }

    public Mothership getByDimensionId(final int dimId) {
        return this.mothershipsByDimension.get(dimId);
    }

    public Mothership getByMothershipId(final int id) {
        return this.mothershipIdList.get(id);
    }

    public Mothership getByName(final String name) {
        return this.mothershipIdList.values().stream().filter(ship -> ship.getName().equals(name)).findFirst()
                .orElse(null);
    }

    @Override
    public void readFromNBT(NBTTagCompound p_76184_1_) {
        final NBTTagList tagList = p_76184_1_.getTagList("MothershipList", 10);
        this.mothershipIdList.clear();
        this.mothershipsByDimension.clear();

        for (int i = 0; i < tagList.tagCount(); i++) {
            final NBTTagCompound nbt2 = tagList.getCompoundTagAt(i); // I think I have to unregister them on player
                                                                     // logout.
            final Mothership m = Mothership.createFromNBT(nbt2);
            if (this.highestId < m.getID()) {
                this.highestId = m.getID();
            }

            if (DimensionManager.isDimensionRegistered(m.getDimensionID())) {
                if (DimensionManager.getProviderType(m.getDimensionID()) != AmunRa.config.mothershipProviderID) {
                    // now that shouldn't happen
                    throw new RuntimeException(
                            "Dimension " + m.getDimensionID()
                                    + " should be registered for an AmunRa Mothership, registered for "
                                    + DimensionManager.getProviderType(m.getDimensionID())
                                    + " instead");
                }
                // it's fine otherwise
            } else {
                DimensionManager.registerDimension(m.getDimensionID(), AmunRa.config.mothershipProviderID);
            }

            this.mothershipIdList.put(m.getID(), m);
            this.mothershipsByDimension.put(m.getDimensionID(), m);
        }

        this.updateAllOrbits();

    }

    /**
     * Hack for client-side dimension registration
     */
    protected void maybeRegisterDimension(final int dimId) {
        if (!DimensionManager.isDimensionRegistered(dimId)) {
            DimensionManager.registerDimension(dimId, AmunRa.config.mothershipProviderID);
        } else {
            // just check if it's registered the right way
            final int type = DimensionManager.getProviderType(dimId);
            if (type != AmunRa.config.mothershipProviderID) {
                throw new RuntimeException(
                        "Dimension " + dimId + " could not be registered for mothership because it's already taken");
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound p_76187_1_) {
        final NBTTagList tagList = new NBTTagList();

        // HashMap<Integer, Mothership> mothershipIdList
        for (final Mothership m : this.mothershipIdList.values()) {
            final NBTTagCompound nbt2 = new NBTTagCompound();
            m.writeToNBT(nbt2);
            tagList.appendTag(nbt2);
        }

        p_76187_1_.setTag("MothershipList", tagList);
    }

    public void tickAllMotherships() {
        boolean hasChanged = false;
        for (final Mothership m : this.mothershipIdList.values()) {
            if (!m.isInTransit()) {
                continue;
            }
            this.numTicksWithoutSave++;
            hasChanged = true;
            if (m.modRemainingTravelTime(-1) <= 0) {
                // arrived

                // we will need the worldprovider here
                m.getWorldProviderServer().endTransit();

                AmunRa.packetPipeline.sendToAll(
                        new PacketSimpleAR(PacketSimpleAR.EnumSimplePacket.C_MOTHERSHIP_TRANSIT_ENDED, m.getID()));
            }
        }
        // if no changes, but still unsaved changes
        if ((hasChanged || !hasChanged && this.numTicksWithoutSave > 0) && this.numTicksWithoutSave >= 1200) {
            this.numTicksWithoutSave = 0;
            this.markDirty();
        }
    }

    /**
     * This is just so that the progress bar is updated on client
     */
    public void tickAllMothershipsClient() {
        for (final Mothership m : this.mothershipIdList.values()) {
            if (!m.isInTransit()) {
                continue;
            }
            if (m.getRemainingTravelTime() > 0) {
                m.modRemainingTravelTime(-1);
            }
        }
    }

    public void unregisterAllMotherships() {

        for (final Integer dimID : this.mothershipsByDimension.keySet()) {
            DimensionManager.unregisterDimension(dimID);
        }

        this.mothershipIdList.clear();
        this.mothershipsByDimension.clear();

    }
}
