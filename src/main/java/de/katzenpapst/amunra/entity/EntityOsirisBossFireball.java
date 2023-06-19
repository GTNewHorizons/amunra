package de.katzenpapst.amunra.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityOsirisBossFireball extends EntityFireball {

    public float damage = 1.0F;

    public EntityOsirisBossFireball(final World world) {
        super(world);
    }

    @Override
    protected void onImpact(MovingObjectPosition p_70227_1_) {
        if (!this.worldObj.isRemote) {
            if (p_70227_1_.entityHit != null && !(p_70227_1_.entityHit instanceof EntityCreeper)) {
                p_70227_1_.entityHit
                        .attackEntityFrom(DamageSource.causeFireballDamage(this, this.shootingEntity), this.damage);
            }

            this.worldObj.newExplosion(
                    (Entity) null,
                    this.posX,
                    this.posY,
                    this.posZ,
                    1.0F,
                    false,
                    this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"));
            this.setDead();
        }
    }

    public EntityOsirisBossFireball(final World world, final double x, final double y, final double z,
            final double accelX, final double accelY, final double accelZ) {
        super(world);
        this.setSize(1.0F, 1.0F);
        this.setLocationAndAngles(x, y, z, this.rotationYaw, this.rotationPitch);
        this.setPosition(x, y, z);
        final double d6 = MathHelper.sqrt_double(accelX * accelX + accelY * accelY + accelZ * accelZ);
        this.accelerationX = accelX / d6 * 0.1D;
        this.accelerationY = accelY / d6 * 0.1D;
        this.accelerationZ = accelZ / d6 * 0.1D;
    }

    public EntityOsirisBossFireball(final World world, final EntityLivingBase target, double accelX, double accelY,
            double accelZ) {
        super(world);
        this.shootingEntity = target;
        this.setSize(1.0F, 1.0F);
        this.setLocationAndAngles(target.posX, target.posY, target.posZ, target.rotationYaw, target.rotationPitch);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.yOffset = 0.0F;
        this.motionX = this.motionY = this.motionZ = 0.0D;
        accelX += this.rand.nextGaussian() * 0.4D;
        accelY += this.rand.nextGaussian() * 0.4D;
        accelZ += this.rand.nextGaussian() * 0.4D;
        final double d3 = MathHelper.sqrt_double(accelX * accelX + accelY * accelY + accelZ * accelZ);
        this.accelerationX = accelX / d3 * 0.1D;
        this.accelerationY = accelY / d3 * 0.1D;
        this.accelerationZ = accelZ / d3 * 0.1D;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setFloat("damage", this.damage);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompound) {
        super.readEntityFromNBT(tagCompound);
        this.damage = tagCompound.getFloat("damage");
    }

}
