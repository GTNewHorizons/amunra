package de.katzenpapst.amunra.world.mapgen.pyramid;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.ChestGenHooks;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.helper.CoordHelper;
import de.katzenpapst.amunra.item.ARItems;
import de.katzenpapst.amunra.world.mapgen.BaseStructureComponent;
import de.katzenpapst.amunra.world.mapgen.BaseStructureStart;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.planets.mars.items.MarsItems;

public class Pyramid extends BaseStructureStart {

    protected int pyramidSize = 56;
    protected BlockMetaPair wallMaterial = ARBlocks.blockAluCrate;
    protected BlockMetaPair floorMaterial = ARBlocks.blockSmoothBasalt;
    protected BlockMetaPair fillMaterial = ARBlocks.blockBasaltBrick;

    // how far in the inner ring is
    private final int innerRingOffset = 32;
    private final int tunnelWidth = 3;
    private final int tunnelHeight = 4;

    private final int mainRoomOffset = 3;

    private final int innerRoomOffset = 13;

    private final int smallRoomWidth = 17;

    private final PyramidRoom[] roomList = new PyramidRoom[12];

    private PyramidRoom centralRoom = null;

    // loot
    public static final String LOOT_CATEGORY_BASIC = "amunraPyramidChest";
    public static final String LOOT_CATEGORY_BOSS = "amunraPyramidChestBoss";

    static private boolean initDone = false;

    static void initLoot() {
        if (initDone) {
            return;
        }
        initDone = true;

        /*
         * public static final WeightedRandomChestContent[] mineshaftChestContents = new WeightedRandomChestContent[] {
         * new WeightedRandomChestContent(Items.iron_ingot, 0, 1, 5, 10), new
         * WeightedRandomChestContent(Items.gold_ingot, 0, 1, 3, 5), new WeightedRandomChestContent(Items.redstone, 0,
         * 4, 9, 5), new WeightedRandomChestContent(Items.dye, 4, 4, 9, 5), new
         * WeightedRandomChestContent(Items.diamond, 0, 1, 2, 3), new WeightedRandomChestContent(Items.coal, 0, 3, 8,
         * 10), new WeightedRandomChestContent(Items.bread, 0, 1, 3, 15), new
         * WeightedRandomChestContent(Items.iron_pickaxe, 0, 1, 1, 1), new
         * WeightedRandomChestContent(Item.getItemFromBlock(Blocks.rail), 0, 4, 8, 1), new
         * WeightedRandomChestContent(Items.melon_seeds, 0, 2, 4, 10), new
         * WeightedRandomChestContent(Items.pumpkin_seeds, 0, 2, 4, 10), new WeightedRandomChestContent(Items.saddle, 0,
         * 1, 1, 3), new WeightedRandomChestContent(Items.iron_horse_armor, 0, 1, 1, 1)};
         */

        // itemStack, MinimumChanceToGenerate, MaximumChanceToGenerate, weight
        final WeightedRandomChestContent alienBook = new WeightedRandomChestContent(
                ARItems.alienBook.getItemStack(0),
                1,
                1,
                3);
        final WeightedRandomChestContent ironIngot = new WeightedRandomChestContent(
                new ItemStack(Items.iron_ingot, 2),
                1,
                5,
                10);
        final WeightedRandomChestContent goldIngot = new WeightedRandomChestContent(
                new ItemStack(Items.gold_ingot, 0),
                1,
                3,
                5);
        final WeightedRandomChestContent diamond = new WeightedRandomChestContent(
                new ItemStack(Items.diamond, 0),
                1,
                3,
                5);

        final WeightedRandomChestContent lithium = new WeightedRandomChestContent(
                ARItems.lithiumGem.getItemStack(1),
                1,
                3,
                5);
        final WeightedRandomChestContent coldcrystal = new WeightedRandomChestContent(
                ARItems.coldCrystal.getItemStack(1),
                1,
                3,
                5);
        final WeightedRandomChestContent ruby = new WeightedRandomChestContent(
                ARItems.rubyGem.getItemStack(1),
                1,
                3,
                5);

        // try to add some GC stuff
        final WeightedRandomChestContent deshPick = new WeightedRandomChestContent(
                new ItemStack(MarsItems.deshPickaxe, 0),
                1,
                1,
                1);

        // WeightedRandomChestContent desh = new WeightedRandomChestContent(new ItemStack(MarsItems.deshPickaxe, 0, 2),
        // 1, 1, 1);

        final WeightedRandomChestContent desh = new WeightedRandomChestContent(
                new ItemStack(MarsItems.marsItemBasic, 0, 0),
                1,
                1,
                5);

        // nanites
        final WeightedRandomChestContent nanites = new WeightedRandomChestContent(
                ARItems.naniteCluster.getItemStack(1),
                1,
                1,
                2);
        final WeightedRandomChestContent pearl = new WeightedRandomChestContent(
                new ItemStack(Items.ender_pearl),
                1,
                1,
                2);

        final ChestGenHooks basicLoot = ChestGenHooks.getInfo(LOOT_CATEGORY_BASIC);
        basicLoot.setMin(5);
        basicLoot.setMax(8);

        basicLoot.addItem(alienBook);
        basicLoot.addItem(ironIngot);
        basicLoot.addItem(goldIngot);
        basicLoot.addItem(diamond);
        basicLoot.addItem(nanites);
        basicLoot.addItem(pearl);
        basicLoot.addItem(coldcrystal);
        basicLoot.addItem(ruby);
        basicLoot.addItem(deshPick);
        basicLoot.addItem(desh);
        if (!AmunRa.isNHCoreLoaded) {
            basicLoot.addItem(lithium);
        }

    }

    public Pyramid(final World world, final int chunkX, final int chunkZ, final Random rand) {
        super(world, chunkX, chunkZ, rand);
        final int startX = CoordHelper.chunkToMinBlock(chunkX);
        final int startZ = CoordHelper.chunkToMinBlock(chunkZ);
        final StructureBoundingBox bb = new StructureBoundingBox(
                startX - 56,
                startZ - 56,
                startX + 56 - 1,
                startZ + 56 - 1);
        this.setStructureBoundingBox(bb);
        // initRooms();
        initLoot();

        AmunRa.LOGGER.debug("Generating Pyramid at {}/{}", startX, startZ);
    }
    /*
     * protected void initRooms() { for(int i=0;i<12;i++) { PyramidRoom room = new PyramidRoom(); room.setParent(this);
     * StructureBoundingBox roomBB = this.getSmallRoomBB(i+1); StructureBoundingBox entranceBB =
     * this.getRoomEntranceBox(i+1, roomBB); room.setBoundingBoxes(entranceBB, roomBB);
     * //room.setStructureBoundingBox(roomBB); //room.setEntranceBB(entranceBB); roomList[i] = room; } int
     * innerRoomTotalOffset = innerRingOffset+tunnelWidth+mainRoomOffset; StructureBoundingBox innerRoomBB = new
     * StructureBoundingBox( this.structBB.minX+innerRoomTotalOffset, this.structBB.minZ+innerRoomTotalOffset,
     * this.structBB.maxX-innerRoomTotalOffset, this.structBB.maxZ-innerRoomTotalOffset ); StructureBoundingBox
     * mainEntranceBB = new StructureBoundingBox(); mainEntranceBB.minX = innerRoomBB.getCenterX()-1;
     * mainEntranceBB.maxX = innerRoomBB.getCenterX()+1; mainEntranceBB.minZ = innerRoomBB.maxZ+1; mainEntranceBB.maxZ =
     * innerRoomBB.maxZ+4; centralRoom = new PyramidRoom(); centralRoom.setBoundingBoxes(innerRoomBB, mainEntranceBB);
     * centralRoom.setParent(this); }
     */

    public void setSmallRooms(final List<BaseStructureComponent> roomList) {
        if (roomList.size() < 12) {
            while (roomList.size() < 12) {
                final PyramidRoom filler = new PyramidRoom();
                roomList.add(filler);
            }
        } else {
            while (roomList.size() > 12) {
                roomList.remove(roomList.size() - 1);
            }
        }
        Collections.shuffle(roomList, this.rand);

        final Object[] tempList = roomList.toArray();

        for (int i = 0; i < 12; i++) {
            if (tempList[i] instanceof final PyramidRoom room) {
                room.setParent(this);
                final StructureBoundingBox roomBB = this.getSmallRoomBB(i + 1);
                roomBB.minY = 0;
                roomBB.maxY = 4;
                final StructureBoundingBox entranceBB = this.getRoomEntranceBox(i + 1, roomBB);
                room.setBoundingBoxes(entranceBB, roomBB);
                this.roomList[i] = room;
            }
        }
    }

    public void setMainRoom(final PyramidRoom room) {
        final int innerRoomTotalOffset = this.innerRingOffset + this.tunnelWidth + this.mainRoomOffset;

        final StructureBoundingBox innerRoomBB = new StructureBoundingBox(
                this.structBB.minX + innerRoomTotalOffset,
                this.structBB.minZ + innerRoomTotalOffset,
                this.structBB.maxX - innerRoomTotalOffset,
                this.structBB.maxZ - innerRoomTotalOffset);

        innerRoomBB.minY = 0;
        innerRoomBB.maxY = 8;

        /*
         * x >= xCenter-1 && x <= xCenter+1 && (y >= 5 && y <= 6+tunnelHeight) && (z <=
         * stopZ-innerRingOffset-tunnelWidth && z >= stopZ-innerRoomTotalOffset)
         */
        final StructureBoundingBox mainEntranceBB = new StructureBoundingBox();

        mainEntranceBB.minX = innerRoomBB.getCenterX() - 1;
        mainEntranceBB.maxX = innerRoomBB.getCenterX() + 1;
        mainEntranceBB.minZ = innerRoomBB.maxZ + 1;
        mainEntranceBB.maxZ = innerRoomBB.maxZ + 4;

        this.centralRoom = room;
        this.centralRoom.setBoundingBoxes(mainEntranceBB, innerRoomBB);
        this.centralRoom.setParent(this);
    }

    @Override
    public boolean generateChunk(final int chunkX, final int chunkZ, final Block[] blocks, final byte[] metas) {
        super.generateChunk(chunkX, chunkZ, blocks, metas);

        // now generate the actual pyramid
        final StructureBoundingBox chunkBB = CoordHelper.getChunkBB(chunkX, chunkZ);// new StructureBoundingBox((chunkX
                                                                                    // << 4),
        // (chunkX<< 4), (chunkX+1 << 4)-1,
        // (chunkX+1 << 4)-1);
        final StructureBoundingBox myBB = this.getStructureBoundingBox();

        if (!chunkBB.intersectsWith(myBB)) {
            return false;
        }

        final int fallbackGround = this.getWorldGroundLevel();
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

        // BlockMetaPair glassPane = new BlockMetaPair(Blocks.glass_pane, (byte) 0);
        // BlockMetaPair air = new BlockMetaPair(Blocks.air, (byte) 0);

        // draw floor first
        final int startX = 0;
        final int stopX = myBB.getXSize();
        final int startZ = 0;
        final int stopZ = myBB.getZSize();

        final int xCenter = (int) Math.ceil((stopX - startX) / 2 + startX);
        // int zCenter = (int)Math.ceil((stopZ-startZ)/2+startZ);

        final int radius = xCenter;

        for (int x = startX; x <= stopX; x++) {
            for (int z = startZ; z <= stopZ; z++) {

                final int highestGroundBlock = this.getHighestSolidBlockInBB(blocks, metas, chunkX, chunkZ, x, z);
                if (highestGroundBlock == -1) {
                    continue; // that should mean that we aren't in the right chunk
                }

                // now fill
                for (int y = highestGroundBlock - 1; y < this.groundLevel; y++) {
                    // padding
                    this.placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, y, z, this.fillMaterial);
                }
                // floor
                this.placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, this.groundLevel, z, this.floorMaterial);

                for (int y = 0; y <= radius; y++) {

                    // if(y >= 12) continue; // FOR DEBUG TESTING

                    if (x >= startX + y && x <= stopX - y && z >= startZ + y && z <= stopZ - y) {
                        if (z == startZ + y || z == stopZ - y || x == startX + y || x == stopX - y) {
                            // wall
                            this.placeBlockRel2BB(
                                    blocks,
                                    metas,
                                    chunkX,
                                    chunkZ,
                                    x,
                                    this.groundLevel + y + 1,
                                    z,
                                    this.wallMaterial);
                        } else {
                            // inner
                            this.placeBlockRel2BB(
                                    blocks,
                                    metas,
                                    chunkX,
                                    chunkZ,
                                    x,
                                    this.groundLevel + y + 1,
                                    z,
                                    this.fillMaterial);
                        }
                    }
                    if (y >= 5 && y <= 13) {
                        // try to do the entrance
                        if (x >= xCenter - 4 && x <= xCenter + 4) {

                            if (z >= startZ + 5 && z <= startZ + 5 + 7) {
                                // surrounding box
                                if (x == xCenter - 4 || x == xCenter + 4 || y == 13) {
                                    if (z == startZ + 5 || y == 13) {
                                        this.placeBlockRel2BB(
                                                blocks,
                                                metas,
                                                chunkX,
                                                chunkZ,
                                                x,
                                                this.groundLevel + y + 1,
                                                z,
                                                this.fillMaterial);
                                    }
                                } else if (z > startZ + 5) {
                                    this.placeBlockRel2BB(
                                            blocks,
                                            metas,
                                            chunkX,
                                            chunkZ,
                                            x,
                                            this.groundLevel + y + 1,
                                            z,
                                            this.fillMaterial);
                                }
                            }

                            // cut in the tunnel
                            if (x >= xCenter - 1 && x <= xCenter + 1
                                    && z >= startZ + 6
                                    && y >= 5
                                    && y <= 6 + this.tunnelHeight
                                    && z >= startZ + 5
                                    && z <= startZ + 5 + this.innerRingOffset - this.tunnelWidth

                            ) {
                                if (y == 5) {
                                    this.placeBlockRel2BB(
                                            blocks,
                                            metas,
                                            chunkX,
                                            chunkZ,
                                            x,
                                            this.groundLevel + y + 1,
                                            z,
                                            this.floorMaterial);
                                } else {
                                    this.placeBlockRel2BB(
                                            blocks,
                                            metas,
                                            chunkX,
                                            chunkZ,
                                            x,
                                            this.groundLevel + y + 1,
                                            z,
                                            Blocks.air,
                                            0);
                                }
                            }
                        }

                        // inner ring
                        // check if we are fully within the range of the inner tunnel first and in the right height
                        if (y >= 5 && y <= 6 + this.tunnelHeight
                                && x >= startX + this.innerRingOffset
                                && x <= stopX - this.innerRingOffset
                                && z >= startZ + this.innerRingOffset
                                && z <= stopZ - this.innerRingOffset) {

                            final boolean xMinEdge = x >= startX + this.innerRingOffset
                                    && x < startX + this.innerRingOffset + this.tunnelWidth;
                            final boolean xMaxEdge = x <= stopX - this.innerRingOffset
                                    && x > stopX - this.innerRingOffset - this.tunnelWidth;
                            final boolean zMinEdge = z >= startZ + this.innerRingOffset
                                    && z < startZ + this.innerRingOffset + this.tunnelWidth;
                            final boolean zMaxEdge = z <= stopZ - this.innerRingOffset
                                    && z > stopZ - this.innerRingOffset - this.tunnelWidth;
                            if (xMinEdge || xMaxEdge || zMinEdge || zMaxEdge) {
                                // inner ring tunnel
                                if (y == 5) {
                                    this.placeBlockRel2BB(
                                            blocks,
                                            metas,
                                            chunkX,
                                            chunkZ,
                                            x,
                                            this.groundLevel + y + 1,
                                            z,
                                            this.floorMaterial);
                                } else {
                                    this.placeBlockRel2BB(
                                            blocks,
                                            metas,
                                            chunkX,
                                            chunkZ,
                                            x,
                                            this.groundLevel + y + 1,
                                            z,
                                            Blocks.air,
                                            0);
                                }
                            }
                        }
                    }
                }
            }
        }

        this.generateSmallRooms(chunkBB, blocks, metas);

        return true;
    }

    protected StructureBoundingBox getRoomEntranceBox(final int position, final StructureBoundingBox roomBox) {
        int direction = 0; // 0 = up, 1 = right, 2 = down, 3 = left
        switch (position) {
            case 1:
                direction = Math.random() > 0.5 ? 0 : 1;
                break;
            case 2:
            case 3:
                direction = 0;
                break;
            case 4:
                direction = Math.random() > 0.5 ? 0 : 3;
                break;
            case 5:
            case 6:
                direction = 3;
                break;
            case 7:
                direction = Math.random() > 0.5 ? 3 : 2;
                break;
            case 8:
            case 9:
                direction = 2;
                break;
            case 10:
                direction = Math.random() > 0.5 ? 1 : 2;
                break;
            case 11:
            case 12:
                direction = 1;
                break;
        }

        final StructureBoundingBox doorBB = new StructureBoundingBox();
        doorBB.minY = 0;
        doorBB.maxY = 255;
        // boolean isOdd = smallRoomWidth % 2 == 1;

        switch (direction) {
            case 0: // up aka +z
                doorBB.minZ = roomBox.maxZ + 1;
                doorBB.maxZ = doorBB.minZ + 1;
                // hmm this is for odd
                doorBB.minX = roomBox.getCenterX() - 1;
                doorBB.maxX = roomBox.getCenterX() + 1;
                break;
            case 1: // right aka +x
                doorBB.minX = roomBox.maxX + 1;
                doorBB.maxX = doorBB.minX + 1;

                doorBB.minZ = roomBox.getCenterZ() - 1;
                doorBB.maxZ = roomBox.getCenterZ() + 1;
                break;
            case 2: // down aka -z
                doorBB.maxZ = roomBox.minZ - 1;
                doorBB.minZ = doorBB.maxZ - 1;

                doorBB.minX = roomBox.getCenterX() - 1;
                doorBB.maxX = roomBox.getCenterX() + 1;
                break;
            case 3: // left aka -x
                doorBB.maxX = roomBox.minX - 1;
                doorBB.minX = doorBB.maxX - 1;

                doorBB.minZ = roomBox.getCenterZ() - 1;
                doorBB.maxZ = roomBox.getCenterZ() + 1;
        }

        return doorBB;

    }

    protected StructureBoundingBox getSmallRoomBB(final int position) {

        final int offsetBetweenRooms = 3;
        final StructureBoundingBox myBB = this.getStructureBoundingBox();
        // now doing it like this:
        /*
         * Z ^ | +-----------+ +-----------+ | +--+ +--+ | | +--+ +--+ | | |10| |9 | | | |8 | |7 | | | +--+ +--+ | |
         * +--+ +--+ | | +--+ +----+ +----+ +--+ | | |11| | +-----+ | |6 | | | +--+ | | | | +--+ | | +--+ | | | | +--+ |
         * | |12| | +-+ +-+ | |5 | | | +--+ +-----------+ +--+ | | +--+ +--+ +--+ +--+ | | |1 | |2 | |3 | |4 | | | +--+
         * +--+ +--+ +--+ | +-------------------------+----> X
         */
        final StructureBoundingBox bb = new StructureBoundingBox();
        bb.minY = 0;
        bb.maxY = 255;
        final int tempRoomWidth = this.smallRoomWidth - 1;
        switch (position) {
            case 1:
                bb.minX = myBB.minX + this.innerRoomOffset;
                bb.maxX = bb.minX + tempRoomWidth;
                bb.minZ = myBB.minZ + this.innerRoomOffset;
                bb.maxZ = bb.minZ + tempRoomWidth;
                break;
            case 2:
                bb.minX = myBB.minX + this.innerRoomOffset + offsetBetweenRooms + tempRoomWidth;
                bb.minZ = myBB.minZ + this.innerRoomOffset;
                bb.maxX = bb.minX + tempRoomWidth;
                bb.maxZ = bb.minZ + tempRoomWidth;
                break;
            case 3:
                bb.maxX = myBB.maxX - this.innerRoomOffset - offsetBetweenRooms - tempRoomWidth + 1;
                bb.minX = bb.maxX - tempRoomWidth;
                bb.minZ = myBB.minZ + this.innerRoomOffset;
                bb.maxZ = bb.minZ + tempRoomWidth;
                break;
            case 4:
                bb.maxX = myBB.maxX - this.innerRoomOffset + 1;
                bb.minX = bb.maxX - tempRoomWidth;
                bb.minZ = myBB.minZ + this.innerRoomOffset;
                bb.maxZ = bb.minZ + tempRoomWidth;
                break;
            case 5:
                bb.minZ = myBB.minZ + this.innerRoomOffset + offsetBetweenRooms + tempRoomWidth;
                bb.maxZ = bb.minZ + tempRoomWidth;
                bb.maxX = myBB.maxX - this.innerRoomOffset + 1;
                bb.minX = bb.maxX - tempRoomWidth;
                break;
            case 6:
                bb.maxZ = myBB.maxZ - this.innerRoomOffset - (offsetBetweenRooms + tempRoomWidth) + 1;
                bb.minZ = bb.maxZ - tempRoomWidth;
                bb.maxX = myBB.maxX - this.innerRoomOffset + 1;
                bb.minX = bb.maxX - tempRoomWidth;
                break;
            case 7:
                bb.maxZ = myBB.maxZ - this.innerRoomOffset + 1;
                bb.minZ = bb.maxZ - tempRoomWidth;
                bb.maxX = myBB.maxX - this.innerRoomOffset + 1;
                bb.minX = bb.maxX - tempRoomWidth;
                break;
            case 8:
                bb.maxZ = myBB.maxZ - this.innerRoomOffset + 1;
                bb.minZ = bb.maxZ - tempRoomWidth;
                bb.maxX = myBB.maxX - this.innerRoomOffset - (offsetBetweenRooms + tempRoomWidth) + 1;
                bb.minX = bb.maxX - tempRoomWidth;
                break;
            case 9:
                bb.maxZ = myBB.maxZ - this.innerRoomOffset + 1;
                bb.minZ = bb.maxZ - tempRoomWidth;
                bb.minX = myBB.minX + this.innerRoomOffset + offsetBetweenRooms + tempRoomWidth;
                bb.maxX = bb.minX + tempRoomWidth;
                break;
            case 10:
                bb.minX = myBB.minX + this.innerRoomOffset;
                bb.maxX = bb.minX + tempRoomWidth;
                bb.maxZ = myBB.maxZ - this.innerRoomOffset + 1;
                bb.minZ = bb.maxZ - tempRoomWidth;
                break;
            case 11:
                bb.minX = myBB.minX + this.innerRoomOffset;
                bb.maxX = bb.minX + tempRoomWidth;
                bb.maxZ = myBB.maxZ - this.innerRoomOffset - (offsetBetweenRooms + tempRoomWidth) + 1;
                bb.minZ = bb.maxZ - tempRoomWidth;
                break;
            case 12:
                bb.minX = myBB.minX + this.innerRoomOffset;
                bb.maxX = bb.minX + tempRoomWidth;
                bb.minZ = myBB.minZ + this.innerRoomOffset + offsetBetweenRooms + tempRoomWidth;
                bb.maxZ = bb.minZ + tempRoomWidth;
                break;
            default:
                // bad
                throw new IllegalArgumentException("Pyramid room position " + position + " is invalid");

        }

        return bb;

    }

    protected void generateSmallRooms(final StructureBoundingBox chunkBB, final Block[] blocks, final byte[] metas) {

        final int chunkX = CoordHelper.blockToChunk(chunkBB.minX);
        final int chunkZ = CoordHelper.blockToChunk(chunkBB.minZ);

        for (final PyramidRoom r : this.roomList) {
            if (r != null && r.getStructureBoundingBox().intersectsWith(chunkBB)) {
                r.generateChunk(chunkX, chunkZ, blocks, metas);
            }
        }

        if (this.centralRoom.getStructureBoundingBox().intersectsWith(chunkBB)) {
            this.centralRoom.generateChunk(chunkX, chunkZ, blocks, metas);
        }

    }

    protected int coords2int(final int x, final int y, final int z) {
        return ((x << 4) + z) * 256 + y;
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
