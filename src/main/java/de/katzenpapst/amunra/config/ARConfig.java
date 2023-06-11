package de.katzenpapst.amunra.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.client.RingsRenderInfo;
import de.katzenpapst.amunra.helper.AstronomyHelper;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.planets.asteroids.AsteroidsModule;

public class ARConfig {

    // ** dimension IDs **
    public int dimNeper = 20;
    public int dimMaahes = 21;
    public int dimAnubis = 22;
    public int dimHorus = 23;
    public int dimSeth = 24;
    public int dimMehen = 25;

    // default tier for my planets and moons
    public int planetDefaultTier = 3;

    public boolean villageAdvancedMachines = false;

    // ** motherships **
    public int maxNumMotherships = -1;
    public int mothershipMaxTier = 10;
    public int mothershipProviderID = -39;

    // motherships will refuse to start transit, if the time is > than this
    public int mothershipMaxTravelTime = 24000;

    public float mothershipSpeedFactor = 1.0F;

    public float mothershipFuelFactor = 1.0F;

    // bodies which motherships cannot orbit
    public Set<String> mothershipBodiesNoOrbit;

    public String validJetEngineFuel;
    public String validIonThrusterCoolant;

    // *** sky rendering and related ***
    // bodies not to render
    public Set<String> bodiesNoRender;

    public Set<String> asteroidBeltBodies;

    // star lines for transit sky
    public int mothershipNumStarLines = 400;

    public int numAsteroids = 600;

    // bodies to render as suns
    public Map<String, Vector3> sunColorMap = new HashMap<>();

    public Map<String, RingsRenderInfo> ringMap = new HashMap<>();

    // ** IDs **
    public int schematicIdShuttle = 11;

    public int guiIdShuttle = 8;

    public float hydroponicsFactor = 1.0F;

    // ** extra default stuff **
    private final String[] defaultExtraSuns = { "tbn36b:0/0.1/1", "selpin:0/0.1/1", "tbn36a:1/0/0",
            "centaurib:1/0.7/0.8", "vega:0.8/0.8/1", "sirius:0.6/0.8/1", "siriusb:1/1/1", "dark:0.1/0.1/0.1",
            "kapteyn:0.70/0.1/0.1" };

    private final String[] defaultPlanetsWithRings = {
            "barnarda5:171:301:galaxyspace:textures/gui/celestialbodies/barnardaRings.png",
            "barnarda6:177:305:galaxyspace:textures/gui/celestialbodies/barnardaRings2.png",
            "appleapachia:8:20:extendedplanets:textures/gui/celestialbodies/appleapachiaRings.png" };

    private final String[] defaultAsteroidBelts = { "okblekbelt", "saturnrings" };

    public boolean generateOres = false;

    // public boolean mothershipUserRestriction = true;

    public ARConfig() {}

    public void processConfig(final Configuration config) {

        config.load();
        final String[] emptySet = {};

        // Configuration goes here.
        // config.getInt(name, category, defaultValue, minValue, maxValue, comment)
        this.dimNeper = config.get("dimension_ids", "Neper", this.dimNeper).getInt();
        this.dimMaahes = config.get("dimension_ids", "Maahes", this.dimMaahes).getInt();
        this.dimAnubis = config.get("dimension_ids", "Anubis", this.dimAnubis).getInt();
        this.dimHorus = config.get("dimension_ids", "Horus", this.dimHorus).getInt();
        this.dimSeth = config.get("dimension_ids", "Seth", this.dimSeth).getInt();
        this.dimMehen = config.get("dimension_ids", "Mehen", this.dimMehen).getInt();

        // villages
        this.villageAdvancedMachines = config
                .get(
                        "villages",
                        "UseAdvancedMachines",
                        false,
                        "If true, robot villages will have advanced solar collectors, storage clusters and heavy wires")
                .getBoolean();

        // general
        this.planetDefaultTier = config.getInt(
                "default_tier",
                "general",
                this.planetDefaultTier,
                0,
                1000,
                "Default tier for AmunRa planets and moons");

        this.hydroponicsFactor = config.getFloat(
                "hydroponicsFactor",
                "general",
                this.hydroponicsFactor,
                Float.MIN_VALUE,
                Float.MAX_VALUE,
                "Multiplier for the oxygen production of the hydroponics unit");

        this.generateOres = config.getBoolean(
                "generateOres",
                "general",
                this.generateOres,
                "If ores should be generated on planets/moons");

        // motherships
        this.maxNumMotherships = config.getInt(
                "numMothershipsPerPlayer",
                "motherships",
                this.maxNumMotherships,
                -1,
                1000,
                "Maximal amount of motherships one single player can have. Set to -1 to remove the restriction.");

        this.mothershipProviderID = config.getInt(
                "mothershipProviderID",
                "motherships",
                this.mothershipProviderID,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                "ID for the Mothership World Provider");

        this.mothershipMaxTier = config.getInt(
                "maxMothershipTier",
                "motherships",
                this.mothershipMaxTier,
                1,
                Integer.MAX_VALUE,
                "Maximal tier which can be reached from a mothership. Motherships will pretty much ignore the tier system otherwise.");

        this.mothershipMaxTravelTime = config.getInt(
                "maxMothershipTravelTime",
                "motherships",
                this.mothershipMaxTravelTime,
                1,
                Integer.MAX_VALUE,
                "Maximal travel time (in ticks) for a mothership. Destinations with a longer travel time are unreachable. 24000 = one Overworld day");

        this.mothershipSpeedFactor = config.getFloat(
                "mothershipSpeedFactor",
                "motherships",
                this.mothershipSpeedFactor,
                Float.MIN_VALUE,
                Float.MAX_VALUE,
                "A factor to be multiplied onto the mothership speed. Higher values = faster motherships.");

        this.mothershipFuelFactor = config.getFloat(
                "mothershipFuelFactor",
                "motherships",
                this.mothershipFuelFactor,
                Float.MIN_VALUE,
                Float.MAX_VALUE,
                "A factor to be multiplied onto the fuel usages of mothership engines. Higher values = higher fuel usage");

        this.mothershipBodiesNoOrbit = this.configGetStringHashSet(
                config,
                "bodiesNoOrbit",
                "motherships",
                emptySet,
                "Bodies which should not be orbitable by motherships");

        this.validJetEngineFuel = config
                .getString("validJetEngineFuel", "motherships", "fuel", "This fluid can be used by Jet Engines");

        this.validIonThrusterCoolant = config.getString(
                "validIonThrusterCoolant",
                "motherships",
                "liquidnitrogen",
                "This fluid can be used by Ion Thrusters");

        // mothershipUserRestriction = config.getBoolean("restrictMothershipToOwner", "mothership", true, "If true, only
        // the one who built the mothership will be able to use it. If false, anyone can");

        // rendering
        this.mothershipNumStarLines = config.getInt(
                "mothershipStarLines",
                "rendering",
                this.mothershipNumStarLines,
                0,
                Integer.MAX_VALUE,
                "Number of speed lines to display while in transit. A lower number might improve performance, while a higher might look nicer.");

        this.numAsteroids = config.getInt(
                "numAsteroids",
                "rendering",
                this.numAsteroids,
                0,
                Integer.MAX_VALUE,
                "Approximate number of asteroids drawn in the sky when 'orbiting' an asteroid belt.");

        // excluded bodies
        this.bodiesNoRender = this.configGetStringHashSet(
                config,
                "skyRenderExclude",
                "rendering",
                emptySet,
                "Names of bodies to exclude from rendering in the sky, for reasons other than being asteroid belts");

        // asteroidBeltBodies
        this.asteroidBeltBodies = this.configGetStringHashSet(
                config,
                "asteroidBelts",
                "rendering",
                this.defaultAsteroidBelts,
                "Names of bodies to be considered asteroid belts. These values are automatically added to skyRenderExclude, so it is not necessary to add them to both.");

        // suns

        final String[] sunData = config.getStringList(
                "additionalSuns",
                "rendering",
                this.defaultExtraSuns,
                "Additional bodies to render with a colored aura, or set the aura of a specific star. \nThe bodies in here will be considered stars on motherships as well. \nFormat: '<bodyName>:<r>/<g>/<b>' with the colors as floats between 0 and 1. \nExample: 'myPlanet:1/0.6/0.1'");
        for (final String str : sunData) {
            final String[] parts1 = str.split(":", 2);
            if (parts1.length < 2) {
                AmunRa.LOGGER.warn("'{}' is not a valid sun configuration", str);
                continue;
            }
            final String body = parts1[0];
            final String color = parts1[1];

            final String[] parts2 = color.split("/", 3);
            if (parts2.length < 3) {
                continue;
            }

            final Vector3 colorVec = new Vector3(
                    Double.parseDouble(parts2[0]),
                    Double.parseDouble(parts2[1]),
                    Double.parseDouble(parts2[2]));

            this.sunColorMap.put(body, colorVec);

        }

        // rings

        final String[] ringData = config.getStringList(
                "planetsWithRings",
                "rendering",
                this.defaultPlanetsWithRings,
                "Bodies to render with rings. \nThe format is: <bodyName>:<gapStart>:<gapEnd>:<Mod_Asset_Prefix>:<textureName>. \nThe 'gapStart' and 'gapEnd' is the number of pixels from the left or the top to the start of the gap for the planet and the end, respectively. \nExample: 'uranus:8:20:galacticraftcore:textures/gui/celestialbodies/uranusRings.png'");
        for (final String str : ringData) {
            final String[] parts1 = str.split(":", 5);
            if (parts1.length < 5) {
                AmunRa.LOGGER.warn("'{}' is not a valid ring configuration", str);
                continue;
            }
            final String body = parts1[0];
            final int gapStart = Integer.parseInt(parts1[1]);
            final int gapEnd = Integer.parseInt(parts1[2]);
            final String assetPrefix = parts1[3];
            final String textureName = parts1[4];

            if (gapStart <= 0 || gapEnd <= 0 || gapEnd <= gapStart) {
                AmunRa.LOGGER.warn("'{}' is not a valid ring configuration", str);
                continue;
            }

            this.ringMap
                    .put(body, new RingsRenderInfo(new ResourceLocation(assetPrefix, textureName), gapStart, gapEnd));
        }
        //

        // schematics
        this.schematicIdShuttle = config.getInt(
                "shuttleSchematicsId",
                "schematics",
                this.schematicIdShuttle,
                6,
                Integer.MAX_VALUE,
                "ID of the Shuttle schematics, must be unique. 0-5 are used by Galacticraft already.");

        this.guiIdShuttle = config.getInt(
                "shuttleGuiId",
                "schematics",
                this.guiIdShuttle,
                8,
                Integer.MAX_VALUE,
                "ID of the Shuttle schematics GUI, must be unique. 0-7 are used by Galacticraft already.");

        // config.get

        // confMaxMothershipTier

        config.save();
    }

    public NBTTagCompound getServerOverrideData() {
        final NBTTagCompound data = new NBTTagCompound();

        // now what do I need?
        // - not the dim IDs
        // - not the client stuff

        data.setInteger("maxNumMotherships", this.maxNumMotherships);

        // data.set
        final NBTTagList bodiesNoList = new NBTTagList();
        for (final String s : this.mothershipBodiesNoOrbit) {
            final NBTTagString strTag = new NBTTagString(s);
            // strTag.func_150285_a_();
            bodiesNoList.appendTag(strTag);
        }
        data.setTag("msBodiesNoOrbit", bodiesNoList);

        data.setInteger("msMaxTier", this.mothershipMaxTier);
        data.setInteger("msMaxTravelTime", this.mothershipMaxTravelTime);
        data.setFloat("msFuelFactor", this.mothershipFuelFactor);
        data.setFloat("msSpeedFactor", this.mothershipSpeedFactor);
        data.setInteger("planetDefaultTier", this.planetDefaultTier);

        return data;
    }

    public void setServerOverrideData(final NBTTagCompound data) {
        this.maxNumMotherships = data.getInteger("maxNumMotherships");

        final NBTTagList bodiesNoList = data
                .getTagList("msBodiesNoOrbit", net.minecraftforge.common.util.Constants.NBT.TAG_STRING);
        this.mothershipBodiesNoOrbit.clear();
        for (int i = 0; i < bodiesNoList.tagCount(); i++) {
            final String strData = bodiesNoList.getStringTagAt(i);
            this.mothershipBodiesNoOrbit.add(strData);
        }

        this.mothershipMaxTier = data.getInteger("msMaxTier");
        this.mothershipMaxTravelTime = data.getInteger("msMaxTravelTime");
        this.mothershipFuelFactor = data.getFloat("msFuelFactor");
        this.mothershipSpeedFactor = data.getFloat("msSpeedFactor");
        this.planetDefaultTier = data.getInteger("planetDefaultTier");
    }

    /**
     * Add some things to the config which should always be in there
     */
    public void setStaticConfigValues() {

        this.asteroidBeltBodies.add(AmunRa.instance.asteroidBeltMehen.getName());
        this.asteroidBeltBodies.add(AmunRa.instance.moonBaalRings.getName());
        this.asteroidBeltBodies.add(AsteroidsModule.planetAsteroids.getName());

        this.bodiesNoRender.addAll(this.asteroidBeltBodies);
        // suns
        this.sunColorMap.put(AmunRa.instance.starAmun.getName(), new Vector3(0.0D, 0.2D, 0.7D));

        // rings. do not override config settings, though
        // the actual planets from GCCore don't even exist at this point oO
        if (!this.ringMap.containsKey("uranus")) {
            this.ringMap.put(
                    "uranus",
                    new RingsRenderInfo(
                            new ResourceLocation(
                                    GalacticraftCore.ASSET_PREFIX,
                                    "textures/gui/celestialbodies/uranusRings.png"),
                            8,
                            20));
        }
        if (!this.ringMap.containsKey("saturn")) {
            this.ringMap.put(
                    "saturn",
                    new RingsRenderInfo(
                            new ResourceLocation(
                                    GalacticraftCore.ASSET_PREFIX,
                                    "textures/gui/celestialbodies/saturnRings.png"),
                            9,
                            21));
        }
    }

    public boolean isSun(final CelestialBody body) {
        return this.sunColorMap.containsKey(body.getName());
    }

    public boolean isAsteroidBelt(final CelestialBody body) {
        return this.asteroidBeltBodies.contains(body.getName());
    }

    private HashSet<String> configGetStringHashSet(final Configuration config, final String name, final String category,
            final String[] defaultValues, final String comment) {
        final String[] data = config.getStringList(name, category, defaultValues, comment);
        final HashSet<String> result = new HashSet<>();
        Collections.addAll(result, data);
        return result;
    }

    /**
     * Looks for collisions between mothershipProviderID and any dimension ID
     */
    public void verifyMothershipProviderId() {
        final CelestialBody body = GalaxyRegistry.getCelestialBodyFromDimensionID(this.mothershipProviderID);

        if (body != null) {
            final String bodyName = AstronomyHelper.getDebugBodyName(body);
            throw new RuntimeException(
                    "Please change \"mothershipProviderID\" in the config file. " + this.mothershipProviderID
                            + " is already in use by "
                            + bodyName);
        }
    }

}
