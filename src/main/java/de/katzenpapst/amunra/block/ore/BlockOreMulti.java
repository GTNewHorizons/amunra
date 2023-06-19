package de.katzenpapst.amunra.block.ore;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.block.BlockBasicMeta;
import de.katzenpapst.amunra.block.SubBlock;
import de.katzenpapst.amunra.item.ItemBlockMulti;

public class BlockOreMulti extends BlockBasicMeta {

    // the subblocks will be the different ores and doing overlays, the main block will define the stone
    // for hardness, explosion resistance and harvest level, the maximum of sub and mainblock will be used

    /**
     * Harvest level for the main block
     */
    protected int mbHarvestLevel = -1;

    /**
     * Harvest tool, if set, it will override the tools of the subblocks
     */
    protected String mbHarvestTool = null;

    public BlockOreMulti(final String name, final String texture, final Material mat) {
        super(name, mat);
        this.textureName = texture;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        return this.blockIcon;
    }

    public BlockOreMulti setMultiblockHarvestLevel(final int level) {
        this.mbHarvestLevel = level;
        return this;
    }

    public int getMultiblockHarvestLevel() {
        return this.mbHarvestLevel;
    }

    public BlockOreMulti setMultiblockHarvestTool(final String tool) {
        this.mbHarvestTool = tool;
        return this;
    }

    public String getMultiblockHarvestTool() {
        return this.mbHarvestTool;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        super.registerBlockIcons(reg);
        this.blockIcon = reg.registerIcon(this.textureName);
    }

    /**
     * Registers the block with the GameRegistry and sets the harvestlevels for all subblocks
     */
    @Override
    public void register() {
        GameRegistry.registerBlock(this, ItemBlockMulti.class, this.getUnlocalizedName());

        for (int i = 0; i < this.subBlocksArray.length; i++) {
            final SubBlock sb = this.subBlocksArray[i];
            if (sb != null) {
                this.setHarvestLevel(
                        this.mbHarvestTool == null ? sb.getHarvestTool(0) : this.mbHarvestTool,
                        Math.max(sb.getHarvestLevel(0), this.getMultiblockHarvestLevel()),
                        i);
                if (sb instanceof SubBlockOre sbOre) {
                    for (final String name : sbOre.getOredictNames()) {
                        OreDictionary.registerOre(name, new ItemStack(this, 1, i));
                    }
                }
            }
        }
    }

    @Override
    public int getRenderType() {
        return AmunRa.multiOreRendererId;
    }

    @Override
    public boolean isValueable(final int metadata) {
        return true;
    }

    @Override
    public float getExplosionResistance(Entity exploder, World world, int x, int y, int z, double explosionX,
            double explosionY, double explosionZ) {
        return Math.max(
                super.getExplosionResistance(exploder, world, x, y, z, explosionX, explosionY, explosionZ),
                // default resistance, should be this.blockResistance / 5.0F
                this.getExplosionResistance(exploder));
    }

    @Override
    public float getBlockHardness(World worldIn, int x, int y, int z) {
        return Math.max(super.getBlockHardness(worldIn, x, y, z), this.blockHardness);
    }

}
