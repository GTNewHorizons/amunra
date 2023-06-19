package de.katzenpapst.amunra.mob.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

public class ModelFirstBoss extends ModelBiped {

    protected ModelRenderer headTopPart;
    protected ModelRenderer headSidePart1;
    protected ModelRenderer headSidePart2;
    protected ModelRenderer headTopPart2;
    protected ModelRenderer beard;

    public ModelFirstBoss() {
        super(0.0F, 0.0F, 64, 64);

        this.addHelmet();
    }

    protected void addHelmet() {
        this.textureWidth = 64;
        this.textureHeight = 64;

        this.headTopPart = new ModelRenderer(this, 0, 32);
        this.headSidePart1 = new ModelRenderer(this, 24, 32);
        this.headSidePart2 = new ModelRenderer(this, 24, 32);
        this.headTopPart2 = new ModelRenderer(this, 0, 40);
        this.beard = new ModelRenderer(this, 38, 32);

        this.headTopPart.addBox(-4F, -2F, -2F, 8, 4, 4);
        this.headTopPart.setRotationPoint(0F, -8.5F, -1F);
        this.headTopPart.setTextureSize(64, 64);
        this.headTopPart.mirror = true;
        this.setRotation(this.headTopPart, -0.5948578F, 0F, 0F);

        this.headSidePart1.addBox(-3F, -1F, -1F, 5, 11, 2);
        this.headSidePart1.setRotationPoint(-3F, -8F, 0F);
        this.headSidePart1.setTextureSize(64, 64);
        this.headSidePart1.mirror = true;
        this.setRotation(this.headSidePart1, 0F, 0F, 0.2974289F);
        this.headSidePart2.mirror = true;

        this.headSidePart2.addBox(-2F, -1F, -1F, 5, 11, 2);
        this.headSidePart2.setRotationPoint(3F, -8F, 0F);
        this.headSidePart2.setTextureSize(64, 64);
        this.headSidePart2.mirror = true;
        this.setRotation(this.headSidePart2, 0F, 0F, -0.2974216F);
        this.headSidePart2.mirror = false;

        this.headTopPart2.addBox(-5F, -1F, -1F, 10, 2, 2);
        this.headTopPart2.setRotationPoint(0F, -9F, 0.1F);
        this.headTopPart2.setTextureSize(64, 64);
        this.headTopPart2.mirror = true;
        this.setRotation(this.headTopPart2, 0F, 0F, 0F);

        this.beard.addBox(-1F, -1F, -0.5F, 2, 8, 1);
        this.beard.setRotationPoint(0F, 0F, -4F);
        this.beard.setTextureSize(64, 64);
        this.beard.mirror = true;
        this.setRotation(this.beard, -0.669215F, 0F, 0F);

        this.bipedHead.addChild(this.headTopPart);
        this.bipedHead.addChild(this.headTopPart2);
        this.bipedHead.addChild(this.headSidePart1);
        this.bipedHead.addChild(this.headSidePart2);
        this.bipedHead.addChild(this.beard);
    }

    private void setRotation(final ModelRenderer model, final float x, final float y, final float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
}
