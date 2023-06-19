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

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/entity/sentry.png");
    private static final ResourceLocation TEXTURE_HIGHLIGHT = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/entity/sentry_highlight.png");

    public RenderSentry() {
        super(new ModelSentry(), 0.5F);
        this.setRenderPassModel(new ModelSentry()); // ?
    }

    @Override
    protected int shouldRenderPass(EntityLivingBase p_77032_1_, int p_77032_2_, float p_77032_3_) {
        if (p_77032_2_ != 0) {
            return -1;
        }
        this.bindTexture(TEXTURE_HIGHLIGHT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);

        GL11.glDepthMask(!p_77032_1_.isInvisible());

        final char c0 = 61680;
        final int j = c0 % 65536;
        final int k = c0 / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j / 1.0F, k / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        return 1;
    }

    @Override
    protected void preRenderCallback(EntityLivingBase p_77041_1_, float p_77041_2_) {
        GL11.glScalef(1.0F, 1.0F, 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
        return TEXTURE;
    }
}
