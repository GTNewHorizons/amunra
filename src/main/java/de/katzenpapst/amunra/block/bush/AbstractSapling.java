package de.katzenpapst.amunra.block.bush;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.TerrainGen;

import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

abstract public class AbstractSapling extends SubBlockBush {

    protected BlockMetaPair wood;
    protected BlockMetaPair leaves;

    public AbstractSapling(final String name, final String texture) {
        super(name, texture);
    }

    public AbstractSapling setWood(final BlockMetaPair wood) {
        this.wood = wood;
        return this;
    }

    public AbstractSapling setLeaves(final BlockMetaPair leaves) {
        this.leaves = leaves;
        return this;
    }

    @Override
    public void updateTick(World worldIn, int x, int y, int z, Random random) {
        if (worldIn.getBlockLightValue(x, y + 1, z) >= 9 && random.nextInt(7) == 0) {
            this.prepareGrowTree(worldIn, x, y, z, random);
        }
    }

    public void prepareGrowTree(final World worldIn, final int x, final int y, final int z, final Random random) {
        final int meta = worldIn.getBlockMetadata(x, y, z);

        if ((meta & 8) == 0) {
            worldIn.setBlockMetadataWithNotify(x, y, z, meta | 8, 4);
        } else {
            this.growTree(worldIn, x, y, z, random);
        }
    }

    public void growTree(final World worldIn, final int x, final int y, final int z, final Random random) {
        if (!TerrainGen.saplingGrowTree(worldIn, random, x, y, z)) return;

        final int meta = worldIn.getBlockMetadata(x, y, z) & 7;

        // self-removal before tree generation
        worldIn.setBlock(x, y, z, Blocks.air, 0, 4);
        if (!this.generate(worldIn, random, x, y, z, true)) {
            // return self on failure
            worldIn.setBlock(x, y, z, (Block) this.parent, meta, 4);
        }
    }

    public boolean checkBlockAt(final World world, final int x, final int y, final int z, final int metadata) {
        return world.getBlock(x, y, z) == this && (world.getBlockMetadata(x, y, z) & 7) == metadata;
    }

    @Override
    public boolean func_149851_a(World worldIn, int x, int y, int z, boolean isClient) {
        return true;
    }

    @Override
    public boolean func_149852_a(World worldIn, Random random, int x, int y, int z) {
        return worldIn.rand.nextFloat() < 0.45D;
    }

    @Override
    public void func_149853_b(World worldIn, Random random, int x, int y, int z) {
        this.prepareGrowTree(worldIn, x, y, z, random);
    }

    protected boolean isBlockReplaceable(final Block block) {
        return block instanceof BlockBushMulti || block.getMaterial() == Material.air
                || block.getMaterial() == Material.leaves
                || block == Blocks.grass
                || block == Blocks.dirt
                || block == Blocks.log
                || block == Blocks.log2
                || block == Blocks.sapling
                || block == Blocks.vine;
    }

    protected boolean canReplaceBlock(final World world, final int x, final int y, final int z) {
        final Block block = world.getBlock(x, y, z);
        return block.isAir(world, x, y, z) || block.isLeaves(world, x, y, z)
                || block.isWood(world, x, y, z)
                || this.isBlockReplaceable(block);
    }

    protected void setBlockAndNotifyAdequately(final World world, final int x, final int y, final int z,
            final Block block, final int meta, final boolean notify) {
        if (notify) {
            world.setBlock(x, y, z, block, meta, 3);
        } else {
            world.setBlock(x, y, z, block, meta, 2);
        }
    }

    abstract public boolean generate(World world, Random rand, int x, int y, int z, boolean notify);

}
