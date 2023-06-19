package de.katzenpapst.amunra.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;

public class SubItemToggle extends SubItem {

    protected String inactiveAssetName;

    protected boolean defaultState = false;

    protected IIcon inactItemIcon;

    public SubItemToggle(final String name, final String assetName, final String inactiveAssetName) {
        super(name, assetName);
        this.inactiveAssetName = inactiveAssetName;
    }

    public SubItemToggle(final String name, final String assetName, final String inactiveAssetName, final String info) {
        super(name, assetName, info);
        this.inactiveAssetName = inactiveAssetName;
    }

    public SubItemToggle(final String name, final String assetName, final String inactiveAssetName, final String info,
            final boolean defaultState) {
        super(name, assetName, info);
        this.inactiveAssetName = inactiveAssetName;
        this.defaultState = defaultState;
    }

    public boolean getState(final ItemStack stack) {
        final NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null || !nbt.hasKey("toggleState")) {
            return this.defaultState;
        }

        return nbt.getBoolean("toggleState");
    }

    public void setState(final ItemStack stack, final boolean state) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
        }

        nbt.setBoolean("toggleState", state);
    }

    public void toggleState(final ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt == null || !nbt.hasKey("toggleState")) {
            nbt = new NBTTagCompound();
            stack.setTagCompound(nbt);
            nbt.setBoolean("toggleState", !this.defaultState);
        } else {
            nbt.setBoolean("toggleState", !nbt.getBoolean("toggleState"));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        super.registerIcons(register);
        this.inactItemIcon = register.registerIcon(AmunRa.TEXTUREPREFIX + this.inactiveAssetName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack p_77650_1_) {
        if (this.getState(p_77650_1_)) {
            return this.itemIcon;
        }
        return this.inactItemIcon;
    }

    @Override
    public IIcon getIcon(ItemStack stack, int pass) {
        if (this.getState(stack)) {
            return this.itemIcon;
        }
        return this.inactItemIcon;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        this.toggleState(itemStackIn);
        return itemStackIn;
    }

    @Override
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List<String> p_77624_3_,
            boolean p_77624_4_) {
        super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);

        if (this.getState(p_77624_1_)) {
            p_77624_3_.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("gui.status.active.name"));
        } else {
            p_77624_3_.add(EnumChatFormatting.RED + StatCollector.translateToLocal("gui.status.disabled.name"));
        }
    }
}
