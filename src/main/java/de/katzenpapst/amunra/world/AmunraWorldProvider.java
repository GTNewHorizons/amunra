package de.katzenpapst.amunra.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.astronomy.AngleDistance;
import de.katzenpapst.amunra.helper.AstronomyHelper;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldProviderSpace;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.IExitHeight;
import micdoodle8.mods.galacticraft.api.world.ISolarLevel;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;

public abstract class AmunraWorldProvider extends WorldProviderSpace implements IExitHeight, ISolarLevel {

    protected float solarLevel = -1;

    /**
     * Gravity relative to OW. 1.35 seems to be the last value where you can jump up blocks. walking up stairs seems to
     * work on any gravity
     */
    protected abstract float getRelativeGravity();

    @Override
    public boolean isDaytime() {
        return this.worldObj.skylightSubtracted < 4;
    }

    @Override
    public float getGravity() {
        return 0.08F * (1 - this.getRelativeGravity());
    }

    @Override
    public double getFuelUsageMultiplier() {
        return this.getRelativeGravity();
    }

    @Override
    public float getFallDamageModifier() {
        return this.getRelativeGravity();
    }

    @Override
    public boolean canSpaceshipTierPass(final int tier) {
        return tier >= AmunRa.config.planetDefaultTier;
    }

    @Override
    public boolean shouldForceRespawn() {
        return !ConfigManagerCore.forceOverworldRespawn;
    }

    @Override
    public boolean hasAtmosphere() {
        return this.getCelestialBody().atmosphere.size() > 0;
    }

    public boolean hasClouds() {
        return this.hasAtmosphere();
    }

    /**
     * Return Vec3D with biome specific fog color
     */
    @Override
    @SideOnly(Side.CLIENT)
    public Vec3 getFogColor(float p_76562_1_, float p_76562_2_) {
        float dayFactor = MathHelper.cos(p_76562_1_ * (float) Math.PI * 2.0F) * 2.0F + 0.5F;

        if (dayFactor < 0.0F) {
            dayFactor = 0.0F;
        }

        if (dayFactor > 1.0F) {
            dayFactor = 1.0F;
        }

        final Vector3 baseColor = this.getFogColor();

        float r = baseColor.floatX();
        float g = baseColor.floatY();
        float b = baseColor.floatZ();
        r *= dayFactor * 0.94F + 0.06F;
        g *= dayFactor * 0.94F + 0.06F;
        b *= dayFactor * 0.91F + 0.09F;
        return Vec3.createVectorHelper(r, g, b);
    }

    @Override
    public float getSolarSize() {
        // this works only for planets...
        final CelestialBody body = this.getCelestialBody();

        if (body instanceof final Moon moon) {
            return 1.0F / moon.getParentPlanet().getRelativeDistanceFromCenter().unScaledDistance;
        }
        return 1.0F / body.getRelativeDistanceFromCenter().unScaledDistance;
    }

    @Override
    public double getSolarEnergyMultiplier() {
        if (this.solarLevel < 0) {
            this.solarLevel = AstronomyHelper
                    .getSolarEnergyMultiplier(this.getCelestialBody(), !this.getCelestialBody().atmosphere.isEmpty());
        }
        return this.solarLevel;
    }
    /*
     * @SideOnly(Side.CLIENT)
     * @Override public Vec3 getFogColor(float var1, float var2) { Vector3 fogColor = this.getFogColor(); return
     * Vec3.createVectorHelper(fogColor.floatX(), fogColor.floatY(), fogColor.floatZ()); }
     */

    @Override
    public Vec3 getSkyColor(final Entity cameraEntity, final float partialTicks) {
        final Vector3 skyColorBase = this.getSkyColor();
        final float celestialAngle = this.worldObj.getCelestialAngle(partialTicks);
        float dayFactor = MathHelper.cos(celestialAngle * (float) Math.PI * 2.0F) * 2.0F + 0.5F;

        if (dayFactor < 0.0F) {
            dayFactor = 0.0F;
        }

        if (dayFactor > 1.0F) {
            dayFactor = 1.0F;
        }

        float red = skyColorBase.floatX();
        float green = skyColorBase.floatY();
        float blue = skyColorBase.floatZ();
        red *= dayFactor;
        green *= dayFactor;
        blue *= dayFactor;

        return Vec3.createVectorHelper(red, green, blue);
    }

    /**
     * This is the part which makes the world brighter or dimmer
     */
    @SideOnly(Side.CLIENT)
    @Override
    public float getSunBrightness(float par1) {

        float factor = this.worldObj.getSunBrightnessBody(par1) + this.getAmunBrightnessFactor(par1);
        if (factor > 1.0F) {
            factor = 1.0F;
        }
        return factor;
    }

    /**
     * The current sun brightness factor for this dimension. 0.0f means no light at all, and 1.0f means maximum
     * sunlight. This will be used for the "calculateSkylightSubtracted" which is for Sky light value calculation. No
     * idea what this actually influences
     *
     * @return The current brightness factor
     */
    @Override
    public float getSunBrightnessFactor(float par1) {
        // I *think* that I could use this to make eclipses etc work
        float factor = this.worldObj.getSunBrightnessFactor(par1) + this.getAmunBrightnessFactor(par1);

        if (factor > 1.0F) {
            factor = 1.0F;
        }

        return factor;
    }

    protected float getAmunBrightnessFactor(final float partialTicks) {
        CelestialBody curBody = this.getCelestialBody();
        if (curBody instanceof Moon moon) {
            curBody = moon.getParentPlanet();
        }
        final AngleDistance ad = AstronomyHelper
                .projectBodyToSky(curBody, AmunRa.instance.starAmun, partialTicks, this.worldObj.getWorldTime());
        // ad.angle is in pi

        // the angle I get is relative to celestialAngle
        float brightnessFactor = 1.0F
                - (MathHelper.cos(this.worldObj.getCelestialAngle(partialTicks) * (float) Math.PI * 2.0F + ad.angle)
                        * 2.0F + 0.5F);

        if (brightnessFactor < 0) {
            brightnessFactor = 0;
        }
        if (brightnessFactor > 1) {
            brightnessFactor = 1;
        }

        brightnessFactor = 1.0F - brightnessFactor;

        // let's say brightnessFactor == 1 -> 0.5 of brightness
        return (float) (brightnessFactor * 0.8 / ad.distance);
    }
}
