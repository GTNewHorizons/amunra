package de.katzenpapst.amunra.block.machine.mothershipEngine;

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.GuiIds;
import de.katzenpapst.amunra.item.ARItems;
import de.katzenpapst.amunra.item.ItemDamagePair;
import de.katzenpapst.amunra.tile.TileEntityMothershipEngineIon;

public class MothershipEngineJetIon extends MothershipEngineJetBase {

    @Deprecated
    protected ItemDamagePair item;

    public MothershipEngineJetIon(final String name, final String texture, final String iconTexture) {
        super(name, texture, iconTexture);
    }

    @Override
    protected TileEntityMothershipEngineIon getMyTileEntity(final World world, final int x, final int y, final int z) {
        if (world.getTileEntity(x, y, z) instanceof TileEntityMothershipEngineIon tileEngine) {
            return tileEngine;
        }

        final String errorMessage = composeMessageForIllegalArgumentException(
                TileEntityMothershipEngineIon.class,
                world.getTileEntity(x, y, z).getClass());

        throw new IllegalArgumentException(errorMessage);
    }

    @Override
    public boolean onMachineActivated(final World world, final int x, final int y, final int z,
            final EntityPlayer entityPlayer, final int side, final float hitX, final float hitY, final float hitZ) {
        // do the isRemote thing here, too?
        entityPlayer.openGui(AmunRa.instance, GuiIds.GUI_MS_ION_ENGINE, world, x, y, z);
        return true;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityMothershipEngineIon();
    }

    @Override
    protected ItemDamagePair getItem() {
        return ARItems.jetItemIon;
    }

    @Override
    public Item getItem(World worldIn, int x, int y, int z) {
        return ARItems.jetItemIon.getItem();
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return ARItems.jetItemIon.getItem();
    }

    @Override
    public int damageDropped(int meta) {
        return ARItems.jetItemIon.getDamage();
    }
}
