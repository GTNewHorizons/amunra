package de.katzenpapst.amunra.item;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.items.GCItems;

public class ItemAbstractBatteryUser extends ItemElectricBase {

    @Override
    public float getMaxElectricityStored(final ItemStack theItem) {
        if (theItem.getTagCompound() == null) {
            theItem.setTagCompound(new NBTTagCompound());
        }
        if (theItem.getTagCompound().hasKey("maxEnergy")) {
            return theItem.getTagCompound().getFloat("maxEnergy");
        }

        final ItemStack bat = this.getUsedBattery(theItem, false);
        final float maxEnergy = ((ItemElectricBase) bat.getItem()).getMaxElectricityStored(bat);
        theItem.getTagCompound().setFloat("maxEnergy", maxEnergy);
        return maxEnergy;
    }

    @Override
    public void onCreated(ItemStack p_77622_1_, World p_77622_2_, EntityPlayer p_77622_3_) {
        // Do NOT to call the parent for this, because there are crafting recipes that create non-empty rayguns
    }

    /**
     * Returns the battery currently in this device as ItemStack. The ItemStack will be newly constructed
     *
     * @param theItem   The ItemAbstractBatteryUser ItemStack
     * @param setEnergy if true, the result itemstack will also have the energy of the current battery user
     */
    public ItemStack getUsedBattery(final ItemStack theItem, final boolean setEnergy) {
        if (theItem.getTagCompound() == null) {
            theItem.setTagCompound(new NBTTagCompound());
        }

        final NBTTagCompound stackNBT = theItem.getTagCompound().getCompoundTag("batteryStack");
        ItemStack batteryStack = null;
        if (stackNBT == null) {
            // default?
            batteryStack = new ItemStack(GCItems.battery, 1, 0);
        } else {
            batteryStack = ItemStack.loadItemStackFromNBT(stackNBT);
            if (batteryStack == null) {
                batteryStack = new ItemStack(GCItems.battery, 1, 0);
            }
        }

        if (setEnergy) {
            ((ItemElectricBase) batteryStack.getItem())
                    .setElectricity(batteryStack, this.getElectricityStored(theItem));
        }
        return batteryStack;
    }

    /**
     * Set the battery to use for this BU. also sets the BU's energy level to that of the battery
     */
    public void setUsedBattery(final ItemStack theItem, final ItemStack battery) {
        if (theItem.getTagCompound() == null) {
            theItem.setTagCompound(new NBTTagCompound());
        }

        final NBTTagCompound batteryStackCompound = new NBTTagCompound();
        battery.writeToNBT(batteryStackCompound);

        theItem.getTagCompound()
                .setFloat("maxEnergy", ((ItemElectricBase) battery.getItem()).getMaxElectricityStored(battery));
        theItem.getTagCompound().setTag("batteryStack", batteryStackCompound);

        this.setElectricity(theItem, ((ItemElectricBase) battery.getItem()).getElectricityStored(battery));
    }

    public Item getUsedBatteryID(final ItemStack theItem) {
        if (theItem.getTagCompound().hasKey("batteryStack")) {
            final NBTTagCompound stackNBT = theItem.getTagCompound().getCompoundTag("batteryStack");
            final ItemStack batteryStack = ItemStack.loadItemStackFromNBT(stackNBT);

            return batteryStack.getItem();
        }
        return GCItems.battery;
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int p_77663_4_, boolean p_77663_5_) {
        final ItemStack battery = this.getUsedBattery(stack, true);
        battery.getItem().onUpdate(battery, worldIn, entityIn, p_77663_4_, p_77663_5_);
        this.setElectricity(stack, ((ItemElectricBase) battery.getItem()).getElectricityStored(battery));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_,
            @SuppressWarnings("rawtypes") List p_77624_3_, boolean p_77624_4_) {
        super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);

        final Item batItem = this.getUsedBatteryID(p_77624_1_);
        final String s = StatCollector.translateToLocal("item.battery-using-item.powerlevel") + ": "
                + StatCollector.translateToLocal(batItem.getUnlocalizedName() + ".name");

        p_77624_3_.add(s);
    }

}
