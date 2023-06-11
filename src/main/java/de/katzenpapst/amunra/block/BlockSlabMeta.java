package de.katzenpapst.amunra.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.helper.BlockMassHelper;
import de.katzenpapst.amunra.item.ItemSlabMulti;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class BlockSlabMeta extends BlockSlab implements IMetaBlock, IMassiveBlock {

    protected final Map<String, Integer> nameMetaMap = new HashMap<>();
    protected SubBlock[] subBlocksArray = new SubBlock[8];
    protected BlockDoubleslabMeta doubleslabMetablock;

    public BlockSlabMeta(final String name, final Material material) {
        // I think the first parameter is true for doubleslabs...
        super(false, material);
        this.setBlockName(name);
    }

    @Override
    public String getUnlocalizedSubBlockName(final int meta) {
        final SubBlock sb = this.getSubBlock(meta);
        if (sb != null) {
            return sb.getUnlocalizedName() + ".slab";
        }
        return this.getUnlocalizedName() + ".slab";
    }

    public void setDoubleslabMeta(final BlockDoubleslabMeta doubleslabMetablock) {
        this.doubleslabMetablock = doubleslabMetablock;
    }

    @Override
    public BlockMetaPair addSubBlock(final int meta, final SubBlock sb) {
        if (meta >= this.subBlocksArray.length || meta < 0) {
            throw new IllegalArgumentException(
                    "Meta " + meta + " must be < " + this.subBlocksArray.length + " and >= 0");
        }

        if (this.subBlocksArray[meta] != null) {
            throw new IllegalArgumentException("Meta " + meta + " is already in use");
        }

        if (this.nameMetaMap.containsKey(sb.getUnlocalizedName())) {
            throw new IllegalArgumentException("Name " + sb.getUnlocalizedName() + " is already in use");
        }
        this.nameMetaMap.put(sb.getUnlocalizedName(), meta);
        this.subBlocksArray[meta] = sb;
        return new BlockMetaPair(this, (byte) meta);
    }

    public BlockMetaPair addSubBlock(final int meta, final BlockMetaPair basedOn) {
        return this.addSubBlock(meta, ((IMetaBlock) basedOn.getBlock()).getSubBlock(basedOn.getMetadata()));
    }

    @Override
    public int getMetaByName(final String name) {
        if (this.nameMetaMap.containsKey(name)) {
            return this.nameMetaMap.get(name);
        }
        throw new IllegalArgumentException("Subblock " + name + " doesn't exist");
    }

    @Override
    public SubBlock getSubBlock(final int meta) {
        return this.subBlocksArray[this.getDistinctionMeta(meta)];
    }

    public SubBlock[] getAllSubBlocks() {
        return this.subBlocksArray;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        final SubBlock sb = this.getSubBlock(meta);
        if (sb != null) {
            return sb.getIcon(side, 0);
        }
        return super.getIcon(side, meta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        for (final SubBlock sb : this.subBlocksArray) {
            if (sb != null) {
                sb.registerBlockIcons(reg);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public CreativeTabs getCreativeTabToDisplayOn() {
        return AmunRa.arTab;
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return Item.getItemFromBlock(this);
    }

    @Override
    public int damageDropped(int meta) {
        return this.getDistinctionMeta(meta);
    }

    @Override
    public int getDamageValue(World worldIn, int x, int y, int z) {
        return worldIn.getBlockMetadata(x, y, z);
    }

    @Override
    public int quantityDropped(int meta, int fortune, Random random) {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        for (int i = 0; i < this.subBlocksArray.length; i++) {
            if (this.subBlocksArray[i] != null) {
                list.add(new ItemStack(itemIn, 1, i));
            }
        }
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        final int meta = world.getBlockMetadata(x, y, z);
        if (this.getSubBlock(meta) != null) {
            return new ItemStack(Item.getItemFromBlock(this), 1, this.getDistinctionMeta(meta));
        }
        return super.getPickBlock(target, world, x, y, z, player);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        return this.getPickBlock(target, world, x, y, z, null);
    }

    @Override
    public void register() {
        // doubleslabMetablock
        GameRegistry
                .registerBlock(this, ItemSlabMulti.class, this.getUnlocalizedName(), this, this.doubleslabMetablock);

        for (int i = 0; i < this.subBlocksArray.length; i++) {
            final SubBlock sb = this.subBlocksArray[i];
            if (sb != null) {
                this.setHarvestLevel(sb.getHarvestTool(0), sb.getHarvestLevel(0), i);
            }
        }
    }

    /**
     * {@code getFullSlabName} Returns the slab block name with the type associated with it
     */
    @Override
    public String func_150002_b(int p_150002_1_) {
        final SubBlock sb = this.getSubBlock(p_150002_1_);
        if (sb != null) {
            return this.getUnlocalizedName() + "." + sb.getUnlocalizedName();
        }
        return this.getUnlocalizedName();
    }

    @Override
    public float getExplosionResistance(Entity par1Entity, World world, int x, int y, int z, double explosionX,
            double explosionY, double explosionZ) {
        final SubBlock sb = this.getSubBlock(world.getBlockMetadata(x, y, z));
        if (sb != null) {
            return sb.getExplosionResistance(par1Entity, world, x, y, z, explosionX, explosionY, explosionZ);
        }
        return super.getExplosionResistance(par1Entity, world, x, y, z, explosionX, explosionY, explosionZ);
    }

    @Override
    public float getBlockHardness(World worldIn, int x, int y, int z) {
        final SubBlock sb = this.getSubBlock(worldIn.getBlockMetadata(x, y, z));
        if (sb != null) {
            return sb.getBlockHardness(worldIn, x, y, z);
        }
        return super.getBlockHardness(worldIn, x, y, z);
    }

    @Override
    public int getNumPossibleSubBlocks() {
        return this.subBlocksArray.length;
    }

    @Override
    public int getExpDrop(IBlockAccess world, int metadata, int fortune) {
        final SubBlock sb = this.getSubBlock(metadata);
        if (sb != null) {
            return sb.getExpDrop(world, 0, fortune);
        }
        return super.getExpDrop(world, metadata, fortune);
    }

    @Override
    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        final SubBlock sb = this.getSubBlock(worldIn.getBlockMetadata(x, y, z));
        if (sb != null) {
            sb.onNeighborBlockChange(worldIn, x, y, z, neighbor);
        }
        super.onNeighborBlockChange(worldIn, x, y, z, neighbor);
    }

    @Override
    public float getMass(final World w, final int x, final int y, final int z, final int meta) {
        // return half the mass, because slab
        return BlockMassHelper.getBlockMass(w, this.getSubBlock(meta), meta, x, y, z) / 2.0f;
    }

    @Override
    public String getHarvestTool(int metadata) {
        final SubBlock sb = this.getSubBlock(metadata);
        return sb == null ? null : sb.getHarvestTool(metadata);
    }

    @Override
    public int getHarvestLevel(int metadata) {
        final SubBlock sb = this.getSubBlock(metadata);
        return sb == null ? -1 : sb.getHarvestLevel(metadata);
    }

    @Override
    public boolean isToolEffective(String type, int metadata) {
        return this.getHarvestTool(metadata).equals(type);
    }
}
