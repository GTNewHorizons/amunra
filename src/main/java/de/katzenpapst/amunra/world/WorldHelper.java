package de.katzenpapst.amunra.world;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import de.katzenpapst.amunra.vec.Vector3int;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class WorldHelper {

    public static BlockMetaPair getBlockMetaPair(final World world, final int x, final int y, final int z) {
        return new BlockMetaPair(world.getBlock(x, y, z), (byte) world.getBlockMetadata(x, y, z));
    }

    public static boolean isBlockMetaPair(final World world, final int x, final int y, final int z,
            final BlockMetaPair bmp) {
        return world.getBlock(x, y, z) == bmp.getBlock() && world.getBlockMetadata(x, y, z) == bmp.getMetadata();
    }

    /**
     * Drop entity in world, copy over tag compound, too
     */
    public static void dropItemInWorld(final World world, final ItemStack stack, final double x, final double y,
            final double z, final double motionX, final double motionY, final double motionZ) {
        final EntityItem itemEntity = new EntityItem(
                world,
                x,
                y,
                z,
                new ItemStack(stack.getItem(), stack.stackSize, stack.getItemDamage()));

        if (stack.hasTagCompound()) {
            itemEntity.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
        }

        itemEntity.motionX = motionX;
        itemEntity.motionY = motionY;
        itemEntity.motionZ = motionZ;
        world.spawnEntityInWorld(itemEntity);
    }

    public static void dropItemInWorld(final World world, final ItemStack stack, final double x, final double y,
            final double z) {
        dropItemInWorld(world, stack, x, y, z, 0, 0, 0);
    }

    public static void dropItemInWorld(final World world, final ItemStack stack, final Entity atEntity) {
        dropItemInWorld(world, stack, atEntity.posX, atEntity.posY, atEntity.posZ, 0, 0, 0);
    }

    /**
     * Attempts to ignite the block at the given position from the given direction
     */
    public static void setFireToBlock(final World worldObj, final int x, final int y, final int z, final double fromX,
            final double fromY, final double fromZ) {
        final double deltaX = x + 0.5 - fromX;
        final double deltaY = y + 0.5 - fromY;
        final double deltaZ = z + 0.5 - fromZ;

        final double deltaXabs = Math.abs(deltaX);
        final double deltaYabs = Math.abs(deltaY);
        final double deltaZabs = Math.abs(deltaZ);

        if (deltaXabs > deltaYabs) {
            if (deltaXabs > deltaZabs) {
                if (deltaX < 0) {
                    setBlockIfFree(worldObj, x + 1, y, z, Blocks.fire, 0);
                } else {
                    setBlockIfFree(worldObj, x - 1, y, z, Blocks.fire, 0);
                }
            } else if (deltaZ < 0) {
                setBlockIfFree(worldObj, x, y, z + 1, Blocks.fire, 0);
            } else {
                setBlockIfFree(worldObj, x, y, z - 1, Blocks.fire, 0);
            }
        } else if (deltaYabs > deltaZabs) {
            if (deltaY < 0) {
                setBlockIfFree(worldObj, x, y + 1, z, Blocks.fire, 0);
            } else {
                setBlockIfFree(worldObj, x, y - 1, z, Blocks.fire, 0);
            }
        } else if (deltaZ < 0) {
            setBlockIfFree(worldObj, x, y, z + 1, Blocks.fire, 0);
        } else {
            setBlockIfFree(worldObj, x, y, z - 1, Blocks.fire, 0);
        }
    }

    public static void setBlockIfFree(final World worldObj, final int x, final int y, final int z, final Block block,
            final int meta) {
        final Block old = worldObj.getBlock(x, y, z);
        if (old == Blocks.air) {
            worldObj.setBlock(x, y, z, block, meta, 3);
        }
    }

    /**
     * Returns true if the given block can be walked through. Will probably return false for fluids, too
     */
    public static boolean isSolid(final World worldObj, final int x, final int y, final int z, final boolean checkTop) {
        final Block b = worldObj.getBlock(x, y, z);
        if (checkTop) {
            return World.doesBlockHaveSolidTopSurface(worldObj, x, y, z);
        }
        // getBlocksMovement returns true when the block does NOT block movement...
        return !b.getBlocksMovement(worldObj, x, y, z) && b.getMaterial().isSolid();
    }

    /**
     * Returns true if the given block can be walked through
     */
    public static boolean isSolid(final World worldObj, final int x, final int y, final int z) {
        return isSolid(worldObj, x, y, z, false);
    }

    /**
     * Checks if given block is safe to place the player
     */
    public static boolean isNonSolid(final World worldObj, final int x, final int y, final int z) {
        final Block b = worldObj.getBlock(x, y, z);

        // so apparently block.getBlocksMovement does the opposite of what one might expect...

        return b.isAir(worldObj, x, y, z)
                || b.getBlocksMovement(worldObj, x, y, z) && !b.getMaterial().isLiquid() && !b.getMaterial().isSolid();
    }

    public static Vector3int getHighestNonEmptyBlock(final World world, final int minX, final int maxX, final int minY,
            final int maxY, final int minZ, final int maxZ) {

        for (int y = maxY; y >= minY; y--) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!isNonSolid(world, x, y, z)) {
                        return new Vector3int(x, y, z);
                    }
                }
            }
        }

        return null;
    }
}
