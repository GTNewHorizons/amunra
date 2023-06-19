package de.katzenpapst.amunra.block.machine;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.block.BlockMachineMeta;
import de.katzenpapst.amunra.block.SubBlockMachine;
import de.katzenpapst.amunra.helper.CoordHelper;
import de.katzenpapst.amunra.tile.TileEntityBlockScale;

public class BlockScale extends SubBlockMachine {

    private IIcon iconTop = null;
    private IIcon iconBottom = null;
    private IIcon iconFront = null;

    protected final String topTexture;
    protected final String bottomTexture;
    protected final String frontTexture;

    public BlockScale(final String name, final String sideTexture, final String topTexture, final String frontTexture,
            final String bottomTexture) {
        super(name, sideTexture);
        this.topTexture = topTexture;
        this.bottomTexture = bottomTexture;
        this.frontTexture = frontTexture;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        super.registerBlockIcons(reg);
        this.iconTop = reg.registerIcon(this.topTexture);
        this.iconBottom = reg.registerIcon(this.bottomTexture);
        this.iconFront = reg.registerIcon(this.frontTexture);

    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        final int realMeta = ((BlockMachineMeta) this.parent).getRotationMeta(meta);

        final ForgeDirection sideFD = ForgeDirection.getOrientation(side);

        return switch (sideFD) {
            case UP -> this.iconTop;
            case DOWN -> this.iconBottom;
            default -> {
                final ForgeDirection front = CoordHelper.rotateForgeDirection(ForgeDirection.SOUTH, realMeta);
                yield sideFD == front ? this.iconFront : this.blockIcon;
            }
        };
    }

    @Override
    public TileEntity createTileEntity(final World world, final int metadata) {
        return new TileEntityBlockScale();
    }

    @Override
    public boolean hasTileEntity(final int metadata) {
        return true;
    }

    @Override
    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        if (worldIn.getTileEntity(x, y, z) instanceof TileEntityBlockScale tileScale) {
            tileScale.doUpdate();
        }
    }

}
