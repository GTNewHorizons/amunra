package de.katzenpapst.amunra.inventory;

import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.inventory.SlotSpecific;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

import de.katzenpapst.amunra.tile.TileEntityIsotopeGenerator;

public class ContainerAtomBattery extends ContainerWithPlayerInventory {

    public ContainerAtomBattery(InventoryPlayer par1InventoryPlayer, TileEntityIsotopeGenerator solarGen) {
        super(solarGen);

        this.addSlotToContainer(new SlotSpecific(solarGen, 0, 152, 83, ItemElectricBase.class));

        initPlayerInventorySlots(par1InventoryPlayer);
    }

    @Override
    public boolean canInteractWith(EntityPlayer var1) {
        return ((TileEntityIsotopeGenerator) this.tileEntity).isUseableByPlayer(var1);
    }

}
