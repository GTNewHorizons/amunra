package de.katzenpapst.amunra.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.block.bush.SubBlockBush;
import de.katzenpapst.amunra.helper.BlockMassHelper;
import de.katzenpapst.amunra.item.ItemBlockMulti;
import micdoodle8.mods.galacticraft.api.block.IDetectableResource;
import micdoodle8.mods.galacticraft.api.block.IPartialSealableBlock;
import micdoodle8.mods.galacticraft.api.block.IPlantableBlock;
import micdoodle8.mods.galacticraft.api.block.ITerraformableBlock;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class BlockBasicMeta extends Block implements IMetaBlock, IDetectableResource, IPlantableBlock,
        ITerraformableBlock, IMassiveBlock, IPartialSealableBlock {

    // protected ArrayList<SubBlock> subBlocks = null;
    protected SubBlock[] subBlocksArray;
    protected final Map<String, Integer> nameMetaMap = new HashMap<>();

    /**
     * If true, the baseblock's name will be prefixed to the subblock's name for localisation
     */
    protected boolean prefixOwnBlockName = false;

    protected String blockNameFU;

    public BlockBasicMeta(final String name, final Material mat, final int numSubBlocks) {
        super(mat); // todo replace this
        this.subBlocksArray = new SubBlock[numSubBlocks];
        this.blockNameFU = name;
        // subBlocks = new ArrayList<SubBlock>(initialCapacity);
        this.setBlockName(name);
    }

    public BlockBasicMeta setPrefixOwnBlockName(final boolean set) {
        this.prefixOwnBlockName = set;
        return this;
    }

    @Override
    public void updateTick(World worldIn, int x, int y, int z, Random random) {
        if (!worldIn.isRemote) {
            final SubBlock sb = this.getSubBlock(worldIn.getBlockMetadata(x, y, z));
            if (sb != null) {
                sb.updateTick(worldIn, x, y, z, random);
            }
        }
    }

    public BlockBasicMeta(final String name, final Material mat) {
        this(name, mat, 16);
    }

    @Override
    public int getMetaByName(final String name) {
        if (this.nameMetaMap.containsKey(name)) {
            return this.nameMetaMap.get(name);
        }
        throw new IllegalArgumentException("Subblock " + name + " doesn't exist in " + this.blockNameFU);
    }

    @Override
    public BlockMetaPair addSubBlock(int meta, SubBlock sb) {
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
        sb.setParent(this);
        this.nameMetaMap.put(sb.getUnlocalizedName(), meta);
        this.subBlocksArray[meta] = sb;
        return new BlockMetaPair(this, (byte) meta);
    }

    @Override
    public SubBlock getSubBlock(final int meta) {
        return this.subBlocksArray[this.getDistinctionMeta(meta)];
    }

    @Override
    public int getNumPossibleSubBlocks() {
        return this.subBlocksArray.length;
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
    public float getExplosionResistance(Entity exploder, World world, int x, int y, int z, double explosionX,
            double explosionY, double explosionZ) {
        final SubBlock sb = this.getSubBlock(world.getBlockMetadata(x, y, z));
        if (sb != null) {
            return sb.getExplosionResistance(exploder, world, x, y, z, explosionX, explosionY, explosionZ);
        }
        return super.getExplosionResistance(exploder, world, x, y, z, explosionX, explosionY, explosionZ);
    }

    @Override
    public float getBlockHardness(World worldIn, int x, int y, int z) {
        final SubBlock sb = this.getSubBlock(worldIn.getBlockMetadata(x, y, z));
        if (sb != null) {
            return sb.getBlockHardness(worldIn, x, y, z);
        }
        return super.getBlockHardness(worldIn, x, y, z);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        /*
         * Face 0 (Bottom Face) Face 1 (Top Face) Face 2 (Northern Face) Face 3 (Southern Face) Face 4 (Western Face)
         * Face 5 (Eastern Face)
         */
        final SubBlock sb = this.getSubBlock(meta);
        if (sb != null) {
            sb.getIcon(side, meta);
        }
        return super.getIcon(side, meta);
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
            return this.getDistinctionMeta(meta);
        }
        return sb.damageDropped(0);
    }

    @Override
    public int getDamageValue(World worldIn, int x, int y, int z) {
        return this.getDistinctionMeta(worldIn.getBlockMetadata(x, y, z));
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
    public boolean hasTileEntity(int meta) {
        final SubBlock sb = this.getSubBlock(meta);
        if (sb == null) {
            return super.hasTileEntity(meta);
        }
        return sb.hasTileEntity(meta);
    }

    @Override
    public TileEntity createTileEntity(World world, int meta) {
        final SubBlock sb = this.getSubBlock(meta);
        if (sb == null) {
            return super.createTileEntity(world, meta);
        }
        return sb.createTileEntity(world, meta);
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
    public boolean getBlocksMovement(IBlockAccess worldIn, int x, int y, int z) {
        final SubBlock sb = this.getSubBlock(worldIn.getBlockMetadata(x, y, z));
        if (sb != null) {
            return sb.getBlocksMovement(worldIn, x, y, z);
        }
        return super.getBlocksMovement(worldIn, x, y, z);
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
            float subY, float subZ) {
        final SubBlock sb = this.getSubBlock(worldIn.getBlockMetadata(x, y, z));
        if (sb != null) {
            return sb.onBlockActivated(worldIn, x, y, z, player, side, subX, subY, subZ);
        }
        return super.onBlockActivated(worldIn, x, y, z, player, side, subX, subY, subZ);
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
    public boolean isTerraformable(World world, int x, int y, int z) {
        final SubBlock sb = this.getSubBlock(world.getBlockMetadata(x, y, z));
        if (sb != null) {
            return sb.isTerraformable(world, x, y, z);
        }
        return false;
    }

    @Override
    public int requiredLiquidBlocksNearby() {
        return 4; // I can't actually return the value of the subblock
    }

    @Override
    public boolean isPlantable(int metadata) {
        final SubBlock sb = this.getSubBlock(metadata);
        if (sb != null) {
            return sb.isPlantable(0);
        }
        return false;
    }

    @Override
    public boolean isValueable(int metadata) {
        final SubBlock sb = this.getSubBlock(metadata);
        if (sb != null) {
            return sb.isValueable(0);
        }
        return false;
    }

    @Override
    public Material getMaterial() {
        return this.blockMaterial;
    }

    @Override
    public boolean canSustainPlant(IBlockAccess world, int x, int y, int z, ForgeDirection direction,
            IPlantable plantable) {
        if (plantable instanceof SubBlockBush sbBush) {
            return sbBush.canPlaceOn(
                    plantable.getPlant(world, x, y + 1, z),
                    plantable.getPlantMetadata(world, x, y + 1, z),
                    0);
        }
        return super.canSustainPlant(world, x, y, z, direction, plantable);
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        final SubBlock sb = this.getSubBlock(world.getBlockMetadata(x, y, z));
        if (sb != null) {
            return sb.getLightValue();
        }
        return super.getLightValue();
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        final SubBlock sb = this.getSubBlock(metadata);
        if (sb == null || sb.dropsSelf()) {
            return super.getDrops(world, x, y, z, metadata, fortune);
        }
        return sb.getDrops(world, x, y, z, 0, fortune);

    }

    @Override
    public String getUnlocalizedSubBlockName(final int meta) {
        final SubBlock sb = this.getSubBlock(meta);
        if (sb == null) {
            return this.blockNameFU;
        }
        if (this.prefixOwnBlockName) {
            return this.blockNameFU + "." + sb.getUnlocalizedName();
        }
        return sb.getUnlocalizedName();
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
    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        final SubBlock sb = this.getSubBlock(worldIn.getBlockMetadata(x, y, z));
        if (sb != null) {
            sb.onNeighborBlockChange(worldIn, x, y, z, neighbor);
        }
    }

    @Override
    public float getMass(final World w, final int x, final int y, final int z, final int meta) {
        final SubBlock sb = this.getSubBlock(meta);
        if (sb instanceof IMassiveBlock massiveBlock) {
            return massiveBlock.getMass(w, x, y, z, meta);
        }
        if (sb == null) {
            return 0.0f;
        }
        return BlockMassHelper.guessBlockMass(w, sb, meta, x, y, z);
    }

    @Override
    public boolean isSealed(World world, int x, int y, int z, ForgeDirection direction) {
        if (this.getSubBlock(world.getBlockMetadata(x, y, z)) instanceof IPartialSealableBlock psb) {
            return psb.isSealed(world, x, y, z, direction);
        }
        return true;
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        final SubBlock sb = this.getSubBlock(meta);
        if (sb != null) {
            sb.breakBlock(worldIn, x, y, z, blockBroken, meta);
        }
    }

    @Override
    public boolean canBlockStay(World worldIn, int x, int y, int z) {
        final SubBlock sb = this.getSubBlock(worldIn.getBlockMetadata(x, y, z));
        if (sb != null) {
            return sb.canBlockStay(worldIn, x, y, z);
        }
        return super.canBlockStay(worldIn, x, y, z);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        final SubBlock sb = this.getSubBlock(worldIn.getBlockMetadata(x, y, z));
        if (sb != null) {
            return sb.getCollisionBoundingBoxFromPool(worldIn, x, y, z);
        }
        return super.getCollisionBoundingBoxFromPool(worldIn, x, y, z);
    }

    @Override
    public boolean canHarvestBlock(EntityPlayer player, int meta) {
        final SubBlock sb = this.getSubBlock(meta);
        if (sb != null) {
            return sb.canHarvestBlock(player, meta);
        }
        return super.canHarvestBlock(player, meta);
    }

    @Override
    public boolean canSilkHarvest(World world, EntityPlayer player, int x, int y, int z, int metadata) {
        final SubBlock sb = this.getSubBlock(metadata);
        if (sb != null) {
            return sb.canSilkHarvest(world, player, x, y, z, metadata);
        }
        return super.canSilkHarvest(world, player, x, y, z, metadata);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        final SubBlock sb = this.getSubBlock(worldIn.getBlockMetadata(x, y, z));
        if (sb != null) {
            return sb.getSelectedBoundingBoxFromPool(worldIn, x, y, z);
        }
        return super.getSelectedBoundingBoxFromPool(worldIn, x, y, z);
    }

    @Override
    public boolean canCollideCheck(int meta, boolean includeLiquid) {
        final SubBlock sb = this.getSubBlock(meta);
        if (sb != null) {
            return sb.canCollideCheck(meta, includeLiquid);
        }
        return super.canCollideCheck(meta, includeLiquid);
    }
}
