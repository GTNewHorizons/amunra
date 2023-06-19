package de.katzenpapst.amunra.mob.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.mob.RobotVillagerProfession;
import de.katzenpapst.amunra.mob.entity.EntityRobotVillager;
import de.katzenpapst.amunra.mob.model.ModelRobotVillager;

public class RenderRobotVillager extends RenderLiving {

    private static final ResourceLocation VILLAGER_TEXTURE = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/entity/robotvillager.png");

    protected ModelRobotVillager villagerModel;

    public RenderRobotVillager() {
        super(new ModelRobotVillager(0.0F), 0.5F);
        this.villagerModel = (ModelRobotVillager) this.mainModel;
    }

    protected int shouldVillagerRenderPass(final EntityRobotVillager par1EntityVillager, final int par2,
            final float par3) {
        return -1;
    }

    public void renderVillager(final EntityRobotVillager par1EntityVillager, final double par2, final double par4,
            final double par6, final float par8, final float par9) {
        super.doRender(par1EntityVillager, par2, par4, par6, par8, par9);
    }

    protected void renderVillagerEquipedItems(final EntityRobotVillager par1EntityVillager, final float par2) {
        super.renderEquippedItems(par1EntityVillager, par2);

        // try some stuff
        this.renderFrontPlate(par1EntityVillager);
    }

    private void renderFrontPlate(final EntityRobotVillager par1EntityVillager) {

        final RobotVillagerProfession prof = RobotVillagerProfession.getProfession(par1EntityVillager.getProfession());
        if (prof == null) {

            // something weird happened
            return;
        }

        GL11.glPushMatrix();
        this.bindTexture(prof.getIcon());
        TextureUtil.func_152777_a(false, false, 1.0F);
        final Tessellator tessellator = Tessellator.instance;

        final float scale = 0.45F;

        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glTranslatef(-scale / 2, 0.10F, -0.20F);

        GL11.glScalef(scale, scale, scale);

        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);

        tessellator.addVertexWithUV(0.0D, 0.0D, 0.0D, 0, 0);
        tessellator.addVertexWithUV(1.0D, 0.0D, 0.0D, 1, 0);
        tessellator.addVertexWithUV(1.0D, 1.0D, 0.0D, 1, 1);
        tessellator.addVertexWithUV(0.0D, 1.0D, 0.0D, 0, 1);
        tessellator.draw();

        GL11.glPopMatrix();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        TextureUtil.func_147945_b();
    }

    protected void preRenderVillager(final EntityRobotVillager par1EntityVillager, final float par2) {
        float f1 = 0.9375F;

        if (par1EntityVillager.getGrowingAge() < 0) {
            f1 = (float) (f1 * 0.5D);
            this.shadowSize = 0.25F;
        } else {
            this.shadowSize = 0.5F;
        }

        GL11.glScalef(f1, f1, f1);
    }

    @Override
    public void doRender(EntityLiving p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_,
            float p_76986_8_, float p_76986_9_) {
        this.renderVillager(
                (EntityRobotVillager) p_76986_1_,
                p_76986_2_,
                p_76986_4_,
                p_76986_6_,
                p_76986_8_,
                p_76986_9_);
    }

    @Override
    protected void preRenderCallback(EntityLivingBase p_77041_1_, float p_77041_2_) {
        this.preRenderVillager((EntityRobotVillager) p_77041_1_, p_77041_2_);
    }

    @Override
    protected int shouldRenderPass(EntityLivingBase p_77032_1_, int p_77032_2_, float p_77032_3_) {
        return this.shouldVillagerRenderPass((EntityRobotVillager) p_77032_1_, p_77032_2_, p_77032_3_);
    }

    @Override
    protected void renderEquippedItems(EntityLivingBase p_77029_1_, float p_77029_2_) {
        this.renderVillagerEquipedItems((EntityRobotVillager) p_77029_1_, p_77029_2_);
    }

    @Override
    public void doRender(EntityLivingBase p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_,
            float p_76986_8_, float p_76986_9_) {
        this.renderVillager(
                (EntityRobotVillager) p_76986_1_,
                p_76986_2_,
                p_76986_4_,
                p_76986_6_,
                p_76986_8_,
                p_76986_9_);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
        return RenderRobotVillager.VILLAGER_TEXTURE;
    }

    @Override
    public void doRender(Entity p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_,
            float p_76986_9_) {
        this.renderVillager(
                (EntityRobotVillager) p_76986_1_,
                p_76986_2_,
                p_76986_4_,
                p_76986_6_,
                p_76986_8_,
                p_76986_9_);
    }
}
