package de.katzenpapst.amunra.tile;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.helper.CoordHelper;
import de.katzenpapst.amunra.helper.NbtHelper;
import micdoodle8.mods.galacticraft.api.entity.IAntiGrav;
import micdoodle8.mods.galacticraft.api.power.IEnergyHandlerGC;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseElectricBlock;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class TileEntityGravitation extends TileBaseElectricBlock implements IInventory, IEnergyHandlerGC {

    // protected Vector3 gravityVector;

    protected double gravity;

    private ItemStack[] containingItems = new ItemStack[1];

    protected AxisAlignedBB gravityBox;

    public boolean isBoxShown;

    public TileEntityGravitation() {
        this.isBoxShown = false;

        // gravityVector = new Vector3(0.0, -0.05D, 0.0);
        this.gravity = -0.05D;
        // Vector3 center = new Vector3(xCoord+0.5D, yCoord+0.5D, zCoord+0.5D);

        // gravityBox = AxisAlignedBB.getBoundingBox(center.x - range, center.y - 0.5, center.z - range, center.x +
        // range, center.y + range, center.z + range);
        this.gravityBox = AxisAlignedBB.getBoundingBox(-5.0, 0, -5.0, +5.0, +5.0, +5.0);

        this.updateEnergyConsumption();
    }

    @Override
    public Packet getDescriptionPacket() {
        final NBTTagCompound data = new NBTTagCompound();
        this.writeToNBT(data);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 2, data);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (this.isBoxShown) {

            return this.getActualGravityBox();
        }
        return super.getRenderBoundingBox();
    }

    protected AxisAlignedBB getActualGravityBox() {
        AxisAlignedBB box = this.getRotatedAABB();// AxisAlignedBB.getBoundingBox(center.x - range, center.y - 0.5,
                                                  // center.z
        return AxisAlignedBB.getBoundingBox(
                this.xCoord + box.minX,
                this.yCoord + box.minY,
                this.zCoord + box.minZ,
                this.xCoord + box.maxX + 1,
                this.yCoord + box.maxY + 1,
                this.zCoord + box.maxZ + 1);
    }

    public AxisAlignedBB getRotatedAABB() {
        final int rotationMeta = this.getRotationMeta();
        final AxisAlignedBB in = this.getGravityBox();

        /*
         * Z ^ | maxVec | v +-----------------+ | | | | | | | | | +--+ | | | | | | X--+ | | | | |
         * +-----------------+--------------> X ^ minVec
         */

        switch (rotationMeta) {
            case 0: // identity
                return CoordHelper.cloneAABB(in); // correct
            case 1: // rotate 180°
                // minX <- maxX
                // maxX <- minX
                // minZ <- maxZ
                // maxZ <- minZ
                return AxisAlignedBB
                        .getBoundingBox(in.maxX * -1, in.minY, in.maxZ * -1, in.minX * -1, in.maxY, in.minZ * -1);
            // correct
            case 2: // rotate 270° in uhrzeigersinn
                // wrong
                // minX <- maxZ
                // maxX <- minZ
                // minZ <- minX
                // maxZ <- maxX
                // return AxisAlignedBB.getBoundingBox(in.maxZ * -1, in.minY, in.minX, in.minZ * -1, in.maxY, in.maxX);
                return AxisAlignedBB.getBoundingBox(in.minZ, in.minY, in.maxX * -1, in.maxZ, in.maxY, in.minX * -1);
            case 3: // rotate 90°
                // minX <- minZ
                // maxX <- maxZ
                // minZ <- maxX
                // maxZ <- minX
                // return AxisAlignedBB.getBoundingBox(in.minZ, in.minY, in.maxX * -1, in.maxZ, in.maxY, in.minX * -1);
                return AxisAlignedBB.getBoundingBox(in.maxZ * -1, in.minY, in.minX, in.minZ * -1, in.maxY, in.maxX);
        }

        return in;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (this.isRunning()) {
            this.doGravity();
        }
    }

    public boolean isRunning() {
        return !this.getDisabled(0) && this.hasEnoughEnergyToRun;
    }

    /*
     * public void setGravityVector(Vector3 vec) { //this.gravityVector = vec; gravity = vec.y; }
     */

    public void setGravityForce(final double value) {
        this.gravity = value;
    }

    public double getGravityForce() {
        return this.gravity;
        // return gravityVector;
    }
    /*
     * public Vector3 getGravityVector() { return new Vector3(0, gravity, 0); //return gravityVector; }
     */

    public AxisAlignedBB getGravityBox() {
        // return AxisAlignedBB.getBoundingBox( - range, - 0.5, - range, + range, + range, + range);
        return this.gravityBox;
    }

    public void setGravityBox(final AxisAlignedBB box) {
        this.gravityBox = box;
    }

    protected void doGravity() {
        final AxisAlignedBB box = this.getActualGravityBox();

        if (!this.worldObj.isRemote) {
            final List<?> list = this.worldObj.getEntitiesWithinAABB(Entity.class, box);

            for (final Object e : list) {
                if (e instanceof IAntiGrav) {
                    continue;
                }
                final Entity ent = (Entity) e;
                if (!(ent instanceof EntityPlayer)) {
                    ent.addVelocity(0.0D, this.gravity, 0.0D);
                    // do something with the fall distance
                }
                ent.fallDistance -= this.gravity * 10.0F;
                if (ent.fallDistance < 0) {
                    ent.fallDistance = 0.0F;
                }
            }
        } else {
            // player stuff has to be done on client
            final List<?> list = this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, box);
            for (final Object e : list) {
                final EntityPlayer p = (EntityPlayer) e;
                AmunRa.proxy.handlePlayerArtificalGravity(p, this.gravity);
            }
        }
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
        ItemStack newStack;

        if (this.containingItems[index].stackSize <= count) {
            newStack = this.containingItems[index];
            this.containingItems[index] = null;
        } else {
            newStack = this.containingItems[index].splitStack(count);

            if (this.containingItems[index].stackSize == 0) {
                this.containingItems[index] = null;
            }
        }
        return newStack;
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
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.containingItems = NbtHelper.readInventory(nbt, this.containingItems.length);

        if (nbt.hasKey("gravforce")) {
            double grav = nbt.getDouble("gravforce");
            if (grav == 0) {
                grav = -0.05D;
            }
            this.setGravityForce(grav);
        } else // backwards compatibility
            if (nbt.hasKey("gravity")) {
                final Vector3 grav = new Vector3(nbt.getCompoundTag("gravity"));
                this.setGravityForce(grav.y);
            }
        if (nbt.hasKey("aabb")) {
            final AxisAlignedBB box = NbtHelper.readAABB(nbt.getCompoundTag("aabb"));
            this.setGravityBox(box);
        }
        this.updateEnergyConsumption();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NbtHelper.writeInventory(nbt, this.containingItems);

        /*
         * NBTTagCompound gravityVectorNBT = new NBTTagCompound(); gravityVector.writeToNBT(gravityVectorNBT);
         */

        final NBTTagCompound aabbNBT = NbtHelper.getAsNBT(this.gravityBox);

        // nbt.setTag("gravity", gravityVectorNBT);
        nbt.setDouble("gravforce", this.gravity);
        nbt.setTag("aabb", aabbNBT);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.containingItems[index] = stack;

        if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
            stack.stackSize = this.getInventoryStackLimit();
        }
    }

    @Override
    public String getInventoryName() {
        return GCCoreUtil.translate("gui.title.gravity");
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
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(final int slotNr, final ItemStack stack) {

        return switch (slotNr) {
            case 0 -> ItemElectricBase.isElectricItem(stack.getItem()); // battery
            default -> false;
        };

    }

    @Override
    public boolean shouldUseEnergy() {
        return !this.getDisabled(0);
    }

    public int getRotationMeta(final int meta) {
        return (meta & 12) >> 2;
    }

    public int getRotationMeta() {
        return (this.getBlockMetadata() & 12) >> 2;
    }

    @Override
    public ForgeDirection getElectricInputDirection() {
        final int metadata = this.getRotationMeta();
        return CoordHelper.rotateForgeDirection(ForgeDirection.NORTH, metadata);
    }

    @Override
    public ItemStack getBatteryInSlot() {
        return this.getStackInSlot(0);
    }

    public void updateEnergyConsumption() {
        final double strength = Math.abs(this.gravity); // getGravityVector().getMagnitude();
        final AxisAlignedBB box = this.getGravityBox();

        final Vector3 size = new Vector3(box.maxX - box.minX + 1, box.maxY - box.minY + 1, box.maxZ - box.minZ + 1);
        final double numBlocks = size.x * size.y * size.z;

        final float maxExtract = (float) (numBlocks * strength);
        this.storage.setMaxExtract(maxExtract);
    }
}
