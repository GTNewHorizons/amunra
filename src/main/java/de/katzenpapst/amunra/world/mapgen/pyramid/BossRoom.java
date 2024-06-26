package de.katzenpapst.amunra.world.mapgen.pyramid;

import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;

import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.mob.entity.EntityMummyBoss;
import de.katzenpapst.amunra.world.mapgen.populator.InitBossSpawner;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class BossRoom extends PyramidRoom {

    @Override
    public boolean generateChunk(final int chunkX, final int chunkZ, final Block[] arrayOfIDs,
            final byte[] arrayOfMeta) {

        super.generateChunk(chunkX, chunkZ, arrayOfIDs, arrayOfMeta);
        /*
         * // try making a room below int roomSize = (roomBB.getXSize()/2) - 8; for(int x = -roomSize; x<=roomSize; x++)
         * { for(int z = -roomSize; z<=roomSize; z++) { for(int y = this.floorLevel-6; y < this.floorLevel-1;y++) {
         * placeBlockAbs(arrayOfIDs, arrayOfMeta, this.roomBB.getCenterX()+x, y, this.roomBB.getCenterZ()+z, chunkX,
         * chunkZ, Blocks.air, (byte) 0); } } } // make a hole in the center int trapdoorMeta = 8 | 0; // 8 = on top, 0
         * = south, 1 = north placeBlockAbs(arrayOfIDs, arrayOfMeta, this.roomBB.getCenterX(), this.floorLevel-1,
         * this.roomBB.getCenterZ(), chunkX, chunkZ, Blocks.trapdoor, (byte) trapdoorMeta); // ladder? BlockMetaPair
         * wallMat = ((Pyramid) this.parent).getWallMaterial(); for(int y = this.floorLevel-6; y <
         * this.floorLevel-1;y++) { placeBlockAbs(arrayOfIDs, arrayOfMeta, this.roomBB.getCenterX(), y,
         * this.roomBB.getCenterZ()+1, chunkX, chunkZ, wallMat.getBlock(), (byte) wallMat.getMetadata());
         * placeBlockAbs(arrayOfIDs, arrayOfMeta, this.roomBB.getCenterX(), y, this.roomBB.getCenterZ(), chunkX, chunkZ,
         * Blocks.ladder, (byte) 2); }
         */
        // for now, just this
        this.placeBossSpawner(
                this.roomBB.getCenterX(),
                this.floorLevel + 2,
                this.roomBB.getCenterZ(),
                chunkX,
                chunkZ,
                arrayOfIDs,
                arrayOfMeta,
                ARBlocks.osirisBossSpawner);

        return true;
    }

    protected void placeBossSpawner(final int x, final int y, final int z, final int chunkX, final int chunkZ,
            final Block[] arrayOfIDs, final byte[] arrayOfMeta, final BlockMetaPair spawner) {
        if (placeBlockAbs(arrayOfIDs, arrayOfMeta, x, y, z, chunkX, chunkZ, spawner)) {
            /*
             * List<Entity> entitiesWithin = this.worldObj.getEntitiesWithinAABB( EntityPlayer.class,
             * AxisAlignedBB.getBoundingBox( this.roomCoords.intX() - 1, this.roomCoords.intY() - 1,
             * this.roomCoords.intZ() - 1, this.roomCoords.intX() + this.roomSize.intX(), this.roomCoords.intY() +
             * this.roomSize.intY(), this.roomCoords.intZ() + this.roomSize.intZ() ) );
             */

            final AxisAlignedBB areaBB = AxisAlignedBB.getBoundingBox(
                    this.roomBB.minX,
                    this.roomBB.minY,
                    this.roomBB.minZ,
                    this.roomBB.maxX + 1,
                    this.roomBB.maxY + 1,
                    this.roomBB.maxZ + 1);
            this.parent.addPopulator(new InitBossSpawner(x, y, z, areaBB, EntityMummyBoss.class));
        }
    }

}
