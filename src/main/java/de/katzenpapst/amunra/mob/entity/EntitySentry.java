package de.katzenpapst.amunra.mob.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.entity.EntityLaserArrow;
import de.katzenpapst.amunra.item.ARItems;
import de.katzenpapst.amunra.item.ItemDamagePair;
import micdoodle8.mods.galacticraft.api.entity.IEntityBreathable;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.core.items.GCItems;

public class EntitySentry extends EntityFlyingMob implements IEntityBreathable {

    private ItemDamagePair[] commonLoot = null;
    private ItemDamagePair[] rareLoot = null;

    public EntitySentry(final World world) {
        super(world);

        this.setSize(1.0F, 1.0F);
        this.isImmuneToFire = true;
        this.experienceValue = 3;

        this.commonLoot = new ItemDamagePair[] { new ItemDamagePair(GCItems.basicItem, 8), // compressed alu
                new ItemDamagePair(GCItems.basicItem, 13) // basic wafer
        };

        this.rareLoot = new ItemDamagePair[] { ARItems.laserDiode, new ItemDamagePair(GCItems.basicItem, 19)// freqModule
        };
    }

    @Override
    protected void performAttack(Entity target, double accelX, double accelY, double accelZ) {

        final EntityLaserArrow attack = new EntityLaserArrow(
                this.worldObj,
                (EntityLivingBase) this,
                new Vector3(this),
                (EntityLivingBase) target);
        attack.setDamage(0.5F);
        this.worldObj.spawnEntityInWorld(attack);

    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    @Override
    protected Item getDropItem() {
        return null;
    }

    @Override
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {

        for (ItemDamagePair element : this.commonLoot) {
            final int numItems = this.rand.nextInt(2) + this.rand.nextInt(1 + p_70628_2_);

            this.entityDropItem(element.getItemStack(numItems), 0.0F);
        }

        if (p_70628_1_ && p_70628_2_ >= 2) {
            final double probability = (p_70628_2_ - 1.0) * 0.05;
            for (ItemDamagePair element : this.rareLoot) {
                if (this.rand.nextDouble() < probability) {
                    this.entityDropItem(element.getItemStack(1), 0.0F);
                    return; // drop only one of these
                }
            }
        }

    }

    @Override
    protected float getVisionDistance() {
        return 30.0F;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        // TODO figure out what this does. Set the max health to 10?
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(5.0D);
    }

    @Override
    protected String getLivingSound() {
        return AmunRa.TEXTUREPREFIX + "mob.sentryblock.idle";
    }

    @Override
    protected String getHurtSound() {
        return AmunRa.TEXTUREPREFIX + "mob.sentryblock.hit";
    }

    @Override
    protected String getDeathSound() {
        return AmunRa.TEXTUREPREFIX + "mob.sentryblock.death";
    }

    @Override
    public String getFiringSound() {
        return AmunRa.TEXTUREPREFIX + "mob.sentryblock.fire";
    }

    @Override
    public boolean canBreath() {
        return true;
    }

}
