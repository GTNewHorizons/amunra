package de.katzenpapst.amunra.world.asteroidWorld;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.astronomy.AngleDistance;
import de.katzenpapst.amunra.helper.AstronomyHelper;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.vector.BlockVec3;
import micdoodle8.mods.galacticraft.planets.asteroids.dimension.WorldProviderAsteroids;
import micdoodle8.mods.galacticraft.planets.asteroids.entities.EntityAstroMiner;

abstract public class AmunRaAsteroidWorldProvider extends WorldProviderAsteroids {

    // Used to list asteroid centres to external code that needs to know them
    protected final Set<AsteroidData> asteroids = new HashSet<>();
    protected boolean dataNotLoaded = true;
    protected AsteroidSaveData datafile;
    protected double solarMultiplier = -1D;

    @Override
    abstract public CelestialBody getCelestialBody();

    @Override
    abstract public Class<? extends IChunkProvider> getChunkProviderClass();

    @Override
    abstract public Class<? extends WorldChunkManager> getWorldChunkManagerClass();

    protected float getRelativeGravity() {
        return 0.1F;
    }

    @Override
    public float getGravity() {
        return 0.072F; // this is equivalent to 0.1
    }

    @Override
    public float getFallDamageModifier() {
        return this.getRelativeGravity();
    }

    @Override
    public double getMeteorFrequency() {
        return 10.0D;
    }

    @Override
    public double getFuelUsageMultiplier() {
        return this.getRelativeGravity();
    }

    @Override
    public float calculateCelestialAngle(long p_76563_1_, float p_76563_3_) {
        return 0.0F;
    }

    @Override
    public boolean canSpaceshipTierPass(final int tier) {
        return tier >= AmunRa.config.planetDefaultTier;
    }

    /**
     * This is the part which makes the world brighter or dimmer
     */
    @SideOnly(Side.CLIENT)
    @Override
    public float getSunBrightness(final float partialTicks) {
        final float factor = this.worldObj.getSunBrightnessBody(partialTicks)
                + this.getAmunBrightnessFactor(partialTicks);
        return factor > 1.0f ? 1.0f : factor;
    }

    /**
     * TODO do something
     */
    protected float getAmunBrightnessFactor(final float partialTicks) {
        CelestialBody curBody = this.getCelestialBody();
        if (curBody instanceof Moon) {
            curBody = ((Moon) curBody).getParentPlanet();
        }
        final AngleDistance ad = AstronomyHelper
                .projectBodyToSky(curBody, AmunRa.instance.starAmun, partialTicks, this.worldObj.getWorldTime());
        // ad.angle is in pi

        // the angle I get is relative to celestialAngle
        float brightnessFactor = 1.0F
                - (MathHelper.cos(this.worldObj.getCelestialAngle(partialTicks) * (float) Math.PI * 2.0F + ad.angle)
                        * 2.0F + 0.5F);

        if (brightnessFactor < 0) {
            brightnessFactor = 0;
        }
        if (brightnessFactor > 1) {
            brightnessFactor = 1;
        }

        brightnessFactor = 1.0F - brightnessFactor;

        // let's say brightnessFactor == 1 -> 0.5 of brightness
        return (float) (brightnessFactor * 0.8 / ad.distance);
    }

    @Override
    public float getSolarSize() {
        final CelestialBody body = this.getCelestialBody();

        if (body instanceof Moon) {
            return 1.0F / ((Moon) body).getParentPlanet().getRelativeDistanceFromCenter().unScaledDistance;
        }
        return 1.0F / body.getRelativeDistanceFromCenter().unScaledDistance;
    }

    @Override
    public float getThermalLevelModifier() {
        return -0.5F;
    }

    @Override
    public void addAsteroid(final int x, final int y, final int z, final int size, final int core) {
        final AsteroidData coords = new AsteroidData(x, y, z, size, core);
        if (!this.asteroids.contains(coords)) {
            if (this.dataNotLoaded) {
                this.loadAsteroidSavedData();
            }
            if (!this.asteroids.contains(coords)) {
                this.addToNBT(this.datafile.datacompound, coords);
                this.asteroids.add(coords);
            }
        }
    }

    @Override
    public void removeAsteroid(final int x, final int y, final int z) {
        final AsteroidData coords = new AsteroidData(x, y, z);
        if (this.asteroids.contains(coords)) {
            this.asteroids.remove(coords);

            if (this.dataNotLoaded) {
                this.loadAsteroidSavedData();
            }
            this.writeToNBT(this.datafile.datacompound);
        }
    }

    abstract public String getSaveDataID();

    protected void loadAsteroidSavedData() {
        this.datafile = (AsteroidSaveData) this.worldObj.loadItemData(AsteroidSaveData.class, this.getSaveDataID());

        if (this.datafile == null) {
            this.datafile = new AsteroidSaveData(this.getSaveDataID());
            this.worldObj.setItemData(this.getSaveDataID(), this.datafile);
            this.writeToNBT(this.datafile.datacompound);
        } else {
            this.readFromNBT(this.datafile.datacompound);
        }

        this.dataNotLoaded = false;
    }

    protected void ensureDataLoaded() {
        if (this.dataNotLoaded) {
            this.loadAsteroidSavedData();
        }
    }

    protected void readFromNBT(final NBTTagCompound nbt) {
        final NBTTagList coordList = nbt.getTagList("coords", 10);
        if (coordList.tagCount() > 0) {
            for (int j = 0; j < coordList.tagCount(); j++) {
                final NBTTagCompound tag1 = coordList.getCompoundTagAt(j);

                if (tag1 != null) {
                    this.asteroids.add(AsteroidData.readFromNBT(tag1));
                }
            }
        }
    }

    protected void writeToNBT(final NBTTagCompound nbt) {
        final NBTTagList coordList = new NBTTagList();
        for (final AsteroidData coords : this.asteroids) {
            final NBTTagCompound tag = new NBTTagCompound();
            coords.writeToNBT(tag);
            coordList.appendTag(tag);
        }
        nbt.setTag("coords", coordList);
        this.datafile.markDirty();
    }

    protected void addToNBT(final NBTTagCompound nbt, final AsteroidData coords) {
        final NBTTagList coordList = nbt.getTagList("coords", 10);
        final NBTTagCompound tag = new NBTTagCompound();
        coords.writeToNBT(tag);
        coordList.appendTag(tag);
        nbt.setTag("coords", coordList);
        this.datafile.markDirty();
    }

    @Override
    public BlockVec3 getClosestAsteroidXZ(final int x, final int y, final int z) {
        this.ensureDataLoaded();

        if (this.asteroids.size() == 0) {
            return null;
        }

        BlockVec3 result = null;
        AsteroidData resultRoid = null;
        int lowestDistance = Integer.MAX_VALUE;

        for (final AsteroidData test : this.asteroids) {
            // if this flag is set, then don't?
            if ((test.sizeAndLandedFlag & 128) != 0) // wtf? It's 1 << 7, but why?
                continue;

            final int dx = x - test.centre.x;
            final int dz = z - test.centre.z;
            final int a = dx * dx + dz * dz;
            if (a < lowestDistance) {
                lowestDistance = a;
                result = test.centre;
                resultRoid = test;
            }
        }

        if (result == null) return null;

        // set the flag?
        resultRoid.sizeAndLandedFlag |= 128; // why?
        this.writeToNBT(this.datafile.datacompound);
        return result.clone();
    }

    /**
     * This seems to be for AstroMiner
     */
    @Override
    public ArrayList<BlockVec3> getClosestAsteroidsXZ(final int x, final int y, final int z, final int facing,
            final int count) {
        if (this.dataNotLoaded) {
            this.loadAsteroidSavedData();
        }

        if (this.asteroids.size() == 0) {
            return null;
        }

        final TreeMap<Integer, BlockVec3> targets = new TreeMap<>();

        for (final AsteroidData roid : this.asteroids) {
            final BlockVec3 test = roid.centre;
            switch (facing) {
                case 2:
                    if (z - 16 < test.z) continue;
                    break;
                case 3:
                    if (z + 16 > test.z) continue;
                    break;
                case 4:
                    if (x - 16 < test.x) continue;
                    break;
                case 5:
                    if (x + 16 > test.x) continue;
                    break;
            }
            final int dx = x - test.x;
            final int dz = z - test.z;
            final int a = dx * dx + dz * dz;
            if (a < 262144) targets.put(a, test);
        }

        final int max = Math.max(count, targets.size());
        if (max <= 0) return null;

        final ArrayList<BlockVec3> returnValues = new ArrayList<>();
        int i = 0;
        final int offset = EntityAstroMiner.MINE_LENGTH_AST / 2;
        for (final BlockVec3 target : targets.values()) {
            final BlockVec3 coords = target.clone();
            AmunRa.LOGGER.debug("Found nearby asteroid at {}", target);
            switch (facing) {
                case 2:
                    coords.z += offset;
                    break;
                case 3:
                    coords.z -= offset;
                    break;
                case 4:
                    coords.x += offset;
                    break;
                case 5:
                    coords.x -= offset;
                    break;
            }
            returnValues.add(coords);
            i++;
            if (i >= count) break;
        }

        return returnValues;
    }

    @Override
    public void registerWorldChunkManager() {
        super.registerWorldChunkManager();
        this.hasNoSky = true;
    }

    @Override
    public double getSolarEnergyMultiplier() {
        if (this.solarMultiplier < 0D) {
            this.solarMultiplier = AstronomyHelper
                    .getSolarEnergyMultiplier(this.getCelestialBody(), !this.getCelestialBody().atmosphere.isEmpty());
        }
        return this.solarMultiplier;
    }

    protected static class AsteroidData {

        protected BlockVec3 centre;
        protected int sizeAndLandedFlag = 15;
        protected int coreAndSpawnedFlag = -2;

        public AsteroidData(final int x, final int y, final int z) {
            this.centre = new BlockVec3(x, y, z);
        }

        public AsteroidData(final int x, final int y, final int z, final int size, final int core) {
            this.centre = new BlockVec3(x, y, z);
            this.sizeAndLandedFlag = size;
            this.coreAndSpawnedFlag = core;
        }

        public AsteroidData(final BlockVec3 bv) {
            this.centre = bv;
        }

        @Override
        public int hashCode() {
            if (this.centre != null) return this.centre.hashCode();
            return 0;
        }

        @Override
        public boolean equals(final Object o) {
            if (o instanceof AsteroidData data) {
                final BlockVec3 vector = data.centre;
                return this.centre.x == vector.x && this.centre.y == vector.y && this.centre.z == vector.z;
            }

            if (o instanceof BlockVec3 vector) {
                return this.centre.x == vector.x && this.centre.y == vector.y && this.centre.z == vector.z;
            }

            return false;
        }

        public NBTTagCompound writeToNBT(final NBTTagCompound tag) {
            tag.setInteger("x", this.centre.x);
            tag.setInteger("y", this.centre.y);
            tag.setInteger("z", this.centre.z);
            tag.setInteger("coreAndFlag", this.coreAndSpawnedFlag);
            tag.setInteger("sizeAndFlag", this.sizeAndLandedFlag);
            return tag;
        }

        public static AsteroidData readFromNBT(final NBTTagCompound tag) {
            final BlockVec3 tempVector = new BlockVec3();
            tempVector.x = tag.getInteger("x");
            tempVector.y = tag.getInteger("y");
            tempVector.z = tag.getInteger("z");

            final AsteroidData roid = new AsteroidData(tempVector);
            if (tag.hasKey("coreAndFlag")) roid.coreAndSpawnedFlag = tag.getInteger("coreAndFlag");
            if (tag.hasKey("sizeAndFlag")) roid.sizeAndLandedFlag = tag.getInteger("sizeAndFlag");

            return roid;
        }
    }

    public class AsteroidSaveData extends WorldSavedData {

        public NBTTagCompound datacompound;

        public AsteroidSaveData(final String s) {
            super(s);
            this.datacompound = new NBTTagCompound();
        }

        @Override
        public void readFromNBT(final NBTTagCompound nbt) {
            this.datacompound = nbt.getCompoundTag("asteroids");
        }

        @Override
        public void writeToNBT(final NBTTagCompound nbt) {
            nbt.setTag("asteroids", this.datacompound);
        }
    }

}
