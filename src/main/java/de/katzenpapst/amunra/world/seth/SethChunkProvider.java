package de.katzenpapst.amunra.world.seth;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.chunk.IChunkProvider;

import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.helper.CoordHelper;
import de.katzenpapst.amunra.world.AmunraChunkProvider;
import de.katzenpapst.amunra.world.TerrainGenerator;
import de.katzenpapst.amunra.world.mapgen.CrystalFormation;
import de.katzenpapst.amunra.world.mapgen.volcano.VolcanoGenerator;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeDecoratorSpace;

public class SethChunkProvider extends AmunraChunkProvider {

    BlockMetaPair rockBlock;
    BlockMetaPair grassBlock;
    BlockMetaPair dirtBlock;

    BlockMetaPair waterBlock;
    BlockMetaPair floorGrassBlock;
    BlockMetaPair floorDirtBlock;
    BlockMetaPair floorStoneBlock;

    protected final int floorDirtWidth = 4;

    protected final int maxWaterHeight = 60;

    private final TerrainGenerator oceanFloorGen;

    protected VolcanoGenerator volcanoGen;

    protected CrystalFormation crystalGen;

    public SethChunkProvider(final World world, final long seed, final boolean mapFeaturesEnabled) {
        super(world, seed, mapFeaturesEnabled);
        this.rockBlock = new BlockMetaPair(Blocks.packed_ice, (byte) 0);
        this.grassBlock = new BlockMetaPair(Blocks.snow, (byte) 0);
        this.dirtBlock = new BlockMetaPair(Blocks.ice, (byte) 0);

        this.floorStoneBlock = new BlockMetaPair(Blocks.hardened_clay, (byte) 0);// ARBlocks.blockYellowRock;
        this.floorDirtBlock = new BlockMetaPair(Blocks.clay, (byte) 0);
        this.floorGrassBlock = ARBlocks.blockUnderwaterGrass;
        this.waterBlock = new BlockMetaPair(Blocks.water, (byte) 0);

        this.oceanFloorGen = new TerrainGenerator(
                this.rand,
                this.floorStoneBlock,
                this.waterBlock,
                30, // heightMod
                35, // smallFeatureMod
                40, // mountainHeightMod
                10, // valleyHeightMod
                25, // seaLevel
                this.maxWaterHeight // maxHeight
        );

        this.volcanoGen = new VolcanoGenerator(this.waterBlock, this.rockBlock, this.dirtBlock, 60, false);

        this.crystalGen = new CrystalFormation(ARBlocks.blockGlowingCoral, this.waterBlock);

        this.monsters = new SpawnListEntry[] {};
        this.biomes = new BiomeGenBase[] { BiomeGenBase.iceMountains };
        this.worldGenerators.add(this.volcanoGen);
    }

    @Override
    public void generateTerrain(final int chunkX, final int chunkZ, final Block[] idArray, final byte[] metaArray) {
        super.generateTerrain(chunkX, chunkZ, idArray, metaArray);

        this.oceanFloorGen.generateTerrain(chunkX, chunkZ, idArray, metaArray);
    }

    @Override
    public void replaceBlocksForBiome(final int chunkX, final int chunkZ, final Block[] arrayOfIDs,
            final byte[] arrayOfMeta, final BiomeGenBase[] par4ArrayOfBiomeGenBase) {
        // generate the default stuff first
        super.replaceBlocksForBiome(chunkX, chunkZ, arrayOfIDs, arrayOfMeta, par4ArrayOfBiomeGenBase);
        // now do my stuff

        for (int curX = 0; curX < 16; ++curX) {
            for (int curZ = 0; curZ < 16; ++curZ) {
                int surfaceHeight = -1;
                for (int curY = this.maxWaterHeight - 1; curY > 0; curY--) {
                    final int index = this.getIndex(curX, curY, curZ);
                    final Block curBlockId = arrayOfIDs[index];
                    final byte curMeta = arrayOfMeta[index];

                    if (curBlockId == this.floorStoneBlock.getBlock()
                            && curMeta == this.floorStoneBlock.getMetadata()) {

                        if (surfaceHeight == -1) {
                            surfaceHeight = curY;
                            arrayOfIDs[index] = this.floorGrassBlock.getBlock();
                            arrayOfMeta[index] = this.floorGrassBlock.getMetadata();
                        } else if (surfaceHeight - curY < this.floorDirtWidth) {
                            arrayOfIDs[index] = this.floorDirtBlock.getBlock();
                            arrayOfMeta[index] = this.floorDirtBlock.getMetadata();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected BiomeDecoratorSpace getBiomeGenerator() {
        return new SethBiomeDecorator();
    }

    @Override
    protected int getSeaLevel() {
        return 120;
    }

    @Override
    protected BlockMetaPair getGrassBlock() {
        return this.grassBlock;
    }

    @Override
    protected BlockMetaPair getDirtBlock() {
        return this.dirtBlock;
    }

    @Override
    protected BlockMetaPair getStoneBlock() {
        return this.rockBlock;
    }

    @Override
    public double getHeightModifier() {
        return 40;
    }

    @Override
    public double getSmallFeatureHeightModifier() {
        return 60;
    }

    @Override
    public double getMountainHeightModifier() {
        return 70;
    }

    @Override
    public double getValleyHeightModifier() {
        return 50;
    }

    @Override
    public void onPopulate(final IChunkProvider provider, final int cX, final int cZ) {

        final int numToGenerate = this.rand.nextInt(this.rand.nextInt(4) + 1);

        final int curChunkMinX = CoordHelper.chunkToMinBlock(cX);
        final int curChunkMinZ = CoordHelper.chunkToMinBlock(cZ);

        for (int i = 0; i < numToGenerate; ++i) {
            final int curX = curChunkMinX + this.rand.nextInt(16) + 8;
            final int curY = 35;
            final int curZ = curChunkMinZ + this.rand.nextInt(16) + 8;
            this.crystalGen.generate(this.worldObj, this.rand, curX, curY, curZ);
        }
    }

}
