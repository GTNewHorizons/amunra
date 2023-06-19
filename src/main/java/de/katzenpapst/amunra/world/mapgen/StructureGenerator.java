package de.katzenpapst.amunra.world.mapgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import de.katzenpapst.amunra.AmunRa;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.MapGenBaseMeta;

/**
 * I'll do a subdivision now: StructureGenerator and Structure For each StructureGenerator there is a subclass of
 * BaseStructure which it generates
 */
abstract public class StructureGenerator extends MapGenBaseMeta {

    public class SubComponentData {

        public Class<? extends BaseStructureComponent> clazz;
        public float probability;
        public int minAmount;
        public int maxAmount;

        public SubComponentData(final Class<? extends BaseStructureComponent> clazz, final float probability,
                final int minAmount, final int maxAmount) {
            this.clazz = clazz;
            this.probability = probability;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }

        public SubComponentData copy() {
            return new SubComponentData(this.clazz, this.probability, this.minAmount, this.maxAmount);
        }

        public float getProbability() {
            return this.probability;
        }
    }

    /**
     * Clones an ArrayList of SubComponentData
     * <p>
     * helper for generateSubComponents
     */
    private List<SubComponentData> cloneSubComponentList(final List<SubComponentData> subCompData) {
        return subCompData.stream().map(SubComponentData::copy).collect(Collectors.toList());
    }

    /**
     * Calculates the sum of all SubComponentData's probability values
     * <p>
     * helper for generateSubComponents
     */
    private float getProbabilityMaximum(final List<SubComponentData> subCompData) {
        return subCompData.parallelStream().map(SubComponentData::getProbability).reduce(0.0f, Float::sum);
    }

    /**
     * Just takes the "clazz" member of the entry and tries to create a new instance of it
     * <p>
     * helper for generateSubComponents
     */
    private BaseStructureComponent generateComponent(final SubComponentData entry) {
        try {
            return entry.clazz.getConstructor().newInstance();
        } catch (final Exception e) {
            AmunRa.LOGGER.error("Instantiating " + entry.clazz.getCanonicalName() + " failed", e);
        }
        return null;
    }

    /**
     * Tries to find a sensible limit (aka total maximum of components) for the given list of SubComponentData
     * <p>
     * helper for generateSubComponents
     */
    private int findComponentLimit(final List<SubComponentData> subCompData, final Random rand) {
        int minComponents = 0;
        int maxComponents = 0;
        boolean everythingHasMax = true;
        for (final SubComponentData entry : subCompData) {
            minComponents += entry.minAmount;
            if (entry.maxAmount > 0) {
                maxComponents += entry.maxAmount;
            } else {
                everythingHasMax = false;
            }
        }
        if (everythingHasMax) {

            return MathHelper.getRandomIntegerInRange(rand, minComponents, maxComponents);
        }
        // otherwise dunno. Kinda guess something?

        return MathHelper.getRandomIntegerInRange(rand, minComponents, minComponents + subCompData.size());

    }

    /**
     * Prepares a list of components from a given array of SubComponentData
     *
     * @param subCompData ArrayList of SubComponentData
     * @param rand        the Random object to use
     * @param limit       the result will not have more entries than this. if 0, a random limit will be used
     */
    protected List<BaseStructureComponent> generateSubComponents(final List<SubComponentData> subCompData,
            final Random rand, int limit) {
        final List<BaseStructureComponent> compList = new ArrayList<>();
        final Map<String, Integer> typeAmountMapping = new HashMap<>();

        if (limit <= 0) {
            limit = this.findComponentLimit(subCompData, rand);
        }

        final List<SubComponentData> curComponents = this.cloneSubComponentList(subCompData);

        do {
            final Iterator<SubComponentData> itr = curComponents.iterator();
            float curValue = 0.0F;

            float total = this.getProbabilityMaximum(curComponents);
            final float curRandom = rand.nextFloat() * total;

            // find an entry
            while (itr.hasNext()) {
                final SubComponentData entry = itr.next();
                final String typeName = entry.clazz.getCanonicalName();

                typeAmountMapping.putIfAbsent(typeName, 0);

                int curAmount = typeAmountMapping.get(typeName);

                final boolean isBelowMinimum = entry.minAmount > 0 && curAmount < entry.minAmount;

                if (
                // automatically pick it if it's minimum isn't reached
                isBelowMinimum ||
                // or if it's in the current rand's range
                        curValue <= curRandom && curRandom <= entry.probability + curValue) {
                    // pick this
                    final BaseStructureComponent cmp = this.generateComponent(entry);
                    if (cmp != null) {
                        compList.add(cmp);
                    }
                    curAmount = curAmount + 1;
                    typeAmountMapping.put(typeName, curAmount);

                    final boolean isMaximumReached = entry.maxAmount > 0 && curAmount >= entry.maxAmount;

                    if (isMaximumReached || cmp == null) {
                        itr.remove();
                        total = this.getProbabilityMaximum(curComponents);
                    }
                    break;
                }
                curValue += entry.probability;
            }

        } while (compList.size() < limit && !curComponents.isEmpty());

        return compList;
    }

    /**
     * Generate one single component from the list. Min and max values from SubComponentData will be ignored
     */
    protected BaseStructureComponent generateOneComponent(final List<SubComponentData> subCompData, final Random rand) {

        BaseStructureComponent result = null;
        Class<? extends BaseStructureComponent> resultClass = null;

        for (final SubComponentData entry : subCompData) {
            if (entry.probability < rand.nextFloat()) {
                resultClass = entry.clazz;
                break;
            }
        }
        if (resultClass == null) {
            // as fallback
            final int i = MathHelper.getRandomIntegerInRange(rand, 0, subCompData.size() - 1);
            resultClass = subCompData.get(i).clazz;
        }

        try {

            result = resultClass.getConstructor().newInstance();
        } catch (final Exception e) {
            AmunRa.LOGGER.error("Instantiating " + resultClass.getCanonicalName() + " failed", e);
        }

        return result;
    }

    protected IChunkProvider chunkProvider = null;

    public class BaseStructureMap extends HashMap<Long, BaseStructureStart> {

        private static final long serialVersionUID = -4123587272811107730L;
    }

    protected BaseStructureMap structureMap = new BaseStructureMap();

    public StructureGenerator() {}

    /**
     * Return some random long for a seed
     */
    abstract protected long getSalt();

    /**
     * Return true if this structure (or any part of it) should be generated in this chunk
     */
    abstract protected boolean canGenerateHere(int chunkX, int chunkZ, Random rand);

    /**
     * Create and maybe somehow init an instance of BaseStructure here
     */
    abstract protected BaseStructureStart createNewStructure(int xChunkCoord, int zChunkCoord);

    abstract public String getName();

    /**
     *
     *
     * @param chunkProvider   current chunk provider
     * @param world           the world
     * @param origXChunkCoord x coord of the currently generating chunk
     * @param origZChunkCoord z coord of the currently generating chunk
     * @param blocks          blocks array
     * @param metadata        metadata array
     */
    @Override
    public void generate(final IChunkProvider chunkProvider, final World world, final int origXChunkCoord,
            final int origZChunkCoord, final Block[] blocks, final byte[] metadata) {
        this.worldObj = world;
        this.chunkProvider = chunkProvider;

        for (int xChunkCoord = origXChunkCoord - this.range; xChunkCoord
                <= origXChunkCoord + this.range; ++xChunkCoord) {
            for (int zChunkCoord = origZChunkCoord - this.range; zChunkCoord
                    <= origZChunkCoord + this.range; ++zChunkCoord) {
                if (this.canGenerateHere(xChunkCoord, zChunkCoord, this.rand)) {
                    this.recursiveGenerate(
                            world,
                            xChunkCoord,
                            zChunkCoord,
                            origXChunkCoord,
                            origZChunkCoord,
                            blocks,
                            metadata);
                }
            }
        }
    }

    @Override
    protected void recursiveGenerate(final World par1World, final int xChunkCoord, final int zChunkCoord,
            final int origXChunkCoord, final int origZChunkCoord, final Block[] arrayOfIDs, final byte[] arrayOfMeta) {
        this.makeStructure(
                par1World,
                xChunkCoord,
                zChunkCoord,
                origXChunkCoord,
                origZChunkCoord,
                arrayOfIDs,
                arrayOfMeta);

    }

    /**
     * Adds stuff like mobs or tileentities, which can't be added in the step where the block and meta arrays are being
     * filled
     */
    public void populate(final IChunkProvider chunkProvider, final World world, final int origXChunkCoord,
            final int origZChunkCoord) {
        this.worldObj = world;
        this.chunkProvider = chunkProvider;

        for (int xChunkCoord = origXChunkCoord - this.range; xChunkCoord
                <= origXChunkCoord + this.range; ++xChunkCoord) {
            for (int zChunkCoord = origZChunkCoord - this.range; zChunkCoord
                    <= origZChunkCoord + this.range; ++zChunkCoord) {
                if (this.canGenerateHere(xChunkCoord, zChunkCoord, this.rand)) {
                    this.recursivePopulate(world, xChunkCoord, zChunkCoord, origXChunkCoord, origZChunkCoord);
                }
            }
        }
    }

    protected void recursivePopulate(final World world, final int xChunkCoord, final int zChunkCoord,
            final int origXChunkCoord, final int origZChunkCoord) {
        final Long key = ChunkCoordIntPair.chunkXZ2Int(xChunkCoord, zChunkCoord);
        if (this.structureMap.containsKey(key)) {
            final BaseStructureStart start = this.structureMap.get(key);
            start.populateChunk(world, origXChunkCoord, origZChunkCoord);
        } else {
            AmunRa.LOGGER.warn(
                    "No {} for population for coords {}/{}, that's weird...",
                    this.getName(),
                    xChunkCoord * 16,
                    zChunkCoord * 16);
        }

    }

    /**
     * Creates or gets an instance of BaseStructure, then makes it generate the current chunk
     */
    protected void makeStructure(final World world, final int xChunkCoord, final int zChunkCoord,
            final int origXChunkCoord, final int origZChunkCoord, final Block[] arrayOfIDs, final byte[] arrayOfMeta) {
        final Long key = ChunkCoordIntPair.chunkXZ2Int(xChunkCoord, zChunkCoord);
        BaseStructureStart start = null;
        if (!this.structureMap.containsKey(key)) {
            start = this.createNewStructure(xChunkCoord, zChunkCoord);
            this.structureMap.put(key, start);
        } else {
            start = this.structureMap.get(key);
        }
        start.generateChunk(origXChunkCoord, origZChunkCoord, arrayOfIDs, arrayOfMeta);

    }

}
