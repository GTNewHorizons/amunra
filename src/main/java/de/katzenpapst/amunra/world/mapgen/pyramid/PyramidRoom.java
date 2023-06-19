package de.katzenpapst.amunra.world.mapgen.pyramid;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import de.katzenpapst.amunra.helper.CoordHelper;
import de.katzenpapst.amunra.world.mapgen.BaseStructureComponent;
import de.katzenpapst.amunra.world.mapgen.populator.TouchBlock;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class PyramidRoom extends BaseStructureComponent {

    protected StructureBoundingBox entranceBB;
    protected StructureBoundingBox roomBB;

    protected boolean placeGlowstoneInEdges = true;

    private boolean roomHeightFixed = false;

    protected int floorLevel;

    public void setBoundingBoxes(final StructureBoundingBox entranceBB, final StructureBoundingBox roomBB) {
        this.entranceBB = entranceBB;
        this.roomBB = roomBB;

        final StructureBoundingBox totalBox = new StructureBoundingBox(roomBB);
        totalBox.expandTo(entranceBB);
        this.setStructureBoundingBox(totalBox);
    }

    public StructureBoundingBox getEntranceBB() {
        return this.entranceBB;
    }

    @Override
    public boolean generateChunk(final int chunkX, final int chunkZ, final Block[] arrayOfIDs,
            final byte[] arrayOfMeta) {

        final StructureBoundingBox chunkBB = CoordHelper.getChunkBB(chunkX, chunkZ);

        final BlockMetaPair floorMat = ((Pyramid) this.parent).getFloorMaterial();

        this.floorLevel = this.parent.getGroundLevel() + 7;
        if (!this.roomHeightFixed) {
            this.roomBB.minY += this.floorLevel;
            this.roomBB.maxY += this.floorLevel;
            this.roomHeightFixed = true;
        }
        final StructureBoundingBox actualRoomBB = intersectBoundingBoxes(chunkBB, this.roomBB);
        if (actualRoomBB != null) {
            for (int x = actualRoomBB.minX; x <= actualRoomBB.maxX; x++) {
                for (int y = actualRoomBB.minY - 1; y <= actualRoomBB.maxY; y++) {
                    for (int z = actualRoomBB.minZ; z <= actualRoomBB.maxZ; z++) {
                        if (y >= actualRoomBB.minY) {

                            placeBlockAbs(arrayOfIDs, arrayOfMeta, x, y, z, chunkX, chunkZ, Blocks.air, (byte) 0);

                        } else {
                            placeBlockAbs(
                                    arrayOfIDs,
                                    arrayOfMeta,
                                    x,
                                    y,
                                    z,
                                    chunkX,
                                    chunkZ,
                                    floorMat.getBlock(),
                                    floorMat.getMetadata());
                        }
                    }
                }
            }
        }

        this.entranceBB.minY = this.roomBB.minY;
        this.entranceBB.maxY = this.entranceBB.minY + 3;

        this.makeEntrance(arrayOfIDs, arrayOfMeta, chunkBB, chunkX, chunkZ, floorMat);

        if (this.placeGlowstoneInEdges) {
            this.drawCornerColumns(actualRoomBB.minY, actualRoomBB.maxY, chunkX, chunkZ, arrayOfIDs, arrayOfMeta);
        }

        return true;
    }

    protected void drawCornerColumns(final int yMin, final int yMax, final int chunkX, final int chunkZ,
            final Block[] arrayOfIDs, final byte[] arrayOfMeta) {

        for (int y = yMin; y <= yMax; y++) {
            if (placeBlockAbs(
                    arrayOfIDs,
                    arrayOfMeta,
                    this.roomBB.minX,
                    y,
                    this.roomBB.minZ,
                    chunkX,
                    chunkZ,
                    Blocks.glowstone,
                    (byte) 0) && y == yMin) {
                // trigger the populator
                this.parent.addPopulator(new TouchBlock(this.roomBB.minX, y, this.roomBB.minZ));
            }

            if (placeBlockAbs(
                    arrayOfIDs,
                    arrayOfMeta,
                    this.roomBB.maxX,
                    y,
                    this.roomBB.minZ,
                    chunkX,
                    chunkZ,
                    Blocks.glowstone,
                    (byte) 0) && y == yMin) {
                // trigger the populator
                this.parent.addPopulator(new TouchBlock(this.roomBB.maxX, y, this.roomBB.minZ));
            }

            if (placeBlockAbs(
                    arrayOfIDs,
                    arrayOfMeta,
                    this.roomBB.minX,
                    y,
                    this.roomBB.maxZ,
                    chunkX,
                    chunkZ,
                    Blocks.glowstone,
                    (byte) 0) && y == yMin) {
                // trigger the populator
                this.parent.addPopulator(new TouchBlock(this.roomBB.minX, y, this.roomBB.maxZ));
            }

            if (placeBlockAbs(
                    arrayOfIDs,
                    arrayOfMeta,
                    this.roomBB.maxX,
                    y,
                    this.roomBB.maxZ,
                    chunkX,
                    chunkZ,
                    Blocks.glowstone,
                    (byte) 0) && y == yMin) {
                // trigger the populator
                this.parent.addPopulator(new TouchBlock(this.roomBB.maxX, y, this.roomBB.maxZ));
            }
        }

    }

    protected void makeEntrance(final Block[] arrayOfIDs, final byte[] arrayOfMeta, final StructureBoundingBox chunkBB,
            final int chunkX, final int chunkZ, final BlockMetaPair floorMat) {
        final StructureBoundingBox entrBoxIntersect = intersectBoundingBoxes(this.entranceBB, chunkBB);

        if (entrBoxIntersect != null) {
            for (int x = entrBoxIntersect.minX; x <= entrBoxIntersect.maxX; x++) {
                for (int y = entrBoxIntersect.minY - 1; y <= entrBoxIntersect.maxY; y++) {
                    for (int z = entrBoxIntersect.minZ; z <= entrBoxIntersect.maxZ; z++) {
                        if (y >= entrBoxIntersect.minY) {
                            placeBlockAbs(arrayOfIDs, arrayOfMeta, x, y, z, chunkX, chunkZ, Blocks.air, (byte) 0);
                        } else {
                            placeBlockAbs(
                                    arrayOfIDs,
                                    arrayOfMeta,
                                    x,
                                    y,
                                    z,
                                    chunkX,
                                    chunkZ,
                                    floorMat.getBlock(),
                                    floorMat.getMetadata());
                        }
                    }
                }
            }
        }
    }

}
