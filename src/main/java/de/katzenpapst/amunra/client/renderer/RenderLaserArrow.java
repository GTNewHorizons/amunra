package de.katzenpapst.amunra.client.renderer;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import de.katzenpapst.amunra.entity.EntityBaseLaserArrow;

public class RenderLaserArrow extends Render {

    public RenderLaserArrow() {
        this.shadowSize = 0.1F;
    }

    protected ResourceLocation func_110779_a(final EntityBaseLaserArrow par1EntityArrow) {
        return par1EntityArrow.getTexture();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
        return this.func_110779_a((EntityBaseLaserArrow) p_110775_1_);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    @Override
    public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_,
            float p_76986_9_) {
        this.bindEntityTexture(p_76986_1_);

        /*
         * final float var24 = entity.rotationPitch; final float var24b = entity.rotationYaw; GL11.glTranslatef((float)
         * x, (float) y, (float) z); GL11.glScalef(0.3F, 0.3F, 0.3F); GL11.glRotatef(var24b, 1.0F, 0.0F, 0.0F);
         * GL11.glRotatef(var24, 0.0F, 0.0F, 1.0F);
         */

        GL11.glPushMatrix();
        GL11.glTranslated(p_76986_2_, p_76986_4_, p_76986_6_);
        GL11.glRotatef(
                p_76986_1_.prevRotationYaw + (p_76986_1_.rotationYaw - p_76986_1_.prevRotationYaw) * p_76986_9_ - 90.0F,
                0.0F,
                1.0F,
                0.0F);
        GL11.glRotatef(
                p_76986_1_.prevRotationPitch + (p_76986_1_.rotationPitch - p_76986_1_.prevRotationPitch) * p_76986_9_,
                0.0F,
                0.0F,
                1.0F);

        final Tessellator tessellator = Tessellator.instance;

        final byte b0 = 0;
        final float f2 = 0.0F;
        final float f3 = 0.5F;
        final float f4 = (0 + b0 * 10) / 32.0F;
        final float f5 = (5 + b0 * 10) / 32.0F;
        final float f6 = 0.0F;
        final float f7 = 0.15625F;
        final float f8 = (5 + b0 * 10) / 32.0F;
        final float f9 = (10 + b0 * 10) / 32.0F;
        final float f10 = 0.05625F;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
        GL11.glDepthMask(false);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680.0F, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GL11.glScalef(f10, f10, f10);
        GL11.glTranslatef(-4.0F, 0.0F, 0.0F);
        GL11.glNormal3f(f10, 0.0F, 0.0F);
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(-7.0D, -2.0D, -2.0D, f6, f8);
        tessellator.addVertexWithUV(-7.0D, -2.0D, 2.0D, f7, f8);
        tessellator.addVertexWithUV(-7.0D, 2.0D, 2.0D, f7, f9);
        tessellator.addVertexWithUV(-7.0D, 2.0D, -2.0D, f6, f9);
        tessellator.draw();
        GL11.glNormal3f(-f10, 0.0F, 0.0F);
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(-7.0D, 2.0D, -2.0D, f6, f8);
        tessellator.addVertexWithUV(-7.0D, 2.0D, 2.0D, f7, f8);
        tessellator.addVertexWithUV(-7.0D, -2.0D, 2.0D, f7, f9);
        tessellator.addVertexWithUV(-7.0D, -2.0D, -2.0D, f6, f9);
        tessellator.draw();

        for (int i = 0; i < 4; ++i) {
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glNormal3f(0.0F, 0.0F, f10);
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(-8.0D, -2.0D, 0.0D, f2, f4);
            tessellator.addVertexWithUV(8.0D, -2.0D, 0.0D, f3, f4);
            tessellator.addVertexWithUV(8.0D, 2.0D, 0.0D, f3, f5);
            tessellator.addVertexWithUV(-8.0D, 2.0D, 0.0D, f2, f5);
            tessellator.draw();
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

}
