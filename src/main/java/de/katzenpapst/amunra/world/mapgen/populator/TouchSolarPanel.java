package de.katzenpapst.amunra.world.mapgen.populator;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import micdoodle8.mods.galacticraft.api.vector.BlockVec3;
import micdoodle8.mods.galacticraft.core.tile.TileEntitySolar;

public class TouchSolarPanel extends AbstractPopulator {

    /**
     * This is just here to make the solar panels generate their fakeblocks
     */
    public TouchSolarPanel(final int x, final int y, final int z) {
        super(x, y, z);// this doesn't need any further stuff
    }

    @Override
    public boolean populate(final World world) {
        final TileEntity tile = world.getTileEntity(this.x, this.y, this.z);
        if (tile instanceof final TileEntitySolar tileSolar) {
            tileSolar.onCreate(new BlockVec3(this.x, this.y, this.z));
            return true;
        }
        return false;
    }

}
