package de.katzenpapst.amunra.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class SubItemFood extends SubItem {

    /** Number of ticks to run while 'EnumAction'ing until result. */
    public final int itemUseDuration = 32; // wat?!
    /** The amount this food item heals the player. */
    private final int healAmount;
    private final float saturationModifier;
    /** If this field is true, the food can be consumed even if the player don't need to eat. */
    protected boolean alwaysEdible = false;
    /** represents the potion effect that will occurr upon eating this food. Set by setPotionEffect */
    private int potionId;
    /** set by setPotionEffect */
    private int potionDuration;
    /** set by setPotionEffect */
    private int potionAmplifier;
    /** probably of the set potion effect occurring */
    private float potionEffectProbability;

    public SubItemFood(final String name, final String assetName, final int healAmount,
            final float saturationModifier) {
        super(name, assetName);
        this.healAmount = healAmount;
        this.saturationModifier = saturationModifier;
    }

    public SubItemFood(final String name, final String assetName, final String info, final int healAmount,
            final float saturationModifier) {
        super(name, assetName, info);
        this.healAmount = healAmount;
        this.saturationModifier = saturationModifier;
    }

    @Override
    public ItemStack onEaten(ItemStack p_77654_1_, World p_77654_2_, EntityPlayer p_77654_3_) {
        --p_77654_1_.stackSize;

        p_77654_3_.getFoodStats().addStats(this.getHealAmount(p_77654_1_), this.getSaturationModifier(p_77654_1_));
        p_77654_2_.playSoundAtEntity(p_77654_3_, "random.burp", 0.5F, p_77654_2_.rand.nextFloat() * 0.1F + 0.9F);
        this.onFoodEaten(p_77654_1_, p_77654_2_, p_77654_3_);
        return p_77654_1_;
    }

    protected void onFoodEaten(final ItemStack itemStack, final World world, final EntityPlayer player) {
        if (!world.isRemote && this.potionId > 0 && world.rand.nextFloat() < this.potionEffectProbability) {
            player.addPotionEffect(new PotionEffect(this.potionId, this.potionDuration * 20, this.potionAmplifier));
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack p_77626_1_) {
        return 32;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.eat;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (player.canEat(this.alwaysEdible)) {
            player.setItemInUse(itemStackIn, this.getMaxItemUseDuration(itemStackIn));
        }

        return itemStackIn;
    }

    public int getHealAmount(final ItemStack itemStack) {
        return this.healAmount;
    }

    public float getSaturationModifier(final ItemStack itemStack) {
        return this.saturationModifier;
    }

    /**
     * sets a potion effect on the item. Args: int potionId, int duration (will be multiplied by 20), int amplifier,
     * float probability of effect happening
     */
    public SubItemFood setPotionEffect(final int potionId, final int duration, final int amplifier,
            final float probability) {
        this.potionId = potionId;
        this.potionDuration = duration;
        this.potionAmplifier = amplifier;
        this.potionEffectProbability = probability;
        return this;
    }

    /**
     * Set the field 'alwaysEdible' to true, and make the food edible even if the player don't need to eat.
     */
    public SubItemFood setAlwaysEdible() {
        this.alwaysEdible = true;
        return this;
    }
}
