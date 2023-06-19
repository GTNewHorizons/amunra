package de.katzenpapst.amunra.world.mapgen.volcano;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.helper.CoordHelper;
import de.katzenpapst.amunra.world.mapgen.BaseStructureStart;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.core.perlin.generator.Gradient;

public class Volcano extends BaseStructureStart {

    protected BlockMetaPair fluid;
    protected BlockMetaPair mountainMaterial;
    protected BlockMetaPair shaftMaterial;
    protected int maxDepth = 2;
    protected int maxHeight = 50; // over ground

    // radius*2+1 will be the circumference
    protected int radius = 50;
    protected int shaftRadius = 2;
    protected int calderaRadius = 6;
    protected int falloffWidth = 9;

    protected int magmaChamberWidth;
    protected int magmaChamberHeight;
    protected Gradient testGrad;

    protected boolean hasMagmaChamber = false;

    public boolean hasMagmaChamber() {
        return this.hasMagmaChamber;
    }

    public void setHasMagmaChamber(final boolean hasMagmaChamber) {
        this.hasMagmaChamber = hasMagmaChamber;
    }

    public Volcano(final World world, final int chunkX, final int chunkZ, final Random rand) {
        super(world, chunkX, chunkZ, rand);
        final int startX = CoordHelper.chunkToMinBlock(chunkX) + MathHelper.getRandomIntegerInRange(rand, 0, 15);
        final int startZ = CoordHelper.chunkToMinBlock(chunkZ) + MathHelper.getRandomIntegerInRange(rand, 0, 15);
        final StructureBoundingBox bb = new StructureBoundingBox(
                startX - this.radius,
                startZ - this.radius,
                startX + this.radius,
                startZ + this.radius);
        this.setStructureBoundingBox(bb);
        AmunRa.LOGGER.debug("Generating Volcano at {}/{}", startX, startZ);

        this.testGrad = new Gradient(this.rand.nextLong(), 4, 0.25F);
        this.testGrad.setFrequency(0.05F);

        this.calderaRadius = MathHelper.getRandomIntegerInRange(rand, 5, 7);
        this.shaftRadius = MathHelper.getRandomIntegerInRange(rand, 1, 3);

        this.radius = MathHelper.getRandomIntegerInRange(rand, 46, 56);

        this.magmaChamberWidth = MathHelper.getRandomIntegerInRange(rand, this.radius - 10, this.radius);
        this.magmaChamberHeight = MathHelper.getRandomIntegerInRange(rand, this.radius / 2, this.radius);

    }

    protected double getHeightFromDistance(final double distance) {
        return this.maxHeight * ((this.radius - distance) / this.radius);
    }

    @Override
    public boolean generateChunk(final int chunkX, final int chunkZ, final Block[] blocks, final byte[] metas) {
        super.generateChunk(chunkX, chunkZ, blocks, metas);

        // test first
        final StructureBoundingBox chunkBB = CoordHelper.getChunkBB(chunkX, chunkZ);
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

        final int xCenter = myBB.getCenterX();
        final int zCenter = myBB.getCenterZ();

        final double sqRadius = Math.pow(this.radius, 2);

        // after this radius, falloff will be used
        final int faloffRadius = this.radius - this.falloffWidth;

        // TODO: make all height variables absolute, then try to figure out
        // why the fuck it explodes with noise
        for (int x = myBB.minX; x <= myBB.maxX; x++) {
            for (int z = myBB.minZ; z <= myBB.maxZ; z++) {

                if (!chunkBB.isVecInside(x, 64, z)) {
                    continue;
                }

                int lowestBlock = getHighestSpecificBlock(
                        blocks,
                        metas,
                        CoordHelper.abs2rel(x, chunkX),
                        CoordHelper.abs2rel(z, chunkZ),
                        this.mountainMaterial.getBlock(),
                        this.mountainMaterial.getMetadata());
                if (lowestBlock == -1) {
                    lowestBlock = this.maxDepth;
                }

                final int xRel = x - xCenter;
                final int zRel = z - zCenter;

                final int sqDistance = xRel * xRel + zRel * zRel;

                final double heightAtCalderaBorder = this.getHeightFromDistance(this.calderaRadius) + this.groundLevel;
                final double fluidHeight = this.getHeightFromDistance(this.shaftRadius) + this.groundLevel;

                if (sqDistance <= sqRadius) {
                    final double distance = Math.sqrt(sqDistance);

                    int height;
                    if (distance <= this.shaftRadius) {
                        height = (int) fluidHeight;
                        height = (int) (heightAtCalderaBorder - (height - heightAtCalderaBorder));
                    } else {

                        height = (int) this.getHeightFromDistance(distance) + this.groundLevel;

                        if (distance > faloffRadius && lowestBlock < height && this.groundLevel > lowestBlock) {
                            // somewhat of a falloff at the edges
                            final double faloffFactor = (distance - faloffRadius) / this.falloffWidth;
                            height = (int) this.lerp(height, lowestBlock, faloffFactor);

                        }

                        // if we are past the caldera radius, go lower again
                        if (distance <= this.calderaRadius) {
                            height = (int) (heightAtCalderaBorder - (height - heightAtCalderaBorder));
                        }

                        height += Math.round(this.testGrad.getNoise(x, z) * 8);
                    }

                    if (height > 255) {
                        height = 255;
                    }
                    if (height < lowestBlock) {
                        height = lowestBlock;
                    }

                    if (distance < this.shaftRadius + 2) {
                        for (int y = this.maxDepth + 1; y < height; y++) {

                            if (distance <= this.shaftRadius) {
                                placeBlockAbs(blocks, metas, x, y, z, chunkX, chunkZ, this.fluid);
                            } else {
                                placeBlockAbs(blocks, metas, x, y, z, chunkX, chunkZ, this.shaftMaterial);
                            }
                        }

                    } else {
                        for (int y = lowestBlock; y < height; y++) {

                            placeBlockAbs(blocks, metas, x, y, z, chunkX, chunkZ, this.mountainMaterial);

                        }
                    }
                }

                if (this.hasMagmaChamber) {
                    // ellipsoid: x²/a² + y²/b² + z²/c² = 1
                    for (int y = 0; y < this.magmaChamberHeight; y++) {
                        if (xRel * xRel / this.magmaChamberWidth * this.magmaChamberWidth
                                + y * y / this.magmaChamberHeight * this.magmaChamberHeight
                                + zRel * zRel / this.magmaChamberWidth * this.magmaChamberWidth <= 1

                        ) {
                            placeBlockAbs(blocks, metas, x, y + this.maxDepth, z, chunkX, chunkZ, this.fluid);
                        }
                    }

                }

            }

        }

        return true;
    }

    public BlockMetaPair getFluid() {
        return this.fluid;
    }

    public void setFluid(final BlockMetaPair fluid) {
        this.fluid = fluid;
    }

    public BlockMetaPair getMountainMaterial() {
        return this.mountainMaterial;
    }

    public void setMountainMaterial(final BlockMetaPair mountainMaterial) {
        this.mountainMaterial = mountainMaterial;
    }

    public BlockMetaPair getShaftMaterial() {
        return this.shaftMaterial;
    }

    public void setShaftMaterial(final BlockMetaPair shaftMaterial) {
        this.shaftMaterial = shaftMaterial;
    }

    public int getMaxDepth() {
        return this.maxDepth;
    }

    public void setMaxDepth(final int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getRadius() {
        return this.radius;
    }

    public void setRadius(final int radius) {
        this.radius = radius;
    }

}
