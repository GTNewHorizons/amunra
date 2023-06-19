package de.katzenpapst.amunra.tile;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.helper.NbtHelper;
import de.katzenpapst.amunra.mob.entity.EntityMummyBoss;
import de.katzenpapst.amunra.mob.entity.IAmunRaBoss;
import de.katzenpapst.amunra.vec.Vector3int;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedCreeper;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSkeleton;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSpider;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedZombie;
import micdoodle8.mods.galacticraft.core.tile.TileEntityAdvanced;

public class TileEntityBossDungeonSpawner extends TileEntityAdvanced implements ITileDungeonSpawner {

    protected Class<? extends IAmunRaBoss> bossClass;
    protected IAmunRaBoss boss;
    protected boolean spawned = false;
    protected boolean isBossDefeated = false;

    protected AxisAlignedBB roomArea = null;

    public TileEntityBossDungeonSpawner() {
        this.bossClass = EntityMummyBoss.class;
    }

    public List<Class<? extends EntityLiving>> getDisabledCreatures() {
        final List<Class<? extends EntityLiving>> list = new ArrayList<>();
        list.add(EntityEvolvedSkeleton.class);
        list.add(EntityEvolvedZombie.class);
        list.add(EntityEvolvedSpider.class);
        list.add(EntityEvolvedCreeper.class);
        return list;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (this.roomArea == null) {
            return;
        }

        if (!this.worldObj.isRemote) {
            if (this.boss != null && ((Entity) this.boss).isDead) {
                this.boss = null;
                this.spawned = false;
            }

            final List<EntityLivingBase> entitiesInRoom = this.worldObj
                    .getEntitiesWithinAABB(EntityLivingBase.class, this.roomArea);
            int numPlayers = 0;
            boolean isBossInRoom = false;
            for (final Entity ent : entitiesInRoom) {
                if (ent instanceof EntityPlayer) {
                    numPlayers++;
                } else if (this.bossClass.isInstance(ent)) {
                    final IAmunRaBoss curBoss = (IAmunRaBoss) ent;
                    if (this.boss == null && curBoss.getSpawner() == this) {
                        this.boss = curBoss;
                        isBossInRoom = true;
                    } else if (this.boss != null && this.boss.equals(curBoss)) {
                        isBossInRoom = true;
                    }
                } else if (this.getDisabledCreatures().contains(ent.getClass())) {
                    ent.setDead();
                }
            }

            if (numPlayers > 0) {

                if (this.boss == null && !this.isBossDefeated && !this.spawned) {
                    // try spawning the boss
                    try {
                        final Constructor<?> c = this.bossClass.getConstructor(World.class);
                        this.boss = (IAmunRaBoss) c.newInstance(this.worldObj);
                        ((Entity) this.boss).setPosition(this.xCoord + 0.5, this.yCoord + 1.0, this.zCoord + 0.5);
                        this.boss.setRoomArea(this.roomArea);
                        this.boss.setSpawner(this);
                        this.spawned = true;
                        isBossInRoom = true;
                        this.worldObj.spawnEntityInWorld((Entity) this.boss);
                    } catch (final Exception e) {
                        AmunRa.LOGGER.warn("Failed to spawn boss", e);
                    }
                }
            } else // check if we have a boss and the player walked out
                if (this.boss != null && !this.isBossDefeated && this.spawned) {
                    // despawn boss

                    this.boss.despawnBoss();
                    this.boss = null;
                    this.spawned = false;
                }

            if (!isBossInRoom && this.spawned && this.boss != null) {
                // do something?
                this.boss.despawnBoss();
                this.boss = null;
                this.spawned = false;
            }
        }
    }

    public void playSpawnSound(final Entity entity) {
        this.worldObj.playSoundAtEntity(entity, GalacticraftCore.TEXTURE_PREFIX + "ambience.scaryscape", 9.0F, 1.4F);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        this.spawned = compound.getBoolean("spawned");
        this.isBossDefeated = compound.getBoolean("defeated");

        try {
            this.bossClass = (Class<? extends IAmunRaBoss>) Class.forName(compound.getString("bossClass"));
        } catch (final Exception e) {
            AmunRa.LOGGER.warn("Failed to parse bossClass from NBT data", e);
        }

        if (compound.hasKey("roomArea")) {
            this.roomArea = NbtHelper.readAABB(compound.getCompoundTag("roomArea"));
        }
        /*
         * if(nbt.hasKey("spawnedBoss")) { Entity ent = this.worldObj.getEntityByID(nbt.getInteger("spawnedBoss"));
         * if(ent != null && ent instanceof IAmunRaBoss) { boss = (IAmunRaBoss)ent; } }
         */

    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setBoolean("spawned", this.spawned);
        compound.setBoolean("defeated", this.isBossDefeated);

        if (this.roomArea != null) {
            compound.setTag("roomArea", NbtHelper.getAsNBT(this.roomArea));
        }

        /*
         * if(boss != null) { int id = ((Entity)boss).getEntityId(); nbt.setInteger("spawnedBoss", id); }
         */
    }

    @Override
    public void setSpawnedBoss(final IAmunRaBoss boss) {
        this.boss = boss;
    }

    @Override
    public IAmunRaBoss getSpawnedBoss() {
        return this.boss;
    }

    @Override
    public Vector3int getBlockPosition() {
        return new Vector3int(this.xCoord, this.yCoord, this.zCoord);
    }

    @Override
    public double getPacketRange() {
        return 0;
    }

    @Override
    public int getPacketCooldown() {
        return 0;
    }

    @Override
    public boolean isNetworkedTile() {
        return false;
    }

    @Override
    public AxisAlignedBB getRoomArea() {
        return this.roomArea;
    }

    @Override
    public void setRoomArea(final AxisAlignedBB aabb) {
        this.roomArea = aabb.copy();
    }

    @Override
    public void onBossDefeated() {
        this.isBossDefeated = true;
        this.spawned = false;
        this.boss = null;
        // attempt selfdestruction
        this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
    }

    @Override
    public void setBossClass(final Class<? extends IAmunRaBoss> theClass) {
        this.bossClass = theClass;
    }

}
