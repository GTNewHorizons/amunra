package de.katzenpapst.amunra.tile;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.item.ItemDamagePair;
import de.katzenpapst.amunra.world.WorldHelper;
import micdoodle8.mods.galacticraft.api.tile.IDisableableMachine;
import micdoodle8.mods.galacticraft.api.transmission.tile.IConnector;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.network.IPacketReceiver;
import micdoodle8.mods.galacticraft.core.tile.TileEntityOxygen;
import micdoodle8.mods.galacticraft.core.util.Annotations.NetworkedField;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class TileEntityHydroponics extends TileEntityOxygen
        implements IPacketReceiver, IDisableableMachine, IInventory, ISidedInventory, IConnector {

    public enum OperationType {
        PLANT_SEED,
        FERTILIZE,
        HARVEST
    }

    public boolean active;
    public static final int OUTPUT_PER_TICK = 100;
    @NetworkedField(targetSide = Side.CLIENT)
    public float lastOxygenCollected;
    private ItemStack[] containingItems = new ItemStack[2];

    @NetworkedField(targetSide = Side.CLIENT)
    public float plantGrowthStatus = -1.0F;

    public static ItemDamagePair seeds = null;
    public static ItemDamagePair bonemeal = null;

    public TileEntityHydroponics() {
        super(6000, 0);
        if (seeds == null) {
            seeds = new ItemDamagePair(Items.wheat_seeds, 0);
        }
        if (bonemeal == null) {
            bonemeal = new ItemDamagePair(Items.dye, 15);
        }
        this.storage.setMaxExtract(5);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (!this.worldObj.isRemote) {

            // this makes the thing output oxygen
            this.produceOxygen();

            // this actually genereates it
            this.generateOxygen();

            // grow the plant inside
            this.growPlant();
        }
    }

    /**
     * Make sure this does not exceed the oxygen stored. This should return 0 if no oxygen is stored. Implementing tiles
     * must respect this or you will generate infinite oxygen.
     */
    @Override
    public float getOxygenProvide(final ForgeDirection direction) {
        return this.getOxygenOutputDirections().contains(direction) ? Math.min(OUTPUT_PER_TICK, this.getOxygenStored())
                : 0.0F;
    }

    @Override
    public boolean shouldPullOxygen() {
        return false;
    }

    @Override
    public String getInventoryName() {
        return GCCoreUtil.translate("tile.hydroponics.name");
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {

        return switch (index) {
            case 0 -> ItemElectricBase.isElectricItem(stack.getItem()); // battery
            case 1 -> seeds.isSameItem(stack) || bonemeal.isSameItem(stack); // seeds or bonemeal
            default -> false;
        };

    }

    @Override
    public boolean shouldUseEnergy() {

        return !this.getDisabled(0);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
        return new int[] { 0, 1 };
    }

    @Override
    public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {
        return this.isItemValidForSlot(p_102007_1_, p_102007_2_);
    }

    @Override
    public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
        return p_102008_1_ == 0 || p_102008_1_ == 1;
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
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.containingItems = this.readStandardItemsFromNBT(compound);
        this.plantGrowthStatus = compound.getFloat("growthStatus");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        this.writeStandardItemsToNBT(compound);
        compound.setFloat("growthStatus", this.plantGrowthStatus);
        // plantedSeed
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

    protected void generateOxygen() {
        if (this.worldObj.rand.nextInt(10) == 0) {
            if (this.hasEnoughEnergyToRun && this.plantGrowthStatus > 0) {
                // 20 air points for 14 blocks per second!
                // 0.075F is the value from oxygen collector, or 0.075F*10.0F
                // divide by 10 for display
                // use it right away for actual generation?

                final float generationRate = 0.3F * 10.0F * this.plantGrowthStatus * AmunRa.config.hydroponicsFactor; // should
                // be 4x
                // the
                // regular
                // amount
                this.lastOxygenCollected = generationRate / 10F;

                this.storedOxygen = Math.max(Math.min(this.storedOxygen + generationRate, this.maxOxygen), 0);
            } else {
                this.lastOxygenCollected = 0;
            }
        }
    }

    protected void growPlant() {
        if (this.plantGrowthStatus == -1.0F || this.plantGrowthStatus == 1.0F
                || this.worldObj.getBlockLightValue(this.xCoord, this.yCoord + 1, this.zCoord) < 9) {
            return;
        }
        // wiki says: 5 - 35 minecraft minutes for one crop stage
        // 1 tick = 3,6 mineSeconds
        // 1 minute = 16,66.. ticks
        // 5 minutes = ca. 83,3333333333 ticks
        // 35 minutes = 583,3333333331

        // assume: p=100% at 400 (faster)
        // check stuff every 40 ticks
        // p=100% at 10 ticks
        // check every 20 -> p=100% at 20 ticks
        // p=0.1
        if (this.ticks % 20 == 0 && this.worldObj.rand.nextFloat() < 0.1F) {
            this.plantGrowthStatus += 0.01F;
            if (this.plantGrowthStatus > 1.0F) {
                this.plantGrowthStatus = 1.0F;
            }
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    /*
     * @Override public Packet getDescriptionPacket() { NBTTagCompound data = new NBTTagCompound();
     * this.writeToNBT(data); return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 2, data); }
     * @Override public void onDataPacket(NetworkManager netManager, S35PacketUpdateTileEntity packet) {
     * readFromNBT(packet.func_148857_g()); }
     */

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.containingItems[index] = stack;

        if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
            stack.stackSize = this.getInventoryStackLimit();
        }
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
    public boolean shouldUseOxygen() {
        return false;
    }

    @Override
    public ForgeDirection getElectricInputDirection() {
        return ForgeDirection.DOWN;
    }

    @Override
    public EnumSet<ForgeDirection> getOxygenOutputDirections() {
        return EnumSet.of(ForgeDirection.EAST, ForgeDirection.WEST, ForgeDirection.NORTH, ForgeDirection.SOUTH);
    }

    @Override
    public EnumSet<ForgeDirection> getOxygenInputDirections() {
        return EnumSet.noneOf(ForgeDirection.class);
    }

    @Override
    public ItemStack getBatteryInSlot() {
        return this.getStackInSlot(0);
    }

    public float getPlantGrowthStatus() {
        return this.plantGrowthStatus;
    }

    public void plantSeed() {
        this.plantGrowthStatus = 0;
    }

    public void fertilize() {
        this.plantGrowthStatus += 0.125F;
    }

    public ItemStack[] getHarvest() {
        if (this.plantGrowthStatus < 0) {
            return new ItemStack[] {};
        }
        if (this.plantGrowthStatus < 1) {
            return new ItemStack[] { seeds.getItemStack(1) };
        }
        final int numSeeds = this.worldObj.rand.nextInt(4);
        return new ItemStack[] { new ItemStack(Items.wheat, 1, 0), seeds.getItemStack(numSeeds) };
    }

    public void harvest(final EntityPlayer player) {

        final ItemStack[] harvest = this.getHarvest();
        for (final ItemStack stack : harvest) {
            if (!player.inventory.addItemStackToInventory(stack)) {
                WorldHelper.dropItemInWorld(this.worldObj, stack, player);
            }
        }
        this.plantGrowthStatus = 0.0F;
    }

    public void performOperation(final int op, final EntityPlayerMP playerBase) {
        final OperationType realOp = OperationType.values()[op];
        ItemStack stack;
        switch (realOp) {
            case PLANT_SEED -> {
                // I hope this works..
                stack = this.containingItems[1];
                if (this.plantGrowthStatus == -1.0F && stack != null
                        && stack.stackSize > 0
                        && seeds.isSameItem(stack)) {
                    stack.stackSize--;
                    if (stack.stackSize <= 0) {
                        stack = null;
                    }
                    this.containingItems[1] = stack;
                    this.plantSeed();
                }
            }
            case FERTILIZE -> {
                stack = this.containingItems[1];
                if (this.plantGrowthStatus >= 0.0F && this.plantGrowthStatus < 1.0F
                        && stack != null
                        && stack.stackSize > 0
                        && bonemeal.isSameItem(stack)) {
                    stack.stackSize--;
                    if (stack.stackSize <= 0) {
                        stack = null;
                    }
                    this.containingItems[1] = stack;
                    this.fertilize();
                }
            }
            case HARVEST -> {
                if (this.plantGrowthStatus == 1.0F) {
                    this.harvest(playerBase);
                }
            }
        }
    }

    @Override
    public boolean isUseableByPlayer(final EntityPlayer par1EntityPlayer) {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this
                && par1EntityPlayer.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
    }
}
