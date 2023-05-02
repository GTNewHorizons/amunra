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
    public IIcon getIcon(final int side, final int meta) {
        return this.blockIcon;
    }

    public BlockOreMulti setMultiblockHarvestLevel(final int level) {
        mbHarvestLevel = level;
        return this;
    }

    public int getMultiblockHarvestLevel() {
        return mbHarvestLevel;
    }

    public BlockOreMulti setMultiblockHarvestTool(final String tool) {
        mbHarvestTool = tool;
        return this;
    }

    public String getMultiblockHarvestTool() {
        return mbHarvestTool;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(final IIconRegister par1IconRegister) {
        super.registerBlockIcons(par1IconRegister);
        this.blockIcon = par1IconRegister.registerIcon(this.textureName);
    }

    /**
     * Registers the block with the GameRegistry and sets the harvestlevels for all subblocks
     */
    @Override
    public void register() {
        GameRegistry.registerBlock(this, ItemBlockMulti.class, this.getUnlocalizedName());

        for (int i = 0; i < subBlocksArray.length; i++) {
            final SubBlock sb = subBlocksArray[i];
            if (sb != null) {
                this.setHarvestLevel(
                        mbHarvestTool == null ? sb.getHarvestTool(0) : mbHarvestTool,
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

    /**
     * The type of render function that is called for this block
     */
    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType() {
        return AmunRa.multiOreRendererId;
    }

    @Override
    public boolean isValueable(final int metadata) {
        return true;
    }

    @Override
    public float getExplosionResistance(final Entity entity, final World world, final int x, final int y, final int z, final double explosionX,
            final double explosionY, final double explosionZ) {
        return Math.max(
                super.getExplosionResistance(entity, world, x, y, z, explosionX, explosionY, explosionZ),
                this.getExplosionResistance(entity) // default resistance, should default to this.blockResistance / 5.0F
        );
    }

    @Override
    public float getBlockHardness(final World par1World, final int x, final int y, final int z) {
        return Math.max(super.getBlockHardness(par1World, x, y, z), this.blockHardness);
    }

}
