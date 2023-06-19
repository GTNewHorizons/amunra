package de.katzenpapst.amunra.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

import de.katzenpapst.amunra.tile.TileEntityMothershipSettings;

public class ContainerMothershipSettings extends ContainerWithPlayerInventory {

    public ContainerMothershipSettings(final InventoryPlayer par1InventoryPlayer,
            final TileEntityMothershipSettings tile) {
        super(tile);

        this.initPlayerInventorySlots(par1InventoryPlayer);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.tileEntity.isUseableByPlayer(player);
    }

}
