package de.katzenpapst.amunra.mob.entity;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.item.ARItems;
import de.katzenpapst.amunra.mob.MobHelper;
import micdoodle8.mods.galacticraft.api.entity.IEntityBreathable;
import micdoodle8.mods.galacticraft.api.world.IAtmosphericGas;

public class EntityPorcodon extends EntityAnimal implements IEntityBreathable, IEntityNonOxygenBreather {

    private ItemStack dropItem = null;

    final private int explosionRadius = 3;
    final private int fuseTime = 30;

    private boolean isIgnited = false;
    private int timeSinceIgnited = 0;

    public EntityPorcodon(final World curWorld) {
        super(curWorld);

        this.setSize(0.9F, 0.9F);
        this.getNavigator().setAvoidsWater(true);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 1.25D));
        this.tasks.addTask(3, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(5, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));

        this.dropItem = ARItems.baseItem.getItemStack("porcodonMeat", 1);

    }

    @Override
    public boolean isAIEnabled() {
        return true;
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
    }

    @Override
    public boolean canBeSteered() {
        return false;
    }

    /**
     * @TODO figure out what this does o_O
     */
    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(16, 0);
    }

    @Override
    protected String getLivingSound() {
        return "mob.pig.say";
    }

    @Override
    protected String getHurtSound() {
        return "mob.pig.say";
    }

    @Override
    protected String getDeathSound() {
        return "mob.pig.death";
    }

    @Override
    protected void func_145780_a(int x, int y, int z, Block blockIn) {
        this.playSound("mob.pig.step", 0.15F, 1.0F);
    }

    @Override
    protected Item getDropItem() {
        return null;
    }

    @Override
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
        // drop at least one meat
        final int j = this.rand.nextInt(3) + 1 + this.rand.nextInt(1 + p_70628_2_);
        final ItemStack toDrop = this.dropItem.copy();
        toDrop.stackSize = j;
        this.entityDropItem(toDrop, 0.0F);

    }

    /**
     * Misnamed imho, this function should be called "doesNotRequireOxygen"
     */
    @Override
    public boolean canBreath() {
        return true;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable p_90011_1_) {
        return new EntityPorcodon(this.worldObj);
    }

    @Override
    public boolean isBreedingItem(ItemStack p_70877_1_) {
        return false;// item != null && item.getItem() == Items.carrot;
    }

    @Override
    public boolean canBreatheIn(final ArrayList<IAtmosphericGas> atmosphere, final boolean isInSealedArea) {
        final boolean hasOxygen = isInSealedArea || atmosphere.contains(IAtmosphericGas.OXYGEN);

        // add stuff if oxygen exists
        if (hasOxygen && !this.isIgnited) {
            this.ignite();
        }
        if (!hasOxygen && this.isIgnited) {
            this.unIgnite();
        }

        return atmosphere.contains(IAtmosphericGas.METHANE);
    }

    private void ignite() {
        this.isIgnited = true;
        this.timeSinceIgnited = 0;
        this.playSound("creeper.primed", 1.0F, 0.5F);
    }

    private void unIgnite() {
        this.isIgnited = false;
        this.timeSinceIgnited = 0;
        this.playSound("random.fizz", 1.0F, 0.5F);
    }

    @Override
    public void onUpdate() {
        if (this.isEntityAlive() && this.isIgnited) {
            this.timeSinceIgnited++;

            if (this.timeSinceIgnited >= this.fuseTime) {
                this.timeSinceIgnited = this.fuseTime;
                this.explode();
            }
        }

        super.onUpdate();
    }

    private void explode() {
        if (!this.worldObj.isRemote) {
            final boolean flag = this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");

            this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, this.explosionRadius, flag);

            // why is this only in the if here?
            this.setDead();
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        return MobHelper.canAnimalSpawnHere(this.worldObj, this, ARBlocks.blockMethaneGrass);
    }

}
