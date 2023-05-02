package de.katzenpapst.amunra.entity;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.IThrowableEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.mob.DamageSourceAR;
import micdoodle8.mods.galacticraft.api.entity.IAntiGrav;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.core.util.OxygenUtil;

abstract public class EntityBaseLaserArrow extends Entity implements IProjectile, IThrowableEntity, IAntiGrav {

    protected int xTile = -1;
    protected int yTile = -1;
    protected int zTile = -1;
    protected Block inTile;
    protected int inData;

    public int canBePickedUp;
    protected Entity shootingEntity;
    protected int ticksInGround;
    protected int ticksInAir;
    protected boolean inGround;

    // protected boolean canPassThroughWater = false;

    final private int expirationTime = 200;
    private final int knockbackStrength = 0;

    // public boolean isHot;

    public float getEntityBrightness(final float f) {
        return 1.0F;
    }

    public EntityBaseLaserArrow(final World world) {
        super(world);
        this.renderDistanceWeight = 10.0D;
        this.setSize(0.5F, 0.5F);
    }

    public EntityBaseLaserArrow(final World world, final double x, final double y, final double z) {
        super(world);
        this.renderDistanceWeight = 10.0D;
        this.setSize(0.5F, 0.5F);
        this.setPosition(x, y, z);
        this.yOffset = 0.0F;
    }

    public EntityBaseLaserArrow(final World world, final EntityLivingBase shooter, final double startX, final double startY, final double startZ) {
        super(world);
        this.shootingEntity = shooter;
        this.setSize(1.0F, 1.0F);
        this.setLocationAndAngles(startX, startY, startZ, shooter.rotationYaw, shooter.rotationPitch);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.yOffset = 0.0F;
        this.motionX = this.motionY = this.motionZ = 0.0D;
    }

    public EntityBaseLaserArrow(final World world, final EntityLivingBase shootingEntity, final EntityLivingBase target, final float randMod) {
        super(world);
        this.renderDistanceWeight = 10.0D;
        this.shootingEntity = shootingEntity;

        if (shootingEntity instanceof EntityPlayer) {
            this.canBePickedUp = 1;
        }

        this.posY = shootingEntity.posY + shootingEntity.getEyeHeight() - 0.10000000149011612D;
        final double xNew = target.posX - shootingEntity.posX;
        final double yNew = target.boundingBox.minY + target.height / 3.0F - this.posY;// why /3?
        final double zNew = target.posZ - shootingEntity.posZ;
        final double planarDistance = MathHelper.sqrt_double(xNew * xNew + zNew * zNew);

        if (planarDistance >= 1.0E-7D) {
            final float xzAngle = (float) (Math.atan2(zNew, xNew) * 180.0D / Math.PI) - 90.0F; // rotational angle in the
                                                                                         // xz-plane?
            final float yAngle = (float) -(Math.atan2(yNew, planarDistance) * 180.0D / Math.PI); // rotational angle to the y?
            final double scaledX = xNew / planarDistance;
            final double scaledY = zNew / planarDistance;
            this.setLocationAndAngles(
                    shootingEntity.posX + scaledX,
                    this.posY,
                    shootingEntity.posZ + scaledY,
                    xzAngle,
                    yAngle);
            this.yOffset = 0.0F;
            final float wtf = (float) planarDistance * 0.2F;
            this.setThrowableHeading(xNew, yNew + wtf, zNew, getSpeed(), randMod);
        }
    }

    public EntityBaseLaserArrow(final World world, final EntityLivingBase shooter, final Vector3 startVec, final EntityLivingBase target) {
        super(world);
        this.posX = startVec.x;
        this.posY = startVec.y;
        this.posZ = startVec.z;
        this.shootingEntity = shooter;
        Vector3 targetPos;
        final AxisAlignedBB aabb = target.boundingBox;

        targetPos = new Vector3(target);

        if (aabb != null) {
            // targetPos.x += aabb.maxX-aabb.minX;
            targetPos.y += aabb.maxY - aabb.minY;
            // targetPos.z += aabb.maxZ-aabb.minZ;
            // targetPos = new Vector3(aabb.maxX-aabb.minX, aabb.maxY-aabb.minY, aabb.maxZ-aabb.minZ);
        }
        // targetPos.y += target.height/2.0;

        final Vector3 thisToTarget = targetPos.difference(startVec);
        // setThrowableHeading normalizes the vector already
        this.yOffset = 0.0F;
        this.setThrowableHeading(thisToTarget.x, thisToTarget.y, thisToTarget.z, getSpeed(), 0.0F);
        // do I still need setLocationAndAngles now?
        // meh
        this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
    }

    public EntityBaseLaserArrow(final World par1World, final EntityLivingBase par2EntityLivingBase) {
        super(par1World);
        this.renderDistanceWeight = 10.0D;
        this.shootingEntity = par2EntityLivingBase;

        if (par2EntityLivingBase instanceof EntityPlayer) {
            this.canBePickedUp = 1;
        }

        this.setSize(0.5F, 0.5F);
        this.setLocationAndAngles(
                par2EntityLivingBase.posX,
                par2EntityLivingBase.posY + par2EntityLivingBase.getEyeHeight(),
                par2EntityLivingBase.posZ,
                par2EntityLivingBase.rotationYaw,
                par2EntityLivingBase.rotationPitch);
        this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
        this.posY -= 0.10000000149011612D;
        this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
        this.setPosition(this.posX, this.posY, this.posZ);
        this.yOffset = 0.0F;
        this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI)
                * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
        this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI)
                * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI);
        this.motionY = -MathHelper.sin(this.rotationPitch / 180.0F * (float) Math.PI);
        this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, getSpeed() * 1.5F, 1.0F);
    }

    abstract protected float getSpeed();

    abstract protected float getDamage();

    abstract protected boolean doesFireDamage();

    abstract public ResourceLocation getTexture();

    /**
     * This happens BEFORE the damage is applied. Add effects here
     */
    protected void onImpactEntity(final MovingObjectPosition mop) {
        if (this.doesFireDamage() && !(mop.entityHit instanceof EntityEnderman)) {
            // hm
            if (OxygenUtil.noAtmosphericCombustion(mop.entityHit.worldObj.provider)) {
                // usually, stuff doesn't burn here
                if (!OxygenUtil.isAABBInBreathableAirBlock(mop.entityHit.worldObj, mop.entityHit.boundingBox, false)) {
                    // and the entity isn't in any sealed area
                    return;
                }
            }
            mop.entityHit.setFire(2);
        }
    }

    @Override
    public void setThrowableHeading(double headingX, double headingY, double headingZ, final float speed, final float randMod) {
        final float f2 = MathHelper.sqrt_double(headingX * headingX + headingY * headingY + headingZ * headingZ);
        headingX /= f2;
        headingY /= f2;
        headingZ /= f2;
        headingX += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * randMod;
        headingY += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * randMod;
        headingZ += this.rand.nextGaussian() * (this.rand.nextBoolean() ? -1 : 1) * 0.007499999832361937D * randMod;
        headingX *= speed;
        headingY *= speed;
        headingZ *= speed;
        this.motionX = headingX;
        this.motionY = headingY;
        this.motionZ = headingZ;
        final float f3 = MathHelper.sqrt_double(headingX * headingX + headingZ * headingZ);
        this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(headingX, headingZ) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(headingY, f3) * 180.0D / Math.PI);
        this.ticksInGround = 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(final double par1, final double par3, final double par5) {
        this.motionX = par1;
        this.motionY = par3;
        this.motionZ = par5;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            final float f = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
            this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(par1, par5) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(par3, f) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            this.ticksInGround = 0;
        }
    }

    protected DamageSource getDamageSource() {
        if (this.shootingEntity == null) {
            return DamageSourceAR.causeLaserDamage("ar_laser", this, this);// ("laserArrow", this,
                                                                           // this).setProjectile();
        }
        return DamageSourceAR.causeLaserDamage("ar_laser", this, this.shootingEntity);
    }

    protected void onPassThrough(final int x, final int y, final int z) {

    }

    protected int getEntityDependentDamage(final Entity ent, final int regularDamage) {
        return regularDamage;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (ticksInAir >= expirationTime) {
            this.setDead();
            return;
        }

        // try this
        if (!this.worldObj.isRemote) {
            onPassThrough((int) posX, (int) posY, (int) posZ);
        }

        final Block block = this.worldObj.getBlock(this.xTile, this.yTile, this.zTile);

        if (!block.isAir(this.worldObj, this.xTile, this.yTile, this.zTile)) {
            block.setBlockBoundsBasedOnState(this.worldObj, this.xTile, this.yTile, this.zTile);
            final AxisAlignedBB axisalignedbb = block
                    .getCollisionBoundingBoxFromPool(this.worldObj, this.xTile, this.yTile, this.zTile);

            if (axisalignedbb != null
                    && axisalignedbb.isVecInside(Vec3.createVectorHelper(this.posX, this.posY, this.posZ))) {
                this.inGround = true;
            }
        }

        if (this.inGround) {
            this.setDead();
        } else {
            ++this.ticksInAir;
            Vec3 vec3 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            Vec3 vec31 = Vec3
                    .createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            MovingObjectPosition movingobjectposition = this.worldObj.func_147447_a(vec3, vec31, false, true, false);
            vec3 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            vec31 = Vec3
                    .createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

            if (movingobjectposition != null) {
                vec31 = Vec3.createVectorHelper(
                        movingobjectposition.hitVec.xCoord,
                        movingobjectposition.hitVec.yCoord,
                        movingobjectposition.hitVec.zCoord);
            }

            // this.rotationPitch += 1F;

            Entity entity = null;
            final List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(
                    this,
                    this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
            double d0 = 0.0D;
            int l;
            float f1;

            for (l = 0; l < list.size(); ++l) {
                final Entity entity1 = list.get(l);

                if (entity1.canBeCollidedWith() && (entity1 != this.shootingEntity || this.ticksInAir >= 5)) {
                    f1 = 0.3F;
                    final AxisAlignedBB axisalignedbb1 = entity1.boundingBox.expand(f1, f1, f1);
                    final MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept(vec3, vec31);

                    if (movingobjectposition1 != null) {
                        final double d1 = vec3.distanceTo(movingobjectposition1.hitVec);

                        if (d1 < d0 || d0 == 0.0D) {
                            entity = entity1;
                            d0 = d1;
                        }
                    }
                }
            }

            if (entity != null) {
                movingobjectposition = new MovingObjectPosition(entity);
            }

            if (movingobjectposition != null && movingobjectposition.entityHit != null
                    && movingobjectposition.entityHit instanceof EntityPlayer) {
                final EntityPlayer entityplayer = (EntityPlayer) movingobjectposition.entityHit;

                if (entityplayer.capabilities.disableDamage || this.shootingEntity instanceof EntityPlayer
                        && !((EntityPlayer) this.shootingEntity).canAttackPlayer(entityplayer)) {
                    movingobjectposition = null;
                }
            }

            float f2;
            float f3;
            final double damage = getDamage();

            if (movingobjectposition != null) {
                if (movingobjectposition.entityHit != null) {
                    // this seems to be some sort of bonus damage
                    f2 = MathHelper.sqrt_double(
                            this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                    final int dmgValue = getEntityDependentDamage(
                            movingobjectposition.entityHit,
                            MathHelper.ceiling_double_int(f2 * damage));

                    final DamageSource damagesource = this.getDamageSource();

                    if (!this.worldObj.isRemote) {
                        this.onImpactEntity(movingobjectposition);
                    }

                    if (movingobjectposition.entityHit.attackEntityFrom(damagesource, dmgValue)) {
                        if (movingobjectposition.entityHit instanceof EntityLivingBase) {
                            final EntityLivingBase entitylivingbase = (EntityLivingBase) movingobjectposition.entityHit;

                            if (this.knockbackStrength > 0) {
                                f3 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);

                                if (f3 > 0.0F) {
                                    movingobjectposition.entityHit.addVelocity(
                                            this.motionX * this.knockbackStrength * 0.6000000238418579D / f3,
                                            0.1D,
                                            this.motionZ * this.knockbackStrength * 0.6000000238418579D / f3);
                                }
                            }

                            if (this.shootingEntity != null) {
                                EnchantmentHelper.func_151384_a(entitylivingbase, this.shootingEntity);
                                EnchantmentHelper
                                        .func_151385_b((EntityLivingBase) this.shootingEntity, entitylivingbase);
                            }

                            if (this.shootingEntity != null && movingobjectposition.entityHit != this.shootingEntity
                                    && movingobjectposition.entityHit instanceof EntityPlayer
                                    && this.shootingEntity instanceof EntityPlayerMP) {
                                ((EntityPlayerMP) this.shootingEntity).playerNetServerHandler
                                        .sendPacket(new S2BPacketChangeGameState(6, 0.0F));
                            }
                        }

                        if (!(movingobjectposition.entityHit instanceof EntityEnderman)) {
                            this.setDead();
                        }
                    } else {
                        this.setDead();
                        /*
                         * // reflexion? this.motionX *= -0.10000000149011612D; this.motionY *= -0.10000000149011612D;
                         * this.motionZ *= -0.10000000149011612D; this.rotationYaw += 180.0F; this.prevRotationYaw +=
                         * 180.0F; this.ticksInAir = 0;
                         */
                    }
                    // ASD END
                } else {
                    this.xTile = movingobjectposition.blockX;
                    this.yTile = movingobjectposition.blockY;
                    this.zTile = movingobjectposition.blockZ;
                    this.inTile = this.worldObj.getBlock(this.xTile, this.yTile, this.zTile);
                    this.inData = this.worldObj.getBlockMetadata(this.xTile, this.yTile, this.zTile);
                    this.motionX = (float) (movingobjectposition.hitVec.xCoord - this.posX);
                    this.motionY = (float) (movingobjectposition.hitVec.yCoord - this.posY);
                    this.motionZ = (float) (movingobjectposition.hitVec.zCoord - this.posZ);
                    f2 = MathHelper.sqrt_double(
                            this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                    this.posX -= this.motionX / f2 * 0.05000000074505806D;
                    this.posY -= this.motionY / f2 * 0.05000000074505806D;
                    this.posZ -= this.motionZ / f2 * 0.05000000074505806D;
                    this.inGround = true;

                    if (!this.inTile.isAir(this.worldObj, this.xTile, this.yTile, this.zTile)) {
                        if (!this.worldObj.isRemote) {
                            this.onImpactBlock(this.worldObj, this.xTile, this.yTile, this.zTile);
                        }
                        this.inTile.onEntityCollidedWithBlock(this.worldObj, this.xTile, this.yTile, this.zTile, this);
                    }
                }
            }

            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);

            final float f4 = 0.99F;
            f1 = 0.05F;

            this.motionX *= f4;
            this.motionY *= f4;
            this.motionZ *= f4;
            // this.motionY -= WorldUtil.getGravityForEntity(this);
            this.setPosition(this.posX, this.posY, this.posZ);
            this.func_145775_I();
        }
    }

    protected void onPassWater() {}

    protected void onImpactBlock(final World worldObj, final int xTile2, final int yTile2, final int zTile2) {

    }

    @Override
    protected void entityInit() {
        this.dataWatcher.addObject(16, Integer.valueOf(0));
    }

    /*
     * public boolean isHot() { return this.dataWatcher.getWatchableObjectInt(16) == 1; } public void setHot(boolean
     * isHot) { this.dataWatcher.updateObject(16, isHot ? 1 : 0); }
     */

    @Override
    protected void readEntityFromNBT(final NBTTagCompound nbttagcompound) {
        this.xTile = nbttagcompound.getShort("xTile");
        this.yTile = nbttagcompound.getShort("yTile");
        this.zTile = nbttagcompound.getShort("zTile");
        this.ticksInAir = nbttagcompound.getShort("life");
        this.inTile = Block.getBlockById(nbttagcompound.getByte("inTile") & 255);
        this.inData = nbttagcompound.getByte("inData") & 255;

        this.inGround = nbttagcompound.getByte("inGround") == 1;

    }

    @Override
    protected void writeEntityToNBT(final NBTTagCompound nbttagcompound) {
        nbttagcompound.setShort("xTile", (short) this.xTile);
        nbttagcompound.setShort("yTile", (short) this.yTile);
        nbttagcompound.setShort("zTile", (short) this.zTile);
        nbttagcompound.setShort("life", (short) this.ticksInAir);
        nbttagcompound.setByte("inTile", (byte) Block.getIdFromBlock(this.inTile));
        nbttagcompound.setByte("inData", (byte) this.inData);
    }

    @Override
    public void onCollideWithPlayer(final EntityPlayer par1EntityPlayer) {
        if (!this.worldObj.isRemote && this.inGround) {
            this.setDead();
        }
    }

    @Override
    public boolean canAttackWithItem() {
        return false;
    }

    /**
     * Gets the entity that threw/created this entity.
     * 
     * @return The owner instance, Null if none.
     */
    @Override
    public Entity getThrower() {
        return this.shootingEntity;
    }

    /**
     * Sets the entity that threw/created this entity.
     * 
     * @param entity The new thrower/creator.
     */
    @Override
    public void setThrower(final Entity entity) {
        this.shootingEntity = entity;
    }

}
