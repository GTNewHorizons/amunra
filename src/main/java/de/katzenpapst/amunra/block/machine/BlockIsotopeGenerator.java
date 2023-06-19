package de.katzenpapst.amunra.block.machine;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.GuiIds;
import de.katzenpapst.amunra.block.SubBlockMachine;
import de.katzenpapst.amunra.helper.CoordHelper;
import de.katzenpapst.amunra.tile.TileEntityIsotopeGenerator;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class BlockIsotopeGenerator extends SubBlockMachine {

    private IIcon iconOutput = null;
    private IIcon iconBlank = null;

    protected final String outputTexture;
    protected final String sideTexture;
    public final float energyGeneration;

    public BlockIsotopeGenerator(final String name, final String frontTexture, final String outputTexture,
            final String sideTexture, final float energyGeneration) {
        super(name, frontTexture);

        this.outputTexture = outputTexture;
        this.sideTexture = sideTexture;
        this.energyGeneration = energyGeneration;
    }

    public static boolean isSideEnergyOutput(final int side) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        super.registerBlockIcons(reg);
        this.iconBlank = reg.registerIcon(this.sideTexture);
        this.iconOutput = reg.registerIcon(this.outputTexture);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        final int realMeta = this.parent.getRotationMeta(meta);

        // assuming East is output and South is front
        final ForgeDirection front = CoordHelper.rotateForgeDirection(ForgeDirection.SOUTH, realMeta);

        // west
        if (side == ForgeDirection.UP.ordinal() || side == ForgeDirection.DOWN.ordinal()) {
            return this.iconBlank;
        }

        if (side == front.ordinal()) {
            return this.blockIcon;
        }
        return this.iconOutput;
    }

    @Override
    public boolean onMachineActivated(final World world, final int x, final int y, final int z,
            final EntityPlayer entityPlayer, final int side, final float hitX, final float hitY, final float hitZ) {
        entityPlayer.openGui(AmunRa.instance, GuiIds.GUI_ATOMBATTERY, world, x, y, z);
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityIsotopeGenerator();
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public String getShiftDescription(int meta) {
        return GCCoreUtil.translate("tile.isotopeGenerator.description");
    }

}
