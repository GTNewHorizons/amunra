package de.katzenpapst.amunra.world.maahes;

import java.util.List;

import net.minecraft.world.gen.feature.WorldGenerator;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.world.AmunraBiomeDecorator;
import de.katzenpapst.amunra.world.WorldGenOre;
import de.katzenpapst.amunra.world.WorldGenTallgrassMeta;
import de.katzenpapst.amunra.world.WorldGenTreeBySapling;

public class MaahesBiomeDecorator extends AmunraBiomeDecorator {

    protected WorldGenerator grassGen = new WorldGenTallgrassMeta(ARBlocks.blockMethaneTGrass);
    protected WorldGenerator treeGen = new WorldGenTreeBySapling(false, 5, ARBlocks.blockMethaneSapling);
    protected WorldGenerator podGen = new WorldGenTreeBySapling(false, 5, ARBlocks.blockPodSapling);

    private final int grassPerChunk = 5;

    @Override
    protected List<WorldGenOre> getOreGenerators() {
        final List<WorldGenOre> list = super.getOreGenerators();

        if (AmunRa.config.generateOres) {
            list.add(new WorldGenOre(ARBlocks.oreAluBasalt, 8, ARBlocks.blockBasalt, 16, 23, 70));
            list.add(new WorldGenOre(ARBlocks.oreGoldBasalt, 6, ARBlocks.blockBasalt, 8, 5, 30));
            list.add(new WorldGenOre(ARBlocks.oreLapisBasalt, 12, ARBlocks.blockBasalt, 6, 2, 20));
        }

        return list;
    }

    @Override
    protected void decorate() {
        super.decorate();

        for (int j = 0; j < this.grassPerChunk; ++j) {
            final int k = this.chunkX + this.mWorld.rand.nextInt(16) + 8;
            final int l = this.chunkZ + this.mWorld.rand.nextInt(16) + 8;
            final int i1 = this.mWorld.rand.nextInt(this.mWorld.getHeightValue(k, l) * 2);

            this.grassGen.generate(this.mWorld, this.mWorld.rand, k, i1, l);
        }
        final int numTrees = Math.round(this.mWorld.rand.nextInt(75) / 100.0F);
        for (int j = 0; j < numTrees; ++j) {
            final int k = this.chunkX + this.mWorld.rand.nextInt(16) + 8;
            final int l = this.chunkZ + this.mWorld.rand.nextInt(16) + 8;
            final int i1 = this.mWorld.getHeightValue(k, l);

            this.treeGen.generate(this.mWorld, this.mWorld.rand, k, i1, l);
        }

        final int numPods = Math.round(this.mWorld.rand.nextInt(65) / 100.0F);
        for (int j = 0; j < numPods; ++j) {
            final int k = this.chunkX + this.mWorld.rand.nextInt(16) + 8;
            final int l = this.chunkZ + this.mWorld.rand.nextInt(16) + 8;
            final int i1 = this.mWorld.getHeightValue(k, l);

            this.podGen.generate(this.mWorld, this.mWorld.rand, k, i1, l);
        }
    }

}
