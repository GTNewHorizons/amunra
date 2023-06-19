package de.katzenpapst.amunra.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.mob.DamageSourceAR;

public class EntityCryoArrow extends EntityBaseLaserArrow {

    private static final ResourceLocation arrowTextures = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/entity/cryoarrow.png");

    public EntityCryoArrow(final World world) {
        super(world);
    }

    public EntityCryoArrow(final World world, final double x, final double y, final double z) {
        super(world, x, y, z);
    }

    @Override
    protected int getEntityDependentDamage(final Entity ent, final int regularDamage) {
        if (ent instanceof EntityBlaze) {
            return regularDamage * 2;
        }
        return regularDamage;
    }

    public EntityCryoArrow(final World world, final EntityLivingBase shootingEntity, final EntityLivingBase target,
            final float randMod) {
        super(world, shootingEntity, target, randMod);
    }

    @Override
    protected void onPassThrough(final int x, final int y, final int z) {

        final Block b = this.worldObj.getBlock(x, y, z);

        if (b == Blocks.water) {
            this.worldObj.setBlock(x, y, z, Blocks.ice);
            this.inWater = false;
        }
        if (b == Blocks.lava) {
            this.playSound("random.fizz", 0.7F, 1.6F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
            this.worldObj.setBlock(x, y, z, Blocks.obsidian);
        }
    }

    public EntityCryoArrow(final World par1World, final EntityLivingBase par2EntityLivingBase) {
        super(par1World, par2EntityLivingBase);
    }

    @Override
    protected float getSpeed() {
        return 3.0F;
    }

    @Override
    protected float getDamage() {
        return 1.0F;
    }

    @Override
    protected boolean doesFireDamage() {
        return false;
    }

    @Override
    public ResourceLocation getTexture() {
        return arrowTextures;
    }

    @Override
    protected void onImpactEntity(final MovingObjectPosition mop) {
        if (mop.entityHit instanceof EntityLivingBase entityLiving) {
            if (entityLiving.isBurning()) {
                entityLiving.extinguish();
            }
            entityLiving.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100, 3));
        }
    }

    @Override
    protected void onImpactBlock(final World worldObj, final int x, final int y, final int z) {
        final Block block = worldObj.getBlock(x, y, z);

        /*
         * if(block == Blocks.water) { worldObj.setBlock(x, y, z, Blocks.ice); } else
         */ if (block == Blocks.lava) {
            worldObj.setBlock(x, y, z, Blocks.obsidian);
        } else if (block == Blocks.fire) {
            worldObj.setBlock(x, y, z, Blocks.air);
        } else if (worldObj.getBlock(x, y + 1, z) == Blocks.fire) {
            worldObj.setBlock(x, y + 1, z, Blocks.air);
        }
    }

    @Override
    protected DamageSource getDamageSource() {
        if (this.shootingEntity == null) {
            return DamageSourceAR.causeLaserDamage("ar_coldray", this, this);
        }
        return DamageSourceAR.causeLaserDamage("ar_coldray", this, this.shootingEntity);
    }

}
