package de.katzenpapst.amunra.client.renderer;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import de.katzenpapst.amunra.tile.TileEntityMothershipEngineAbstract;

public class RenderMothershipJet extends TileEntitySpecialRenderer {

    protected ResourceLocation texture;
    protected final IModelCustom model;

    public RenderMothershipJet(final IModelCustom leModel, final ResourceLocation texture) {
        this.model = leModel;
        this.texture = texture;
    }

    public void renderMothershipEngine(final TileEntityMothershipEngineAbstract tile, final double x, final double y,
            final double z, final float partialTicks) {
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y, (float) z + 0.5F);

        float rotation = 0.0F;

        /*
         * 2 -> -Z 1 -> -X 3 -> +X 0 -> +Z
         */
        switch (tile.getRotationMeta()) {
            case 0:
                rotation = 0;// 180.0F;// -> Z
                break;
            case 1:
                rotation = 270.0F;// 90.0F;// -> -X
                break;
            case 2:
                rotation = 180.0F;// 0;// -> -Z
                break;
            case 3:
                rotation = 90.0F;// 270.0F;// -> X
                break;
        }

        GL11.glScalef(0.5F, 0.5F, 0.5F);
        GL11.glRotatef(rotation, 0, 1, 0);
        GL11.glTranslatef(0.0F, 1.0F, 1.0F);

        this.bindTexture(this.texture);
        this.model.renderAll();

        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void renderTileEntityAt(final TileEntity entity, final double x, final double y, final double z,
            final float partialTickTime) {
        this.renderMothershipEngine((TileEntityMothershipEngineAbstract) entity, x, y, z, partialTickTime);
        // micdoodle8.mods.galacticraft.planets.mars.client.render.tile.TileEntityCryogenicChamberRenderer
    }

}
