package de.katzenpapst.amunra.tile;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;

import cpw.mods.fml.relauncher.Side;
import de.katzenpapst.amunra.client.sound.ISoundableTile;
import de.katzenpapst.amunra.helper.CoordHelper;
import de.katzenpapst.amunra.mob.DamageSourceAR;
import de.katzenpapst.amunra.mothership.fueldisplay.MothershipFuelRequirements;
import de.katzenpapst.amunra.vec.Vector3int;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseElectricBlockWithInventory;
import micdoodle8.mods.galacticraft.core.util.Annotations.NetworkedField;

public abstract class TileEntityMothershipEngineAbstract extends TileBaseElectricBlockWithInventory
        implements ITileMothershipEngine, IFluidHandler, ISidedInventory, IInventory, ISoundableTile {

    protected Fluid fuel;

    protected int numBoosters = 0;
    protected final int tankCapacity = 12000;

    protected final int exhaustCheckLength = 5;
    // whenever this one needs to update itself
    protected boolean needsUpdate = true;

    protected boolean loadedFuelLastTick = false;

    protected boolean isInUseForTransit = false;

    protected boolean shouldPlaySound = false;

    protected boolean soundStarted = false;

    protected boolean isObstructed = false;

    protected AxisAlignedBB exhaustBB = null;

    @NetworkedField(targetSide = Side.CLIENT)
    public FluidTank fuelTank = new FluidTank(this.tankCapacity);
    protected ItemStack[] containingItems;
    public static final int MAX_LENGTH = 10;
    protected BlockMetaPair boosterBlock;
    protected PositionedSoundRecord leSound;

    public int getScaledFuelLevel(final int i) {
        final double fuelLevel = this.fuelTank.getFluid() == null ? 0 : this.fuelTank.getFluid().amount;

        return (int) (fuelLevel * i / this.fuelTank.getCapacity());
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.containingItems = this.readStandardItemsFromNBT(compound);
        if (compound.hasKey("numBoosters")) {
            this.numBoosters = compound.getInteger("numBoosters");
        }

        this.fuelTank.setCapacity(this.getTankCapacity());

        if (compound.hasKey("fuelTank")) {
            this.fuelTank.readFromNBT(compound.getCompoundTag("fuelTank"));
        }

        if (compound.hasKey("needsUpdate")) {
            this.needsUpdate = compound.getBoolean("needsUpdate");
        }
        if (compound.hasKey("usedForTransit")) {
            this.isInUseForTransit = compound.getBoolean("usedForTransit");
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        this.writeStandardItemsToNBT(compound);
        if (this.fuelTank.getFluid() != null) {
            compound.setTag("fuelTank", this.fuelTank.writeToNBT(new NBTTagCompound()));
        }
        compound.setInteger("numBoosters", this.numBoosters);
        compound.setBoolean("needsUpdate", this.needsUpdate);
        compound.setBoolean("usedForTransit", this.isInUseForTransit);
    }

    @Override
    public Packet getDescriptionPacket() {
        final NBTTagCompound var1 = new NBTTagCompound();
        this.writeToNBT(var1);

        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, var1);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
    }

    /**
     * Calculates tank capacity based on the boosters
     *
     * @return
     */
    abstract protected int getTankCapacity();

    public Vector3 getCenterPosition() {
        return new Vector3(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D);
    }

    public boolean isObstructed() {
        this.checkBlocksInWay();
        return this.isObstructed;
    }

    public Vector3 getExhaustDirection() {
        /*
         * -Z => 0 +Z => 2 -X => 3 +X => 1
         */
        return switch (this.getRotationMeta()) {
            case 0 -> new Vector3(0, 0, -1);
            case 1 -> new Vector3(1, 0, 0);
            case 2 -> new Vector3(0, 0, 1);
            case 3 -> new Vector3(-1, 0, 0);
            default -> new Vector3(0, 0, 0);
        };
    }

    public Vector3 getExhaustPosition(final double scale) {
        final double random1 = this.worldObj.rand.nextGaussian() * 0.10F * scale;
        final double random2 = this.worldObj.rand.nextGaussian() * 0.10F * scale;
        final double offset = 0.40D;
        final Vector3 result = new Vector3(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D);

        switch (this.getRotationMeta()) {
            case 0 -> {
                result.x += random1;
                result.y += random2;
                result.z -= offset;
            }
            case 1 -> {
                result.x += offset;
                result.y += random1;
                result.z += random2;
            }
            case 2 -> {
                result.x += random1;
                result.y += random2;
                result.z += offset;
            }
            case 3 -> {
                result.x -= offset;
                result.y += random1;
                result.z += random2;
            }
        }

        return result;
    }

    protected void startSound() {
        this.shouldPlaySound = true;
        this.soundStarted = true;
    }

    protected void stopSound() {
        this.shouldPlaySound = false;
        this.soundStarted = false;
    }

    protected AxisAlignedBB getExhaustAABB() {
        final Vector3 exDir = this.getExhaustDirection();

        final Vector3 startPos = this.getCenterPosition();
        final Vector3 minVec = new Vector3(0, 0, 0);
        final Vector3 maxVec = new Vector3(0, 0, 0);

        minVec.y = startPos.y - 0.5;
        maxVec.y = startPos.y + 0.5;

        // figure out the aabb
        if (exDir.x != 0) {
            // pointing towards +x or -x
            minVec.z = startPos.z - 0.5;
            maxVec.z = startPos.z + 0.5;

            if (exDir.x < 0) {
                minVec.x = startPos.x - this.exhaustCheckLength - 0.5;
                maxVec.x = startPos.x - 0.5;
            } else {
                minVec.x = startPos.x + 0.5;
                maxVec.x = startPos.x + 0.5 + this.exhaustCheckLength;
            }
        } else if (exDir.z != 0) {
            // pointing towards +z or -z
            minVec.x = startPos.x - 0.5;
            maxVec.x = startPos.x + 0.5;

            if (exDir.z < 0) {
                minVec.z = startPos.z - this.exhaustCheckLength - 0.5;
                maxVec.z = startPos.z - 0.5;
            } else {
                minVec.z = startPos.z + 0.5;
                maxVec.z = startPos.z + 0.5 + this.exhaustCheckLength;
            }
        } else {
            return null;
        }

        // Returns a bounding box with the specified bounds. Args: minX, minY, minZ, maxX, maxY, maxZ
        return AxisAlignedBB.getBoundingBox(minVec.x, minVec.y, minVec.z, maxVec.x, maxVec.y, maxVec.z);

    }

    protected void checkBlocksInWay() {

        final Vector3 exDir = this.getExhaustDirection();
        final Vector3 blockPos = new Vector3(this);

        this.isObstructed = false;

        for (int i = 0; i < this.exhaustCheckLength; i++) {
            blockPos.translate(exDir);

            final Block b = blockPos.getBlock(this.worldObj);
            if (!b.isAir(this.worldObj, blockPos.intX(), blockPos.intY(), blockPos.intZ())) {
                this.isObstructed = true;
                return;
            }
        }

    }

    protected void checkEntitiesInWay() {

        if (this.exhaustBB == null) {
            this.exhaustBB = this.getExhaustAABB();
            // if it's still null, it's very bad
            if (this.exhaustBB == null) {
                return;
            }
        }
        // minX, minY, minZ, maxX, maxY, maxZ

        final Vector3 myPos = this.getCenterPosition();
        final Vector3 exhaustDir = this.getExhaustDirection();

        final List<EntityLivingBase> list = this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, this.exhaustBB);

        if (list != null) {
            for (EntityLivingBase entity : list) {
                entity.setFire(5);

                Vector3 entityPos = new Vector3(entity);

                final double factor = entityPos.distance(myPos);

                entityPos = exhaustDir.clone().scale(0.2 / factor);

                final float damage = (float) (10.0F / factor);

                entity.attackEntityFrom(DamageSourceAR.dsEngine, damage);
                entity.addVelocity(entityPos.x, entityPos.y, entityPos.z);
            }
        }
    }

    protected void spawnParticles() {
        /*
         * Vector3 particleStart = getExhaustPosition(); Vector3 particleDirection = getExhaustDirection().scale(5);
         * AmunRa.proxy.spawnParticles(ParticleType.PT_MOTHERSHIP_JET_FLAME, this.worldObj, particleStart,
         * particleDirection); AmunRa.proxy.spawnParticles(ParticleType.PT_MOTHERSHIP_JET_FLAME, this.worldObj,
         * particleStart, particleDirection); AmunRa.proxy.spawnParticles(ParticleType.PT_MOTHERSHIP_JET_FLAME,
         * this.worldObj, particleStart, particleDirection);
         * AmunRa.proxy.spawnParticles(ParticleType.PT_MOTHERSHIP_JET_FLAME, this.worldObj, particleStart,
         * particleDirection);
         */
    }

    abstract protected boolean isItemFuel(ItemStack fuel);

    protected void processFluids() {
        // more stuff
        this.loadedFuelLastTick = false;

        final ItemStack canister = this.containingItems[0];

        if (canister != null && this.isItemFuel(canister)) {
            // attempt to drain it into the tank
            FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(canister);
            // int spaceForFluid = this.fuelTank.getCapacity() -
            /*
             * if (this.fuelTank.getFluid() == null || this.fuelTank.getFluid().amount + liquid.amount <=
             * this.fuelTank.getCapacity()) {
             */
            final int fluidAmount = this.fuelTank.getFluid() == null ? 0 : this.fuelTank.getFluid().amount;
            final int spaceForFluid = this.fuelTank.getCapacity() - fluidAmount;

            // attempt to drain as much as we have space
            if (canister.getItem() instanceof IFluidContainerItem) {
                // try to do
                final FluidStack drained = ((IFluidContainerItem) canister.getItem())
                        .drain(canister, spaceForFluid, true);
                if (drained != null && drained.amount > 0) {
                    this.fuelTank.fill(new FluidStack(this.fuel, drained.amount), true);
                    liquid = ((IFluidContainerItem) canister.getItem()).getFluid(canister);
                    if (liquid == null || liquid.amount == 0) {
                        // this should replace the container with it's empty version
                        final ItemStack canisterNew = FluidContainerRegistry.drainFluidContainer(canister);
                        if (canisterNew != null) {
                            this.containingItems[0] = canisterNew;
                        }
                    }
                }
            } else {
                // attempt to drain it all at once
                final int capacity = FluidContainerRegistry.getContainerCapacity(canister);

                if (spaceForFluid >= capacity) {
                    // now drain it
                    this.fuelTank.fill(new FluidStack(this.fuel, capacity), true);
                    final ItemStack canisterNew = FluidContainerRegistry.drainFluidContainer(canister);
                    if (canisterNew != null) {
                        this.containingItems[0] = canisterNew;
                    }
                }
            }
        }
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (this.isInUseForTransit) {
            if (!this.soundStarted) {
                this.startSound();
            }
            this.spawnParticles();

            // check for entities behind me
            this.checkEntitiesInWay();
        } else {
            if (this.soundStarted) {
                this.stopSound();
            }
            this.exhaustBB = null;
        }

        if (!this.worldObj.isRemote) {

            // so, on an actual server-client setup, this actually happens on the server side
            if (this.needsUpdate) {
                this.updateMultiblock();
                this.needsUpdate = false;
            }

            this.processFluids();
        }

    }

    @Override
    public boolean canUpdate() {
        // maybe return this.needsUpdate?
        return true;
    }

    @Override
    public FluidStack drain(final ForgeDirection from, final FluidStack resource, final boolean doDrain) {
        // can't drain
        return null;
    }

    @Override
    public FluidStack drain(final ForgeDirection from, final int maxDrain, final boolean doDrain) {
        // can't drain
        return null;
    }

    public int getRotationMeta(final int meta) {
        return (meta & 12) >> 2;
    }

    public int getRotationMeta() {
        return (this.getBlockMetadata() & 12) >> 2;
    }

    @Override
    public boolean canFill(final ForgeDirection from, final Fluid fluid) {
        // can fill from everywhere except back
        final int metadata = this.getRotationMeta();

        return !CoordHelper.rotateForgeDirection(ForgeDirection.NORTH, metadata).equals(from);
    }

    @Override
    public int fill(final ForgeDirection from, final FluidStack resource, final boolean doFill) {
        int used = 0;

        if (this.canFill(from, resource.getFluid())) {
            used = this.fuelTank.fill(resource, doFill);
        }

        return used;
    }

    @Override
    public boolean canDrain(final ForgeDirection from, final Fluid fluid) {
        // can't drain
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(final ForgeDirection from) {
        final int metadata = this.getRotationMeta();
        if (CoordHelper.rotateForgeDirection(ForgeDirection.NORTH, metadata).equals(from)) {
            return null;
        }
        return new FluidTankInfo[] { new FluidTankInfo(this.fuelTank) };
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    protected ItemStack[] getContainingItems() {
        return this.containingItems;
    }

    @Override
    public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {
        return this.isItemValidForSlot(p_102007_1_, p_102007_2_);
    }

    @Override
    public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
        return p_102008_1_ == 0;
    }

    public Vector3int getLastBoosterPosition() {

        return switch (this.getRotationMeta()) {
            case 0 ->
                // rotation = 180.0F;// -> Z
                new Vector3int(this.xCoord, this.yCoord, this.zCoord + this.numBoosters);
            case 1 ->
                // rotation = 90.0F;// -> -X
                new Vector3int(this.xCoord - this.numBoosters, this.yCoord, this.zCoord);
            case 2 ->
                // rotation = 0;// -> -Z
                new Vector3int(this.xCoord, this.yCoord, this.zCoord - this.numBoosters);
            case 3 ->
                // rotation = 270.0F;// -> X
                new Vector3int(this.xCoord + this.numBoosters, this.yCoord, this.zCoord);
            default -> new Vector3int(this.xCoord, this.yCoord, this.zCoord);
        };
    }

    @Override
    public int getSizeInventory() {
        return this.containingItems.length;
    }

    @Override
    public ItemStack getStackInSlot(int slotIn) {
        return this.containingItems[slotIn];
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (this.containingItems[index] == null) {
            return null;
        }
        ItemStack var3;

        if (this.containingItems[index].stackSize <= count) {
            var3 = this.containingItems[index];
            this.containingItems[index] = null;
        } else {
            var3 = this.containingItems[index].splitStack(count);

            if (this.containingItems[index].stackSize == 0) {
                this.containingItems[index] = null;
            }
        }
        return var3;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        if (this.containingItems[index] != null) {
            final ItemStack var2 = this.containingItems[index];
            this.containingItems[index] = null;
            return var2;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.containingItems[index] = stack;

        if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
            stack.stackSize = this.getInventoryStackLimit();
        }
    }

    @Override
    public boolean isUseableByPlayer(final EntityPlayer player) {

        // this check has to be more complex
        if (player.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D) {
            return true;
        }
        // now stuff
        final Vector3int lastBooster = this.getLastBoosterPosition();
        if (lastBooster.x == this.xCoord) {
            final float minZ = Math.min(lastBooster.z, this.zCoord);
            final float maxZ = Math.max(lastBooster.z, this.zCoord);
            // double distSq = 0;
            if (player.posZ < minZ) {
                return this.xCoord * this.xCoord + Math.pow(minZ - player.posZ, 2) <= 64.0D;
            }
            if (player.posZ > maxZ) {
                return this.xCoord * this.xCoord + Math.pow(player.posZ - maxZ, 2) <= 64.0D;
            } else {
                // we are between the jet and the last booster on the z axis,
                // just look if we are not too far away from the x axis
                return Math.abs(player.posX - this.xCoord) <= 8;
            }
        }
        final float minX = Math.min(lastBooster.x, this.xCoord);
        final float maxX = Math.max(lastBooster.x, this.xCoord);
        // double distSq = 0;
        if (player.posX < minX) {
            return this.zCoord * this.zCoord + Math.pow(minX - player.posX, 2) <= 64.0D;
        }
        if (player.posX > maxX) {
            return this.zCoord * this.zCoord + Math.pow(player.posX - maxX, 2) <= 64.0D;
        } else {
            // we are between the jet and the last booster on the z axis,
            // just look if we are not too far away from the x axis
            return Math.abs(player.posZ - this.zCoord) <= 8;
        }
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    public int getNumBoosters() {
        return this.numBoosters;
    }

    public BlockMetaPair getBoosterBlock() {
        return this.boosterBlock;
    }

    protected boolean attachBooster(final int x, final int y, final int z, final boolean notifyClient) {
        final BlockMetaPair booster = this.getBoosterBlock();
        final Block worldBlock = this.worldObj.getBlock(x, y, z);
        final int worldMeta = this.worldObj.getBlockMetadata(x, y, z);
        final TileEntity worldTile = this.worldObj.getTileEntity(x, y, z);

        if (!booster.getBlock().equals(worldBlock) || booster.getMetadata() != worldMeta
                || worldTile == null
                || !(worldTile instanceof TileEntityMothershipEngineBooster)
                || ((TileEntityMothershipEngineBooster) worldTile).hasMaster()) {
            return false;
        }

        // actually attach
        ((TileEntityMothershipEngineBooster) worldTile).setMaster(this.xCoord, this.yCoord, this.zCoord);
        this.numBoosters++;

        if (notifyClient) {
            this.updateBooster(x, y, z);
        }

        return true;
    }

    protected void updateBooster(final int x, final int y, final int z) {
        final TileEntity worldTile = this.worldObj.getTileEntity(x, y, z);
        if (worldTile != null) {
            worldTile.markDirty();
            this.worldObj.markBlockForUpdate(x, y, z);
        }
    }

    protected boolean detachBooster(final int x, final int y, final int z, final boolean notifyClient) {
        final BlockMetaPair booster = this.getBoosterBlock();
        final Block worldBlock = this.worldObj.getBlock(x, y, z);
        final int worldMeta = this.worldObj.getBlockMetadata(x, y, z);
        final TileEntity worldTile = this.worldObj.getTileEntity(x, y, z);

        if (!booster.getBlock().equals(worldBlock) || booster.getMetadata() != worldMeta
                || worldTile == null
                || !(worldTile instanceof TileEntityMothershipEngineBooster)
                || !((TileEntityMothershipEngineBooster) worldTile).isMaster(this.xCoord, this.yCoord, this.zCoord)) {
            return false;
        }

        ((TileEntityMothershipEngineBooster) worldTile).clearMaster();
        if (notifyClient) {
            this.updateBooster(x, y, z);
        }

        return true;
    }

    /**
     * Check if the block at the given position is (or should be) within the current multiblock
     */
    public boolean isPartOfMultiBlock(final int x, final int y, final int z) {
        // for each axis, the other two coordinates should be the same
        // and the relevant one should be within numBoosters of my coordinate, in the right direction
        return switch (this.getRotationMeta()) {
            case 0 ->
                // rotation = 180.0F;// -> Z
                this.xCoord == x && this.yCoord == y && this.zCoord + this.numBoosters >= z && this.zCoord + 1 <= z;
            case 1 ->
                // rotation = 90.0F;// -> -X
                this.zCoord == z && this.yCoord == y && this.xCoord - this.numBoosters <= x && this.xCoord - 1 >= x;
            case 2 ->
                // rotation = 0;// -> -Z
                this.xCoord == x && this.yCoord == y && this.zCoord - this.numBoosters <= z && this.zCoord - 1 >= z;
            case 3 ->
                // rotation = 270.0F;// -> X
                this.zCoord == z && this.yCoord == y && this.xCoord + this.numBoosters >= x && this.xCoord + 1 <= x;
            default -> false;
        };
    }

    /**
     * Resets any boosters in the current direction
     */
    public void resetMultiblock() {
        this.resetMultiblockInternal(true);
    }

    protected void resetMultiblockInternal(final boolean notifyClient) {

        if (this.numBoosters == 0) {
            this.numBoosters = MAX_LENGTH;
        }

        switch (this.getRotationMeta()) {
            case 0 -> {
                // rotation = 180.0F;// -> Z
                for (int i = 0; i < this.numBoosters; i++) {
                    this.detachBooster(this.xCoord, this.yCoord, this.zCoord + i + 1, notifyClient);
                }
            }
            case 1 -> {
                // rotation = 90.0F;// -> -X
                for (int i = 0; i < this.numBoosters; i++) {
                    this.detachBooster(this.xCoord - i - 1, this.yCoord, this.zCoord, notifyClient);
                }
            }
            case 2 -> {
                // rotation = 0;// -> -Z
                for (int i = 0; i < this.numBoosters; i++) {
                    this.detachBooster(this.xCoord, this.yCoord, this.zCoord - i - 1, notifyClient);
                }
            }
            case 3 -> {
                // rotation = 270.0F;// -> X
                for (int i = 0; i < this.numBoosters; i++) {
                    this.detachBooster(this.xCoord + i + 1, this.yCoord, this.zCoord, notifyClient);
                }
            }
        }
        this.numBoosters = 0;
        this.fuelTank.setCapacity(0);
    }

    /**
     * Tell the tile that it should update the multiblock structure
     */
    public void scheduleUpdate() {
        this.needsUpdate = true;
    }

    /**
     * Reset and create in one
     */
    public void updateMultiblock() {
        // fuelTank.getCapacity()
        final int prevNumBlocks = this.numBoosters;
        this.resetMultiblockInternal(false);
        this.createMultiblockInternal(false);

        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);

        // also do it for any blocks we potentially touched in the process
        this.notifyClientAboutBoosters(Math.max(prevNumBlocks, this.numBoosters));
    }

    protected void notifyClientAboutBoosters(final int prevNumBoosters) {
        switch (this.getRotationMeta()) {
            case 0 -> {
                // rotation = 180.0F;// -> Z
                for (int i = 0; i < prevNumBoosters; i++) {
                    this.updateBooster(this.xCoord, this.yCoord, this.zCoord + i + 1);
                }
            }
            case 1 -> {
                // rotation = 90.0F;// -> -X
                for (int i = 0; i < prevNumBoosters; i++) {
                    this.updateBooster(this.xCoord - i - 1, this.yCoord, this.zCoord);
                }
            }
            case 2 -> {
                // rotation = 0;// -> -Z
                for (int i = 0; i < prevNumBoosters; i++) {
                    this.updateBooster(this.xCoord, this.yCoord, this.zCoord - i - 1);
                }
            }
            case 3 -> {
                // rotation = 270.0F;// -> X
                for (int i = 0; i < prevNumBoosters; i++) {
                    this.updateBooster(this.xCoord + i + 1, this.yCoord, this.zCoord);
                }
            }
        }
    }

    /**
     * Checks for boosters in the current direction, if they don't have masters yet, add them to myself
     */
    public void createMultiblock() {
        this.createMultiblockInternal(true);
    }

    protected void createMultiblockInternal(final boolean notifyClient) {
        // this should check all the stuff
        this.numBoosters = 0;
        // happens on server only, I think
        switch (this.getRotationMeta()) {
            case 0 -> {
                // rotation = 180.0F;// -> Z
                for (int i = 0; i < MAX_LENGTH; i++) {
                    if (!this.attachBooster(this.xCoord, this.yCoord, this.zCoord + i + 1, notifyClient)) {
                        break;
                    }
                }
            }
            case 1 -> {
                // rotation = 90.0F;// -> -X
                for (int i = 0; i < MAX_LENGTH; i++) {
                    if (!this.attachBooster(this.xCoord - i - 1, this.yCoord, this.zCoord, notifyClient)) {
                        break;
                    }
                }
            }
            case 2 -> {
                // rotation = 0;// -> -Z
                for (int i = 0; i < MAX_LENGTH; i++) {
                    if (!this.attachBooster(this.xCoord, this.yCoord, this.zCoord - i - 1, notifyClient)) {
                        break;
                    }
                }
            }
            case 3 -> {
                // rotation = 270.0F;// -> X
                for (int i = 0; i < MAX_LENGTH; i++) {
                    if (!this.attachBooster(this.xCoord + i + 1, this.yCoord, this.zCoord, notifyClient)) {
                        break;
                    }
                }
            }
        }
        this.fuelTank.setCapacity(this.getTankCapacity());
        if (this.fuelTank.getCapacity() < this.fuelTank.getFluidAmount()) {
            this.fuelTank.drain(this.fuelTank.getFluidAmount() - this.fuelTank.getCapacity(), true);
        }
    }

    /**
     * Should consume the fuel needed for the transition, on client side also start any animation or something alike.
     * This will be called for all engines which are actually being used
     */
    @Override
    public void beginTransit(final long duration) {
        this.isInUseForTransit = true;

        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    /**
     * This should be the master source on how much fuel we will need
     */
    @Override
    abstract public MothershipFuelRequirements getFuelRequirements(long duration);

    /**
     * Will be called on all which return true from isInUse on transit end
     */
    @Override
    public void endTransit() {
        this.isInUseForTransit = false;
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    /**
     * Should return whenever beginTransit has been called on this engine, and endTransit hasn't yet
     */
    @Override
    public boolean isInUse() {
        return this.isInUseForTransit;
    }

    @Override
    public void setDisabled(final int index, final boolean disabled) {
        if (!this.isInUse()) {
            // while disabling an engine in use won't do anything, still, don't do that.
            super.setDisabled(index, disabled);
        }
    }

    @Override
    public ForgeDirection getElectricInputDirection() {
        return null;
    }

    @Override
    public ItemStack getBatteryInSlot() {
        return null;
    }

    @Override
    public boolean isDonePlaying() {
        return !this.isInUseForTransit;
    }

    @Override
    abstract public double getThrust();

    @Override
    public void slowDischarge() {
        // don't!
    }

    @Override
    public abstract boolean canRunForDuration(long duration);

    @Override
    public int getDirection() {
        return this.getRotationMeta(this.getBlockMetadata());
    }

    @Override
    public boolean isEnabled() {

        return !this.getDisabled(0) && !this.isObstructed();
    }

    @Override
    public void updateFacing() {
        this.resetMultiblock();
        this.scheduleUpdate();
        super.updateFacing();
    }

}
