package de.katzenpapst.amunra.block;

import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldProviderSpace;
import micdoodle8.mods.galacticraft.api.world.IAtmosphericGas;

public class MethaneGrass extends SubBlockGrass {

    @SideOnly(Side.CLIENT)
    protected IIcon blockIconSide, blockIconBottom;

    public MethaneGrass(final String name) {
        super(name, "amunra:methanegrass", "amunra:methanegrassside", "amunra:methanedirt");
    }

    @Override
    public BlockMetaPair getDirtBlock() {
        return ARBlocks.blockMethaneDirt;
    }

    @Override
    public boolean canLiveHere(final World world, final int x, final int y, final int z) {
        // now this grass can only live in a methane atmosphere
        return world.provider instanceof WorldProviderSpace spaceProvider && super.canLiveHere(world, x, y, z)
                && spaceProvider.isGasPresent(IAtmosphericGas.METHANE);
    }

    @Override
    public boolean canSpread(final World world, final int x, final int y, final int z) {
        return world.getBlockLightValue(x, y + 1, z) >= 9;
    }

}
