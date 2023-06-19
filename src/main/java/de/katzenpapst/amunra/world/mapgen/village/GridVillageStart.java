package de.katzenpapst.amunra.world.mapgen.village;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.helper.CoordHelper;
import de.katzenpapst.amunra.world.mapgen.BaseStructureComponent;
import de.katzenpapst.amunra.world.mapgen.BaseStructureStart;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class GridVillageStart extends BaseStructureStart {

    protected BlockMetaPair pathMaterial = ARBlocks.blockBasaltRegolith;

    protected BlockMetaPair wallMaterial = ARBlocks.blockAluCrate;
    protected BlockMetaPair floorMaterial = ARBlocks.blockSmoothBasalt;
    protected BlockMetaPair fillMaterial = ARBlocks.blockBasaltBrick;

    protected int numGridElements = 0;

    protected int gridSize = 9;

    protected int gridSideLength = 0;

    protected final Map<Integer, GridVillageComponent> componentsByGrid = new HashMap<>();

    /**
     * Instantiates the thing, the coords in here should be the START point
     */
    public GridVillageStart(final World world, final int chunkX, final int chunkZ, final Random rand) {

        super(world, chunkX, chunkZ, rand);
        final int startBlockX = CoordHelper.chunkToMinBlock(chunkX) + this.startX;
        final int startBlockZ = CoordHelper.chunkToMinBlock(chunkZ) + this.startZ;

        AmunRa.LOGGER.debug("Generating the Village at x={}, z={}", startBlockX, startBlockZ);
    }

    /**
     * From what I understood, this is called first, then, after this is done, populateChunk is called and after that,
     * the chunk is done and won't be touched again
     */
    @Override
    public boolean generateChunk(final int chunkX, final int chunkZ, final Block[] arrayOfIDs,
            final byte[] arrayOfMeta) {
        super.generateChunk(chunkX, chunkZ, arrayOfIDs, arrayOfMeta);
        this.drawGrid(chunkX, chunkZ, arrayOfIDs, arrayOfMeta);

        this.drawGridComponents(chunkX, chunkZ, arrayOfIDs, arrayOfMeta);
        return true;
    }

    public void setComponents(final List<BaseStructureComponent> components) {

        // byte should be enough for gridsize

        this.numGridElements = components.size();

        this.gridSideLength = (int) Math.ceil(Math.sqrt(this.numGridElements));

        // now the effective grid width is this.gridSize+3
        final int effectiveGridSize = this.gridSize + 3;

        final int squareWidth = effectiveGridSize * this.gridSideLength;

        final int startBlockX = CoordHelper.chunkToMinBlock(this.chunkX) + this.startX;
        final int startBlockZ = CoordHelper.chunkToMinBlock(this.chunkZ) + this.startZ;

        // my own structBB
        this.structBB = new StructureBoundingBox();
        this.structBB.minX = startBlockX - (int) Math.floor(squareWidth / 2);
        this.structBB.maxX = startBlockX + (int) Math.ceil(squareWidth / 2);
        this.structBB.minZ = startBlockZ - (int) Math.floor(squareWidth / 2);
        this.structBB.maxZ = startBlockZ + (int) Math.ceil(squareWidth / 2);
        this.structBB.minY = 0;
        this.structBB.maxY = 255;

        final int totalGridElems = this.gridSideLength * this.gridSideLength;
        // pad the components
        for (int i = this.numGridElements; i < totalGridElems; i++) {
            components.add(GridVillageComponent.DUMMY);
        }

        Collections.shuffle(components, this.rand);

        byte gridX = 0;
        byte gridZ = 0;
        for (final BaseStructureComponent comp : components) {
            if (comp == GridVillageComponent.DUMMY || !(comp instanceof GridVillageComponent)) {
                continue;
            }
            final int index = gridX + (gridZ << 8);

            final StructureBoundingBox componentBox = new StructureBoundingBox(
                    this.structBB.minX + effectiveGridSize * gridX + 2,
                    this.structBB.minZ + effectiveGridSize * gridZ + 2,
                    this.structBB.minX + effectiveGridSize * gridX + 1 + this.gridSize,
                    this.structBB.minZ + effectiveGridSize * gridZ + 1 + this.gridSize);

            componentBox.getXSize();
            comp.setStructureBoundingBox(componentBox);
            comp.setCoordMode(this.rand.nextInt(4));
            comp.setParent(this);
            this.componentsByGrid.put(index, (GridVillageComponent) comp);
            gridX++;
            if (gridX >= this.gridSideLength) {
                gridX = 0;
                gridZ++;
            }
        }

    }

    public BlockMetaPair getPathMaterial() {
        return this.pathMaterial;
    }

    public void setPathMaterial(final BlockMetaPair pathMaterial) {
        this.pathMaterial = pathMaterial;
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

    protected void drawStuffInGrid(final int chunkX, final int chunkZ, final int gridX, final int gridZ,
            final Block[] arrayOfIDs, final byte[] arrayOfMeta) {
        // now how do I calculate the grid's position?
        // I think it's
        final int effectiveGridSize = this.gridSize + 3;
        final int testX = this.structBB.minX + effectiveGridSize * gridX + 2;
        final int testZ = this.structBB.minZ + effectiveGridSize * gridZ + 2;

        // now try
        for (int x = 0; x < this.gridSize; x++) {
            for (int z = 0; z < this.gridSize; z++) {
                final int relX = CoordHelper.abs2rel(testX + x, chunkX);
                final int relZ = CoordHelper.abs2rel(testZ + z, chunkZ);
                this.placeBlockOnGround(
                        arrayOfIDs,
                        arrayOfMeta,
                        relX,
                        relZ,
                        this.wallMaterial.getBlock(),
                        this.wallMaterial.getMetadata());
            }
        }
    }

    protected void drawGrid(final int chunkX, final int chunkZ, final Block[] arrayOfIDs, final byte[] arrayOfMeta) {
        // hmmm how do I do this now?
        // length of the square
        final int effectiveGridSize = this.gridSize + 3;

        /*
         * StructureBoundingBox chunkBB = CoordHelper.getChunkBB(chunkX, chunkZ); int minGroundLevel =
         * this.getAverageGroundLevel(arrayOfIDs, arrayOfMeta, structBB, chunkBB, -1);
         */

        for (int x = this.structBB.minX; x < this.structBB.maxX; x++) {
            for (int z = this.structBB.minZ; z < this.structBB.maxZ; z++) {
                final int testX = x - this.structBB.minX;
                final int testZ = z - this.structBB.minZ;
                boolean drawX = false;
                boolean drawZ = false;

                if (testX != 0 && testX % effectiveGridSize == 0) {
                    drawX = true;
                }
                if (testZ != 0 && testZ % effectiveGridSize == 0) {
                    drawZ = true;
                }

                if (!drawX && !drawZ) {
                    continue;
                }
                final int relX = CoordHelper.abs2rel(x, chunkX);
                final int relZ = CoordHelper.abs2rel(z, chunkZ);

                if (drawX && drawZ) {
                    // crossing

                    this.placeBlockOnGround(
                            arrayOfIDs,
                            arrayOfMeta,
                            relX - 1,
                            relZ - 1,
                            this.pathMaterial.getBlock(),
                            this.pathMaterial.getMetadata());
                    this.placeBlockOnGround(
                            arrayOfIDs,
                            arrayOfMeta,
                            relX,
                            relZ - 1,
                            this.pathMaterial.getBlock(),
                            this.pathMaterial.getMetadata());
                    this.placeBlockOnGround(
                            arrayOfIDs,
                            arrayOfMeta,
                            relX + 1,
                            relZ - 1,
                            this.pathMaterial.getBlock(),
                            this.pathMaterial.getMetadata());

                    this.placeBlockOnGround(
                            arrayOfIDs,
                            arrayOfMeta,
                            relX - 1,
                            relZ,
                            this.pathMaterial.getBlock(),
                            this.pathMaterial.getMetadata());
                    this.placeBlockOnGround(
                            arrayOfIDs,
                            arrayOfMeta,
                            relX,
                            relZ,
                            this.pathMaterial.getBlock(),
                            this.pathMaterial.getMetadata());
                    this.placeBlockOnGround(
                            arrayOfIDs,
                            arrayOfMeta,
                            relX + 1,
                            relZ,
                            this.pathMaterial.getBlock(),
                            this.pathMaterial.getMetadata());

                    this.placeBlockOnGround(
                            arrayOfIDs,
                            arrayOfMeta,
                            relX - 1,
                            relZ + 1,
                            this.pathMaterial.getBlock(),
                            this.pathMaterial.getMetadata());
                    this.placeBlockOnGround(
                            arrayOfIDs,
                            arrayOfMeta,
                            relX,
                            relZ + 1,
                            this.pathMaterial.getBlock(),
                            this.pathMaterial.getMetadata());
                    this.placeBlockOnGround(
                            arrayOfIDs,
                            arrayOfMeta,
                            relX + 1,
                            relZ + 1,
                            this.pathMaterial.getBlock(),
                            this.pathMaterial.getMetadata());
                } else if (drawX) {
                    this.placeBlockOnGround(
                            arrayOfIDs,
                            arrayOfMeta,
                            relX - 1,
                            relZ,
                            this.pathMaterial.getBlock(),
                            this.pathMaterial.getMetadata());
                    this.placeBlockOnGround(
                            arrayOfIDs,
                            arrayOfMeta,
                            relX,
                            relZ,
                            this.pathMaterial.getBlock(),
                            this.pathMaterial.getMetadata());
                    this.placeBlockOnGround(
                            arrayOfIDs,
                            arrayOfMeta,
                            relX + 1,
                            relZ,
                            this.pathMaterial.getBlock(),
                            this.pathMaterial.getMetadata());
                } else if (drawZ) {
                    this.placeBlockOnGround(
                            arrayOfIDs,
                            arrayOfMeta,
                            relX,
                            relZ - 1,
                            this.pathMaterial.getBlock(),
                            this.pathMaterial.getMetadata());
                    this.placeBlockOnGround(
                            arrayOfIDs,
                            arrayOfMeta,
                            relX,
                            relZ,
                            this.pathMaterial.getBlock(),
                            this.pathMaterial.getMetadata());
                    this.placeBlockOnGround(
                            arrayOfIDs,
                            arrayOfMeta,
                            relX,
                            relZ + 1,
                            this.pathMaterial.getBlock(),
                            this.pathMaterial.getMetadata());
                }
            }
        }
    }

    /**
     * Places a block into the topmost solid block
     */
    protected void placeBlockOnGround(final Block[] arrayOfIDs, final byte[] arrayOfMeta, final int relX,
            final int relZ, final Block block, final int meta) {
        if (relX < 0 || relX >= 16 || relZ < 0 || relZ >= 16) {
            return;
        }
        final int y = BaseStructureComponent.getHighestSolidBlock(arrayOfIDs, arrayOfMeta, relX, relZ);
        BaseStructureComponent.placeBlockRel(arrayOfIDs, arrayOfMeta, relX, y - 1, relZ, block, meta);
    }

    protected void drawGridComponents(final int chunkX, final int chunkZ, final Block[] arrayOfIDs,
            final byte[] arrayOfMeta) {

        // int effectiveGridSize = this.gridSize+3;

        final StructureBoundingBox chunkBox = CoordHelper.getChunkBB(chunkX, chunkZ);

        for (int gridX = 0; gridX < this.gridSideLength; gridX++) {
            for (int gridZ = 0; gridZ < this.gridSideLength; gridZ++) {

                final int index = gridX + (gridZ << 8);

                if (!this.componentsByGrid.containsKey(index)) {
                    continue;
                }

                final GridVillageComponent curComp = this.componentsByGrid.get(index);

                // fail for chunk z = -28
                // ALL components should intersect with -27
                if (curComp.getStructureBoundingBox().intersectsWith(chunkBox)) {
                    // continue; // not in this chunk

                    curComp.generateChunk(chunkX, chunkZ, arrayOfIDs, arrayOfMeta);
                }

            }
        }
    }

}
