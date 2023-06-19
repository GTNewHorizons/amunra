package de.katzenpapst.amunra.world;

import java.util.Random;

import net.minecraft.block.Block;

import de.katzenpapst.amunra.helper.CoordHelper;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.ChunkProviderSpace;
import micdoodle8.mods.galacticraft.core.perlin.generator.Gradient;

/**
 * Basically this thing is just one wrapper for a copy of ChunkProviderSpace::generateTerrain, just that it isn't inside
 * a ChunkProviderSpace. I made this for Seth, which has basically two surfaces.
 *
 */
public class TerrainGenerator {

    protected final Gradient noiseGenBase;
    protected final Gradient noiseGenSmallHill;
    protected final Gradient noiseGenMountain;
    protected final Gradient noiseGenFeature;
    protected final Gradient noiseGenLargeFilter;
    protected final Gradient noiseGenValley;
    protected final Gradient noiseGenSmallFilter;

    protected final float heightModifier; // getHeightModifier
    protected final float smallFeatureHeightModifier; // getSmallFeatureHeightModifier
    protected final double mountainHeightMod; // getMountainHeightModifier
    protected final double valleyHeightMod; // getValleyHeightModifier
    protected final double seaLevel; // getSeaLevel

    // these are constants in the original
    protected final float mainFeatureFilterMod;
    protected final float largeFeatureFilterMod;
    protected final float smallFeatureFilterMod;

    protected final BlockMetaPair stoneBlock;
    protected final BlockMetaPair airBlock;

    protected final int maxHeight;

    protected Random rand;

    /**
     * Full constructor
     *
     * @param rand                  The Random object
     * @param stoneBlock            The Block(MetaPair) to use for stone
     * @param airBlock              The Block(MetaPair) to fill up the space above the stones
     * @param heightMod             General terrain height, usually from getHeightModifier
     * @param smallFeatureMod       Small hill height, usually from getSmallFeatureHeightModifier
     * @param mountainHeightMod     Mountain height, usually from getMountainHeightModifier
     * @param valleyHeightMod       Valley height (depth?), usually from getValleyHeightModifier
     * @param seaLevel              Medium height(?), usually from getSeaLevel
     * @param maxHeight             The space above the stones up to maxHeight gets filled up with airBlock. Also, no
     *                              terrain will be generated above this
     * @param mainFeatureFilterMod  Not sure, default = 4
     * @param largeFeatureFilterMod Not sure, default = 8
     * @param smallFeatureFilterMod Not sure, default = 8
     */
    public TerrainGenerator(final Random rand, final BlockMetaPair stoneBlock, final BlockMetaPair airBlock,
            final float heightMod, final float smallFeatureMod, final double mountainHeightMod,
            final double valleyHeightMod, final double seaLevel, final int maxHeight, final float mainFeatureFilterMod,
            final float largeFeatureFilterMod, final float smallFeatureFilterMod) {
        this.rand = rand;

        this.stoneBlock = stoneBlock;
        this.airBlock = airBlock;

        this.maxHeight = maxHeight;

        this.noiseGenBase = new Gradient(this.rand.nextLong(), 4, 0.25F);
        this.noiseGenSmallHill = new Gradient(this.rand.nextLong(), 4, 0.25F);
        this.noiseGenMountain = new Gradient(this.rand.nextLong(), 4, 0.25F);
        this.noiseGenValley = new Gradient(this.rand.nextLong(), 2, 0.25F);
        this.noiseGenFeature = new Gradient(this.rand.nextLong(), 1, 0.25F);
        this.noiseGenLargeFilter = new Gradient(this.rand.nextLong(), 1, 0.25F);
        this.noiseGenSmallFilter = new Gradient(this.rand.nextLong(), 1, 0.25F);

        this.heightModifier = heightMod;
        this.smallFeatureHeightModifier = smallFeatureMod;
        this.valleyHeightMod = valleyHeightMod;
        this.seaLevel = seaLevel;
        this.mountainHeightMod = mountainHeightMod;

        this.mainFeatureFilterMod = mainFeatureFilterMod;
        this.largeFeatureFilterMod = largeFeatureFilterMod;
        this.smallFeatureFilterMod = smallFeatureFilterMod;
    }

    /**
     * "Light" constructor. The other 3 values are set to 4, 8, 8, since that's their values in ChunkProviderSpace
     */
    public TerrainGenerator(final Random rand, final BlockMetaPair stoneBlock, final BlockMetaPair airBlock,
            final float heightMod, final float smallFeatureMod, final double mountainHeightMod,
            final double valleyHeightMod, final double seaLevel, final int maxHeight) {
        this(
                rand,
                stoneBlock,
                airBlock,
                heightMod,
                smallFeatureMod,
                mountainHeightMod,
                valleyHeightMod,
                seaLevel,
                maxHeight,
                4,
                8,
                8);
    }

    /**
     * Even "lighter" constructor. The other 4 values are set to 255, 4, 8, 8, since that's their values in
     * ChunkProviderSpace
     */
    public TerrainGenerator(final Random rand, final BlockMetaPair stoneBlock, final BlockMetaPair airBlock,
            final float heightMod, final float smallFeatureMod, final double mountainHeightMod,
            final double valleyHeightMod, final double seaLevel) {
        this(
                rand,
                stoneBlock,
                airBlock,
                heightMod,
                smallFeatureMod,
                mountainHeightMod,
                valleyHeightMod,
                seaLevel,
                255,
                4,
                8,
                8);
    }

    /**
     * Basically a clone of {@link ChunkProviderSpace#generateTerrain(int, int, Block[], byte[])} I just need it in a
     * more configurable form
     *
     */
    public void generateTerrain(final int chunkX, final int chunkZ, final Block[] idArray, final byte[] metaArray) {
        this.noiseGenBase.setFrequency(0.015F);
        this.noiseGenSmallHill.setFrequency(0.01F);
        this.noiseGenMountain.setFrequency(0.01F);
        this.noiseGenValley.setFrequency(0.01F);
        this.noiseGenFeature.setFrequency(0.01F);
        this.noiseGenLargeFilter.setFrequency(0.001F);
        this.noiseGenSmallFilter.setFrequency(0.005F);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // magic
                final double baseHeight = this.noiseGenBase.getNoise(chunkX * 16 + x, chunkZ * 16 + z)
                        * this.heightModifier;
                final double smallHillHeight = this.noiseGenSmallHill.getNoise(chunkX * 16 + x, chunkZ * 16 + z)
                        * this.smallFeatureHeightModifier;
                double mountainHeight = Math.abs(this.noiseGenMountain.getNoise(chunkX * 16 + x, chunkZ * 16 + z));
                double valleyHeight = Math.abs(this.noiseGenValley.getNoise(chunkX * 16 + x, chunkZ * 16 + z));
                final double featureFilter = this.noiseGenFeature.getNoise(chunkX * 16 + x, chunkZ * 16 + z)
                        * this.mainFeatureFilterMod;
                final double largeFilter = this.noiseGenLargeFilter.getNoise(chunkX * 16 + x, chunkZ * 16 + z)
                        * this.largeFeatureFilterMod;
                final double smallFilter = this.noiseGenSmallFilter.getNoise(chunkX * 16 + x, chunkZ * 16 + z)
                        * this.smallFeatureFilterMod - 0.5;
                mountainHeight = this.lerp(
                        smallHillHeight,
                        mountainHeight * this.mountainHeightMod,
                        this.fade(this.clamp(mountainHeight * 2, 0, 1)));
                valleyHeight = this.lerp(
                        smallHillHeight,
                        valleyHeight * this.valleyHeightMod - this.valleyHeightMod + 9,
                        this.fade(this.clamp((valleyHeight + 2) * 4, 0, 1)));

                double yDev = this.lerp(valleyHeight, mountainHeight, this.fade(largeFilter));
                yDev = this.lerp(smallHillHeight, yDev, smallFilter);
                yDev = this.lerp(baseHeight, yDev, featureFilter);

                for (int y = 0; y <= this.maxHeight; y++) {
                    final int index = CoordHelper.getIndex(x, y, z);
                    if (y < this.seaLevel + yDev) {
                        idArray[index] = this.stoneBlock.getBlock();
                        metaArray[index] = this.stoneBlock.getMetadata();
                    } else {
                        idArray[index] = this.airBlock.getBlock();
                        metaArray[index] = this.airBlock.getMetadata();
                    }
                }
            }
        }
    }

    protected double lerp(final double d1, final double d2, final double t) {
        if (t < 0.0) {
            return d1;
        }
        if (t > 1.0) {
            return d2;
        }
        return d1 + (d2 - d1) * t;
    }

    protected double clamp(final double x, final double min, final double max) {
        if (x < min) {
            return min;
        }
        return Math.min(x, max);
    }

    protected double fade(final double n) {
        return n * n * n * (n * (n * 6 - 15) + 10);
    }
}
