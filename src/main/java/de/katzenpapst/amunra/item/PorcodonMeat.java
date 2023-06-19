package de.katzenpapst.amunra.item;

import net.minecraft.potion.Potion;

public class PorcodonMeat extends SubItemFood {

    public PorcodonMeat() {
        super("porcodonMeat", "green_bacon", "item.porcodonMeat.description", 4, 0.1F);
        this.setPotionEffect(Potion.poison.id, 30, 2, 1.0F);

    }

    @Override
    public int getFuelDuration() {
        return 3200;
    }

}
