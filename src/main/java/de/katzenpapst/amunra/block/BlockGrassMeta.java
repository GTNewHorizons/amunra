package de.katzenpapst.amunra.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class BlockGrassMeta extends BlockBasicMeta implements IGrowable {

    public BlockGrassMeta(final String name, final Material mat) {
        super(name, mat);
        this.setTickRandomly(true);
    }

    @Override
    public BlockMetaPair addSubBlock(final int meta, final SubBlock sb) {
        if (sb instanceof SubBlockGrass) {
            return super.addSubBlock(meta, sb);
        }
        throw new IllegalArgumentException("BlockGrassMulti can only accept SubBlockGrass");
    }

    /**
     * func_149851_a is basically a stillGrowing() method. It returns (or should return) true if the growth stage is
     * less than the max growth stage.
     *
     * @see <a href=https://forums.minecraftforge.net/topic/22365-understanding-igrowable/#comment-115221>Understanding
     *      IGrowable - Modder Support - Forge Forums</a>
     */
    @Override
    public boolean func_149851_a(World worldIn, int x, int y, int z, boolean isClient) {
        return true;
    }

    /**
     * func_149852_a is basically a canBoneMealSpeedUpGrowth() method. I usually just return true, but depends on your
     * crop.
     *
     * @see <a href=https://forums.minecraftforge.net/topic/22365-understanding-igrowable/#comment-115221>Understanding
     *      IGrowable - Modder Support - Forge Forums</a>
     */
    @Override
    public boolean func_149852_a(World worldIn, Random random, int x, int y, int z) {
        return true;
    }

    /**
     * Ticks the block if it's been scheduled
     */
    @Override
    public void updateTick(World worldIn, int x, int y, int z, Random random) {
        final int meta = worldIn.getBlockMetadata(x, y, z);
        if(!(this.getSubBlock(meta) instanceof SubBlockGrass sb)) {
            return;
        }
        final BlockMetaPair dirtForm = sb.getDirtBlock();
        if (!worldIn.isRemote) {
            if (!sb.canLiveHere(worldIn, x, y, z)) {
                worldIn.setBlock(x, y, z, dirtForm.getBlock(), dirtForm.getMetadata(), 3);
            } else if (sb.canSpread(worldIn, x, y, z)) {
                for (int l = 0; l < 4; ++l) {
                    final int nbX = x + random.nextInt(3) - 1;
                    final int nbY = y + random.nextInt(5) - 3;
                    final int nbZ = z + random.nextInt(3) - 1;
                    final Block block = worldIn.getBlock(nbX, nbY, nbZ);
                    final int blockMeta = worldIn.getBlockMetadata(nbX, nbY, nbZ);

                    if (block == dirtForm.getBlock() && blockMeta == dirtForm.getMetadata()
                            && sb.canLiveHere(worldIn, nbX, nbY, nbZ)) {
                        worldIn.setBlock(nbX, nbY, nbZ, this, meta, 3);
                    }
                }
            }
        }
    }

    /**
     * func_149853_b is basically an incrementGrowthStage() method. In vanilla crops the growth stage is stored in
     * metadata so then in this method you would increment it if it wasn't already at maximum and store back in
     * metadata.
     *
     * @see <a href=https://forums.minecraftforge.net/topic/22365-understanding-igrowable/#comment-115221>Understanding
     *      IGrowable - Modder Support - Forge Forums</a>
     */
    @Override
    public void func_149853_b(World worldIn, Random random, int x, int y, int z) {
        int l = 0;
        final int meta = worldIn.getBlockMetadata(x, y, z);
        if(!(this.getSubBlock(meta) instanceof SubBlockGrass sb)) {
            return;
        }

        while (l < 128) {
            int blockAboveX = x;
            int blockAboveY = y + 1;
            int blockAboveZ = z;
            int grassNearby = 0;

            while (true) {
                if (grassNearby < l / 16) // why 1/16??
                {
                    blockAboveX += random.nextInt(3) - 1;
                    blockAboveY += (random.nextInt(3) - 1) * random.nextInt(3) / 2;
                    blockAboveZ += random.nextInt(3) - 1;
                    if (worldIn.getBlock(blockAboveX, blockAboveY - 1, blockAboveZ) == this && // I hope I can use
                                                                                               // "this"
                                                                                               // here
                            worldIn.getBlockMetadata(blockAboveX, blockAboveY, blockAboveZ) == meta
                            && !worldIn.getBlock(blockAboveX, blockAboveY, blockAboveZ).isNormalCube()) {
                        ++grassNearby;
                        continue;
                    }
                } else if (worldIn.getBlock(blockAboveX, blockAboveY, blockAboveZ).getMaterial() == Material.air) {
                    sb.growPlantsOnTop(worldIn, random, blockAboveX, blockAboveY, blockAboveZ);
                }

                ++l;
                break;
            }
        }
    }
}
