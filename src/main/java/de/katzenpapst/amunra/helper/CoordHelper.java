package de.katzenpapst.amunra.helper;

import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.ForgeDirection;

public class CoordHelper {

    /**
     * Convert a block coordinate to the coordinate of the chunk it is in
     */
    public static int blockToChunk(final int blockCoord) {
        return blockCoord >> 4;
    }

    /**
     * Get the coordinate of the minimum block of a chunk
     */
    public static int chunkToMinBlock(final int chunkCoord) {
        return chunkCoord << 4;
    }

    /**
     * Get the coordinate of the maximum block of a chunk
     */
    public static int chunkToMaxBlock(final int chunkCoord) {
        return (chunkCoord + 1 << 4) - 1;
    }

    public static StructureBoundingBox getChunkBB(final int chunkX, final int chunkZ) {
        return new StructureBoundingBox(
                chunkToMinBlock(chunkX),
                chunkToMinBlock(chunkZ),
                chunkToMaxBlock(chunkX),
                chunkToMaxBlock(chunkZ));
    }

    /**
     * Converts an absolute coordinate to relative. Does not validate the result
     */
    public static int abs2rel(final int absCoord, final int chunkCoord) {
        return absCoord - chunkToMinBlock(chunkCoord);
    }

    public static int abs2rel(final int absCoord) {
        final int chunkCoord = CoordHelper.blockToChunk(absCoord);
        return absCoord - chunkToMinBlock(chunkCoord);
    }

    /**
     * Converts a relative chunk coordinate to an absolute one Does not validate the input
     */
    public static int rel2abs(final int relCoord, final int chunkCoord) {
        return relCoord + chunkToMinBlock(chunkCoord);
    }

    /**
     * Converts the coordinates to the index for a blocks/metas array
     */
    public static int getIndex(final int x, final int y, final int z) {
        return (x * 16 + z) * 256 + y;
    }

    public static AxisAlignedBB cloneAABB(final AxisAlignedBB box) {
        return AxisAlignedBB.getBoundingBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    /**
     * This should transform a ForgeDirection according to rotation metadata
     *
     * @param rotationMetadata this should be ONLY the rotation metadata, ANDed and byteshifted, if necessary: 0 2-+-3 1
     */
    public static ForgeDirection rotateForgeDirection(final ForgeDirection dir, final int rotationMetadata) {
        int dirOrdinal = dir.ordinal();
        if (dirOrdinal < 2 || dirOrdinal > 5) {
            return dir;
        }

        dirOrdinal = rotateForgeDirectionOrdinal(dirOrdinal, rotationMetadata);
        return ForgeDirection.getOrientation(dirOrdinal);
    }

    private static int rotateForgeDirectionOrdinal(final int dirOrdinal, final int rotationMeta) {
        return switch (rotationMeta) {
            case 0 -> // identity
                dirOrdinal;
            case 1 -> // rotate 180°
                switch (dirOrdinal) {
                    case 2 -> // N
                        3; // S
                    case 3 -> // S
                        2; // N
                    case 4 -> // W
                        5; // E
                    case 5 -> // E
                        4;
                    default -> // W
                        -1;
                };
            case 2 -> // rotate 270°
                switch (dirOrdinal) {
                    case 2 -> // N
                        4; // W
                    case 3 -> // S
                        5; // E
                    case 4 -> // W
                        3; // S
                    case 5 -> // E
                        2;
                    default -> // N
                        -1;
                };
            case 3 -> // rotate 90°
                switch (dirOrdinal) {
                    case 2 -> // N
                        5; // E
                    case 3 -> // S
                        4; // W
                    case 4 -> // W
                        2; // N
                    case 5 -> // E
                        3;
                    default -> // S
                        -1;
                };
            default -> dirOrdinal;
        };
    }

}
