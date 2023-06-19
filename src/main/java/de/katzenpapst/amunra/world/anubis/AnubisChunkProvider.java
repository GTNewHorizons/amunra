package de.katzenpapst.amunra.world.anubis;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.world.AmunraChunkProvider;
import de.katzenpapst.amunra.world.mapgen.village.BoxHouseComponent;
import de.katzenpapst.amunra.world.mapgen.village.DomedHouseComponent;
import de.katzenpapst.amunra.world.mapgen.village.GridVillageGenerator;
import de.katzenpapst.amunra.world.mapgen.village.SolarField;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeDecoratorSpace;

public class AnubisChunkProvider extends AmunraChunkProvider {

    protected GridVillageGenerator gVillage = new GridVillageGenerator();

    public AnubisChunkProvider(final World world, final long seed, final boolean mapFeaturesEnabled) {
        super(world, seed, mapFeaturesEnabled);

        this.gVillage.addComponentType(BoxHouseComponent.class, 0.9F, 2, 4);
        this.gVillage.addComponentType(SolarField.class, 0.7F, 2, 6);
        this.gVillage.addComponentType(DomedHouseComponent.class, 0.7F, 2, 4);

        // TODO fill in with caves and villages
        this.worldGenerators.add(this.gVillage);
    }

    @Override
    protected BiomeDecoratorSpace getBiomeGenerator() {
        return new AnubisBiomeDecorator();
    }

    @Override
    protected BlockMetaPair getDirtBlock() {
        return ARBlocks.blockBasaltRegolith;
    }

    @Override
    protected BlockMetaPair getGrassBlock() {
        return ARBlocks.blockDust;
    }

    @Override
    protected BlockMetaPair getStoneBlock() {
        return ARBlocks.blockBasalt;
    }

    @Override
    public double getHeightModifier() {
        return 12;
    }

    @Override
    public double getMountainHeightModifier() {
        return 95;
    }

    @Override
    protected int getSeaLevel() {
        return 93; // taken from Mars
    }

    @Override
    public double getSmallFeatureHeightModifier() {
        return 26;
    }

    @Override
    public double getValleyHeightModifier() {
        return 60;
    }

    @Override
    public void populate(IChunkProvider p_73153_1_, int p_73153_2_, int p_73153_3_) {
        super.populate(p_73153_1_, p_73153_2_, p_73153_3_);

        this.gVillage.populate(this, this.worldObj, p_73153_2_, p_73153_3_);
    }

    @Override
    public void recreateStructures(int p_82695_1_, int p_82695_2_) {}

}
