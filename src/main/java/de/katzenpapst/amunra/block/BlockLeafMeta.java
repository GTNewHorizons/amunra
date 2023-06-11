package de.katzenpapst.amunra.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.item.ItemBlockMulti;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class BlockLeafMeta extends BlockLeaves implements IMetaBlock {

    public static String[] unlocLeafNames = null;
    protected final Map<String, Integer> nameMetaMap = new HashMap<>();
    protected SubBlock[] subBlocksArray = new SubBlock[4];

    public BlockLeafMeta(final Material mat, final boolean gfxMode) {
        this.setLightOpacity(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBlockColor() {
        return 0xFFFFFF;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderColor(int meta) {
        return 0xFFFFFF;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess worldIn, int x, int y, int z) {
        return 0xFFFFFF;
    }

    @Override
    public BlockMetaPair addSubBlock(final int meta, final SubBlock sb) {
        if (!(sb instanceof SubBlockLeaf)) {
            throw new IllegalArgumentException("SubBlocks need to be instanceof SubBlockLeaf");
        }
        if (meta >= this.subBlocksArray.length || meta < 0) {
            throw new IllegalArgumentException(
                    "Meta " + meta + " must be <= " + (this.subBlocksArray.length - 1) + " && >= 0");
        }

        if (this.subBlocksArray[meta] != null) {
            throw new IllegalArgumentException("Meta " + meta + " is already in use");
        }

        if (this.nameMetaMap.get(sb.getUnlocalizedName()) != null) {
            throw new IllegalArgumentException("Name " + sb.getUnlocalizedName() + " is already in use");
        }
        sb.setParent(this);
        this.nameMetaMap.put(sb.getUnlocalizedName(), meta);
        this.subBlocksArray[meta] = sb;
        return new BlockMetaPair(this, (byte) meta);
    }

    @Override
    public int getMetaByName(final String name) {
        if (this.nameMetaMap.containsKey(name)) {
            return this.nameMetaMap.get(name);
        }
        throw new IllegalArgumentException("Subblock " + name + " doesn't exist");
    }

    @Override
    public SubBlock getSubBlock(int meta) {
        // this works like wood now
        meta = this.getDistinctionMeta(meta);
        return this.subBlocksArray[meta];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        final SubBlock sb = this.getSubBlock(meta);
        if (sb == null) {
            return null;
        }
        if (this.isOpaqueCube()) {
            return ((SubBlockLeaf) sb).getOpaqueIcon(side);
        }
        return sb.getIcon(side, 0);
    }

    /**
     * Seems to return all unlocalized names
     */
    @Override
    public String[] func_150125_e() {
        if (unlocLeafNames == null) {
            unlocLeafNames = new String[this.nameMetaMap.size()];
            for (final Map.Entry<String, Integer> entry : this.nameMetaMap.entrySet()) {
                final String key = entry.getKey();
                final int value = entry.getValue();
                unlocLeafNames[value] = key;
            }
        }
        return unlocLeafNames;
    }

    @Override
    public void register() {
        GameRegistry.registerBlock(this, ItemBlockMulti.class, this.getUnlocalizedName());

        for (int i = 0; i < this.subBlocksArray.length; i++) {
            final SubBlock sb = this.subBlocksArray[i];
            if (sb != null) {
                this.setHarvestLevel(sb.getHarvestTool(0), sb.getHarvestLevel(0), i);
            }
        }

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
        final SubBlock sb = this.getSubBlock(meta);

        if (sb == null || sb.dropsSelf()) {
            return Item.getItemFromBlock(this);
        }
        return sb.getItemDropped(0, random, fortune);
    }

    @Override
    public int damageDropped(int meta) {
        final SubBlock sb = this.getSubBlock(meta);
        if (sb == null || sb.dropsSelf()) {
            return meta;
        }
        return sb.damageDropped(0);
    }

    @Override
    public int getDamageValue(World worldIn, int x, int y, int z) {
        return worldIn.getBlockMetadata(x, y, z);
    }

    @Override
    public int quantityDropped(int meta, int fortune, Random random) {
        final SubBlock sb = this.getSubBlock(meta);
        if (sb == null || sb.dropsSelf()) {
            return 1;
        }
        return sb.quantityDropped(meta, fortune, random);
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
    public TileEntity createTileEntity(World world, int meta) {
        return null;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        final int meta = world.getBlockMetadata(x, y, z) & 3;
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
    public boolean getBlocksMovement(IBlockAccess worldIn, int x, int y, int z) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess worldIn, int x, int y, int z, int side) {
        final Block block = worldIn.getBlock(x, y, z);
        if (!this.isOpaqueCube() && block == this) {
            return true;
        }
        return super.shouldSideBeRendered(worldIn, x, y, z, side);

    }

    @Override
    public boolean renderAsNormalBlock() {
        return this.isOpaqueCube();
    }

    @Override
    public String getUnlocalizedSubBlockName(final int meta) {
        final SubBlock sb = this.getSubBlock(meta);
        if (sb != null) {
            return sb.getUnlocalizedName();
        }
        return this.getUnlocalizedName();
    }

    @Override
    public int getNumPossibleSubBlocks() {
        return 4;
    }

    @Override
    public int onBlockPlaced(World worldIn, int x, int y, int z, int side, float subX, float subY, float subZ,
            int meta) {
        final SubBlock sb = this.getSubBlock(meta);
        if (sb != null) {
            return sb.onBlockPlaced(worldIn, x, y, z, side, subX, subY, subZ, meta);
        }
        return super.onBlockPlaced(worldIn, x, y, z, side, subX, subY, subZ, meta);
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
    }

}
