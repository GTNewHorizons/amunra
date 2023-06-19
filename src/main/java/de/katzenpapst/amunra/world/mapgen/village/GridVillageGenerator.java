package de.katzenpapst.amunra.world.mapgen.village;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.util.MathHelper;

import de.katzenpapst.amunra.world.mapgen.BaseStructureComponent;
import de.katzenpapst.amunra.world.mapgen.BaseStructureStart;
import de.katzenpapst.amunra.world.mapgen.StructureGenerator;

public class GridVillageGenerator extends StructureGenerator {

    /*
     * protected class ComponentEntry { public Class<? extends GridVillageComponent> clazz; public float probability;
     * public int minAmount; public int maxAmount; public ComponentEntry(Class<? extends GridVillageComponent> clazz,
     * float probability, int minAmount, int maxAmount) { this.clazz = clazz; this.probability = probability;
     * this.minAmount = minAmount; this.maxAmount = maxAmount; } }
     */
    protected final List<SubComponentData> components = new ArrayList<>();

    protected int gridSize = 32;

    protected final Map<Long, GridVillageStart> structureMap = new HashMap<>();

    public void addComponentType(final Class<? extends GridVillageComponent> clazz, final float probability) {
        this.addComponentType(clazz, probability, 0, 0);
    }

    public void addComponentType(final Class<? extends GridVillageComponent> clazz, final float probability,
            final int minAmount, final int maxAmount) {
        this.components.add(new SubComponentData(clazz, probability, minAmount, maxAmount));
    }

    @Override
    protected boolean canGenerateHere(final int chunkX, final int chunkZ, final Random rand) {
        final int rangeShift = 5;
        final int range = 1 << rangeShift;
        final int superchunkX = chunkX >> rangeShift;
        final int superchunkZ = chunkZ >> rangeShift;

        final int chunkStartX = superchunkX << rangeShift;
        final int chunkStartZ = superchunkZ << rangeShift;
        final int chunkEndX = chunkStartX + range - 1;
        final int chunkEndZ = chunkStartZ + range - 1;
        // this square of chunk coords superchunkX,superchunkX+range-1 and superchunkZ,superchunkZ+range-1
        // now could contain a village
        this.rand.setSeed(this.worldObj.getSeed() ^ this.getSalt() ^ superchunkX ^ superchunkZ);

        final int actualVillageX = MathHelper.getRandomIntegerInRange(this.rand, chunkStartX, chunkEndX);
        final int actualVillageZ = MathHelper.getRandomIntegerInRange(this.rand, chunkStartZ, chunkEndZ);

        return chunkX == actualVillageX && chunkZ == actualVillageZ;

        /*
         * final long randX = chunkX * getSalt(); final long randZ = chunkZ * getSalt(); this.rand.setSeed(randX ^ randZ
         * ^ this.worldObj.getSeed()); return this.rand.nextInt(700) == 0;
         */
    }

    @Override
    protected BaseStructureStart createNewStructure(final int xChunkCoord, final int zChunkCoord) {
        final Random rand4structure = new Random(this.worldObj.getSeed() ^ this.getSalt() ^ xChunkCoord ^ zChunkCoord);
        final GridVillageStart start = new GridVillageStart(this.worldObj, xChunkCoord, zChunkCoord, rand4structure);
        final List<BaseStructureComponent> compList = this.generateSubComponents(this.components, rand4structure, 0);
        start.setComponents(compList);

        return start;
    }

    @Override
    public String getName() {
        return "GridVillage";
    }

    @Override
    protected long getSalt() {
        return 1098540180186541L;
    }

}
