package de.katzenpapst.amunra.world.mapgen.village;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.helper.CoordHelper;
import de.katzenpapst.amunra.world.mapgen.populator.TouchSolarPanel;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.core.blocks.BlockSolar;
import micdoodle8.mods.galacticraft.core.blocks.GCBlocks;

public class SolarField extends GridVillageComponent {

    @Override
    public boolean generateChunk(final int chunkX, final int chunkZ, final Block[] blocks, final byte[] metas) {

        // now, how to get the height?
        final StructureBoundingBox chunkBB = CoordHelper.getChunkBB(chunkX, chunkZ);
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
        final BlockMetaPair floor = ((GridVillageStart) this.parent).getFloorMaterial();
        final BlockMetaPair padding = ((GridVillageStart) this.parent).getFillMaterial();

        // draw floor first
        final int startX = 0;
        final int stopX = myBB.getXSize();
        final int startZ = 0;
        final int stopZ = myBB.getZSize();

        final int zCenter = (int) Math.ceil((stopZ - startZ) / 2 + startZ);

        final int aluWireMetadata = AmunRa.config.villageAdvancedMachines ? 1 : 0;

        for (int x = startX; x < stopX; x++) {
            for (int z = startZ; z < stopZ; z++) {

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

                // clear stuff
                for (int y = this.groundLevel; y < 255; y++) {
                    this.placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, y, z, Blocks.air, 0);
                }

                // place stuff?
                if (x == startX + 2 || x == stopX - 3) {
                    if (z == startZ + 2) {
                        // place collectors, facing towards +z
                        /*
                         * if(placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, groundLevel, z, GCBlocks.solarPanel,
                         * this.rotateStandardMetadata(0, this.coordMode))) { this.parent.addPopulator(new
                         * TouchSolarPanel(getXWithOffset(x, z), groundLevel, getZWithOffset(x, z))); }
                         */
                        this.placeSolarPanel(blocks, metas, chunkX, chunkZ, x, this.groundLevel, z, 0);

                    } else if (z == stopZ - 3) {
                        // place collectors, facing towards -z
                        this.placeSolarPanel(blocks, metas, chunkX, chunkZ, x, this.groundLevel, z, 1);
                        /*
                         * if(placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, groundLevel, z, GCBlocks.solarPanel,
                         * this.rotateStandardMetadata(1, this.coordMode))) { this.parent.addPopulator(new
                         * TouchSolarPanel(getXWithOffset(x, z), groundLevel, getZWithOffset(x, z))); }
                         */
                    } else if (z > startZ + 2 && z < stopZ - 3) {
                        this.placeBlockRel2BB(
                                blocks,
                                metas,
                                chunkX,
                                chunkZ,
                                x,
                                this.groundLevel,
                                z,
                                GCBlocks.aluminumWire,
                                aluWireMetadata);

                    }
                } else if (z == zCenter && x > startX + 2 && x < stopX - 3) {
                    this.placeBlockRel2BB(
                            blocks,
                            metas,
                            chunkX,
                            chunkZ,
                            x,
                            this.groundLevel,
                            z,
                            GCBlocks.aluminumWire,
                            aluWireMetadata);

                } else if (x == startX + 1 && z == zCenter) {
                    // ok now how to rotate it?
                    // I think the first 2 bits are the orientation
                    int storageMetadata = rotateStandardMetadata(2, this.coordMode);
                    if (AmunRa.config.villageAdvancedMachines) {
                        storageMetadata = storageMetadata | 8;
                    }
                    this.placeBlockRel2BB(
                            blocks,
                            metas,
                            chunkX,
                            chunkZ,
                            x,
                            this.groundLevel,
                            z,
                            GCBlocks.machineTiered,
                            storageMetadata);
                }

            }
        }

        return true;

    }

    private void placeSolarPanel(final Block[] blocks, final byte[] metas, final int chunkX, final int chunkZ,
            final int x, final int y, final int z, final int meta) {
        int rotationMetadata = rotateStandardMetadata(meta, this.coordMode);
        if (AmunRa.config.villageAdvancedMachines) {
            rotationMetadata = rotationMetadata | BlockSolar.ADVANCED_METADATA;
        }
        if (this.placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, y, z, GCBlocks.solarPanel, rotationMetadata)) {
            this.parent.addPopulator(
                    new TouchSolarPanel(this.getXWithOffset(x, z), this.groundLevel, this.getZWithOffset(x, z)));
        }
    }

}
