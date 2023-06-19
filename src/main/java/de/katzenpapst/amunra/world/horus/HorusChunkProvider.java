package de.katzenpapst.amunra.world.horus;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.world.AmunraChunkProvider;
import de.katzenpapst.amunra.world.mapgen.pyramid.BossRoom;
import de.katzenpapst.amunra.world.mapgen.pyramid.ChestRoom;
import de.katzenpapst.amunra.world.mapgen.pyramid.PitRoom;
import de.katzenpapst.amunra.world.mapgen.pyramid.PyramidGenerator;
import de.katzenpapst.amunra.world.mapgen.pyramid.PyramidRoom;
import de.katzenpapst.amunra.world.mapgen.volcano.VolcanoGenerator;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeDecoratorSpace;

public class HorusChunkProvider extends AmunraChunkProvider {

    protected final BlockMetaPair stoneBlock = new BlockMetaPair(Blocks.obsidian, (byte) 0);
    protected PyramidGenerator pyramid = new PyramidGenerator();
    private final VolcanoGenerator volcanoGen;

    public HorusChunkProvider(final World world, final long seed, final boolean mapFeaturesEnabled) {
        super(world, seed, mapFeaturesEnabled);
        this.pyramid.setFillMaterial(ARBlocks.blockBasaltBrick);
        this.pyramid.setFloorMaterial(ARBlocks.blockSmoothBasalt);
        this.pyramid.setWallMaterial(ARBlocks.blockObsidianBrick);
        this.pyramid.addComponentType(ChestRoom.class, 0.25F);
        this.pyramid.addComponentType(PitRoom.class, 0.25F);
        this.pyramid.addComponentType(PyramidRoom.class, 0.5F);
        this.pyramid.addMainRoomType(BossRoom.class, 1.0F);

        this.volcanoGen = new VolcanoGenerator(
                new BlockMetaPair(Blocks.lava, (byte) 0),
                this.stoneBlock,
                this.stoneBlock,
                15,
                true);

        this.worldGenerators.add(this.volcanoGen);
        this.worldGenerators.add(this.pyramid);
    }

    @Override
    protected BiomeDecoratorSpace getBiomeGenerator() {
        return new HorusBiomeDecorator();
    }

    @Override
    protected int getSeaLevel() {
        return 64;
    }

    @Override
    protected BlockMetaPair getGrassBlock() {
        return ARBlocks.blockObsidiSand;
    }

    @Override
    protected BlockMetaPair getDirtBlock() {
        return ARBlocks.blockObsidiGravel;
    }

    @Override
    protected BlockMetaPair getStoneBlock() {
        return this.stoneBlock;
    }

    @Override
    public double getHeightModifier() {
        return 20;
    }

    @Override
    public double getSmallFeatureHeightModifier() {
        return 40;
    }

    @Override
    public double getMountainHeightModifier() {
        return 60;
    }

    @Override
    public double getValleyHeightModifier() {
        return 60;
    }

    @Override
    public void populate(IChunkProvider p_73153_1_, int p_73153_2_, int p_73153_3_) {
        super.populate(p_73153_1_, p_73153_2_, p_73153_3_);

        this.pyramid.populate(this, this.worldObj, p_73153_2_, p_73153_3_);

    }

}
