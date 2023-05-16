package de.katzenpapst.amunra.world;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import java.util.List;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.ChunkProviderSpace;
import micdoodle8.mods.galacticraft.core.world.gen.BiomeGenBaseOrbit;

abstract public class AmunraChunkProvider extends ChunkProviderSpace {

    public static final int CHUNK_SIZE_X = 16;
    public static final int CHUNK_SIZE_Y = 256;
    public static final int CHUNK_SIZE_Z = 16;
    
    protected BiomeGenBase[] biomes = {BiomeGenBaseOrbit.space};

    public AmunraChunkProvider(final World world, final long seed, final boolean mapFeaturesEnabled) {
        super(world, seed, mapFeaturesEnabled);
    }

    @Override
    public int getCraterProbability() {
        // vestigial
        return 2000;
    }
    
    @Override
    protected BiomeGenBase[] getBiomesForGeneration() {
        return this.biomes;
    }

    /**
     * I failed fixing this. I might do this as mapgen instead
     */
    @Override
    public void makeCrater(final int craterX, final int craterZ, final int chunkX, final int chunkZ, final int size,
            final Block[] chunkArray, final byte[] metaArray) {
        /*
         * final double centerFalloff = 0.01; final double borderFalloff = 0.02; final double centerHeightFactor = 0.5;
         * final double borderHeightFactor = 1; final double craterHeight = 9; final double borderStartLimit = 0.75;
         * double height = this.getSeaLevel(); double sizeSq = size * size; for (int x = 0; x <
         * AmunraChunkProvider.CHUNK_SIZE_X; x++) { for (int z = 0; z < AmunraChunkProvider.CHUNK_SIZE_Z; z++) { double
         * xDev = craterX - (chunkX + x); double zDev = craterZ - (chunkZ + z); if (xDev * xDev + zDev * zDev < sizeSq)
         * { xDev /= size; zDev /= size; // this is the distance from the crater's center, normed to size, squared final
         * double radiusSq = xDev * xDev + zDev * zDev; //final double borderFactor = radiusSq/sizeSq; final double
         * radius = Math.sqrt(radiusSq); double yDev = 0; // 0.2/((x^2+0.2)) + 0.2/(((x-3)^2+0.2)) // center // yDev +=
         * centerHeightFactor*centerFalloff/(radiusSq+centerFalloff); // border yDev +=
         * borderHeightFactor*borderFalloff/(Math.pow(borderStartLimit-radius, 2)+borderFalloff); yDev *= craterHeight;
         * yDev = height-(craterHeight-yDev); int highestY = this.getHighestNonAir(chunkArray, x, z); //if(radius >
         * borderStartLimit && yDev < highestY) { // yDev = this.fuckYouLerp(yDev, highestY, radius-borderStartLimit);
         * //} if(yDev>127) { yDev = 127; } if(yDev > highestY) { for(int y=(int)yDev;y>highestY;y--) { if (Blocks.air
         * == chunkArray[this.getIndex(x, y, z)]) { chunkArray[this.getIndex(x, y, z)] = getStoneBlock().getBlock();
         * metaArray[this.getIndex(x, y, z)] = getStoneBlock().getMetadata(); } } } else { for (int y = highestY; y >
         * yDev; y--) { if (Blocks.air != chunkArray[this.getIndex(x, y, z)]) { chunkArray[this.getIndex(x, y, z)] =
         * Blocks.air; metaArray[this.getIndex(x, y, z)] = 0; } } } } } }
         */
    }

    /**
     * Because private...
     */
    protected double lerp(final double d1, final double d2, final double t) {
        if (t < 0.0) {
            return d1;
        }
        if (t > 1.0) {
            return d2;
        }
        return d1 + (d2 - d1) * t;
    }

    protected int getIndex(final int x, final int y, final int z) {
        return (x * 16 + z) * 256 + y;
    }

}
