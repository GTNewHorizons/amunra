package de.katzenpapst.amunra.client.fx;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import micdoodle8.mods.galacticraft.api.vector.Vector3;

public class EntityFXMothershipJetFire extends EntityFX {

    public EntityFXMothershipJetFire(final World world, final Vector3 pos, final Vector3 motion) {
        super(world, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);

        this.particleScale = 2.0F;

        // this is needed because the vanilla code adds a y component
        this.motionX = motion.x + (float) (Math.random() * 2.0D - 1.0D) * 0.4F;
        this.motionY = motion.y + (float) (Math.random() * 2.0D - 1.0D) * 0.4F;
        this.motionZ = motion.z + (float) (Math.random() * 2.0D - 1.0D) * 0.4F;
        final float f = (float) (Math.random() + Math.random() + 1.0D) * 0.15F;
        final float f1 = MathHelper.sqrt_double(
                this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ) / 9.0F;
        this.motionX = this.motionX / f1 * f * 0.4D;
        this.motionY = this.motionY / f1 * f * 0.4D;
        this.motionZ = this.motionZ / f1 * f * 0.4D;

        this.particleRed = 1.0F;
        this.particleGreen = 120F / 255F + this.rand.nextFloat() / 3;
        this.particleBlue = 55F / 255F;

        this.particleMaxAge = (int) (Math.ceil(this.particleMaxAge) * 2.0F);

        this.noClip = true; // for now
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setDead();
            return;
        }

        // after a while, vary my other coordinates slightly

        this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        this.particleGreen += 0.01F;
    }

    @Override
    public void renderParticle(Tessellator p_70539_1_, float p_70539_2_, float p_70539_3_, float p_70539_4_,
            float p_70539_5_, float p_70539_6_, float p_70539_7_) {
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        super.renderParticle(p_70539_1_, p_70539_2_, p_70539_3_, p_70539_4_, p_70539_5_, p_70539_6_, p_70539_7_);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
    }

    @Override
    public int getBrightnessForRender(float p_70070_1_) {
        return 0xF000F0;
    }

    @Override
    public float getBrightness(float p_70013_1_) {
        return 1.0F;
    }
}
