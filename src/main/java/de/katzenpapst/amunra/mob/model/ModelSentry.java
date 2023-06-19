package de.katzenpapst.amunra.mob.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

import org.lwjgl.opengl.GL11;

public class ModelSentry extends ModelBase {

    ModelRenderer body;

    ModelRenderer leftarm;
    ModelRenderer rightarm;
    ModelRenderer toparm;
    ModelRenderer bottomarm;

    public ModelSentry() {
        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-8F, -8F, -8F, 16, 16, 16);
        this.body.setRotationPoint(0F, 8F, 0F);
        this.body.setTextureSize(64, 32);
        this.body.mirror = true;

        final float yOffset = -8F;

        this.leftarm = new ModelRenderer(this, 0, 0);
        this.leftarm.addBox(-1F, 0F, -1F, 2, 12, 2);
        this.leftarm.setRotationPoint(-9F, 8F + yOffset, -4F);
        this.leftarm.setTextureSize(64, 32);
        this.leftarm.mirror = true;
        this.setRotation(this.leftarm, 1.570796F, 0F, 0F);
        this.rightarm = new ModelRenderer(this, 0, 0);
        this.rightarm.addBox(-1F, 0F, -1F, 2, 12, 2);
        this.rightarm.setRotationPoint(9F, 8F + yOffset, -4F);
        this.rightarm.setTextureSize(64, 32);
        this.rightarm.mirror = true;
        this.setRotation(this.rightarm, 1.570796F, 0F, 0F);
        this.toparm = new ModelRenderer(this, 0, 0);
        this.toparm.addBox(-1F, 0F, -1F, 2, 12, 2);
        this.toparm.setRotationPoint(0F, -1F + yOffset, -4F);
        this.toparm.setTextureSize(64, 32);
        this.toparm.mirror = true;
        this.setRotation(this.toparm, 1.570796F, 0F, 0F);
        this.bottomarm = new ModelRenderer(this, 0, 0);
        this.bottomarm.addBox(-1F, 0F, -1F, 2, 12, 2);
        this.bottomarm.setRotationPoint(0F, 17F + yOffset, -4F);
        this.bottomarm.setTextureSize(64, 32);
        this.bottomarm.mirror = true;
        this.setRotation(this.bottomarm, 1.570796F, 0F, 0F);

        this.body.addChild(this.leftarm);
        this.body.addChild(this.rightarm);
        this.body.addChild(this.toparm);
        this.body.addChild(this.bottomarm);
    }

    private void setRotation(final ModelRenderer model, final float x, final float y, final float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_,
            float p_78087_5_, float p_78087_6_, Entity p_78087_7_) {
        this.body.rotateAngleY = p_78087_4_ / (180F / (float) Math.PI);
        this.body.rotateAngleX = p_78087_5_ / (180F / (float) Math.PI);

        this.rightarm.rotateAngleY = 0.2F * MathHelper.sin(p_78087_3_ * 0.3F) + 0.4F;
        this.leftarm.rotateAngleY = -(0.2F * MathHelper.sin(p_78087_3_ * 0.3F) + 0.4F);
        this.toparm.rotateAngleX = (float) (0.2 * MathHelper.sin(p_78087_3_ * 0.3F) + 0.4 + Math.PI / 2.0);
        this.bottomarm.rotateAngleX = -(float) (0.2 * MathHelper.sin(p_78087_3_ * 0.3F) + 0.4 - Math.PI / 2.0);

    }

    @Override
    public void render(Entity p_78088_1_, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_,
            float p_78088_6_, float p_78088_7_) {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, p_78088_1_);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.6F, 0.0F);
        this.body.render(p_78088_7_);
        /*
         * leftarm.render(someConstant); rightarm.render(someConstant); toparm.render(someConstant);
         * bottomarm.render(someConstant);
         */
        GL11.glPopMatrix();
    }

}
