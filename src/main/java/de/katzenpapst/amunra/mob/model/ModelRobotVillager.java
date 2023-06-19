package de.katzenpapst.amunra.mob.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelRobotVillager extends ModelVillager {

    ModelRenderer rightArm;
    ModelRenderer leftArm;

    float ticks = 0;
    int curVisorPhase = 0;

    public ModelRobotVillager(final float par1) {
        this(par1, 0.0F, 64, 64);
    }

    public ModelRobotVillager(final float scaleOrSo, final float par2, final int textureX, final int textureY) {
        super(scaleOrSo, par2, 0, 0);

        this.villagerHead = new ModelRenderer(this).setTextureSize(textureX, textureY);
        this.villagerHead.setRotationPoint(0.0F, -1.5F + par2, 0.0F);
        this.villagerHead.setTextureOffset(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, scaleOrSo + 0.001F);
        // I'll abuse it as the neck
        this.villagerNose = new ModelRenderer(this).setTextureSize(textureX, textureY);
        this.villagerNose.setRotationPoint(0.0F, par2 - 1.0F, 0.0F);
        this.villagerNose.setTextureOffset(24, 0).addBox(-1.0F, -1.0F, -1.0F, 2, 4, 2, scaleOrSo + 0.002F);

        this.villagerBody = new ModelRenderer(this).setTextureSize(textureX, textureY);
        this.villagerBody.setRotationPoint(0.0F, 0.0F + par2, 0.0F);
        this.villagerBody.setTextureOffset(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8, 12, 6, scaleOrSo + 0.003F);
        this.villagerBody.setTextureOffset(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8, 18, 6, scaleOrSo + 0.5F + 0.004F);

        this.villagerBody.addChild(this.villagerNose);

        /*
         * this.villagerArms = new ModelRenderer(this).setTextureSize(textureX, textureY);
         * this.villagerArms.setRotationPoint(0.0F, 0.0F + par2 + 2.0F, 0.0F); this.villagerArms.setTextureOffset(44,
         * 22).addBox(-8.0F, -2.0F, -2.0F, 4, 8, 4, scaleOrSo + 0.005F); this.villagerArms.setTextureOffset(44,
         * 22).addBox(4.0F, -2.0F, -2.0F, 4, 8, 4, scaleOrSo + 0.0001F); this.villagerArms.setTextureOffset(40,
         * 38).addBox(-4.0F, 2.0F, -2.0F, 8, 4, 4, scaleOrSo + 0.0004F);
         */
        this.rightVillagerLeg = new ModelRenderer(this, 0, 22).setTextureSize(textureX, textureY);
        this.rightVillagerLeg.setRotationPoint(-2.0F, 12.0F + par2, 0.0F);
        this.rightVillagerLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, scaleOrSo + 0.0006F);
        this.leftVillagerLeg = new ModelRenderer(this, 0, 22).setTextureSize(textureX, textureY);
        this.leftVillagerLeg.mirror = true;
        this.leftVillagerLeg.setRotationPoint(2.0F, 12.0F + par2, 0.0F);
        this.leftVillagerLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, scaleOrSo + 0.0002F);

        this.rightArm = this.generateArm(scaleOrSo, par2, textureX, textureY, false);
        this.leftArm = this.generateArm(scaleOrSo, par2, textureX, textureY, true);

    }

    private ModelRenderer generateArm(final float scaleOrSo, final float par2, final int textureX, final int textureY,
            final boolean mirror) {

        float factor = 1.0F;
        if (mirror) {
            factor = -1.0F;
        }

        final ModelRenderer arm = new ModelRenderer(this).setTextureSize(textureX, textureY);

        arm.setRotationPoint(-5.5F * factor, 0.5F + par2, 0.0F);

        arm.setTextureOffset(40, 38).addBox(-1.5F, 0.0F, -1.5F, 3, 9, 3, scaleOrSo + 0.004F);
        // arm.rotateAngleX = -(float) (Math.PI/4);

        arm.mirror = mirror;

        final ModelRenderer clawFrontUpper = new ModelRenderer(this).setTextureSize(textureX, textureY);
        clawFrontUpper.setRotationPoint(0, 8.5F, -1.0F);
        clawFrontUpper.setTextureOffset(40, 50).addBox(-0.5F, 0.5F, -0.5F, 1, 2, 1, scaleOrSo + 0.004F);

        clawFrontUpper.rotateAngleX = -(float) Math.PI / 4;

        final ModelRenderer clawBackUpper = new ModelRenderer(this).setTextureSize(textureX, textureY);
        clawBackUpper.setRotationPoint(0, 8.5F, 1.0F);
        clawBackUpper.setTextureOffset(40, 50).addBox(-0.5F, 0.5F, -0.5F, 1, 2, 1, scaleOrSo + 0.004F);

        clawBackUpper.rotateAngleX = (float) Math.PI / 4;

        final ModelRenderer clawFrontLower = new ModelRenderer(this).setTextureSize(textureX, textureY);
        clawFrontLower.setRotationPoint(0.0F, 9.5F, 3.0F);
        clawFrontLower.setTextureOffset(40, 50).addBox(-0.5F, 0.5F, -0.5F, 1, 2, 1, scaleOrSo + 0.004F);
        clawFrontLower.rotateAngleX = -(float) Math.PI / 4;

        final ModelRenderer clawBackLower = new ModelRenderer(this).setTextureSize(textureX, textureY);
        clawBackLower.setRotationPoint(0.0F, 9.5F, -3.0F);
        clawBackLower.setTextureOffset(40, 50).addBox(-0.5F, 0.5F, -0.5F, 1, 2, 1, scaleOrSo + 0.004F);

        clawBackLower.rotateAngleX = (float) Math.PI / 4;

        arm.addChild(clawFrontUpper);
        arm.addChild(clawFrontLower);
        arm.addChild(clawBackUpper);
        arm.addChild(clawBackLower);
        return arm;
    }

    @Override
    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_,
            float p_78087_5_, float p_78087_6_, Entity p_78087_7_) {
        this.villagerHead.rotateAngleY = p_78087_4_ / (180F / (float) Math.PI);
        this.villagerHead.rotateAngleX = p_78087_5_ / (180F / (float) Math.PI);
        /*
         * this.villagerArms.rotationPointY = 3.0F; this.villagerArms.rotationPointZ = -1.0F;
         * this.villagerArms.rotateAngleX = -0.75F;
         */
        this.rightVillagerLeg.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F) * 1.4F * p_78087_2_ * 0.5F;
        this.leftVillagerLeg.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F + (float) Math.PI) * 1.4F
                * p_78087_2_
                * 0.5F;
        this.rightVillagerLeg.rotateAngleY = 0.0F;
        this.leftVillagerLeg.rotateAngleY = 0.0F;
    }

    @Override
    public void render(Entity p_78088_1_, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_,
            float p_78088_6_, float p_78088_7_) {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, p_78088_1_);

        this.villagerHead.render(p_78088_7_);
        this.villagerBody.render(p_78088_7_);
        this.rightVillagerLeg.render(p_78088_7_);
        this.leftVillagerLeg.render(p_78088_7_);

        this.rightArm.render(p_78088_7_);
        this.leftArm.render(p_78088_7_);

    }
}
