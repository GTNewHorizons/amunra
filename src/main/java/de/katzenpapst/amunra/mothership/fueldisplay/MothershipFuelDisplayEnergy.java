package de.katzenpapst.amunra.mothership.fueldisplay;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import micdoodle8.mods.galacticraft.core.energy.EnergyDisplayHelper;
import micdoodle8.mods.galacticraft.core.items.GCItems;

public class MothershipFuelDisplayEnergy extends MothershipFuelDisplay {

    protected ItemStack stack;

    protected static MothershipFuelDisplayEnergy instance = null;

    protected MothershipFuelDisplayEnergy() {
        this.stack = new ItemStack(GCItems.battery, 1, 0);
    }

    public static MothershipFuelDisplayEnergy getInstance() {
        if (instance == null) {
            instance = new MothershipFuelDisplayEnergy();
        }
        return instance;
    }

    @Override
    public IIcon getIcon() {
        return this.stack.getItem().getIconFromDamage(this.stack.getItemDamage());
    }

    @Override
    public String getDisplayName() {
        return StatCollector.translateToLocal("gui.message.energy");
    }

    @Override
    public int getSpriteNumber() {
        return this.stack.getItemSpriteNumber();
    }

    @Override
    public String getUnit() {
        return "gJ";
    }

    @Override
    public float getFactor() {
        return 1;
    }

    @Override
    public String formatValue(final float value) {
        return EnergyDisplayHelper.getEnergyDisplayS(value);
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof MothershipFuelDisplayEnergy)) {
            return false;
        }
        return other == this;
    }

    @Override
    public int hashCode() {
        return this.stack.hashCode() + 135842;
    }

}
