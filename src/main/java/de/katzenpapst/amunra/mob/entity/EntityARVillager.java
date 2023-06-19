package de.katzenpapst.amunra.mob.entity;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.village.Village;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.api.entity.IEntityBreathable;
import micdoodle8.mods.galacticraft.api.world.IAtmosphericGas;

public class EntityARVillager extends EntityAgeable implements IEntityBreathable, IEntityNonOxygenBreather {

    private int randomTickDivider;
    private boolean isMating;
    private boolean isPlaying;
    private Village villageObj;
    private EntityPlayer buyingPlayer;
    private MerchantRecipeList buyingList;
    private int wealth;
    private boolean isLookingForHome;

    public EntityARVillager(final World par1World) {
        super(par1World);
        this.randomTickDivider = 0;
        this.isMating = false;
        this.isPlaying = false;
        this.villageObj = null;
        this.setSize(0.6F, 2.35F);
        this.getNavigator().setBreakDoors(true);
        this.getNavigator().setAvoidsWater(true);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIMoveIndoors(this));
        this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.3F));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 15.0F, 1.0F));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityVillager.class, 15.0F, 0.05F));
        this.tasks.addTask(9, new EntityAIWander(this, 0.3F));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 15.0F));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.5D);
    }

    @Override
    public boolean isAIEnabled() {
        return true;
    }

    @Override
    protected void updateAITick() {
        if (--this.randomTickDivider <= 0) {
            this.worldObj.villageCollectionObj.addVillagerPosition(
                    MathHelper.floor_double(this.posX),
                    MathHelper.floor_double(this.posY),
                    MathHelper.floor_double(this.posZ));
            this.randomTickDivider = 70 + this.rand.nextInt(50);
            this.villageObj = this.worldObj.villageCollectionObj.findNearestVillage(
                    MathHelper.floor_double(this.posX),
                    MathHelper.floor_double(this.posY),
                    MathHelper.floor_double(this.posZ),
                    32);

            if (this.villageObj == null) {
                this.detachHome();
            } else {
                final ChunkCoordinates chunkcoordinates = this.villageObj.getCenter();
                this.setHomeArea(
                        chunkcoordinates.posX,
                        chunkcoordinates.posY,
                        chunkcoordinates.posZ,
                        (int) (this.villageObj.getVillageRadius() * 0.6F));

                if (this.isLookingForHome) {
                    this.isLookingForHome = false;
                    this.villageObj.setDefaultPlayerReputation(5);
                }
            }
        }

        super.updateAITick();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(16, 0);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("Profession", this.getProfession());
        tagCompound.setInteger("Riches", this.wealth);

        if (this.buyingList != null) {
            tagCompound.setTag("Offers", this.buyingList.getRecipiesAsTags());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        this.setProfession(tagCompund.getInteger("Profession"));
        this.wealth = tagCompund.getInteger("Riches");

        if (tagCompund.hasKey("Offers")) {
            final NBTTagCompound nbttagcompound1 = tagCompund.getCompoundTag("Offers");
            this.buyingList = new MerchantRecipeList(nbttagcompound1);
        }
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    protected String getLivingSound() {
        return "mob.villager.idle";
    }

    @Override
    protected String getHurtSound() {
        return "mob.villager.hit";
    }

    @Override
    protected String getDeathSound() {
        return "mob.villager.death";
    }

    public void setProfession(final int par1) {
        this.dataWatcher.updateObject(16, par1);
    }

    public int getProfession() {
        return this.dataWatcher.getWatchableObjectInt(16);
    }

    public boolean isMating() {
        return this.isMating;
    }

    public void setMating(final boolean par1) {
        this.isMating = par1;
    }

    public void setPlaying(final boolean par1) {
        this.isPlaying = par1;
    }

    public boolean isPlaying() {
        return this.isPlaying;
    }

    @Override
    public void setRevengeTarget(EntityLivingBase p_70604_1_) {
        super.setRevengeTarget(p_70604_1_);

        if (this.villageObj != null && p_70604_1_ != null) {
            this.villageObj.addOrRenewAgressor(p_70604_1_);

            if (p_70604_1_ instanceof EntityPlayer player) {
                byte b0 = -1;

                if (this.isChild()) {
                    b0 = -3;
                }

                this.villageObj.setReputationForPlayer(player.getCommandSenderName(), b0);

                if (this.isEntityAlive()) {
                    this.worldObj.setEntityState(this, (byte) 13);
                }
            }
        }
    }

    @Override
    public void onDeath(DamageSource p_70645_1_) {
        if (this.villageObj != null) {
            final Entity entity = p_70645_1_.getEntity();

            if (entity != null) {
                if (entity instanceof EntityPlayer player) {
                    this.villageObj.setReputationForPlayer(player.getCommandSenderName(), -2);
                } else if (entity instanceof IMob) {
                    this.villageObj.endMatingSeason();
                }
            } else {
                final EntityPlayer entityplayer = this.worldObj.getClosestPlayerToEntity(this, 16.0D);

                if (entityplayer != null) {
                    this.villageObj.endMatingSeason();
                }
            }
        }

        super.onDeath(p_70645_1_);
    }

    public void setCustomer(final EntityPlayer par1EntityPlayer) {
        this.buyingPlayer = par1EntityPlayer;
    }

    public EntityPlayer getCustomer() {
        return this.buyingPlayer;
    }

    public boolean isTrading() {
        return this.buyingPlayer != null;
    }

    public void useRecipe(final MerchantRecipe par1MerchantRecipe) {
        par1MerchantRecipe.incrementToolUses();

        if (par1MerchantRecipe.getItemToBuy().getItem() == Items.emerald) {
            this.wealth += par1MerchantRecipe.getItemToBuy().stackSize;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleHealthUpdate(byte p_70103_1_) {
        switch (p_70103_1_) {
            case 12:
                this.generateRandomParticles("heart");
                break;
            case 13:
                this.generateRandomParticles("angryVillager");
                break;
            case 14:
                this.generateRandomParticles("happyVillager");
                break;
            default:
                super.handleHealthUpdate(p_70103_1_);
                break;
        }
    }

    @SideOnly(Side.CLIENT)
    private void generateRandomParticles(final String par1Str) {
        for (int i = 0; i < 5; ++i) {
            final double d0 = this.rand.nextGaussian() * 0.02D;
            final double d1 = this.rand.nextGaussian() * 0.02D;
            final double d2 = this.rand.nextGaussian() * 0.02D;
            this.worldObj.spawnParticle(
                    par1Str,
                    this.posX + this.rand.nextFloat() * this.width * 2.0F - this.width,
                    this.posY + 1.0D + this.rand.nextFloat() * this.height,
                    this.posZ + this.rand.nextFloat() * this.width * 2.0F - this.width,
                    d0,
                    d1,
                    d2);
        }
    }

    public void setLookingForHome() {
        this.isLookingForHome = true;
    }

    public EntityARVillager func_90012_b(final EntityAgeable par1EntityAgeable) {
        return new EntityARVillager(this.worldObj);
    }

    @Override
    public EntityAgeable createChild(final EntityAgeable par1EntityAgeable) {
        return this.func_90012_b(par1EntityAgeable);
    }

    @Override
    public boolean canBreath() {
        return true;
    }

    @Override
    public boolean canBreatheIn(final ArrayList<IAtmosphericGas> atmosphere, final boolean isInSealedArea) {
        return atmosphere.contains(IAtmosphericGas.METHANE);
    }
}
