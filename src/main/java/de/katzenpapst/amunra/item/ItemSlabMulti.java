package de.katzenpapst.amunra.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.block.BlockDoubleslabMeta;
import de.katzenpapst.amunra.block.BlockSlabMeta;

public class ItemSlabMulti extends ItemBlockMulti {

    private final boolean isDoubleSlab;

    protected final Block singleSlab;
    protected final Block doubleSlab;

    public ItemSlabMulti(final Block block, final BlockSlabMeta singleSlab, final BlockDoubleslabMeta doubleSlab) {
        super(block);

        this.singleSlab = singleSlab;
        this.doubleSlab = doubleSlab;
        this.isDoubleSlab = block == doubleSlab;
    }

    protected boolean placeDoubleSlab(final World world, final int x, final int y, final int z, final int meta) {
        return world.setBlock(x, y, z, this.doubleSlab, meta, 3);
    }

    protected void combine(final World world, final ItemStack stack, final int x, final int y, final int z,
            final int meta) {
        if (world.checkNoEntityCollision(this.doubleSlab.getCollisionBoundingBoxFromPool(world, x, y, z))
                && this.placeDoubleSlab(world, x, y, z, meta)) {
            world.playSoundEffect(
                    x + 0.5F,
                    y + 0.5F,
                    z + 0.5F,
                    this.doubleSlab.stepSound.func_150496_b(),
                    (this.doubleSlab.stepSound.getVolume() + 1.0F) / 2.0F,
                    this.doubleSlab.stepSound.getPitch() * 0.8F);
            --stack.stackSize;
        }
    }

    @Override
    public boolean onItemUse(ItemStack p_77648_1_, EntityPlayer p_77648_2_, World p_77648_3_, int p_77648_4_,
            int p_77648_5_, int p_77648_6_, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
        if (this.isDoubleSlab) {
            return super.onItemUse(
                    p_77648_1_,
                    p_77648_2_,
                    p_77648_3_,
                    p_77648_4_,
                    p_77648_5_,
                    p_77648_6_,
                    p_77648_7_,
                    p_77648_8_,
                    p_77648_9_,
                    p_77648_10_);
        }
        if (p_77648_1_.stackSize == 0
                || !p_77648_2_.canPlayerEdit(p_77648_4_, p_77648_5_, p_77648_6_, p_77648_7_, p_77648_1_)) {
            return false;
        }
        final Block block = p_77648_3_.getBlock(p_77648_4_, p_77648_5_, p_77648_6_);
        final int worldMeta = p_77648_3_.getBlockMetadata(p_77648_4_, p_77648_5_, p_77648_6_);
        final int worldDistinctionMeta = worldMeta & 7;
        final boolean isHighestBitSet = (worldMeta & 8) != 0; // I think the meaning is: isSlabOnTop

        if ((isHighestBitSet ? p_77648_7_ == 0 : p_77648_7_ == 1) && block == this.singleSlab
                && worldDistinctionMeta == p_77648_1_.getItemDamage()) {
            // we are rightclicking on a slab with which we can merge
            this.combine(p_77648_3_, p_77648_1_, p_77648_4_, p_77648_5_, p_77648_6_, worldDistinctionMeta);

            return true;
        } else {
            return this.tryCombiningWithSide(
                    p_77648_1_,
                    p_77648_2_,
                    p_77648_3_,
                    p_77648_4_,
                    p_77648_5_,
                    p_77648_6_,
                    p_77648_7_)
                    || super.onItemUse(
                            p_77648_1_,
                            p_77648_2_,
                            p_77648_3_,
                            p_77648_4_,
                            p_77648_5_,
                            p_77648_6_,
                            p_77648_7_,
                            p_77648_8_,
                            p_77648_9_,
                            p_77648_10_);
        }
    }

    /**
     * No idea what this actually is, but it helps slab placement
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean func_150936_a(World p_150936_1_, int p_150936_2_, int p_150936_3_, int p_150936_4_, int p_150936_5_,
            EntityPlayer p_150936_6_, ItemStack p_150936_7_) {
        final int xNew = p_150936_2_;
        final int yNew = p_150936_3_;
        final int zNew = p_150936_4_;
        final Block block = p_150936_1_.getBlock(p_150936_2_, p_150936_3_, p_150936_4_);
        final int meta = p_150936_1_.getBlockMetadata(p_150936_2_, p_150936_3_, p_150936_4_);
        int distinctionMeta = meta & 7;
        final boolean isUpperSlab = (meta & 8) != 0;

        if ((isUpperSlab ? p_150936_5_ == 0 : p_150936_5_ == 1) && block == this.singleSlab
                && distinctionMeta == p_150936_7_.getItemDamage()) {
            return true;
        }
        switch (p_150936_5_) {
            case 0:
                --p_150936_3_;
                break;
            case 1:
                ++p_150936_3_;
                break;
            case 2:
                --p_150936_4_;
                break;
            case 3:
                ++p_150936_4_;
                break;
            case 4:
                --p_150936_2_;
                break;
            case 5:
                ++p_150936_2_;
                break;
        }

        final Block newBlock = p_150936_1_.getBlock(p_150936_2_, p_150936_3_, p_150936_4_);
        final int newMeta = p_150936_1_.getBlockMetadata(p_150936_2_, p_150936_3_, p_150936_4_);
        distinctionMeta = newMeta & 7;
        return (newBlock == this.singleSlab && distinctionMeta == p_150936_7_.getItemDamage())
                || super.func_150936_a(p_150936_1_, xNew, yNew, zNew, p_150936_5_, p_150936_6_, p_150936_7_);
    }

    private boolean tryCombiningWithSide(final ItemStack stack, final EntityPlayer player, final World world, int x,
            int y, int z, final int side) {
        switch (side) {
            case 0:
                --y;
                break;
            case 1:
                ++y;
                break;
            case 2:
                --z;
                break;
            case 3:
                ++z;
                break;
            case 4:
                --x;
                break;
            case 5:
                ++x;
                break;
        }

        final Block block = world.getBlock(x, y, z);
        final int worldMeta = world.getBlockMetadata(x, y, z);
        final int worldDistinctionMeta = worldMeta & 7;

        if (block == this.singleSlab && worldDistinctionMeta == stack.getItemDamage()) {
            this.combine(world, stack, x, y, z, worldDistinctionMeta);

            return true;
        }
        return false;
    }

}
