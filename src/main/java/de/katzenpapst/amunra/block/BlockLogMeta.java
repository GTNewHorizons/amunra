package de.katzenpapst.amunra.block;

import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class BlockLogMeta extends BlockBasicMeta {

    public BlockLogMeta(final String name, final Material mat) {
        super(name, mat, 4); // only 4 subblocks of wood are possible
    }

    @Override
    public BlockMetaPair addSubBlock(final int meta, final SubBlock sb) {
        if (sb instanceof SubBlockWood) {
            return super.addSubBlock(meta, sb);
        }
        throw new IllegalArgumentException("BlockWoodMulti can only accept SubBlockWood");
    }

    @Override
    public SubBlock getSubBlock(final int meta) {
        return this.subBlocksArray[meta & 3]; // use only the first 2 bits, the rest is rotation
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        /*
         * Face 0 (Bottom Face) Face 1 (Top Face) Face 2 (Northern Face) Face 3 (Southern Face) Face 4 (Western Face)
         * Face 5 (Eastern Face)
         */
        final SubBlock sb = this.getSubBlock(meta);
        if(sb != null) {
            final int rotationMeta = (meta & 12) >> 2;
            return sb.getIcon(side, rotationMeta);
        }
        return super.getIcon(side, meta);
    }

    @Override
    public int damageDropped(int meta) {
        return super.damageDropped(meta & 3);
    }

    @Override
    public boolean isWood(IBlockAccess world, int x, int y, int z) {
        return true;
    }

    @Override
    public int getRenderType() {
        return 31; // ..?
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        final int meta = world.getBlockMetadata(x, y, z);
        if (this.getSubBlock(meta) != null) {
            return new ItemStack(Item.getItemFromBlock(this), 1, this.getDistinctionMeta(meta));
        }

        return super.getPickBlock(target, world, x, y, z);
    }

    @Override
    public int onBlockPlaced(World worldIn, int x, int y, int z, int side, float subX, float subY, float subZ,
            int meta) {
        final int actualMeta = meta & 3;
        final int rotationalMeta = switch (side) {
            case 2, 3 -> 8;
            case 4, 5 -> 4;
            default -> 0;
        };

        return actualMeta | rotationalMeta;
    }

    @Override
    public boolean canSustainLeaves(IBlockAccess world, int x, int y, int z) {
        return true;
    }

}
