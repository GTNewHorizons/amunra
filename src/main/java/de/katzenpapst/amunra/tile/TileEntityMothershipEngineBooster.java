package de.katzenpapst.amunra.tile;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.common.Optional.Interface;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.vec.Vector3int;
import micdoodle8.mods.galacticraft.api.transmission.NetworkType;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseUniversalElectrical;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.planets.asteroids.AsteroidsModule;

/**
 * This is supposed to be an universal booster TileEntity, used by all booster blocks
 *
 * @author katzenpapst
 *
 */
@Interface(iface = "cofh.api.energy.IEnergyReceiver", modid = "CoFHAPI|energy")
public class TileEntityMothershipEngineBooster extends TileBaseUniversalElectrical
        implements IFluidHandler, ISidedInventory, IInventory, IEnergyReceiver {

    public static ResourceLocation topFallback = new ResourceLocation(
            AsteroidsModule.ASSET_PREFIX,
            "textures/blocks/machine.png");
    public static ResourceLocation sideFallback = new ResourceLocation(
            AsteroidsModule.ASSET_PREFIX,
            "textures/blocks/machine_side.png");

    protected final String assetPrefix = AmunRa.ASSETPREFIX;
    protected final String assetPath = "textures/blocks/";

    protected boolean masterPresent = false;
    protected int masterX;
    protected int masterY;
    protected int masterZ;

    protected Class<? extends TileEntityMothershipEngineAbstract> masterType = TileEntityMothershipEngineJet.class;

    public boolean isValidMaster(final TileEntity tile) {
        return this.masterType.isInstance(tile);
    }

    public void reset() {
        this.masterPresent = false;
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    public void setMaster(final int x, final int y, final int z) {
        this.masterX = x;
        this.masterY = y;
        this.masterZ = z;
        this.masterPresent = true;
    }

    public int getMasterX() {
        return this.masterX;
    }

    public int getMasterY() {
        return this.masterY;
    }

    public int getMasterZ() {
        return this.masterZ;
    }

    public void clearMaster() {
        this.masterPresent = false;
    }

    public boolean isMaster(final int x, final int y, final int z) {
        return this.masterPresent && x == this.masterX && y == this.masterY && z == this.masterZ;
    }

    public Vector3int getMasterPosition() {
        return new Vector3int(this.masterX, this.masterY, this.masterZ);
    }

    public boolean hasMaster() {
        // meh
        return this.getMasterTile() != null;
    }

    /**
     * Reset and update the master, if I have any
     */
    public void updateMaster(final boolean rightNow) {
        if (!this.masterPresent) return;

        final TileEntity masterTile = this.worldObj.getTileEntity(this.masterX, this.masterY, this.masterZ);
        if (!(masterTile instanceof TileEntityMothershipEngineAbstract jetTile)
                || !jetTile.isPartOfMultiBlock(this.xCoord, this.yCoord, this.zCoord)) {
            this.reset();
            return;
        }

        if (rightNow) {
            jetTile.updateMultiblock();
        } else {
            jetTile.scheduleUpdate();
        }
    }

    /**
     * Using the master coordinates, get a position where the next booster could be
     */
    public Vector3int getPossibleNextBooster() {
        if (!this.hasMaster()) {
            return null;
        }
        if (this.xCoord == this.masterX) {
            if (this.zCoord < this.masterZ) {
                return new Vector3int(this.xCoord, this.yCoord, this.zCoord - 1);
            }
            if (this.zCoord > this.masterZ) {
                return new Vector3int(this.xCoord, this.yCoord, this.zCoord + 1);
            } else {
                return null;
            }
        }
        if (this.zCoord == this.masterZ) {
            if (this.xCoord < this.masterX) {
                return new Vector3int(this.xCoord - 1, this.yCoord, this.zCoord);
            }
            if (this.xCoord > this.masterX) {
                return new Vector3int(this.xCoord + 1, this.yCoord, this.zCoord);
            } else {}
        }
        return null;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.masterPresent = compound.getBoolean("hasMaster");
        this.masterX = compound.getInteger("masterX");
        this.masterY = compound.getInteger("masterY");
        this.masterZ = compound.getInteger("masterZ");
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("hasMaster", this.masterPresent);
        compound.setInteger("masterX", this.masterX);
        compound.setInteger("masterY", this.masterY);
        compound.setInteger("masterZ", this.masterZ);
    }

    public TileEntityMothershipEngineAbstract getMasterTile() {
        if (!this.masterPresent) {
            return null;
        }
        final TileEntity tile = this.worldObj.getTileEntity(this.masterX, this.masterY, this.masterZ);
        if (!(tile instanceof TileEntityMothershipEngineAbstract tileEngine)) {
            // oops
            this.masterPresent = false;
            return null;
        }
        return tileEngine;
    }

    @Override
    public int getSizeInventory() {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return 0;
        }
        return tile.getSizeInventory();
    }

    @Override
    public ItemStack getStackInSlot(int slotIn) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return null;
        }
        return tile.getStackInSlot(slotIn);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return null;
        }
        return tile.decrStackSize(index, count);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return null;
        }
        return tile.getStackInSlotOnClosing(index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return;
        }
        tile.setInventorySlotContents(index, stack);
    }

    @Override
    public String getInventoryName() {
        // I'm not sure if it's even needed to do this, but...
        return GCCoreUtil.translate("tile.mothership.rocketJetEngine.name");
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return 0;
        }
        return tile.getInventoryStackLimit();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return false;
        }
        // I think it's better to calculate this here
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this
                && player.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return false;
        }
        return tile.isItemValidForSlot(index, stack);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return new int[] {};
        }
        return tile.getAccessibleSlotsFromSide(p_94128_1_);
    }

    @Override
    public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return false;
        }
        return tile.canInsertItem(p_102007_1_, p_102007_2_, p_102007_3_);
    }

    @Override
    public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return false;
        }
        return tile.canExtractItem(p_102008_1_, p_102008_2_, p_102008_3_);
    }

    @Override
    public int fill(final ForgeDirection from, final FluidStack resource, final boolean doFill) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return 0;
        }
        return tile.fill(from, resource, doFill);
    }

    @Override
    public FluidStack drain(final ForgeDirection from, final FluidStack resource, final boolean doDrain) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return null;
        }
        return tile.drain(from, resource, doDrain);
    }

    @Override
    public FluidStack drain(final ForgeDirection from, final int maxDrain, final boolean doDrain) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return null;
        }
        return tile.drain(from, maxDrain, doDrain);
    }

    @Override
    public boolean canFill(final ForgeDirection from, final Fluid fluid) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return false;
        }
        return tile.canFill(from, fluid);
    }

    @Override
    public boolean canDrain(final ForgeDirection from, final Fluid fluid) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return false;
        }
        return tile.canDrain(from, fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(final ForgeDirection from) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return null;
        }
        return tile.getTankInfo(from);
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

    public ResourceLocation getBlockIconFromSide(final int side) {

        // fallback
        if (side > 1) {
            return sideFallback;
        }
        return topFallback;

    }

    @Override
    public EnumSet<ForgeDirection> getElectricalInputDirections() {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return EnumSet.noneOf(ForgeDirection.class);
        }
        // EnumSet.
        return tile.getElectricalInputDirections();
    }

    @Override
    public boolean canConnect(final ForgeDirection direction, final NetworkType type) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return false;
        }
        return tile.canConnect(direction, type);
    }

    // Five methods for compatibility with basic electricity
    @Override
    public float receiveElectricity(final ForgeDirection from, final float receive, final int tier,
            final boolean doReceive) {
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return 0F;
        }
        return tile.receiveElectricity(from, receive, tier, doReceive);
    }

    @Override
    public float provideElectricity(final ForgeDirection from, final float request, final boolean doProvide) {
        return 0.F;// do not provide
    }

    @Override
    public float getRequest(final ForgeDirection direction) {
        // not sure what this does
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return 0F;
        }
        return tile.getRequest(direction);
    }

    @Override
    public float getProvide(final ForgeDirection direction) {
        return 0;
    }

    @Override
    public int getTierGC() {
        return this.tierGC;
    }

    @Override
    public void setTierGC(final int newTier) {
        this.tierGC = newTier;
    }

    @Override
    public int receiveEnergy(final ForgeDirection from, final int maxReceive, final boolean simulate) {
        // forward this to the master, too
        final TileEntityMothershipEngineAbstract tile = this.getMasterTile();
        if (tile == null) {
            return 0;
        }
        return tile.receiveEnergy(from, maxReceive, simulate);
    }
}
