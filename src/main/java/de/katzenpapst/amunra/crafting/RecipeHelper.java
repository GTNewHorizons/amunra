package de.katzenpapst.amunra.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import cpw.mods.fml.common.registry.GameRegistry;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.block.BlockStairsAR;
import de.katzenpapst.amunra.block.ore.BlockOreMulti;
import de.katzenpapst.amunra.block.ore.SubBlockOre;
import de.katzenpapst.amunra.entity.spaceship.EntityShuttle;
import de.katzenpapst.amunra.item.ARItems;
import de.katzenpapst.amunra.item.ItemDamagePair;
import de.katzenpapst.amunra.schematic.SchematicPageShuttle;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.api.recipe.CircuitFabricatorRecipes;
import micdoodle8.mods.galacticraft.api.recipe.CompressorRecipes;
import micdoodle8.mods.galacticraft.api.recipe.INasaWorkbenchRecipe;
import micdoodle8.mods.galacticraft.api.recipe.ISchematicPage;
import micdoodle8.mods.galacticraft.api.recipe.SchematicRegistry;
import micdoodle8.mods.galacticraft.api.recipe.SpaceStationRecipe;
import micdoodle8.mods.galacticraft.core.blocks.BlockAirLockFrame;
import micdoodle8.mods.galacticraft.core.blocks.GCBlocks;
import micdoodle8.mods.galacticraft.core.energy.item.ItemElectricBase;
import micdoodle8.mods.galacticraft.core.items.GCItems;
import micdoodle8.mods.galacticraft.core.recipe.NasaWorkbenchRecipe;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import micdoodle8.mods.galacticraft.planets.asteroids.items.AsteroidsItems;

public class RecipeHelper {

    public static SpaceStationRecipe mothershipRecipe;

    protected static Map<Item, Vector<INasaWorkbenchRecipe>> nasaWorkbenchRecipes = new HashMap<>();

    protected static List<CircuitFabricatorRecipe> circuitFabricatorRecipes = new ArrayList<>();

    public RecipeHelper() {}

    public static void initRecipes() {

        initNasaWorkbenchCrafting();

        if (AmunRa.isNHCoreLoaded) {
            return;
        }

        final ItemStack freqModuleStack = new ItemStack(GCItems.basicItem, 1, 19);
        final ItemStack enderWaferStack = ARItems.waferEnder.getItemStack(1);
        final ItemStack lithiumMeshStack = ARItems.lithiumMesh.getItemStack(1);
        final ItemStack uranMeshStack = ARItems.uraniumMesh.getItemStack(1);
        final ItemStack lithiumGemStack = ARItems.lithiumGem.getItemStack(1);
        final ItemStack compressedAluStack = new ItemStack(GCItems.basicItem, 1, 8);
        final ItemStack compressedIronStack = new ItemStack(GCItems.basicItem, 1, 11);
        final ItemStack compressedTinStack = new ItemStack(GCItems.basicItem, 1, 7);
        final ItemStack compressedSteelStack = new ItemStack(GCItems.basicItem, 1, 9);
        final ItemStack compressedTitaniumStack = new ItemStack(AsteroidsItems.basicItem, 1, 6);
        final ItemStack button = new ItemStack(Item.getItemFromBlock(Blocks.stone_button), 1);
        final ItemStack laserDiodeStack = ARItems.laserDiode.getItemStack(1);
        final ItemStack cryoDiodeStack = ARItems.cryoDiode.getItemStack(1);
        final ItemStack beamCore = new ItemStack(AsteroidsItems.basicItem, 1, 8);
        final ItemStack waferSolar = new ItemStack(GCItems.basicItem, 1, 12);
        final ItemStack waferAdvanced = new ItemStack(GCItems.basicItem, 1, 14);
        final ItemStack thermalControllerStack = ARItems.thermalControl.getItemStack(1);
        final ItemStack thermalStuff = new ItemStack(AsteroidsItems.basicItem, 1, 7); // thermal cloth
        final ItemStack heavyWire = new ItemStack(GCBlocks.aluminumWire, 1, 1);

        final ItemStack tinCanStack = new ItemStack(GCItems.canister, 1, 0);// GCItems.basicItem, 7

        // Wood
        GameRegistry.addShapelessRecipe(
                ARBlocks.getItemStack(ARBlocks.blockMethanePlanks, 4),
                ARBlocks.getItemStack(ARBlocks.blockMethaneLog, 1));
        GameRegistry.addShapelessRecipe(
                ARBlocks.getItemStack(ARBlocks.blockPodPlanks, 4),
                ARBlocks.getItemStack(ARBlocks.blockPodBark, 1));

        // Mothership
        final HashMap<Object, Integer> inputMap = new HashMap<>();
        inputMap.put(compressedTinStack, 64);
        inputMap.put(compressedAluStack, 16);
        inputMap.put(ARBlocks.getItemStack(ARBlocks.blockMothershipController, 1), 1);
        inputMap.put(Items.iron_ingot, 24);
        mothershipRecipe = new SpaceStationRecipe(inputMap);

        // Circuit fabricator recipes
        final List<ItemStack> silicons = new ArrayList<>();
        silicons.add(new ItemStack(GCItems.basicItem, 1, 2));
        silicons.addAll(OreDictionary.getOres(ConfigManagerCore.otherModsSilicon));
        // add the silicon of GC, apparently it's not in the same oredict
        final ItemStack[] siliconArray = new ItemStack[silicons.size()];
        silicons.toArray(siliconArray);

        // for NEI, see:
        // micdoodle8.mods.galacticraft.core.nei.NEIGalacticraftConfig.addCircuitFabricatorRecipes()

        addCircuitFabricatorRecipe(
                enderWaferStack,
                new ItemStack(Items.diamond),
                siliconArray,
                siliconArray,
                new ItemStack(Items.redstone),
                new ItemStack(Items.ender_pearl));

        addCircuitFabricatorRecipe(
                lithiumMeshStack,
                lithiumGemStack,
                siliconArray,
                siliconArray,
                new ItemStack(Items.redstone),
                new ItemStack(Items.paper));

        addCircuitFabricatorRecipe(
                uranMeshStack,
                "ingotUranium",
                siliconArray,
                siliconArray,
                new ItemStack(Items.redstone),
                lithiumMeshStack);

        // Compressing
        CompressorRecipes.addRecipe(
                ARItems.lightPlating.getItemStack(1),
                "XYX",
                "XYX",
                'X',
                new ItemStack(GCItems.basicItem, 1, 8), // compressed alu
                'Y',
                new ItemStack(AsteroidsItems.basicItem, 1, 6)); // compressed titanium

        CompressorRecipes.addRecipe(ARItems.compressedGold.getItemStack(1), "XX", 'X', new ItemStack(Items.gold_ingot));

        // Smelting
        // cobble to smooth
        GameRegistry.addSmelting(
                ARBlocks.getItemStack(ARBlocks.blockBasaltCobble, 1),
                ARBlocks.getItemStack(ARBlocks.blockBasalt, 1),
                1.0F);

        GameRegistry.addSmelting(
                ARBlocks.getItemStack(ARBlocks.blockRedCobble, 1),
                ARBlocks.getItemStack(ARBlocks.blockRedRock, 1),
                1.0F);

        GameRegistry.addSmelting(
                ARBlocks.getItemStack(ARBlocks.blockYellowCobble, 1),
                ARBlocks.getItemStack(ARBlocks.blockYellowRock, 1),
                1.0F);

        // rebar to steel
        GameRegistry.addSmelting(ARItems.ancientRebar.getItemStack(1), ARItems.steelIngot.getItemStack(1), 1.5F);

        // Raygun reload
        final ItemStack battery = new ItemStack(GCItems.battery, 1, OreDictionary.WILDCARD_VALUE);
        final ItemStack liBattery = new ItemStack(ARItems.batteryLithium, 1, OreDictionary.WILDCARD_VALUE);
        final ItemStack quBattery = new ItemStack(ARItems.batteryQuantum, 1, OreDictionary.WILDCARD_VALUE);
        final ItemStack enBattery = new ItemStack(ARItems.batteryEnder, 1, OreDictionary.WILDCARD_VALUE);
        final ItemStack nuBattery = new ItemStack(ARItems.batteryNuclear, 1, OreDictionary.WILDCARD_VALUE);

        final ItemStack raygun = new ItemStack(ARItems.raygun, 1, OreDictionary.WILDCARD_VALUE);
        final ItemStack cryogun = new ItemStack(ARItems.cryogun, 1, OreDictionary.WILDCARD_VALUE);
        final ItemStack nanotool = new ItemStack(ARItems.nanotool, 1, OreDictionary.WILDCARD_VALUE);

        final ItemStack[] batteries = { battery, liBattery, quBattery, enBattery, nuBattery };

        // Regular crafting

        // batteries
        // lithium battery
        GameRegistry.addRecipe(
                liBattery,
                " X ",
                "XBX",
                "XAX",
                'X',
                compressedAluStack,
                'A',
                enderWaferStack,
                'B',
                lithiumMeshStack);

        // advanced battery
        GameRegistry.addRecipe(
                enBattery,
                " X ",
                "XBX",
                "XAX",
                'X',
                compressedAluStack,
                'A',
                enderWaferStack,
                'B',
                Blocks.redstone_block);

        // nuclear battery
        GameRegistry.addRecipe(
                nuBattery,
                " X ",
                "XBX",
                "XAX",
                'X',
                compressedAluStack,
                'A',
                enderWaferStack,
                'B',
                uranMeshStack);

        // laser diode
        GameRegistry.addRecipe(
                laserDiodeStack,
                "XXX",
                "ABC",
                "XXX",
                'X',
                compressedAluStack,
                'A',
                Blocks.glass_pane,
                'B',
                ARItems.rubyGem.getItemStack(1),
                'C',
                beamCore);

        // cryo diode
        GameRegistry.addRecipe(
                cryoDiodeStack,
                "XXX",
                "ABC",
                "XXX",
                'X',
                compressedAluStack,
                'A',
                Blocks.glass_pane,
                'B',
                ARItems.coldCrystal.getItemStack(1),
                'C',
                beamCore);

        // laser gun
        addRaygunRecipe(
                raygun,
                batteries,
                "XYZ",
                " AZ",
                "  B",
                'X',
                laserDiodeStack,
                'Y',
                enderWaferStack,
                'Z',
                compressedSteelStack,
                'A',
                button,
                'B',
                battery);

        // cryo gun
        addRaygunRecipe(
                cryogun,
                batteries,
                "XYZ",
                " AZ",
                "  B",
                'X',
                cryoDiodeStack,
                'Y',
                enderWaferStack,
                'Z',
                compressedSteelStack,
                'A',
                button,
                'B',
                battery);

        // multitool
        addRaygunRecipe(
                nanotool,
                batteries,
                "NCN",
                " H ",
                " B ",
                'N',
                ARItems.naniteCluster.getItemStack(1),
                'C',
                ARItems.naniteControl.getItemStack(1),
                'H',
                compressedTitaniumStack,
                'B',
                battery);

        // my crafter
        GameRegistry.addRecipe(
                ARBlocks.getItemStack(ARBlocks.blockWorkbench, 1),
                "XX ",
                "XX ",
                "   ",
                'X',
                compressedIronStack);

        // hydroponic
        GameRegistry.addRecipe(
                ARBlocks.getItemStack(ARBlocks.blockHydro, 2),
                "GGG",
                "CDC",
                "TTT",
                'G',
                Blocks.glass,
                'C',
                GCBlocks.oxygenCollector,
                'D',
                Blocks.dirt,
                'T',
                compressedTitaniumStack);

        // scale
        GameRegistry.addRecipe(
                ARBlocks.getItemStack(ARBlocks.blockScale, 1),
                "XXX",
                "ABC",
                "AAA",
                'X',
                compressedTinStack,
                'A',
                compressedSteelStack,
                'B',
                waferAdvanced,
                'C',
                Blocks.glass_pane);

        GameRegistry.addRecipe(
                ARItems.shuttleTank.getItemStack(1),
                "XXX",
                "XAX",
                "XXX",
                'X',
                compressedTinStack,
                'A',
                tinCanStack);

        GameRegistry.addRecipe(
                ARItems.dockGangway.getItemStack(1),
                "IXI",
                "IXI",
                "IXI",
                'X',
                new ItemStack(Blocks.wool, 1, 15), // black wool
                'I',
                new ItemStack(GCItems.flagPole));

        GameRegistry.addRecipe(
                ARItems.dockDoor.getItemStack(1),
                "SSX",
                "SXS",
                "XSS",
                'S',
                compressedSteelStack,
                'X',
                compressedAluStack);

        GameRegistry.addRecipe(
                ARBlocks.getItemStack(ARBlocks.blockShuttleDock, 1),
                "XCX",
                "DAG",
                "XCX",
                'D',
                ARItems.dockDoor.getItemStack(1),
                'G',
                ARItems.dockGangway.getItemStack(1),
                'A',
                Blocks.dropper,
                'X',
                new ItemStack(GCBlocks.airLockFrame, 1, BlockAirLockFrame.METADATA_AIR_LOCK_FRAME),
                'C',
                new ItemStack(GCBlocks.airLockFrame, 1, BlockAirLockFrame.METADATA_AIR_LOCK_CONTROLLER));

        GameRegistry.addRecipe(
                ARBlocks.getItemStack(ARBlocks.blockGravity, 1),
                "XBX",
                "XDA",
                "XWX",
                'D',
                ARItems.darkShard.getItemStack(1),
                'W',
                enderWaferStack,
                'X',
                compressedTitaniumStack,
                'B',
                beamCore,
                'A',
                ARItems.transformer.getItemStack(1));

        // block crafting
        GameRegistry.addShapelessRecipe(
                ARBlocks.getItemStack(ARBlocks.blockSmoothBasalt, 1),
                ARBlocks.getItemStack(ARBlocks.blockBasalt, 1));

        GameRegistry.addRecipe(
                ARBlocks.getItemStack(ARBlocks.blockBasaltBrick, 4),
                "XX",
                "XX",
                'X',
                ARBlocks.getItemStack(ARBlocks.blockBasalt, 1));

        GameRegistry.addRecipe(ARBlocks.getItemStack(ARBlocks.blockObsidianBrick, 4), "XX", "XX", 'X', Blocks.obsidian);

        GameRegistry.addRecipe(
                ARBlocks.getItemStack(ARBlocks.blockAluCrate, 32),
                " X ",
                "X X",
                " X ",
                'X',
                new ItemStack(GCItems.basicItem, 1, 8));

        // uranium
        GameRegistry.addRecipe(
                ARBlocks.getItemStack(ARBlocks.blockUraniumBlock, 1),
                "XXX",
                "XXX",
                "XXX",
                'X',
                ARItems.uraniumIngot.getItemStack(1));

        GameRegistry.addShapelessRecipe(
                ARItems.uraniumIngot.getItemStack(9),
                ARBlocks.getItemStack(ARBlocks.blockUraniumBlock, 1));

        // nuclear generators
        // basic
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARBlocks.getItemStack(ARBlocks.blockIsotopeGeneratorBasic, 1),
                        "XAX",
                        "XBC",
                        "XDX",
                        'X',
                        compressedSteelStack, // compressed steel
                        'A',
                        waferAdvanced,
                        'B',
                        "ingotUranium",
                        'C',
                        new ItemStack(GCBlocks.aluminumWire, 1, 0), // basic wire
                        'D',
                        waferSolar));

        // advanced
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARBlocks.getItemStack(ARBlocks.blockIsotopeGeneratorAdvanced, 1),
                        "XAX",
                        "XBC",
                        "XDX",
                        'X',
                        compressedTitaniumStack, // compressed titanium
                        'A',
                        enderWaferStack,
                        'B',
                        "blockUranium",
                        'C',
                        new ItemStack(GCBlocks.aluminumWire, 1, 1), // basic wire
                        'D',
                        lithiumMeshStack));

        // Chests
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARBlocks.getItemStack(ARBlocks.chestAlu, 1),
                        "XXX",
                        "X X",
                        "XXX",
                        'X',
                        "compressedAluminum"));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARBlocks.getItemStack(ARBlocks.chestSteel, 1),
                        "XXX",
                        "X X",
                        "XXX",
                        'X',
                        "compressedSteel"));

        // Mothership things

        // controller
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARBlocks.getItemStack(ARBlocks.blockMothershipController, 1),
                        "XBX",
                        "XAG",
                        "XCX",
                        'A',
                        freqModuleStack, // freq module here
                        'B',
                        enderWaferStack,
                        'C',
                        AsteroidsItems.orionDrive,
                        'X',
                        "compressedTitanium",
                        'G',
                        Blocks.glass_pane));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARBlocks.getItemStack(ARBlocks.blockMothershipSettings, 1),
                        "XBX",
                        "XAG",
                        "XXC",
                        'A',
                        freqModuleStack,
                        'B',
                        enderWaferStack,
                        'C',
                        Blocks.lever,
                        'X',
                        "compressedTitanium",
                        'G',
                        Blocks.glass_pane));

        // other stuff

        // thermal thingy
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARItems.thermalControl.getItemStack(2),
                        " A ",
                        "XBX",
                        " C ",
                        'A',
                        waferAdvanced,
                        'B',
                        new ItemStack(Items.redstone, 1),
                        'C',
                        GCItems.oxygenVent,
                        'X',
                        new ItemStack(GCItems.canister, 1, 0)));

        // suit
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARItems.thermalHelm.getItemStack(1),
                        "XAX",
                        "X X",
                        "   ",
                        'A',
                        thermalControllerStack,
                        'X',
                        thermalStuff));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARItems.thermalChest.getItemStack(1),
                        "X X",
                        "XAX",
                        "XAX",
                        'A',
                        thermalControllerStack,
                        'X',
                        thermalStuff));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARItems.thermalLegs.getItemStack(1),
                        "XXX",
                        "A A",
                        "X X",
                        'A',
                        thermalControllerStack,
                        'X',
                        thermalStuff));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARItems.thermalBoots.getItemStack(1),
                        "   ",
                        "A A",
                        "X X",
                        'A',
                        thermalControllerStack,
                        'X',
                        thermalStuff));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARItems.tricorder.getItemStack(1),
                        "XAX",
                        "XBC",
                        "XDE",
                        'X',
                        compressedTinStack,
                        'A',
                        freqModuleStack,
                        'B',
                        waferAdvanced,
                        'C',
                        Blocks.glass_pane,
                        'D',
                        nuBattery,
                        'E',
                        Blocks.stone_button));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARItems.naniteControl.getItemStack(1),
                        "XAX",
                        "CBC",
                        "XDX",
                        'X',
                        compressedTinStack,
                        'C',
                        freqModuleStack,
                        'B',
                        beamCore,
                        'D',
                        lithiumMeshStack,
                        'A',
                        ARItems.naniteCluster.getItemStack(1)));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARItems.gravityDisabler.getItemStack(1),
                        "XEX",
                        "RMR",
                        "XBX",
                        'X',
                        compressedAluStack,
                        'E',
                        enderWaferStack,
                        'R',
                        Items.redstone,
                        'M',
                        ARItems.darkShard.getItemStack(1),
                        'B',
                        nuBattery));

        final ItemStack rocketBoosterTier1 = new ItemStack(GCItems.rocketEngine, 1, 1);

        // jet
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARBlocks.getItemStack(ARBlocks.blockMsEngineRocketJet, 1),
                        "X X",
                        " AB",
                        "X X",
                        'A',
                        new ItemStack(AsteroidsItems.basicItem, 1, 1), // heavy rocket engine here
                        'B',
                        rocketBoosterTier1, // tier 1 booster here
                        'X',
                        "compressedTitanium"));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARBlocks.getItemStack(ARBlocks.blockMsEngineRocketBooster, 1),
                        "XXX",
                        "BCB",
                        "XXX",
                        'B',
                        rocketBoosterTier1, // tier 1 booster here
                        'C',
                        new ItemStack(GCItems.canister, 1, 0), // empty canister
                        'X',
                        "compressedTitanium"));

        // ionthruster
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARBlocks.getItemStack(ARBlocks.blockMsEngineIonJet, 1),
                        "XXX",
                        "BA ",
                        "XXX",
                        'A',
                        ARItems.goldFoil.getItemStack(1),
                        'B',
                        beamCore,
                        'X',
                        "compressedTitanium"));

        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARBlocks.getItemStack(ARBlocks.blockMsEngineIonBooster, 1),
                        "XCX",
                        "BAB",
                        "XXX",
                        'A',
                        ARItems.goldFoil.getItemStack(1),
                        'B',
                        beamCore,
                        'C',
                        ARItems.transformer.getItemStack(1),
                        'X',
                        "compressedTitanium"));

        // random misc items
        // shuttle legs
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARItems.shuttleLegs.getItemStack(1),
                        "AXA",
                        "X  ",
                        "A  ",
                        'X',
                        new ItemStack(GCItems.flagPole),
                        'A',
                        "compressedTitanium"));

        // shuttle cone
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARItems.noseCone.getItemStack(1),
                        " X ",
                        "XAX",
                        "   ",
                        'A',
                        ARItems.lightPlating.getItemStack(1),
                        'X',
                        "compressedTitanium"));

        // gold vent
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARItems.goldFoil.getItemStack(1),
                        "XAX",
                        "AXA",
                        "XAX",
                        'A',
                        compressedAluStack,
                        'X',
                        "compressedGold"));

        // transformer
        GameRegistry.addRecipe(
                new ShapedOreRecipe(
                        ARItems.transformer.getItemStack(1),
                        "XAX",
                        "XAX",
                        "XAX",
                        'A',
                        heavyWire,
                        'X',
                        "compressedMeteoricIron"));

        // new ItemStack(AsteroidsItems.basicItem, 1, 6)

        initOreSmelting();

        addSlabAndStairsCrafting(ARBlocks.blockAluCrate, ARBlocks.slabAluCrate, ARBlocks.stairsAluCrate);
        addSlabAndStairsCrafting(ARBlocks.blockBasalt, ARBlocks.slabBasaltBlock, ARBlocks.stairsBasaltBlock);
        addSlabAndStairsCrafting(ARBlocks.blockBasaltBrick, ARBlocks.slabBasaltBrick, ARBlocks.stairsBasaltBrick);
        addSlabAndStairsCrafting(ARBlocks.blockSmoothBasalt, ARBlocks.slabBasaltSmooth, ARBlocks.stairsSmoothBasalt);
        addSlabAndStairsCrafting(ARBlocks.blockMethanePlanks, ARBlocks.slabMethanePlanks, ARBlocks.stairsMethanePlanks);
        addSlabAndStairsCrafting(ARBlocks.blockObsidianBrick, ARBlocks.slabObsidianBrick, ARBlocks.stairsObsidianBrick);
        addSlabAndStairsCrafting(ARBlocks.blockPodPlanks, ARBlocks.slabPodPlanks, ARBlocks.stairsPodPlanks);
    }

    public static void verifyNasaWorkbenchCrafting() {
        final Map<Integer, ISchematicPage> pagesByPageID = new HashMap<>();
        final Map<Integer, ISchematicPage> pagesByGuiID = new HashMap<>();

        for (final ISchematicPage page : SchematicRegistry.schematicRecipes) {

            final int curPageID = page.getPageID();
            final int curGuiID = page.getGuiID();

            if (pagesByPageID.containsKey(curPageID)) {
                final ISchematicPage oldPage = pagesByPageID.get(curPageID);
                if (AmunRa.config.schematicIdShuttle == curPageID) {
                    throw new RuntimeException(
                            "Please change shuttleSchematicsId in the config file. " + curPageID
                                    + " is already in use.");
                }
                AmunRa.LOGGER.warn(
                        "Possible Page ID conflict: {} and {} on {}",
                        page.getClass().getName(),
                        oldPage.getClass().getName(),
                        curPageID);
            } else {
                pagesByPageID.put(curPageID, page);
            }

            if (pagesByGuiID.containsKey(curGuiID)) {
                final ISchematicPage oldPage = pagesByGuiID.get(curGuiID);
                if (AmunRa.config.guiIdShuttle == curGuiID) {
                    throw new RuntimeException(
                            "Please change shuttleGuiId in the config file. " + curGuiID + " is already in use.");
                }
                AmunRa.LOGGER.warn(
                        "Possible Page ID conflict: {} and {} on {}",
                        page.getClass().getName(),
                        oldPage.getClass().getName(),
                        curPageID);
            } else {
                pagesByGuiID.put(curGuiID, page);
            }
        }

    }

    protected static void addCircuitFabricatorRecipe(final ItemStack output, final Object... inputs) {
        ItemStack[] optional = null;

        if (inputs.length < 4) {
            // bad
            throw new RuntimeException("Not enough inputs for circuit fabricator");
        }
        // crystal
        ItemStack[] crystal = getStacksForInput(inputs[0]);
        ItemStack[] silicon1 = getStacksForInput(inputs[1]);
        ItemStack[] silicon2 = getStacksForInput(inputs[2]);
        ItemStack[] redstone = getStacksForInput(inputs[3]);
        if (inputs.length > 4) {
            optional = getStacksForInput(inputs[4]);
        }
        addCircuitFabricatorRecipe(output, crystal, silicon1, silicon2, redstone, optional);
    }

    private static ItemStack[] getStacksForInput(final Object input) {
        if (input instanceof ItemStack inputAsStack) {
            return new ItemStack[] { inputAsStack };
        }
        if (input instanceof String inputAsString) {
            final List<ItemStack> ores = OreDictionary.getOres(inputAsString);
            final ItemStack[] asArray = new ItemStack[ores.size()];
            ores.toArray(asArray);
            return asArray;
        }
        if (input instanceof ItemStack[]inputAsStacks) {
            return inputAsStacks;
        }
        throw new RuntimeException("Bad input");
    }

    protected static void addCircuitFabricatorRecipe(final ItemStack output, final ItemStack[] crystal,
            final ItemStack[] silicon1, final ItemStack[] silicon2, final ItemStack[] redstone,
            final ItemStack[] optional) {
        // NEI can understand arrays of ItemStack, I can give it there as is
        final CircuitFabricatorRecipe cfr = new CircuitFabricatorRecipe(
                output,
                crystal,
                silicon1,
                silicon2,
                redstone,
                optional);
        circuitFabricatorRecipes.add(cfr);
        // oh my
        for (ItemStack element : crystal) {
            for (ItemStack element2 : silicon1) {
                for (ItemStack element3 : silicon2) {
                    for (ItemStack element4 : redstone) {
                        // optional can be empty
                        if (optional.length > 0) {
                            for (ItemStack element5 : optional) {
                                addCircuitFabricatorRecipeInternal(
                                        output,
                                        element,
                                        element2,
                                        element3,
                                        element4,
                                        element5);
                            }
                        } else {
                            addCircuitFabricatorRecipeInternal(output, element, element2, element3, element4, null);
                        }
                    }
                }
            }
        }

        // 0 - Crystal slot 1 - Silicon slot 2 - Silicon slot 3 - Redstone slot 4 -
        // * Optional slot
        /*
         * new ItemStack(Items.diamond), silicon, silicon, new ItemStack(Items.redstone), new
         * ItemStack(Items.ender_pearl)
         */
    }

    protected static void addCircuitFabricatorRecipeInternal(final ItemStack output, final ItemStack crystal,
            final ItemStack silicon1, final ItemStack silicon2, final ItemStack redstone, final ItemStack optional) {
        if (optional != null) {
            CircuitFabricatorRecipes
                    .addRecipe(output, new ItemStack[] { crystal, silicon1, silicon2, redstone, optional });
        } else {
            CircuitFabricatorRecipes.addRecipe(output, new ItemStack[] { crystal, silicon1, silicon2, redstone });
        }
    }

    private static void initNasaWorkbenchCrafting() {

        SchematicRegistry.registerSchematicRecipe(new SchematicPageShuttle());

        final ItemStack lightPlate = ARItems.lightPlating.getItemStack(1);
        final ItemStack shuttleLeg = ARItems.shuttleLegs.getItemStack(1);
        // Schematic
        final HashMap<Integer, ItemStack> input = new HashMap<>();

        if (AmunRa.isNHCoreLoaded && AmunRa.isASPLoaded) {
            final ItemStack fins = GameRegistry.findItemStack("dreamcraft", "item.HeavyDutyRocketFinsTier4", 1);

            // top row, single slot
            input.put(1, ARItems.noseCone.getItemStack(1));
            // body
            input.put(2, lightPlate);
            input.put(3, lightPlate);
            input.put(4, lightPlate);
            // next 3
            input.put(5, lightPlate);
            input.put(6, new ItemStack(GameRegistry.findItem("AdvancedSolarPanel", "asp_crafting_items"), 1, 5));
            input.put(7, lightPlate);

            input.put(8, lightPlate);
            input.put(9, lightPlate);
            input.put(10, lightPlate);

            // second to last row, the fins start here
            input.put(11, fins);

            // for now, potentially change this
            input.put(12, lightPlate);
            input.put(13, lightPlate);
            input.put(14, lightPlate);

            input.put(15, fins);

            // last row
            input.put(16, shuttleLeg);
            // engine?
            input.put(17, GameRegistry.findItemStack("dreamcraft", "item.HeavyDutyRocketEngineTier4", 1));
            input.put(18, shuttleLeg);
        } else {
            // top row, single slot
            input.put(1, ARItems.noseCone.getItemStack(1));
            // body
            input.put(2, lightPlate);
            input.put(3, lightPlate);
            input.put(4, lightPlate);
            // next 3
            input.put(5, lightPlate);
            input.put(6, new ItemStack(Blocks.glass_pane, 1, 0));
            input.put(7, lightPlate);

            input.put(8, lightPlate);
            input.put(9, lightPlate);
            input.put(10, lightPlate);

            // second to last row, the fins start here
            input.put(11, new ItemStack(GCItems.partFins));

            // for now, potentially change this
            input.put(12, lightPlate);
            input.put(13, lightPlate);
            input.put(14, lightPlate);

            input.put(15, new ItemStack(GCItems.partFins));

            // last row
            input.put(16, shuttleLeg);
            // engine?
            input.put(17, new ItemStack(GCItems.rocketEngine));
            input.put(18, shuttleLeg);
        }

        // chests
        input.put(19, null);
        input.put(20, null);
        input.put(21, null);

        addRocketRecipeWithChestPermutations(ARItems.shuttleItem, input);
    }

    private static void initOreSmelting() {
        addSmeltingForMultiOre(ARBlocks.metaBlockBasaltOre);
        addSmeltingForMultiOre(ARBlocks.metaBlockObsidianOre);
        addSmeltingForMultiOre(ARBlocks.metaBlockHardClayOre);
        addSmeltingForMultiOre(ARBlocks.metaBlockConcreteOre);
        addSmeltingForMultiOre(ARBlocks.metaBlockAsteroidOre);

    }

    private static void addSmeltingForMultiOre(final BlockOreMulti block) {
        for (int i = 0; i < block.getNumPossibleSubBlocks(); i++) {
            final SubBlockOre sb = (SubBlockOre) block.getSubBlock(i);
            if (sb != null && sb.getSmeltItem() != null) {
                final ItemStack input = new ItemStack(block, 1, i);
                GameRegistry.addSmelting(input, sb.getSmeltItem(), 1.0F);
            }
        }
    }

    private static void addSlabAndStairsCrafting(final BlockMetaPair block, final BlockMetaPair slab,
            final BlockStairsAR stairsAluCrate) {
        final ItemStack blockStack = ARBlocks.getItemStack(block, 1);
        // slab
        GameRegistry.addRecipe(ARBlocks.getItemStack(slab, 6), "XXX", 'X', blockStack);
        // slab to block
        GameRegistry.addRecipe(blockStack, "X", "X", 'X', ARBlocks.getItemStack(slab, 1));

        final ItemStack stairStack = new ItemStack(stairsAluCrate, 4);

        // stairs
        GameRegistry.addRecipe(stairStack, "  X", " XX", "XXX", 'X', blockStack);
        // stairs reverse
        GameRegistry.addRecipe(stairStack, "X  ", "XX ", "XXX", 'X', blockStack);
    }

    /**
     * adds a crafting recipe for a gun and reloading recipes
     *
     * @param recipe the very last argument must be the battery
     */
    private static void addRaygunRecipe(final ItemStack gun, final ItemStack[] batteries, final Object... recipe) {
        // TODO find a way to display what is actually being crafted
        for (final ItemStack battery : batteries) {

            final Object[] modifiedRecipe = recipe.clone();
            final Object lastObject = modifiedRecipe[modifiedRecipe.length - 1];
            if (!(lastObject instanceof ItemStack lastStack) || !(lastStack.getItem() instanceof ItemElectricBase)) {
                throw new RuntimeException("Bad Raygun Recipe!");
            }
            modifiedRecipe[modifiedRecipe.length - 1] = battery;

            // the gun itself
            /*
             * ItemStack gunWithBattery = gun.copy(); ((ItemAbstractRaygun)
             * gunWithBattery.getItem()).setUsedBattery(gunWithBattery, battery);
             * gunWithBattery.setItemDamage(OreDictionary.WILDCARD_VALUE);
             */

            GameRegistry.addRecipe(gun, modifiedRecipe);

            // reloading recipes

            GameRegistry.addShapelessRecipe(gun, gun, battery);
        }
    }

    /**
     * Adds recipes for rocket chest permutations, with meta = 0 for 0 chests, 1 for 1 chest, etc
     *
     * @param rocket the item which will be crafted
     * @param input  the input hashmap
     */
    public static void addRocketRecipeWithChestPermutations(final Item rocket,
            final HashMap<Integer, ItemStack> input) {
        final int chestSlot1 = 19;
        final int chestSlot2 = 20;
        final int chestSlot3 = 21;

        ItemStack chest;
        ItemStack tank;

        if (AmunRa.isIronChestsLoaded) {
            // Copper Chest
            chest = GameRegistry.findItemStack("IronChest", "BlockIronChest", 1);
            Items.apple.setDamage(chest, 3);
        } else {
            chest = new ItemStack(Blocks.chest);
        }

        if (AmunRa.isIronTanksLoaded) {
            tank = GameRegistry.findItemStack("irontank", "copperTank", 1);
        } else {
            tank = ARItems.shuttleTank.getItemStack(1);
        }

        /*
         * ItemStack numChests0 = new ItemStack(rocket, 1, 0); ItemStack numChests1 = new ItemStack(rocket, 1, 1);
         * ItemStack numChests2 = new ItemStack(rocket, 1, 2); ItemStack numChests3 = new ItemStack(rocket, 1, 3);
         */

        // 0-0
        RocketRecipeHelper rrh = new RocketRecipeHelper(null);
        addRocketRecipeWithChestPermutations(
                new ItemStack(rocket, 1, EntityShuttle.encodeItemDamage(0, 0)),
                input,
                chestSlot1,
                chestSlot2,
                chestSlot3,
                rrh);

        // 1-0
        rrh = new RocketRecipeHelper(chest, null);
        addRocketRecipeWithChestPermutations(
                new ItemStack(rocket, 1, EntityShuttle.encodeItemDamage(1, 0)),
                input,
                chestSlot1,
                chestSlot2,
                chestSlot3,
                rrh);

        // 2-0
        rrh = new RocketRecipeHelper(null, chest);
        addRocketRecipeWithChestPermutations(
                new ItemStack(rocket, 1, EntityShuttle.encodeItemDamage(2, 0)),
                input,
                chestSlot1,
                chestSlot2,
                chestSlot3,
                rrh);

        // 3-0
        rrh = new RocketRecipeHelper(chest);
        addRocketRecipeWithChestPermutations(
                new ItemStack(rocket, 1, EntityShuttle.encodeItemDamage(3, 0)),
                input,
                chestSlot1,
                chestSlot2,
                chestSlot3,
                rrh);

        // now with tanks
        // 0-1
        rrh = new RocketRecipeHelper(tank, null);
        addRocketRecipeWithChestPermutations(
                new ItemStack(rocket, 1, EntityShuttle.encodeItemDamage(0, 1)),
                input,
                chestSlot1,
                chestSlot2,
                chestSlot3,
                rrh);

        // 0-2
        rrh = new RocketRecipeHelper(null, tank);
        addRocketRecipeWithChestPermutations(
                new ItemStack(rocket, 1, EntityShuttle.encodeItemDamage(0, 2)),
                input,
                chestSlot1,
                chestSlot2,
                chestSlot3,
                rrh);

        // 0-3
        rrh = new RocketRecipeHelper(tank);
        addRocketRecipeWithChestPermutations(
                new ItemStack(rocket, 1, EntityShuttle.encodeItemDamage(0, 3)),
                input,
                chestSlot1,
                chestSlot2,
                chestSlot3,
                rrh);

        // 1-1
        rrh = new RocketRecipeHelper(tank, chest, null);
        addRocketRecipeWithChestPermutations(
                new ItemStack(rocket, 1, EntityShuttle.encodeItemDamage(1, 1)),
                input,
                chestSlot1,
                chestSlot2,
                chestSlot3,
                rrh);

        // 2-1
        rrh = new RocketRecipeHelper(tank, chest);
        addRocketRecipeWithChestPermutations(
                new ItemStack(rocket, 1, EntityShuttle.encodeItemDamage(2, 1)),
                input,
                chestSlot1,
                chestSlot2,
                chestSlot3,
                rrh);

        // 1-2
        rrh = new RocketRecipeHelper(chest, tank);
        addRocketRecipeWithChestPermutations(
                new ItemStack(rocket, 1, EntityShuttle.encodeItemDamage(1, 2)),
                input,
                chestSlot1,
                chestSlot2,
                chestSlot3,
                rrh);

    }

    public static void addRocketRecipeWithChestPermutations(final ItemStack output,
            final HashMap<Integer, ItemStack> incompleteInput, final int chestSlot1, final int chestSlot2,
            final int chestSlot3, final RocketRecipeHelper rrh) {

        final List<ItemStack> chest1 = rrh.getStacks(0);
        final List<ItemStack> chest2 = rrh.getStacks(1);
        final List<ItemStack> chest3 = rrh.getStacks(2);
        HashMap<Integer, ItemStack> input;
        for (int i = 0; i < chest1.size(); i++) {
            input = new HashMap<>(incompleteInput);
            input.put(chestSlot1, chest1.get(i));
            input.put(chestSlot2, chest2.get(i));
            input.put(chestSlot3, chest3.get(i));
            addNasaWorkbenchRecipe(output, input);
        }
    }

    public static void addNasaWorkbenchRecipe(final ItemStack result, final HashMap<Integer, ItemStack> input) {
        addNasaWorkbenchRecipe(new NasaWorkbenchRecipe(result, input));
    }

    public static void addNasaWorkbenchRecipe(final INasaWorkbenchRecipe recipe) {
        final Item item = recipe.getRecipeOutput().getItem();
        Vector<INasaWorkbenchRecipe> recipeArray = nasaWorkbenchRecipes.computeIfAbsent(item, k -> new Vector<>());
        recipeArray.addElement(recipe);
    }

    public static ItemStack findMatchingRecipeFor(final Item expectedOutput, final IInventory craftMatrix) {
        final Vector<INasaWorkbenchRecipe> recipeArray = nasaWorkbenchRecipes.get(expectedOutput);
        if (recipeArray == null) {
            return null;
        }
        for (final INasaWorkbenchRecipe recipe : recipeArray) {
            if (recipe.matches(craftMatrix)) {
                return recipe.getRecipeOutput();
            }
        }
        return null;
    }

    public static Vector<INasaWorkbenchRecipe> getAllRecipesFor(final Item expectedOutput) {
        return nasaWorkbenchRecipes.get(expectedOutput);
    }

    public static INasaWorkbenchRecipe getMostCompleteRecipeFor(final Item expectedOutput) {
        if (nasaWorkbenchRecipes.containsKey(expectedOutput)) {
            return nasaWorkbenchRecipes.get(expectedOutput).lastElement();
        }
        return null;
    }

    /**
     * gets an arraylist of the recipe. if there are multiple possibilities for the same slot, it will contain multiple
     * itemstacks
     */
    public static Map<Integer, Set<ItemDamagePair>> getNasaWorkbenchRecipeForContainer(final Item expectedOutput) {

        final Map<Integer, Set<ItemDamagePair>> result = new HashMap<>();

        final Vector<INasaWorkbenchRecipe> recipeArray = nasaWorkbenchRecipes.get(expectedOutput);
        for (final INasaWorkbenchRecipe curRecipe : recipeArray) {

            for (final int slotNr : curRecipe.getRecipeInput().keySet()) {
                result.computeIfAbsent(slotNr, t -> new HashSet<>());
                final ItemStack stack = curRecipe.getRecipeInput().get(slotNr);
                if (stack == null) {
                    continue;
                }
                final ItemDamagePair type = new ItemDamagePair(stack);
                if (!result.get(slotNr).contains(type)) {
                    result.get(slotNr).add(type);
                }
            }
        }
        return result;
    }

    public static List<CircuitFabricatorRecipe> getCircuitFabricatorRecipes() {
        return circuitFabricatorRecipes;
    }
}
