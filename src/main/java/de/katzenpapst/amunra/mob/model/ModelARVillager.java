package de.katzenpapst.amunra.mob.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelARVillager extends ModelVillager {
    // public ModelRenderer brain;

    public ModelRenderer antenna1;
    public ModelRenderer antenna2;

    protected ModelRenderer[] tailBoxes = new ModelRenderer[4];

    private static final int[][] tailBoxPositions = { { 32, 48 }, { 32, 8 }, { 32, 0 }, { 56, 16 } };
    private static final int[] tailBoxSizes = { 8, 6, 4, 2 };
    private static final float[] offsets = { -2.0F, 1.5F, 4.0F, 6.0F };

    public ModelARVillager(final float par1) {
        this(par1, 0.0F, 64, 64);
    }

    public ModelARVillager(final float scaleOrSo, final float par2, final int textureX, final int textureY) {
        super(scaleOrSo, par2, 0, 0);

        this.villagerHead = new ModelRenderer(this).setTextureSize(textureX, textureY);
        this.villagerHead.setRotationPoint(0.0F, 0.0F + par2, 0.0F);
        this.villagerHead.setTextureOffset(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, scaleOrSo + 0.001F);
        this.villagerNose = new ModelRenderer(this).setTextureSize(textureX, textureY);
        this.villagerNose.setRotationPoint(0.0F, par2 - 2.0F, 0.0F);
        this.villagerNose.setTextureOffset(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2, 4, 2, scaleOrSo + 0.002F);
        this.villagerHead.addChild(this.villagerNose);
        this.villagerBody = new ModelRenderer(this).setTextureSize(textureX, textureY);
        this.villagerBody.setRotationPoint(0.0F, 0.0F + par2, 0.0F);
        this.villagerBody.setTextureOffset(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8, 12, 6, scaleOrSo + 0.003F);
        this.villagerBody.setTextureOffset(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8, 18, 6, scaleOrSo + 0.5F + 0.004F);
        this.villagerArms = new ModelRenderer(this).setTextureSize(textureX, textureY);
        this.villagerArms.setRotationPoint(0.0F, 0.0F + par2 + 2.0F, 0.0F);
        this.villagerArms.setTextureOffset(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4, 8, 4, scaleOrSo + 0.005F);
        this.villagerArms.setTextureOffset(44, 22).addBox(4.0F, -2.0F, -2.0F, 4, 8, 4, scaleOrSo + 0.0001F);
        this.villagerArms.setTextureOffset(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8, 4, 4, scaleOrSo + 0.0004F);
        this.rightVillagerLeg = new ModelRenderer(this, 0, 22).setTextureSize(textureX, textureY);
        this.rightVillagerLeg.setRotationPoint(-2.0F, 12.0F + par2, 0.0F);
        this.rightVillagerLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, scaleOrSo + 0.0006F);
        this.leftVillagerLeg = new ModelRenderer(this, 0, 22).setTextureSize(textureX, textureY);
        this.leftVillagerLeg.mirror = true;
        this.leftVillagerLeg.setRotationPoint(2.0F, 12.0F + par2, 0.0F);
        this.leftVillagerLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, scaleOrSo + 0.0002F);

        final float antennaOffset = -6.0F;

        this.antenna1 = new ModelRenderer(this).setTextureSize(textureX, textureY);
        this.antenna1.setRotationPoint(0.5F, antennaOffset + par2, 0.0F); // +1
        this.antenna1.setTextureOffset(54, 6).addBox(4.0F, -0.5F, -0.5F, 4, 1, 1, scaleOrSo + 0.5F);

        final ModelRenderer antennaTip1 = new ModelRenderer(this).setTextureSize(textureX, textureY);
        antennaTip1.setRotationPoint(8.0F, 0.0F, 0.0F);
        antennaTip1.setTextureOffset(56, 8).addBox(0.0F, -1.5F, -1.5F, 1, 3, 3, scaleOrSo + 0.5F);
        antennaTip1.rotateAngleX = (float) Math.PI / 4;
        this.antenna1.addChild(antennaTip1);
        this.antenna1.rotateAngleZ = (float) -Math.PI / 4;

        this.antenna2 = new ModelRenderer(this).setTextureSize(textureX, textureY);
        this.antenna2.setRotationPoint(-0.5F, antennaOffset + par2, 0.0F); // -1
        this.antenna2.setTextureOffset(54, 6).addBox(4.0F, -0.5F, -0.5F, 4, 1, 1, scaleOrSo + 0.5F);

        final ModelRenderer antennaTip2 = new ModelRenderer(this).setTextureSize(textureX, textureY);
        antennaTip2.setRotationPoint(8.0F, 0.0F, 0.0F);
        antennaTip2.setTextureOffset(56, 8).addBox(0.0F, -1.5F, -1.5F, 1, 3, 3, scaleOrSo + 0.5F);
        antennaTip2.rotateAngleX = (float) Math.PI / 4;
        this.antenna2.addChild(antennaTip2);
        this.antenna2.rotateAngleY = (float) Math.PI;
        this.antenna2.rotateAngleZ = (float) Math.PI / 4;

        // try to do a tail
        for (int i = 0; i < this.tailBoxes.length; i++) {
            this.tailBoxes[i] = new ModelRenderer(this).setTextureSize(textureX, textureY);
            final int[] curData = tailBoxPositions[i];
            final int curSize = tailBoxSizes[i];
            final float halfSize = curSize / 2.0F;
            final float curPos = offsets[i];
            this.tailBoxes[i].setRotationPoint(0.0F, 12.0F + par2, curPos - 2);
            this.tailBoxes[i].setTextureOffset(curData[0], curData[1])
                    .addBox(-halfSize, -curSize + 12.0F, curPos, curSize, curSize, curSize);
        }

        this.villagerHead.addChild(this.antenna1);
        this.villagerHead.addChild(this.antenna2);

    }

    @Override
    public void setRotationAngles(float p_78087_1_, float p_78087_2_, float p_78087_3_, float p_78087_4_,
            float p_78087_5_, float p_78087_6_, Entity p_78087_7_) {
        this.villagerHead.rotateAngleY = p_78087_4_ / (180F / (float) Math.PI);
        this.villagerHead.rotateAngleX = p_78087_5_ / (180F / (float) Math.PI);
        this.villagerArms.rotationPointY = 3.0F;
        this.villagerArms.rotationPointZ = -1.0F;
        this.villagerArms.rotateAngleX = -0.75F;

        final float test = MathHelper.cos(p_78087_1_ * 0.6662F) * 1.4F * p_78087_2_ * 0.5F;

        this.rightVillagerLeg.rotateAngleX = test;
        this.leftVillagerLeg.rotateAngleX = MathHelper.cos(p_78087_1_ * 0.6662F + (float) Math.PI) * 1.4F
                * p_78087_2_
                * 0.5F;
        this.rightVillagerLeg.rotateAngleY = 0.0F;
        this.leftVillagerLeg.rotateAngleY = 0.0F;

        for (int i = 0; i < this.tailBoxes.length; i++) {
            final float curPos = offsets[i];
            this.tailBoxes[i].rotationPointZ = curPos * test * 2 + 5 * test - 2;
        }

    }

    @Override
    public void render(Entity p_78088_1_, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_,
            float p_78088_6_, float p_78088_7_) {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, p_78088_1_);
        this.villagerHead.render(p_78088_7_);
        this.villagerBody.render(p_78088_7_);
        this.villagerArms.render(p_78088_7_);

        for (ModelRenderer element : this.tailBoxes) {
            element.render(p_78088_7_);
        }

    }

}
