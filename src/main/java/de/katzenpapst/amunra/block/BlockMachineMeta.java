package de.katzenpapst.amunra.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.item.ItemBlockMulti;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.core.blocks.BlockTileGC;
import micdoodle8.mods.galacticraft.core.energy.tile.TileBaseUniversalElectrical;
import micdoodle8.mods.galacticraft.core.items.ItemBlockDesc;

public class BlockMachineMeta extends BlockTileGC implements ItemBlockDesc.IBlockShiftDesc, IMetaBlock {

    protected final SubBlockMachine[] subBlocksArray;
    protected final Map<String, Integer> nameMetaMap = new HashMap<>();
    protected String blockNameFU;

    public BlockMachineMeta(final String name, final Material material) {
        super(material);
        this.blockNameFU = name;
        this.subBlocksArray = new SubBlockMachine[4];
        this.setBlockName(this.blockNameFU);
    }

    protected BlockMachineMeta(final String name, final Material material, final int numSubBlocks) {
        super(material);
        this.blockNameFU = name;
        this.subBlocksArray = new SubBlockMachine[numSubBlocks];
        this.setBlockName(this.blockNameFU);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        final SubBlock leBlock = this.getSubBlock(meta);
        if (leBlock == null) {
            return null;
        }
        return leBlock.getIcon(side, meta);
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

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        for (final SubBlock sb : this.subBlocksArray) {
            if (sb != null) {
                sb.registerBlockIcons(reg);
            }
        }
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
    public String getShiftDescription(final int meta) {
        if (this.getSubBlock(meta) instanceof SubBlockMachine sbm) {
            return sbm.getShiftDescription(0);
        }
        return "";
    }

    @Override
    public boolean showDescription(final int meta) {
        if (this.getSubBlock(meta) instanceof SubBlockMachine sbm) {
            return sbm.showDescription(0);
        }
        return false;
    }

    @Override
    public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX,
            float hitY, float hitZ) {
        if (this.getSubBlock(world.getBlockMetadata(x, y, z)) instanceof SubBlockMachine sbm) {
            return sbm.onMachineActivated(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ);
        }
        return super.onMachineActivated(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ);
    }

    @Override
    public BlockMetaPair addSubBlock(int meta, SubBlock sb) {
        if (!(sb instanceof SubBlockMachine sbm)) {
            throw new IllegalArgumentException("BlockMachineMeta can only accept SubBlockMachine");
        }
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
        this.subBlocksArray[meta] = sbm;
        return new BlockMetaPair(this, (byte) meta);
    }

    @Override
    public int getMetaByName(final String name) {
        if (this.nameMetaMap.containsKey(name)) {
            return this.nameMetaMap.get(name);
        }
        throw new IllegalArgumentException("Subblock " + name + " doesn't exist in " + this.blockNameFU);
    }

    @Override
    public SubBlock getSubBlock(final int meta) {
        return this.subBlocksArray[this.getDistinctionMeta(meta)];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public CreativeTabs getCreativeTabToDisplayOn() {
        return AmunRa.arTab;
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
        GameRegistry.registerBlock(this, ItemBlockMulti.class, this.getUnlocalizedName());

        for (int i = 0; i < this.subBlocksArray.length; i++) {
            final SubBlock sb = this.subBlocksArray[i];
            if (sb != null) {
                this.setHarvestLevel(sb.getHarvestTool(0), sb.getHarvestLevel(0), i);
            }
        }
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
    public TileEntity createTileEntity(World world, int metadata) {
        final SubBlock sb = this.getSubBlock(metadata);
        if (sb != null) {
            return sb.createTileEntity(world, metadata);
        }
        return super.createTileEntity(world, metadata);
    }

    @Override
    public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX,
            float hitY, float hitZ) {
        final int metadata = world.getBlockMetadata(x, y, z);
        final SubBlock sb = this.getSubBlock(metadata);
        if (sb == null || !sb.canBeMoved(world, x, y, z)) {
            return false;
        }

        final int original = this.getRotationMeta(metadata);
        int change = switch (original) {
            case 0 -> 3;
            case 1 -> 2;
            case 3 -> 1;
            default -> 0;
        };

        change = this.addRotationMeta(this.getDistinctionMeta(metadata), change);

        final TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileBaseUniversalElectrical tileUE) {
            tileUE.updateFacing();
        }

        world.setBlockMetadataWithNotify(x, y, z, change, 3);
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        final int metadata = worldIn.getBlockMetadata(x, y, z);
        final int dist = this.getDistinctionMeta(metadata);
        final int angle = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        int change = switch (angle) {
            case 0 -> 1;
            case 1 -> 2;
            case 3 -> 3;
            default -> 0;
        };

        change = this.addRotationMeta(dist, change);

        worldIn.setBlockMetadataWithNotify(x, y, z, change, 3);

        final TileEntity tile = worldIn.getTileEntity(x, y, z);

        if (tile instanceof TileBaseUniversalElectrical tileUE) {
            tileUE.updateFacing();
        }

        final SubBlock sb = this.getSubBlock(dist);
        if (sb != null) {
            sb.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);
        }
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

    @Override
    public boolean getBlocksMovement(IBlockAccess worldIn, int x, int y, int z) {
        final SubBlock sb = this.getSubBlock(worldIn.getBlockMetadata(x, y, z));
        if (sb != null) {
            return sb.getBlocksMovement(worldIn, x, y, z);
        }
        return super.getBlocksMovement(worldIn, x, y, z);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float hitX,
            float hitY, float hitZ) {
        final int meta = world.getBlockMetadata(x, y, z);

        // Check if the player is holding a wrench or an electric item. If so, call the wrench event.
        if (this.isUsableWrench(entityPlayer, entityPlayer.inventory.getCurrentItem(), x, y, z)) {
            this.damageWrench(entityPlayer, entityPlayer.inventory.getCurrentItem(), x, y, z);

            if ((entityPlayer.isSneaking()
                    && this.onSneakUseWrench(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ))
                    || this.onUseWrench(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ)) {
                return true;
            }
        }
        // handle the other stuff in the subblock
        if (this.getSubBlock(meta) instanceof SubBlockMachine sb) {
            if (entityPlayer.isSneaking()
                    && sb.onSneakMachineActivated(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ)) {
                return true;
            }
            return sb.onMachineActivated(world, x, y, z, entityPlayer, side, hitX, hitY, hitZ);
        }
        return false;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        final SubBlock sb = this.getSubBlock(metadata);
        if (sb != null) {
            return sb.hasTileEntity(metadata);
        }
        return super.hasTileEntity(metadata);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        final SubBlock sb = this.getSubBlock(metadata);
        if (sb != null) {
            sb.breakBlock(world, x, y, z, block, metadata);
        }
        super.breakBlock(world, x, y, z, block, metadata);
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
    public int onBlockPlaced(World worldIn, int x, int y, int z, int side, float subX, float subY, float subZ,
            int meta) {
        final SubBlock sb = this.getSubBlock(meta);
        if (sb != null) {
            return sb.onBlockPlaced(worldIn, x, y, z, side, subX, subY, subZ, meta);
        }
        return super.onBlockPlaced(worldIn, x, y, z, side, subX, subY, subZ, meta);
    }

    @Override
    public int getNumPossibleSubBlocks() {
        return this.subBlocksArray.length;
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        final SubBlock sb = this.getSubBlock(world.getBlockMetadata(x, y, z));
        if (sb != null) {
            return sb.removedByPlayer(world, player, x, y, z, willHarvest);
        }
        return super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
        final SubBlock sb = this.getSubBlock(world.getBlockMetadata(x, y, z));
        if (sb != null) {
            return sb.removedByPlayer(world, player, x, y, z);
        }
        return super.removedByPlayer(world, player, x, y, z);
    }

    @Override
    public boolean canReplace(World worldIn, int x, int y, int z, int side, ItemStack itemIn) {
        if (itemIn != null) {
            final SubBlock sb = this.getSubBlock(itemIn.getItemDamage());
            if (sb != null) {
                return sb.canReplace(worldIn, x, y, z, side, itemIn);
            }
        }
        return super.canReplace(worldIn, x, y, z, side, itemIn);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World worldIn, int x, int y, int z, Random random) {
        final SubBlock sb = this.getSubBlock(worldIn.getBlockMetadata(x, y, z));
        if (sb != null) {
            sb.randomDisplayTick(worldIn, x, y, z, random);
        }
    }
}
