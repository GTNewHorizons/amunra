package de.katzenpapst.amunra.block;

import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldProviderSpace;

public class VacuumGrass extends SubBlockGrass {

    @SideOnly(Side.CLIENT)
    protected IIcon blockIconSide, blockIconBottom;

    public VacuumGrass(final String name, final String textureTop, final String textureSide,
            final String textureBottom) {
        super(name, textureTop, textureSide, textureBottom);
    }

    @Override
    public BlockMetaPair getDirtBlock() {
        return ARBlocks.blockMethaneDirt;
    }

    @Override
    public boolean canLiveHere(final World world, final int x, final int y, final int z) {
        return world.provider instanceof WorldProviderSpace spaceProvider && spaceProvider.getCelestialBody() != null
                && spaceProvider.getCelestialBody().atmosphere.isEmpty()
                && super.canLiveHere(world, x, y, z);

    }
}
