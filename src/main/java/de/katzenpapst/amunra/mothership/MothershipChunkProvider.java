package de.katzenpapst.amunra.mothership;

import java.util.Arrays;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;

import micdoodle8.mods.galacticraft.core.world.gen.BiomeGenBaseOrbit;

public class MothershipChunkProvider extends ChunkProviderGenerate { // for now, just like this

    protected final Random rand;
    protected final World worldObjNonPrivate;

    public MothershipChunkProvider(final World par1World, final long par2, final boolean par4) {
        super(par1World, par2, par4);
        this.rand = new Random(par2);
        this.worldObjNonPrivate = par1World;
    }

    @Override
    public boolean unloadQueuedChunks() {
        return false;
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    @Override
    public boolean saveChunks(boolean p_73151_1_, IProgressUpdate p_73151_2_) {
        return true;
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public Chunk provideChunk(int p_73154_1_, int p_73154_2_) {
        this.rand.setSeed(p_73154_1_ * 341873128712L + p_73154_2_ * 132897987541L);
        final Block[] ids = new Block[32768];
        Arrays.fill(ids, Blocks.air);
        final byte[] meta = new byte[32768];

        final Chunk chunk = new Chunk(this.worldObjNonPrivate, ids, meta, p_73154_1_, p_73154_2_);

        Arrays.fill(chunk.getBiomeArray(), (byte) BiomeGenBaseOrbit.space.biomeID);

        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public boolean chunkExists(int p_73149_1_, int p_73149_2_) {
        return true;
    }

    @Override
    public void populate(IChunkProvider p_73153_1_, int p_73153_2_, int p_73153_3_) {
        BlockFalling.fallInstantly = true;
        final int blockX = p_73153_2_ * 16;
        final int blockZ = p_73153_3_ * 16;
        this.rand.setSeed(this.worldObjNonPrivate.getSeed());
        final long seed1 = this.rand.nextLong() / 2L * 2L + 1L;
        final long seed2 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(p_73153_2_ * seed1 + p_73153_3_ * seed2 ^ this.worldObjNonPrivate.getSeed());
        if (blockX == 0 && blockZ == 0) {
            // this generates the basis structure
            new MothershipWorldGen().generate(this.worldObjNonPrivate, this.rand, 0, 64, 0);
        }
        BlockFalling.fallInstantly = false;
    }
}
