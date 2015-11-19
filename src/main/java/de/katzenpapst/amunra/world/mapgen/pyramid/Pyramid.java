package de.katzenpapst.amunra.world.mapgen.pyramid;

import java.util.Random;

import cpw.mods.fml.common.FMLLog;
import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.world.CoordHelper;
import de.katzenpapst.amunra.world.mapgen.BaseStructureStart;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class Pyramid extends BaseStructureStart
{
	protected int pyramidSize = 56;
	protected BlockMetaPair wallMaterial = ARBlocks.blockAluCrate;
	protected BlockMetaPair floorMaterial = ARBlocks.blockSmoothBasalt;
	protected BlockMetaPair fillMaterial = ARBlocks.blockBasaltBrick;

	// how far in the inner ring is
	private int innerRingOffset = 32;
	private int tunnelWidth = 3;
	private int tunnelHeight = 4;

	private int mainRoomOffset = 3;

	private int sideRoomWidth = 13;
	private int innerRoomOffset = 13;

	private PyramidRoom[] roomList = new PyramidRoom[12];

	public Pyramid(World world, int chunkX, int chunkZ, Random rand) {
		super(world, chunkX, chunkZ, rand);
		int startX = CoordHelper.chunkToMinBlock(chunkX);
		int startZ = CoordHelper.chunkToMinBlock(chunkZ);
		StructureBoundingBox bb = new StructureBoundingBox(startX-56,startZ-56,startX+56,startZ+56);
		this.setStructureBoundingBox(bb);
		initRooms();

		FMLLog.info("Generating Pyramid at "+startX+"/"+startZ);
	}

	protected void initRooms() {
		for(int i=0;i<12;i++) {
			PyramidRoom room = new PyramidRoom();
			room.setParent(this);
			room.setStructureBoundingBox(this.getSmallRoomBB(i+1));
			roomList[i] = room;
		}
	}

	@Override
	public boolean generateChunk(int chunkX, int chunkZ, Block[] blocks, byte[] metas) {
		super.generateChunk(chunkX, chunkZ, blocks, metas);

		// now generate the actual pyramid
		StructureBoundingBox chunkBB = CoordHelper.getChunkBB(chunkX, chunkZ);//new StructureBoundingBox((chunkX << 4), (chunkX<< 4), (chunkX+1 << 4)-1, (chunkX+1 << 4)-1);
		StructureBoundingBox myBB = this.getStructureBoundingBox();

		if(!chunkBB.intersectsWith(myBB)) {
			return false;
		}

		int fallbackGround = this.getWorldGroundLevel();
		if(groundLevel == -1) {
			groundLevel = getAverageGroundLevel(blocks, metas, getStructureBoundingBox(), chunkBB, fallbackGround);
			if(groundLevel == -1) {
				groundLevel = fallbackGround; // but this shouldn't even happen...
			}
		}



		//BlockMetaPair glassPane = new BlockMetaPair(Blocks.glass_pane, (byte) 0);
		//BlockMetaPair air = new BlockMetaPair(Blocks.air, (byte) 0);

		// draw floor first
		int startX = 0;
		int stopX = myBB.getXSize() - 1;
		int startZ = 0;
		int stopZ = myBB.getZSize() - 1;





		int xCenter = (int)Math.ceil((stopX-startX)/2+startX);
		int zCenter = (int)Math.ceil((stopZ-startZ)/2+startZ);

		int radius = xCenter;


		for(int x = startX; x <= stopX; x++) {
			for(int z = startZ; z <= stopZ; z++) {

				int highestGroundBlock = getHighestSolidBlockInBB(blocks, metas, chunkX, chunkZ, x, z);
				if(highestGroundBlock == -1) {
					continue; // that should mean that we aren't in the right chunk
				}


				// now fill
				for(int y=highestGroundBlock-1;y<groundLevel; y++) {
					//padding
					placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, y, z, fillMaterial);
				}
				// floor
				placeBlockRel2BB(blocks, metas,chunkX, chunkZ, x, groundLevel, z, floorMaterial);

				/*if(startX == x || startZ == z || stopX == x || stopZ == z) {
					placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, groundLevel, z, wallMaterial);
				}*/

				for(int y = 0; y <= radius; y++) {

					// if(y >= 10) continue; // FOR TESTING

					if((x >= startX+y && x <= stopX-y) && (z >= startZ+y && z <= stopZ-y)) {
						if((z == startZ+y || z == stopZ-y) || (x == startX+y || x == stopX-y)) {
							// wall
							placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, groundLevel+y+1, z, wallMaterial);
						} else {
							// inner
							placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, groundLevel+y+1, z, fillMaterial);
						}
					}
					if(y >= 5 && y <= 13) {
						// try to do the entrance
						if(x >= xCenter-4 && x <= xCenter+4) {

							if(z >= startZ+5 && z <= startZ+5+7) {
								// surrounding box
								if((x == xCenter-4 || x == xCenter+4) || y == 13) {
									if(z == startZ+5 || y == 13) {
										placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, groundLevel+y+1, z, fillMaterial);
									}
								} else {
									if(z > startZ+5) {
										placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, groundLevel+y+1, z, fillMaterial);
									}
								}
							}

							// cut in the tunnel
							if(x >= xCenter-1 && x <= xCenter+1 &&
									(y >= 6 && y <= 6+tunnelHeight) && (z >= startZ+5 && z <= startZ+5+innerRingOffset-tunnelWidth)) {
								placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, groundLevel+y+1, z, Blocks.air, 0);
							}
						}


						// inner ring
						// check if we are fully within the range of the inner tunnel first and in the right height
						if(
								(y >= 6 && y <= 6+tunnelHeight) &&
								(x >= startX+innerRingOffset && x <= stopX-innerRingOffset) &&
								(z >= startZ+innerRingOffset && z <= stopZ-innerRingOffset)) {

							boolean xMinEdge = (x >= startX+innerRingOffset && x < startX+innerRingOffset+tunnelWidth);
							boolean xMaxEdge = (x <= stopX-innerRingOffset && x > stopX-innerRingOffset-tunnelWidth);
							boolean zMinEdge = (z >= startZ+innerRingOffset && z < startZ+innerRingOffset+tunnelWidth);
							boolean zMaxEdge = (z <= stopZ-innerRingOffset && z > stopZ-innerRingOffset-tunnelWidth);
							if(xMinEdge || xMaxEdge || zMinEdge || zMaxEdge) {
								// inner ring tunnel
								placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, groundLevel+y+1, z, Blocks.air, 0);
							}
						}

						// small rooms



						int innerRoomTotalOffset = innerRingOffset+tunnelWidth+mainRoomOffset;
						// entrance to the innermost room
						// cut in the tunnel
						if(x >= xCenter-1 && x <= xCenter+1 &&
							(y >= 6 && y <= 6+tunnelHeight) &&
							(z <= stopZ-innerRingOffset-tunnelWidth && z >= stopZ-innerRoomTotalOffset) )
						{
							placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, groundLevel+y+1, z, Blocks.air, 0);
						}



						// innermost room
						if(y >= 6 && y <= 12 &&
							(x > startX+innerRoomTotalOffset && x < stopX-innerRoomTotalOffset) &&
							(z > startZ+innerRoomTotalOffset && z < stopZ-innerRoomTotalOffset)
						) {
							placeBlockRel2BB(blocks, metas, chunkX, chunkZ, x, groundLevel+y+1, z, Blocks.air, 0);
						}

					}

				}




			}

		}

		generateSmallRooms(chunkBB, blocks, metas);

		return true;
	}

	protected void makeHoleForRoom(int position) {
		int direction = 0; // 0 = up, 1 = right, 2 = down, 3 = left
		switch(position) {
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

	}

	protected StructureBoundingBox getSmallRoomBB(int position) {
		int smallRoomWidth = 16;
		int offsetBetweenRooms = 3;
		StructureBoundingBox myBB = this.getStructureBoundingBox();
		// now doing it like this:
		/*
		 * Z
		 * ^
		 * |
		 * +-----------+ +-----------+
		 * | +--+ +--+ | | +--+ +--+ |
		 * | |10| |9 | | | |8 | |7 | |
		 * | +--+ +--+ | | +--+ +--+ |
		 * | +--+ +----+ +----+ +--+ |
		 * | |11| |  +-----+  | |6 | |
		 * | +--+ |  |     |  | +--+ |
		 * | +--+ |  |     |  | +--+ |
		 * | |12| |  +-+ +-+  | |5 | |
		 * | +--+ +-----------+ +--+ |
		 * | +--+ +--+     +--+ +--+ |
		 * | |1 | |2 |     |3 | |4 | |
		 * | +--+ +--+     +--+ +--+ |
		 * +-------------------------+----> X
		 * */
		StructureBoundingBox bb = new StructureBoundingBox();
		bb.minY = 0;
		bb.maxY = 255;
		switch(position) {
		case 1:
			bb.minX = myBB.minX+this.innerRoomOffset;
			bb.maxX = bb.minX+smallRoomWidth;
			bb.minZ = myBB.minZ+this.innerRoomOffset;
			bb.maxZ = bb.minZ+smallRoomWidth;
			break;
		case 2:
			bb.minX = myBB.minX+this.innerRoomOffset+offsetBetweenRooms+smallRoomWidth;
			bb.minZ = myBB.minZ+this.innerRoomOffset;
			bb.maxX = bb.minX+smallRoomWidth;
			bb.maxZ = bb.minZ+smallRoomWidth;
			break;
		case 3:
			// TODO fix myBB
			bb.maxX = myBB.maxX-this.innerRoomOffset-offsetBetweenRooms-smallRoomWidth;
			bb.minX = bb.maxX-smallRoomWidth;
			bb.minZ = myBB.minZ+this.innerRoomOffset;
			bb.maxZ = bb.minZ+smallRoomWidth;
			break;
		case 4:
			// TODO fix myBB
			bb.maxX = myBB.maxX-this.innerRoomOffset;
			bb.minX = bb.maxX-smallRoomWidth;
			bb.minZ = myBB.minZ+this.innerRoomOffset;
			bb.maxZ = bb.minZ+smallRoomWidth;
			break;
		case 5:
			bb.minZ = myBB.minZ+this.innerRoomOffset+offsetBetweenRooms+smallRoomWidth;
			bb.maxZ = bb.minZ+smallRoomWidth;
			bb.maxX = myBB.maxX-this.innerRoomOffset;
			bb.minX = bb.maxX-smallRoomWidth;
			break;
		case 6:
			bb.maxZ = myBB.maxZ-this.innerRoomOffset-(offsetBetweenRooms+smallRoomWidth);
			bb.minZ = bb.maxZ-smallRoomWidth;
			bb.maxX = myBB.maxX-this.innerRoomOffset;
			bb.minX = bb.maxX-smallRoomWidth;
			break;
		case 7:
			bb.maxZ = myBB.maxZ-this.innerRoomOffset;
			bb.minZ = bb.maxZ-smallRoomWidth;
			bb.maxX = myBB.maxX-this.innerRoomOffset;
			bb.minX = bb.maxX-smallRoomWidth;
			break;
		case 8:
			bb.maxZ = myBB.maxZ-this.innerRoomOffset;
			bb.minZ = bb.maxZ-smallRoomWidth;
			bb.maxX = myBB.maxX-this.innerRoomOffset-(offsetBetweenRooms+smallRoomWidth);
			bb.minX = bb.maxX-smallRoomWidth;
			break;
		case 9:
			bb.maxZ = myBB.maxZ-this.innerRoomOffset;
			bb.minZ = bb.maxZ-smallRoomWidth;
			bb.minX = myBB.minX+this.innerRoomOffset+offsetBetweenRooms+smallRoomWidth;
			bb.maxX = bb.minX+smallRoomWidth;
			break;
		case 10:
			bb.minX = myBB.minX+this.innerRoomOffset;
			bb.maxX = bb.minX+smallRoomWidth;
			bb.maxZ = myBB.maxZ-this.innerRoomOffset;
			bb.minZ = bb.maxZ-smallRoomWidth;
			break;
		case 11:
			bb.minX = myBB.minX+this.innerRoomOffset;
			bb.maxX = bb.minX+smallRoomWidth;
			bb.maxZ = myBB.maxZ-this.innerRoomOffset-(offsetBetweenRooms+smallRoomWidth);
			bb.minZ = bb.maxZ-smallRoomWidth;
			break;
		case 12:
			bb.minX = myBB.minX+this.innerRoomOffset;
			bb.maxX = bb.minX+smallRoomWidth;
			bb.minZ = myBB.minZ+this.innerRoomOffset+offsetBetweenRooms+smallRoomWidth;
			bb.maxZ = bb.minZ+smallRoomWidth;
			break;
		default:
			// bad
			throw new IllegalArgumentException("Pyramid room position "+position+" is invalid");

		}

		return bb;

	}


	protected void generateSmallRooms(StructureBoundingBox chunkBB, Block[] blocks, byte[] metas) {

		int chunkX = CoordHelper.blockToChunk(chunkBB.minX);
		int chunkZ = CoordHelper.blockToChunk(chunkBB.minZ);

		for(PyramidRoom r: roomList) {
			if(r.getStructureBoundingBox().intersectsWith(chunkBB)) {
				r.generateChunk(chunkX, chunkZ, blocks, metas);
			}
		}

		// also make holes in the walls


	}



    protected int coords2int(int x, int y, int z) {
    	int coords = ((x << 4) + z) * 256 + y;
    	return coords;
    }


    public BlockMetaPair getWallMaterial() {
		return wallMaterial;
	}

	public void setWallMaterial(BlockMetaPair wallMaterial) {
		this.wallMaterial = wallMaterial;
	}

	public BlockMetaPair getFloorMaterial() {
		return floorMaterial;
	}

	public void setFloorMaterial(BlockMetaPair floorMaterial) {
		this.floorMaterial = floorMaterial;
	}

	public BlockMetaPair getFillMaterial() {
		return fillMaterial;
	}

	public void setFillMaterial(BlockMetaPair fillMaterial) {
		this.fillMaterial = fillMaterial;
	}
}
