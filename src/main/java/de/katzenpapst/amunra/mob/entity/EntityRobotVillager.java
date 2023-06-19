package de.katzenpapst.amunra.mob.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.village.Village;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.mob.RobotVillagerProfession;
import micdoodle8.mods.galacticraft.api.entity.IEntityBreathable;

public class EntityRobotVillager extends EntityAgeable implements IEntityBreathable, INpc, IMerchant {

    private int randomTickDivider;
    private boolean isMating;
    private boolean isPlaying;
    private Village villageObj;
    private EntityPlayer buyingPlayer;
    private MerchantRecipeList buyingList = null;
    private int wealth;
    private boolean field_82190_bM;
    private int timeUntilReset = 0;
    private boolean needsInit;

    /*
     * For now, I'll just keep the professions in here
     */

    protected static List<ResourceLocation> professionIcons = new ArrayList<>();

    public EntityRobotVillager(final World par1World) {
        this(par1World, -1);
    }

    public EntityRobotVillager(final World par1World, final int profession) {
        super(par1World);

        this.randomTickDivider = 0;
        this.isMating = false;
        this.needsInit = true;
        this.isPlaying = false;
        this.villageObj = null;
        this.setSize(0.6F, 2.35F);
        this.getNavigator().setBreakDoors(true);
        this.getNavigator().setAvoidsWater(true);
        this.tasks.addTask(2, new EntityAIMoveIndoors(this));
        this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
        this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.3F));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 15.0F, 1.0F));
        this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityRobotVillager.class, 15.0F, 0.05F));
        this.tasks.addTask(9, new EntityAIWander(this, 0.3F));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 15.0F));

        if (profession != -1) {
            this.setProfession(profession);
        }
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
    public boolean canBreatheUnderwater() {
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

                if (this.field_82190_bM) {
                    this.field_82190_bM = false;
                    this.villageObj.setDefaultPlayerReputation(5);
                }
            }
        }

        //// ASD
        if (!this.isTrading() && this.timeUntilReset > 0) {
            --this.timeUntilReset;

            if (this.timeUntilReset <= 0) {
                if (this.needsInit) {
                    if (this.buyingList.size() > 1) {

                        for (MerchantRecipe merchantrecipe : (Iterable<MerchantRecipe>) this.buyingList) {
                            if (merchantrecipe.isRecipeDisabled()) {
                                merchantrecipe.func_82783_a(this.rand.nextInt(6) + this.rand.nextInt(6) + 2);
                            }
                        }
                    }

                    this.addDefaultEquipmentAndRecipies(1);
                    this.needsInit = false;

                    /*
                     * if (this.villageObj != null && this.lastBuyingPlayer != null) {
                     * this.worldObj.setEntityState(this, (byte)14);
                     * this.villageObj.setReputationForPlayer(this.lastBuyingPlayer, 1); }
                     */
                }

                this.addPotionEffect(new PotionEffect(Potion.regeneration.id, 200, 0));
            }
        }
        //// ASD

        super.updateAITick();
    }

    /**
     * based on the villagers profession add items, equipment, and recipies adds par1 random items to the list of things
     * that the villager wants to buy. (at most 1 of each wanted type is added)
     */
    @SuppressWarnings("unchecked")
    private void addDefaultEquipmentAndRecipies(final int p_70950_1_) {
        // now do the recipes
        if (this.buyingList == null) {
            this.buyingList = new MerchantRecipeList();
        }
        this.buyingList.clear();

        final RobotVillagerProfession prof = RobotVillagerProfession.getProfession(this.getProfession());
        final MerchantRecipeList baseList = prof.getRecipeList();
        switch (baseList.size()) {
            case 0 -> {}
            case 1 -> this.buyingList.add(baseList.get(0));
            default -> {
                // for now have just 1 offer
                final int numOffers = this.worldObj.rand.nextInt(baseList.size() - 1) + 1;// ensure it's at least 1
                final HashMap<Integer, Boolean> uniqCache = new HashMap<>();
                for (int i = 0; i < numOffers; i++) {
                    final int randOffer = this.worldObj.rand.nextInt(baseList.size());
                    if (uniqCache.containsKey(randOffer)) {
                        continue;
                    }
                    uniqCache.put(randOffer, true);
                    this.buyingList.add(baseList.get(randOffer));
                }
            }
        }
    }

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    @Override
    public boolean interact(EntityPlayer p_70085_1_) {
        final ItemStack itemstack = p_70085_1_.inventory.getCurrentItem();
        final boolean flag = itemstack != null && itemstack.getItem() == Items.spawn_egg;

        if (this.getRecipes(p_70085_1_).size() == 0) {
            this.sayNo();
            return super.interact(p_70085_1_);
        }

        if (flag || !this.isEntityAlive() || this.isTrading() || this.isChild() || p_70085_1_.isSneaking()) {
            return super.interact(p_70085_1_);
        }
        if (!this.worldObj.isRemote) {
            this.setCustomer(p_70085_1_);
            p_70085_1_.displayGUIMerchant(this, this.getProfessionName());
        }

        return true;
    }

    public String getProfessionName() {
        final RobotVillagerProfession prof = RobotVillagerProfession.getProfession(this.getProfession());
        return StatCollector.translateToLocal("profession." + prof.getName() + ".name");
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
    public IEntityLivingData onSpawnWithEgg(IEntityLivingData p_110161_1_) {
        p_110161_1_ = super.onSpawnWithEgg(p_110161_1_);
        this.setProfession(RobotVillagerProfession.getRandomProfession(this.worldObj.rand));
        return p_110161_1_;
    }

    @Override
    protected String getLivingSound() {
        return AmunRa.TEXTUREPREFIX + "mob.robotvillager.idle";
    }

    @Override
    protected String getHurtSound() {
        return AmunRa.TEXTUREPREFIX + "mob.robotvillager.hit";
    }

    @Override
    protected String getDeathSound() {
        return AmunRa.TEXTUREPREFIX + "mob.robotvillager.death";
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

            if (p_70604_1_ instanceof EntityPlayer) {
                byte b0 = -1;

                if (this.isChild()) {
                    b0 = -3;
                }

                this.villageObj.setReputationForPlayer(p_70604_1_.getCommandSenderName(), b0);

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
                if (entity instanceof EntityPlayer) {
                    this.villageObj.setReputationForPlayer(entity.getCommandSenderName(), -2);
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

    @Override
    public void setCustomer(EntityPlayer p_70932_1_) {
        this.buyingPlayer = p_70932_1_;
    }

    @Override
    public EntityPlayer getCustomer() {
        return this.buyingPlayer;
    }

    public boolean isTrading() {
        return this.buyingPlayer != null;
    }

    @Override
    public void useRecipe(MerchantRecipe p_70933_1_) {
        p_70933_1_.incrementToolUses();

        if (p_70933_1_.getItemToBuy().getItem() == Items.emerald) {
            this.wealth += p_70933_1_.getItemToBuy().stackSize;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleHealthUpdate(byte p_70103_1_) {
        switch (p_70103_1_) {
            case 12 -> this.generateRandomParticles("heart");
            case 13 -> this.generateRandomParticles("angryVillager");
            case 14 -> this.generateRandomParticles("happyVillager");
            default -> super.handleHealthUpdate(p_70103_1_);
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

    public void func_82187_q() {
        this.field_82190_bM = true;
    }

    public EntityRobotVillager func_90012_b(final EntityAgeable par1EntityAgeable) {
        return new EntityRobotVillager(this.worldObj);
    }

    @Override
    public EntityAgeable createChild(EntityAgeable p_90011_1_) {
        return this.func_90012_b(p_90011_1_);
    }

    @Override
    public boolean canBreath() {
        return true;
    }

    @Override
    public MerchantRecipeList getRecipes(EntityPlayer p_70934_1_) {
        if (this.buyingList == null) {
            this.addDefaultEquipmentAndRecipies(1);
        }

        return this.buyingList;
    }

    @Override
    public void setRecipes(MerchantRecipeList p_70930_1_) {}

    public void sayYes() {
        this.playSound(AmunRa.TEXTUREPREFIX + "mob.robotvillager.yay", this.getSoundVolume(), this.getSoundPitch());
    }

    public void sayNo() {
        this.playSound(AmunRa.TEXTUREPREFIX + "mob.robotvillager.nope", this.getSoundVolume(), this.getSoundPitch());
    }

    /**
     * Seems to be for playing the yes and no sounds
     */
    @Override
    public void func_110297_a_(ItemStack p_110297_1_) {
        if (!this.worldObj.isRemote && this.livingSoundTime > -this.getTalkInterval() + 20) {
            this.livingSoundTime = -this.getTalkInterval();

            if (p_110297_1_ != null) {
                // return
                this.sayYes();
            } else {
                this.sayNo();
            }
        }

    }
}
