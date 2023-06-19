package de.katzenpapst.amunra.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.client.BlockRenderHelper;
import de.katzenpapst.amunra.tile.TileEntityMothershipEngineAbstract;
import de.katzenpapst.amunra.tile.TileEntityMothershipEngineBooster;

public class RenderMothershipBooster extends TileEntitySpecialRenderer {

    private ResourceLocation texture = new ResourceLocation(AmunRa.ASSETPREFIX, "textures/blocks/jet-base.png");

    public RenderMothershipBooster(final ResourceLocation texture) {
        this.texture = texture;
    }

    protected void renderMSBooster(final TileEntityMothershipEngineBooster entity, final double x, final double y,
            final double z, final float partialTickTime) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y, (float) z);

        // I hope this works
        final Tessellator tess = Tessellator.instance;
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // HACK
        Minecraft.getMinecraft().renderEngine.bindTexture(this.texture);

        //
        final TileEntityMothershipEngineAbstract masterTile = entity.getMasterTile();
        if (masterTile == null) {
            BlockRenderHelper.renderFaceYNeg(tess, 0, 0.5, 0.25, 0.75, false);
            BlockRenderHelper.renderFaceYPos(tess, 0, 0.5, 0.25, 0.75, false);

            BlockRenderHelper.renderFaceZNeg(tess, 0, 0, 0.25, 0.25);
            BlockRenderHelper.renderFaceZPos(tess, 0, 0, 0.25, 0.25);
            BlockRenderHelper.renderFaceXNeg(tess, 0, 0, 0.25, 0.25);
            BlockRenderHelper.renderFaceXPos(tess, 0, 0, 0.25, 0.25);
        } else {
            int nrInMultiblock = 0;
            boolean isFirst = false;
            boolean isLast = false;
            final boolean doRotate = entity.getMasterZ() == entity.zCoord;

            if (entity.getMasterX() == entity.xCoord) {
                // we are on the same x
                nrInMultiblock = entity.getMasterZ() - entity.zCoord;

            } else {
                // same z
                nrInMultiblock = entity.getMasterX() - entity.xCoord;
            }
            if (nrInMultiblock == 1 || -nrInMultiblock == masterTile.getNumBoosters()) {
                isFirst = true;
            }
            if (nrInMultiblock == -1 || nrInMultiblock == masterTile.getNumBoosters()) {
                isLast = true;
            }
            double usageOffset = 0;
            if (masterTile.isInUse()) {
                usageOffset = 0.25;
            }
            if (isFirst && isLast) {

                BlockRenderHelper.renderFaceYNeg(tess, 0, 0.5, 0.25, 0.75, false);
                BlockRenderHelper.renderFaceYPos(tess, 0, 0.5, 0.25, 0.75, false);

                if (doRotate) {
                    // side
                    BlockRenderHelper.renderFaceZNeg(tess, 0, 0 + usageOffset, 0.25, 0.25 + usageOffset);
                    BlockRenderHelper.renderFaceZPos(tess, 0, 0 + usageOffset, 0.25, 0.25 + usageOffset);

                    // front
                    BlockRenderHelper.renderFaceXNeg(tess, 0, 0.75, 0.25, 1);
                    BlockRenderHelper.renderFaceXPos(tess, 0, 0.75, 0.25, 1);

                } else {
                    BlockRenderHelper.renderFaceZNeg(tess, 0, 0.75, 0.25, 1);
                    BlockRenderHelper.renderFaceZPos(tess, 0, 0.75, 0.25, 1);

                    BlockRenderHelper.renderFaceXNeg(tess, 0, 0 + usageOffset, 0.25, 0.25 + usageOffset);
                    BlockRenderHelper.renderFaceXPos(tess, 0, 0 + usageOffset, 0.25, 0.25 + usageOffset);
                }

            } else if (isFirst) {
                BlockRenderHelper.renderFaceYNeg(tess, 0.25, 0.5, 0.5, 0.75, doRotate);
                BlockRenderHelper.renderFaceYPos(tess, 0.25, 0.5, 0.5, 0.75, doRotate);

                if (doRotate) {
                    BlockRenderHelper.renderFaceZNeg(tess, 0.25, 0 + usageOffset, 0.5, 0.25 + usageOffset);
                    BlockRenderHelper.renderFaceZPos(tess, 0.25, 0 + usageOffset, 0.5, 0.25 + usageOffset);

                    BlockRenderHelper.renderFaceXNeg(tess, 0, 0.75, 0.25, 1);
                    BlockRenderHelper.renderFaceXPos(tess, 0, 0.75, 0.25, 1);
                } else {
                    BlockRenderHelper.renderFaceXNeg(tess, 0.25, 0 + usageOffset, 0.5, 0.25 + usageOffset);
                    BlockRenderHelper.renderFaceXPos(tess, 0.25, 0 + usageOffset, 0.5, 0.25 + usageOffset);

                    BlockRenderHelper.renderFaceZNeg(tess, 0, 0.75, 0.25, 1);
                    BlockRenderHelper.renderFaceZPos(tess, 0, 0.75, 0.25, 1);
                }

            } else if (isLast) {
                BlockRenderHelper.renderFaceYNeg(tess, 0.75, 0.5, 1.0, 0.75, doRotate);
                BlockRenderHelper.renderFaceYPos(tess, 0.75, 0.5, 1.0, 0.75, doRotate);

                if (doRotate) {
                    BlockRenderHelper.renderFaceZNeg(tess, 0.75, 0 + usageOffset, 1.0, 0.25 + usageOffset);
                    BlockRenderHelper.renderFaceZPos(tess, 0.75, 0 + usageOffset, 1.0, 0.25 + usageOffset);

                    BlockRenderHelper.renderFaceXNeg(tess, 0, 0.75, 0.25, 1);
                    BlockRenderHelper.renderFaceXPos(tess, 0, 0.75, 0.25, 1);
                } else {
                    BlockRenderHelper.renderFaceXNeg(tess, 0.75, 0 + usageOffset, 1.0, 0.25 + usageOffset);
                    BlockRenderHelper.renderFaceXPos(tess, 0.75, 0 + usageOffset, 1.0, 0.25 + usageOffset);

                    BlockRenderHelper.renderFaceZNeg(tess, 0, 0.75, 0.25, 1);
                    BlockRenderHelper.renderFaceZPos(tess, 0, 0.75, 0.25, 1);
                }

            } else {
                BlockRenderHelper.renderFaceYNeg(tess, 0.5, 0.5, 0.75, 0.75, doRotate);
                BlockRenderHelper.renderFaceYPos(tess, 0.5, 0.5, 0.75, 0.75, doRotate);

                if (doRotate) {
                    BlockRenderHelper.renderFaceZNeg(tess, 0.5, 0 + usageOffset, 0.75, 0.25 + usageOffset);
                    BlockRenderHelper.renderFaceZPos(tess, 0.5, 0 + usageOffset, 0.75, 0.25 + usageOffset);
                } else {
                    BlockRenderHelper.renderFaceXNeg(tess, 0.5, 0 + usageOffset, 0.75, 0.25 + usageOffset);
                    BlockRenderHelper.renderFaceXPos(tess, 0.5, 0 + usageOffset, 0.75, 0.25 + usageOffset);
                }

            }
        }

        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileEntity p_147500_1_, double p_147500_2_, double p_147500_4_, double p_147500_6_,
            float p_147500_8_) {
        this.renderMSBooster(
                (TileEntityMothershipEngineBooster) p_147500_1_,
                p_147500_2_,
                p_147500_4_,
                p_147500_6_,
                p_147500_8_);
    }

}
