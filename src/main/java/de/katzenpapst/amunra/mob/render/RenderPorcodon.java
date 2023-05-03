package de.katzenpapst.amunra.mob.render;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.mob.model.ModelPorcodon;

public class RenderPorcodon extends RenderLiving {

    protected static final ResourceLocation pTextures = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/entity/porcodon/porcodon.png");

    public RenderPorcodon() {
        // model, shadowSize
        super(new ModelPorcodon(), 0.7F);
        // TODO Auto-generated constructor stub

        // wat
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    @Override
    protected ResourceLocation getEntityTexture(final Entity p_110775_1_) {
        return pTextures;
    }

}
