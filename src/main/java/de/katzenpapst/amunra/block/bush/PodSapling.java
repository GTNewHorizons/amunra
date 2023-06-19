package de.katzenpapst.amunra.block.bush;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import de.katzenpapst.amunra.block.ARBlocks;

public class PodSapling extends AbstractSapling {

    protected int widthRadius = 4;
    protected int height = 10;

    public PodSapling(final String name, final String texture) {
        super(name, texture);
    }

    protected boolean canGenerateHere(final World world, final Random rand, final int x, final int y, final int z,
            final int height, final double outerRadius) {
        for (int curY = -1; curY < height + 3; curY++) {
            for (int curX = (int) -outerRadius; curX <= +outerRadius; curX++) {
                for (int curZ = (int) -outerRadius; curZ <= +outerRadius; curZ++) {
                    if (curY == -1) {
                        final Block block = world.getBlock(curX + x, curY + y, curZ + z);
                        // check if the ground is dirt or grass
                        if (block.getMaterial() != Material.ground && block.getMaterial() != Material.grass
                                && !this.isBlockReplaceable(block)) {
                            return false;
                        }
                    } else if (!this.canReplaceBlock(world, curX + x, curY + y, curZ + z)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean generate(final World world, final Random rand, final int x, final int y, final int z,
            final boolean notify) {
        final double heightHalf = Math.ceil(this.height / 2) + rand.nextInt(3);
        final double centerOffset = 3 + rand.nextInt(2);

        final double outerRadius = this.widthRadius + rand.nextInt(3); //

        final double innerRadius = outerRadius - 1;
        final double innerHeightRadius = heightHalf - 1;

        int topMostY = 0;

        if (!this.canGenerateHere(world, rand, x, y, z, this.height, outerRadius)) {
            return false;
        }

        for (int curY = -1; curY < this.height + 3; curY++) {
            for (int curX = (int) -outerRadius; curX <= +outerRadius; curX++) {
                for (int curZ = (int) -outerRadius; curZ <= +outerRadius; curZ++) {
                    // some kind of sinus function?
                    if (Math.pow(curX / outerRadius, 2) + Math.pow((curY - centerOffset) / heightHalf, 2)
                            + Math.pow(curZ / outerRadius, 2) <= 1) {

                        if (curY > -1 && Math.pow(curX / innerRadius, 2)
                                + Math.pow((curY - centerOffset) / innerHeightRadius, 2)
                                + Math.pow(curZ / innerRadius, 2) < 1) {
                            // inner stuff
                            if (curY <= centerOffset) {
                                this.setBlockAndNotifyAdequately(
                                        world,
                                        curX + x,
                                        curY + y,
                                        curZ + z,
                                        this.leaves.getBlock(),
                                        this.leaves.getMetadata(),
                                        notify);
                            } else {
                                this.setBlockAndNotifyAdequately(
                                        world,
                                        curX + x,
                                        curY + y,
                                        curZ + z,
                                        Blocks.air,
                                        0,
                                        notify);
                            }
                        } else {
                            this.setBlockAndNotifyAdequately(
                                    world,
                                    curX + x,
                                    curY + y,
                                    curZ + z,
                                    this.wood.getBlock(),
                                    this.wood.getMetadata(),
                                    notify);
                            if (topMostY < curY) {
                                topMostY = curY;
                            }
                        }
                        /*
                         * if (block1.isAir(world, curX+x, curY+y, curZ+z) || block1.isLeaves(world, curX+x, curY+y,
                         * curZ+z)) { this.setBlockAndNotifyAdequately(world, curX+x, curY+y, curZ+z,
                         * this.wood.getBlock(), this.wood.getMetadata(), notify); }
                         */

                    }
                }
            }
        }
        // can't figure out a formula, so just add it on top now
        // make a hole
        for (int curY = 0; curY < 3; curY++) {
            this.setBlockAndNotifyAdequately(world, x, curY + topMostY + y - 1, z, Blocks.air, 0, notify);
            if (curY > 0) {
                // place stuff around?
                this.setBlockAndNotifyAdequately(
                        world,
                        x - 1,
                        curY + topMostY + y - 1,
                        z,
                        this.wood.getBlock(),
                        this.wood.getMetadata(),
                        notify);
                this.setBlockAndNotifyAdequately(
                        world,
                        x + 1,
                        curY + topMostY + y - 1,
                        z,
                        this.wood.getBlock(),
                        this.wood.getMetadata(),
                        notify);
                this.setBlockAndNotifyAdequately(
                        world,
                        x,
                        curY + topMostY + y - 1,
                        z - 1,
                        this.wood.getBlock(),
                        this.wood.getMetadata(),
                        notify);
                this.setBlockAndNotifyAdequately(
                        world,
                        x,
                        curY + topMostY + y - 1,
                        z + 1,
                        this.wood.getBlock(),
                        this.wood.getMetadata(),
                        notify);

                if (curY == 1) {
                    this.setBlockAndNotifyAdequately(
                            world,
                            x + 1,
                            curY + topMostY + y - 1,
                            z - 1,
                            this.wood.getBlock(),
                            this.wood.getMetadata(),
                            notify);
                    this.setBlockAndNotifyAdequately(
                            world,
                            x - 1,
                            curY + topMostY + y - 1,
                            z - 1,
                            this.wood.getBlock(),
                            this.wood.getMetadata(),
                            notify);
                    this.setBlockAndNotifyAdequately(
                            world,
                            x + 1,
                            curY + topMostY + y - 1,
                            z + 1,
                            this.wood.getBlock(),
                            this.wood.getMetadata(),
                            notify);
                    this.setBlockAndNotifyAdequately(
                            world,
                            x - 1,
                            curY + topMostY + y - 1,
                            z + 1,
                            this.wood.getBlock(),
                            this.wood.getMetadata(),
                            notify);

                    this.setBlockAndNotifyAdequately(
                            world,
                            x - 2,
                            curY + topMostY + y - 1,
                            z,
                            this.wood.getBlock(),
                            this.wood.getMetadata(),
                            notify);
                    this.setBlockAndNotifyAdequately(
                            world,
                            x + 2,
                            curY + topMostY + y - 1,
                            z,
                            this.wood.getBlock(),
                            this.wood.getMetadata(),
                            notify);
                    this.setBlockAndNotifyAdequately(
                            world,
                            x,
                            curY + topMostY + y - 1,
                            z - 2,
                            this.wood.getBlock(),
                            this.wood.getMetadata(),
                            notify);
                    this.setBlockAndNotifyAdequately(
                            world,
                            x,
                            curY + topMostY + y - 1,
                            z + 2,
                            this.wood.getBlock(),
                            this.wood.getMetadata(),
                            notify);

                }

            }
        }
        return true;
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

}
