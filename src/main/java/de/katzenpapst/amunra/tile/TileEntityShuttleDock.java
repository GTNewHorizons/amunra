package de.katzenpapst.amunra.tile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.FluidStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.GuiIds;
import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.entity.spaceship.EntityShuttle;
import de.katzenpapst.amunra.item.ItemShuttle;
import de.katzenpapst.amunra.network.packet.PacketSimpleAR;
import de.katzenpapst.amunra.world.ShuttleDockHandler;
import micdoodle8.mods.galacticraft.api.entity.ICargoEntity;
import micdoodle8.mods.galacticraft.api.entity.IDockable;
import micdoodle8.mods.galacticraft.api.entity.IFuelable;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntitySpaceshipBase.EnumLaunchPhase;
import micdoodle8.mods.galacticraft.api.tile.IFuelDock;
import micdoodle8.mods.galacticraft.api.tile.ILandingPadAttachable;
import micdoodle8.mods.galacticraft.api.vector.BlockVec3;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.network.IPacketReceiver;
import micdoodle8.mods.galacticraft.core.network.PacketSimple;
import micdoodle8.mods.galacticraft.core.tile.IMultiBlock;
import micdoodle8.mods.galacticraft.core.tile.TileEntityAdvanced;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class TileEntityShuttleDock extends TileEntityAdvanced
        implements IFuelable, IFuelDock, ICargoEntity, IMultiBlock, IInventory, IPacketReceiver {

    protected boolean hasShuttleDocked;
    protected int actionCooldown;

    protected ItemStack[] containingItems = new ItemStack[1];
    protected IDockable dockedEntity;

    public enum DockOperation {
        DEPLOY_SHUTTLE,
        GET_SHUTTLE,
        MOUNT_SHUTTLE
    }

    protected void dropItemsAtExit(final List<ItemStack> cargo) {
        final Vector3 pos = this.getExitPosition();
        for (final ItemStack item : cargo) {
            final EntityItem itemEntity = new EntityItem(this.worldObj, pos.x, pos.y, pos.z, item);
            this.worldObj.spawnEntityInWorld(itemEntity);
        }
    }

    public void performDockOperation(final int op, final EntityPlayerMP player) {
        if (op >= DockOperation.values().length) {
            return;
        }
        this.performDockOperation(DockOperation.values()[op], player);
    }

    /**
     * Server-side part
     */
    public void performDockOperation(final DockOperation op, final EntityPlayerMP player) {
        if (this.actionCooldown > 0) {
            return;
        }
        this.actionCooldown = 20;
        ItemShuttle shuttleItem;
        EntityShuttle shuttleEntity;
        ItemStack stack;
        switch (op) {
            case DEPLOY_SHUTTLE:
                if (this.dockedEntity != null) {
                    return; // doesn't work
                }
                stack = this.getStackInSlot(0);
                if (stack == null || stack.stackSize == 0
                        || !(stack.getItem() instanceof ItemShuttle)
                        || this.isObstructed()) {
                    return;
                }
                shuttleItem = (ItemShuttle) stack.getItem();
                final Vector3 pos = this.getShuttlePosition();
                shuttleEntity = shuttleItem.spawnRocketEntity(stack, this.worldObj, pos.x, pos.y, pos.z);
                this.dockEntity(shuttleEntity);
                stack.stackSize--;
                if (stack.stackSize <= 0) {
                    stack = null;
                }
                this.setInventorySlotContents(0, stack);
                this.hasShuttleDocked = true;
                break;
            case GET_SHUTTLE:
                if (this.dockedEntity == null) {
                    return;
                }
                stack = this.getStackInSlot(0);
                if (stack != null) {
                    return;
                }
                shuttleEntity = (EntityShuttle) this.dockedEntity;
                stack = shuttleEntity.getItemRepresentation();

                final List<ItemStack> cargo = shuttleEntity.getCargoContents();
                this.dropItemsAtExit(cargo);

                this.setInventorySlotContents(0, stack);
                shuttleEntity.setDead();
                this.dockedEntity = null;
                this.hasShuttleDocked = false;

                break;
            case MOUNT_SHUTTLE:
                if (this.dockedEntity == null) {
                    return;
                }
                shuttleEntity = (EntityShuttle) this.dockedEntity;
                if (shuttleEntity.riddenByEntity != null) {
                    return;
                }
                player.mountEntity(shuttleEntity);
                GalacticraftCore.packetPipeline
                        .sendTo(new PacketSimple(PacketSimple.EnumSimplePacket.C_CLOSE_GUI, new Object[] {}), player);
                break;
            default:
                return;

        }
        this.updateAvailabilityInWorldData();
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    public void performDockOperationClient(final DockOperation op) {
        final int opInt = op.ordinal();

        final Object[] payload = { this.xCoord, this.yCoord, this.zCoord, opInt };
        AmunRa.packetPipeline
                .sendToServer(new PacketSimpleAR(PacketSimpleAR.EnumSimplePacket.S_DOCK_OPERATION, payload));
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.containingItems = this.readStandardItemsFromNBT(compound);
        this.hasShuttleDocked = compound.getBoolean("hasShuttle");
        this.actionCooldown = compound.getInteger("actionCooldown");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        this.writeStandardItemsToNBT(compound);
        compound.setBoolean("hasShuttle", this.hasShuttleDocked);
        compound.setInteger("actionCooldown", this.actionCooldown);
    }

    public ItemStack[] readStandardItemsFromNBT(final NBTTagCompound nbt) {
        final NBTTagList itemTag = nbt.getTagList("Items", 10);
        final int length = this.containingItems.length;
        final ItemStack[] result = new ItemStack[length];

        for (int i = 0; i < itemTag.tagCount(); ++i) {
            final NBTTagCompound stackNbt = itemTag.getCompoundTagAt(i);
            final int slotNr = stackNbt.getByte("Slot") & 255;

            if (slotNr < length) {
                result[slotNr] = ItemStack.loadItemStackFromNBT(stackNbt);
            }
        }
        return result;
    }

    public boolean hasShuttle() {
        return this.hasShuttleDocked;
    }

    public void writeStandardItemsToNBT(final NBTTagCompound nbt) {
        final NBTTagList list = new NBTTagList();
        final int length = this.containingItems.length;

        for (int i = 0; i < length; ++i) {
            if (this.containingItems[i] != null) {
                final NBTTagCompound stackNbt = new NBTTagCompound();
                stackNbt.setByte("Slot", (byte) i);
                this.containingItems[i].writeToNBT(stackNbt);
                list.appendTag(stackNbt);
            }
        }

        nbt.setTag("Items", list);
    }

    @Override
    public Packet getDescriptionPacket() {
        final NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);

        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
    }

    public Vector3 getShuttlePosition() {
        switch (this.getRotationMeta()) {
            case 0:
                return new Vector3(this.xCoord + 0.5, this.yCoord, this.zCoord - 1.5D);
            case 2:
                return new Vector3(this.xCoord - 1.5D, this.yCoord, this.zCoord + 0.5D);
            case 1:
                return new Vector3(this.xCoord + 0.5, this.yCoord, this.zCoord + 2.5D);
            case 3:
                return new Vector3(this.xCoord + 2.5D, this.yCoord, this.zCoord + 0.5D);
        }
        return null;
    }

    public float getExitRotation() {
        switch (this.getRotationMeta()) {
            case 0: // -> +Z (the side which is towards the player)
                return 0.0F;
            case 2: // -> -Z
                return 270.0F;
            case 1: // -> -X
                return 180.0F;
            case 3: // -> +X
                return 90.0F;
        }
        return 0;
    }

    public Vector3 getExitPosition() {
        switch (this.getRotationMeta()) {
            case 0: // -> +Z (the side which is towards the player)
                return new Vector3(this.xCoord + 0.5, this.yCoord, this.zCoord + 1.5D);
            case 2: // -> -Z
                return new Vector3(this.xCoord + 1.5D, this.yCoord, this.zCoord + 0.5D);
            case 1: // -> -X
                return new Vector3(this.xCoord + 0.5, this.yCoord, this.zCoord - 0.5D);
            case 3: // -> +X
                return new Vector3(this.xCoord - 0.5D, this.yCoord, this.zCoord + 0.5D);
        }
        return null;
    }

    protected void repositionEntity() {
        final Vector3 pos = this.getShuttlePosition();

        ((Entity) this.dockedEntity).setPosition(pos.x, pos.y, pos.z);
    }

    protected void dockNearbyShuttle() {
        // this is an awful hack...
        final Vector3 expectedPosition = this.getShuttlePosition();
        final List<EntityShuttle> list = this.worldObj.getEntitiesWithinAABB(
                EntityShuttle.class,
                AxisAlignedBB.getBoundingBox(
                        expectedPosition.x - 0.5D,
                        expectedPosition.y - 0.5D,
                        expectedPosition.z - 0.5D,
                        expectedPosition.x + 0.5D,
                        expectedPosition.y + 0.5D,
                        expectedPosition.z + 0.5D));

        for (EntityShuttle fuelable : list) {
            if (fuelable.launchPhase == EnumLaunchPhase.UNIGNITED.ordinal()) {

                // fuelable.setPad(this);
                this.dockEntity(fuelable);

                break;
            }
        }
        if (!list.isEmpty()) {
            this.updateAvailabilityInWorldData();
        }
    }

    @Override
    public void onChunkUnload() {}

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (!this.worldObj.isRemote) {
            if (this.actionCooldown > 0) {
                this.actionCooldown--;
            }
            if (this.dockedEntity != null) {

                final EntityShuttle shuttle = (EntityShuttle) this.dockedEntity;
                // before we do anything else
                if (shuttle.isDead) {
                    this.dockedEntity = null;
                } else if (shuttle.launchPhase == EnumLaunchPhase.LAUNCHED.ordinal()) {
                    // undock
                    shuttle.setPad(null);
                    this.dockedEntity = null;
                    this.updateAvailabilityInWorldData();
                } else // from time to time, reposition?
                    if (this.ticks % 40 == 0) {
                        this.repositionEntity();
                    }
            }

            if (this.dockedEntity == null) {
                // attempt to redock something
                this.dockNearbyShuttle();
            }

            // update status
            if (this.dockedEntity == null && this.hasShuttleDocked) {
                this.hasShuttleDocked = false;
                this.markDirty();
                this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            } else if (this.dockedEntity != null && !this.hasShuttleDocked) {
                this.hasShuttleDocked = true;
                this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            }

            // from time to time, update the dockdata
            if (this.ticks % 35 == 0) {
                this.updateAvailabilityInWorldData();
            }
        }
    }

    protected void updateAvailabilityInWorldData() {
        final boolean curAvailability = this.isAvailable();
        final boolean wasAvailableLastCheck = ShuttleDockHandler.getStoredAvailability(this);
        if (wasAvailableLastCheck != curAvailability) {
            ShuttleDockHandler.setStoredAvailability(this, curAvailability);
        }
    }

    public int getRotationMeta(final int meta) {
        return (meta & 12) >> 2;
    }

    public int getRotationMeta() {
        return (this.getBlockMetadata() & 12) >> 2;
    }

    @Override
    public EnumCargoLoadingState addCargo(final ItemStack stack, final boolean doAdd) {
        if (this.dockedEntity != null) {
            return this.dockedEntity.addCargo(stack, doAdd);
        }

        return EnumCargoLoadingState.NOTARGET;
    }

    @Override
    public RemovalResult removeCargo(final boolean doRemove) {
        if (this.dockedEntity != null) {
            return this.dockedEntity.removeCargo(doRemove);
        }

        return new RemovalResult(EnumCargoLoadingState.NOTARGET, null);
    }

    protected void checkTileAt(final Set<ILandingPadAttachable> connectedTiles, final int x, final int y, final int z) {
        final TileEntity tile = this.worldObj.getTileEntity(x, y, z);

        if (tile instanceof ILandingPadAttachable landingPad
                && landingPad.canAttachToLandingPad(this.worldObj, this.xCoord, this.yCoord, this.zCoord)) {
            connectedTiles.add(landingPad);
        }
    }

    @Override
    public HashSet<ILandingPadAttachable> getConnectedTiles() {
        final HashSet<ILandingPadAttachable> connectedTiles = new HashSet<>();

        // check the blocks in a doorframe form around me
        // below
        this.checkTileAt(connectedTiles, this.xCoord, this.yCoord - 1, this.zCoord);

        // above
        this.checkTileAt(connectedTiles, this.xCoord, this.yCoord + 2, this.zCoord);

        // sides
        switch (this.getRotationMeta()) {
            case 0: // -> +Z (the side which is towards the player)
            case 2: // -> -Z
                this.checkTileAt(connectedTiles, this.xCoord - 1, this.yCoord, this.zCoord);
                this.checkTileAt(connectedTiles, this.xCoord + 1, this.yCoord, this.zCoord);
                this.checkTileAt(connectedTiles, this.xCoord - 1, this.yCoord + 1, this.zCoord);
                this.checkTileAt(connectedTiles, this.xCoord + 1, this.yCoord + 1, this.zCoord);
                break;
            case 1: // -> -X
            case 3: // -> +X
                this.checkTileAt(connectedTiles, this.xCoord, this.yCoord, this.zCoord - 1);
                this.checkTileAt(connectedTiles, this.xCoord, this.yCoord, this.zCoord + 1);
                this.checkTileAt(connectedTiles, this.xCoord, this.yCoord + 1, this.zCoord - 1);
                this.checkTileAt(connectedTiles, this.xCoord, this.yCoord + 1, this.zCoord + 1);
                break;
        }
        // maybe do the edges, too?

        return connectedTiles;
    }

    @Override
    public boolean isBlockAttachable(final IBlockAccess world, final int x, final int y, final int z) {
        final TileEntity tile = world.getTileEntity(x, y, z);
        // maybe prevent launch controllers from working here?
        if (tile instanceof ILandingPadAttachable landingPad) {
            return landingPad.canAttachToLandingPad(world, this.xCoord, this.yCoord, this.zCoord);
        }

        return false;
    }

    @Override
    public IDockable getDockedEntity() {
        return this.dockedEntity;
    }

    @Override
    public void dockEntity(final IDockable entity) {
        if (entity == this.dockedEntity) {
            return;
        }
        if (entity instanceof EntityShuttle shuttle) {
            this.dockedEntity = entity;
            shuttle.setPad(this);
            this.repositionEntity();
        } else if (entity == null) {
            this.dockedEntity = null;
        }
        this.updateAvailabilityInWorldData();
    }

    @Override
    public int addFuel(final FluidStack fluid, final boolean doFill) {
        if (this.dockedEntity != null) {
            return this.dockedEntity.addFuel(fluid, doFill);
        }

        return 0;
    }

    @Override
    public FluidStack removeFuel(final int amount) {
        if (this.dockedEntity != null) {
            return this.dockedEntity.removeFuel(amount);
        }

        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {

        switch (this.getRotationMeta()) {
            case 0:
                return AxisAlignedBB.getBoundingBox(
                        this.xCoord,
                        this.yCoord,
                        this.zCoord - 1,
                        this.xCoord + 1,
                        this.yCoord + 2,
                        this.zCoord + 1);
            case 1:
                return AxisAlignedBB.getBoundingBox(
                        this.xCoord,
                        this.yCoord,
                        this.zCoord,
                        this.xCoord + 1,
                        this.yCoord + 2,
                        this.zCoord + 2);
            case 2:
                return AxisAlignedBB.getBoundingBox(
                        this.xCoord - 1,
                        this.yCoord,
                        this.zCoord,
                        this.xCoord + 1,
                        this.yCoord + 2,
                        this.zCoord + 1);
            case 3:
                return AxisAlignedBB.getBoundingBox(
                        this.xCoord,
                        this.yCoord,
                        this.zCoord,
                        this.xCoord + 2,
                        this.yCoord + 2,
                        this.zCoord + 1);
        }
        return AxisAlignedBB.getBoundingBox(
                this.xCoord,
                this.yCoord,
                this.zCoord,
                this.xCoord + 1,
                this.yCoord + 2,
                this.zCoord + 1);
    }

    @Override
    public double getPacketRange() {
        return 30.0D;
    }

    @Override
    public int getPacketCooldown() {
        return 50;
    }

    @Override
    public boolean isNetworkedTile() {
        return true;
    }

    @Override
    public boolean onActivated(final EntityPlayer entityPlayer) {
        entityPlayer.openGui(
                AmunRa.instance,
                GuiIds.GUI_SHUTTLE_DOCK,
                this.worldObj,
                this.xCoord,
                this.yCoord,
                this.zCoord);
        return true;
    }

    @Override
    public void onCreate(final BlockVec3 placedPosition) {
        ShuttleDockHandler.addDock(this);

        final int buildHeight = this.worldObj.getHeight() - 1;

        if (placedPosition.y + 1 > buildHeight) return;

        final BlockVec3 vecStrut = new BlockVec3(placedPosition.x, placedPosition.y + 1, placedPosition.z);
        ARBlocks.metaBlockFake.makeFakeBlock(
                this.worldObj,
                vecStrut,
                new BlockVec3(this.xCoord, this.yCoord, this.zCoord),
                ARBlocks.fakeBlockSealable);
    }

    @Override
    public void onDestroy(final TileEntity callingBlock) {
        ShuttleDockHandler.removeDock(this);

        final Block b = this.worldObj.getBlock(this.xCoord, this.yCoord + 1, this.zCoord);
        if (b == ARBlocks.metaBlockFake) {
            this.worldObj.setBlockToAir(this.xCoord, this.yCoord + 1, this.zCoord);
        }
        if (callingBlock != this) {
            // someone else called this, drop my actual block, too
            this.worldObj.func_147480_a(this.xCoord, this.yCoord, this.zCoord, true);

        }
    }

    @Override
    public String getInventoryName() {
        return GCCoreUtil.translate("tile.shuttleDock.name");
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
        ItemStack resultStack;

        if (this.containingItems[index].stackSize <= count) {
            resultStack = this.containingItems[index];
            this.containingItems[index] = null;
        } else {
            resultStack = this.containingItems[index].splitStack(count);

            if (this.containingItems[index].stackSize == 0) {
                this.containingItems[index] = null;
            }
        }
        return resultStack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        if (this.containingItems[index] != null) {
            final ItemStack result = this.containingItems[index];
            this.containingItems[index] = null;
            return result;
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
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return player.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;

    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == 0 && stack.getItem() instanceof ItemShuttle;
    }

    protected boolean areBlocksWithin(final int minX, final int minY, final int minZ, final int maxX, final int maxY,
            final int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (!this.worldObj.isAirBlock(x, y, z)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isObstructed() {
        final int minY = this.yCoord - 2;
        final int maxY = this.yCoord + 3;
        int minX;
        int maxX;
        int minZ;
        int maxZ;
        // check

        switch (this.getRotationMeta()) {
            case 0:
                minX = this.xCoord - 1;
                maxX = this.xCoord + 1;
                minZ = this.zCoord - 3;
                maxZ = this.zCoord - 1;
                break;
            case 2:
                minX = this.xCoord - 3;
                maxX = this.xCoord - 1;
                minZ = this.zCoord - 1;
                maxZ = this.zCoord + 1;
                break;
            case 1:
                minX = this.xCoord - 1;
                maxX = this.xCoord + 1;
                minZ = this.zCoord + 1;
                maxZ = this.zCoord + 3;
                break;
            case 3:
                minX = this.xCoord + 1;
                maxX = this.xCoord + 3;
                minZ = this.zCoord - 1;
                maxZ = this.zCoord + 1;
                break;
            default:
                return false;
        }

        return this.areBlocksWithin(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public boolean isAvailable() {
        if (this.dockedEntity != null || this.hasShuttleDocked) { // the former isn't that reliable, since dockedEntity
                                                                  // won't
            // be set until it has been rediscovered in the update
            return false;
        }
        return !this.isObstructed();
    }
}
