package de.katzenpapst.amunra.client.renderer.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.client.BlockRenderHelper;

public class ModelHydroponics {

    private final ResourceLocation texture = new ResourceLocation(AmunRa.ASSETPREFIX, "textures/blocks/hydroponics2.png");

    public ModelHydroponics() {}

    public void render(final Tessellator tess, final float growthStatus, final boolean connectNorth, final boolean connectSouth,
            final boolean connectWest, final boolean connectEast) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        Minecraft.getMinecraft().renderEngine.bindTexture(this.texture);

        // block render stuff
        BlockRenderHelper.renderFaceYNeg(tess, 0, 0, 0.25, 0.5, false);
        BlockRenderHelper.renderFaceYPos(tess, 0.5, 0, 0.75, 0.5, false);

        // 0.75, 0, 1.0, 0.5 -> connected
        // 0.25, 0, 0.5, 0.5 -> not connected
        //

        if (connectWest) {
            BlockRenderHelper.renderFaceXNeg(tess, 0.75, 0, 1.0, 0.5);
            this.renderConnector(tess);
        } else {
            BlockRenderHelper.renderFaceXNeg(tess, 0.25, 0, 0.5, 0.5);
        }

        if (connectEast) {
            BlockRenderHelper.renderFaceXPos(tess, 0.75, 0, 1.0, 0.5);
            GL11.glPushMatrix();
            GL11.glRotatef(180.0F, 0, 1.0F, 0);
            GL11.glTranslatef(-1.0F, 0.0F, -1.0F);

            this.renderConnector(tess);
            GL11.glPopMatrix();
        } else {
            BlockRenderHelper.renderFaceXPos(tess, 0.25, 0, 0.5, 0.5);
        }

        if (connectNorth) {
            BlockRenderHelper.renderFaceZNeg(tess, 0.75, 0, 1.0, 0.5);
            GL11.glPushMatrix();
            GL11.glRotatef(270.0F, 0, 1.0F, 0);
            GL11.glTranslatef(0.0F, 0.0F, -1.0F);

            this.renderConnector(tess);
            GL11.glPopMatrix();
        } else {
            BlockRenderHelper.renderFaceZNeg(tess, 0.25, 0, 0.5, 0.5);
        }

        if (connectSouth) {
            BlockRenderHelper.renderFaceZPos(tess, 0.75, 0, 1.0, 0.5);
            GL11.glPushMatrix();
            GL11.glRotatef(90.0F, 0, 1.0F, 0);
            GL11.glTranslatef(-1.0F, 0.0F, 0.0F);

            this.renderConnector(tess);
            GL11.glPopMatrix();
        } else {
            BlockRenderHelper.renderFaceZPos(tess, 0.25, 0, 0.5, 0.5);
        }

        // now do the grass

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, -0.655F, 0.0F);
        tess.startDrawingQuads();
        tess.setNormal(0.0F, 0.8F, 0.0F);
        tess.addVertexWithUV(1, 1, 1, 0, 1.0);// 10
        tess.addVertexWithUV(1, 1, 0, 0.25, 1.0);// 00
        tess.addVertexWithUV(0, 1, 0, 0.25, 0.5);// 01
        tess.addVertexWithUV(0, 1, 1, 0, 0.5);// 11
        tess.draw();

        // now try doing the wheat
        // tess.setBrightness(te.getBlockType().getMixedBrightnessForBlock(te.getWorldObj(), te.xCoord, te.yCoord,
        // te.zCoord));
        if (growthStatus >= 0) {

            tess.setColorOpaque_F(1.0F, 1.0F, 1.0F);

            final double factor = 0.6;
            GL11.glScaled(factor, factor, factor);

            final int wheatState = (int) (growthStatus * 7);

            this.renderWheat(wheatState, 0.5 / factor - 0.5, 1.0 / factor, 0.5 / factor - 0.5);
        }
        GL11.glPopMatrix();

    }

    protected void renderConnector(final Tessellator tess) {
        // try doing the box
        // +Y
        tess.startDrawingQuads();
        tess.setNormal(0.0F, 0.8F, 0.0F);
        tess.addVertexWithUV(0.1, 0.75, 0.75, 0.25, 0.765625);// 10
        tess.addVertexWithUV(0.1, 0.75, 0.25, 0.375, 0.765625);// 00
        tess.addVertexWithUV(0, 0.75, 0.25, 0.375, 0.71875);// 01
        tess.addVertexWithUV(0, 0.75, 0.75, 0.25, 0.71875);// 11
        tess.draw();

        // +Z
        tess.startDrawingQuads();
        tess.setNormal(0.8F, 0.0F, 0.0F);
        tess.addVertexWithUV(0, 0.75, 0.75, 0.3984375, 0.5);
        tess.addVertexWithUV(0, 0.31, 0.75, 0.3984375, 0.71875);
        tess.addVertexWithUV(0.1, 0.31, 0.75, 0.375, 0.71875);
        tess.addVertexWithUV(0.1, 0.75, 0.75, 0.375, 0.5);
        tess.draw();

        // -Z
        tess.startDrawingQuads();
        tess.setNormal(-0.8F, 0.0F, 0.0F);

        tess.addVertexWithUV(0, 0.75, 0.25, 0.3984375, 0.5);
        tess.addVertexWithUV(0.1, 0.75, 0.25, 0.375, 0.5);
        tess.addVertexWithUV(0.1, 0.31, 0.25, 0.375, 0.71875);
        tess.addVertexWithUV(0, 0.31, 0.25, 0.3984375, 0.71875);
        tess.draw();

        // +X
        tess.startDrawingQuads();
        tess.setNormal(0.0F, 0.0F, 0.8F);
        tess.addVertexWithUV(0.1, 0.31, 0.75, 0.25, 0.71875);
        tess.addVertexWithUV(0.1, 0.31, 0.25, 0.375, 0.71875);
        tess.addVertexWithUV(0.1, 0.75, 0.25, 0.375, 0.5);
        tess.addVertexWithUV(0.1, 0.75, 0.75, 0.25, 0.5);
        tess.draw();

        // box END
    }

    protected void renderWheat(final int meta, final double x, final double y, final double z) {
        final Tessellator tessellator = Tessellator.instance;
        // IIcon iicon = this.getBlockIconFromSideAndMetadata(p_147795_1_, 0, p_147795_2_);
        final IIcon iicon = Blocks.wheat.getIcon(0, meta);

        final ResourceLocation resourcelocation = Minecraft.getMinecraft().renderEngine.getResourceLocation(0);
        Minecraft.getMinecraft().renderEngine.bindTexture(resourcelocation);

        final double d3 = iicon.getMinU();
        final double d4 = iicon.getMinV();
        final double d5 = iicon.getMaxU();
        final double d6 = iicon.getMaxV();
        double d7 = x + 0.5D - 0.25D;
        double d8 = x + 0.5D + 0.25D;
        double d9 = z + 0.5D - 0.5D;
        double d10 = z + 0.5D + 0.5D;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(d7, y + 1.0D, d9, d3, d4);
        tessellator.addVertexWithUV(d7, y + 0.0D, d9, d3, d6);
        tessellator.addVertexWithUV(d7, y + 0.0D, d10, d5, d6);
        tessellator.addVertexWithUV(d7, y + 1.0D, d10, d5, d4);
        tessellator.addVertexWithUV(d7, y + 1.0D, d10, d3, d4);
        tessellator.addVertexWithUV(d7, y + 0.0D, d10, d3, d6);
        tessellator.addVertexWithUV(d7, y + 0.0D, d9, d5, d6);
        tessellator.addVertexWithUV(d7, y + 1.0D, d9, d5, d4);
        tessellator.addVertexWithUV(d8, y + 1.0D, d10, d3, d4);
        tessellator.addVertexWithUV(d8, y + 0.0D, d10, d3, d6);
        tessellator.addVertexWithUV(d8, y + 0.0D, d9, d5, d6);
        tessellator.addVertexWithUV(d8, y + 1.0D, d9, d5, d4);
        tessellator.addVertexWithUV(d8, y + 1.0D, d9, d3, d4);
        tessellator.addVertexWithUV(d8, y + 0.0D, d9, d3, d6);
        tessellator.addVertexWithUV(d8, y + 0.0D, d10, d5, d6);
        tessellator.addVertexWithUV(d8, y + 1.0D, d10, d5, d4);
        d7 = x + 0.5D - 0.5D;
        d8 = x + 0.5D + 0.5D;
        d9 = z + 0.5D - 0.25D;
        d10 = z + 0.5D + 0.25D;
        tessellator.addVertexWithUV(d7, y + 1.0D, d9, d3, d4);
        tessellator.addVertexWithUV(d7, y + 0.0D, d9, d3, d6);
        tessellator.addVertexWithUV(d8, y + 0.0D, d9, d5, d6);
        tessellator.addVertexWithUV(d8, y + 1.0D, d9, d5, d4);
        tessellator.addVertexWithUV(d8, y + 1.0D, d9, d3, d4);
        tessellator.addVertexWithUV(d8, y + 0.0D, d9, d3, d6);
        tessellator.addVertexWithUV(d7, y + 0.0D, d9, d5, d6);
        tessellator.addVertexWithUV(d7, y + 1.0D, d9, d5, d4);
        tessellator.addVertexWithUV(d8, y + 1.0D, d10, d3, d4);
        tessellator.addVertexWithUV(d8, y + 0.0D, d10, d3, d6);
        tessellator.addVertexWithUV(d7, y + 0.0D, d10, d5, d6);
        tessellator.addVertexWithUV(d7, y + 1.0D, d10, d5, d4);
        tessellator.addVertexWithUV(d7, y + 1.0D, d10, d3, d4);
        tessellator.addVertexWithUV(d7, y + 0.0D, d10, d3, d6);
        tessellator.addVertexWithUV(d8, y + 0.0D, d10, d5, d6);
        tessellator.addVertexWithUV(d8, y + 1.0D, d10, d5, d4);
        tessellator.draw();
    }

}
