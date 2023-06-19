package de.katzenpapst.amunra.world.mapgen.village;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityCreature;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import de.katzenpapst.amunra.helper.CoordHelper;
import de.katzenpapst.amunra.mob.entity.EntityRobotVillager;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.core.blocks.GCBlocks;

public class BoxHouseComponent extends GridVillageComponent {

    protected int houseHeight = 5;

    @Override
    public boolean generateChunk(final int chunkX, final int chunkZ, final Block[] blocks, final byte[] metas) {

        // now, how to get the height?
        final StructureBoundingBox chunkBB = CoordHelper.getChunkBB(chunkX, chunkZ);// new StructureBoundingBox((chunkX
                                                                                    // << 4),
        final int fallbackGround = this.parent.getWorldGroundLevel();
        if (this.groundLevel == -1) {
            this.groundLevel = getAverageGroundLevel(
                    blocks,
                    metas,
                    this.getStructureBoundingBox(),
                    chunkBB,
                    fallbackGround);
            if (this.groundLevel == -1) {
                this.groundLevel = fallbackGround; // but this shouldn't even happen...
            }
        }

        final StructureBoundingBox myBB = this.getStructureBoundingBox();
        final BlockMetaPair mat = ((GridVillageStart) this.parent).getWallMaterial();
        final BlockMetaPair floor = ((GridVillageStart) this.parent).getFloorMaterial();
        final BlockMetaPair padding = ((GridVillageStart) this.parent).getFillMaterial();
        final BlockMetaPair path = ((GridVillageStart) this.parent).getPathMaterial();
        final BlockMetaPair glassPane = new BlockMetaPair(Blocks.glass_pane, (byte) 0);
        final BlockMetaPair air = new BlockMetaPair(Blocks.air, (byte) 0);

        // draw floor first
        final int startX = 1;
        final int stopX = myBB.getXSize() - 2;
        final int startZ = 1;
        final int stopZ = myBB.getZSize() - 2;

        final int xCenter = (int) Math.ceil((stopX - startX) / 2 + startX);
        final int zCenter = (int) Math.ceil((stopZ - startZ) / 2 + startZ);
        for (int x = startX; x <= stopX; x++) {
            for (int z = startZ; z <= stopZ; z++) {

                final int highestGroundBlock = this.getHighestSolidBlockInBB(blocks, metas, chunkX, chunkZ, x, z);
                if (highestGroundBlock == -1) {
                    continue; // that should mean that we aren't in the right chunk
                }

                // now fill
                for (int y = highestGroundBlock - 1; y < this.groundLevel; y++) {
                    // padding
                    this.placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, y, z, padding);
                }
                // floor
                this.placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, this.groundLevel - 1, z, floor);

                // now try spawing villagers...
                if (x == xCenter && z == zCenter) {
                    this.spawnVillager(x, this.groundLevel, z);
                }

                // now walls, most complex part
                for (int y = 0; y < this.houseHeight - 1; y++) {
                    // wall check
                    if (x == startX || x == stopX || z == startZ || z == stopZ) {

                        if (
                        // this should just continue working...
                        this.shouldGenerateWindowHere(x, y, z, xCenter, startX, stopX, startZ, stopZ)) {

                            this.placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, this.groundLevel + y, z, glassPane);
                        } else if (z == startZ && x == xCenter && (y == 0 || y == 1)) {
                            // TODO figure out how to do doors
                            this.placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, this.groundLevel + y, z, air);
                        } else {
                            // just place a wall, for now
                            this.placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, this.groundLevel + y, z, mat);
                        }
                    } else { // end of wall check
                        // this is interior
                        this.placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, this.groundLevel + y, z, air);

                        // maybe place torches?
                        if (x == startX + 1 && z == zCenter && y == 2) {
                            this.placeBlockRel2BB(
                                    blocks,
                                    metas,
                                    chunkX,
                                    chunkZ,
                                    x,
                                    this.groundLevel + y,
                                    z,
                                    GCBlocks.glowstoneTorch,
                                    rotateTorchMetadata(1, this.coordMode));
                        } else if (x == stopX - 1 && z == zCenter && y == 2) {
                            this.placeBlockRel2BB(
                                    blocks,
                                    metas,
                                    chunkX,
                                    chunkZ,
                                    x,
                                    this.groundLevel + y,
                                    z,
                                    GCBlocks.glowstoneTorch,
                                    rotateTorchMetadata(2, this.coordMode));
                        } else if (z == startZ + 1 && x == xCenter && y == 2) {
                            this.placeBlockRel2BB(
                                    blocks,
                                    metas,
                                    chunkX,
                                    chunkZ,
                                    x,
                                    this.groundLevel + y,
                                    z,
                                    GCBlocks.glowstoneTorch,
                                    rotateTorchMetadata(3, this.coordMode));
                            // rotate to -z?
                        } else if (z == stopZ - 1 && x == xCenter && y == 2) {
                            this.placeBlockRel2BB(
                                    blocks,
                                    metas,
                                    chunkX,
                                    chunkZ,
                                    x,
                                    this.groundLevel + y,
                                    z,
                                    GCBlocks.glowstoneTorch,
                                    rotateTorchMetadata(4, this.coordMode));
                            // rotate to -z?
                        }
                    }
                }
                // finally, roof
                this.placeBlockRel2BB(
                        blocks,
                        metas,
                        chunkX,
                        chunkZ,
                        x,
                        this.groundLevel + this.houseHeight - 1,
                        z,
                        mat);

            }
        }
        final int highestGroundBlock = this
                .getHighestSolidBlockInBB(blocks, metas, chunkX, chunkZ, xCenter, startZ - 1);
        // stuff before the door
        if (highestGroundBlock != -1) {
            // groundLevel and groundLevel +1 should be free, and potentially place
            // a block at groundLevel-1
            if (highestGroundBlock >= this.groundLevel) {
                this.placeBlockRel2BB(blocks, metas, chunkX, chunkZ, xCenter, this.groundLevel, startZ - 1, air);
                this.placeBlockRel2BB(blocks, metas, chunkX, chunkZ, xCenter, this.groundLevel + 1, startZ - 1, air);
            }
            // place the other stuff anyway...
            this.placeBlockRel2BB(blocks, metas, chunkX, chunkZ, xCenter, this.groundLevel - 1, startZ - 1, path);
            this.placeBlockRel2BB(blocks, metas, chunkX, chunkZ, xCenter, this.groundLevel - 2, startZ - 1, padding);
        }

        return true;
    }

    protected void spawnVillager(final int x, final int y, final int z) {
        final EntityCreature villager = new EntityRobotVillager(this.parent.getWorld());
        villager.onSpawnWithEgg(null);// NO IDEA
        final int xOffset = this.getXWithOffset(x, z);
        final int zOffset = this.getZWithOffset(x, z);
        this.parent.spawnLater(villager, xOffset, y, zOffset);
    }

    private boolean shouldGenerateWindowHere(final int x, final int y, final int z, final int doorPos, final int startX,
            final int stopX, final int startZ, final int stopZ) {
        if (y > this.houseHeight - 3 || y < 1) {
            return false;
        }

        if ((x == startX || x == stopX) && z > startZ + 1 && z < stopZ - 1 && (z - startZ) % 2 == 0) {
            return true;
        }

        if ((z == startZ || z == stopZ) && x > startX + 1 && x < stopX - 1 && (x - startX) % 2 == 0) {
            return z != startZ || (x != doorPos + 1 && x != doorPos - 1);
        }

        return false;
    }
}
