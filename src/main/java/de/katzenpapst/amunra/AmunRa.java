package de.katzenpapst.amunra;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.command.CommandMothershipForceArrive;
import de.katzenpapst.amunra.command.CommandMothershipInfo;
import de.katzenpapst.amunra.command.CommandMoveMothership;
import de.katzenpapst.amunra.command.CommandShuttleTeleport;
import de.katzenpapst.amunra.config.ARConfig;
import de.katzenpapst.amunra.crafting.RecipeHelper;
import de.katzenpapst.amunra.entity.EntityCryoArrow;
import de.katzenpapst.amunra.entity.EntityLaserArrow;
import de.katzenpapst.amunra.entity.EntityOsirisBossFireball;
import de.katzenpapst.amunra.entity.spaceship.EntityShuttle;
import de.katzenpapst.amunra.entity.spaceship.EntityShuttleFake;
import de.katzenpapst.amunra.event.CraftingHandler;
import de.katzenpapst.amunra.event.EventHandlerAR;
import de.katzenpapst.amunra.event.FurnaceHandler;
import de.katzenpapst.amunra.helper.InteroperabilityHelper;
import de.katzenpapst.amunra.item.ARItems;
import de.katzenpapst.amunra.mob.RobotVillagerProfession;
import de.katzenpapst.amunra.mob.entity.EntityARVillager;
import de.katzenpapst.amunra.mob.entity.EntityAlienBug;
import de.katzenpapst.amunra.mob.entity.EntityMummyBoss;
import de.katzenpapst.amunra.mob.entity.EntityPorcodon;
import de.katzenpapst.amunra.mob.entity.EntityRobotVillager;
import de.katzenpapst.amunra.mob.entity.EntitySentry;
import de.katzenpapst.amunra.mothership.MothershipWorldProvider;
import de.katzenpapst.amunra.network.ARChannelHandler;
import de.katzenpapst.amunra.network.packet.ConnectionPacketAR;
import de.katzenpapst.amunra.proxy.ARSidedProxy;
import de.katzenpapst.amunra.tick.ConnectionEvents;
import de.katzenpapst.amunra.tick.TickHandlerServer;
import de.katzenpapst.amunra.tile.TileEntityARChest;
import de.katzenpapst.amunra.tile.TileEntityARChestLarge;
import de.katzenpapst.amunra.tile.TileEntityBlockScale;
import de.katzenpapst.amunra.tile.TileEntityBossDungeonSpawner;
import de.katzenpapst.amunra.tile.TileEntityGravitation;
import de.katzenpapst.amunra.tile.TileEntityHydroponics;
import de.katzenpapst.amunra.tile.TileEntityIsotopeGenerator;
import de.katzenpapst.amunra.tile.TileEntityMothershipController;
import de.katzenpapst.amunra.tile.TileEntityMothershipEngineBooster;
import de.katzenpapst.amunra.tile.TileEntityMothershipEngineBoosterIon;
import de.katzenpapst.amunra.tile.TileEntityMothershipEngineIon;
import de.katzenpapst.amunra.tile.TileEntityMothershipEngineJet;
import de.katzenpapst.amunra.tile.TileEntityMothershipSettings;
import de.katzenpapst.amunra.tile.TileEntityShuttleDock;
import de.katzenpapst.amunra.tile.TileEntityShuttleDockFake;
import de.katzenpapst.amunra.world.anubis.AnubisWorldProvider;
import de.katzenpapst.amunra.world.horus.HorusWorldProvider;
import de.katzenpapst.amunra.world.maahes.MaahesWorldProvider;
import de.katzenpapst.amunra.world.mehen.MehenWorldProvider;
import de.katzenpapst.amunra.world.neper.NeperWorldProvider;
import de.katzenpapst.amunra.world.seth.SethWorldProvider;
import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody.ScalableDistance;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import micdoodle8.mods.galacticraft.api.galaxies.Star;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.IAtmosphericGas;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.dimension.TeleportTypeMoon;
import micdoodle8.mods.galacticraft.core.dimension.TeleportTypeOverworld;
import micdoodle8.mods.galacticraft.core.dimension.TeleportTypeSpaceStation;
import micdoodle8.mods.galacticraft.core.items.GCItems;
import micdoodle8.mods.galacticraft.core.util.CreativeTabGC;
import micdoodle8.mods.galacticraft.planets.asteroids.dimension.TeleportTypeAsteroids;
import micdoodle8.mods.galacticraft.planets.asteroids.items.AsteroidsItems;

@Mod(
        modid = AmunRa.MODID,
        version = AmunRa.VERSION,
        dependencies = "required-after:GalacticraftCore@[3.0.61-GTNH,);" + "required-after:GalacticraftMars;"
                + "after:dreamcraft;"
                + "after:IronChest;"
                + "after:AdvancedSolarPanel",
        name = AmunRa.MODNAME)
public class AmunRa {

    public static final String MODID = "GalacticraftAmunRa";
    public static final String MODNAME = "Amun-Ra";
    public static final String VERSION = Tags.VERSION;

    public static ARChannelHandler packetPipeline;

    @Instance(AmunRa.MODID)
    public static AmunRa instance;

    public static final String ASSETPREFIX = "amunra";
    public static final String TEXTUREPREFIX = ASSETPREFIX + ":";

    public Star starRa = null;
    public Planet starAmun = null;
    public SolarSystem systemAmunRa = null;

    public Planet planetOsiris = null;
    public Planet planetHorus = null;
    public Planet planetBaal = null;
    public Planet planetAnubis = null;
    public Planet asteroidBeltMehen = null;
    public Planet planetSekhmet = null;

    public Moon moonBaalRings = null;
    public Moon moonKhonsu;
    public Moon moonNeper;
    public Moon moonIah;
    public Moon moonBastet;
    public Moon moonMaahes;
    public Moon moonThoth;
    public Moon moonSeth;

    public Moon moonKebe;

    public static CreativeTabs arTab;

    // protected BlockBasicMeta basicMultiBlock;
    private int nextID = 0;

    public static int chestRenderId;
    public static int msBoosterRendererId;
    public static int multiOreRendererId;
    public static int dummyRendererId;

    public static final ARConfig config = new ARConfig();

    protected ArrayList<ResourceLocation> possibleMothershipTextures = new ArrayList<>();
    protected ArrayList<ResourceLocation> possibleAsteroidTextures = new ArrayList<>();

    public static boolean isNHCoreLoaded;
    public static boolean isIronChestsLoaded;
    public static boolean isIronTanksLoaded;
    public static boolean isASPLoaded;

    @SidedProxy(
            clientSide = "de.katzenpapst.amunra.proxy.ClientProxy",
            serverSide = "de.katzenpapst.amunra.proxy.ServerProxy")
    public static ARSidedProxy proxy;

    public static final Logger LOGGER = LogManager.getLogger(MODNAME);

    @EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        isNHCoreLoaded = Loader.isModLoaded("dreamcraft");
        isIronChestsLoaded = Loader.isModLoaded("IronChest");
        isIronTanksLoaded = Loader.isModLoaded("irontank");
        isASPLoaded = Loader.isModLoaded("AdvancedSolarPanel");

        final Configuration configFile = new Configuration(event.getSuggestedConfigurationFile());

        config.processConfig(configFile);

        ARBlocks.initBlocks();
        ARItems.initItems();
        // this works for entityLivingEvent...
        MinecraftForge.EVENT_BUS.register(new EventHandlerAR());
        // ...but not for onCrafting.
        FMLCommonHandler.instance().bus().register(new CraftingHandler());
        GameRegistry.registerFuelHandler(new FurnaceHandler());

        this.possibleMothershipTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/mothership_icons/0.png"));
        this.possibleMothershipTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/mothership_icons/1.png"));
        this.possibleMothershipTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/mothership_icons/2.png"));
        this.possibleMothershipTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/mothership_icons/3.png"));
        this.possibleMothershipTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/mothership_icons/4.png"));
        this.possibleMothershipTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/mothership_icons/5.png"));
        this.possibleMothershipTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/mothership_icons/6.png"));
        this.possibleMothershipTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/mothership_icons/7.png"));

        this.possibleAsteroidTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/asteroid_icons/0.png"));
        this.possibleAsteroidTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/asteroid_icons/1.png"));
        this.possibleAsteroidTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/asteroid_icons/2.png"));
        this.possibleAsteroidTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/asteroid_icons/3.png"));
        this.possibleAsteroidTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/asteroid_icons/4.png"));
        this.possibleAsteroidTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/asteroid_icons/5.png"));
        this.possibleAsteroidTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/asteroid_icons/6.png"));
        this.possibleAsteroidTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/asteroid_icons/7.png"));
        this.possibleAsteroidTextures
                .add(new ResourceLocation(AmunRa.ASSETPREFIX, "textures/gui/asteroid_icons/8.png"));

        ConnectionPacketAR.bus = NetworkRegistry.INSTANCE.newEventDrivenChannel(ConnectionPacketAR.CHANNEL);
        ConnectionPacketAR.bus.register(new ConnectionPacketAR());

        FMLCommonHandler.instance().bus().register(new ConnectionEvents());

        proxy.preInit(event);
    }

    @SuppressWarnings("unchecked")
    public List<ResourceLocation> getPossibleMothershipTextures() {
        return (List<ResourceLocation>) this.possibleMothershipTextures.clone();
    }

    @SuppressWarnings("unchecked")
    public List<ResourceLocation> getPossibleAsteroidTextures() {
        return (List<ResourceLocation>) this.possibleAsteroidTextures.clone();
    }

    public void addPossibleMothershipTexture(final ResourceLocation loc) {
        this.possibleMothershipTextures.add(loc);
    }

    @EventHandler
    public void init(final FMLInitializationEvent event) {
        AmunRa.arTab = new CreativeTabGC(CreativeTabs.getNextID(), "AmunRaTab", ARItems.shuttleItem, 0);

        packetPipeline = ARChannelHandler.init();

        this.initCelestialBodies();
        this.initCreatures();
        this.registerTileEntities();
        this.initOtherEntities();
        RecipeHelper.initRecipes();

        proxy.init(event);
    }

    @EventHandler
    public void serverStarting(final FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandShuttleTeleport());
        event.registerServerCommand(new CommandMoveMothership());
        event.registerServerCommand(new CommandMothershipInfo());
        event.registerServerCommand(new CommandMothershipForceArrive());
    }

    @EventHandler
    public void serverInit(final FMLServerStartedEvent event) {
        TickHandlerServer.restart();
    }

    @EventHandler
    public void postInit(final FMLPostInitializationEvent event) {
        proxy.postInit(event);

        NetworkRegistry.INSTANCE.registerGuiHandler(AmunRa.instance, new GuiHandler());
        FMLCommonHandler.instance().bus().register(new TickHandlerServer());

        TileEntityMothershipEngineJet.jetFuel = FluidRegistry.getFluid(config.validJetEngineFuel);
        TileEntityMothershipEngineIon.coolant = FluidRegistry.getFluid(config.validIonThrusterCoolant);

        // failsafes
        this.doCompatibilityChecks();

        // mod compatibility
        InteroperabilityHelper.initCompatibility();
    }

    private void doCompatibilityChecks() {
        // sanity checks go here
        // verify crafting
        RecipeHelper.verifyNasaWorkbenchCrafting();

        // verify mothership provider ID
        config.verifyMothershipProviderId();

    }

    // stolen from GC....
    public int nextInternalID() {
        this.nextID++;
        return this.nextID - 1;
    }

    public void registerCreature(final Class<? extends Entity> entityClass, final String entityName,
            final int eggBgColor, final int eggFgColor) {
        final int newID = EntityRegistry.findGlobalUniqueEntityId();
        EntityRegistry.registerGlobalEntityID(entityClass, entityName, newID, eggBgColor, eggFgColor);
        EntityRegistry.registerModEntity(entityClass, entityName, this.nextInternalID(), AmunRa.instance, 80, 3, true);
    }

    public void registerNonMobEntity(final Class<? extends Entity> var0, final String var1, final int trackingDistance,
            final int updateFreq, final boolean sendVel) {
        EntityRegistry.registerModEntity(
                var0,
                var1,
                this.nextInternalID(),
                AmunRa.instance,
                trackingDistance,
                updateFreq,
                sendVel);
    }

    public void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityIsotopeGenerator.class, "AmunRa Atomic Battery");
        GameRegistry.registerTileEntity(TileEntityMothershipController.class, "AmunRa Mothership Controller");
        GameRegistry.registerTileEntity(TileEntityMothershipEngineJet.class, "AmunRa Mothership Engine");
        GameRegistry.registerTileEntity(TileEntityMothershipSettings.class, "AmunRa Mothership Settings");
        GameRegistry.registerTileEntity(TileEntityMothershipEngineBooster.class, "AmunRa Mothership Engine Booster");

        GameRegistry.registerTileEntity(TileEntityMothershipEngineIon.class, "AmunRa Mothership Ion Engine");
        GameRegistry
                .registerTileEntity(TileEntityMothershipEngineBoosterIon.class, "AmunRa Mothership Ion Engine Booster");

        GameRegistry.registerTileEntity(TileEntityBlockScale.class, "AmunRa Block Scale");

        GameRegistry.registerTileEntity(TileEntityShuttleDock.class, "AmunRa Shuttle Dock");
        GameRegistry.registerTileEntity(TileEntityShuttleDockFake.class, "AmunRa Shuttle Dock Fake");

        GameRegistry.registerTileEntity(TileEntityHydroponics.class, "AmunRa Hydroponics");
        GameRegistry.registerTileEntity(TileEntityGravitation.class, "AmunRa Gravity Engine");
        GameRegistry.registerTileEntity(TileEntityBossDungeonSpawner.class, "AmunRa Dungeon Spawner Osiris");

        GameRegistry.registerTileEntity(TileEntityARChest.class, "AmunRa Chest");
        GameRegistry.registerTileEntity(TileEntityARChestLarge.class, "AmunRa Chest Large");
    }

    protected void initCreatures() {
        this.registerCreature(EntityPorcodon.class, "porcodon", 0xff9d9d, 0x4fc451);

        this.registerCreature(EntityARVillager.class, "alienVillagerAR", 0x292233, 0xa38e36);
        this.registerCreature(EntityRobotVillager.class, "robotVillager", 0x626260, 0x141514);

        this.registerCreature(EntitySentry.class, "sentryRobot", 0x626260, 0x141514);

        this.registerCreature(EntityAlienBug.class, "alienBug", 0x40201e, 0x312c2b);

        this.registerCreature(EntityMummyBoss.class, "osirisBoss", 0x40201e, 0xffff0b);// ffff0b

        // register trading stuff
        this.registerTrading();

    }

    protected void initOtherEntities() {
        this.registerNonMobEntity(EntityLaserArrow.class, "laserArrow", 150, 5, true);
        this.registerNonMobEntity(EntityCryoArrow.class, "cryoArrow", 150, 5, true);
        this.registerNonMobEntity(EntityShuttle.class, "Shuttle", 150, 1, false);
        this.registerNonMobEntity(EntityShuttleFake.class, "ShuttleFake", 150, 5, false);
        this.registerNonMobEntity(EntityOsirisBossFireball.class, "cryoArrow", 150, 5, true);
    }

    protected void registerTrading() {
        RobotVillagerProfession.addProfession(
                new RobotVillagerProfession(
                        new ResourceLocation(GalacticraftCore.ASSET_PREFIX, "textures/blocks/electricFurnace.png"),
                        "furnace")
                                .addRecipe(Items.beef, 4, Items.cooked_beef)
                                .addRecipe(
                                        new ItemStack(Items.iron_axe, 1),
                                        new ItemStack(Items.emerald, 6),
                                        new ItemStack(Items.iron_ingot, 3))
                                .addRecipe(
                                        new ItemStack(Items.iron_door, 1),
                                        new ItemStack(Items.emerald, 12),
                                        new ItemStack(Items.iron_ingot, 6))
                                .addRecipe(
                                        new ItemStack(Items.iron_hoe, 1),
                                        new ItemStack(Items.emerald, 4),
                                        new ItemStack(Items.iron_ingot, 2))
                                .addRecipe(
                                        new ItemStack(Items.iron_pickaxe, 1),
                                        new ItemStack(Items.emerald, 6),
                                        new ItemStack(Items.iron_ingot, 3))
                                .addRecipe(
                                        new ItemStack(Items.iron_shovel, 1),
                                        new ItemStack(Items.emerald, 2),
                                        new ItemStack(Items.iron_ingot, 1)));

        final ItemStack emptyCan = new ItemStack(GCItems.oilCanister, 1, GCItems.oilCanister.getMaxDamage());

        // offers oxygen refill, and maybe other stuff, TBD
        RobotVillagerProfession.addProfession(
                new RobotVillagerProfession(
                        new ResourceLocation(GalacticraftCore.ASSET_PREFIX, "textures/blocks/machine_compressor_1.png"),
                        "compressor")
                                .addRecipe(
                                        emptyCan,
                                        new ItemStack(Items.emerald, 24),
                                        new ItemStack(AsteroidsItems.canisterLOX, 1, 1))
                                .addRecipe(
                                        emptyCan,
                                        new ItemStack(Items.emerald, 4),
                                        new ItemStack(AsteroidsItems.canisterLN2, 1, 1))
                                .addRecipe(new ItemStack(Items.emerald, 2), emptyCan)
                                .addRecipe(
                                        new ItemStack(GCItems.oxTankLight, 1, GCItems.oxTankLight.getMaxDamage()),
                                        new ItemStack(Items.emerald, 4),
                                        new ItemStack(GCItems.oxTankLight, 1))
                                .addRecipe(
                                        new ItemStack(GCItems.oxTankMedium, 1, GCItems.oxTankMedium.getMaxDamage()),
                                        new ItemStack(Items.emerald, 8),
                                        new ItemStack(GCItems.oxTankMedium, 1))
                                .addRecipe(
                                        new ItemStack(GCItems.oxTankHeavy, 1, GCItems.oxTankHeavy.getMaxDamage()),
                                        new ItemStack(Items.emerald, 16),
                                        new ItemStack(GCItems.oxTankHeavy, 1)));

        /*
         * can't make the battery work, because it resets on being crafted // register battery refill
         * RobotVillagerProfession.addProfession(new RobotVillagerProfession( new
         * ResourceLocation(GalacticraftCore.ASSET_PREFIX, "textures/blocks/coalGenerator.png"), "generator")
         * .addRecipe(new ItemStack(GCItems.battery, 1, GCItems.battery.getMaxDamage()), new ItemStack(Items.emerald, 8)
         * , new ItemStack(GCItems.battery, 1, 50)) );
         */
        RobotVillagerProfession.addProfession(
                new RobotVillagerProfession(
                        new ResourceLocation(GalacticraftCore.ASSET_PREFIX, "textures/blocks/refinery_front.png"),
                        "refinery")
                                .addRecipe(
                                        new ItemStack(GCItems.oilCanister, 1, 1),
                                        new ItemStack(Items.emerald, 16),
                                        new ItemStack(GCItems.fuelCanister, 1, 1))
                                .addRecipe(
                                        emptyCan,
                                        new ItemStack(Items.emerald, 26),
                                        new ItemStack(GCItems.fuelCanister, 1, 1)));
        RobotVillagerProfession.addProfession(
                new RobotVillagerProfession(
                        new ResourceLocation(GalacticraftCore.ASSET_PREFIX, "textures/blocks/electric_compressor.png"),
                        "ingotcompressor")
                                .addRecipe(
                                        new ItemStack(Items.iron_ingot, 2),
                                        new ItemStack(Items.emerald, 4),
                                        new ItemStack(GCItems.basicItem, 1, 11))// 11 = iron

                                .addRecipe(
                                        new ItemStack(GCItems.basicItem, 2, 5),
                                        new ItemStack(Items.emerald, 4),
                                        new ItemStack(GCItems.basicItem, 1, 8))// 8 = alu
                                .addRecipe(
                                        new ItemStack(GCItems.basicItem, 2, 4),
                                        new ItemStack(Items.emerald, 4),
                                        new ItemStack(GCItems.basicItem, 1, 7))// 7 = tin
                                .addRecipe(
                                        new ItemStack(GCItems.basicItem, 2, 3),
                                        new ItemStack(Items.emerald, 4),
                                        new ItemStack(GCItems.basicItem, 1, 6))// 6 = copper
        );

        RobotVillagerProfession.addProfession(
                new RobotVillagerProfession(
                        new ResourceLocation(GalacticraftCore.ASSET_PREFIX, "textures/blocks/circuit_fabricator.png"),
                        "circuitfabricator")
                                .addRecipe(
                                        new ItemStack(Items.dye, 1, 4),
                                        new ItemStack(Items.emerald, 4),
                                        new ItemStack(GCItems.basicItem, 9, 12))// solar thingys
                                .addRecipe(
                                        new ItemStack(Blocks.redstone_torch),
                                        new ItemStack(Items.emerald, 6),
                                        new ItemStack(GCItems.basicItem, 3, 13))// basic wafer
                                .addRecipe(
                                        new ItemStack(Items.repeater),
                                        new ItemStack(Items.emerald, 8),
                                        new ItemStack(GCItems.basicItem, 2, 14))// advanced wafer
                                .addRecipe(
                                        new ItemStack(Items.ender_pearl),
                                        new ItemStack(Items.emerald, 10),
                                        ARItems.baseItem.getItemStack("waferEnder", 1))// ender wafer

        );

        /*
         * RobotVillagerProfession.addProfession(new RobotVillagerProfession( new ResourceLocation(AmunRa.ASSETPREFIX,
         * "textures/blocks/crafter.png"), "crafter") .addRecipe(new ItemStack(Items.dye, 1, 4), new
         * ItemStack(Items.emerald, 4), new ItemStack(GCItems.basicItem, 9, 12)) );
         */
    }

    protected void initCelestialBodies() {

        this.systemAmunRa = new SolarSystem("systemAmunRa", "milkyWay");
        this.starRa = new Star("starRa");
        this.systemAmunRa.setMainStar(this.starRa).setMapPosition(new Vector3(3.0F, -1.5F, 0.0F));
        GalaxyRegistry.registerSolarSystem(this.systemAmunRa);

        this.starRa.setBodyIcon(new ResourceLocation(ASSETPREFIX, "textures/gui/celestialbodies/sun-red2.png"));
        this.starRa.setParentSolarSystem(this.systemAmunRa);

        this.starAmun = this.createPlanet("starAmun", "sun-blue.png", Math.PI * 0.1, 0.7, 0.9);
        this.starAmun.setRelativeSize(3.0F);
        this.starAmun.setParentSolarSystem(this.systemAmunRa);
        GalaxyRegistry.registerPlanet(this.starAmun);

        // two inner planets
        this.planetOsiris = this.createPlanet("osiris", "planet-mercury.png", Math.PI * 0.8, 0.34, 0.4);
        this.planetOsiris.setParentSolarSystem(this.systemAmunRa);
        this.planetOsiris.setRelativeSize(0.8F);
        GalaxyRegistry.registerPlanet(this.planetOsiris);

        this.planetHorus = this.createPlanet("horus", "planet-horus.png", Math.PI * 1.3, 0.55, 0.458);
        this.planetHorus.setRelativeSize(1.05F);
        this.planetHorus.setParentSolarSystem(this.systemAmunRa);
        this.planetHorus.setDimensionInfo(config.dimHorus, HorusWorldProvider.class);
        GalacticraftRegistry.registerTeleportType(HorusWorldProvider.class, new TeleportTypeMoon());
        this.planetHorus.setTierRequired(config.planetDefaultTier);
        GalaxyRegistry.registerPlanet(this.planetHorus);

        // gas giant
        this.planetBaal = this.createPlanet("baal", "planet-gas03.png", Math.PI * 1.9, 1.2, 1.4);
        this.planetBaal.setParentSolarSystem(this.systemAmunRa);
        this.planetBaal.setRelativeSize(2.2F);
        GalaxyRegistry.registerPlanet(this.planetBaal);

        // .. and its moons
        // ring, aka innermost moon
        // the regular moon has a distance of 13
        this.moonBaalRings = this.createMoon("baalRings", "micromoon.png", 1.58, 9, 100);
        this.moonBaalRings.setParentPlanet(this.planetBaal);
        GalaxyRegistry.registerMoon(this.moonBaalRings);

        // moon god, but something to do with the creation of life? so maybe stuff here as well
        this.moonKhonsu = this.createMoon("khonsu", "moon.png", 1.9 * Math.PI, 12.45, 110);
        this.moonKhonsu.setParentPlanet(this.planetBaal);
        this.moonKhonsu.setRelativeSize(0.45F);
        GalaxyRegistry.registerMoon(this.moonKhonsu);

        // this will have an oxygen atmosphere. neper was some kind of a grain god, so
        this.moonNeper = this.createMoon("neper", "planet-life-o2.png", 1.58, 14.9, 140);
        this.moonNeper.atmosphere.add(IAtmosphericGas.NITROGEN);
        this.moonNeper.atmosphere.add(IAtmosphericGas.OXYGEN);
        this.moonNeper.atmosphere.add(IAtmosphericGas.ARGON);
        this.moonNeper.atmosphere.add(IAtmosphericGas.HELIUM);
        this.moonNeper.setDimensionInfo(config.dimNeper, NeperWorldProvider.class);
        this.moonNeper.setParentPlanet(this.planetBaal);
        this.moonNeper.setTierRequired(config.planetDefaultTier);
        this.moonNeper.setRelativeSize(0.89F);
        GalacticraftRegistry.registerTeleportType(NeperWorldProvider.class, new TeleportTypeOverworld());
        // GalacticraftRegistry.registerTeleportType(WorldProviderMoon.class, new TeleportTypeMoon());
        // GalacticraftRegistry.registerTeleportType(WorldProviderSurface.class, new TeleportTypeOverworld());
        GalaxyRegistry.registerMoon(this.moonNeper);

        // just some dead rock. iah was a moon god
        this.moonIah = this.createMoon("iah", "moon.png", 3.1, 18.5, 162);
        this.moonIah.setParentPlanet(this.planetBaal);
        this.moonIah.setRelativeSize(0.21F);
        GalaxyRegistry.registerMoon(this.moonIah);

        // an asteroid belt. todo figure the other stuff out later
        this.asteroidBeltMehen = this.createPlanet("asteroidBeltMehen", "micromoon.png", Math.PI * 0.19, 1.4, 1.6);
        this.asteroidBeltMehen.setParentSolarSystem(this.systemAmunRa);
        this.asteroidBeltMehen.setDimensionInfo(config.dimMehen, MehenWorldProvider.class);
        this.asteroidBeltMehen.setTierRequired(config.planetDefaultTier);
        GalacticraftRegistry.registerTeleportType(MehenWorldProvider.class, new TeleportTypeAsteroids());
        GalaxyRegistry.registerPlanet(this.asteroidBeltMehen);

        // another gas giant?
        this.planetSekhmet = this.createPlanet("sekhmet", "planet-gas02.png", Math.PI * 0.6, 1.6, 1.8);
        this.planetSekhmet.setParentSolarSystem(this.systemAmunRa);
        this.planetSekhmet.setRelativeSize(2.42F);
        GalaxyRegistry.registerPlanet(this.planetSekhmet);

        // ... and it's moons
        // cat goddess, of course it's a moon of sekhmet
        this.moonBastet = this.createMoon("bast", "moon.png", 3.1, 9.8, 122);
        this.moonBastet.setParentPlanet(this.planetSekhmet);
        this.moonBastet.setRelativeSize(0.758F);
        GalaxyRegistry.registerMoon(this.moonBastet);

        // lion goddess, dito
        this.moonMaahes = this.createMoon("maahes", "planet-life-ch4.png", 4.514, 11.4, 136);
        this.moonMaahes.setRelativeSize(0.912F);
        this.moonMaahes.setParentPlanet(this.planetSekhmet);
        this.moonMaahes.atmosphere.add(IAtmosphericGas.CO2);
        this.moonMaahes.atmosphere.add(IAtmosphericGas.METHANE);
        this.moonMaahes.atmosphere.add(IAtmosphericGas.HYDROGEN);
        this.moonMaahes.atmosphere.add(IAtmosphericGas.ARGON);
        this.moonMaahes.setDimensionInfo(config.dimMaahes, MaahesWorldProvider.class);
        this.moonMaahes.setTierRequired(config.planetDefaultTier);
        GalacticraftRegistry.registerTeleportType(MaahesWorldProvider.class, new TeleportTypeOverworld());

        GalaxyRegistry.registerMoon(this.moonMaahes);

        this.moonThoth = this.createMoon("thoth", "moon.png", 1.9, 15.5, 145);
        this.moonThoth.setRelativeSize(0.68F);
        this.moonThoth.setParentPlanet(this.planetSekhmet);
        GalaxyRegistry.registerMoon(this.moonThoth);

        // this will be the ice ocean moon now
        this.moonSeth = this.createMoon("seth", "planet-ice2.png", 6, 17.98, 198);
        this.moonSeth.setRelativeSize(0.457F);
        this.moonSeth.setParentPlanet(this.planetSekhmet);
        // moonSeth.atmosphere.add(IAtmosphericGas.NITROGEN);
        this.moonSeth.setDimensionInfo(config.dimSeth, SethWorldProvider.class);
        this.moonSeth.setTierRequired(config.planetDefaultTier);
        GalacticraftRegistry.registerTeleportType(SethWorldProvider.class, new TeleportTypeMoon());
        GalaxyRegistry.registerMoon(this.moonSeth);

        // a small rocky planet
        this.planetAnubis = this.createPlanet("anubis", "moon.png", Math.PI * 0.36, 1.9, 2.2);
        this.planetAnubis.setParentSolarSystem(this.systemAmunRa);
        this.planetAnubis.setDimensionInfo(config.dimAnubis, AnubisWorldProvider.class);
        this.planetAnubis.setRelativeSize(0.65F);
        GalacticraftRegistry.registerTeleportType(AnubisWorldProvider.class, new TeleportTypeMoon());
        this.planetAnubis.setTierRequired(config.planetDefaultTier);
        GalaxyRegistry.registerPlanet(this.planetAnubis);

        // ..with a moon nonetheless
        this.moonKebe = this.createMoon("kebe", "moon.png", 5.1, 19, 253);
        this.moonKebe.setRelativeSize(0.32F);
        this.moonKebe.setParentPlanet(this.planetAnubis);
        GalaxyRegistry.registerMoon(this.moonKebe);

        // For motherships:
        final boolean flag = DimensionManager
                .registerProviderType(config.mothershipProviderID, MothershipWorldProvider.class, false);
        if (!flag) {
            throw new RuntimeException(
                    "Could not register provider mothership provider ID. Please change I:mothershipProviderID in the config.");
        }
        GalacticraftRegistry.registerTeleportType(MothershipWorldProvider.class, new TeleportTypeSpaceStation());

        // default stuff
        config.setStaticConfigValues();
    }

    protected Planet createPlanet(final String name, final String texture, final double phaseShift,
            final double distance, final double orbitTime) {
        final Planet pl = new Planet(name);
        this.setCelestialBodyStuff(pl, texture, phaseShift, distance, orbitTime);
        return pl;
    }

    protected Moon createMoon(final String name, final String texture, final double phaseShift, final double distance,
            final double orbitTime) {
        final Moon pl = new Moon(name);
        this.setCelestialBodyStuff(pl, texture, phaseShift, distance, orbitTime);
        return pl;
    }

    protected void setCelestialBodyStuff(final CelestialBody body, final String texture, final double phaseShift,
            final double distance, final double orbitTime) {
        body.setBodyIcon(new ResourceLocation(ASSETPREFIX, "textures/gui/celestialbodies/" + texture))
                .setPhaseShift((float) phaseShift)
                .setRelativeDistanceFromCenter(new ScalableDistance((float) distance, (float) distance))
                .setRelativeOrbitTime((float) orbitTime);
    }

    public static boolean isDevEnvironment() {
        return (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    }

    /*
     * @SideOnly(Side.CLIENT) public void setClientMothershipData(MothershipWorldData data) { mothershipDataClient =
     * data; } public MothershipWorldData getMothershipData() { if(FMLCommonHandler.instance().getSide() == Side.CLIENT)
     * { return this.mothershipDataClient; } return TickHandlerServer.mothershipData; }
     */
}
