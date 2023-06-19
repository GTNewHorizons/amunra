package de.katzenpapst.amunra.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.mothership.fueldisplay.MothershipFuelDisplay;
import de.katzenpapst.amunra.mothership.fueldisplay.MothershipFuelDisplayFluid;
import de.katzenpapst.amunra.mothership.fueldisplay.MothershipFuelRequirements;
import de.katzenpapst.amunra.proxy.ARSidedProxy.ParticleType;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

/**
 * This is supposed to be used for any jet blocks
 *
 * @author katzenpapst
 *
 */
public class TileEntityMothershipEngineJet extends TileEntityMothershipEngineAbstract {

    protected MothershipFuelDisplay fuelType = null;
    public static Fluid jetFuel;

    public TileEntityMothershipEngineJet() {
        this.boosterBlock = ARBlocks.blockMsEngineRocketBooster;
        this.containingItems = new ItemStack[1];

        this.fuel = jetFuel;
        this.fuelType = new MothershipFuelDisplayFluid(this.fuel);
    }

    @Override
    public boolean shouldUseEnergy() {
        return false;
    }

    @Override
    public void beginTransit(final long duration) {
        final MothershipFuelRequirements reqs = this.getFuelRequirements(duration);
        final int fuelReq = reqs.get(this.fuelType);
        this.fuelTank.drain(fuelReq, true);
        super.beginTransit(duration);
    }

    @Override
    protected boolean isItemFuel(final ItemStack itemstack) {
        FluidStack containedFluid = null;
        if (itemstack.getItem() instanceof IFluidContainerItem itemContainer) {
            containedFluid = itemContainer.getFluid(itemstack);
        }
        if (containedFluid == null) {
            containedFluid = FluidContainerRegistry.getFluidForFilledItem(itemstack);
        }
        if (containedFluid != null) {
            return this.fuel == containedFluid.getFluid();
        }
        return false;
    }

    /**
     * Calculates tank capacity based on the boosters
     */
    @Override
    protected int getTankCapacity() {
        return 10000 * this.numBoosters;
    }

    @Override
    protected void startSound() {
        super.startSound();
        AmunRa.proxy.playTileEntitySound(this, new ResourceLocation(AmunRa.TEXTUREPREFIX + "mothership.engine.rocket"));
    }

    @Override
    protected void spawnParticles() {
        final Vector3 particleStart = this.getExhaustPosition(1);
        final Vector3 particleDirection = this.getExhaustDirection().scale(5);

        AmunRa.proxy
                .spawnParticles(ParticleType.PT_MOTHERSHIP_JET_FLAME, this.worldObj, particleStart, particleDirection);
        AmunRa.proxy
                .spawnParticles(ParticleType.PT_MOTHERSHIP_JET_FLAME, this.worldObj, particleStart, particleDirection);
        AmunRa.proxy
                .spawnParticles(ParticleType.PT_MOTHERSHIP_JET_FLAME, this.worldObj, particleStart, particleDirection);
        AmunRa.proxy
                .spawnParticles(ParticleType.PT_MOTHERSHIP_JET_FLAME, this.worldObj, particleStart, particleDirection);

    }

    @Override
    public boolean canFill(final ForgeDirection from, final Fluid fluid) {
        if (this.fuel == fluid) {
            return super.canFill(from, fluid);
        }
        return false;
    }

    @Override
    public String getInventoryName() {
        return GCCoreUtil.translate("tile.mothershipEngineRocket.name");
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == 0 && stack != null) {
            return this.isItemFuel(stack);
        }
        /*
         * FluidStack containedFluid = FluidContainerRegistry.getFluidForFilledItem(itemstack);
         * if(containedFluid.getFluid() == fuel) { return true; }
         */
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
        return new int[] { 0 };
    }

    @Override
    public double getThrust() {
        return this.getNumBoosters() * 2000000.0D;
    }

    /**
     * This should return how much fuel units are consumed per AU travelled, in millibuckets
     */
    public float getFuelUsagePerTick() {
        return 2.0F;
    }

    @Override
    public MothershipFuelRequirements getFuelRequirements(final long duration) {
        final int totalFuelNeed = (int) Math
                .ceil(this.getFuelUsagePerTick() * duration * AmunRa.config.mothershipFuelFactor);

        final MothershipFuelRequirements result = new MothershipFuelRequirements();

        result.add(this.fuelType, totalFuelNeed);

        return result;
    }

    @Override
    public boolean canRunForDuration(final long duration) {
        final MothershipFuelRequirements reqs = this.getFuelRequirements(duration);

        return reqs.get(this.fuelType) <= this.fuelTank.getFluidAmount();
    }

}
