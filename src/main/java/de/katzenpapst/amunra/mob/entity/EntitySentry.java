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
    protected void performAttack(final Entity target, final double accelX, final double accelY, final double accelZ) {

        // EntityLargeFireball entitylargefireball = new EntityLargeFireball(this.worldObj, this, startX, startY,
        // startZ);
        // entitylargefireball.field_92057_e = this.explosionStrength;
        /*
         * double size = 4.0D; Vec3 vec3 = this.getLook(1.0F); double x = this.posX + vec3.xCoord * size; double y =
         * this.posY + (double)(this.height / 2.0F) + 0.5D; double z = this.posZ + vec3.zCoord * size;
         */

        final EntityLaserArrow attack = new EntityLaserArrow(
                this.worldObj,
                (EntityLivingBase) this,
                new Vector3(this),
                (EntityLivingBase) target);// new EntityLaserArrow(this.worldObj, (EntityLivingBase)this,
                                           // (EntityLivingBase)target, 0.0F);
        attack.setDamage(0.5F);
        // attack.setDoesFireDamage(false);
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

    /**
     * Drop 0-2 items of this living's type.
     * 
     * @param hitByPlayer - Whether this entity has recently been hit by a player.
     * @param lootLevel   - Level of Looting used to kill this mob.
     */
    @Override
    protected void dropFewItems(final boolean hitByPlayer, final int lootLevel) {

        for (ItemDamagePair element : this.commonLoot) {
            final int numItems = this.rand.nextInt(2) + this.rand.nextInt(1 + lootLevel);

            this.entityDropItem(element.getItemStack(numItems), 0.0F);
        }

        if (hitByPlayer && lootLevel >= 2) {
            final double probability = (lootLevel - 1.0) * 0.05;
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

    /**
     * Returns the sound this mob makes while it's alive.
     */
    @Override
    protected String getLivingSound() {
        return AmunRa.TEXTUREPREFIX + "mob.sentryblock.idle";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    @Override
    protected String getHurtSound() {
        return AmunRa.TEXTUREPREFIX + "mob.sentryblock.hit";
    }

    /**
     * Returns the sound this mob makes on death.
     */
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
