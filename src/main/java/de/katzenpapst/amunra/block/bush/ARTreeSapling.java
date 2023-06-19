package de.katzenpapst.amunra.block.bush;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import de.katzenpapst.amunra.block.ARBlocks;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class ARTreeSapling extends AbstractSapling {

    protected int canopyWidth = 7;
    protected int canopyHeight = 9;
    protected int canopyCenter = 5;
    protected int minStemHeight = 2;

    public ARTreeSapling(final String name, final String texture) {
        super(name, texture);
    }

    @Override
    public boolean canPlaceOn(final BlockMetaPair blockToCheck, final int meta) {
        return this.canPlaceOn(blockToCheck.getBlock(), blockToCheck.getMetadata(), meta);
    }

    @Override
    public boolean canPlaceOn(final Block blockToCheck, final int metaToCheck, final int meta) {
        return blockToCheck == ARBlocks.blockMethaneDirt.getBlock()
                && metaToCheck == ARBlocks.blockMethaneDirt.getMetadata()
                || blockToCheck == ARBlocks.blockMethaneGrass.getBlock()
                        && metaToCheck == ARBlocks.blockMethaneGrass.getMetadata()
                || blockToCheck == ARBlocks.blockVacuumGrass.getBlock()
                        && metaToCheck == ARBlocks.blockVacuumGrass.getMetadata();
    }

    protected boolean canGenerateHere(final World world, final Random rand, final int x, final int y, final int z,
            final int stemHeight, final int halfCanopyWidth, final int canopyHeight) {
        // check stem first
        for (int curY = y; curY <= y + stemHeight; ++curY) {
            if (!this.canReplaceBlock(world, x, curY, z)) {
                return false;
            }
        }
        // just check the boundingbox for now
        for (int curY = y + stemHeight + 1; curY <= y + 1 + stemHeight + canopyHeight; ++curY) {

            for (int curX = x - halfCanopyWidth; curX <= x + halfCanopyWidth; ++curX) {
                for (int curZ = z - halfCanopyWidth; curZ <= z + halfCanopyWidth; ++curZ) {
                    if (curY < 0 || curY >= 256 || !this.canReplaceBlock(world, curX, curY, curZ)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Actual generation function. Can be called from worldgen
     *
     * @param notify apparently I have to pass false here on worldgen and true on sapling-growth
     */
    @Override
    public boolean generate(final World world, final Random rand, final int x, final int y, final int z,
            final boolean notify) {
        final int curTreeHeight = rand.nextInt(3) + this.minStemHeight + this.canopyHeight;

        final int halfWidth = (int) Math.ceil((this.canopyWidth - 1) / 2);
        final int halfHeight = (int) Math.ceil((this.canopyHeight - 1) / 2);
        final int stemHeight = curTreeHeight - this.canopyHeight;

        int highestYLeaf = 0;

        if (y >= 1 && y + curTreeHeight + 1 <= 256) {
            Block block;

            if (!this.canGenerateHere(world, rand, x, y, z, stemHeight, halfWidth, this.canopyHeight)) {
                return false;
            }

            final Block block2 = world.getBlock(x, y - 1, z);
            final int meta2 = world.getBlockMetadata(x, y - 1, z);

            final boolean isSoil = this.canPlaceOn(new BlockMetaPair(block2, (byte) meta2), 0);
            if (isSoil && y < 256 - curTreeHeight - 1) {
                block2.onPlantGrow(world, x, y - 1, z, x, y, z);

                // generate the leaves first
                for (int curY = y + stemHeight; curY <= y + stemHeight + this.canopyHeight; ++curY) {
                    for (int curX = x - halfWidth; curX <= x + halfWidth; ++curX) {

                        for (int curZ = z - halfWidth; curZ <= z + halfWidth; ++curZ) {
                            final double eFactor = this.getEllipsoidFactor(
                                    curX - x,
                                    curY - y - this.canopyCenter,
                                    curZ - z,
                                    halfWidth,
                                    halfHeight);
                            if (eFactor <= 1.0D) {
                                // randomly don't
                                if (eFactor > 0.90D && rand.nextDouble() < 0.5D) {
                                    continue;
                                }
                                // draw them
                                final Block block1 = world.getBlock(curX, curY, curZ);

                                if (block1.isAir(world, curX, curY, curZ) || block1.isLeaves(world, curX, curY, curZ)
                                        || this.isBlockReplaceable(block1)) {
                                    this.setBlockAndNotifyAdequately(
                                            world,
                                            curX,
                                            curY,
                                            curZ,
                                            this.leaves.getBlock(),
                                            this.leaves.getMetadata(),
                                            notify);
                                }

                                if (highestYLeaf < curY) {
                                    highestYLeaf = curY;
                                }
                            }
                        }
                    }
                }

                // stem

                for (int curY = 0; curY < highestYLeaf - y; ++curY) {
                    block = world.getBlock(x, y + curY, z);

                    if (block.isAir(world, x, y + curY, z) || block.isLeaves(world, x, y + curY, z)
                            || this.isBlockReplaceable(block)) {
                        this.setBlockAndNotifyAdequately(
                                world,
                                x,
                                y + curY,
                                z,
                                this.wood.getBlock(),
                                this.wood.getMetadata(),
                                notify);

                    }
                }
                return true;
            }

        }
        return false;
    }

    protected double getEllipsoidFactor(final float x, final float y, final float z, final float widthZX,
            final float height) {
        return Math.pow(x, 2) / (widthZX * widthZX) + Math.pow(y, 2) / (height * height)
                + Math.pow(z, 2) / (widthZX * widthZX);
    }

}
