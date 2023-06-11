package de.katzenpapst.amunra.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.api.vector.BlockVec3;
import micdoodle8.mods.galacticraft.core.tile.TileEntityMulti;

public class BlockMetaFake extends BlockBasicMeta implements ITileEntityProvider {

    public BlockMetaFake(final String name, final Material mat) {
        super(name, mat);
    }

    @Override
    public ItemStack getPickBlock(final MovingObjectPosition target, final World world, final int x, final int y,
            final int z, final EntityPlayer player) {
        final SubBlock sb = this.getSubBlock(world.getBlockMetadata(x, y, z));
        if(sb != null) {
            return sb.getPickBlock(target, world, x, y, z, player);
        }
        return super.getPickBlock(target, world, x, y, z, player);
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public CreativeTabs getCreativeTabToDisplayOn() {
        return null;
    }

    public void makeFakeBlock(final World world, final BlockVec3 position, final BlockVec3 mainBlock,
            final BlockMetaPair bmp) {
        world.setBlock(position.x, position.y, position.z, this, bmp.getMetadata(), 3);
        if(world.getTileEntity(position.x, position.y, position.z) instanceof TileEntityMulti multi) {
            multi.setMainBlock(mainBlock);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        final SubBlock sb = this.getSubBlock(meta);
        if(sb != null) {
            return sb.createTileEntity(worldIn, meta);
        }
        return null;
    }

}
