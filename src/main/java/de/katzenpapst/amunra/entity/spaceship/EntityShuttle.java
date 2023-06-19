package de.katzenpapst.amunra.entity.spaceship;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.helper.CoordHelper;
import de.katzenpapst.amunra.helper.ShuttleTeleportHelper;
import de.katzenpapst.amunra.item.ARItems;
import de.katzenpapst.amunra.network.packet.PacketSimpleAR;
import de.katzenpapst.amunra.network.packet.PacketSimpleAR.EnumSimplePacket;
import de.katzenpapst.amunra.tile.TileEntityShuttleDock;
import de.katzenpapst.amunra.vec.Vector3int;
import de.katzenpapst.amunra.world.ShuttleDockHandler;
import de.katzenpapst.amunra.world.WorldHelper;
import io.netty.buffer.ByteBuf;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntitySpaceshipBase;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityTieredRocket;
import micdoodle8.mods.galacticraft.api.tile.IFuelDock;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.api.world.IZeroGDimension;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import micdoodle8.mods.galacticraft.core.tile.TileEntityMulti;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;

public class EntityShuttle extends EntityTieredRocket {

    protected boolean doKnowOnWhatImStanding = false;
    protected boolean isOnBareGround = false;

    protected int numTanks = 0;

    // so, apparently, there is no real way to figure out when an entity has been dismounted
    protected Entity prevRiddenByEntity = null;

    protected Vector3int dockPosition = null;

    public EntityShuttle(final World par1World) {
        super(par1World);

        this.setSize(1.2F, 5.5F);
        this.yOffset = 1.5F;
    }

    public EntityShuttle(final World world, final double posX, final double posY, final double posZ, final int type) {
        super(world, posX, posY, posZ);
        this.setSize(1.2F, 3.5F);
        this.yOffset = 1.5F;
        this.decodeItemDamage(type);
        this.cargoItems = new ItemStack[this.getSizeInventory()];
        this.fuelTank = new FluidTank(getFuelCapacityFromDamage(type));
    }

    public void setTargetDock(final Vector3int dockPos) {
        this.targetVec = dockPos.toBlockVec3();
    }

    protected void decodeItemDamage(final int dmg) {

        this.rocketType = getRocketTypeFromDamage(dmg);
        this.numTanks = getNumTanksFromDamage(dmg);
    }

    protected int encodeItemDamage() {
        return encodeItemDamage(this.rocketType.ordinal(), this.numTanks);
    }

    public void setLanding() {
        this.landing = true;
        this.launchPhase = EnumLaunchPhase.LAUNCHED.ordinal();
    }

    public static int encodeItemDamage(final int numChests, final int numTanks) {
        return numChests | numTanks << 2;
    }

    public static int getFuelCapacityFromDamage(final int damage) {
        final int numTanks = getNumTanksFromDamage(damage);
        return (1000 + 500 * numTanks) * ConfigManagerCore.rocketFuelFactor;
    }

    public static EnumRocketType getRocketTypeFromDamage(final int damage) {
        return EnumRocketType.values()[getNumChestsFromDamage(damage)];
    }

    public static boolean isPreFueled(final int damage) {
        return damage == 15;
    }

    public static int getNumChestsFromDamage(final int damage) {
        return damage & 3;
    }

    public static int getNumTanksFromDamage(final int damage) {
        return damage >> 2 & 3;
    }

    @Override
    public void decodePacketdata(final ByteBuf buffer) {
        this.numTanks = buffer.readInt();
        super.decodePacketdata(buffer);
    }

    @Override
    public void getNetworkedData(final ArrayList<Object> list) {
        list.add(this.numTanks);
        super.getNetworkedData(list);
    }

    public EntityShuttle(final World par1World, final double par2, final double par4, final double par6,
            final boolean reversed, final int rocketType, final ItemStack[] inv) {
        this(par1World, par2, par4, par6, rocketType);
        this.cargoItems = inv;
    }

    @Override
    public int getSizeInventory() {
        if (this.rocketType == null) return 2;
        /*
         * if(this.rocketType == EnumRocketType.PREFUELED) { return 56; }
         */
        return this.rocketType.getInventorySpace();
    }

    public void setCargoContents(final ItemStack[] newCargo) {

        this.cargoItems = new ItemStack[this.getSizeInventory()];
        int curIndex = 0;

        for (ItemStack element : newCargo) {
            if (element == null) {
                continue;
            }
            this.cargoItems[curIndex] = element.copy();
            curIndex++;
        }
        this.markDirty();
    }

    /**
     * Return the full item representation of the entity, including type, fuel, and whatever else
     *
     * @return
     */
    public ItemStack getItemRepresentation() {
        final ItemStack rocket = new ItemStack(ARItems.shuttleItem, 1, this.encodeItemDamage());
        rocket.setTagCompound(new NBTTagCompound());
        rocket.getTagCompound().setInteger("RocketFuel", this.fuelTank.getFluidAmount());

        return rocket;
    }

    @Override
    public ItemStack getPickedResult(MovingObjectPosition target) {
        return new ItemStack(ARItems.shuttleItem, 1, this.encodeItemDamage());
    }

    @Override
    public int getRocketTier() {
        // Keep it at 0, the shuttle can't reach most stuff
        return 0;
    }

    @Override
    public float getCameraZoom() {
        return 15.0F;
    }

    @Override
    public boolean defaultThirdPerson() {
        return true;
    }

    @Override
    public int getFuelTankCapacity() {
        return 1000 + 500 * this.numTanks;
    }

    @Override
    public int getPreLaunchWait() {
        return 400;
    }

    @Override
    public double getOnPadYOffset() {
        return 1.6D;
    }

    /**
     * This gets added onto getOnPadYOffset
     */
    public double getOnGroundYOffset() {
        return 1.0D;
    }

    public double getDistanceFromGround() {
        return 2.8D;
    }

    @Override
    public double getMountedYOffset() {
        return 0.0D;
    }

    private void makeFlame(final double x2, final double y2, final double z2, final Vector3 motionVec,
            final boolean getLaunched) {
        if (getLaunched) {
            GalacticraftCore.proxy.spawnParticle(
                    "launchFlameLaunched",
                    new Vector3(x2 + 0.4 - this.rand.nextDouble() / 10, y2, z2 + 0.4 - this.rand.nextDouble() / 10),
                    motionVec,
                    new Object[] { this.riddenByEntity });
            GalacticraftCore.proxy.spawnParticle(
                    "launchFlameLaunched",
                    new Vector3(x2 - 0.4 + this.rand.nextDouble() / 10, y2, z2 + 0.4 - this.rand.nextDouble() / 10),
                    motionVec,
                    new Object[] { this.riddenByEntity });
            GalacticraftCore.proxy.spawnParticle(
                    "launchFlameLaunched",
                    new Vector3(x2 - 0.4 + this.rand.nextDouble() / 10, y2, z2 - 0.4 + this.rand.nextDouble() / 10),
                    motionVec,
                    new Object[] { this.riddenByEntity });
            GalacticraftCore.proxy.spawnParticle(
                    "launchFlameLaunched",
                    new Vector3(x2 + 0.4 - this.rand.nextDouble() / 10, y2, z2 - 0.4 + this.rand.nextDouble() / 10),
                    motionVec,
                    new Object[] { this.riddenByEntity });
            GalacticraftCore.proxy.spawnParticle(
                    "launchFlameLaunched",
                    new Vector3(x2, y2, z2),
                    motionVec,
                    new Object[] { this.riddenByEntity });
            GalacticraftCore.proxy.spawnParticle(
                    "launchFlameLaunched",
                    new Vector3(x2 + 0.4, y2, z2),
                    motionVec,
                    new Object[] { this.riddenByEntity });
            GalacticraftCore.proxy.spawnParticle(
                    "launchFlameLaunched",
                    new Vector3(x2 - 0.4, y2, z2),
                    motionVec,
                    new Object[] { this.riddenByEntity });
            GalacticraftCore.proxy.spawnParticle(
                    "launchFlameLaunched",
                    new Vector3(x2, y2, z2 + 0.4D),
                    motionVec,
                    new Object[] { this.riddenByEntity });
            GalacticraftCore.proxy.spawnParticle(
                    "launchFlameLaunched",
                    new Vector3(x2, y2, z2 - 0.4D),
                    motionVec,
                    new Object[] { this.riddenByEntity });
            return;
        }

        final double x1 = motionVec.x;
        final double y1 = motionVec.y;
        final double z1 = motionVec.z;
        GalacticraftCore.proxy.spawnParticle(
                "launchFlameIdle",
                new Vector3(x2 + 0.4 - this.rand.nextDouble() / 10, y2, z2 + 0.4 - this.rand.nextDouble() / 10),
                new Vector3(x1 + 0.5D, y1 - 0.3D, z1 + 0.5D),
                new Object[] { this.riddenByEntity });
        GalacticraftCore.proxy.spawnParticle(
                "launchFlameIdle",
                new Vector3(x2 - 0.4 + this.rand.nextDouble() / 10, y2, z2 + 0.4 - this.rand.nextDouble() / 10),
                new Vector3(x1 - 0.5D, y1 - 0.3D, z1 + 0.5D),
                new Object[] { this.riddenByEntity });
        GalacticraftCore.proxy.spawnParticle(
                "launchFlameIdle",
                new Vector3(x2 - 0.4 + this.rand.nextDouble() / 10, y2, z2 - 0.4 + this.rand.nextDouble() / 10),
                new Vector3(x1 - 0.5D, y1 - 0.3D, z1 - 0.5D),
                new Object[] { this.riddenByEntity });
        GalacticraftCore.proxy.spawnParticle(
                "launchFlameIdle",
                new Vector3(x2 + 0.4 - this.rand.nextDouble() / 10, y2, z2 - 0.4 + this.rand.nextDouble() / 10),
                new Vector3(x1 + 0.5D, y1 - 0.3D, z1 - 0.5D),
                new Object[] { this.riddenByEntity });
        GalacticraftCore.proxy.spawnParticle(
                "launchFlameIdle",
                new Vector3(x2 + 0.4, y2, z2),
                new Vector3(x1 + 0.8D, y1 - 0.3D, z1),
                new Object[] { this.riddenByEntity });
        GalacticraftCore.proxy.spawnParticle(
                "launchFlameIdle",
                new Vector3(x2 - 0.4, y2, z2),
                new Vector3(x1 - 0.8D, y1 - 0.3D, z1),
                new Object[] { this.riddenByEntity });
        GalacticraftCore.proxy.spawnParticle(
                "launchFlameIdle",
                new Vector3(x2, y2, z2 + 0.4D),
                new Vector3(x1, y1 - 0.3D, z1 + 0.8D),
                new Object[] { this.riddenByEntity });
        GalacticraftCore.proxy.spawnParticle(
                "launchFlameIdle",
                new Vector3(x2, y2, z2 - 0.4D),
                new Vector3(x1, y1 - 0.3D, z1 - 0.8D),
                new Object[] { this.riddenByEntity });
    }

    protected void spawnParticles(final boolean launched) {
        if (!this.isDead) {
            double x1 = 3.2 * Math.cos(this.rotationYaw / 57.2957795D) * Math.sin(this.rotationPitch / 57.2957795D);
            double z1 = 3.2 * Math.sin(this.rotationYaw / 57.2957795D) * Math.sin(this.rotationPitch / 57.2957795D);
            double y1 = 3.2 * Math.cos((this.rotationPitch - 180) / 57.2957795D);
            if (this.landing && this.targetVec != null) {
                double modifier = this.posY - this.targetVec.y;
                modifier = Math.max(modifier, 1.0);
                x1 *= modifier / 60.0D;
                y1 *= modifier / 60.0D;
                z1 *= modifier / 60.0D;
            }

            final double y2 = this.prevPosY + (this.posY - this.prevPosY) + y1;

            final double x2 = this.posX + x1;
            final double z2 = this.posZ + z1;
            final Vector3 motionVec = new Vector3(x1, y1, z1);
            final Vector3 d1 = new Vector3(y1 * 0.1D, -x1 * 0.1D, z1 * 0.1D).rotate(315 - this.rotationYaw, motionVec);
            final Vector3 d2 = new Vector3(x1 * 0.1D, -z1 * 0.1D, y1 * 0.1D).rotate(315 - this.rotationYaw, motionVec);
            final Vector3 d3 = new Vector3(-y1 * 0.1D, x1 * 0.1D, z1 * 0.1D).rotate(315 - this.rotationYaw, motionVec);
            final Vector3 d4 = new Vector3(x1 * 0.1D, z1 * 0.1D, -y1 * 0.1D).rotate(315 - this.rotationYaw, motionVec);
            final Vector3 mv1 = motionVec.clone().translate(d1);
            final Vector3 mv2 = motionVec.clone().translate(d2);
            final Vector3 mv3 = motionVec.clone().translate(d3);
            final Vector3 mv4 = motionVec.clone().translate(d4);
            // T3 - Four flameballs which spread
            this.makeFlame(x2 + d1.x, y2 + d1.y, z2 + d1.z, mv1, this.getLaunched());
            this.makeFlame(x2 + d2.x, y2 + d2.y, z2 + d2.z, mv2, this.getLaunched());
            this.makeFlame(x2 + d3.x, y2 + d3.y, z2 + d3.z, mv3, this.getLaunched());
            this.makeFlame(x2 + d4.x, y2 + d4.y, z2 + d4.z, mv4, this.getLaunched());
        }
    }

    @Override
    protected void failRocket() {
        if (this.shouldCancelExplosion() && this.landing && this.launchPhase == EnumLaunchPhase.LAUNCHED.ordinal()) {
            // seems like I just landed
            this.launchPhase = EnumLaunchPhase.UNIGNITED.ordinal();
            this.landing = false;

            return;
        }

        super.failRocket();
    }

    protected void repositionMountedPlayer(final Entity entity) {
        if (!(entity instanceof EntityPlayer player)) {
            return;
        }
        if (this.getLandingPad() != null && this.getLandingPad() instanceof TileEntityShuttleDock tileDock) {
            // just rotate the player away from the dock
            player.rotationYaw = tileDock.getExitRotation();
            player.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
        }
    }

    protected void repositionDismountedPlayer(final Entity entity) {
        if (!(entity instanceof EntityPlayer player)) {
            return;
        }
        if (this.getLandingPad() != null && this.getLandingPad() instanceof TileEntityShuttleDock tileDock) {
            final Vector3 pos = tileDock.getExitPosition();
            player.rotationYaw = tileDock.getExitRotation();
            player.setPositionAndUpdate(pos.x, pos.y, pos.z);
        } else {
            // try not doing this?
            // check for safe positions
            final int xPos = (int) (this.posX - 0.5D);
            final int yPos = (int) (this.posY - this.getYOffset());
            final int zPos = (int) (this.posZ - 0.5D);

            if (this.isSafeForPlayer(xPos, yPos, zPos - 2)) {
                player.setPositionAndUpdate(this.posX, yPos, this.posZ - 2);
            } else if (this.isSafeForPlayer(xPos, yPos, zPos + 2)) {
                player.setPositionAndUpdate(this.posX, yPos, this.posZ + 2);
            } else if (this.isSafeForPlayer(xPos - 2, yPos, zPos)) {
                player.setPositionAndUpdate(this.posX - 2, yPos, this.posZ);
            } else if (this.isSafeForPlayer(xPos + 2, yPos, zPos)) {
                player.setPositionAndUpdate(this.posX + 2, yPos, this.posZ);
            }
        }
    }

    protected boolean isSafeForPlayer(final double x, final double y, final double z) {
        final int y1 = (int) y;

        return WorldHelper.isNonSolid(this.worldObj, (int) x, y1, (int) z)
                && WorldHelper.isNonSolid(this.worldObj, (int) x, y1 + 1, (int) z)
                && WorldHelper.isSolid(this.worldObj, (int) x, y1 - 1, (int) z, true);
    }

    protected void tryFindAnotherDock() {
        final Vector3int dock = ShuttleDockHandler.findAvailableDock(this.worldObj.provider.dimensionId);
        if (dock != null) {

            // reposition myself a little to be above it
            final double yBak = this.posY;
            this.setPosition(dock.x, yBak, dock.z);
            this.targetVec = dock.toBlockVec3();
        } else {
            this.targetVec = null;
        }
    }

    protected void tryToDock() {
        final int chunkx = CoordHelper.blockToChunk(this.targetVec.x);
        final int chunkz = CoordHelper.blockToChunk(this.targetVec.z);
        if (this.worldObj.getChunkProvider().chunkExists(chunkx, chunkz)) {

            final TileEntity te = this.targetVec.getTileEntity(this.worldObj);
            if (te instanceof IFuelDock) {

                if (!(te instanceof TileEntityShuttleDock tileDock)) {
                    // just a regular dock. oh well
                    return;
                }
                if (tileDock.isAvailable()) {
                    // finally
                    tileDock.dockEntity(this);
                } else {
                    this.tryFindAnotherDock();
                }

            } else {
                // attempt to find another one?
                this.tryFindAnotherDock();
            }
        } // otherwise wait
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        // handle player dismounting
        if (!this.worldObj.isRemote) {

            if (this.prevRiddenByEntity != this.riddenByEntity) {
                if (this.riddenByEntity == null && this.prevRiddenByEntity != null) {
                    // seems like someone just dismounted
                    // playerRepositionTicks = 20;
                    this.repositionDismountedPlayer(this.prevRiddenByEntity);
                    // repositioningEntity = prevRiddenByEntity;
                } else if (this.prevRiddenByEntity == null && this.riddenByEntity != null) {
                    this.repositionMountedPlayer(this.riddenByEntity);
                }
                this.prevRiddenByEntity = this.riddenByEntity;
            }

            // try this
            if (this.landing && this.targetVec != null) {
                this.tryToDock();
            }
        }

        int i;

        if (this.timeUntilLaunch >= 100) {
            i = Math.abs(this.timeUntilLaunch / 100);
        } else {
            i = 1;
        }

        if ((this.getLaunched() || this.launchPhase == EnumLaunchPhase.IGNITED.ordinal() && this.rand.nextInt(i) == 0)
                && !ConfigManagerCore.disableSpaceshipParticles
                && this.hasValidFuel()) {
            if (this.worldObj.isRemote) {
                this.spawnParticles(this.getLaunched());
            }
        }

        if (this.getLaunched()) {
            // failsafe
            if (this.riddenByEntity == null) {
                this.landing = true; // go back
            }

            if (this.hasValidFuel()) {
                if (!this.landing) {
                    double d = this.timeSinceLaunch / 150;

                    d = Math.min(d, 1);

                    if (d != 0.0) {
                        this.motionY = -d * 2.0D * Math.cos((this.rotationPitch - 180) * Math.PI / 180.0D);
                    }
                } else {
                    this.motionY -= 0.008D;
                }

                double multiplier = 1.0D;

                if (this.worldObj.provider instanceof IGalacticraftWorldProvider gcProvider) {
                    multiplier = gcProvider.getFuelUsageMultiplier();

                    if (multiplier <= 0) {
                        multiplier = 1;
                    }
                }

                if (this.timeSinceLaunch % MathHelper.floor_double(2 * (1 / multiplier)) == 0) {
                    this.removeFuel(1);
                    if (!this.hasValidFuel()) this.stopRocketSound();
                }
            } else {
                // no valid fuel
                // enter landing mode
                this.landing = true;

                if (!this.worldObj.isRemote && Math.abs(Math.sin(this.timeSinceLaunch / 1000)) / 10 != 0.0) {
                    this.motionY -= Math.abs(Math.sin(this.timeSinceLaunch / 1000)) / 20;
                }
            }
        }

        if (this.launchPhase == EnumLaunchPhase.UNIGNITED.ordinal()) {

            this.checkStandingPosition();

        }
    }

    @Override
    public void stopRocketSound() {
        super.stopRocketSound();
        this.rocketSoundUpdater = null; // I hope this works
    }

    protected void checkStandingPosition() {
        // recheck this from time to time anyway
        if (this.worldObj.isRemote || this.doKnowOnWhatImStanding && this.ticksExisted % 40 != 0) {
            return;
        }

        if (this.getLandingPad() != null) {
            this.doKnowOnWhatImStanding = true;
        } else {
            if (this.dockPosition != null) {
                final TileEntity tile = this.worldObj
                        .getTileEntity(this.dockPosition.x, this.dockPosition.y, this.dockPosition.z);
                if (tile != null) {
                    if (tile instanceof IFuelDock) {
                        this.setPad((IFuelDock) tile);
                        this.landEntity(tile);
                        this.dockPosition = null;
                        return;
                    }
                    // something went wrong
                    this.dockPosition = null;
                }
            }

            boolean isInZeroG = this.worldObj.provider instanceof IZeroGDimension;

            // let's look downward
            final int bX = (int) (this.posX - 0.5D);
            final int bY = (int) (this.posY - 0.5D - 1);
            final int bZ = (int) (this.posZ - 0.5D);

            final Vector3int highest = WorldHelper
                    .getHighestNonEmptyBlock(this.worldObj, bX - 1, bX + 1, bY - 5, bY, bZ - 1, bZ + 1);

            if (highest != null) {
                TileEntity tileBelow = this.worldObj.getTileEntity(highest.x, highest.y, highest.z);
                IFuelDock dockTile = null;
                if (tileBelow != null) {
                    if (tileBelow instanceof TileEntityMulti) {
                        tileBelow = ((TileEntityMulti) tileBelow).getMainBlockTile();
                    }
                    if (tileBelow instanceof IFuelDock) {
                        dockTile = (IFuelDock) tileBelow;
                    }
                }
                if (dockTile != null) {
                    this.isOnBareGround = false;
                    this.doKnowOnWhatImStanding = true;
                    if (this.getLandingPad() != dockTile) {
                        this.landEntity((TileEntity) dockTile);
                    }
                } else {
                    this.isOnBareGround = true;
                    this.doKnowOnWhatImStanding = true;
                    if (!isInZeroG) {
                        this.adjustGroundPosition(highest.y);
                    }
                }
            } else if (!isInZeroG) {
                // make the rocket land
                this.setLanding();
            }
        }
    }

    @Override
    public void landEntity(final int x, final int y, final int z) {
        final TileEntity tile = this.worldObj.getTileEntity(x, y, z);

        this.landEntity(tile);
    }

    public void landEntity(final TileEntity tile) {
        if (tile instanceof IFuelDock dock && this.isDockValid(dock)) {
            if (!this.worldObj.isRemote) {
                // Drop any existing rocket on the landing pad
                if (dock.getDockedEntity() instanceof EntitySpaceshipBase entitySpaceship
                        && dock.getDockedEntity() != this) {
                    entitySpaceship.dropShipAsItem();
                    entitySpaceship.setDead();
                }
                this.setPad(dock);
            }
            this.onRocketLand(tile.xCoord, tile.yCoord, tile.zCoord);
        }
    }

    @Override
    public void setPad(final IFuelDock pad) {
        this.isOnBareGround = false;
        this.doKnowOnWhatImStanding = true;
        super.setPad(pad);
    }

    protected void adjustGroundPosition(final int blockYPos) {
        this.setPosition(this.posX, this.getDistanceFromGround() + blockYPos, this.posZ);
    }

    @Override
    public void onReachAtmosphere() {
        // Not launch controlled
        if (this.riddenByEntity != null && !this.worldObj.isRemote
                && this.riddenByEntity instanceof EntityPlayerMP player) {
            this.onTeleport(player);
            final GCPlayerStats stats = this.setGCPlayerStats(player);

            // this is the part which activates the celestial gui
            toCelestialSelection(player, stats, this.getRocketTier());

        }

        // Destroy any rocket which reached the top of the atmosphere and is not controlled by a Launch Controller
        this.setDead();
    }

    public GCPlayerStats setGCPlayerStats(final EntityPlayerMP player) {
        final GCPlayerStats stats = GCPlayerStats.get(player);

        if (this.cargoItems == null || this.cargoItems.length == 0) {
            stats.rocketStacks = new ItemStack[2];
        } else {
            stats.rocketStacks = this.cargoItems;
        }

        stats.rocketType = this.encodeItemDamage();
        stats.rocketItem = ARItems.shuttleItem;
        stats.fuelLevel = this.fuelTank.getFluidAmount();
        return stats;
    }

    public static void toCelestialSelection(final EntityPlayerMP player, final GCPlayerStats stats, final int tier) {
        toCelestialSelection(player, stats, tier, true);
    }

    public static void toCelestialSelection(final EntityPlayerMP player, final GCPlayerStats stats, final int tier,
            final boolean useFakeEntity) {
        player.mountEntity(null);
        stats.spaceshipTier = tier;
        // replace this with my own stuff. this must only contain the nearby stuff
        final Map<String, Integer> map = ShuttleTeleportHelper.getArrayOfPossibleDimensions(player);
        String dimensionList = "";
        int count = 0;
        for (final Entry<String, Integer> entry : map.entrySet()) {
            dimensionList = dimensionList.concat(entry.getKey() + (count < map.entrySet().size() - 1 ? "?" : ""));
            count++;
        }

        AmunRa.packetPipeline.sendTo(
                new PacketSimpleAR(
                        EnumSimplePacket.C_OPEN_SHUTTLE_GUI,
                        player.getGameProfile().getName(),
                        dimensionList),
                player);
        // do not use this for the shuttle
        stats.usingPlanetSelectionGui = false;
        stats.savedPlanetList = dimensionList;

        if (useFakeEntity) {
            final Entity fakeEntity = new EntityShuttleFake(
                    player.worldObj,
                    player.posX,
                    player.posY,
                    player.posZ,
                    0.0F);
            player.worldObj.spawnEntityInWorld(fakeEntity);
            player.mountEntity(fakeEntity);
        }
    }

    @Override
    public List<ItemStack> getItemsDropped(final List<ItemStack> droppedItems) {
        super.getItemsDropped(droppedItems);
        final ItemStack rocket = this.getItemRepresentation();
        droppedItems.add(rocket);
        return droppedItems;
    }

    public List<ItemStack> getCargoContents() {
        final List<ItemStack> droppedItemList = new ArrayList<>();
        if (this.cargoItems != null) {
            for (final ItemStack item : this.cargoItems) {
                if (item != null) {
                    droppedItemList.add(item);
                }
            }
        }

        return droppedItemList;
    }

    @Override
    protected void writeEntityToNBT(final NBTTagCompound nbt) {
        nbt.setInteger("NumTanks", this.numTanks);

        if (this.getLandingPad() != null) {
            final Vector3int pos = new Vector3int((TileEntity) this.getLandingPad());
            nbt.setTag("dockPosition", pos.toNBT());
        }

        super.writeEntityToNBT(nbt);
    }

    @Override
    protected void readEntityFromNBT(final NBTTagCompound nbt) {
        this.numTanks = nbt.getInteger("NumTanks");
        if (nbt.hasKey("dockPosition")) {
            final NBTTagCompound dockPosNbt = nbt.getCompoundTag("dockPosition");
            if (dockPosNbt != null) {
                this.dockPosition = new Vector3int(dockPosNbt);
            }
        }

        super.readEntityFromNBT(nbt);
    }

}
