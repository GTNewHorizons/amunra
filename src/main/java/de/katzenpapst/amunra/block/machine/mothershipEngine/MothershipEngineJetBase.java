package de.katzenpapst.amunra.block.machine.mothershipEngine;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.GuiIds;
import de.katzenpapst.amunra.block.machine.AbstractBlockMothershipRestricted;
import de.katzenpapst.amunra.item.ItemDamagePair;
import de.katzenpapst.amunra.tile.TileEntityMothershipEngineAbstract;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public abstract class MothershipEngineJetBase extends AbstractBlockMothershipRestricted {

    protected String iconTexture;

    public MothershipEngineJetBase(final String name, final String texture, final String iconTexture) {
        super(name, texture);

        this.iconTexture = iconTexture;
    }

    @Override
    public String getItemIconName() {
        return this.iconTexture;
    }

    /**
     * Not sure why I have to do this here, but...
     */
    abstract protected ItemDamagePair getItem();

    protected TileEntityMothershipEngineAbstract getMyTileEntity(final World world, final int x, final int y,
            final int z) {
        if (world.getTileEntity(x, y, z) instanceof TileEntityMothershipEngineAbstract tileEngine) {
            return tileEngine;
        }

        throw new IllegalArgumentException(
                "Expected " + TileEntityMothershipEngineAbstract.class
                        + " but got "
                        + world.getTileEntity(x, y, z).getClass());
    }

    @Override
    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        if (worldIn.getTileEntity(x, y, z) instanceof TileEntityMothershipEngineAbstract tileEngine) {
            tileEngine.scheduleUpdate();
        }
    }

    @Override
    public int getRenderType() {
        return AmunRa.dummyRendererId;
    }

    @Override
    public Item getItem(World worldIn, int x, int y, int z) {
        return this.getItem().getItem();
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return this.getItem().getItem();
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    protected void openGui(final World world, final int x, final int y, final int z, final EntityPlayer entityPlayer) {
        entityPlayer.openGui(AmunRa.instance, GuiIds.GUI_MS_ROCKET_ENGINE, world, x, y, z);
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public int damageDropped(int meta) {
        return this.getItem().getDamage();
    }

    @Override
    public String getShiftDescription(int meta) {
        return GCCoreUtil.translate("tile.mothershipEngineRocket.description");
    }

    @Override
    public boolean canBeMoved(final World world, final int x, final int y, final int z) {
        return !this.getMyTileEntity(world, x, y, z).isInUse();
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
