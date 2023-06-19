package de.katzenpapst.amunra.tile;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import de.katzenpapst.amunra.block.BlockMachineMeta;
import de.katzenpapst.amunra.block.SubBlockMachine;
import de.katzenpapst.amunra.block.machine.BlockIsotopeGenerator;
import de.katzenpapst.amunra.helper.CoordHelper;
import micdoodle8.mods.galacticraft.api.tile.IDisableableMachine;
import micdoodle8.mods.galacticraft.api.transmission.NetworkType;
import micdoodle8.mods.galacticraft.api.transmission.tile.IConnector;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseUniversalElectricalSource;
import micdoodle8.mods.galacticraft.core.network.IPacketReceiver;
import micdoodle8.mods.galacticraft.core.util.Annotations.NetworkedField;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class TileEntityIsotopeGenerator extends TileBaseUniversalElectricalSource
        implements IPacketReceiver, IDisableableMachine, IInventory, ISidedInventory, IConnector {

    @NetworkedField(targetSide = Side.CLIENT)
    public boolean disabled = false;
    @NetworkedField(targetSide = Side.CLIENT)
    public int disableCooldown = 0;
    private ItemStack[] containingItems = new ItemStack[1];
    // energy capacity
    protected final int energyCapacity = 30000;
    // energy generated when enabled
    protected float energyGeneration = 1;
    // actual generated energy
    @NetworkedField(targetSide = Side.CLIENT)
    public float generateWatts = 0;

    public static final int MAX_GENERATE_WATTS = 200;

    private boolean initialised = false;

    private SubBlockMachine subBlock = null;

    protected float generationBoost = -1;

    public TileEntityIsotopeGenerator() {
        // init();
    }

    public int getScaledElecticalLevel(final int i) {
        return (int) Math.floor(this.getEnergyStoredGC() * i / this.getMaxEnergyStoredGC());
    }

    protected void init() {
        // get generation rate
        this.energyGeneration = ((BlockIsotopeGenerator) this.getSubBlock()).energyGeneration;

        this.storage.setMaxExtract(MAX_GENERATE_WATTS);
        this.storage.setMaxReceive(MAX_GENERATE_WATTS);
        this.storage.setCapacity(this.energyCapacity);
        this.initialised = true;
    }

    public SubBlockMachine getSubBlock() {
        if (this.subBlock == null) {
            this.subBlock = (SubBlockMachine) ((BlockMachineMeta) this.getBlockType())
                    .getSubBlock(this.getBlockMetadata());
        }
        return this.subBlock;
    }

    @Override
    public void updateEntity() {

        if (!this.initialised) {
            this.init();
        }

        // this seems to be the important line
        this.receiveEnergyGC(null, this.generateWatts, false);

        super.updateEntity();

        if (!this.worldObj.isRemote) {
            // recharge the item?
            this.recharge(this.containingItems[0]);
            if (this.getDisabled(0)) {
                this.generateWatts = 0;
                this.generationBoost = -1;
            } else {
                if (this.generationBoost == -1 || this.ticks % 20 == 0) {
                    this.generationBoost = this.getEnvironmentalEnergyBoost();
                }

                this.generateWatts = Math.min(this.energyGeneration * this.generationBoost, MAX_GENERATE_WATTS);

            }

            if (this.disableCooldown > 0) {
                this.disableCooldown--;
            }
        }

        this.produce();
    }

    public float getEnvironmentalEnergyBoost() {
        float thermalLevel = 0.0F;

        if (this.worldObj.provider instanceof IGalacticraftWorldProvider) {
            thermalLevel = ((IGalacticraftWorldProvider) this.worldObj.provider).getThermalLevelModifier();
        }

        // e^(0.25*-x)
        // used a plotter to find a function which looks halfway good...
        final float result = (float) Math.exp(-0.25D * thermalLevel);
        return Math.min(result, 10.0F);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.storage.setCapacity(compound.getFloat("maxEnergy"));
        this.setDisabled(0, compound.getBoolean("disabled"));
        this.disableCooldown = compound.getInteger("disabledCooldown");

        final NBTTagList var2 = compound.getTagList("Items", 10);
        this.containingItems = new ItemStack[this.getSizeInventory()];

        for (int var3 = 0; var3 < var2.tagCount(); ++var3) {
            final NBTTagCompound var4 = var2.getCompoundTagAt(var3);
            final int var5 = var4.getByte("Slot") & 255;

            if (var5 < this.containingItems.length) {
                this.containingItems[var5] = ItemStack.loadItemStackFromNBT(var4);
            }
        }

        this.initialised = false;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setFloat("maxEnergy", this.getMaxEnergyStoredGC());
        compound.setInteger("disabledCooldown", this.disableCooldown);
        compound.setBoolean("disabled", this.getDisabled(0));

        final NBTTagList list = new NBTTagList();

        for (int var3 = 0; var3 < this.containingItems.length; ++var3) {
            if (this.containingItems[var3] != null) {
                final NBTTagCompound var4 = new NBTTagCompound();
                var4.setByte("Slot", (byte) var3);
                this.containingItems[var3].writeToNBT(var4);
                list.appendTag(var4);
            }
        }

        compound.setTag("Items", list);
    }

    @Override
    public EnumSet<ForgeDirection> getElectricalInputDirections() {
        return EnumSet.noneOf(ForgeDirection.class);
    }

    @Override
    public EnumSet<ForgeDirection> getElectricalOutputDirections() {
        // int metadata = this.getBlockMetadata() & 3;
        final int metadata = this.getRotationMeta(this.getBlockMetadata());

        return EnumSet.of(
                CoordHelper.rotateForgeDirection(ForgeDirection.EAST, metadata),
                CoordHelper.rotateForgeDirection(ForgeDirection.WEST, metadata),
                CoordHelper.rotateForgeDirection(ForgeDirection.NORTH, metadata),
                ForgeDirection.UNKNOWN);
    }

    public int getRotationMeta(final int meta) {
        return (meta & 12) >> 2;
    }

    @Override
    public ForgeDirection getElectricalOutputDirectionMain() {
        final int metadata = this.getRotationMeta(this.getBlockMetadata());

        return CoordHelper.rotateForgeDirection(ForgeDirection.EAST, metadata);
    }

    @Override
    public boolean canConnect(final ForgeDirection direction, final NetworkType type) {
        if (direction == null || ForgeDirection.UNKNOWN.equals(direction) || type != NetworkType.POWER) {
            return false;
        }

        return this.getElectricalOutputDirections().contains(direction);
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
        return new int[] { 0 };
    }

    @Override
    public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {
        return this.isItemValidForSlot(p_102007_1_, p_102007_2_);
    }

    @Override
    public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
        return p_102008_1_ == 0;
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
    public String getInventoryName() {
        return GCCoreUtil.translate("tile." + this.getSubBlock().getUnlocalizedName() + ".name");
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
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this
                && player.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == 0 && ItemElectricBase.isElectricItem(stack.getItem());
    }

    @Override
    public void setDisabled(int index, boolean disabled) {
        if (this.disableCooldown == 0) {
            this.disabled = disabled;
            this.disableCooldown = 20;
        }
    }

    @Override
    public boolean getDisabled(int index) {
        return this.disabled;
    }

}
