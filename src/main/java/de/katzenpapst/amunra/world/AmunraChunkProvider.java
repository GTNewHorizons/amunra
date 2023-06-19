package de.katzenpapst.amunra.world;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.chunk.IChunkProvider;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.ChunkProviderSpace;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.MapGenBaseMeta;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedCreeper;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSkeleton;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedZombie;
import micdoodle8.mods.galacticraft.core.world.gen.BiomeGenBaseOrbit;

abstract public class AmunraChunkProvider extends ChunkProviderSpace {

    public static final int CHUNK_SIZE_X = 16;
    public static final int CHUNK_SIZE_Y = 256;
    public static final int CHUNK_SIZE_Z = 16;

    protected BiomeGenBase[] biomes = { BiomeGenBaseOrbit.space };
    protected SpawnListEntry[] creatures = {};
    protected SpawnListEntry[] monsters = { new SpawnListEntry(EntityEvolvedSkeleton.class, 100, 4, 4),
            new SpawnListEntry(EntityEvolvedCreeper.class, 100, 4, 4),
            new SpawnListEntry(EntityEvolvedZombie.class, 100, 4, 4) };
    protected final List<MapGenBaseMeta> worldGenerators = new ArrayList<>();

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

    @Override
    protected SpawnListEntry[] getCreatures() {
        return this.creatures;
    }

    @Override
    protected SpawnListEntry[] getMonsters() {
        return this.monsters;
    }

    @Override
    protected List<MapGenBaseMeta> getWorldGenerators() {
        return this.worldGenerators;
    }

    @Override
    public void onChunkProvide(int cX, int cZ, Block[] blocks, byte[] metadata) {}

    @Override
    public void onPopulate(IChunkProvider provider, int cX, int cZ) {}

    /**
     * I failed fixing this. I might do this as mapgen instead
     */
    @Override
    public void makeCrater(final int craterX, final int craterZ, final int chunkX, final int chunkZ, final int size,
            final Block[] chunkArray, final byte[] metaArray) {}

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
