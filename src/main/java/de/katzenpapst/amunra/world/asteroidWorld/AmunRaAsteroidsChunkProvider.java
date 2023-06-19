package de.katzenpapst.amunra.world.asteroidWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.feature.WorldGenerator;

import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.world.WorldGenTallgrassMeta;
import de.katzenpapst.amunra.world.WorldGenTreeBySapling;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.api.vector.BlockVec3;
import micdoodle8.mods.galacticraft.core.blocks.GCBlocks;
import micdoodle8.mods.galacticraft.core.perlin.NoiseModule;
import micdoodle8.mods.galacticraft.core.perlin.generator.Billowed;
import micdoodle8.mods.galacticraft.core.perlin.generator.Gradient;
import micdoodle8.mods.galacticraft.planets.asteroids.blocks.AsteroidBlocks;
import micdoodle8.mods.galacticraft.planets.asteroids.dimension.WorldProviderAsteroids;
import micdoodle8.mods.galacticraft.planets.asteroids.world.gen.BiomeGenBaseAsteroids;
import micdoodle8.mods.galacticraft.planets.asteroids.world.gen.SpecialAsteroidBlock;
import micdoodle8.mods.galacticraft.planets.asteroids.world.gen.SpecialAsteroidBlockHandler;

/**
 * This is copypaste from ChunkProviderAsteroids. I would have extended that class, but everything there is private
 *
 * @author katzenpapst
 *
 */
public class AmunRaAsteroidsChunkProvider extends ChunkProviderGenerate {

    public static class WorldGenData {

        public int chance;
        public WorldGenerator worldGen;

        public WorldGenData(final WorldGenerator worldGen, final int chance) {
            this.chance = chance;
            this.worldGen = worldGen;
        }
    }

    protected Map<BlockMetaPair, Integer> extraOreGen = new HashMap<>();

    protected BlockMetaPair[] asteroidStoneBlocks;

    protected BlockMetaPair denseIce;

    protected BlockMetaPair dirt;
    protected BlockMetaPair grass;
    protected BlockMetaPair light;

    protected WorldGenData[] generatorsTrees;
    protected WorldGenData[] generatorsGrass;
    protected WorldGenData[] generatorsLakes;

    private final Random rand;

    private final World worldObj;

    private final NoiseModule asteroidDensity;

    private final NoiseModule asteroidTurbulance;

    private final NoiseModule asteroidSkewX;
    private final NoiseModule asteroidSkewY;
    private final NoiseModule asteroidSkewZ;

    protected final SpecialAsteroidBlockHandler coreHandler;
    protected final SpecialAsteroidBlockHandler shellHandler;

    // DO NOT CHANGE
    private static final int CHUNK_SIZE_X = 16;
    private static final int CHUNK_SIZE_Y = 256;
    private static final int CHUNK_SIZE_Z = 16;

    private static final int MAX_ASTEROID_RADIUS = 25;
    private static final int MIN_ASTEROID_RADIUS = 5;

    private static final int MAX_ASTEROID_SKEW = 8;

    private static final int MIN_ASTEROID_Y = 48;
    private static final int MAX_ASTEROID_Y = CHUNK_SIZE_Y - 48;

    private static final int ASTEROID_CHANCE = 800; // About 1 / n chance per XZ pair

    private static final int ASTEROID_SHELL_CHANCE = 2; // 1 / n chance per asteroid

    private static final int MIN_BLOCKS_PER_CHUNK = 50;
    private static final int MAX_BLOCKS_PER_CHUNK = 200;

    private static final int NOISE_OFFSET_SIZE = 256;

    private static final float MIN_HOLLOW_SIZE = .6F;
    private static final float MAX_HOLLOW_SIZE = .8F;
    private static final int HOLLOW_CHANCE = 10; // 1 / n chance per asteroid
    private static final int MIN_RADIUS_FOR_HOLLOW = 15;
    private static final int TALL_GRASS_CHANCE = 2;
    private static final int GLOWSTONE_CHANCE = 20;

    private final List<AsteroidData> largeAsteroids = new ArrayList<>();
    private static final Set<BlockVec3> CHUNKS_DONE = new HashSet<>();
    private int largeAsteroidsLastChunkX;
    private int largeAsteroidsLastChunkZ;

    public AmunRaAsteroidsChunkProvider(final World world, final long seed, final boolean mapFeaturesEnabled) {
        super(world, seed, mapFeaturesEnabled);
        this.worldObj = world;
        this.rand = new Random(seed);

        this.asteroidStoneBlocks = new BlockMetaPair[] { new BlockMetaPair(AsteroidBlocks.blockBasic, (byte) 0),
                new BlockMetaPair(AsteroidBlocks.blockBasic, (byte) 1),
                new BlockMetaPair(AsteroidBlocks.blockBasic, (byte) 2) };

        this.shellHandler = new SpecialAsteroidBlockHandler();
        this.coreHandler = new SpecialAsteroidBlockHandler();

        this.asteroidDensity = new Billowed(this.rand.nextLong(), 2, .25F);
        this.asteroidDensity.setFrequency(.009F);
        this.asteroidDensity.amplitude = .6F;

        this.asteroidTurbulance = new Gradient(this.rand.nextLong(), 1, .2F);
        this.asteroidTurbulance.setFrequency(.08F);
        this.asteroidTurbulance.amplitude = .5F;

        this.asteroidSkewX = new Gradient(this.rand.nextLong(), 1, 1);
        this.asteroidSkewX.amplitude = MAX_ASTEROID_SKEW;
        this.asteroidSkewX.frequencyX = 0.005F;

        this.asteroidSkewY = new Gradient(this.rand.nextLong(), 1, 1);
        this.asteroidSkewY.amplitude = MAX_ASTEROID_SKEW;
        this.asteroidSkewY.frequencyY = 0.005F;

        this.asteroidSkewZ = new Gradient(this.rand.nextLong(), 1, 1);
        this.asteroidSkewZ.amplitude = MAX_ASTEROID_SKEW;
        this.asteroidSkewZ.frequencyZ = 0.005F;

        this.initBlockTypes();

    }

    protected void initBlockTypes() {
        final BlockMetaPair oreAlu = new BlockMetaPair(AsteroidBlocks.blockBasic, (byte) 3);
        final BlockMetaPair oreTitanium = new BlockMetaPair(AsteroidBlocks.blockBasic, (byte) 4);
        final BlockMetaPair oreIron = new BlockMetaPair(AsteroidBlocks.blockBasic, (byte) 5);

        final BlockMetaPair oreSilicon = new BlockMetaPair(GCBlocks.basicBlock, (byte) 8);
        final BlockMetaPair oreMeteorIron = new BlockMetaPair(GCBlocks.basicBlock, (byte) 12);

        this.denseIce = new BlockMetaPair(AsteroidBlocks.blockDenseIce, (byte) 0);

        this.dirt = ARBlocks.blockMethaneDirt;
        this.grass = ARBlocks.blockVacuumGrass;
        this.light = new BlockMetaPair(Blocks.glowstone, (byte) 0);

        this.generatorsTrees = new WorldGenData[] {
                new WorldGenData(new WorldGenTreeBySapling(false, 5, ARBlocks.blockMethaneSapling), 2) };
        this.generatorsGrass = new WorldGenData[] {
                new WorldGenData(new WorldGenTallgrassMeta(ARBlocks.blockMethaneTGrass), 2) };
        this.generatorsLakes = new WorldGenData[] { new WorldGenData(new WorldGenLakes(Blocks.water), 2),
                new WorldGenData(new WorldGenLakes(Blocks.lava), 2) };

        this.addBlockToHandler(this.coreHandler, this.asteroidStoneBlocks[2], 5, .3);
        this.addBlockToHandler(this.coreHandler, this.asteroidStoneBlocks[1], 7, .3);
        this.addBlockToHandler(this.coreHandler, this.asteroidStoneBlocks[0], 1, .25);
        // ores
        this.addBlockToHandler(this.coreHandler, oreAlu, 15, .2);// 250
        this.addBlockToHandler(this.coreHandler, oreTitanium, 10, .15); // 400
        this.addBlockToHandler(this.coreHandler, oreIron, 23, .2); // 300
        this.addBlockToHandler(this.coreHandler, oreSilicon, 12, .15);
        this.addBlockToHandler(this.coreHandler, oreMeteorIron, 6, .13);
        // addBlockToHandler(coreHandler, oreDiamond, 1, .1);

        this.extraOreGen.put(oreTitanium, 400);
        this.extraOreGen.put(oreAlu, 250);
        this.extraOreGen.put(oreIron, 300);

        this.addBlockToHandler(this.shellHandler, this.asteroidStoneBlocks[0], 1, .15);
        this.addBlockToHandler(this.shellHandler, this.asteroidStoneBlocks[1], 3, .15);
        this.addBlockToHandler(this.shellHandler, this.asteroidStoneBlocks[2], 1, .15);
        this.addBlockToHandler(this.shellHandler, this.denseIce, 1, .15);
    }

    protected void addBlockToHandler(final SpecialAsteroidBlockHandler handler, final BlockMetaPair b,
            final int probability, final double thickness) {
        handler.addBlock(new SpecialAsteroidBlock(b.getBlock(), b.getMetadata(), probability, thickness));
    }

    public void generateTerrain(final int chunkX, final int chunkZ, final Block[] idArray, final byte[] metaArray,
            final boolean flagDataOnly) {
        this.largeAsteroids.clear();
        final Random random = new Random();
        final int rangeY = MAX_ASTEROID_Y - MIN_ASTEROID_Y;
        final int rangeSize = MAX_ASTEROID_RADIUS - MIN_ASTEROID_RADIUS;

        // If there is an asteroid centre nearby, it might need to generate some asteroid parts in this chunk
        for (int i = chunkX - 3; i < chunkX + 3; i++) {
            final int minX = i * 16;
            final int maxX = minX + CHUNK_SIZE_X;
            for (int k = chunkZ - 3; k < chunkZ + 3; k++) {
                final int minZ = k * 16;
                final int maxZ = minZ + CHUNK_SIZE_Z;

                // NOTE: IF UPDATING THIS CODE also update addLargeAsteroids() which is the same algorithm
                // ??? ^^ this now seems redundant
                for (int x = minX; x < maxX; x += 2) {
                    for (int z = minZ; z < maxZ; z += 2) {
                        // The next line is called 3136 times per chunk generated. getNoise is a little slow.
                        if (this.randFromPointPos(x, z)
                                < (this.asteroidDensity.getNoise(x, z) + .4) / ASTEROID_CHANCE) {
                            random.setSeed(x + z * 3067);
                            final int y = random.nextInt(rangeY) + MIN_ASTEROID_Y;
                            final int size = random.nextInt(rangeSize) + MIN_ASTEROID_RADIUS;

                            // Generate the parts of the asteroid which are in this chunk
                            this.generateAsteroid(
                                    random,
                                    x,
                                    y,
                                    z,
                                    chunkX << 4,
                                    chunkZ << 4,
                                    size,
                                    idArray,
                                    metaArray,
                                    flagDataOnly);
                        }
                    }
                }
            }
        }
    }

    private void generateAsteroid(final Random rand, final int asteroidX, final int asteroidY, final int asteroidZ,
            final int chunkX, final int chunkZ, final int size, final Block[] blockArray, final byte[] metaArray,
            final boolean flagDataOnly) {
        final SpecialAsteroidBlock core = this.coreHandler.getBlock(rand, size);

        SpecialAsteroidBlock shell = null;
        if (rand.nextInt(ASTEROID_SHELL_CHANCE) == 0) {
            shell = this.shellHandler.getBlock(rand, size);
        }

        boolean isHollow = false;
        final float hollowSize = rand.nextFloat() * (MAX_HOLLOW_SIZE - MIN_HOLLOW_SIZE) + MIN_HOLLOW_SIZE;
        if (rand.nextInt(HOLLOW_CHANCE) == 0 && size >= MIN_RADIUS_FOR_HOLLOW) {
            isHollow = true;
            shell = new SpecialAsteroidBlock(this.denseIce.getBlock(), this.denseIce.getMetadata(), 1, .15);
        }

        // Add to the list of asteroids for external use
        ((WorldProviderAsteroids) this.worldObj.provider)
                .addAsteroid(asteroidX, asteroidY, asteroidZ, size, isHollow ? -1 : core.index);

        final int xMin = this.clamp(Math.max(chunkX, asteroidX - size - MAX_ASTEROID_SKEW - 2) - chunkX, 0, 16);
        final int zMin = this.clamp(Math.max(chunkZ, asteroidZ - size - MAX_ASTEROID_SKEW - 2) - chunkZ, 0, 16);
        final int yMin = asteroidY - size - MAX_ASTEROID_SKEW - 2;
        final int yMax = asteroidY + size + MAX_ASTEROID_SKEW + 2;
        final int xMax = this.clamp(Math.min(chunkX + 16, asteroidX + size + MAX_ASTEROID_SKEW + 2) - chunkX, 0, 16);
        final int zMax = this.clamp(Math.min(chunkZ + 16, asteroidZ + size + MAX_ASTEROID_SKEW + 2) - chunkZ, 0, 16);
        final int xSize = xMax - xMin;
        final int ySize = yMax - yMin;
        final int zSize = zMax - zMin;

        if (xSize <= 0 || ySize <= 0 || zSize <= 0) return;

        final float noiseOffsetX = this.randFromPoint(asteroidX, asteroidY, asteroidZ) * NOISE_OFFSET_SIZE + chunkX;
        final float noiseOffsetY = this.randFromPoint(asteroidX * 7, asteroidY * 11, asteroidZ * 13)
                * NOISE_OFFSET_SIZE;
        final float noiseOffsetZ = this.randFromPoint(asteroidX * 17, asteroidY * 23, asteroidZ * 29)
                * NOISE_OFFSET_SIZE + chunkZ;
        this.setOtherAxisFrequency(1F / (size * 2F / 2F));

        final float[] sizeXArray = new float[ySize * zSize];
        final float[] sizeZArray = new float[xSize * ySize];
        final float[] sizeYArray = new float[xSize * zSize];

        for (int x = 0; x < xSize; x++) {
            final int xx = x * zSize;
            final float xxx = x + noiseOffsetX;
            for (int z = 0; z < zSize; z++) {
                sizeYArray[xx + z] = this.asteroidSkewY.getNoise(xxx, z + noiseOffsetZ);
            }
        }

        final AsteroidData asteroidData = new AsteroidData(
                isHollow,
                sizeYArray,
                xMin,
                zMin,
                xMax,
                zMax,
                zSize,
                size,
                asteroidX,
                asteroidY,
                asteroidZ);
        this.largeAsteroids.add(asteroidData);
        this.largeAsteroidsLastChunkX = chunkX;
        this.largeAsteroidsLastChunkZ = chunkZ;

        if (flagDataOnly) return;

        for (int y = 0; y < ySize; y++) {
            final int yy = y * zSize;
            final float yyy = y + noiseOffsetY;
            for (int z = 0; z < zSize; z++) {
                sizeXArray[yy + z] = this.asteroidSkewX.getNoise(yyy, z + noiseOffsetZ);
            }
        }

        for (int x = 0; x < xSize; x++) {
            final int xx = x * ySize;
            final float xxx = x + noiseOffsetX;
            for (int y = 0; y < ySize; y++) {
                sizeZArray[xx + y] = this.asteroidSkewZ.getNoise(xxx, y + noiseOffsetY);
            }
        }

        double shellThickness = 0;
        int terrainY = 0;
        int terrainYY = 0;
        if (shell != null) shellThickness = 1.0 - shell.thickness;
        for (int x = xMax - 1; x >= xMin; x--) {
            final int indexXY = (x - xMin) * ySize - yMin;
            final int indexXZ = (x - xMin) * zSize - zMin;
            final int distanceX = asteroidX - (x + chunkX);
            final int indexBaseX = x * CHUNK_SIZE_Y << 4;
            final float xx = x + chunkX;

            for (int z = zMin; z < zMax; z++) {
                if (isHollow) {
                    final float sizeModY = sizeYArray[indexXZ + z];
                    terrainY = this.getTerrainHeightFor(sizeModY, asteroidY, size);
                    terrainYY = this.getTerrainHeightFor(sizeModY, asteroidY - 1, size);
                }

                float sizeY = size + sizeYArray[indexXZ + z];
                sizeY *= sizeY;
                final int distanceZ = asteroidZ - (z + chunkZ);
                final int indexBase = indexBaseX | z * CHUNK_SIZE_Y;
                final float zz = z + chunkZ;

                for (int y = yMin; y < yMax; y++) {
                    float dSizeX = distanceX / (size + sizeXArray[(y - yMin) * zSize + z - zMin]);
                    float dSizeZ = distanceZ / (size + sizeZArray[indexXY + y]);
                    dSizeX *= dSizeX;
                    dSizeZ *= dSizeZ;
                    int distanceY = asteroidY - y;
                    distanceY *= distanceY;
                    float distance = dSizeX + distanceY / sizeY + dSizeZ;
                    float distanceAbove = distance;
                    distance += this.asteroidTurbulance.getNoise(xx, y, zz);

                    if (isHollow && distance <= hollowSize) {
                        distanceAbove += this.asteroidTurbulance.getNoise(xx, y + 1, zz);
                        if (distanceAbove <= 1 && y - 1 == terrainYY) {
                            final int index = indexBase | y + 1;
                            blockArray[index] = this.light.getBlock();
                            metaArray[index] = this.light.getMetadata();
                        }
                    }

                    if (distance <= 1) {
                        final int index = indexBase | y;
                        if (isHollow && distance <= hollowSize) {
                            if (y == terrainY) {
                                blockArray[index] = this.grass.getBlock();
                                metaArray[index] = this.grass.getMetadata();
                            } else if (y < terrainY) {
                                blockArray[index] = this.dirt.getBlock();
                                metaArray[index] = this.dirt.getMetadata();
                            } else {
                                blockArray[index] = Blocks.air;
                                metaArray[index] = 0;
                            }
                        } else if (distance <= core.thickness) {
                            if (rand.nextBoolean()) {
                                blockArray[index] = core.block;
                                metaArray[index] = core.meta;
                            } else {
                                blockArray[index] = this.asteroidStoneBlocks[0].getBlock();
                                metaArray[index] = this.asteroidStoneBlocks[0].getMetadata();
                            }
                        } else if (shell != null && distance >= shellThickness) {
                            blockArray[index] = shell.block;
                            metaArray[index] = shell.meta;
                        } else {
                            blockArray[index] = this.asteroidStoneBlocks[1].getBlock();
                            metaArray[index] = this.asteroidStoneBlocks[1].getMetadata();
                        }
                    }
                }
            }
        }

        if (isHollow) {
            shellThickness = 0;
            if (shell != null) shellThickness = 1.0 - shell.thickness;
            for (int x = xMin; x < xMax; x++) {
                final int indexXY = (x - xMin) * ySize - yMin;
                final int indexXZ = (x - xMin) * zSize - zMin;
                int distanceX = asteroidX - (x + chunkX);
                distanceX *= distanceX;
                final int indexBaseX = x * CHUNK_SIZE_Y << 4;

                for (int z = zMin; z < zMax; z++) {
                    float sizeY = size + sizeYArray[indexXZ + z];
                    sizeY *= sizeY;
                    int distanceZ = asteroidZ - (z + chunkZ);
                    distanceZ *= distanceZ;
                    final int indexBase = indexBaseX | z * CHUNK_SIZE_Y;

                    for (int y = yMin; y < yMax; y++) {
                        float sizeX = size + sizeXArray[(y - yMin) * zSize + z - zMin];
                        float sizeZ = size + sizeZArray[indexXY + y];
                        sizeX *= sizeX;
                        sizeZ *= sizeZ;
                        int distanceY = asteroidY - y;
                        distanceY *= distanceY;
                        float distance = distanceX / sizeX + distanceY / sizeY + distanceZ / sizeZ;
                        distance += this.asteroidTurbulance.getNoise(x + chunkX, y, z + chunkZ);

                        if (distance <= 1) {
                            final int index = indexBase | y;
                            final int indexAbove = indexBase | y + 1;
                            if (Blocks.air == blockArray[indexAbove]
                                    && (blockArray[index] == this.asteroidStoneBlocks[0].getBlock()
                                            || blockArray[index] == this.grass.getBlock())
                                    && this.rand.nextInt(GLOWSTONE_CHANCE) == 0) {
                                blockArray[index] = this.light.getBlock();
                                metaArray[index] = this.light.getMetadata();
                            }
                        }
                    }
                }
            }
        }
    }

    private void setOtherAxisFrequency(final float frequency) {
        this.asteroidSkewX.frequencyY = frequency;
        this.asteroidSkewX.frequencyZ = frequency;

        this.asteroidSkewY.frequencyX = frequency;
        this.asteroidSkewY.frequencyZ = frequency;

        this.asteroidSkewZ.frequencyX = frequency;
        this.asteroidSkewZ.frequencyY = frequency;
    }

    private int clamp(int x, final int min, final int max) {
        if (x < min) {
            x = min;
        } else if (x > max) {
            x = max;
        }
        return x;
    }

    private double clamp(double x, final double min, final double max) {
        if (x < min) {
            x = min;
        } else if (x > max) {
            x = max;
        }
        return x;
    }

    private int getTerrainHeightFor(final float yMod, final int asteroidY, final int asteroidSize) {
        return (int) (asteroidY - asteroidSize / 4 + yMod * 1.5F);
    }

    private int getTerrainHeightAt(final int x, final int z, final float[] yModArray, final int xMin, final int zMin,
            final int zSize, final int asteroidY, final int asteroidSize) {
        final int index = (x - xMin) * zSize - zMin;
        if (index < yModArray.length && index >= 0) {
            final float yMod = yModArray[index];
            return this.getTerrainHeightFor(yMod, asteroidY, asteroidSize);
        }
        return 1;
    }

    @Override
    public Chunk provideChunk(int p_73154_1_, int p_73154_2_) {
        this.rand.setSeed(p_73154_1_ * 341873128712L + p_73154_2_ * 132897987541L);
        final Block[] ids = new Block[65536];
        final byte[] meta = new byte[65536];
        this.generateTerrain(p_73154_1_, p_73154_2_, ids, meta, false);

        final Chunk curChunk = new Chunk(this.worldObj, ids, meta, p_73154_1_, p_73154_2_);
        final byte[] chunkBiomeArray = curChunk.getBiomeArray();

        for (int i = 0; i < chunkBiomeArray.length; ++i) {
            chunkBiomeArray[i] = (byte) BiomeGenBaseAsteroids.asteroid.biomeID;
        }

        this.generateSkylightMap(curChunk, p_73154_1_, p_73154_2_);

        return curChunk;
    }

    private float randFromPoint(final int x, final int y, final int z) {
        int n = x + z * 57 + y * 571;
        n ^= n << 13;
        n = n * (n * n * 15731 + 789221) + 1376312589 & 0x7fffffff;
        return 1.0F - n / 1073741824.0F;
    }

    private float randFromPoint(final int x, final int z) {
        int n = x + z * 57;
        n ^= n << 13;
        n = n * (n * n * 15731 + 789221) + 1376312589 & 0x7fffffff;
        return 1.0F - n / 1073741824.0F;
    }

    private float randFromPointPos(final int x, final int z) {
        int n = x + z * 57;
        n ^= n << 13;
        n = n * (n * n * 15731 + 789221) + 1376312589 & 0x3fffffff;
        return 1.0F - n / 1073741824.0F;
    }

    @Override
    public boolean chunkExists(int p_73149_1_, int p_73149_2_) {
        return true;
    }

    /**
     * @param x         start x
     * @param maxLength how far to look max
     * @param isAir     if true, counts air blocks, if false, counts the NOT airblocks
     */
    protected int getCountInNegXDirection(final IChunkProvider world, final int x, final int y, final int z,
            final int maxLength, final boolean isAir) {
        int count = 0;

        for (int i = 0; i < maxLength; i++) {
            if (this.worldObj.getBlock(x - i - 1, y, z) instanceof BlockAir != isAir) {
                break;
            }
            count++;
        }

        return count;
    }

    @Override
    public void populate(IChunkProvider p_73153_1_, int p_73153_2_, int p_73153_3_) {
        final int x = p_73153_2_ << 4;
        final int z = p_73153_3_ << 4;
        if (!CHUNKS_DONE.add(new BlockVec3(x, 0, z))) return;

        BlockFalling.fallInstantly = true;
        this.worldObj.getBiomeGenForCoords(x + 16, z + 16);
        BlockFalling.fallInstantly = false;

        this.rand.setSeed(this.worldObj.getSeed());
        final long var7 = this.rand.nextLong() / 2L * 2L + 1L;
        final long var9 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(p_73153_2_ * var7 + p_73153_3_ * var9 ^ this.worldObj.getSeed());

        // 50:50 chance to include small blocks each chunk
        if (this.rand.nextBoolean()) {
            final double density = this.asteroidDensity.getNoise(p_73153_2_ * 16, p_73153_3_ * 16) * 0.54;
            final double numOfBlocks = this.clamp(this.randFromPoint(p_73153_2_, p_73153_3_), .4, 1)
                    * MAX_BLOCKS_PER_CHUNK
                    * density + MIN_BLOCKS_PER_CHUNK;
            final int y0 = this.rand.nextInt(2);
            Block block;
            int meta;
            final int yRange = MAX_ASTEROID_Y - MIN_ASTEROID_Y;

            for (int i = 0; i < numOfBlocks; i++) {
                final int y = this.rand.nextInt(yRange) + MIN_ASTEROID_Y;

                // 50:50 chance vertically as well
                if (y0 == y / 16 % 2) {
                    final int px = x + this.rand.nextInt(CHUNK_SIZE_X);
                    final int pz = z + this.rand.nextInt(CHUNK_SIZE_Z);

                    block = this.asteroidStoneBlocks[1].getBlock();
                    meta = this.asteroidStoneBlocks[1].getMetadata();

                    // extra ore stuff
                    for (final BlockMetaPair b : this.extraOreGen.keySet()) {
                        final int chance = this.extraOreGen.get(b);
                        if (this.rand.nextInt(chance) == 0) {
                            block = b.getBlock();
                            meta = b.getMetadata();
                            break;
                        }
                    }

                    this.worldObj.setBlock(px, y, pz, block, meta, 2);
                    // this does something about the light
                    // so, the more blocks towards -x are NOT air, the brighter are we?!
                    int count = 7;
                    if (!(this.worldObj.getBlock(px - 1, y, pz) instanceof BlockAir)) count = 1;// 0
                    else if (!(this.worldObj.getBlock(px - 2, y, pz) instanceof BlockAir)) count = 3;// 1
                    else if (!(this.worldObj.getBlock(px - 3, y, pz) instanceof BlockAir)) count = 5;// 2
                    else if (!(this.worldObj.getBlock(px - 4, y, pz) instanceof BlockAir)) count = 6;// 3

                    count = this.adjustBrightnessValue(count);

                    this.worldObj.setLightValue(EnumSkyBlock.Block, px - 1, y, pz, count);// +1
                }
            }
        }

        if (this.largeAsteroidsLastChunkX != p_73153_2_ || this.largeAsteroidsLastChunkZ != p_73153_3_) {
            this.generateTerrain(p_73153_2_, p_73153_3_, null, null, true);
        }

        this.rand.setSeed(p_73153_2_ * var7 + p_73153_3_ * var9 ^ this.worldObj.getSeed());

        // Look for hollow asteroids to populate
        if (!this.largeAsteroids.isEmpty()) {
            for (final AsteroidData asteroidIndex : this.largeAsteroids) {
                if (!asteroidIndex.isHollow) continue;

                final float[] sizeYArray = asteroidIndex.sizeYArray;
                final int xMin = asteroidIndex.xMinArray;
                final int zMin = asteroidIndex.zMinArray;
                final int zSize = asteroidIndex.zSizeArray;
                final int asteroidY = asteroidIndex.asteroidYArray;
                final int asteroidSize = asteroidIndex.asteroidSizeArray;
                boolean treesdone = false;

                for (final WorldGenData data : this.generatorsTrees) {
                    if (this.rand.nextInt(data.chance) == 0) {

                        for (int tries = 0; tries < 5; tries++) {
                            final int i = this.rand.nextInt(16) + x + 8;
                            final int k = this.rand.nextInt(16) + z + 8;
                            if (data.worldGen.generate(
                                    this.worldObj,
                                    this.rand,
                                    i,
                                    this.getTerrainHeightAt(
                                            i - x,
                                            k - z,
                                            sizeYArray,
                                            xMin,
                                            zMin,
                                            zSize,
                                            asteroidY,
                                            asteroidSize),
                                    k))
                                break;
                        }
                        treesdone = true;
                        break;
                    }
                }

                if (!treesdone || this.rand.nextInt(TALL_GRASS_CHANCE) == 0) {
                    for (final WorldGenData data : this.generatorsGrass) {
                        if (this.rand.nextInt(data.chance) == 0) {
                            final int i = this.rand.nextInt(16) + x + 8;
                            final int k = this.rand.nextInt(16) + z + 8;
                            data.worldGen.generate(
                                    this.worldObj,
                                    this.rand,
                                    i,
                                    this.getTerrainHeightAt(
                                            i - x,
                                            k - z,
                                            sizeYArray,
                                            xMin,
                                            zMin,
                                            zSize,
                                            asteroidY,
                                            asteroidSize),
                                    k);
                        }
                    }
                }
                /*
                 * if(rand.nextInt(FLOWER_CHANCE) == 0) { int i = rand.nextInt(16) + x + 8; int k = rand.nextInt(16) + z
                 * + 8; new WorldGenFlowers(this.FLOWER).generate(worldObj, rand, i, this.getTerrainHeightAt(i - x, k -
                 * z, sizeYArray, xMin, zMin, zSize, asteroidY, asteroidSize), k); }
                 */
                // lakes
                for (final WorldGenData data : this.generatorsLakes) {
                    if (this.rand.nextInt(data.chance) == 0) {
                        final int i = this.rand.nextInt(16) + x + 8;
                        final int k = this.rand.nextInt(16) + z + 8;
                        data.worldGen.generate(
                                this.worldObj,
                                this.rand,
                                i,
                                this.getTerrainHeightAt(
                                        i - x,
                                        k - z,
                                        sizeYArray,
                                        xMin,
                                        zMin,
                                        zSize,
                                        asteroidY,
                                        asteroidSize),
                                k);
                    }
                }
            }
        }

        // Update all block lighting
        for (int xx = 0; xx < 16; xx++) {
            final int xPos = x + xx;
            for (int zz = 0; zz < 16; zz++) {
                final int zPos = z + zz;

                // Asteroid at min height 48, size 20, can't have lit blocks below 16
                for (int y = 16; y < 240; y++) {
                    this.worldObj.updateLightByType(EnumSkyBlock.Block, xPos, y, zPos);
                }
            }
        }

    }

    public void generateSkylightMap(final Chunk chunk, final int cx, final int cz) {
        for (int j = 0; j < 16; j++) {
            if (chunk.getBlockStorageArray()[j] == null)
                chunk.getBlockStorageArray()[j] = new ExtendedBlockStorage(j << 4, false);
        }

        final int i = chunk.getTopFilledSegment();
        chunk.heightMapMinimum = Integer.MAX_VALUE;

        for (int j = 0; j < 16; ++j) {
            int k = 0;

            while (k < 16) {
                chunk.precipitationHeightMap[j + (k << 4)] = -999;
                int y = i + 15;

                while (true) {
                    if (y > 0) {
                        if (chunk.func_150808_b(j, y - 1, k) == 0) {
                            --y;
                            continue;
                        }

                        chunk.heightMap[k << 4 | j] = y;

                        if (y < chunk.heightMapMinimum) {
                            chunk.heightMapMinimum = y;
                        }
                    }

                    ++k;
                    break;
                }
            }
        }

        for (final AsteroidData a : this.largeAsteroids) {
            int yMin = a.asteroidYArray - a.asteroidSizeArray;
            int yMax = a.asteroidYArray + a.asteroidSizeArray;
            int xMin = a.xMinArray;
            if (yMin < 0) yMin = 0;
            if (yMax > 255) yMax = 255;
            if (xMin == 0) xMin = 1;
            for (int x = a.xMax - 1; x >= xMin; x--) {
                for (int z = a.zMinArray; z < a.zMax; z++) {
                    for (int y = yMin; y < yMax; y++) {
                        if (chunk.getBlock(x - 1, y, z) instanceof BlockAir
                                && !(chunk.getBlock(x, y, z) instanceof BlockAir)) {
                            int count = 2;

                            if (x > 1 && chunk.getBlock(x - 2, y, z) instanceof BlockAir) count += 2;
                            if (x > 2) {
                                if (chunk.getBlock(x - 3, y, z) instanceof BlockAir) count += 2;
                                if (chunk.getBlock(x - 3, y + 1, z) instanceof BlockAir) count++;
                                if (chunk.getBlock(x - 3, y + 1, z) instanceof BlockAir) count++;
                                if (z > 0 && chunk.getBlock(x - 3, y, z - 1) instanceof BlockAir) count++;
                                if (z < 15 && chunk.getBlock(x - 3, y, z + 1) instanceof BlockAir) count++;
                            }
                            if (x > 3) {
                                if (chunk.getBlock(x - 4, y, z) instanceof BlockAir) count += 2;
                                if (chunk.getBlock(x - 4, y + 1, z) instanceof BlockAir) count++;
                                if (chunk.getBlock(x - 4, y + 1, z) instanceof BlockAir) count++;
                                if (z > 0 && !(chunk.getBlock(x - 4, y, z - 1) instanceof BlockAir)) count++;
                                if (z < 15 && !(chunk.getBlock(x - 4, y, z + 1) instanceof BlockAir)) count++;
                            }
                            count = this.adjustBrightnessValue(count);
                            chunk.func_150807_a(x - 1, y, z, GCBlocks.brightAir, 15 - count);
                            final ExtendedBlockStorage extendedblockstorage = chunk.getBlockStorageArray()[y >> 4];
                            if (extendedblockstorage != null) {
                                // here it seems to be the opposite
                                extendedblockstorage.setExtBlocklightValue(x - 1, y & 15, z, count + 2);
                            }
                        }
                    }
                }
            }
        }

        chunk.isModified = true;
    }

    /**
     * higher values = lighter dimension. Probably should not exceed 12
     */
    protected int adjustBrightnessValue(int count) {
        count += 2;

        if (count > 12) count = 12;

        return count;
    }

    @Override
    public boolean saveChunks(boolean p_73151_1_, IProgressUpdate p_73151_2_) {
        return true;
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public String makeString() {
        return "RandomLevelSource";
    }

    @Override
    public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType p_73155_1_, int p_73155_2_, int p_73155_3_,
            int p_73155_4_) {
        return null;
    }

    /**
     * Whether a large asteroid is located the provided coordinates
     *
     * @param x0 X-Coordinate to check, in Block Coords
     * @param z0 Z-Coordinate to check, in Block Coords
     * @return True if large asteroid is located here, False if not
     */
    public BlockVec3 isLargeAsteroidAt(final int x0, final int z0) {
        int xToCheck;
        int zToCheck;
        for (int i0 = 0; i0 <= 32; i0++) {
            for (int i1 = -i0; i1 <= i0; i1++) {
                xToCheck = (x0 >> 4) + i0;
                zToCheck = (z0 >> 4) + i1;

                if (this.isLargeAsteroidAt0(xToCheck * 16, zToCheck * 16)) {
                    return new BlockVec3(xToCheck * 16, 0, zToCheck * 16);
                }

                xToCheck = (x0 >> 4) + i0;
                zToCheck = (z0 >> 4) - i1;

                if (this.isLargeAsteroidAt0(xToCheck * 16, zToCheck * 16)) {
                    return new BlockVec3(xToCheck * 16, 0, zToCheck * 16);
                }

                xToCheck = (x0 >> 4) - i0;
                zToCheck = (z0 >> 4) + i1;

                if (this.isLargeAsteroidAt0(xToCheck * 16, zToCheck * 16)) {
                    return new BlockVec3(xToCheck * 16, 0, zToCheck * 16);
                }

                xToCheck = (x0 >> 4) - i0;
                zToCheck = (z0 >> 4) - i1;

                if (this.isLargeAsteroidAt0(xToCheck * 16, zToCheck * 16)) {
                    return new BlockVec3(xToCheck * 16, 0, zToCheck * 16);
                }
            }
        }

        return null;
    }

    private boolean isLargeAsteroidAt0(final int x0, final int z0) {
        for (int x = x0; x < x0 + CHUNK_SIZE_X; x += 2) {
            for (int z = z0; z < z0 + CHUNK_SIZE_Z; z += 2) {
                if (Math.abs(this.randFromPoint(x, z)) < (this.asteroidDensity.getNoise(x, z) + .4) / ASTEROID_CHANCE) {
                    return true;
                }
            }
        }

        return false;
    }

    private static class AsteroidData {

        public boolean isHollow;
        public float[] sizeYArray;
        public int xMinArray;
        public int zMinArray;
        public int xMax;
        public int zMax;
        public int zSizeArray;
        public int asteroidSizeArray;
        public int asteroidYArray;

        public AsteroidData(final boolean hollow, final float[] sizeYArray2, final int xMin, final int zMin,
                final int xmax, final int zmax, final int zSize, final int size, final int asteroidX,
                final int asteroidY, final int asteroidZ) {
            this.isHollow = hollow;
            this.sizeYArray = sizeYArray2.clone();
            this.xMinArray = xMin;
            this.zMinArray = zMin;
            this.xMax = xmax;
            this.zMax = zmax;
            this.zSizeArray = zSize;
            this.asteroidSizeArray = size;
            this.asteroidYArray = asteroidY;
        }
    }
}
