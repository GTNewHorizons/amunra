package de.katzenpapst.amunra.mob.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelBug extends ModelBase {

    ModelRenderer body;
    ModelRenderer head;
    ModelRenderer tail1;
    ModelRenderer lefthand2;
    ModelRenderer lefthand1;
    ModelRenderer leftarm;
    ModelRenderer righthand2;
    ModelRenderer righthand1;
    ModelRenderer rightarm;
    ModelRenderer legLeft3;
    ModelRenderer legLeft2;
    ModelRenderer legLeft1;
    ModelRenderer legRight3;
    ModelRenderer legRight2;
    ModelRenderer legRight1;

    public ModelBug() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-7F, -4F, -7F, 14, 8, 14);
        this.body.setRotationPoint(0F, 0F, 0F);
        this.body.setTextureSize(64, 64);
        this.body.mirror = true;
        this.setRotation(this.body, 0F, 0F, 0F);

        this.head = new ModelRenderer(this, 0, 22);
        this.head.addBox(-3F, -3F, -6F, 6, 6, 6);
        this.head.setRotationPoint(0F, 0F, -7F);
        this.head.setTextureSize(64, 64);
        this.head.mirror = true;
        this.setRotation(this.head, 0F, 0F, 0F);

        this.tail1 = new ModelRenderer(this, 32, 22);
        this.tail1.addBox(-4F, -3F, 0F, 8, 4, 3);
        this.tail1.setRotationPoint(0F, 1F, 7F);
        this.tail1.setTextureSize(64, 64);
        this.tail1.mirror = true;
        this.setRotation(this.tail1, 0F, 0F, 0F);

        // left claw
        this.lefthand2 = new ModelRenderer(this, 0, 0);
        this.lefthand2.mirror = true;
        this.lefthand2.addBox(0F, -1F, -4F, 1, 2, 4);
        this.lefthand2.setRotationPoint(0F, 8F, 0F);
        this.lefthand2.setTextureSize(64, 64);
        this.lefthand2.mirror = true;
        this.setRotation(this.lefthand2, 1.570796F, 0F, 0F);
        this.lefthand2.mirror = false;

        this.lefthand1 = new ModelRenderer(this, 0, 0);
        this.lefthand1.addBox(-1F, -1F, -4F, 1, 2, 4);
        this.lefthand1.setRotationPoint(0F, 8F, 0F);
        this.lefthand1.setTextureSize(64, 64);
        this.lefthand1.mirror = true;
        this.setRotation(this.lefthand1, 1.570796F, 0F, 0F);

        this.leftarm = new ModelRenderer(this, 24, 22);
        this.leftarm.addBox(-1F, 0F, -1F, 2, 8, 2);
        this.leftarm.setRotationPoint(-5F, 0F, -7F);
        this.leftarm.setTextureSize(64, 64);
        this.leftarm.mirror = true;
        // setRotation(leftarm, -1.570796F, 0F, 0F);
        this.setRotation(this.leftarm, -1.570796F, 0.5F, 0F);
        this.leftarm.addChild(this.lefthand1);
        this.leftarm.addChild(this.lefthand2);

        // right claw
        this.righthand2 = new ModelRenderer(this, 0, 0);
        this.righthand2.addBox(-1F, -1F, -4F, 1, 2, 4);
        this.righthand2.setRotationPoint(0F, 8F, 0F);
        this.righthand2.setTextureSize(64, 64);
        this.righthand2.mirror = true;
        this.setRotation(this.righthand2, 1.570796F, 0F, 0F);

        this.righthand1 = new ModelRenderer(this, 0, 0);
        this.righthand1.mirror = true;
        this.righthand1.addBox(0F, -1F, -4F, 1, 2, 4);
        this.righthand1.setRotationPoint(0F, 8F, 0F);
        this.righthand1.setTextureSize(64, 64);
        this.righthand1.mirror = true;
        this.setRotation(this.righthand1, 1.570796F, 0F, 0F);
        this.righthand1.mirror = false;

        this.rightarm = new ModelRenderer(this, 24, 22);
        this.rightarm.mirror = true;
        this.rightarm.addBox(-1F, 0F, -1F, 2, 8, 2);
        this.rightarm.setRotationPoint(5F, 0F, -7F);
        this.rightarm.setTextureSize(64, 64);
        this.rightarm.mirror = true;
        // setRotation(rightarm, -1.570796F, 0F, 0F);
        this.setRotation(this.rightarm, -1.570796F, -0.5F, 0F);
        this.rightarm.mirror = false;
        this.rightarm.addChild(this.righthand1);
        this.rightarm.addChild(this.righthand2);

        // left legs
        this.legLeft3 = new ModelRenderer(this, 42, 0);
        this.legLeft3.addBox(-1F, 0F, -1F, 2, 10, 2);
        this.legLeft3.setRotationPoint(-7F, 1F, 4F);
        this.legLeft3.setTextureSize(64, 64);
        this.legLeft3.mirror = true;
        // setRotation(legLeft3, 0F, 0F, 1.134464F);
        this.setRotation(this.legLeft3, 0.3F, 0F, 1.134464F);

        this.legLeft2 = new ModelRenderer(this, 42, 0);
        this.legLeft2.addBox(-1F, 0F, -1F, 2, 10, 2);
        this.legLeft2.setRotationPoint(-7F, 1F, 0F);
        this.legLeft2.setTextureSize(64, 64);
        this.legLeft2.mirror = true;
        this.setRotation(this.legLeft2, 0F, 0F, 1.134464F);

        this.legLeft1 = new ModelRenderer(this, 42, 0);
        this.legLeft1.addBox(-1F, 0F, -1F, 2, 10, 2);
        this.legLeft1.setRotationPoint(-7F, 1F, -4F);
        this.legLeft1.setTextureSize(64, 64);
        this.legLeft1.mirror = true;
        // setRotation(legLeft1, 0F, 0F, 1.134464F);
        this.setRotation(this.legLeft1, -0.3F, 0F, 1.134464F);

        // right legs
        this.legRight3 = new ModelRenderer(this, 42, 0);
        this.legRight3.mirror = true;
        this.legRight3.addBox(-1F, 0F, -1F, 2, 10, 2);
        this.legRight3.setRotationPoint(7F, 1F, 4F);
        this.legRight3.setTextureSize(64, 64);
        this.legRight3.mirror = true;
        // setRotation(legRight3, 0F, 0F, -1.134464F);
        this.setRotation(this.legRight3, 0.3F, 0F, -1.134464F);
        this.legRight3.mirror = false;

        this.legRight2 = new ModelRenderer(this, 42, 0);
        this.legRight2.mirror = true;
        this.legRight2.addBox(-1F, 0F, -1F, 2, 10, 2);
        this.legRight2.setRotationPoint(7F, 1F, 0F);
        this.legRight2.setTextureSize(64, 64);
        this.legRight2.mirror = true;
        this.setRotation(this.legRight2, 0F, 0F, -1.134464F);
        this.legRight2.mirror = false;

        this.legRight1 = new ModelRenderer(this, 42, 0);
        this.legRight1.mirror = true;
        this.legRight1.addBox(-1F, 0F, -1F, 2, 10, 2);
        this.legRight1.setRotationPoint(7F, 1F, -4F);
        this.legRight1.setTextureSize(64, 64);
        this.legRight1.mirror = true;
        // setRotation(legRight1, 0F, 0F, -1.134464F);
        this.setRotation(this.legRight1, -0.3F, 0F, -1.134464F);
        this.legRight1.mirror = false;
    }

    private void setRotation(final ModelRenderer model, final float x, final float y, final float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void render(Entity p_78088_1_, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_,
            float p_78088_6_, float p_78088_7_) {
        super.render(p_78088_1_, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_);
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, p_78088_1_);
        this.body.render(p_78088_7_);
        this.head.render(p_78088_7_);
        this.tail1.render(p_78088_7_);
        this.leftarm.render(p_78088_7_);
        this.rightarm.render(p_78088_7_);
        this.legLeft3.render(p_78088_7_);
        this.legLeft2.render(p_78088_7_);
        this.legLeft1.render(p_78088_7_);
        this.legRight3.render(p_78088_7_);
        this.legRight2.render(p_78088_7_);
        this.legRight1.render(p_78088_7_);
    }

    @Override
    // public void setRotationAngles(float time, float walkSpeed, float appendageRotation, float rotationYaw, float
    // rotationPitch, float scale, Entity entity)
    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_,
            float p_78087_5_, float p_78087_6_, Entity p_78087_7_) {
        this.head.rotateAngleY = p_78087_4_ / (180F / (float) Math.PI);
        this.head.rotateAngleX = p_78087_5_ / (180F / (float) Math.PI);

        // reset to default
        this.setRotation(this.legLeft1, -0.3F, 0F, 1.134464F);
        this.setRotation(this.legLeft2, 0F, 0F, 1.134464F);
        this.setRotation(this.legLeft3, 0.3F, 0F, 1.134464F);

        this.setRotation(this.legRight1, -0.3F, 0F, -1.134464F);
        this.setRotation(this.legRight2, 0F, 0F, -1.134464F);
        this.setRotation(this.legRight3, 0.3F, 0F, -1.134464F);

        this.setRotation(this.leftarm, -1.570796F, 0.5F, 0F);
        this.setRotation(this.rightarm, -1.570796F, -0.5F, 0F);

        this.setRotation(this.leftarm, -1.570796F, 0.5F, 0F);
        // setRotation(lefthand1, -0.3F, 0F, 1.134464F);
        this.setRotation(this.lefthand1, 1.570796F, 0F, 0F);
        this.setRotation(this.lefthand2, 1.570796F, 0F, 0F);

        this.setRotation(this.rightarm, -1.570796F, -0.5F, 0F);
        this.setRotation(this.righthand1, 1.570796F, 0F, 0F);
        this.setRotation(this.righthand2, 1.570796F, 0F, 0F);

        // now rotate

        final float leg1Y = -(MathHelper.cos(p_78087_1_ * 0.6662F * 2.0F + 0.0F) * 0.4F) * p_78087_2_;
        final float leg3Y = -(MathHelper.cos(p_78087_1_ * 0.6662F * 2.0F + (float) Math.PI) * 0.4F) * p_78087_2_;
        // float leg6Y = -(MathHelper.cos(time * 0.6662F * 2.0F + ((float)Math.PI / 2F)) * 0.4F) * walkSpeed;
        final float leg7Y = -(MathHelper.cos(p_78087_1_ * 0.6662F * 2.0F + (float) Math.PI * 3F / 2F) * 0.4F)
                * p_78087_2_;

        final float leg1Z = Math.abs(MathHelper.sin(p_78087_1_ * 0.6662F + 0.0F) * 0.4F) * p_78087_2_;
        final float leg3Z = Math.abs(MathHelper.sin(p_78087_1_ * 0.6662F + (float) Math.PI) * 0.4F) * p_78087_2_;
        // float leg5Z = Math.abs(MathHelper.sin(time * 0.6662F + ((float)Math.PI / 2F)) * 0.4F) * walkSpeed;
        final float leg7Z = Math.abs(MathHelper.sin(p_78087_1_ * 0.6662F + (float) Math.PI * 3F / 2F) * 0.4F)
                * p_78087_2_;

        this.legLeft1.rotateAngleY += leg1Y;
        this.legRight1.rotateAngleY -= leg1Y;
        this.legLeft1.rotateAngleX += leg1Z;
        this.legRight1.rotateAngleX -= leg1Z;

        this.legLeft2.rotateAngleY += leg3Y;
        this.legRight2.rotateAngleY -= leg3Y;
        this.legLeft2.rotateAngleX += leg3Z;
        this.legRight2.rotateAngleX -= leg3Z;

        this.legLeft3.rotateAngleY += leg7Y;
        this.legRight3.rotateAngleY -= leg7Y;
        this.legLeft3.rotateAngleX += leg7Z;
        this.legRight3.rotateAngleX -= leg7Z;

        this.rightarm.rotateAngleZ += MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
        this.rightarm.rotateAngleX += MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
        this.righthand1.rotateAngleZ += MathHelper.cos(p_78087_3_ * 0.5F) * 0.5F - 0.5F;
        this.righthand2.rotateAngleZ -= MathHelper.cos(p_78087_3_ * 0.5F) * 0.5F - 0.5F;

        this.leftarm.rotateAngleZ -= MathHelper.cos(p_78087_3_ * 0.09F) * 0.05F + 0.05F;
        this.leftarm.rotateAngleX -= MathHelper.sin(p_78087_3_ * 0.067F) * 0.05F;
        this.lefthand1.rotateAngleZ -= MathHelper.cos(p_78087_3_ * 0.5F) * 0.5F - 0.5F;
        this.lefthand2.rotateAngleZ += MathHelper.cos(p_78087_3_ * 0.5F) * 0.5F - 0.5F;
        /*
         * this.spiderLeg1.rotateAngleY += leg1Y; this.spiderLeg2.rotateAngleY += -leg1Y; this.spiderLeg3.rotateAngleY
         * += leg3Y; this.spiderLeg4.rotateAngleY += -leg3Y; this.spiderLeg5.rotateAngleY += leg6Y;
         * this.spiderLeg6.rotateAngleY += -leg6Y; this.spiderLeg7.rotateAngleY += leg7Y; this.spiderLeg8.rotateAngleY
         * += -leg7Y; this.spiderLeg1.rotateAngleZ += leg1Z; this.spiderLeg2.rotateAngleZ += -leg1Z;
         * this.spiderLeg3.rotateAngleZ += leg3Z; this.spiderLeg4.rotateAngleZ += -leg3Z; this.spiderLeg5.rotateAngleZ
         * += leg5Z; this.spiderLeg6.rotateAngleZ += -leg5Z; this.spiderLeg7.rotateAngleZ += leg7Z;
         * this.spiderLeg8.rotateAngleZ += -leg7Z;
         */

        // this.legLeft1.rotateAngleZ = f / (180F / (float)Math.PI);

        super.setRotationAngles(p_78087_1_, p_78087_2_, p_78087_3_, p_78087_4_, p_78087_5_, p_78087_6_, p_78087_7_);
    }
}
