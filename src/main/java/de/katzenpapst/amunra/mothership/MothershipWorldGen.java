package de.katzenpapst.amunra.mothership;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import de.katzenpapst.amunra.block.ARBlocks;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.core.blocks.GCBlocks;

public class MothershipWorldGen extends WorldGenerator {

    private final BlockMetaPair groundBlock;
    private final BlockMetaPair decoBlock;
    private final BlockMetaPair glassBlock;
    private final BlockMetaPair msController;
    private final BlockMetaPair msIndestructible;

    /*
     * private BlockMetaPair msJet; private BlockMetaPair msEngine;
     */

    public MothershipWorldGen() {
        this.groundBlock = new BlockMetaPair(GCBlocks.basicBlock, (byte) 4);
        this.decoBlock = new BlockMetaPair(GCBlocks.basicBlock, (byte) 3);
        this.glassBlock = new BlockMetaPair(Blocks.glass, (byte) 0);
        this.msController = ARBlocks.blockMothershipController;
        this.msIndestructible = ARBlocks.blockMsBase;
        /*
         * msJet = ARBlocks.blockMsEngineRocketJet; msEngine = ARBlocks.blockMsEngineRocketBooster;
         */
    }

    @Override
    public boolean generate(World p_76484_1_, Random p_76484_2_, int p_76484_3_, int p_76484_4_, int p_76484_5_) {
        // for this, assume the coordinates we got are the center
        // make one big plane first
        int startX = p_76484_3_ - 3;
        int stopX = p_76484_3_ + 3;
        int startZ = p_76484_5_ - 3;
        int stopZ = p_76484_5_ + 3;

        for (int x = startX; x <= stopX; x++) {
            for (int z = startZ; z <= stopZ; z++) {
                if (x == startX || x == stopX || z == startZ || z == stopZ) {
                    p_76484_1_.setBlock(x, p_76484_4_, z, this.decoBlock.getBlock(), this.decoBlock.getMetadata(), 3);
                } else {
                    p_76484_1_
                            .setBlock(x, p_76484_4_, z, this.groundBlock.getBlock(), this.groundBlock.getMetadata(), 3);
                }
            }
        }

        // place that one failsafe block
        // msIndestructible
        p_76484_1_.setBlock(
                p_76484_3_,
                p_76484_4_,
                p_76484_5_,
                this.msIndestructible.getBlock(),
                this.msIndestructible.getMetadata(),
                3);

        startX = p_76484_3_ - 3;
        stopX = p_76484_3_ + 3;
        startZ = p_76484_5_ - 3 - 7;
        stopZ = p_76484_5_ + 3 - 7;

        for (int x = startX; x <= stopX; x++) {
            for (int z = startZ; z <= stopZ; z++) {
                // floor
                p_76484_1_.setBlock(x, p_76484_4_, z, this.groundBlock.getBlock(), this.groundBlock.getMetadata(), 3);

                // sides
                if (x == startX || x == stopX || z == startZ || z == stopZ) {
                    // roof border
                    p_76484_1_.setBlock(
                            x,
                            p_76484_4_ + 4,
                            z,
                            this.groundBlock.getBlock(),
                            this.groundBlock.getMetadata(),
                            3);
                    if (x > startX + 1 && x < stopX - 1 || z > startZ + 1 && z < stopZ - 1) {
                        continue;
                    }
                    // walls
                    for (int y = p_76484_4_ + 1; y < p_76484_4_ + 4; y++) {
                        if (y > p_76484_4_ + 1 && y < p_76484_4_ + 3) {
                            p_76484_1_.setBlock(x, y, z, this.glassBlock.getBlock(), this.glassBlock.getMetadata(), 3);
                        } else {
                            p_76484_1_.setBlock(x, y, z, this.decoBlock.getBlock(), this.decoBlock.getMetadata(), 3);
                        }
                    }
                } else {
                    // roof center
                    p_76484_1_.setBlock(
                            x,
                            p_76484_4_ + 4,
                            z,
                            this.glassBlock.getBlock(),
                            this.glassBlock.getMetadata(),
                            3);
                }
            }
        }

        // "wings"
        startX = p_76484_3_ - 1 + 5;
        stopX = p_76484_3_ + 1 + 5;
        startZ = p_76484_5_ - 1 - 7;
        stopZ = p_76484_5_ + 1 - 7;

        for (int x = startX; x <= stopX; x++) {
            for (int z = startZ; z <= stopZ; z++) {
                p_76484_1_.setBlock(x, p_76484_4_, z, this.groundBlock.getBlock(), this.groundBlock.getMetadata(), 3);
            }
        }

        // machines
        // 0, -9 0 => controller
        final int rotationMeta = 2;
        p_76484_1_.setBlock(
                p_76484_3_ - 3,
                p_76484_4_ + 1,
                p_76484_5_ - 7,
                this.msController.getBlock(),
                this.msController.getMetadata() | rotationMeta << 2,
                3);

        return true;
    }
}
