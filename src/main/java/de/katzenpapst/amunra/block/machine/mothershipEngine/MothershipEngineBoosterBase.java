package de.katzenpapst.amunra.block.machine.mothershipEngine;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.GuiIds;
import de.katzenpapst.amunra.block.machine.AbstractBlockMothershipRestricted;
import de.katzenpapst.amunra.tile.TileEntityMothershipEngineAbstract;
import de.katzenpapst.amunra.tile.TileEntityMothershipEngineBooster;
import de.katzenpapst.amunra.vec.Vector3int;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class MothershipEngineBoosterBase extends AbstractBlockMothershipRestricted {

    protected String activeTextureName;
    protected IIcon activeBlockIcon;
    protected ResourceLocation boosterTexture = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/blocks/jet-base.png");

    public MothershipEngineBoosterBase(final String name, final String texture, final String activeTexture) {
        super(name, texture);
        this.activeTextureName = activeTexture;
    }

    public MothershipEngineBoosterBase(final String name, final String texture, final String activeTexture,
            final String tool, final int harvestLevel) {
        super(name, texture, tool, harvestLevel);
        this.activeTextureName = activeTexture;
    }

    @Override
    public boolean onMachineActivated(final World world, final int x, final int y, final int z,
            final EntityPlayer entityPlayer, final int side, final float hitX, final float hitY, final float hitZ) {
        final TileEntity leTile = world.getTileEntity(x, y, z);
        if (!(leTile instanceof TileEntityMothershipEngineBooster tile)) {
            return false;
        }
        if (tile.hasMaster()) {
            return super.onMachineActivated(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ);
        }
        return false;
    }

    @Override
    protected void openGui(World world, int x, int y, int z, EntityPlayer entityPlayer) {
        if (world.isRemote) {
            return;
        }
        final TileEntity leTile = world.getTileEntity(x, y, z);
        if (leTile != null) {
            final TileEntityMothershipEngineBooster tile = (TileEntityMothershipEngineBooster) leTile;
            final Vector3int pos = tile.getMasterPosition();

            entityPlayer.openGui(AmunRa.instance, GuiIds.GUI_MS_ROCKET_ENGINE, world, pos.x, pos.y, pos.z);
        }
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    public MothershipEngineBoosterBase(final String name, final String texture, final String activeTexture,
            final String tool, final int harvestLevel, final float hardness, final float resistance) {
        super(name, texture, tool, harvestLevel, hardness, resistance);
        this.activeTextureName = activeTexture;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityMothershipEngineBooster();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        super.registerBlockIcons(reg);
        this.activeBlockIcon = reg.registerIcon(this.activeTextureName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, final int meta) {
        if (side <= 1) {
            return this.blockIcon;
        }
        return this.activeBlockIcon;
    }

    @Override
    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        // these are MY coords
        final TileEntity leTile = worldIn.getTileEntity(x, y, z);
        if (leTile == null) return;

        if (leTile instanceof TileEntityMothershipEngineAbstract tileEngine) {
            tileEngine.scheduleUpdate();
        } else if (leTile instanceof TileEntityMothershipEngineBooster tileBooster) {
            tileBooster.updateMaster(false);
            // attempt to continue the process
            final Vector3int pos = tileBooster.getPossibleNextBooster();
            if (pos != null) {
                worldIn.notifyBlockOfNeighborChange(pos.x, pos.y, pos.z, tileBooster.blockType);
            }
        }
    }

    @Override
    public String getShiftDescription(int meta) {
        return GCCoreUtil.translate("tile.mothershipEngineRocket.description");
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isBlockNormalCube() {
        return true;
    }

    @Override
    public boolean isNormalCube() {
        return true;
    }

    @Override
    public int getRenderType() {
        return AmunRa.msBoosterRendererId;
    }

    public ResourceLocation getBoosterTexture() {
        return this.boosterTexture;
    }

    @Override
    public boolean canBeMoved(final World world, final int x, final int y, final int z) {
        if (world.getTileEntity(x, y, z) instanceof TileEntityMothershipEngineBooster tileBooster) {
            final TileEntityMothershipEngineAbstract master = tileBooster.getMasterTile();
            return master == null || !master.isInUse();
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
        if (this.canBeMoved(world, x, y, z)) {
            return super.removedByPlayer(world, player, x, y, z);
        }
        return false;
    }

}
