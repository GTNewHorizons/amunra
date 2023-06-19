package de.katzenpapst.amunra.client.renderer;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import de.katzenpapst.amunra.client.renderer.model.ModelHydroponics;
import de.katzenpapst.amunra.tile.TileEntityHydroponics;
import micdoodle8.mods.galacticraft.api.transmission.tile.IOxygenReceiver;
import micdoodle8.mods.galacticraft.core.util.OxygenUtil;

public class RenderHydroponics extends TileEntitySpecialRenderer {

    private final ModelHydroponics model = new ModelHydroponics();

    @Override
    public void renderTileEntityAt(TileEntity p_147500_1_, double p_147500_2_, double p_147500_4_, double p_147500_6_,
            float p_147500_8_) {
        if (p_147500_1_ instanceof TileEntityHydroponics tile) {
            GL11.glPushMatrix();
            GL11.glTranslated(p_147500_2_, p_147500_4_, p_147500_6_);

            final TileEntity[] connections = OxygenUtil.getAdjacentOxygenConnections(tile);

            // TODO: check if the code in this For is correct
            for (int i = 0; i < connections.length; i++) {
                final TileEntity cur = connections[i];
                final ForgeDirection direction = ForgeDirection.values()[i];

                if (cur instanceof IOxygenReceiver
                        && ((IOxygenReceiver) cur).getOxygenRequest(direction.getOpposite()) <= 0) {
                    connections[i] = null;
                }
            }

            final boolean hasNorth = connections[ForgeDirection.NORTH.ordinal()] != null;
            final boolean hasSouth = connections[ForgeDirection.SOUTH.ordinal()] != null;
            final boolean hasWest = connections[ForgeDirection.WEST.ordinal()] != null;
            final boolean hasEast = connections[ForgeDirection.EAST.ordinal()] != null;

            final Tessellator tess = Tessellator.instance;
            this.model.render(tess, tile.getPlantGrowthStatus(), hasNorth, hasSouth, hasWest, hasEast);

            GL11.glPopMatrix();
        }

    }

}
