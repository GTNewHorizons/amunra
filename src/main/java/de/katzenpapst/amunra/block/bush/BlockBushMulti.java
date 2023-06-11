package de.katzenpapst.amunra.block.bush;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;

import de.katzenpapst.amunra.block.BlockBasicMeta;
import de.katzenpapst.amunra.block.SubBlock;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

/**
 * @see BlockBush
 */
public class BlockBushMulti extends BlockBasicMeta implements IGrowable, IShearable, IPlantable {

    public BlockBushMulti(final String name, final Material mat) {
        super(name, mat);
        this.setTickRandomly(true);
    }

    public BlockBushMulti(final String name, final Material mat, final int numSubBlocks) {
        super(name, mat, numSubBlocks);
        this.setTickRandomly(true);
    }

    @Override
    public void updateTick(World worldIn, int x, int y, int z, Random random) {
        this.checkAndDropBlock(worldIn, x, y, z);
    }

    /**
     * checks if the block can stay, if not drop as item
     */
    protected void checkAndDropBlock(final World world, final int x, final int y, final int z) {
        if (!this.canBlockStay(world, x, y, z)) {
            this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
            world.setBlock(x, y, z, Blocks.air, 0, 2);
        }
    }

    @Override
    public BlockMetaPair addSubBlock(int meta, SubBlock sb) {
        if (!(sb instanceof SubBlockBush)) {
            throw new IllegalArgumentException("BlockBushMulti can only accept SubBlockBush");
        }
        return super.addSubBlock(meta, sb);
    }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, int x, int y, int z) {
        final int meta = world.getBlockMetadata(x, y, z);
        return ((SubBlockBush) this.getSubBlock(meta)).isShearable(item, world, x, y, z);
    }

    @Override
    public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z, int fortune) {
        final int meta = world.getBlockMetadata(x, y, z);
        return ((SubBlockBush) this.getSubBlock(meta)).onSheared(item, world, x, y, z, fortune);
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
        if (this.getSubBlock(worldIn.getBlockMetadata(x, y, z)) instanceof SubBlockBush bush) {
            return bush.func_149851_a(worldIn, x, y, z, isClient);
        }
        return false;
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
        if (this.getSubBlock(worldIn.getBlockMetadata(x, y, z)) instanceof SubBlockBush bush) {
            return bush.func_149852_a(worldIn, random, x, y, z);
        }
        return false;
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
        if (this.getSubBlock(worldIn.getBlockMetadata(x, y, z)) instanceof SubBlockBush bush) {
            bush.func_149853_b(worldIn, random, x, y, z);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        return null;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 1;
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
        if (this.getSubBlock(world.getBlockMetadata(x, y, z)) instanceof SubBlockBush bush) {
            return bush.getPlantType(world, x, y, z);
        }
        return EnumPlantType.Plains;
    }

    @Override
    public Block getPlant(IBlockAccess world, int x, int y, int z) {
        return this;
    }

    @Override
    public int getPlantMetadata(IBlockAccess world, int x, int y, int z) {
        return world.getBlockMetadata(x, y, z);
    }

    public boolean canPlaceOn(final BlockMetaPair blockToCheck, final int meta) {
        return this.canPlaceOn(blockToCheck.getBlock(), blockToCheck.getMetadata(), meta);
    }

    public boolean canPlaceOn(final Block blockToCheck, final int metaToCheck, final int meta) {
        if (this.getSubBlock(meta) instanceof SubBlockBush bush) {
            return bush.canPlaceOn(blockToCheck, metaToCheck, 0);
        }
        return false;
    }

    @Override
    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        super.onNeighborBlockChange(worldIn, x, y, z, neighbor);
        this.checkAndDropBlock(worldIn, x, y, z);
    }
    /*
     * @Override public boolean isCollidable() { return false; }
     */
}
