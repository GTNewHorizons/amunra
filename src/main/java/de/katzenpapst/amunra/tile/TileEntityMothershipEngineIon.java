package de.katzenpapst.amunra.tile;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.helper.CoordHelper;
import de.katzenpapst.amunra.mothership.fueldisplay.MothershipFuelDisplay;
import de.katzenpapst.amunra.mothership.fueldisplay.MothershipFuelDisplayEnergy;
import de.katzenpapst.amunra.mothership.fueldisplay.MothershipFuelDisplayFluid;
import de.katzenpapst.amunra.mothership.fueldisplay.MothershipFuelRequirements;
import de.katzenpapst.amunra.proxy.ARSidedProxy.ParticleType;
import micdoodle8.mods.galacticraft.api.transmission.NetworkType;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class TileEntityMothershipEngineIon extends TileEntityMothershipEngineAbstract {

    protected MothershipFuelDisplay fuelType = null;
    protected MothershipFuelDisplay fuelTypeEnergy = null;
    public static Fluid coolant;

    public TileEntityMothershipEngineIon() {
        this.boosterBlock = ARBlocks.blockMsEngineIonBooster;
        this.containingItems = new ItemStack[2];
        this.fuel = coolant;
        // AsteroidsItems.canisterLN2

        this.fuelType = new MothershipFuelDisplayFluid(this.fuel);
        this.fuelTypeEnergy = MothershipFuelDisplayEnergy.getInstance();

        // AsteroidsModule
    }

    @Override
    protected void startSound() {
        super.startSound();
        AmunRa.proxy
                .playTileEntitySound(this, new ResourceLocation(GalacticraftCore.TEXTURE_PREFIX + "entity.astrominer"));
    }

    @Override
    protected void spawnParticles() {

        final Vector3 particleStart = this.getExhaustPosition(1.8);
        final Vector3 particleDirection = this.getExhaustDirection().scale(5);

        AmunRa.proxy
                .spawnParticles(ParticleType.PT_MOTHERSHIP_ION_FLAME, this.worldObj, particleStart, particleDirection);
        AmunRa.proxy
                .spawnParticles(ParticleType.PT_MOTHERSHIP_ION_FLAME, this.worldObj, particleStart, particleDirection);
        AmunRa.proxy
                .spawnParticles(ParticleType.PT_MOTHERSHIP_ION_FLAME, this.worldObj, particleStart, particleDirection);
        AmunRa.proxy
                .spawnParticles(ParticleType.PT_MOTHERSHIP_ION_FLAME, this.worldObj, particleStart, particleDirection);

    }

    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
        // TODO fix
        return new int[] { 0, 1 };

    }

    @Override
    public void beginTransit(final long duration) {

        final MothershipFuelRequirements reqs = this.getFuelRequirements(duration);

        final int energyReq = reqs.get(this.fuelTypeEnergy);
        final int fuelReq = reqs.get(this.fuelType);

        this.storage.extractEnergyGCnoMax(energyReq, false);
        this.fuelTank.drain(fuelReq, true);

        super.beginTransit(duration);
    }

    @Override
    public String getInventoryName() {
        return GCCoreUtil.translate("tile.mothershipEngineIon.name");
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (stack == null) return false;

        switch (index) {
            case 0 -> {
                final FluidStack containedFluid = FluidContainerRegistry.getFluidForFilledItem(stack);
                if (containedFluid != null && containedFluid.getFluid() == this.fuel) {
                    return true;
                }
            }
            case 1 -> {
                return ItemElectricBase.isElectricItem(stack.getItem());
            }
        }
        return false;
    }

    public float getFuelUsagePerTick() {
        return 1.0F;
    }

    public float getEnergyUsagePerTick() {
        return 10.0F;
    }

    @Override
    public boolean shouldUseEnergy() {
        return false;
    }

    @Override
    public double getThrust() {
        return this.getNumBoosters() * 25000000.0D;
    }

    @Override
    protected int getTankCapacity() {
        return 2000 * this.numBoosters;
    }

    protected float getEnergyCapacity() {
        return STANDARD_CAPACITY * this.numBoosters;
    }

    @Override
    public boolean canFill(final ForgeDirection from, final Fluid fluid) {

        // ARItems
        // GCItems
        // other stuff?
        if (this.fuel != fluid && !FluidRegistry.getFluidName(fluid).equals(this.fuel.getName())) {
            return false;
        }

        return super.canFill(from, fluid);
    }

    @Override
    protected boolean isItemFuel(final ItemStack itemstack) {
        final FluidStack containedFluid = FluidContainerRegistry.getFluidForFilledItem(itemstack);
        return containedFluid != null && containedFluid.getFluid() == this.fuel;
    }

    @Override
    public int getRotationMeta(final int meta) {
        return (meta & 12) >> 2;
    }

    @Override
    public ForgeDirection getElectricInputDirection() {

        final int metadata = this.getRotationMeta(this.getBlockMetadata());

        return CoordHelper.rotateForgeDirection(ForgeDirection.SOUTH, metadata);

    }

    @Override
    public EnumSet<ForgeDirection> getElectricalInputDirections() {
        // EnumSet.
        return EnumSet.allOf(ForgeDirection.class);
    }

    @Override
    public boolean canConnect(final ForgeDirection direction, final NetworkType type) {
        return direction != null && !ForgeDirection.UNKNOWN.equals(direction) && type == NetworkType.POWER;
    }

    @Override
    public ItemStack getBatteryInSlot() {
        return this.containingItems[1];
    }

    @Override
    public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
        return p_102008_1_ == 0 || p_102008_1_ == 1;
    }

    @Override
    public void slowDischarge() {
        // don't
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        final float capacity = compound.getFloat("energyCapacity");
        this.storage.setCapacity(capacity);
        super.readFromNBT(compound);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setFloat("energyCapacity", this.storage.getCapacityGC());
    }

    @Override
    protected void createMultiblockInternal(final boolean notifyClient) {
        super.createMultiblockInternal(notifyClient);
        this.storage.setCapacity(this.getEnergyCapacity());
    }

    @Override
    public MothershipFuelRequirements getFuelRequirements(final long duration) {
        final int totalFuelNeed = (int) Math
                .ceil(this.getFuelUsagePerTick() * duration * AmunRa.config.mothershipFuelFactor);

        final float totalEnergyNeed = this.getEnergyUsagePerTick() * duration * AmunRa.config.mothershipFuelFactor;

        final MothershipFuelRequirements result = new MothershipFuelRequirements();

        result.add(this.fuelType, totalFuelNeed);

        result.add(this.fuelTypeEnergy, (int) totalEnergyNeed);

        return result;
    }

    @Override
    public boolean canRunForDuration(final long duration) {
        final MothershipFuelRequirements reqs = this.getFuelRequirements(duration);

        final int fuelNeeded = reqs.get(this.fuelType);
        final int powerNeeded = reqs.get(this.fuelTypeEnergy);

        return this.storage.getEnergyStoredGC() >= powerNeeded && this.fuelTank.getFluidAmount() > fuelNeeded;
    }

}
