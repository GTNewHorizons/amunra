package de.katzenpapst.amunra.world.mapgen;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import de.katzenpapst.amunra.helper.CoordHelper;
import de.katzenpapst.amunra.world.mapgen.populator.SetSignText;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

abstract public class BaseStructureComponent {

    protected int groundLevel = -1;
    protected BaseStructureStart parent = null;

    protected int coordMode = 0;
    protected StructureBoundingBox structBB;

    public int getGroundLevel() {
        return this.groundLevel;
    }

    public void setStructureBoundingBox(final StructureBoundingBox structBB) {
        this.structBB = structBB;
    }

    public StructureBoundingBox getStructureBoundingBox() {
        return this.structBB;
    }

    public boolean generateChunk(final int chunkX, final int chunkZ, final Block[] arrayOfIDs,
            final byte[] arrayOfMeta) {
        return true;
    }

    public void setParent(final BaseStructureStart parent) {
        this.parent = parent;
    }

    public void setCoordMode(final int coordMode) {
        this.coordMode = coordMode;
    }

    /**
     * "Spawns" an entity. Does not check
     *
     * @param entityToSpawn
     * @param x
     * @param z
     */
    /*
     * protected void spawnEntity(Class<? extends EntityLiving> entityToSpawn, int x, int z) { EntityLiving ent = null;
     * try { ent = entityToSpawn.getConstructor(World.class).newInstance(this.parent.getWorld()); } catch (Throwable e)
     * { e.printStackTrace(); return; } ent.onSpawnWithEgg(null);// NO IDEA int xOffset = getXWithOffset(x, z); //y =
     * getYWithOffset(y); int zOffset = getZWithOffset(x, z); this.parent.spawnLater(ent, xOffset, groundLevel,
     * zOffset); }
     */

    protected int translateX(final int x, final int z) {
        switch (this.coordMode) {
            case 0:
            case 2:
                return x; // keep them as-is
            case 1:
                // translate z to "relative to bb", then do what getXWithOffset did
                return this.structBB.maxX - (z - this.structBB.minZ);
            case 3:
                // similar to above
                return this.structBB.minX + z - this.structBB.minZ;
        }

        return x;
    }

    protected int translateZ(final int x, final int z) {
        return switch (this.coordMode) {
            case 0 -> z;
            case 1, 3 -> this.structBB.minZ + x - this.structBB.minX;
            case 2 -> this.structBB.maxZ - (z - this.structBB.minZ);
            default -> z;
        };
    }

    protected int getHighestSolidBlockInBB(final Block[] blocks, final byte[] metas, final int chunkX, final int chunkZ,
            final int x, final int z) {
        final int xOffset = this.getXWithOffset(x, z);
        // y = getYWithOffset(y);
        final int zOffset = this.getZWithOffset(x, z);

        final int relX = CoordHelper.abs2rel(xOffset, chunkX);
        final int relZ = CoordHelper.abs2rel(zOffset, chunkZ);
        if (relX < 0 || relX >= 16 || relZ < 0 || relZ >= 16) {
            return -1;
        }

        return getHighestSolidBlock(blocks, metas, relX, relZ);
    }

    /**
     * Fill an area with blocks
     *
     * @param blocks
     * @param metas
     * @param chunkBB
     * @param box
     * @param block   / protected boolean drawArea(Block[] blocks, byte[] metas, StructureBoundingBox chunkBB,
     *                StructureBoundingBox box, BlockMetaPair block) {
     *
     *                StructureBoundingBox actualBox = intersectBoundingBoxes(chunkBB, box); if(actualBox == null) {
     *                return false; } for(int x=actualBox.minX; x<=actualBox.maxX; x++) { for(int y=actualBox.minY;
     *                y<=actualBox.maxY; y++) { for(int z=actualBox.minZ; z<=actualBox.maxZ; z++) { int xOffset =
     *                getXWithOffset(x, z); int zOffset = getZWithOffset(x, z); int relX = CoordHelper.abs2rel(xOffset);
     *                int relZ = CoordHelper.abs2rel(zOffset); placeBlockRel(blocks, metas, relX, y, relZ, block); } } }
     *
     *                return true; }
     */

    protected void fillBox(final Block[] blocks, final byte[] metas, final StructureBoundingBox box, final Block block,
            final byte meta) {

        for (int x = box.minX; x <= box.maxX; x++) {
            for (int y = box.minY; y <= box.maxY; y++) {
                for (int z = box.minZ; z <= box.maxZ; z++) {
                    final int chunkX = CoordHelper.blockToChunk(x);
                    final int chunkZ = CoordHelper.blockToChunk(z);
                    placeBlockAbs(blocks, metas, x, y, z, chunkX, chunkZ, block, meta);
                }
            }
        }
    }

    protected void fillBox(final Block[] blocks, final byte[] metas, final StructureBoundingBox box,
            final BlockMetaPair bmp) {
        this.fillBox(blocks, metas, box, bmp.getBlock(), bmp.getMetadata());
    }

    public static StructureBoundingBox intersectBoundingBoxesXZ(final StructureBoundingBox box1,
            final StructureBoundingBox box2) {
        final StructureBoundingBox result = new StructureBoundingBox();

        result.minX = Math.max(box1.minX, box2.minX);
        result.minZ = Math.max(box1.minZ, box2.minZ);

        result.maxX = Math.min(box1.maxX, box2.maxX);
        result.maxZ = Math.min(box1.maxZ, box2.maxZ);

        if (result.minX > result.maxX || result.minZ > result.maxZ) {
            return null;
        }

        return result;
    }

    public static StructureBoundingBox intersectBoundingBoxes(final StructureBoundingBox box1,
            final StructureBoundingBox box2) {
        final StructureBoundingBox result = new StructureBoundingBox();

        result.minX = Math.max(box1.minX, box2.minX);
        result.minY = Math.max(box1.minY, box2.minY);
        result.minZ = Math.max(box1.minZ, box2.minZ);

        result.maxX = Math.min(box1.maxX, box2.maxX);
        result.maxY = Math.min(box1.maxY, box2.maxY);
        result.maxZ = Math.min(box1.maxZ, box2.maxZ);

        if (result.minX > result.maxX || result.minY > result.maxY || result.minZ > result.maxZ) {
            return null;
        }

        return result;
    }

    protected boolean placeBlockRel2BB(final Block[] blocks, final byte[] metas, final int chunkX, final int chunkZ,
            final int x, final int y, final int z, final BlockMetaPair block) {
        final int xOffset = this.getXWithOffset(x, z);
        // y = getYWithOffset(y);
        final int zOffset = this.getZWithOffset(x, z);

        final int relX = CoordHelper.abs2rel(xOffset, chunkX);
        final int relZ = CoordHelper.abs2rel(zOffset, chunkZ);
        /*
         * if(relX < 0 || relX >= 16 || relZ < 0 || relZ >= 16) { return false; }
         */
        return placeBlockRel(blocks, metas, relX, y, relZ, block);
    }

    protected BlockMetaPair getBlockRel2BB(final Block[] blocks, final byte[] metas, final int chunkX, final int chunkZ,
            final int x, final int y, final int z) {
        final int xOffset = this.getXWithOffset(x, z);
        // y = getYWithOffset(y);
        final int zOffset = this.getZWithOffset(x, z);

        final int relX = CoordHelper.abs2rel(xOffset, chunkX);
        final int relZ = CoordHelper.abs2rel(zOffset, chunkZ);
        /*
         * if(relX < 0 || relX >= 16 || relZ < 0 || relZ >= 16) { return null; }
         */
        return getBlockRel(blocks, metas, relX, y, relZ);
    }

    protected boolean placeBlockRel2BB(final Block[] blocks, final byte[] metas, final int chunkX, final int chunkZ,
            final int x, final int y, final int z, final Block block, final int meta) {
        final int xOffset = this.getXWithOffset(x, z);
        // y = getYWithOffset(y);
        final int zOffset = this.getZWithOffset(x, z);

        final int relX = CoordHelper.abs2rel(xOffset, chunkX);
        final int relZ = CoordHelper.abs2rel(zOffset, chunkZ);
        if (relX < 0 || relX >= 16 || relZ < 0 || relZ >= 16) {
            return false;
        }
        return placeBlockRel(blocks, metas, relX, y, relZ, block, meta);
    }

    protected int getXWithOffset(final int x, final int z) {
        return switch (this.coordMode) {
            case 0, 2 -> this.structBB.minX + x;
            case 1 -> this.structBB.maxX - z;
            case 3 -> this.structBB.minX + z;
            default -> x;
        };
    }

    protected int getZWithOffset(final int x, final int z) {
        return switch (this.coordMode) {
            case 0 -> this.structBB.minZ + z;
            case 1, 3 -> this.structBB.minZ + x;
            case 2 -> this.structBB.maxZ - z;
            default -> z;
        };
    }

    protected void placeStandingSign(final Block[] blocks, final byte[] metas, final int chunkX, final int chunkZ,
            final int x, final int y, final int z, final String text) {

        if (this.placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, y, z, Blocks.standing_sign, (byte) 0)) {
            final int xOffset = this.getXWithOffset(x, z);
            // y = getYWithOffset(y);
            final int zOffset = this.getZWithOffset(x, z);
            final SetSignText sst = new SetSignText(xOffset, y, zOffset, text);
            this.parent.addPopulator(sst);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////// STATIC HELPERS //////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * For doors, in a sense, and furnaces
     *
     * 1 0-+-2 3
     */
    public static int rotateDoorlikeMetadata(final int unrotated, final int coordMode) {
        return rotateUniversalMetadata(unrotated, coordMode, 1, 3, 2, 0);
    }

    public static int rotateTorchMetadata(final int unrotated, final int coordMode) {
        // error with coordMode=1, everything is just the wrong way round

        return rotateStairlikeMetadata(unrotated - 1, coordMode) + 1;
    }

    /**
     * This should work for stairs 0 = E 1 = W 2 = S 3 = N for torches, add +1; 0 means nothing and 5 means "on the
     * ground", and the interpretation is "torch FACING dir"
     *
     * 3 1-+-0 2
     */
    public static int rotateStairlikeMetadata(final int unrotated, final int coordMode) {
        return rotateUniversalMetadata(unrotated, coordMode, 3, 2, 0, 1);
    }

    /**
     * Universal function for metadata rotation, based on what I found using trial&error with torches
     *
     * n w-+-e s
     */
    public static int rotateUniversalMetadata(final int unrotated, final int coordMode, final int n, final int s,
            final int e, final int w) {
        switch (coordMode) {
            /*
             * case 0: return unrotated;
             */
            case 1:
                if (unrotated == n) return e;
                if (unrotated == e) return s;
                if (unrotated == w) return n;
                if (unrotated == s) return w;
                break;
            case 2:
                if (unrotated == n) return s;
                if (unrotated == s) return n;
                break; // unrotated will be returned anyway
            case 3:
                if (unrotated == e) return s;
                if (unrotated == w) return n;
                if (unrotated == s) return e;
                if (unrotated == n) return w;
                break;
        }
        return unrotated;
    }

    /**
     * Rotates metadata for the 2 4-+-5 3 model, aka rotateStandardMetadata +2
     */
    public static int rotatePistonlikeMetadata(final int unrotated, final int coordMode) {
        return rotateStandardMetadata(unrotated - 2, coordMode) + 2;
    }

    /**
     * Rotates the metadata which most things seem to use: 0 2-+-3 1 This should work for solar collectors and
     * trapdoors, but in a reversed non-intuitive way
     */
    public static int rotateStandardMetadata(final int unrotated, final int coordMode) {
        return rotateUniversalMetadata(unrotated, coordMode, 0, 1, 3, 2);
    }

    public static int getAverageGroundLevel(final Block[] blocks, final byte[] metas,
            final StructureBoundingBox totalBB, final StructureBoundingBox chunkBB, final int minimum) {
        int sum = 0;
        int total = 0;

        final int chunkX = CoordHelper.blockToChunk(chunkBB.minX);// chunkBB.minX / 16;
        final int chunkZ = CoordHelper.blockToChunk(chunkBB.minZ);// chunkBB.minZ / 16;

        for (int z = totalBB.minZ; z <= totalBB.maxZ; ++z) {
            for (int x = totalBB.minX; x <= totalBB.maxX; ++x) {
                if (chunkBB.isVecInside(x, 64, z)) {
                    sum += Math.max(
                            getHighestSolidBlock(
                                    blocks,
                                    metas,
                                    CoordHelper.abs2rel(x, chunkX),
                                    CoordHelper.abs2rel(z, chunkZ)),
                            minimum);

                    ++total;
                }
            }
        }

        if (total == 0) {
            return -1;
        }
        return sum / total;
    }

    /**
     * Get highest block in a column, chunk-relative coordinates
     */
    public static int getHighestSolidBlock(final Block[] blocks, final byte[] metas, final int relX, final int relZ) {

        for (int y = 255; y >= 0; y--) {
            final int index = getIndex(relX, y, relZ);
            final Block curBlock = blocks[index];
            if (curBlock == null) {
                continue;
            }
            // int meta = metas[index];
            if (curBlock.getMaterial().blocksMovement() && curBlock.getMaterial() != Material.leaves) {
                return y + 1;
            }
        }
        return -1;
    }

    /**
     * Get specific block in a column, chunk-relative coordinates
     */
    public static int getHighestSpecificBlock(final Block[] blocks, final byte[] metas, final int relX, final int relZ,
            final Block block, final byte meta) {

        for (int y = 255; y >= 0; y--) {
            final int index = getIndex(relX, y, relZ);

            if (blocks[index] == block && metas[index] == meta) {
                return y;
            }
        }
        return -1;
    }

    /**
     * Places a block into the arrays using coordinates relative to the current chunk
     */
    public static boolean placeBlockRel(final Block[] blocks, final byte[] metas, final int x, final int y, final int z,
            final Block id, final int meta) {
        if (x < 0 || x >= 16 || z < 0 || z >= 16) {
            return false;
        }
        final int index = getIndex(x, y, z);
        blocks[index] = id;
        metas[index] = (byte) meta;

        return true;
    }

    public static boolean placeBlockRel(final Block[] blocks, final byte[] metas, final int x, final int y, final int z,
            final BlockMetaPair block) {
        if (x < 0 || x >= 16 || z < 0 || z >= 16) {
            return false;
        }
        final int index = getIndex(x, y, z);
        blocks[index] = block.getBlock();
        metas[index] = block.getMetadata();

        return true;
    }

    public static BlockMetaPair getBlockRel(final Block[] blocks, final byte[] metas, final int x, final int y,
            final int z) {
        if (x < 0 || x >= 16 || z < 0 || z >= 16) {
            return null;
        }
        final int index = getIndex(x, y, z);

        return new BlockMetaPair(blocks[index], metas[index]);
    }

    /**
     * Places a block into the arrays using absolute coordinates+coordinates of the current chunk. If the coordinates
     * are not inside the given chunk, nothing happens. Block/meta version
     */
    public static boolean placeBlockAbs(final Block[] blocks, final byte[] metas, final int x, final int y, final int z,
            final int cx, final int cz, final Block id, final int meta) {
        return placeBlockRel(blocks, metas, CoordHelper.abs2rel(x, cx), y, CoordHelper.abs2rel(z, cz), id, meta);
    }

    /**
     * Places a block into the arrays using absolute coordinates+coordinates of the current chunk. If the coordinates
     * are not inside the given chunk, nothing happens. BlockMetaPair version
     */
    public static boolean placeBlockAbs(final Block[] blocks, final byte[] metas, final int x, final int y, final int z,
            final int cx, final int cz, final BlockMetaPair block) {
        return placeBlockRel(blocks, metas, CoordHelper.abs2rel(x, cx), y, CoordHelper.abs2rel(z, cz), block);
    }

    /**
     * Places a block into the arrays using absolute coordinates. Assumes the chunk the coordinates are in is to be
     * edited. BlockMetaPair version
     */
    public static boolean placeBlockAbs(final Block[] blocks, final byte[] metas, final int x, final int y, final int z,
            final BlockMetaPair block) {
        return placeBlockRel(blocks, metas, CoordHelper.abs2rel(x), y, CoordHelper.abs2rel(z), block);
    }

    /**
     * Places a block into the arrays using absolute coordinates. Assumes the chunk the coordinates are in is to be
     * edited. Block/meta version
     */
    public static boolean placeBlockAbs(final Block[] blocks, final byte[] metas, final int x, final int y, final int z,
            final Block id, final int meta) {
        return placeBlockRel(blocks, metas, CoordHelper.abs2rel(x), y, CoordHelper.abs2rel(z), id, meta);
    }

    /**
     * Converts coordinates to the index as required for the arrays
     */
    public static int getIndex(final int x, final int y, final int z) {
        return (x * 16 + z) * 256 + y;
    }

    /**
     * lerp
     */
    public double lerp(final double d1, final double d2, final double t) {
        if (t < 0.0) {
            return d1;
        }
        if (t > 1.0) {
            return d2;
        }
        return d1 + (d2 - d1) * t;
    }

}
