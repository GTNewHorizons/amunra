package de.katzenpapst.amunra.mothership;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;

import org.lwjgl.opengl.GL11;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.world.SkyProviderDynamic;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.api.galaxies.Star;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;

public class SkyProviderMothership extends SkyProviderDynamic {

    protected CelestialBody mothershipParent;
    protected boolean isInTransit = false;

    protected int jetDirection = 0;

    protected float transitOffset = 0;
    protected long curWorldTime = -1;

    protected final double skyBoxLength = 100.0D;
    protected final double cylinderLength = this.skyBoxLength * 12;
    protected final double angleWidth = 0.5D / this.skyBoxLength;
    protected final int numStarLines;

    protected final float starLineSpeed = 20;

    public SkyProviderMothership(final IGalacticraftWorldProvider worldProvider) {
        super(worldProvider);
        this.numStarLines = AmunRa.config.mothershipNumStarLines;

        this.hasHorizon = false;
    }

    protected void initStarLines(final int list, final double radius) {

        GL11.glPushMatrix();
        GL11.glNewList(list, GL11.GL_COMPILE);

        final Random rand = new Random(); // 10842L
        final Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();

        final double size = 0.5D;
        final double skyRadius = 100.0D;
        for (int starIndex = 0; starIndex < 400; ++starIndex) {

            final double theta = rand.nextDouble() * Math.PI * 2;
            final double angleWidth = size / skyRadius; // should be

            final double x1 = Math.cos(theta - angleWidth) * skyRadius;
            final double y1 = Math.sin(theta - angleWidth) * skyRadius;
            final double x2 = Math.cos(theta + angleWidth) * skyRadius;
            final double y2 = Math.sin(theta + angleWidth) * skyRadius;
            final double zBase = rand.nextDouble() * skyRadius * 2 - skyRadius;
            final double length = rand.nextDouble() * 20.0D;

            // DC is in front

            // paint
            // A
            tess.addVertex(x1, y1, zBase);
            // B
            tess.addVertex(x1, y1, zBase + length);
            // C
            tess.addVertex(x2, y2, zBase + length);
            // D
            tess.addVertex(x2, y2, zBase);
        }

        tess.draw();

        GL11.glEndList();
        GL11.glPopMatrix();

    }

    /*
     * protected void renderMothershipParent() { double distanceToParent = 1; float zIndex = (float)
     * (20/distanceToParent); float distance = (float) (curBodyPlanet.getRelativeSize() / distanceToParent); // my
     * parent this.nearBodiesToRender.add( new BodyRenderTask(curBodyPlanet, 0, zIndex, distance, (float) Math.PI) ); }
     */

    @Override
    protected void renderSystem(final float partialTicks, final WorldClient world, final Tessellator tess,
            final Minecraft mc) {
        super.renderSystem(partialTicks, world, tess, mc);

        // now do the planet we are orbiting

        GL11.glPushMatrix();
        // rotate back
        if (this.rType != RenderType.STAR && !AmunRa.config.isSun(this.mothershipParent)) {
            if (!this.isAsteroidBelt) {
                GL11.glRotatef(180 - this.currentCelestialAngle * 360, 1.0F, 0.0F, 0.0F);

                this.renderPlanetByAngle(
                        tess,
                        this.mothershipParent,
                        0,
                        20,
                        10,
                        fixAngle((float) (this.currentCelestialAngle * PI_DOUBLE)));
            }
        } else {
            GL11.glRotatef(-this.currentCelestialAngle * 360, 1.0F, 0.0F, 0.0F);
            this.renderPlanetByAngle(tess, this.mothershipParent, 0, 20, 15, 0);
        }

        GL11.glPopMatrix();
    }

    @Override
    protected boolean excludeBodyFromRendering(final CelestialBody body) {
        return body.equals(this.mothershipParent);
    }
    /*
     * @Override protected void prepareSystemForRender(long curWorldTime, float partialTicks) {
     * super.prepareSystemForRender(curWorldTime, partialTicks); renderMothershipParent(); }
     */

    @Override
    protected void initVars() {

        if (((Mothership) this.curBody).isInTransit()) {
            this.curBodyPlanet = null;
            this.curSystem = null;
            this.mothershipParent = null;
            this.isInTransit = true;
            this.curWorldTime = -1;

            if (((MothershipWorldProvider) this.worldProvider).getTheoreticalTransitData() != null) {
                this.jetDirection = ((MothershipWorldProvider) this.worldProvider)
                        .getTheoreticalTransitData().direction;
            } else {
                this.jetDirection = -1;
            }
            this.clearAsteroidRenderList();
        } else {
            this.mothershipParent = ((Mothership) this.curBody).getParent();
            if (this.mothershipParent instanceof Planet) {
                // pretend we are the planet itself
                this.rType = RenderType.PLANET;
                this.curBodyPlanet = ((Mothership) this.curBody).getParent();
                this.curSystem = ((Planet) this.mothershipParent).getParentSolarSystem();

                // but use the distance from the planet
                this.curBodyDistance = this.mothershipParent.getRelativeDistanceFromCenter().unScaledDistance;

                this.sunSize = 1.0F / this.curBodyDistance;
            } else if (this.mothershipParent instanceof Moon) {
                // pretend we are a sibling moon
                this.rType = RenderType.MOON;
                this.curBodyPlanet = ((Moon) this.mothershipParent).getParentPlanet();
                this.curSystem = ((Moon) this.mothershipParent).getParentPlanet().getParentSolarSystem();

                this.curBodyDistance = this.curBodyPlanet.getRelativeDistanceFromCenter().unScaledDistance;

                this.sunSize = 1.0F / this.curBodyDistance;
            } else if (this.mothershipParent instanceof Star) {
                // pretend we are a star?
                this.rType = RenderType.STAR;
                this.curBodyPlanet = this.curBody;
                this.curSystem = ((Star) this.mothershipParent).getParentSolarSystem();
                this.curBodyDistance = this.curBody.getRelativeDistanceFromCenter().unScaledDistance;

                this.sunSize = 5;
            }
            this.checkAsteroidRendering(this.mothershipParent);

            this.isInTransit = false;
        }

        this.hasAtmosphere = false;

    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        if (this.isInTransit != ((Mothership) this.curBody).isInTransit()) {
            this.initVars();
        }

        if (!this.isInTransit) {
            super.render(partialTicks, world, mc);
        } else {
            this.renderTransitSky(partialTicks, world, mc);
        }
    }

    protected void renderTransitSky(final float partialTicks, final WorldClient world, final Minecraft mc) {

        // try stuff
        if (this.curWorldTime == -1) {
            this.curWorldTime = world.getWorldTime();
        }
        // I need the actual time here
        this.transitOffset = partialTicks + world.getWorldTime() - this.curWorldTime;

        this.renderStarLines(this.transitOffset);

    }

    protected void renderStarLines(final float curTime) {
        //// BEGIN

        float angle = 0;
        switch (this.jetDirection) {
            case 0:
                angle = 180.0F;
                break;
            case 1:
                angle = 90.0F;
                break;
            case 2:
                angle = 0;
                break;
            case 3:
                angle = 270.0F;
                break;
            case -1:
                // means we haven't got this from the worldprovider yet
                // keep bothering it until it gets the packet
                if (((MothershipWorldProvider) this.worldProvider).getTheoreticalTransitData() != null) {
                    this.jetDirection = ((MothershipWorldProvider) this.worldProvider)
                            .getTheoreticalTransitData().direction;
                }
                return;

        }

        final Random starLineRand = new Random(10842L);
        GL11.glPushMatrix();
        GL11.glRotatef(angle, 0.0F, 1.0F, 0.0F);

        final Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();

        for (int starIndex = 0; starIndex < 400; ++starIndex) {

            final double theta = starLineRand.nextDouble() * Math.PI * 2;

            double zBase = MathHelper.getRandomDoubleInRange(starLineRand, 0, this.cylinderLength * 2);

            // motion offset
            zBase += curTime * this.starLineSpeed;
            zBase = zBase % (this.cylinderLength * 2) - this.cylinderLength;

            double x1 = Math.cos(theta - this.angleWidth) * this.skyBoxLength;
            double y1 = Math.sin(theta - this.angleWidth) * this.skyBoxLength;
            double x2 = Math.cos(theta + this.angleWidth) * this.skyBoxLength;
            double y2 = Math.sin(theta + this.angleWidth) * this.skyBoxLength;
            final double length = starLineRand.nextDouble() * 20.0D;

            // project the lines onto the cylinder's circles if necessary
            if (zBase < -this.skyBoxLength || zBase + length > this.skyBoxLength) {
                // zBase = skyRadius
                x1 = x1 / zBase * this.skyBoxLength;
                y1 = y1 / zBase * this.skyBoxLength;
                x2 = x2 / zBase * this.skyBoxLength;
                y2 = y2 / zBase * this.skyBoxLength;
                // zBase = skyRadius;
            }

            // paint
            // A
            tess.addVertex(x1, y1, zBase);
            // B
            tess.addVertex(x1, y1, zBase + length);
            // C
            tess.addVertex(x2, y2, zBase + length);
            // D
            tess.addVertex(x2, y2, zBase);
        }

        tess.draw();

        GL11.glPopMatrix();
        //// END
    }
}
