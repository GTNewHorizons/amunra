package de.katzenpapst.amunra.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import micdoodle8.mods.galacticraft.api.block.IDetectableResource;
import micdoodle8.mods.galacticraft.api.block.IPlantableBlock;
import micdoodle8.mods.galacticraft.api.block.ITerraformableBlock;

public class SubBlock extends Block implements IDetectableResource, IPlantableBlock, ITerraformableBlock {

    protected int sbHarvestLevel = -1;
    protected String sbHarvestTool = "";

    // because blockName is private without getters...
    protected String blockNameFU;

    protected IIcon textureIcon;

    protected IMetaBlock parent = null;

    public SubBlock(final String name, final String texture) {
        super(Material.rock);
        this.init(name, texture, "pickaxe", 1, 1.5F, 10.0F);
    }

    public SubBlock(final String name, final String texture, final String tool, final int harvestLevel) {
        super(Material.rock);
        this.init(name, texture, tool, harvestLevel, 1.5F, 10.0F);
    }

    public SubBlock(final String name, final String texture, final String tool, final int harvestLevel,
            final float hardness, final float resistance) {
        super(Material.rock);
        this.init(name, texture, tool, harvestLevel, hardness, resistance);
    }

    protected void init(final String name, final String texture, final String harvestTool, final int havestLevel,
            final float hardness, final float resistance) {
        this.blockNameFU = name;
        this.setBlockName(name);
        this.setBlockTextureName(texture);
        this.setHarvestLevel(harvestTool, havestLevel);
        this.setHardness(hardness);
        this.setResistance(resistance);
    }

    @Override
    public String getLocalizedName() {
        return this.blockNameFU; // multiblock does that
    }

    @Override
    public String getUnlocalizedName() {
        return this.blockNameFU;
    }

    /**
     * if true, multiblock does the stuff itself
     */
    public boolean dropsSelf() {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return null;
    }

    @Override
    public boolean isTerraformable(World world, int x, int y, int z) {
        return false;
    }

    @Override
    public int requiredLiquidBlocksNearby() {
        return 4;
    }

    @Override
    public boolean isPlantable(int metadata) {
        return false;
    }

    @Override
    public boolean isValueable(int metadata) {
        return false;
    }

    public void setParent(final IMetaBlock parent) {
        if (parent instanceof Block) {
            this.parent = parent;
        }
    }

    public IMetaBlock getParent() {
        return this.parent;
    }

    @Override
    public void setHarvestLevel(String toolClass, int level) {
        this.sbHarvestLevel = level;
        this.sbHarvestTool = toolClass;
    }

    public SubBlock setHarvestInfo(String toolClass, int level) {
        this.setHarvestLevel(toolClass, level);
        return this;
    }

    @Override
    public String getHarvestTool(int metadata) {
        return this.sbHarvestTool;
    }

    @Override
    public int getHarvestLevel(int metadata) {
        return this.sbHarvestLevel;
    }

    @Override
    public void setHarvestLevel(String toolClass, int level, int metadata) {
        this.setHarvestLevel(toolClass, level);
    }

    public boolean canBeMoved(final World world, final int x, final int y, final int z) {
        return true;
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        // NOT calling super here, the metablock is doing that part
    }
}
