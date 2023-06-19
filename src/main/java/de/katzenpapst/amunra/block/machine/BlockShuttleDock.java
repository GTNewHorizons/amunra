package de.katzenpapst.amunra.block.machine;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.block.SubBlockMachine;
import de.katzenpapst.amunra.tile.TileEntityShuttleDock;
import micdoodle8.mods.galacticraft.api.vector.BlockVec3;

public class BlockShuttleDock extends SubBlockMachine {

    public BlockShuttleDock(final String name, final String texture) {
        super(name, texture);
    }

    public BlockShuttleDock(final String name, final String texture, final String tool, final int harvestLevel) {
        super(name, texture, tool, harvestLevel);
    }

    public BlockShuttleDock(final String name, final String texture, final String tool, final int harvestLevel,
            final float hardness, final float resistance) {
        super(name, texture, tool, harvestLevel, hardness, resistance);
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityShuttleDock();
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        final TileEntity te = worldIn.getTileEntity(x, y, z);
        if (te instanceof TileEntityShuttleDock tileDock) {
            tileDock.onDestroy(te);
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        if (worldIn.getTileEntity(x, y, z) instanceof TileEntityShuttleDock tileDock) {
            tileDock.onCreate(new BlockVec3(x, y, z));
        }
    }

    @Override
    public int getRenderType() {
        return AmunRa.dummyRendererId;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, int x, int y, int z) {
        return worldIn.getBlock(x, y, z).isReplaceable(worldIn, x, y, z)
                && worldIn.getBlock(x, y + 1, z).isReplaceable(worldIn, x, y + 1, z);
    }

    @Override
    public boolean onMachineActivated(final World world, final int x, final int y, final int z,
            final EntityPlayer entityPlayer, final int side, final float hitX, final float hitY, final float hitZ) {
        if (world.getTileEntity(x, y, z) instanceof TileEntityShuttleDock tileDock) {
            tileDock.onActivated(entityPlayer);
        }
        return true;
    }
}
