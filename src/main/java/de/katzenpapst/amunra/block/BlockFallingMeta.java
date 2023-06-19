package de.katzenpapst.amunra.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BlockFallingMeta extends BlockBasicMeta {

    public BlockFallingMeta(final String name, final Material mat) {
        super(name, mat);
    }

    @Override
    public void onBlockAdded(World worldIn, int x, int y, int z) {
        worldIn.scheduleBlockUpdate(x, y, z, this, this.tickRate(worldIn));
    }

    @Override
    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        worldIn.scheduleBlockUpdate(x, y, z, this, this.tickRate(worldIn));
    }

    @Override
    public void updateTick(World worldIn, int x, int y, int z, Random random) {
        if (!worldIn.isRemote) {
            this.doTheFalling(worldIn, x, y, z);
        }
    }

    private void doTheFalling(final World world, final int x, int y, final int z) {
        if (canContinueFalling(world, x, y - 1, z) && y >= 0) {
            final int b0 = 32;
            // It might be a good idea to check for the value of the official falling block...
            if (!BlockFalling.fallInstantly && world.checkChunksExist(x - b0, y - b0, z - b0, x + b0, y + b0, z + b0)) {
                if (!world.isRemote) {
                    final EntityFallingBlock entityfallingblock = new EntityFallingBlock(
                            world,
                            x + 0.5F,
                            y + 0.5F,
                            z + 0.5F,
                            this,
                            world.getBlockMetadata(x, y, z));
                    this.onEntityCreated(entityfallingblock);
                    world.spawnEntityInWorld(entityfallingblock);
                }
            } else {
                world.setBlockToAir(x, y, z);

                while (canContinueFalling(world, x, y - 1, z) && y > 0) {
                    --y;
                }

                if (y > 0) {
                    world.setBlock(x, y, z, this);
                }
            }
        }
    }

    protected void onEntityCreated(final EntityFallingBlock entity) {}

    @Override
    public int tickRate(World worldIn) {
        return 2;
    }

    public static boolean canContinueFalling(final World world, final int x, final int y, final int z) {
        final Block block = world.getBlock(x, y, z);

        if (block.isAir(world, x, y, z)) {
            return true;
        }
        if (block == Blocks.fire) {
            return true;
        }
        // TODO figure out how it works for forge fluids
        final Material material = block.getMaterial();
        return material == Material.water || material == Material.lava;
    }

}
