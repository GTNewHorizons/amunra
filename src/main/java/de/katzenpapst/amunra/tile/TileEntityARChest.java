package de.katzenpapst.amunra.tile;

import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import de.katzenpapst.amunra.block.BlockARChest;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class TileEntityARChest extends TileEntity implements IInventory {

    public TileEntityARChest() {}

    protected BlockMetaPair chestType;

    protected ItemStack[] chestContents = new ItemStack[36];

    /**
     * Determines if the check for adjacent chests has taken place.
     */
    public boolean adjacentChestChecked = false;

    /**
     * Contains the chest tile located adjacent to this one (if any)
     */
    public TileEntityARChest adjacentChestZNeg;

    /**
     * Contains the chest tile located adjacent to this one (if any)
     */
    public TileEntityARChest adjacentChestXPos;

    /**
     * Contains the chest tile located adjacent to this one (if any)
     */
    public TileEntityARChest adjacentChestXNeg;

    /**
     * Contains the chest tile located adjacent to this one (if any)
     */
    public TileEntityARChest adjacentChestZPos;

    /**
     * The current angle of the lid (between 0 and 1)
     */
    public float lidAngle;

    /**
     * The angle of the lid last tick
     */
    public float prevLidAngle;

    /**
     * The number of players currently using this chest
     */
    public int numUsingPlayers;

    /**
     * Server sync counter (once per 20 ticks)
     */
    private int ticksSinceSync;

    @Override
    public int getSizeInventory() {
        return 27;
    }

    @Override
    public ItemStack getStackInSlot(int slotIn) {
        return this.chestContents[slotIn];
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (this.chestContents[index] == null) {
            return null;
        }
        ItemStack itemstack;

        if (this.chestContents[index].stackSize <= count) {
            itemstack = this.chestContents[index];
            this.chestContents[index] = null;
        } else {
            itemstack = this.chestContents[index].splitStack(count);

            if (this.chestContents[index].stackSize == 0) {
                this.chestContents[index] = null;
            }
        }
        this.markDirty();
        return itemstack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        if (this.chestContents[index] != null) {
            final ItemStack itemstack = this.chestContents[index];
            this.chestContents[index] = null;
            return itemstack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.chestContents[index] = stack;

        if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
            stack.stackSize = this.getInventoryStackLimit();
        }

        this.markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        final NBTTagList nbttaglist = compound.getTagList("Items", 10);
        this.chestContents = new ItemStack[this.getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            final NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            final int j = nbttagcompound1.getByte("Slot") & 255;

            if (j < this.chestContents.length) {
                this.chestContents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        final NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.chestContents.length; ++i) {
            if (this.chestContents[i] != null) {
                final NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) i);
                this.chestContents[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        compound.setTag("Items", nbttaglist);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this
                && player.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void updateContainingBlockInfo() {
        super.updateContainingBlockInfo();
        this.adjacentChestChecked = false;
    }

    private void resetAdjacentChestOrSomething(final TileEntityARChest par1TileEntityChest, final int direction) {
        if (par1TileEntityChest.isInvalid()) {
            this.adjacentChestChecked = false;
        } else if (this.adjacentChestChecked) {
            switch (direction) {
                case 0:
                    if (this.adjacentChestZPos != par1TileEntityChest) {
                        this.adjacentChestChecked = false;
                    }

                    break;
                case 1:
                    if (this.adjacentChestXNeg != par1TileEntityChest) {
                        this.adjacentChestChecked = false;
                    }

                    break;
                case 2:
                    if (this.adjacentChestZNeg != par1TileEntityChest) {
                        this.adjacentChestChecked = false;
                    }

                    break;
                case 3:
                    if (this.adjacentChestXPos != par1TileEntityChest) {
                        this.adjacentChestChecked = false;
                    }
            }
        }
    }

    /**
     * Performs the check for adjacent chests to determine if this chest is double or not.
     */
    public void checkForAdjacentChests() {
        // Block b = this.getBlockType();
        if (!this.canDoublechest()) {
            return;
        }
        if (!this.adjacentChestChecked) {
            this.adjacentChestChecked = true;
            this.adjacentChestZNeg = null;
            this.adjacentChestXPos = null;
            this.adjacentChestXNeg = null;
            this.adjacentChestZPos = null;

            if (this.isSameChestType(this.xCoord - 1, this.yCoord, this.zCoord)) {
                this.adjacentChestXNeg = (TileEntityARChest) this.worldObj
                        .getTileEntity(this.xCoord - 1, this.yCoord, this.zCoord);
            }

            if (this.isSameChestType(this.xCoord + 1, this.yCoord, this.zCoord)) {
                this.adjacentChestXPos = (TileEntityARChest) this.worldObj
                        .getTileEntity(this.xCoord + 1, this.yCoord, this.zCoord);
            }

            if (this.isSameChestType(this.xCoord, this.yCoord, this.zCoord - 1)) {
                this.adjacentChestZNeg = (TileEntityARChest) this.worldObj
                        .getTileEntity(this.xCoord, this.yCoord, this.zCoord - 1);
            }

            if (this.isSameChestType(this.xCoord, this.yCoord, this.zCoord + 1)) {
                this.adjacentChestZPos = (TileEntityARChest) this.worldObj
                        .getTileEntity(this.xCoord, this.yCoord, this.zCoord + 1);
            }

            if (this.adjacentChestZNeg != null) {
                this.adjacentChestZNeg.resetAdjacentChestOrSomething(this, 0);
            }

            if (this.adjacentChestZPos != null) {
                this.adjacentChestZPos.resetAdjacentChestOrSomething(this, 2);
            }

            if (this.adjacentChestXPos != null) {
                this.adjacentChestXPos.resetAdjacentChestOrSomething(this, 1);
            }

            if (this.adjacentChestXNeg != null) {
                this.adjacentChestXNeg.resetAdjacentChestOrSomething(this, 3);
            }
        }
    }

    protected boolean canDoublechest() {
        return true;
    }

    private boolean isSameChestType(final int x, final int y, final int z) {
        if (this.chestType == null) {
            this.chestType = new BlockMetaPair(this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord), (byte) 0);
        }
        final Block block = this.worldObj.getBlock(x, y, z);

        return block != null && block == this.chestType.getBlock();
    }

    /**
     * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner uses this to count
     * ticks and creates a new spawn inside its implementation.
     */
    @Override
    public void updateEntity() {
        super.updateEntity();
        this.checkForAdjacentChests();
        ++this.ticksSinceSync;
        float f;

        if (!this.worldObj.isRemote && this.numUsingPlayers != 0
                && (this.ticksSinceSync + this.xCoord + this.yCoord + this.zCoord) % 200 == 0) {
            this.numUsingPlayers = 0;
            f = 5.0F;
            final List<?> list = this.worldObj.getEntitiesWithinAABB(
                    EntityPlayer.class,
                    AxisAlignedBB.getBoundingBox(
                            this.xCoord - f,
                            this.yCoord - f,
                            this.zCoord - f,
                            this.xCoord + 1 + f,
                            this.yCoord + 1 + f,
                            this.zCoord + 1 + f));
            final Iterator<?> iterator = list.iterator();

            while (iterator.hasNext()) {
                final EntityPlayer entityplayer = (EntityPlayer) iterator.next();

                if (entityplayer.openContainer instanceof ContainerChest) {
                    final IInventory iinventory = ((ContainerChest) entityplayer.openContainer)
                            .getLowerChestInventory();

                    if (iinventory == this || iinventory instanceof InventoryLargeChest
                            && ((InventoryLargeChest) iinventory).isPartOfLargeChest(this)) {
                        ++this.numUsingPlayers;
                    }
                }
            }
        }

        this.prevLidAngle = this.lidAngle;
        f = 0.05F;
        double d0;

        if (this.numUsingPlayers > 0 && this.lidAngle == 0.0F
                && this.adjacentChestZNeg == null
                && this.adjacentChestXNeg == null) {
            double d1 = this.xCoord + 0.5D;
            d0 = this.zCoord + 0.5D;

            if (this.adjacentChestZPos != null) {
                d0 += 0.5D;
            }

            if (this.adjacentChestXPos != null) {
                d1 += 0.5D;
            }

            this.worldObj.playSoundEffect(
                    d1,
                    this.yCoord + 0.5D,
                    d0,
                    "random.chestopen",
                    0.5F,
                    this.worldObj.rand.nextFloat() * 0.1F + 0.6F);
        }

        if (this.numUsingPlayers == 0 && this.lidAngle > 0.0F || this.numUsingPlayers > 0 && this.lidAngle < 1.0F) {
            final float f1 = this.lidAngle;

            if (this.numUsingPlayers > 0) {
                this.lidAngle += f;
            } else {
                this.lidAngle -= f;
            }

            if (this.lidAngle > 1.0F) {
                this.lidAngle = 1.0F;
            }

            final float f2 = 0.5F;

            if (this.lidAngle < f2 && f1 >= f2 && this.adjacentChestZNeg == null && this.adjacentChestXNeg == null) {
                d0 = this.xCoord + 0.5D;
                double d2 = this.zCoord + 0.5D;

                if (this.adjacentChestZPos != null) {
                    d2 += 0.5D;
                }

                if (this.adjacentChestXPos != null) {
                    d0 += 0.5D;
                }

                this.worldObj.playSoundEffect(
                        d0,
                        this.yCoord + 0.5D,
                        d2,
                        "random.chestclosed",
                        0.5F,
                        this.worldObj.rand.nextFloat() * 0.1F + 0.6F);
            }

            if (this.lidAngle < 0.0F) {
                this.lidAngle = 0.0F;
            }
        }
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            this.numUsingPlayers = type;
            return true;
        }
        return super.receiveClientEvent(id, type);
    }

    @Override
    public void openInventory() {
        if (this.numUsingPlayers < 0) {
            this.numUsingPlayers = 0;
        }

        ++this.numUsingPlayers;
        this.worldObj
                .addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType(), 1, this.numUsingPlayers);
        this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
        this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord - 1, this.zCoord, this.getBlockType());
    }

    @Override
    public void closeInventory() {
        if (this.getBlockType() != null && this.getBlockType() instanceof BlockARChest) {
            --this.numUsingPlayers;
            this.worldObj
                    .addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType(), 1, this.numUsingPlayers);
            this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
            this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord - 1, this.zCoord, this.getBlockType());
        }
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        this.updateContainingBlockInfo();
        this.checkForAdjacentChests();
    }

    @Override
    public String getInventoryName() {
        return this.getBlockType().getLocalizedName();
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    public static boolean isOcelotBlockingChest(final World par0World, final int par1, final int par2, final int par3) {
        final Iterator<EntityOcelot> var4 = par0World.getEntitiesWithinAABB(
                EntityOcelot.class,
                AxisAlignedBB.getBoundingBox(par1, par2 + 1, par3, par1 + 1, par2 + 2, par3 + 1)).iterator();
        EntityOcelot var6;

        do {
            if (!var4.hasNext()) {
                return false;
            }

            var6 = (EntityOcelot) var4.next();
        } while (!var6.isSitting());

        return true;
    }

}
