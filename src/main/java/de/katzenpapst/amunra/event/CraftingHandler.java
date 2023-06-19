package de.katzenpapst.amunra.event;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import de.katzenpapst.amunra.item.ARItems;
import de.katzenpapst.amunra.item.ItemAbstractBatteryUser;
import de.katzenpapst.amunra.item.ItemBaseBattery;
import de.katzenpapst.amunra.item.ItemCryogun;
import de.katzenpapst.amunra.item.ItemNanotool;
import de.katzenpapst.amunra.item.ItemRaygun;
import micdoodle8.mods.galacticraft.core.items.ItemBattery;

public class CraftingHandler {

    public static CraftingHandler INSTANCE = new CraftingHandler();

    public CraftingHandler() {}

    @SubscribeEvent
    public void onCrafting(final ItemCraftedEvent event) {

        if (event.crafting.getItem() instanceof ItemRaygun) {
            this.handleRaygunCrafting(event, ARItems.raygun);
            return;
        }
        if (event.crafting.getItem() instanceof ItemCryogun) {
            this.handleRaygunCrafting(event, ARItems.cryogun);
            return;
        }
        if (event.crafting.getItem() instanceof ItemNanotool) {
            this.handleRaygunCrafting(event, ARItems.nanotool);
        }
    }

    private void handleRaygunCrafting(final ItemCraftedEvent event, final ItemAbstractBatteryUser gun) {
        int indexGun = -1;
        int indexBattery = -1;

        for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
            final ItemStack curItem = event.craftMatrix.getStackInSlot(i);
            if (curItem == null) continue;
            if (curItem.getItem() instanceof ItemAbstractBatteryUser) {
                indexGun = i;
            } else if (curItem.getItem() instanceof ItemBattery || curItem.getItem() instanceof ItemBaseBattery) {
                indexBattery = i;
            }
        }

        if (indexBattery != -1) {
            final ItemStack newBattery = event.craftMatrix.getStackInSlot(indexBattery);
            if (indexGun != -1) {
                // there is another gun in the ingredients, so this is recharging
                final ItemStack oldGunStack = event.craftMatrix.getStackInSlot(indexGun);

                final ItemStack oldBattery = gun.getUsedBattery(event.craftMatrix.getStackInSlot(indexGun), true);
                event.player.inventory.addItemStackToInventory(oldBattery);

                // replace the nbt stuff
                final NBTBase nbt = oldGunStack.stackTagCompound.copy();
                event.crafting.stackTagCompound = (NBTTagCompound) nbt;

            } else {

            }
            // always set the energy of the battery from the ingredients to the finished gun
            gun.setUsedBattery(event.crafting, newBattery);
        }

    }
}
