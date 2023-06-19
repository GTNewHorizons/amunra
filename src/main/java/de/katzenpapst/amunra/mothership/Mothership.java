package de.katzenpapst.amunra.mothership;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.helper.AstronomyHelper;
import de.katzenpapst.amunra.helper.PlayerID;
import de.katzenpapst.amunra.tick.TickHandlerServer;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.api.galaxies.Satellite;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import micdoodle8.mods.galacticraft.api.galaxies.Star;
import micdoodle8.mods.galacticraft.core.proxy.ClientProxyCore;

public class Mothership extends CelestialBody {

    public enum PermissionMode {
        ALL, // everyone
        NONE, // only owner
        WHITELIST, // owner+everyone on list
        BLACKLIST // everyone except people on list
    }

    protected Set<PlayerID> playerSetLanding = new HashSet<>();
    protected Set<PlayerID> playerSetUsage = new HashSet<>();

    protected PermissionMode permModeLanding = PermissionMode.NONE;
    protected PermissionMode permModeUsage = PermissionMode.NONE;

    protected PlayerID owner;

    protected String msName = "";

    protected CelestialBody previousParent;
    protected CelestialBody currentParent;

    protected long travelTimeTotal;
    protected long travelTimeRemaining;

    protected boolean inTransit = false;

    protected int mothershipId;

    // the backslash should definitely not be valid for unlocalizedName
    public static final String nameSeparator = "\\";

    public Mothership(final int id, final PlayerID owner) {
        super("mothership_" + id);
        this.mothershipId = id;

        this.owner = owner;

        this.setBodyIcon(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/mothership_icons/0.png"));
        this.setRelativeOrbitTime(5);
    }

    public Mothership(final int id, final UUID ownerUUID, final String ownerName) {
        this(id, new PlayerID(ownerUUID, ownerName));
    }

    public boolean setParent(final CelestialBody parent) {
        if (parent instanceof Satellite) {
            return false;
        }

        this.currentParent = parent;
        this.inTransit = false;
        this.travelTimeRemaining = 0;

        return true;
    }

    public Set<PlayerID> getPlayerListLanding() {
        return this.playerSetLanding;
    }

    public void addPlayerToListLanding(final PlayerID pi) {
        this.playerSetLanding.add(pi);
    }

    public void setPlayerListLanding(final Collection<PlayerID> list) {
        this.playerSetLanding.clear();
        this.playerSetLanding.addAll(list);
    }

    public Set<PlayerID> getPlayerListUsage() {
        return this.playerSetUsage;
    }

    public void addPlayerToListUsage(final PlayerID pi) {
        this.playerSetUsage.add(pi);
    }

    public void setPlayerListUsage(final Collection<PlayerID> list) {
        this.playerSetUsage.clear();
        this.playerSetUsage.addAll(list);
    }

    public PermissionMode getLandingPermissionMode() {
        return this.permModeLanding;
    }

    public void setLandingPermissionMode(final PermissionMode mode) {
        this.permModeLanding = mode;
    }

    public PermissionMode getUsagePermissionMode() {
        return this.permModeUsage;
    }

    public void setUsagePermissionMode(final PermissionMode mode) {
        this.permModeUsage = mode;
    }

    @Override
    public boolean getReachable() {
        return this.isReachable;
    }

    @Override
    public void setUnreachable() {
        // noop
        this.isReachable = true;
    }

    @Override
    public String getLocalizedName() {
        if (this.msName.isEmpty()) {
            this.msName = String.format(StatCollector.translateToLocal("mothership.default.name"), this.mothershipId);
        }
        return this.msName;
    }

    public void setLocalizedName(final String newName) {
        this.msName = newName;
    }

    public boolean isInTransit() {
        return this.inTransit;
    }

    /**
     * Returns the parent, if stationary
     *
     * @return
     */
    public CelestialBody getParent() {
        if (this.inTransit) {
            return null;
        }
        return this.currentParent;
    }

    /**
     * Returns the previous parent, if in transit
     *
     * @return
     */
    public CelestialBody getSource() {
        if (this.inTransit) {
            return this.previousParent;
        }
        return null;
    }

    /**
     * Returns the destination or the parent if stationary
     *
     * @return
     */
    public CelestialBody getDestination() {
        return this.currentParent;
    }

    /**
     * Only do stuff regarding this object itself, send the packets and stuff at someplace else. This should only ever
     * be called from the the MothershipWorldProvider!
     *
     * @param target
     * @return
     */
    public boolean startTransit(final CelestialBody target, final long travelTime) {
        if (!canBeOrbited(target) || this.isInTransit() || travelTime > AmunRa.config.mothershipMaxTravelTime) {
            return false;
        }

        AmunRa.LOGGER.debug("Mothership {} will begin transit to {}", this.getID(), target.getName());

        // allow change of route in mid-transit, too
        this.inTransit = true;
        this.travelTimeTotal = travelTime;
        this.travelTimeRemaining = this.travelTimeTotal;
        this.previousParent = this.currentParent;
        this.currentParent = target;
        TickHandlerServer.mothershipData.updateOrbitsFor(this.previousParent);
        // mark the MS data dirty here?

        return true;
    }

    /**
     * This should only ever be called from the the MothershipWorldProvider!
     *
     * @return
     */
    public boolean endTransit() {
        if (!this.inTransit) {
            return false;
        }
        AmunRa.LOGGER.info("Mothership {} finished transit", this.getID());
        this.previousParent = null;
        this.travelTimeRemaining = 0;
        this.travelTimeTotal = 0;
        this.inTransit = false;
        TickHandlerServer.mothershipData.updateOrbitsFor(this.currentParent);

        return true;
    }

    public boolean forceArrival() {
        if (!this.inTransit) {
            return false;
        }
        this.travelTimeRemaining = 0;
        return true;
    }

    /**
     * Gets the MothershipWorldProvider of this mothership. Will probably load the world if it isn't. Server only.
     *
     * @return
     */
    // @ SideOnly(Side.SERVER)
    public MothershipWorldProvider getWorldProviderServer() {
        final MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
        final WorldServer ws = mcServer.worldServerForDimension(this.getDimensionID());
        if (ws == null || !(ws.provider instanceof MothershipWorldProvider)) {
            return null;
        }
        return (MothershipWorldProvider) ws.provider;
    }

    /**
     * Basically checks if the current world is the world of this MS, if yes, returns the provider. Client only.
     *
     * @return
     */
    @SideOnly(Side.CLIENT)
    public MothershipWorldProvider getWorldProviderClient() {
        final World ws = ClientProxyCore.mc.theWorld;
        if (ws != null && ws.provider.dimensionId == this.getDimensionID()) {
            return (MothershipWorldProvider) ws.provider;
        }
        return null;
    }

    public double getTravelDistanceTo(final CelestialBody target) {
        return AstronomyHelper.getDistance(this.currentParent, target);
    }

    /**
     * For rendering bars and such
     *
     * @param barLength
     * @return
     */
    public int getScaledTravelTime(final int barLength) {
        final float remain = this.getRemainingTravelTime();
        final float total = this.getTotalTravelTime();
        final float relative = remain / total;
        final float scaled = (1 - relative) * barLength;
        return (int) scaled;
    }

    public PlayerID getOwner() {
        return this.owner;
    }

    @Override
    public int getID() {
        return this.mothershipId;
    }

    @Override
    public String getUnlocalizedNamePrefix() {
        return "mothership";
    }

    public static boolean canBeOrbited(final CelestialBody body) {
        return AmunRa.config.mothershipMaxTier >= body.getTierRequirement() && body instanceof Planet
                || body instanceof Moon
                || body instanceof Star && !AmunRa.config.mothershipBodiesNoOrbit.contains(body.getName());
    }

    public CelestialBody setDimensionInfo(final int dimID) {
        return this.setDimensionInfo(dimID, MothershipWorldProvider.class);
    }

    /**
     * Finds mothership-able bodies by "url", aka "solarSystem\planet\moon"
     *
     * @param bodyName
     * @return
     */
    public static CelestialBody findBodyByNamePath(final String bodyName) {

        SolarSystem curSys = null;
        CelestialBody body = null;
        CelestialBody moon = null;

        final String[] parts = bodyName.split(Pattern.quote(nameSeparator));

        for (int i = 0; i < parts.length; i++) {
            switch (i) {
                case 0:
                    curSys = GalaxyRegistry.getRegisteredSolarSystems().get(parts[i]);
                    body = curSys.getMainStar();
                    break;
                case 1:
                    body = GalaxyRegistry.getRegisteredPlanets().get(parts[i]);
                    // sanity check
                    if (!((Planet) body).getParentSolarSystem().equals(curSys)) {
                        throw new RuntimeException("Planet " + body.getName() + " is not in " + bodyName);
                    }
                    break;
                case 2:
                    moon = GalaxyRegistry.getRegisteredMoons().get(parts[i]);
                    // sanity checks
                    if (!((Moon) moon).getParentPlanet().equals(body)) {
                        throw new RuntimeException("Moon " + moon.getName() + " is not in " + bodyName);
                    }
                    // at this point, we are done anyway
                    return moon;
            }
        }
        if (body == null) {
            throw new RuntimeException("Could not find body for " + bodyName);
        }
        return body;
    }

    /**
     * Finds mothership-able bodies by galacticraft body name, aka the english name...
     */
    public static CelestialBody findBodyByGCBodyName(final String bodyName) {
        final Collection<SolarSystem> sysList = GalaxyRegistry.getRegisteredSolarSystems().values();
        CelestialBody body;
        for (final SolarSystem sys : sysList) {
            body = sys.getMainStar();
            if (body.getName().equals(bodyName)) {
                return body;
            }
        }
        body = GalaxyRegistry.getRegisteredPlanets().get(bodyName);
        if (body != null) {
            return body;
        }

        return GalaxyRegistry.getRegisteredMoons().get(bodyName);
    }

    public static CelestialBody findBodyByString(final String str) {
        if (str.contains(nameSeparator)) {
            return findBodyByNamePath(str);
        }
        return findBodyByGCBodyName(str);
    }

    public static Mothership createFromNBT(final NBTTagCompound data) {
        if (!data.hasKey("id") || !data.hasKey("owner")) {
            throw new RuntimeException("Invalid Mothership!");
        }
        final int id = data.getInteger("id");
        final String ownerUUID = data.getString("owner");
        final String ownerName = data.getString("ownerName");

        final Mothership result = new Mothership(id, UUID.fromString(ownerUUID), ownerName);

        // these must always be set, a mothership is invalid without

        final String parentId = data.getString("parentName");
        final CelestialBody foundParent = findBodyByNamePath(parentId);

        final String prevParentId = data.getString("prevParentName");
        if (!prevParentId.isEmpty()) {
            result.previousParent = findBodyByNamePath(prevParentId);
        }

        result.currentParent = foundParent;
        result.inTransit = data.getBoolean("inTransit");
        result.travelTimeRemaining = data.getLong("travelTimeRemaining");
        result.travelTimeTotal = data.getLong("travelTimeTotal");

        result.setDimensionInfo(data.getInteger("dim"));
        result.isReachable = true;

        result.readSettingsFromNBT(data);

        return result;
    }

    public void writeToNBT(final NBTTagCompound data) {
        data.setString("owner", this.owner.getUUID().toString());
        data.setString("ownerName", this.owner.getName());
        data.setInteger("id", this.mothershipId);
        data.setInteger("dim", this.dimensionID);

        final String parentId = AstronomyHelper.getOrbitableBodyName(this.currentParent);
        data.setString("parentName", parentId);

        if (this.previousParent != null) {
            final String prevParentId = AstronomyHelper.getOrbitableBodyName(this.previousParent);
            data.setString("prevParentName", prevParentId);
        }

        data.setBoolean("inTransit", this.inTransit);
        data.setLong("travelTimeRemaining", this.travelTimeRemaining);
        data.setLong("travelTimeTotal", this.travelTimeTotal);

        this.writeSettingsToNBT(data);
    }

    /**
     * "Settings" are things which a player can change
     */
    public void readSettingsFromNBT(final NBTTagCompound data) {
        if (data.hasKey("bodyIcon")) {
            this.setBodyIcon(new ResourceLocation(data.getString("bodyIcon")));
        }
        if (data.hasKey("name")) {
            this.msName = data.getString("name");
        }

        NBTTagList list = data.getTagList("playerList", Constants.NBT.TAG_COMPOUND);
        this.playerSetLanding = new HashSet<>();
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound playerData = list.getCompoundTagAt(i);
            final PlayerID pd = new PlayerID(playerData);
            this.playerSetLanding.add(pd);
        }

        if (data.hasKey("permissionMode")) {
            final int modeIndex = data.getInteger("permissionMode");
            this.permModeLanding = PermissionMode.values()[modeIndex];
        }

        list = data.getTagList("playerListUse", Constants.NBT.TAG_COMPOUND);
        this.playerSetUsage = new HashSet<>();
        for (int i = 0; i < list.tagCount(); i++) {
            final NBTTagCompound playerData = list.getCompoundTagAt(i);
            final PlayerID pd = new PlayerID(playerData);
            this.playerSetUsage.add(pd);
        }

        if (data.hasKey("permissionModeUse")) {
            final int modeIndex = data.getInteger("permissionModeUse");
            this.permModeUsage = PermissionMode.values()[modeIndex];
        }
    }

    public void writeSettingsToNBT(final NBTTagCompound data) {
        data.setString("bodyIcon", this.getBodyIcon().toString());
        data.setString("name", this.msName);

        NBTTagList list = new NBTTagList();

        for (final PlayerID p : this.playerSetLanding) {
            list.appendTag(p.getNbt());
        }
        data.setTag("playerList", list);

        list = new NBTTagList();

        for (final PlayerID p : this.playerSetUsage) {
            list.appendTag(p.getNbt());
        }
        data.setTag("playerListUse", list);

        data.setInteger("permissionMode", this.permModeLanding.ordinal());
        data.setInteger("permissionModeUse", this.permModeUsage.ordinal());
    }

    public long getTotalTravelTime() {
        return this.travelTimeTotal;
    }

    public long getRemainingTravelTime() {
        return this.travelTimeRemaining;
    }

    public long modRemainingTravelTime(final int mod) {
        this.travelTimeRemaining += mod;
        return this.travelTimeRemaining;
    }

    public void setRemainingTravelTime(final int set) {
        this.travelTimeRemaining = set;
    }

    /**
     * Returns whenever the given player is the owner of this mothership
     */
    public boolean isPlayerOwner(final EntityPlayer player) {
        return this.owner.isSameUser(player);
    }

    /**
     * Returns whenever the given player is the owner of this mothership
     */
    public boolean isPlayerOwner(final PlayerID player) {
        return this.owner.equals(player);
    }

    public boolean isPlayerUsagePermitted(final EntityPlayer player) {

        if (player.capabilities.isCreativeMode) {
            return true;
        }

        return this.isPlayerPermitted(player, this.permModeUsage);
    }

    /**
     * Returns whenever the given player is permitted to land on this mothership
     */
    public boolean isPlayerLandingPermitted(final EntityPlayer player) {

        if (player.capabilities.isCreativeMode) {
            return true;
        }

        return this.isPlayerPermitted(player, this.permModeLanding);
    }

    protected boolean isPlayerPermitted(final EntityPlayer player, final PermissionMode mode) {
        final PlayerID playerId = new PlayerID(player);

        return switch (this.permModeLanding) {
            case ALL -> true;
            case BLACKLIST -> this.isPlayerOwner(playerId) || !this.playerSetLanding.contains(playerId);
            case WHITELIST -> this.isPlayerOwner(playerId) || this.playerSetLanding.contains(playerId);
            case NONE -> this.isPlayerOwner(playerId);
            default -> this.isPlayerOwner(playerId);
        };
    }
}
