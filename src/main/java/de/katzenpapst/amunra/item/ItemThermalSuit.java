package de.katzenpapst.amunra.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import micdoodle8.mods.galacticraft.api.item.IItemThermal;
import micdoodle8.mods.galacticraft.core.proxy.ClientProxyCore;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class ItemThermalSuit extends Item implements IItemThermal {

    protected int thermalStrength;

    protected String[] iconStrings = new String[4];
    protected IIcon[] icons = new IIcon[4];
    protected final String[] names = { "helmet", "chest", "legs", "boots" };

    public ItemThermalSuit(final String name, final int thermalStrength, final String helmetIcon,
            final String chestIcon, final String legsIcon, final String bootsIcon) {
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setUnlocalizedName(name);

        this.thermalStrength = thermalStrength;
        this.iconStrings[0] = AmunRa.TEXTUREPREFIX + helmetIcon;
        this.iconStrings[1] = AmunRa.TEXTUREPREFIX + chestIcon;
        this.iconStrings[2] = AmunRa.TEXTUREPREFIX + legsIcon;
        this.iconStrings[3] = AmunRa.TEXTUREPREFIX + bootsIcon;
    }

    public ItemDamagePair getHelmet() {
        return new ItemDamagePair(this, 0);
    }

    public ItemDamagePair getChest() {
        return new ItemDamagePair(this, 1);
    }

    public ItemDamagePair getLegts() {
        return new ItemDamagePair(this, 2);
    }

    public ItemDamagePair getBoots() {
        return new ItemDamagePair(this, 3);
    }

    @Override
    public int getThermalStrength() {
        return this.thermalStrength;
    }

    @Override
    public boolean isValidForSlot(ItemStack stack, int armorSlot) {
        return armorSlot == stack.getItemDamage();
    }

    public void register() {
        GameRegistry.registerItem(this, this.getUnlocalizedName(), AmunRa.MODID);
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return AmunRa.arTab;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack p_77613_1_) {
        return ClientProxyCore.galacticraftItem;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        for (int i = 0; i < 4; i++) {
            this.icons[i] = register.registerIcon(this.iconStrings[i]);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return this.getUnlocalizedName() + "." + this.names[stack.getItemDamage()];
    }

    @Override
    public IIcon getIconFromDamage(int p_77617_1_) {
        return this.icons[p_77617_1_];
    }

    @Override
    public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List<ItemStack> p_150895_3_) {
        for (int i = 0; i < 4; i++) {
            p_150895_3_.add(new ItemStack(p_150895_1_, 1, i));
        }
    }

    @Override
    public int getMetadata(int p_77647_1_) {
        return p_77647_1_;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List<String> p_77624_3_,
            boolean p_77624_4_) {
        p_77624_3_.add(GCCoreUtil.translateWithFormat("item.thermalSuit.thermalLevel.name", this.thermalStrength));
        /*
         * String info = getSubItem(par1ItemStack.getItemDamage()).getItemInfo(); if(info != null) {
         * par3List.add(GCCoreUtil.translate(info)); }
         */
    }
}
