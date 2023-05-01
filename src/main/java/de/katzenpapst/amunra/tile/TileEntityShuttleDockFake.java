package de.katzenpapst.amunra.tile;

import java.util.HashSet;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.FluidStack;

import micdoodle8.mods.galacticraft.api.entity.ICargoEntity;
import micdoodle8.mods.galacticraft.api.entity.IDockable;
import micdoodle8.mods.galacticraft.api.entity.IFuelable;
import micdoodle8.mods.galacticraft.api.tile.IFuelDock;
import micdoodle8.mods.galacticraft.api.tile.ILandingPadAttachable;
import micdoodle8.mods.galacticraft.core.tile.TileEntityMulti;

public class TileEntityShuttleDockFake extends TileEntityMulti implements IFuelable, IFuelDock, ICargoEntity {

    public TileEntityShuttleDockFake() {}

    @Override
    public EnumCargoLoadingState addCargo(ItemStack stack, boolean doAdd) {

        TileEntity main = getMainBlockTile();
        if (main instanceof ICargoEntity) {
            return ((ICargoEntity) main).addCargo(stack, doAdd);
        }
        return EnumCargoLoadingState.NOTARGET;
    }

    @Override
    public RemovalResult removeCargo(boolean doRemove) {
        TileEntity main = getMainBlockTile();
        if (main instanceof ICargoEntity) {
            return ((ICargoEntity) main).removeCargo(doRemove);
        }
        return new RemovalResult(EnumCargoLoadingState.NOTARGET, null);
    }

    @Override
    public HashSet<ILandingPadAttachable> getConnectedTiles() {
        TileEntity main = getMainBlockTile();
        if (main instanceof IFuelDock) {
            return ((IFuelDock) main).getConnectedTiles();
        }

        return new HashSet<ILandingPadAttachable>();
    }

    @Override
    public boolean isBlockAttachable(IBlockAccess world, int x, int y, int z) {
        TileEntity main = getMainBlockTile();
        if (main instanceof IFuelDock) {
            return ((IFuelDock) main).isBlockAttachable(world, x, y, z);
        }
        return false;
    }

    @Override
    public IDockable getDockedEntity() {
        TileEntity main = getMainBlockTile();
        if (main instanceof IFuelDock) {
            return ((IFuelDock) main).getDockedEntity();
        }
        return null;
    }

    @Override
    public void dockEntity(IDockable entity) {
        TileEntity main = getMainBlockTile();
        if (main instanceof IFuelDock) {
            ((IFuelDock) main).dockEntity(entity);
        }
    }

    @Override
    public int addFuel(FluidStack fluid, boolean doDrain) {
        TileEntity main = getMainBlockTile();
        if (main instanceof IFuelable) {
            return ((IFuelable) main).addFuel(fluid, doDrain);
        }
        return 0;
    }

    @Override
    public FluidStack removeFuel(int amount) {
        TileEntity main = getMainBlockTile();
        if (main instanceof IFuelable) {
            return ((IFuelable) main).removeFuel(amount);
        }
        return null;
    }

}
