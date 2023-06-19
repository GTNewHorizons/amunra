package de.katzenpapst.amunra.client.fx;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.api.vector.Vector3;

public class EntityFXGravityDust extends EntityFX {

    public EntityFXGravityDust(final World par1World, final Vector3 position, final Vector3 motion) {
        super(par1World, position.x, position.y, position.z, motion.x, motion.y, motion.z);

        // better position
        final double xDev = par1World.rand.nextGaussian() * 0.75;
        final double zDev = par1World.rand.nextGaussian() * 0.75;

        position.x += xDev;
        position.z += zDev;

        if (motion.y < 0) {
            // < 0 means downwards
            position.y += 2;
        }

        final double maxLength = 1.2;

        this.setPosition(position.x, position.y, position.z);

        this.motionX = 0;
        this.motionY = motion.y;
        this.motionZ = 0;
        final Vector3 color = new Vector3(0.4, 0.4, 0.4);
        this.particleRed = color.floatX();
        this.particleGreen = color.floatY();
        this.particleBlue = color.floatZ();
        final double g = Math.abs(motion.y);
        final double timeNeeded = maxLength / g;
        this.particleMaxAge = (int) (Math.random() * 10.0D + timeNeeded);
        this.noClip = true;
        this.setParticleTextureIndex((int) (Math.random() * 8.0D));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float p_70070_1_) {
        final int brightness = super.getBrightnessForRender(p_70070_1_);
        final double var3 = Math.pow((double) this.particleAge / (double) this.particleMaxAge, 3.0);
        final int var4 = brightness & 255;
        int var5 = brightness >> 16 & 255;
        var5 += (int) (var3 * 240.0);

        if (var5 > 240) {
            var5 = 240;
        }

        return var4 | var5 << 16;
    }

    @Override
    public float getBrightness(float p_70013_1_) {
        final float brightness = super.getBrightness(p_70013_1_);
        final float var3 = (float) Math.pow((double) this.particleAge / (double) this.particleMaxAge, 4.0);
        return brightness * (1.0F - var3) + var3;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setDead();
        }
    }
}
