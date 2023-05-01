package de.katzenpapst.amunra.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import de.katzenpapst.amunra.AmunRa;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;

public class ItemBaseBattery extends ItemElectricBase {

    final protected float capacity;

    public ItemBaseBattery(String assetName, float capacity) {
        super();
        this.setUnlocalizedName(assetName);
        this.setTextureName(AmunRa.TEXTUREPREFIX + assetName);
        this.capacity = capacity;
    }

    public ItemBaseBattery(String assetName, float capacity, float maxTransfer) {
        this(assetName, capacity);
        this.transferMax = maxTransfer;
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return AmunRa.arTab;
    }

    @Override
    public float getMaxElectricityStored(ItemStack itemStack) {
        return this.capacity;
    }

}
