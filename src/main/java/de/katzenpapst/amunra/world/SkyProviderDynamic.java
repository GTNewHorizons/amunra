package de.katzenpapst.amunra.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IRenderHandler;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.client.FMLClientHandler;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.client.RingsRenderInfo;
import de.katzenpapst.amunra.helper.AstronomyHelper;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import micdoodle8.mods.galacticraft.api.galaxies.Star;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;

public class SkyProviderDynamic extends IRenderHandler {

    public class BodyRenderTask implements Comparable<BodyRenderTask> {

        // angle at which the body should be rendered in the sky of the current body
        public double angle;
        // zIndex in order to make the bodies overlap
        public double zIndex;
        // size of the body in the sky
        public double scale;

        /*
         * this *should* be the current body's phase on the current sky 0 = new, 0 < x < 180 = waxing, 180 = full, 180 <
         * 360 = waning it should even automatically work for moons
         */
        public double phaseAngle;
        // the body to render
        public CelestialBody body;

        public BodyRenderTask(final CelestialBody body, final double angle, final double zIndex, final double scale,
                final double phaseAngle) {
            this.body = body;
            this.angle = fixAngle(angle);
            this.zIndex = zIndex;
            this.scale = scale;
            this.phaseAngle = fixAngle(phaseAngle);
        }

        @Override
        public int compareTo(final BodyRenderTask other) {
            return Double.compare(other.zIndex, this.zIndex);
        }
    }

    // how to render the sky
    protected enum RenderType {
        // render as if we are on a star, whenever that makes sense or not
        STAR,
        // render as if we are on a planet
        PLANET,
        // render as if we are on a moon
        MOON,
        // rings (aka, asteroid belt around a planet) are complicated, so they get their own
        RINGS
    }

    protected List<ResourceLocation> asteroidTextures = null;

    protected RenderType rType = RenderType.PLANET;

    protected boolean hasHorizon = true;

    public static final double PI_HALF = Math.PI / 2;
    public static final double PI_DOUBLE = Math.PI * 2;

    public static final float PLANET_AXIS_ANGLE_DEFAULT = -19.0F;
    public static final float PLANET_AXIS_ANGLE_ASTEROID = -90.0F;

    public static final float MOON_AXIS_ANGLE_ASTEROID = 10.0F;
    public static final float MOON_AXIS_ANGLE_DEFAULT = 10.0F;

    protected final List<BodyRenderTask> farBodiesToRender = new ArrayList<>();
    protected final List<BodyRenderTask> nearBodiesToRender = new ArrayList<>();

    // angle of the system in the sky
    private static float planetAxisAngle = -19.0F;
    // angle of the moons' orbits relative to the equator
    private static final float moonAxisAngle = 10.0F;

    private final double parentSunFactor = 6.0D;
    private final double parentPlanetFactor = 80.0D;
    private final double siblingPlanetFactor = 1.0D;
    private final double siblingStarFactor = 4.0D;

    private final double siblingMoonFactor = 80.0D;
    private final double childMoonFactor = 400.0D;
    private final double childPlanetFactor = 5.0D;

    public int starList;
    public int glSkyList;
    public int glSkyList2;
    protected float sunSize;

    // system to render in the sky
    protected SolarSystem curSystem;
    // the body to render it around
    protected CelestialBody curBody;
    // this is either the same as curBody, or it's parent, if its a moon
    protected CelestialBody curBodyPlanet;
    // the distance of this body or it's parent from the sun
    protected float curBodyDistance;
    protected float boxWidthHalf = 311;

    protected boolean hasAtmosphere = true;

    protected Vec3 planetSkyColor = null; // this will be set at the beginning of each render() call

    protected float currentCelestialAngle = 0;

    protected IGalacticraftWorldProvider worldProvider;

    protected boolean isAsteroidBelt;
    protected boolean isAsteroidBeltMoon;
    public int asteroidList = 0;

    public static double fixAngle(double angle) {
        while (angle > PI_DOUBLE) {
            angle -= PI_DOUBLE;
        }
        while (angle < 0) {
            angle += PI_DOUBLE;
        }
        return angle;
    }

    public SkyProviderDynamic(final IGalacticraftWorldProvider worldProvider) {
        this.curBody = worldProvider.getCelestialBody();
        this.worldProvider = worldProvider;
        // find the current system

        this.initVars();

        final int displayLists = GLAllocation.generateDisplayLists(3);
        this.starList = displayLists;
        this.glSkyList = displayLists + 1;
        this.glSkyList2 = displayLists + 2;

        // Bind stars to display list
        GL11.glPushMatrix();
        GL11.glNewList(this.starList, GL11.GL_COMPILE);
        this.prepareStars();
        GL11.glEndList();
        GL11.glPopMatrix();

        final Tessellator tessellator = Tessellator.instance;
        // begin of glSkyList
        GL11.glNewList(this.glSkyList, GL11.GL_COMPILE);
        final byte byte2 = 64;
        final int i = 256 / byte2 + 2;
        float f = 16F;

        // what exactly is this? is this the skybox?
        for (int j = -byte2 * i; j <= byte2 * i; j += byte2) {
            for (int l = -byte2 * i; l <= byte2 * i; l += byte2) {
                tessellator.startDrawingQuads();
                tessellator.addVertex(j, f, l);
                tessellator.addVertex(j + byte2, f, l);
                tessellator.addVertex(j + byte2, f, l + byte2);
                tessellator.addVertex(j, f, l + byte2);
                tessellator.draw();
            }
        }

        GL11.glEndList();
        // end of glSkyList

        // begin of glSkyList2
        GL11.glNewList(this.glSkyList2, GL11.GL_COMPILE);
        f = -16F;
        tessellator.startDrawingQuads();

        for (int k = -byte2 * i; k <= byte2 * i; k += byte2) {
            for (int i1 = -byte2 * i; i1 <= byte2 * i; i1 += byte2) {
                tessellator.addVertex(k + byte2, f, i1);
                tessellator.addVertex(k, f, i1);
                tessellator.addVertex(k, f, i1 + byte2);
                tessellator.addVertex(k + byte2, f, i1 + byte2);
            }
        }

        tessellator.draw();
        GL11.glEndList();
        // end of glSkyList2
    }

    protected void checkAsteroidRendering(final CelestialBody body) {
        if (AmunRa.config.isAsteroidBelt(body)) {
            // figure out a seed
            final long seed = body.getName().hashCode() ^ 8546845L;
            this.initAsteroidRenderList(seed);
            this.isAsteroidBelt = true;
            this.hasHorizon = false;
            planetAxisAngle = PLANET_AXIS_ANGLE_ASTEROID;
            this.isAsteroidBeltMoon = body instanceof Moon;
            if (this.isAsteroidBeltMoon) {
                this.rType = RenderType.RINGS;
            }
        } else {
            this.isAsteroidBelt = false;
            this.clearAsteroidRenderList();
            planetAxisAngle = PLANET_AXIS_ANGLE_DEFAULT;
        }
    }

    protected void initAsteroidRenderList(final long seed) {

        if (this.asteroidTextures == null) {
            this.asteroidTextures = AmunRa.instance.getPossibleAsteroidTextures();
        }

        final int numIcons = this.asteroidTextures.size();

        this.asteroidList = GLAllocation.generateDisplayLists(numIcons);
        final Random rand = new Random(seed);

        for (int listIndex = 0; listIndex < numIcons; listIndex++) {

            GL11.glPushMatrix();
            GL11.glNewList(this.asteroidList + listIndex, GL11.GL_COMPILE);

            final Tessellator tess = Tessellator.instance;
            final int numObjects = (int) (rand.nextFloat() * AmunRa.config.numAsteroids / numIcons);
            tess.startDrawingQuads();

            for (int starIndex = 0; starIndex < numObjects; ++starIndex) {
                double randX = rand.nextFloat() * 2.0F - 1.0F;
                double randY = rand.nextFloat() * 2.0F - 1.0F;
                double randZ = rand.nextFloat() * 2.0F - 1.0F;
                final double size = 0.15F + rand.nextFloat() * 4.0F;
                double sqDistance = randX * randX + randY * randY + randZ * randZ;

                if (sqDistance < 1.0D && sqDistance > 0.01D) {
                    sqDistance = 1.0D / Math.sqrt(sqDistance);
                    randX *= sqDistance;
                    randY *= sqDistance;
                    randZ *= sqDistance;
                    final double newX = randX * 100.0D;
                    final double newY = randY * 100.0D;
                    final double newZ = randZ * 100.0D;
                    final double atanXZ = Math.atan2(randX, randZ); // the angle at Z?
                    final double xLength = Math.sin(atanXZ);
                    final double zLength = Math.cos(atanXZ);
                    final double atanPlaneY = Math.atan2(Math.sqrt(randX * randX + randZ * randZ), randY);
                    final double yLength = Math.sin(atanPlaneY);
                    final double planarLength = Math.cos(atanPlaneY);
                    final double rotationMaybe = rand.nextDouble() * Math.PI * 2.0D;
                    final double rotationSin = Math.sin(rotationMaybe);
                    final double rotationCos = Math.cos(rotationMaybe);

                    // texture
                    // try stuff
                    final double color = 0.9;
                    GL11.glColor4d(color, color, color, 1.0);

                    // this draws the actual rect
                    for (int vertexIndex = 0; vertexIndex < 4; ++vertexIndex) {
                        final double zero = 0.0D;
                        final double indexBasedOffset1 = ((vertexIndex & 2) - 1) * size;
                        final double indexBasedOffset2 = ((vertexIndex + 1 & 2) - 1) * size;
                        final double var47 = indexBasedOffset1 * rotationCos - indexBasedOffset2 * rotationSin;
                        final double var49 = indexBasedOffset2 * rotationCos + indexBasedOffset1 * rotationSin;
                        final double vertexY = var47 * yLength + zero * planarLength;
                        final double var55 = zero * yLength - var47 * planarLength;
                        final double vertexX = var55 * xLength - var49 * zLength;
                        final double vertexZ = var49 * xLength + var55 * zLength;
                        final double u = (vertexIndex + 1 & 2) / 2.0;// 00, 01, 10, 11
                        final double v = (vertexIndex & 2) / 2.0;
                        tess.addVertexWithUV(newX + vertexX, newY + vertexY, newZ + vertexZ, u, v);
                    }
                }
            }

            tess.draw();

            GL11.glEndList();
        }
        GL11.glPopMatrix();
    }

    protected void clearAsteroidRenderList() {
        if (this.isAsteroidBelt) {
            GLAllocation.deleteDisplayLists(this.asteroidList); // I hope this is how it works
        }
    }

    protected void initVars() {

        this.sunSize = 2 * this.worldProvider.getSolarSize();
        if (this.curBody instanceof Planet) {
            this.rType = RenderType.PLANET;
            this.curBodyPlanet = this.curBody;
            this.curSystem = ((Planet) this.curBody).getParentSolarSystem();
        } else if (this.curBody instanceof Moon) {
            this.rType = RenderType.MOON;
            this.curBodyPlanet = ((Moon) this.curBody).getParentPlanet();
            this.curSystem = ((Moon) this.curBody).getParentPlanet().getParentSolarSystem();
        } else if (this.curBody instanceof Star) {
            this.rType = RenderType.STAR;
            this.curSystem = ((Star) this.curBody).getParentSolarSystem();
            // this skyprovider is only for moons and planets
        }

        this.checkAsteroidRendering(this.curBody);

        this.hasAtmosphere = this.curBody.atmosphere.size() > 0;
        this.curBodyDistance = this.curBodyPlanet.getRelativeDistanceFromCenter().unScaledDistance;
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        this.planetSkyColor = world.getSkyColor(mc.renderViewEntity, partialTicks);
        float skyR = (float) this.planetSkyColor.xCoord;
        float skyG = (float) this.planetSkyColor.yCoord;
        float skyB = (float) this.planetSkyColor.zCoord;
        float redIthink;

        if (mc.gameSettings.anaglyph) {
            final float f4 = (skyR * 30.0F + skyG * 59.0F + skyB * 11.0F) / 100.0F;
            final float f5 = (skyR * 30.0F + skyG * 70.0F) / 100.0F;
            redIthink = (skyR * 30.0F + skyB * 70.0F) / 100.0F;
            skyR = f4;
            skyG = f5;
            skyB = redIthink;
        }

        GL11.glColor3f(skyR, skyG, skyB);
        final Tessellator tessellator1 = Tessellator.instance;
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_FOG);
        GL11.glColor3f(skyR, skyG, skyB);
        // doing something with glSkyList...
        GL11.glCallList(this.glSkyList);

        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        RenderHelper.disableStandardItemLighting();
        float someXoffset;
        float someYoffset;
        float someZoffset;
        float someOtherY;

        // AH this seems to be what prevents the stars to be visible at day
        float curBrightness = world.getStarBrightness(partialTicks);

        // here, mars does star rendering

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);

        GL11.glPushMatrix();
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);

        this.currentCelestialAngle = this.getCelestialAngle(world, partialTicks);

        if (this.isAsteroidBelt && this.isAsteroidBeltMoon) {
            GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
        }
        GL11.glRotatef(this.currentCelestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(planetAxisAngle, 0, 1.0F, 0);

        this.renderStars(curBrightness);

        curBrightness = 1.0F - curBrightness;

        // here, Mars would render the sun? at y = 100.0D
        GL11.glPopMatrix();

        if (this.isAsteroidBelt) {
            this.renderAsteroids();
        }

        // BEGIN?
        GL11.glShadeModel(GL11.GL_FLAT);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPushMatrix();
        someXoffset = 0.0F;
        someYoffset = 0.0F;
        someZoffset = 0.0F;
        GL11.glTranslatef(someXoffset, someYoffset, someZoffset);
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        // rotates the sky by the celestial angle on the x axis
        // this seems to mean that the x-axis is the rotational axis of the planet
        // does the sun move from -z to z or the other way round?
        if (this.isAsteroidBelt && this.isAsteroidBeltMoon) {
            GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
        }
        GL11.glRotatef(this.currentCelestialAngle * 360.0F, 1.0F, 0.0F, 0.0F);

        // so at this point, I'm where the sun is supposed to be. This is where I have to start.

        // Render system
        if (this.hasAtmosphere) {
            GL11.glEnable(GL11.GL_FOG);
            this.renderSystem(partialTicks, world, tessellator1, mc);
            GL11.glDisable(GL11.GL_FOG);
        } else {
            this.renderSystem(partialTicks, world, tessellator1, mc);
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);

        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_FOG);
        GL11.glPopMatrix();
        // END?

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor3f(0.0F, 0.0F, 0.0F);

        final double playerOverHorizonLevel = mc.thePlayer.getPosition(partialTicks).yCoord - world.getHorizon();

        // WTF is this doing?
        // I think this obscures stuff below the horizon
        if (this.hasHorizon) {

            if (playerOverHorizonLevel < 0.0D) {
                GL11.glPushMatrix();
                GL11.glTranslatef(0.0F, 12.0F, 0.0F);
                GL11.glCallList(this.glSkyList2);
                GL11.glPopMatrix();
                someYoffset = 1.0F;
                someZoffset = -((float) (playerOverHorizonLevel + 65.0D));
                someOtherY = -someYoffset;
                tessellator1.startDrawingQuads();
                tessellator1.setColorRGBA_I(0, 255);
                tessellator1.addVertex(-someYoffset, someZoffset, someYoffset);
                tessellator1.addVertex(someYoffset, someZoffset, someYoffset);
                tessellator1.addVertex(someYoffset, someOtherY, someYoffset);
                tessellator1.addVertex(-someYoffset, someOtherY, someYoffset);
                tessellator1.addVertex(-someYoffset, someOtherY, -someYoffset);
                tessellator1.addVertex(someYoffset, someOtherY, -someYoffset);
                tessellator1.addVertex(someYoffset, someZoffset, -someYoffset);
                tessellator1.addVertex(-someYoffset, someZoffset, -someYoffset);
                tessellator1.addVertex(someYoffset, someOtherY, -someYoffset);
                tessellator1.addVertex(someYoffset, someOtherY, someYoffset);
                tessellator1.addVertex(someYoffset, someZoffset, someYoffset);
                tessellator1.addVertex(someYoffset, someZoffset, -someYoffset);
                tessellator1.addVertex(-someYoffset, someZoffset, -someYoffset);
                tessellator1.addVertex(-someYoffset, someZoffset, someYoffset);
                tessellator1.addVertex(-someYoffset, someOtherY, someYoffset);
                tessellator1.addVertex(-someYoffset, someOtherY, -someYoffset);
                tessellator1.addVertex(-someYoffset, someOtherY, -someYoffset);
                tessellator1.addVertex(-someYoffset, someOtherY, someYoffset);
                tessellator1.addVertex(someYoffset, someOtherY, someYoffset);
                tessellator1.addVertex(someYoffset, someOtherY, -someYoffset);
                tessellator1.draw();
            }

            if (world.provider.isSkyColored()) {
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glColor3f(skyR * 0.2F + 0.04F, skyG * 0.2F + 0.04F, skyB * 0.6F + 0.1F);
            } else {
                /*
                 * GL11.glEnable(GL11.GL_BLEND); GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                 */
                GL11.glColor3f(skyR, skyG, skyB);
            }

            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, -((float) (playerOverHorizonLevel - 16.0D)), 0.0F);
            GL11.glCallList(this.glSkyList2); // what are these lists?
            GL11.glPopMatrix();

        }
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glDisable(GL11.GL_FOG);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glDepthMask(true);

    }

    /**
     * calls calculateCelestialAngle
     */
    public float getCelestialAngle(final WorldClient world, final float partialTicks) {
        if (this.isAsteroidBelt && !this.isAsteroidBeltMoon) {
            return 0.0F;
        }
        return world.getCelestialAngle(partialTicks);
    }

    protected void renderAsteroids() {

        // do this
        GL11.glPushMatrix();
        final int numIcons = this.asteroidTextures.size();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        for (int i = 0; i < numIcons; i++) {
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(this.asteroidTextures.get(i));
            GL11.glCallList(this.asteroidList + i);
        }
        GL11.glPopMatrix();
    }

    protected void renderStars(final float curBrightness) {
        if (this.hasAtmosphere) {
            if (curBrightness > 0.0F) {
                GL11.glColor4f(curBrightness, curBrightness, curBrightness, curBrightness);
                GL11.glCallList(this.starList);
            }
        } else {
            GL11.glColor4f(0.7F, 0.7F, 0.7F, 0.7F);
            GL11.glCallList(this.starList);
        }
    }

    /**
     * Render other planets of the current system
     *
     * @param body                The body to render
     * @param curBodyOrbitalAngle The current orbital angle
     * @param curWorldTime        World time
     * @param partialTicks        Partial ticks
     * @param timeFactor          The time factor of orbital motion. monthFactor for moons, yearFactor for planets
     * @param distanceToParent    distance to the parent
     * @return
     */
    protected BodyRenderTask renderSiblingBody(final CelestialBody body, final double curBodyOrbitalAngle,
            final long curWorldTime, final float partialTicks, final double timeFactor, final double distanceToParent) {
        final float dist = body.getRelativeDistanceFromCenter().unScaledDistance;

        // orbital angle of the planet
        final double curOrbitalAngle = this.getOrbitalAngle(
                body.getRelativeOrbitTime(),
                body.getPhaseShift(),
                curWorldTime,
                partialTicks,
                timeFactor);

        // angle between connection line curBody<-->sun and planet<-->sun
        final double innerAngle = fixAngle(curOrbitalAngle - curBodyOrbitalAngle);// Math.PI-curOrbitalAngle;

        // distance between curBody<-->planet, also needed for scaling
        final double distanceToPlanet = this.getDistanceToBody(innerAngle, distanceToParent, dist); // = sqrt( r1² + r2²
                                                                                                    // - 2*r1*r2
        // - cos(innerAngle) )

        final double projectedAngle = this.projectAngle(innerAngle, dist, distanceToPlanet, distanceToParent);

        // if(planet.equals(other))
        double distance;
        double size = body.getRelativeSize();
        if (body instanceof Planet) {

            if (AstronomyHelper.isStar(body)) {
                distance = size / distanceToPlanet / 4.0D * this.siblingStarFactor;
            } else {
                distance = size / distanceToPlanet / 4.0D * this.siblingPlanetFactor;
            }
        } else {
            if (size > 0.6) {
                size = 0.6;
            }
            distance = size / distanceToPlanet * this.siblingMoonFactor;
        }

        return new BodyRenderTask(body, projectedAngle, distanceToPlanet, distance, innerAngle);
    }

    /**
     * Render siblings, if we are around a planet
     *
     * @param curOrbitalAngle
     * @param curWorldTime
     * @param partialTicks
     */
    protected void renderSiblingMoons(final double curOrbitalAngle, final long curWorldTime, final float partialTicks) {
        final double distanceToParent = this.curBody.getRelativeDistanceFromCenter().unScaledDistance;
        for (final Moon moon : GalaxyRegistry.getRegisteredMoons().values()) {

            if (moon.getParentPlanet() == null || !moon.getParentPlanet().equals(this.curBodyPlanet)
                    || moon.equals(this.curBody)
                    || this.excludeBodyFromRendering(moon)
                    || AmunRa.config.bodiesNoRender.contains(moon.getName())) {
                continue;
            }
            final BodyRenderTask task = this.renderSiblingBody(
                    moon,
                    curOrbitalAngle,
                    curWorldTime,
                    partialTicks,
                    AstronomyHelper.monthFactor,
                    distanceToParent);

            if (task == null) {
                continue;
            }

            this.nearBodiesToRender.add(task);
        }
    }

    /**
     * Render other planets of the current system
     *
     * @param curBodyOrbitalAngle
     * @param curWorldTime
     * @param partialTicks
     */
    protected void renderSiblingPlanets(final double curBodyOrbitalAngle, final long curWorldTime,
            final float partialTicks) {
        for (final Planet planet : GalaxyRegistry.getRegisteredPlanets().values()) {
            // oh well I hope this doesn't kill the performance

            if (planet.getParentSolarSystem() != this.curSystem || planet.equals(this.curBodyPlanet)
                    || AmunRa.config.bodiesNoRender.contains(planet.getName())) {
                continue;
            }

            final BodyRenderTask task = this.renderSiblingBody(
                    planet,
                    curBodyOrbitalAngle,
                    curWorldTime,
                    partialTicks,
                    AstronomyHelper.yearFactor,
                    this.curBodyDistance);
            if (task == null) {
                continue;
            }
            this.farBodiesToRender.add(task);
        }
    }

    protected void renderChildPlanets(final long curWorldTime, final float partialTicks) {
        double curOrbitalAngle;
        for (final Planet planet : GalaxyRegistry.getRegisteredPlanets().values()) {
            // oh well I hope this doesn't kill the performance
            if (planet.getParentSolarSystem() != this.curSystem || planet.equals(this.curBodyPlanet)
                    || AmunRa.config.bodiesNoRender.contains(planet.getName())) {
                continue;
            }

            final double zIndex = planet.getRelativeDistanceFromCenter().unScaledDistance;

            // orbital angle of the planet
            curOrbitalAngle = this.getOrbitalAngle(
                    planet.getRelativeOrbitTime(),
                    planet.getPhaseShift(),
                    curWorldTime,
                    partialTicks,
                    AstronomyHelper.yearFactor);

            final double scale = planet.getRelativeSize() / zIndex / 4.0D * this.childPlanetFactor;

            this.farBodiesToRender.add(new BodyRenderTask(planet, curOrbitalAngle, zIndex, scale, 0// always fully
                                                                                                   // lighted
            ));

        }
    }

    /**
     * Render the moons around a planet
     *
     * @param curWorldTime
     * @param partialTicks
     */
    protected void renderChildMoons(final long curWorldTime, final float partialTicks) {
        double curOrbitalAngle;
        for (final Moon moon : GalaxyRegistry.getRegisteredMoons().values()) {
            // it almost seems like there are orphan moons somewhere
            if (moon.getParentPlanet() == null || !moon.getParentPlanet().equals(this.curBodyPlanet)
                    || AmunRa.config.bodiesNoRender.contains(moon.getName())) {
                continue;
            }

            curOrbitalAngle = this.getOrbitalAngle(
                    moon.getRelativeOrbitTime(),
                    moon.getPhaseShift(),
                    curWorldTime,
                    partialTicks,
                    AstronomyHelper.monthFactor);
            // not projecting the angle here
            final double zIndex = moon.getRelativeDistanceFromCenter().unScaledDistance / 20;
            // earth moon: relative size = 0,2
            double moonSize = moon.getRelativeSize();
            if (moonSize > 0.6) {
                moonSize = 0.6;
            }

            final double distance = moonSize / moon.getRelativeDistanceFromCenter().unScaledDistance
                    * this.childMoonFactor;
            this.nearBodiesToRender
                    .add(new BodyRenderTask(moon, curOrbitalAngle, zIndex, distance, Math.PI - curOrbitalAngle));
        }
    }

    /**
     * Hack for motherships >_>
     *
     * @param body
     * @return
     */
    protected boolean excludeBodyFromRendering(final CelestialBody body) {
        return false;
    }

    /**
     * If we are around a planet, render that.
     *
     * @param curOrbitalAngle
     */
    protected void renderParentPlanet(final double curOrbitalAngle) {
        final double distanceToParent = this.curBody.getRelativeDistanceFromCenter().unScaledDistance;

        final double mainBodyOrbitalAngle = Math.PI - curOrbitalAngle;
        final double zIndex = (float) (20 / distanceToParent);
        final double distance = (float) (this.curBodyPlanet.getRelativeSize() / distanceToParent)
                * this.parentPlanetFactor;
        // my parent
        this.nearBodiesToRender
                .add(new BodyRenderTask(this.curBodyPlanet, mainBodyOrbitalAngle, zIndex, distance, curOrbitalAngle));
    }

    protected void renderRingsParentPlanet(final CelestialBody bodyToRender) {
        final double distanceToParent = this.curBody.getRelativeDistanceFromCenter().unScaledDistance;

        final double zIndex = (float) (20 / distanceToParent);
        final double distance = (float) (this.curBodyPlanet.getRelativeSize() / distanceToParent)
                * this.parentPlanetFactor;

        final BodyRenderTask task = new BodyRenderTask(
                bodyToRender,
                -this.currentCelestialAngle * PI_DOUBLE + Math.PI,
                zIndex,
                distance,
                this.currentCelestialAngle * PI_DOUBLE);

        this.nearBodiesToRender.add(task);
    }

    protected void renderMainStar() {
        final double distance = this.sunSize / this.curBodyDistance * this.parentSunFactor;

        this.farBodiesToRender.add(
                new BodyRenderTask(
                        this.curSystem.getMainStar(), // body
                        0.0D, // angle
                        this.curBodyDistance, // zIndex
                        distance, // scale
                        0.0D // phaseAngle
                )); // phaseAngle = 0 for the sun
    }

    protected double prepareSystemForRender(final long curWorldTime, final float partialTicks) {
        double curOrbitalAngle = 0;

        switch (this.rType) {
            case STAR ->
                // only child planets
                this.renderChildPlanets(curWorldTime, partialTicks);
            case PLANET -> {
                // star, sibling planets and child moons
                // get my own angle
                curOrbitalAngle = this.getOrbitalAngle(
                        this.curBodyPlanet.getRelativeDistanceFromCenter().unScaledDistance,
                        this.curBodyPlanet.getPhaseShift(),
                        curWorldTime,
                        partialTicks,
                        AstronomyHelper.yearFactor);
                this.renderMainStar();
                this.renderSiblingPlanets(curOrbitalAngle, curWorldTime, partialTicks);
                this.renderChildMoons(curWorldTime, partialTicks);
            }
            case MOON -> {
                curOrbitalAngle = this.getOrbitalAngle(
                        this.curBody.getRelativeOrbitTime(), // debug?
                        this.curBody.getPhaseShift(),
                        curWorldTime,
                        partialTicks,
                        AstronomyHelper.monthFactor);
                this.renderMainStar();
                this.renderParentPlanet(curOrbitalAngle);
                this.renderSiblingMoons(curOrbitalAngle, curWorldTime, partialTicks);
            }
            case RINGS -> {
                // do something similar to planet rendering. pretend we render being on the parent body.. kinda
                curOrbitalAngle = this.getOrbitalAngle(
                        this.curBodyPlanet.getRelativeDistanceFromCenter().unScaledDistance,
                        this.curBodyPlanet.getPhaseShift(),
                        curWorldTime,
                        partialTicks,
                        AstronomyHelper.yearFactor);
                this.renderMainStar();
                this.renderRingsParentPlanet(this.curBodyPlanet);
                this.renderSiblingMoons(curOrbitalAngle, curWorldTime, partialTicks);
            }
        }
        return curOrbitalAngle;
    }

    protected void renderSystem(final float partialTicks, final WorldClient world, final Tessellator tess,
            final Minecraft mc) {
        // assume we are at the position of the sun

        this.farBodiesToRender.clear();
        this.nearBodiesToRender.clear();
        GL11.glDisable(GL11.GL_TEXTURE_2D); // important, stuff flickers otherwise

        final long curWorldTime = world.getWorldTime();

        this.prepareSystemForRender(curWorldTime, partialTicks);

        Collections.sort(this.farBodiesToRender);
        Collections.sort(this.nearBodiesToRender);

        GL11.glPushMatrix();
        if (this.isAsteroidBelt) {

            if (!this.isAsteroidBeltMoon) {
                GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(90.0F, 0, 1.0F, 0);
            } else {
                // rotate back, so that the parent planet is at the same position
                GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
            }
        }

        // now do moons
        GL11.glPushMatrix();
        // try to rotate it
        if (!this.isAsteroidBelt) {
            GL11.glRotatef(planetAxisAngle, 0, 1.0F, 0);
        }

        GL11.glEnable(GL11.GL_BLEND);
        // actually render the stuff

        for (final BodyRenderTask task : this.farBodiesToRender) {
            this.renderPlanetByAngle(tess, task.body, task.angle, task.zIndex + 120.0F, task.scale, task.phaseAngle);

        }

        GL11.glPopMatrix();

        GL11.glPushMatrix();

        if (!this.isAsteroidBelt) {
            GL11.glRotatef(moonAxisAngle, 0, 1.0F, 0);
        }
        for (final BodyRenderTask task : this.nearBodiesToRender) {
            this.renderPlanetByAngle(tess, task.body, task.angle, task.zIndex + 100.0F, task.scale, task.phaseAngle);
        }
        GL11.glPopMatrix();

        GL11.glPopMatrix();
    }

    protected double getOrbitalAngle(final double relOrbitTime, final double phaseShift, final long worldTime,
            final double partialTicks, final double orbitFactor) {

        final double curYearLength = relOrbitTime * orbitFactor;
        final long j = worldTime % (long) curYearLength;
        final double orbitPos = (j + partialTicks) / curYearLength;// - 0.25F;
        return orbitPos * PI_DOUBLE + phaseShift;
    }

    private double getDistanceToBody(final double innerAngle, final double bodyDistance,
            final double otherBodyDistance) {
        return Math.sqrt(
                Math.pow(otherBodyDistance, 2) + Math.pow(bodyDistance, 2)
                        - 2 * otherBodyDistance * bodyDistance * Math.cos(innerAngle));
    }

    /**
     * Should convert an angle around the sun into an angle around this body
     * <p>
     *
     * @param innerAngle              in radians, the angle between curBody<-->sun and otherBody<-->sun
     * @param otherBodyDistance       other body's orbital radius
     * @param distFromThisToOtherBody
     * @return
     */
    private double projectAngle(final double innerAngle, final double otherBodyDistance,
            final double distFromThisToOtherBody, final double curBodyDistance) {
        // omg now do dark mathemagic

        final double sinBeta = Math.sin(innerAngle);

        // distFromThisToOtherBody = x
        // curBodyDistance = d
        // otherBodyDistance = r

        // gamma
        final double angleAroundCurBody = Math.asin(otherBodyDistance * sinBeta / distFromThisToOtherBody);

        if (curBodyDistance > otherBodyDistance) {
            return angleAroundCurBody;
        }

        // now fix this angle...
        // for this, I need the third angle, too
        final double delta = Math.asin(sinBeta / distFromThisToOtherBody * curBodyDistance);

        final double angleSum = innerAngle + delta + angleAroundCurBody;
        if (Math.abs(Math.abs(angleSum) / Math.PI - 1) < 0.001) {
            // aka angleSUm = 180 or -180
            return angleAroundCurBody;
        }
        return Math.PI - angleAroundCurBody;
    }

    protected void renderSunAura(final Tessellator tessellator1, final Vector3 color, final double size,
            final double brightness, double zIndex) {
        GL11.glPushMatrix();
        GL11.glShadeModel(GL11.GL_SMOOTH);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);

        // small sun aura START
        zIndex += 0.01F;

        float maxOpacity = (float) (brightness / 18.0F);
        if (maxOpacity > 1) {
            maxOpacity = 1.0F;
        }
        // maxOpacity = 0.4D;
        tessellator1.startDrawing(GL11.GL_TRIANGLE_FAN);
        tessellator1.setColorRGBA_F((float) color.x, (float) color.y, (float) color.z, maxOpacity);
        tessellator1.addVertex(0.0D, zIndex, 0.0D);
        // byte b0 = 16;
        tessellator1.setColorRGBA_F((float) color.x, (float) color.y, (float) color.z, 0.0F);

        // Render sun aura

        tessellator1.addVertex(-size, zIndex, -size);
        tessellator1.addVertex(0.0D, zIndex, -size * 1.5F);
        tessellator1.addVertex(size, zIndex, -size);
        tessellator1.addVertex(size * 1.5F, zIndex, 0.0D);
        tessellator1.addVertex(size, zIndex, size);
        tessellator1.addVertex(0.0D, zIndex, size * 1.5F);
        tessellator1.addVertex(-size, zIndex, size);
        tessellator1.addVertex(-size * 1.5F, zIndex, 0.0D);
        tessellator1.addVertex(-size, zIndex, -size);

        tessellator1.draw();

        GL11.glPopMatrix();
    }

    protected void renderRing(final Tessellator tessellator1, final RingsRenderInfo ringTexture, final double angle,
            double zIndex, final double scale, final double phaseAngle) {
        FMLClientHandler.instance().getClient().renderEngine.bindTexture(ringTexture.textureLocation);

        zIndex += 0.1;

        if (ringTexture.textureSize == null) {

            // THIS WORKS
            final int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
            final int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
            ringTexture.setTextureSize(width, height);
        }

        // now do stuff
        // we just rendered a planet with 2*scale edge length
        // this corresponds to the gapSize here
        double textureWidth;
        double textureHeight;
        double heightOffset = 0;
        double widthOffset = 0;
        final int gapSize = ringTexture.gapEnd - ringTexture.gapStart;
        final double scalingFactor = 2.0D * scale / gapSize;
        if (ringTexture.textureSize.x >= ringTexture.textureSize.y) {
            // failsafe
            if (ringTexture.textureSize.x <= gapSize) {
                return;
            }
            // width is leading
            // 2*scale / gapSize = actualWidth / textureWidth
            textureWidth = scalingFactor * ringTexture.textureSize.x;
            // x/y = width/height
            // y/x = h/w
            textureHeight = (double) ringTexture.textureSize.y / (double) ringTexture.textureSize.x * textureWidth;

            final double startOffsetScaled = ringTexture.gapStart * scalingFactor;

            widthOffset = startOffsetScaled - (textureWidth - 2.0D * scale) / 2.0D;

        } else {
            // failsafe
            if (ringTexture.textureSize.y <= gapSize) {
                return;
            }

            textureHeight = scalingFactor * ringTexture.textureSize.y;
            // x/y = w/h
            // w = h * x/y
            textureWidth = (double) ringTexture.textureSize.x / (double) ringTexture.textureSize.y * textureHeight;

            final double startOffsetScaled = ringTexture.gapStart * scalingFactor;

            heightOffset = startOffsetScaled - (textureHeight - 2.0D * scale) / 2.0D;
        }

        final double widthHalf = textureWidth / 2;
        final double heightHalf = textureHeight / 2;

        tessellator1.startDrawingQuads();
        tessellator1.setColorRGBA_F(1.0F, 1.0F, 1.0F, 1.0F);
        tessellator1.addVertexWithUV(-heightHalf + heightOffset, zIndex, widthHalf + widthOffset, 0, 0);
        tessellator1.addVertexWithUV(-heightHalf + heightOffset, zIndex, -widthHalf + widthOffset, 1, 0);
        tessellator1.addVertexWithUV(heightHalf + heightOffset, zIndex, -widthHalf + widthOffset, 1, 1);
        tessellator1.addVertexWithUV(heightHalf + heightOffset, zIndex, widthHalf + widthOffset, 0, 1);
        tessellator1.draw();

    }

    protected void renderPlanetByAngle(final Tessellator tessellator1, final CelestialBody body, final double angle,
            final double zIndex, final double scale, final double phaseAngle) {

        // at a scale of 0.15, the body is about 2x2 pixels
        // so this is rather generous, I think
        if (scale < 0.13D) {
            return;
        }

        boolean usePhaseOverlay = true;
        GL11.glPushMatrix();

        final double overlayScale = scale + 0.001D;

        // rotate on x
        GL11.glRotatef((float) (angle / Math.PI * 180), 1.0F, 0.0F, 0.0F);

        Vector3 color = AmunRa.config.sunColorMap.get(body.getName());
        if (body instanceof Star && color == null) {
            color = new Vector3(1.0F, 0.4F, 0.1F);
        }
        if (color != null) {
            this.renderSunAura(tessellator1, color, scale * 5.0D, scale, zIndex);
            usePhaseOverlay = false;
        }

        // actual planet
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(body.getBodyIcon());

        GL11.glTranslatef(0, 0, 0);

        tessellator1.startDrawingQuads();
        tessellator1.setColorRGBA_F(1.0F, 1.0F, 1.0F, 1.0F);
        tessellator1.addVertexWithUV(-scale, zIndex, -scale, 0, 0);
        tessellator1.addVertexWithUV(scale, zIndex, -scale, 1, 0);
        tessellator1.addVertexWithUV(scale, zIndex, scale, 1, 1);
        tessellator1.addVertexWithUV(-scale, zIndex, scale, 0, 1);
        tessellator1.draw();

        // actual planet END

        if (usePhaseOverlay) {
            this.drawPhaseOverlay(phaseAngle, body, scale + 0.01D, tessellator1, zIndex);
        }

        final RingsRenderInfo ringTex = AmunRa.config.ringMap.get(body.getName());
        if (ringTex != null) {
            this.renderRing(tessellator1, ringTex, angle, zIndex, overlayScale, phaseAngle);
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glPopMatrix();

    }

    private void drawPhaseOverlay(final double phaseAngle, final CelestialBody body, final double overlayScale,
            final Tessellator tessellator1, final double zIndex) {
        double startOffset = 0;
        double stopOffset = 0;

        /*
         * pi/2 => half waning pi => new 3/2pi => half waxing 0=2pi => full
         */
        boolean canBeBehindTheSun = body instanceof Planet && !body.equals(this.curBodyPlanet)
                && body.getRelativeDistanceFromCenter().unScaledDistance
                        > this.curBodyPlanet.getRelativeDistanceFromCenter().unScaledDistance;

        // canBeBehindTheSun might be true

        if (!canBeBehindTheSun || phaseAngle < PI_HALF || phaseAngle > PI_DOUBLE - PI_HALF) {
            // this means, body is behind the current body
            if (phaseAngle < Math.PI) {
                // larger angle -> smaller offset
                startOffset = overlayScale + (1 - phaseAngle / PI_HALF) * overlayScale;
            } else {
                // smaller ange -> larger offset
                stopOffset = overlayScale + (1 - (PI_DOUBLE - phaseAngle) / PI_HALF) * overlayScale;
            }

        } else if (phaseAngle < Math.PI) {
            // more phaseAngle -> largerStartOffset
            startOffset = phaseAngle / Math.PI * overlayScale * 2;
            // since stopOffset is substracted from the end coord, 0 is ok here
        } else {
            // since start is added, 0 should work here
            // more phaseAngle -> SMALLER stopOffset
            stopOffset = (Math.PI * 2 - phaseAngle) / Math.PI * overlayScale * 2;
        }

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.85F);

        tessellator1.startDrawingQuads();
        /*
         * Z stopOffset ^ V | D---C | | | | A---B | ^ | startOffset +------------------> X
         */
        final double length = 2.0D * overlayScale;
        final double texStartOffset = startOffset / length;
        final double texStopOffset = (length - stopOffset) / length;

        // length = 2*overlayScale
        // texStartOffset =

        // A
        tessellator1.addVertexWithUV(-overlayScale, zIndex + 0.01F, -overlayScale + startOffset, 0, texStartOffset);
        // B
        tessellator1.addVertexWithUV(overlayScale, zIndex + 0.01F, -overlayScale + startOffset, 1, texStartOffset);

        // C
        tessellator1.addVertexWithUV(overlayScale, zIndex + 0.01F, overlayScale - stopOffset, 1, texStopOffset);
        // D
        tessellator1.addVertexWithUV(-overlayScale, zIndex + 0.01F, overlayScale - stopOffset, 0, texStopOffset);
        tessellator1.draw();
    }

    private void prepareStars() {
        final Random rand = new Random(10842L);
        final Tessellator var2 = Tessellator.instance;
        var2.startDrawingQuads();

        for (int starIndex = 0; starIndex < 6000; ++starIndex) {
            double var4 = rand.nextFloat() * 2.0F - 1.0F;
            double var6 = rand.nextFloat() * 2.0F - 1.0F;
            double var8 = rand.nextFloat() * 2.0F - 1.0F;
            final double var10 = 0.15F + rand.nextFloat() * 0.1F;
            double var12 = var4 * var4 + var6 * var6 + var8 * var8;

            if (var12 < 1.0D && var12 > 0.01D) {
                var12 = 1.0D / Math.sqrt(var12);
                var4 *= var12;
                var6 *= var12;
                var8 *= var12;
                final double var14 = var4 * 100.0D;
                final double var16 = var6 * 100.0D;
                final double var18 = var8 * 100.0D;
                final double var20 = Math.atan2(var4, var8);
                final double var22 = Math.sin(var20);
                final double var24 = Math.cos(var20);
                final double var26 = Math.atan2(Math.sqrt(var4 * var4 + var8 * var8), var6);
                final double var28 = Math.sin(var26);
                final double var30 = Math.cos(var26);
                final double var32 = rand.nextDouble() * Math.PI * 2.0D;
                final double var34 = Math.sin(var32);
                final double var36 = Math.cos(var32);

                for (int var38 = 0; var38 < 4; ++var38) {
                    final double var39 = 0.0D;
                    final double var41 = ((var38 & 2) - 1) * var10;
                    final double var43 = ((var38 + 1 & 2) - 1) * var10;
                    final double var47 = var41 * var36 - var43 * var34;
                    final double var49 = var43 * var36 + var41 * var34;
                    final double var53 = var47 * var28 + var39 * var30;
                    final double var55 = var39 * var28 - var47 * var30;
                    final double var57 = var55 * var22 - var49 * var24;
                    final double var61 = var49 * var22 + var55 * var24;
                    var2.addVertex(var14 + var57, var16 + var53, var18 + var61);
                }
            }
        }

        var2.draw();
    }

    public float getSkyBrightness(final float par1) {
        final float var2 = FMLClientHandler.instance().getClient().theWorld.getCelestialAngle(par1);
        float var3 = 1.0F - (MathHelper.sin(var2 * (float) Math.PI * 2.0F) * 2.0F + 0.25F);

        if (var3 < 0.0F) {
            var3 = 0.0F;
        }

        if (var3 > 1.0F) {
            var3 = 1.0F;
        }

        return var3 * var3 * 1F;
    }
}
