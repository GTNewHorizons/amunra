package de.katzenpapst.amunra.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.entity.EntityBaseLaserArrow;

public abstract class ItemAbstractRaygun extends ItemAbstractBatteryUser {

    // set to true for chargeMode, instead of single-shot mode, which would fire each time
    // the player rightclicks
    protected boolean chargeMode = false;

    public ItemAbstractRaygun(final String assetName) {
        this.setUnlocalizedName(assetName);
        this.setTextureName(AmunRa.TEXTUREPREFIX + assetName);
        this.maxStackSize = 1;
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return AmunRa.arTab;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isFull3D() {
        return true;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack p_77615_1_, World p_77615_2_, EntityPlayer p_77615_3_, int p_77615_4_) {
        if (!this.chargeMode) {
            return;
        }

        this.fire(p_77615_1_, p_77615_3_, p_77615_2_);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack p_77626_1_) {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.bow;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        /*
         * ArrowNockEvent event = new ArrowNockEvent(entityPlayer, itemStack); MinecraftForge.EVENT_BUS.post(event); if
         * (event.isCanceled()) { return event.result; }
         */
        if (player.capabilities.isCreativeMode || this.getElectricityStored(itemStackIn) >= this.getEnergyPerShot()) {

            player.setItemInUse(itemStackIn, this.getMaxItemUseDuration(itemStackIn));
            if (!this.chargeMode) {
                this.fire(itemStackIn, player, worldIn);
            }
        } else if (!worldIn.isRemote) {
            worldIn.playSoundAtEntity(
                    player,
                    this.getEmptySound(),
                    1.0F,
                    1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + 0.5F);

        }

        return itemStackIn;
    }

    public float getEnergyPerShot() {
        return 300.0F;
    }

    protected String getFiringSound() {
        return AmunRa.TEXTUREPREFIX + "weapon.lasergun.shot";
    }

    protected String getEmptySound() {
        return AmunRa.TEXTUREPREFIX + "weapon.lasergun.empty";
    }

    protected boolean fire(final ItemStack itemStack, final EntityPlayer entityPlayer, final World world) {
        if (!entityPlayer.capabilities.isCreativeMode) {
            this.setElectricity(
                    itemStack,
                    this.getElectricityStored(itemStack) - this.getModifiedEnergyPerShot(itemStack));
        }
        if (!world.isRemote) {
            world.playSoundAtEntity(
                    entityPlayer,
                    this.getFiringSound(),
                    1.0F,
                    1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + 0.5F);
            this.spawnProjectile(itemStack, entityPlayer, world);
        }
        return true;
    }

    protected float getModifiedEnergyPerShot(final ItemStack stack) {
        float base = this.getEnergyPerShot();

        final int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);
        // max level seems to be 5
        final float relativeEff = efficiency / 10.0F;

        return base * (1.0F - relativeEff);
    }

    protected void spawnProjectile(final ItemStack itemStack, final EntityPlayer entityPlayer, final World world) {
        final EntityBaseLaserArrow ent = this.createProjectile(itemStack, entityPlayer, world);

        // enchantment stuff

        world.spawnEntityInWorld(ent);
    }

    abstract protected EntityBaseLaserArrow createProjectile(ItemStack itemStack, EntityPlayer entityPlayer,
            World world);

    @Override
    public int getItemEnchantability() {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        this.itemIcon = register.registerIcon(this.getIconString());
    }

    @Override
    public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
        return super.getIcon(stack, renderPass, player, usingItem, useRemaining);
    }

}
