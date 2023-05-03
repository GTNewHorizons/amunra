package de.katzenpapst.amunra.block;

import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldProviderSpace;
import micdoodle8.mods.galacticraft.api.world.IAtmosphericGas;

public class MethaneGrass extends SubBlockGrass {

    // blockIcon = 0 = top
    @SideOnly(Side.CLIENT)
    protected IIcon blockIconSide;
    @SideOnly(Side.CLIENT)
    protected IIcon blockIconBottom;

    public MethaneGrass(final String name) {
        super(name, "amunra:methanegrass", "amunra:methanegrassside", "amunra:methanedirt");
    }

    /**
     * Return the block what this should revert to if the conditions are bad
     * 
     * @return
     */
    @Override
    public BlockMetaPair getDirtBlock() {
        return ARBlocks.blockMethaneDirt;
    }

    /**
     * Return true if the current conditions are good for this grasses survival, usually light stuff The Multiblock will
     * replace it with this.getDirtBlock() Will also be called for dirt neighbors of this in order to check if this
     * *could* live there
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @return
     */
    @Override
    public boolean canLiveHere(final World world, final int x, final int y, final int z) {
        // now this grass can only live in a methane atmosphere
        return world.provider instanceof WorldProviderSpace && super.canLiveHere(world, x, y, z)
                && ((WorldProviderSpace) world.provider).isGasPresent(IAtmosphericGas.METHANE);
        // !OxygenUtil.testContactWithBreathableAir(world, world.getBlock(x, y+1, z), x, y, z, 0);
    }

    /**
     * Return true if the conditions are right in order to spread to blocks returned by this.getDirtBlock() no call of
     * canLiveHere is needed
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @return
     */
    @Override
    public boolean canSpread(final World world, final int x, final int y, final int z) {
        return world.getBlockLightValue(x, y + 1, z) >= 9;
    }

}
