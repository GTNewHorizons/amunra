package de.katzenpapst.amunra.world.mapgen;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.util.ForgeDirection;

import de.katzenpapst.amunra.world.WorldHelper;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class CrystalFormation extends WorldGenerator {

    protected BlockMetaPair material;
    protected BlockMetaPair airBlock;
    boolean allowDownward;
    boolean allowUpward;

    /**
     *
     * @param material      Material to generate the formations from
     * @param airBlock      Block to be considered empty, like air or water or so. Can be null, then world.isAirBlock
     *                      will be used
     * @param allowUpward   If true, crystals can grow upwards from the floor
     * @param allowDownward If true, crystals can grow downwards from the ceiling
     */
    public CrystalFormation(final BlockMetaPair material, final BlockMetaPair airBlock, final boolean allowUpward,
            final boolean allowDownward) {
        this.material = material;
        this.airBlock = airBlock;
        this.allowDownward = allowDownward;
        this.allowUpward = allowUpward;
    }

    public CrystalFormation(final BlockMetaPair material) {
        this(material, null, true, true);
    }

    public CrystalFormation(final BlockMetaPair material, final BlockMetaPair airBlock) {
        this(material, airBlock, true, true);
    }

    public CrystalFormation(final BlockMetaPair material, final boolean allowUpward, final boolean allowDownward) {
        this(material, null, allowUpward, allowDownward);
    }

    protected boolean canPlaceHere(final World world, final int x, final int y, final int z) {
        if (y < 0 || y > 255) {
            return false;
        }
        if (this.airBlock == null) {
            return world.isAirBlock(x, y, z);
        }

        return WorldHelper.isBlockMetaPair(world, x, y, z, this.airBlock);
    }

    protected boolean isSolidBlock(final World world, final int x, final int y, final int z, final boolean down) {
        final ForgeDirection dir = down ? ForgeDirection.DOWN : ForgeDirection.UP;
        return world.isSideSolid(x, y, z, dir);
    }

    protected int getLowestBlock(final World world, final int x, final int y, final int z) {
        for (int curY = y; curY >= 0; curY--) {
            if (!this.canPlaceHere(world, x, curY, z)) {
                return curY;
            }
        }
        return -1;
    }

    protected int getHighestBlock(final World world, final int x, final int y, final int z) {
        for (int curY = y; curY <= 255; curY++) {
            if (!this.canPlaceHere(world, x, curY, z)) {
                return curY;
            }
        }
        return -1;
    }

    @Override
    public boolean generate(final World world, final Random rand, final int x, final int y, final int z) {
        boolean downwards = true;

        if (!this.canPlaceHere(world, x, y, z)) {
            return false;
        }
        // find lowest and highest block from here
        final int lowestY = this.getLowestBlock(world, x, y, z);
        final int highestY = this.getHighestBlock(world, x, y, z);
        int actualY = 0;

        if (lowestY < 0 && highestY >= 0 && this.allowDownward) {
            downwards = true;
        } else if (lowestY >= 0 && highestY < 0 && this.allowUpward) {
            downwards = false;
        } else if (lowestY >= 0 && highestY >= 0) {
            // both seem to be set
            if (this.allowDownward && this.allowUpward) {
                downwards = rand.nextBoolean();
            } else if (this.allowDownward) {
                downwards = true;
            } else if (this.allowUpward) {
                downwards = false;
            } else {
                return false;
            }
        } else {
            return false;
        }

        if (downwards) {
            actualY = highestY - 1; // start one below the highest
        } else {
            actualY = lowestY + 1; // start one above the highest
        }

        if (!this.canPlaceHere(world, x, actualY, z)) {
            return false;
        }

        world.setBlock(x, actualY, z, this.material.getBlock(), this.material.getMetadata(), 2);

        for (int l = 0; l < 1500; ++l) {
            final int curX = x + rand.nextInt(8) - rand.nextInt(8);
            int curY = actualY;
            final int curZ = z + rand.nextInt(8) - rand.nextInt(8);

            if (downwards) {
                curY -= rand.nextInt(12);
            } else {
                curY += rand.nextInt(12);
            }

            if (this.canPlaceHere(world, curX, curY, curZ)) {
                int num = 0;

                for (int neighbour = 0; neighbour < 6; ++neighbour) {
                    Block block = null;
                    int meta = 0;

                    switch (neighbour) {
                        case 0 -> {
                            block = world.getBlock(curX - 1, curY, curZ);
                            meta = world.getBlockMetadata(curX - 1, curY, curZ);
                        }
                        case 1 -> {
                            block = world.getBlock(curX + 1, curY, curZ);
                            meta = world.getBlockMetadata(curX + 1, curY, curZ);
                        }
                        case 2 -> {
                            block = world.getBlock(curX, curY - 1, curZ);
                            meta = world.getBlockMetadata(curX, curY - 1, curZ);
                        }
                        case 3 -> {
                            block = world.getBlock(curX, curY + 1, curZ);
                            meta = world.getBlockMetadata(curX, curY + 1, curZ);
                        }
                        case 4 -> {
                            block = world.getBlock(curX, curY, curZ - 1);
                            meta = world.getBlockMetadata(curX, curY, curZ - 1);
                        }
                        case 5 -> {
                            block = world.getBlock(curX, curY, curZ + 1);
                            meta = world.getBlockMetadata(curX, curY, curZ + 1);
                        }
                    }

                    if (block == this.material.getBlock() && meta == this.material.getMetadata()) {
                        ++num;
                    }
                }

                if (num == 1) {
                    world.setBlock(curX, curY, curZ, this.material.getBlock(), this.material.getMetadata(), 2);
                }
            }
        }

        return true;
    }

}
