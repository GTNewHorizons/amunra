package de.katzenpapst.amunra.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.mob.DamageSourceAR;
import de.katzenpapst.amunra.world.WorldHelper;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.core.util.OxygenUtil;

public class EntityLaserArrow extends EntityBaseLaserArrow {

    protected float damage = 2.0F;

    protected boolean doesFireDamage = true;

    private static final ResourceLocation ARROW_TEXTURE = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/entity/laserarrow.png");

    public EntityLaserArrow(final World world, final EntityLivingBase shooter, final Vector3 startVec,
            final EntityLivingBase target) {
        super(world, shooter, startVec, target);
    }

    public EntityLaserArrow(final World world) {
        super(world);
    }

    public EntityLaserArrow(final World world, final EntityLivingBase shooter, final double startX, final double startY,
            final double startZ) {
        super(world, shooter, startX, startY, startZ);
    }

    public EntityLaserArrow(final World world, final double x, final double y, final double z) {
        super(world, x, y, z);
    }

    public EntityLaserArrow(final World world, final EntityLivingBase shootingEntity, final EntityLivingBase target,
            final float randMod) {
        super(world, shootingEntity, target, randMod);
    }

    public EntityLaserArrow(final World par1World, final EntityLivingBase par2EntityLivingBase) {
        super(par1World, par2EntityLivingBase);
    }

    @Override
    protected float getSpeed() {
        return 3.0F;
    }

    @Override
    protected float getDamage() {
        return this.damage;
    }

    public void setDoesFireDamage(final boolean set) {
        this.doesFireDamage = set;
    }

    public void setDamage(final float newDmg) {
        this.damage = newDmg;
    }

    @Override
    protected boolean doesFireDamage() {
        return this.doesFireDamage;
    }

    @Override
    public ResourceLocation getTexture() {
        return ARROW_TEXTURE;
    }

    @Override
    protected int getEntityDependentDamage(final Entity ent, final int regularDamage) {
        if (ent instanceof EntityBlaze) {
            return Math.max(regularDamage / 2, 1);
        }
        return regularDamage;
    }

    @Override
    protected void onImpactBlock(final World worldObj, final int x, final int y, final int z) {
        final Block block = worldObj.getBlock(x, y, z);
        final int meta = worldObj.getBlockMetadata(x, y, z);

        // first tests first

        if (block == Blocks.ice) {
            worldObj.setBlock(x, y, z, Blocks.water, 0, 3);
            return;
        }

        if (block == Blocks.snow || block == Blocks.snow_layer) {
            worldObj.setBlock(x, y, z, Blocks.air, 0, 3);
            return;
        }

        final ItemStack smeltResult = FurnaceRecipes.smelting().getSmeltingResult(new ItemStack(block, 1, meta));
        if (smeltResult != null) {

            final int blockId = Item.getIdFromItem(smeltResult.getItem());
            if (blockId > 0) {
                final Block b = Block.getBlockById(blockId);
                if (b != Blocks.air) {
                    /**
                     * Sets the block ID and metadata at a given location. Args: X, Y, Z, new block ID, new metadata,
                     * flags. Flag 1 will cause a block update. Flag 2 will send the change to clients (you almost
                     * always want this). Flag 4 prevents the block from being re-rendered, if this is a client world.
                     * Flags can be added together.
                     */
                    worldObj.setBlock(x, y, z, b, smeltResult.getItemDamage(), 3);
                    return;
                }
            }
        }
        if (OxygenUtil.noAtmosphericCombustion(worldObj.provider)) {

            if (OxygenUtil
                    .isAABBInBreathableAirBlock(worldObj, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1))) {
                WorldHelper.setFireToBlock(worldObj, x, y, z, this.posX, this.posY, this.posZ);
            }
        } else {
            WorldHelper.setFireToBlock(worldObj, x, y, z, this.posX, this.posY, this.posZ);
        }
    }

    protected void setFireToBlock(final World worldObj, final int x, final int y, final int z) {
        // omg

        final double deltaX = x + 0.5 - this.posX;
        final double deltaY = y + 0.5 - this.posY;
        final double deltaZ = z + 0.5 - this.posZ;

        final double deltaXabs = Math.abs(deltaX);
        final double deltaYabs = Math.abs(deltaY);
        final double deltaZabs = Math.abs(deltaZ);

        if (deltaXabs > deltaYabs) {
            if (deltaXabs > deltaZabs) {
                if (deltaX < 0) {
                    worldObj.setBlock(x + 1, y, z, Blocks.fire);
                } else {
                    worldObj.setBlock(x - 1, y, z, Blocks.fire);
                }
            } else if (deltaZ < 0) {
                worldObj.setBlock(x, y, z + 1, Blocks.fire);
            } else {
                worldObj.setBlock(x, y, z - 1, Blocks.fire);
            }
        } else if (deltaYabs > deltaZabs) {
            if (deltaY < 0) {
                worldObj.setBlock(x, y + 1, z, Blocks.fire);
            } else {
                // is there even fire from below?
                worldObj.setBlock(x, y - 1, z, Blocks.fire);
            }
        } else if (deltaZ < 0) {
            worldObj.setBlock(x, y, z + 1, Blocks.fire);
        } else {
            worldObj.setBlock(x, y, z - 1, Blocks.fire);
        }

    }

    @Override
    protected void onPassThrough(final int x, final int y, final int z) {
        final Block b = this.worldObj.getBlock(x, y, z);

        if (b == Blocks.water) {
            this.worldObj.setBlock(x, y, z, Blocks.air);
            this.playSound("random.fizz", 0.7F, 1.6F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F);
            this.inWater = false;
        }
    }

    @Override
    protected DamageSource getDamageSource() {
        if (this.shootingEntity == null) {
            return DamageSourceAR.causeLaserDamage("ar_heatray", this, this);
        }
        return DamageSourceAR.causeLaserDamage("ar_heatray", this, this.shootingEntity);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        this.damage = tagCompund.getFloat("damage");
        this.doesFireDamage = tagCompund.getBoolean("fireDmg");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setFloat("damage", this.damage);
        tagCompound.setBoolean("fireDmg", this.doesFireDamage);
    }

}
