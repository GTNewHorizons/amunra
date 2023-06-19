package de.katzenpapst.amunra.mob.entity;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import micdoodle8.mods.galacticraft.api.entity.IEntityBreathable;
import micdoodle8.mods.galacticraft.api.world.IAtmosphericGas;

public class EntityAlienBug extends EntityMob implements IEntityNonOxygenBreather, IEntityBreathable {

    public EntityAlienBug(final World world) {
        super(world);
        this.setSize(1.4F, 0.9F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(16, (byte) 0);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!this.worldObj.isRemote) {
            this.setBesideClimbableBlock(this.isCollidedHorizontally);
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(16.0D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.800000011920929D);
    }

    @Override
    protected Entity findPlayerToAttack() {
        final float f = this.getBrightness(1.0F);

        if (f < 0.5F) {
            final double d0 = 16.0D;
            return this.worldObj.getClosestVulnerablePlayerToEntity(this, d0);
        }
        return null;
    }

    @Override
    protected String getLivingSound() {
        return "mob.spider.say";
    }

    @Override
    protected String getHurtSound() {
        return "mob.spider.say";
    }

    @Override
    protected String getDeathSound() {
        return "mob.spider.death";
    }

    // onStep?
    @Override
    protected void func_145780_a(final int x, final int y, final int z, final Block block) {
        this.playSound("mob.spider.step", 0.15F, 1.0F);
    }

    @Override
    protected void attackEntity(Entity p_70785_1_, float p_70785_2_) {
        final float f1 = this.getBrightness(1.0F);

        if (f1 > 0.5F && this.rand.nextInt(100) == 0) {
            this.entityToAttack = null;
        } else if (p_70785_2_ > 2.0F && p_70785_2_ < 6.0F && this.rand.nextInt(10) == 0) {
            if (this.onGround) {
                // get closer to target?
                final double deltaX = p_70785_1_.posX - this.posX;
                final double deltaZ = p_70785_1_.posZ - this.posZ;
                final float planarDistance = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);
                this.motionX = deltaX / planarDistance * 0.5D * 0.800000011920929D
                        + this.motionX * 0.20000000298023224D;
                this.motionZ = deltaZ / planarDistance * 0.5D * 0.800000011920929D
                        + this.motionZ * 0.20000000298023224D;
                this.motionY = 0.4000000059604645D;
            }
        } else {
            super.attackEntity(p_70785_1_, p_70785_2_);
        }
    }

    @Override
    protected Item getDropItem() {
        return Items.string;
    }

    @Override
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {}

    @Override
    public boolean isOnLadder() {
        return this.isBesideClimbableBlock();
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    public boolean isPotionApplicable(PotionEffect p_70687_1_) {
        return true;
    }

    /**
     * Returns true if the WatchableObject (Byte) is 0x01 otherwise returns false. The WatchableObject is updated using
     * setBesideClimableBlock.
     */
    public boolean isBesideClimbableBlock() {
        return (this.dataWatcher.getWatchableObjectByte(16) & 1) != 0;
    }

    /**
     * Updates the WatchableObject (Byte) created in entityInit(), setting it to 0x01 if par1 is true or 0x00 if it is
     * false.
     */
    public void setBesideClimbableBlock(final boolean isBeside) {
        byte b0 = this.dataWatcher.getWatchableObjectByte(16);

        if (isBeside) {
            b0 = (byte) (b0 | 1);
        } else {
            b0 &= -2;
        }

        this.dataWatcher.updateObject(16, b0);
    }

    @Override
    public IEntityLivingData onSpawnWithEgg(IEntityLivingData p_110161_1_) {
        return super.onSpawnWithEgg(p_110161_1_);
    }

    @Override
    public boolean canBreath() {
        return true;
    }

    @Override
    public boolean canBreatheIn(final ArrayList<IAtmosphericGas> atmosphere, final boolean isInSealedArea) {
        // TODO: maybe make them afraid of oxygen? maybe later

        return atmosphere.contains(IAtmosphericGas.METHANE);
    }
}
