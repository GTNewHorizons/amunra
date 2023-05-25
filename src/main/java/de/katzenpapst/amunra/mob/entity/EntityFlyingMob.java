package de.katzenpapst.amunra.mob.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.monster.IMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.api.entity.IAntiGrav;
import micdoodle8.mods.galacticraft.api.vector.Vector3;

public abstract class EntityFlyingMob extends EntityFlying implements IMob, IAntiGrav {

    public int courseChangeCooldown;
    public double waypointX;
    public double waypointY;
    public double waypointZ;
    private Entity targetedEntity;
    /** Cooldown time between target loss and new target aquirement. */
    private int aggroCooldown;
    public int prevAttackCounter;
    public int attackCounter;
    /** The explosion radius of spawned fireballs. */
    protected int explosionStrength = 1;

    protected static final float distanceToKeep = 10.0F;

    public EntityFlyingMob(final World world) {
        super(world);
    }

    @SideOnly(Side.CLIENT)
    public boolean useShootingTexture() {
        // copied over from the ghast. WTF is this?
        // oh, this seems to be where the renderer decides which texture to use
        // see net.minecraft.client.renderer.entity.RenderGhast.getEntityTexture(EntityGhast)
        return this.dataWatcher.getWatchableObjectByte(16) != 0;
    }

    abstract protected float getVisionDistance();

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable()) {
            return false;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(16, (int) 0);
    }

    abstract protected void performAttack(Entity target, double accelX, double accelY, double accelZ);

    protected void findWaypoint() {
        if (this.targetedEntity != null) {
            // attempt to move closer to the target
            final Vector3 targetVec = new Vector3(this.targetedEntity);
            Vector3 myPos = new Vector3(this);
            final Vector3 thisToTarget = myPos.difference(targetVec);
            // I don't get around sqrt'ing here
            final double distance = thisToTarget.getMagnitude();
            thisToTarget.scale(distanceToKeep / distance); // scale the vector to distanceToKeep
            myPos = targetVec.translate(thisToTarget);
            // this should be correct now...
            if (this.isCourseTraversable(myPos.x, myPos.y, myPos.z, distance)) {
                this.waypointX = myPos.x;
                this.waypointY = myPos.y;
                this.waypointZ = myPos.z;
                return;
            }
        }
        // otherwise, get a random point
        this.waypointX = this.posX + (this.rand.nextFloat() * 2.0F - 1.0F) * 16.0F;
        this.waypointY = this.posY + (this.rand.nextFloat() * 2.0F - 1.0F) * 16.0F;
        this.waypointZ = this.posZ + (this.rand.nextFloat() * 2.0F - 1.0F) * 16.0F;
    }

    @Override
    protected void updateEntityActionState() {
        if (!this.worldObj.isRemote && this.worldObj.difficultySetting == EnumDifficulty.PEACEFUL) {
            this.setDead();
        }

        this.despawnEntity();
        this.prevAttackCounter = this.attackCounter;
        final double deltaX = this.waypointX - this.posX;
        final double deltaY = this.waypointY - this.posY;
        final double deltaZ = this.waypointZ - this.posZ;
        double distanceSq = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;

        if (this.targetedEntity != null && this.targetedEntity.isDead) {
            this.targetedEntity = null;
        }

        if (distanceSq < 1.0D || distanceSq > 3600.0D) {
            // find next waypoint?
            this.findWaypoint();
        }

        if (this.courseChangeCooldown-- <= 0) {
            this.courseChangeCooldown += this.rand.nextInt(5) + 2;
            distanceSq = MathHelper.sqrt_double(distanceSq);

            if (this.isCourseTraversable(this.waypointX, this.waypointY, this.waypointZ, distanceSq)) {
                this.motionX += deltaX / distanceSq * 0.1D;
                this.motionY += deltaY / distanceSq * 0.1D;
                this.motionZ += deltaZ / distanceSq * 0.1D;
            } else {
                this.waypointX = this.posX;
                this.waypointY = this.posY;
                this.waypointZ = this.posZ;
            }
        }

        if (this.targetedEntity == null || this.aggroCooldown-- <= 0) {
            // target locked?
            this.targetedEntity = this.worldObj.getClosestVulnerablePlayerToEntity(this, this.getVisionDistance());

            if (this.targetedEntity != null) {
                this.aggroCooldown = 20;
            }
        }

        final double maxTargetDistance = 64.0D;

        if (this.targetedEntity != null
                && this.targetedEntity.getDistanceSqToEntity(this) < maxTargetDistance * maxTargetDistance) {
            this.faceEntity(this.targetedEntity, 10.0F, this.getVerticalFaceSpeed());
            final double accelX = this.targetedEntity.posX - this.posX;
            final double accelY = this.targetedEntity.boundingBox.minY + this.targetedEntity.height / 2.0F
                    - (this.posY + this.height / 2.0F);
            final double accelZ = this.targetedEntity.posZ - this.posZ;
            this.renderYawOffset = this.rotationYaw = -((float) Math.atan2(accelX, accelZ)) * 180.0F / (float) Math.PI;

            if (this.canEntityBeSeen(this.targetedEntity)) {
                if (this.attackCounter == 10) {
                    // WTF?
                    // 1007 might be some sort of an ID
                    // playSoundAtEntity(entity, GalacticraftCore.TEXTURE_PREFIX + "ambience.scaryscape", 9.0F, 1.4F)
                    // this.worldObj.playSoundAtEntity(this, AmunRa.TEXTUREPREFIX + "mob.sentryblock.fire", 1.0F, 1.0F);
                    // this.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1007, (int)this.posX, (int)this.posY,
                    // (int)this.posZ, 0);
                    // charging?
                }

                ++this.attackCounter;

                if (this.attackCounter == 20) {
                    // another one. WTF is this?
                    // this.worldObj.playSoundAtEntity(this, AmunRa.TEXTUREPREFIX + "mob.sentryblock.fire",
                    // getSoundVolume(), 1.0F);
                    this.worldObj.playSoundAtEntity(this, this.getFiringSound(), this.getSoundVolume(), 1.0F);
                    // this.worldObj.playAuxSFXAtEntity((EntityPlayer)null, 1008, (int)this.posX, (int)this.posY,
                    // (int)this.posZ, 0);
                    // this seems to be an actual attack
                    this.performAttack(this.targetedEntity, accelX, accelY, accelZ);

                    // actual attack end
                    this.attackCounter = -40;
                }
            } else if (this.attackCounter > 0) {
                --this.attackCounter;
            }
        } else {
            this.rotationPitch = this.defaultPitch;
            this.renderYawOffset = this.rotationYaw = -((float) Math.atan2(this.motionX, this.motionZ)) * 180.0F
                    / (float) Math.PI;

            if (this.attackCounter > 0) {
                --this.attackCounter;
            }
        }

        if (!this.worldObj.isRemote) {
            final byte b1 = this.dataWatcher.getWatchableObjectByte(16);
            final byte b0 = (byte) (this.attackCounter > 10 ? 1 : 0);

            if (b1 != b0) {
                this.dataWatcher.updateObject(16, Byte.valueOf(b0));
            }
        }
    }

    /**
     * True if the ghast has an unobstructed line of travel to the waypoint.
     */
    protected boolean isCourseTraversable(final double p_70790_1_, final double p_70790_3_, final double p_70790_5_,
            final double distance) {
        final double relDeltaX = (this.waypointX - this.posX) / distance;
        final double relDeltaY = (this.waypointY - this.posY) / distance;
        final double relDeltaZ = (this.waypointZ - this.posZ) / distance;
        final AxisAlignedBB axisalignedbb = this.boundingBox.copy();

        for (int i = 1; i < distance; ++i) {
            axisalignedbb.offset(relDeltaX, relDeltaY, relDeltaZ);

            if (!this.worldObj.getCollidingBoundingBoxes(this, axisalignedbb).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.rand.nextInt(20) == 0 && super.getCanSpawnHere()
                && this.worldObj.difficultySetting != EnumDifficulty.PEACEFUL;
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 1;
    }

    abstract public String getFiringSound();

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("ExplosionPower", this.explosionStrength);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);

        if (tagCompund.hasKey("ExplosionPower", 99)) {
            this.explosionStrength = tagCompund.getInteger("ExplosionPower");
        }
    }

}
