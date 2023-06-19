package de.katzenpapst.amunra.block;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.item.ItemSlabMulti;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class BlockDoubleslabMeta extends BlockBasicMeta {

    protected final BlockSlabMeta slabMetablock;

    /**
     * I think this has to match the Slabs, meta-wise
     */
    public BlockDoubleslabMeta(final String name, final Material mat, final BlockSlabMeta slabMetablock) {
        super(name, mat, 8);
        this.slabMetablock = slabMetablock;
        slabMetablock.setDoubleslabMeta(this);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        return this.slabMetablock.getPickBlock(target, world, x, y, z);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        final ArrayList<ItemStack> ret = new ArrayList<>();

        final int count = this.quantityDropped(metadata, fortune, world.rand);
        for (int i = 0; i < count; i++) {
            final Item item = this.getItemDropped(metadata, world.rand, fortune);
            if (item != null) {
                ret.add(new ItemStack(item, 1, this.damageDropped(metadata)));
            }
        }
        return ret;
    }

    @Override
    public String getUnlocalizedSubBlockName(final int meta) {
        final SubBlock sb = this.getSubBlock(meta);
        if (sb != null) {
            return sb.getUnlocalizedName() + ".slab";
        }
        return super.getUnlocalizedSubBlockName(meta);
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return this.slabMetablock.getItemDropped(meta, random, fortune);
    }

    @Override
    public int damageDropped(int meta) {
        return this.getDistinctionMeta(meta);
    }

    @Override
    public int quantityDropped(int meta, int fortune, Random random) {
        return 2;
    }

    @Override
    public BlockMetaPair addSubBlock(final int meta, final SubBlock sb) {
        if (meta >= this.subBlocksArray.length || meta < 0) {
            throw new IllegalArgumentException(
                    "Meta " + meta + " must be < " + this.subBlocksArray.length + " and >= 0");
        }
        if (this.subBlocksArray[meta] != null) {
            throw new IllegalArgumentException("Meta " + meta + " is already in use in " + this.blockNameFU);
        }
        if (this.nameMetaMap.containsKey(sb.getUnlocalizedName())) {
            throw new IllegalArgumentException(
                    "Name " + sb.getUnlocalizedName() + " is already in use in " + this.blockNameFU);
        }
        this.nameMetaMap.put(sb.getUnlocalizedName(), meta);
        this.subBlocksArray[meta] = sb;
        return new BlockMetaPair(this, (byte) meta);
    }

    public BlockMetaPair addSubBlock(final int meta) {
        return this.addSubBlock(meta, this.slabMetablock.getSubBlock(meta));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public CreativeTabs getCreativeTabToDisplayOn() {
        return null;
    }

    @Override
    public void register() {
        for (int i = 0; i < this.slabMetablock.getAllSubBlocks().length; i++) {
            final SubBlock sb = this.slabMetablock.getSubBlock(i);
            if (sb != null) {
                this.addSubBlock(i, sb);
            }
        }
        GameRegistry.registerBlock(this, ItemSlabMulti.class, this.getUnlocalizedName(), this.slabMetablock, this);

        for (int i = 0; i < this.subBlocksArray.length; i++) {
            final SubBlock sb = this.subBlocksArray[i];
            if (sb != null) {
                this.setHarvestLevel(sb.getHarvestTool(0), sb.getHarvestLevel(0), i);
            }
        }
    }
}
