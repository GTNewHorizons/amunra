package de.katzenpapst.amunra.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import micdoodle8.mods.galacticraft.core.items.ItemBlockDesc;
import micdoodle8.mods.galacticraft.core.proxy.ClientProxyCore;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class ItemBasicMulti extends Item implements ItemBlockDesc.IBlockShiftDesc {
    // public static final String[] names = { "solar_module_0", "solar_module_1", "rawSilicon", "ingotCopper",
    // "ingotTin", "ingotAluminum", "compressedCopper", "compressedTin", "compressedAluminum", "compressedSteel",
    // "compressedBronze", "compressedIron", "waferSolar", "waferBasic", "waferAdvanced", "dehydratedApple",
    // "dehydratedCarrot", "dehydratedMelon", "dehydratedPotato", "frequencyModule" };

    // protected IIcon[] icons = new IIcon[ItemBasic.names.length];
    protected ArrayList<SubItem> subItems = null;

    protected Map<String, Integer> nameDamageMapping = null;

    public ItemBasicMulti(final String name) {
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setUnlocalizedName(name);
        this.subItems = new ArrayList<>();
        this.nameDamageMapping = new HashMap<>();
    }

    public ItemStack getItemStack(final String name, final int count) {
        return this.getItemStack(this.getDamageByName(name), count);
    }

    public ItemStack getItemStack(final int damage, final int count) {
        // ensure it exists
        if (this.subItems.get(damage) == null) {
            throw new IllegalArgumentException(
                    "SubItem with damage " + damage + " does not exist in " + this.getUnlocalizedName());
        }

        return new ItemStack(this, count, damage);
    }

    public ItemDamagePair addSubItem(final int damage, final SubItem item) {
        if (damage >= this.subItems.size()) {
            this.subItems.ensureCapacity(damage);
            while (damage >= this.subItems.size()) {
                this.subItems.add(null);
            }
        }
        if (this.subItems.get(damage) != null) {
            throw new IllegalArgumentException(
                    "SubItem with damage " + damage + " already exists in " + this.getUnlocalizedName());
        }
        final String itemName = item.getUnlocalizedName();
        if (this.nameDamageMapping.get(itemName) != null) {
            throw new IllegalArgumentException(
                    "SubItem with name " + itemName + " already exists in " + this.getUnlocalizedName());
        }
        this.nameDamageMapping.put(itemName, damage);
        this.subItems.add(damage, item);
        return new ItemDamagePair(this, damage);
    }

    public int getDamageByName(final String name) {
        if (!this.nameDamageMapping.containsKey(name)) {
            return -1;
        }
        return this.nameDamageMapping.get(name);
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
        for (final SubItem item : this.subItems) {
            if (item == null) continue;

            item.registerIcons(register);
            // item.icon = iconRegister.registerIcon(item.getIconString());
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return this.getUnlocalizedName() + "." + this.getSubItem(stack.getItemDamage()).getUnlocalizedName();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int p_77617_1_) {
        return this.subItems.get(p_77617_1_).getIconFromDamage(0);
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass) {
        return this.subItems.get(stack.getItemDamage()).getIcon(stack, pass);
    }

    /**
     * Returns the icon index of the stack given as argument.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack p_77650_1_) {
        return this.subItems.get(p_77650_1_.getItemDamage()).getIconIndex(p_77650_1_);
    }

    @Override
    public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List<ItemStack> p_150895_3_) {
        for (int i = 0; i < this.subItems.size(); i++) {
            if (this.subItems.get(i) == null) continue;
            p_150895_3_.add(new ItemStack(p_150895_1_, 1, i));
        }
    }

    @Override
    public int getMetadata(int p_77647_1_) {
        return p_77647_1_;
    }

    public SubItem getSubItem(final int damage) {
        if (damage >= this.subItems.size() || this.subItems.get(damage) == null) {
            throw new IllegalArgumentException(
                    "Requested invalid SubItem " + damage + " from " + this.getUnlocalizedName());
        }
        return this.subItems.get(damage);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List<String> p_77624_3_,
            boolean p_77624_4_) {
        final SubItem item = this.getSubItem(p_77624_1_.getItemDamage());

        item.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);

        String info = item.getItemInfo();
        if (info != null) {
            info = GCCoreUtil.translate(info);
            p_77624_3_
                    .addAll(FMLClientHandler.instance().getClient().fontRenderer.listFormattedStringToWidth(info, 150));
        }
    }

    @Override
    public ItemStack onEaten(ItemStack p_77654_1_, World p_77654_2_, EntityPlayer p_77654_3_) {
        return this.getSubItem(p_77654_1_.getItemDamage()).onEaten(p_77654_1_, p_77654_2_, p_77654_3_);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack p_77626_1_) {
        return this.getSubItem(p_77626_1_.getItemDamage()).getMaxItemUseDuration(p_77626_1_);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return this.getSubItem(stack.getItemDamage()).getItemUseAction(stack);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        return this.getSubItem(itemStackIn.getItemDamage()).onItemRightClick(itemStackIn, worldIn, player);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        return this.getSubItem(stack.getItemDamage()).onLeftClickEntity(stack, player, entity);
    }

    public int getFuelDuration(final int meta) {
        return this.getSubItem(meta).getFuelDuration();
    }

    @Override
    public String getShiftDescription(final int meta) {
        return null;
    }

    @Override
    public boolean showDescription(final int meta) {
        return false;
    }
}
