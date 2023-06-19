package de.katzenpapst.amunra.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

import de.katzenpapst.amunra.item.ItemShuttle;
import de.katzenpapst.amunra.tile.TileEntityShuttleDock;
import micdoodle8.mods.galacticraft.core.inventory.SlotSpecific;

public class ContainerShuttleDock extends ContainerWithPlayerInventory {

    public ContainerShuttleDock(final InventoryPlayer player, final TileEntityShuttleDock tile) {
        super(tile);

        this.addSlotToContainer(new SlotSpecific(tile, 0, 137, 59, ItemShuttle.class));

        this.initPlayerInventorySlots(player, 9);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.tileEntity.isUseableByPlayer(player);
    }

}
