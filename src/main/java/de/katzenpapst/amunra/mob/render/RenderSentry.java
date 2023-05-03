package de.katzenpapst.amunra.mob.render;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.mob.model.ModelSentry;

public class RenderSentry extends RenderLiving {

    private static final ResourceLocation textures = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/entity/sentry.png");
    private static final ResourceLocation textures_highlight = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/entity/sentry_highlight.png");
    // private static final ResourceLocation ghastShootingTextures = new
    // ResourceLocation("textures/entity/ghast/ghast_shooting.png");

    public RenderSentry() {
        super(new ModelSentry(), 0.5F);
        this.setRenderPassModel(new ModelSentry()); // ?
    }

    @Override
    protected int shouldRenderPass(final EntityLivingBase p_77032_1_, final int p_77032_2_, final float p_77032_3_) {
        if (p_77032_2_ != 0) {
            return -1;
        }
        this.bindTexture(textures_highlight);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);

        if (p_77032_1_.isInvisible()) {
            GL11.glDepthMask(false);
        } else {
            GL11.glDepthMask(true);
        }

        final char c0 = 61680;
        final int j = c0 % 65536;
        final int k = c0 / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j / 1.0F, k / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        return 1;
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    @Override
    protected void preRenderCallback(final EntityLivingBase entity, final float partialTickTime) {
        // this.preRenderCallback((EntitySentry)entity, partialTickTime);
        GL11.glScalef(1.0F, 1.0F, 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    @Override
    protected ResourceLocation getEntityTexture(final Entity entity) {
        return textures;
    }
}
