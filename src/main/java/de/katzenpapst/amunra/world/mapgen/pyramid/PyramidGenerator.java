package de.katzenpapst.amunra.world.mapgen.pyramid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.util.MathHelper;

import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.world.mapgen.BaseStructureComponent;
import de.katzenpapst.amunra.world.mapgen.BaseStructureStart;
import de.katzenpapst.amunra.world.mapgen.StructureGenerator;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class PyramidGenerator extends StructureGenerator {

    protected BlockMetaPair wallMaterial = ARBlocks.blockAluCrate;
    protected BlockMetaPair floorMaterial = ARBlocks.blockSmoothBasalt;
    protected BlockMetaPair fillMaterial = ARBlocks.blockBasaltBrick;

    protected final List<SubComponentData> components = new ArrayList<>();
    protected final List<SubComponentData> potentialMainRooms = new ArrayList<>();

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
    }

    public void addComponentType(final Class<? extends PyramidRoom> clazz, final float probability) {
        this.addComponentType(clazz, probability, 0, 0);
    }

    public void addComponentType(final Class<? extends PyramidRoom> clazz, final float probability, final int minAmount,
            final int maxAmount) {
        this.components.add(new SubComponentData(clazz, probability, minAmount, maxAmount));
    }

    public void addMainRoomType(final Class<? extends PyramidRoom> clazz, final float probability) {
        this.potentialMainRooms.add(new SubComponentData(clazz, probability, 0, 0));
    }

    @Override
    protected BaseStructureStart createNewStructure(final int xChunkCoord, final int zChunkCoord) {
        final Pyramid p = new Pyramid(this.worldObj, xChunkCoord, zChunkCoord, this.rand);
        p.setFillMaterial(this.fillMaterial);
        p.setFloorMaterial(this.floorMaterial);
        p.setWallMaterial(this.wallMaterial);

        final Random rand4structure = new Random(this.worldObj.getSeed() ^ this.getSalt() ^ xChunkCoord ^ zChunkCoord);

        final List<BaseStructureComponent> compList = this.generateSubComponents(this.components, rand4structure, 12);

        p.setSmallRooms(compList);

        p.setMainRoom((PyramidRoom) this.generateOneComponent(this.potentialMainRooms, rand4structure));

        return p;
    }

    @Override
    public String getName() {
        return "Pyramid";
    }

    @Override
    protected long getSalt() {
        return 549865610521L;
    }

    public BlockMetaPair getWallMaterial() {
        return this.wallMaterial;
    }

    public void setWallMaterial(final BlockMetaPair wallMaterial) {
        this.wallMaterial = wallMaterial;
    }

    public BlockMetaPair getFloorMaterial() {
        return this.floorMaterial;
    }

    public void setFloorMaterial(final BlockMetaPair floorMaterial) {
        this.floorMaterial = floorMaterial;
    }

    public BlockMetaPair getFillMaterial() {
        return this.fillMaterial;
    }

    public void setFillMaterial(final BlockMetaPair fillMaterial) {
        this.fillMaterial = fillMaterial;
    }

}
