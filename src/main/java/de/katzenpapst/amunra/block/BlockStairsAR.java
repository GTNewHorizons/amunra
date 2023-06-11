package de.katzenpapst.amunra.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.MapColor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.helper.BlockMassHelper;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class BlockStairsAR extends BlockStairs implements IMassiveBlock {

    private final BlockMetaPair sourceBlock;

    public BlockStairsAR(final BlockMetaPair sourceBlock) {
        // protected constructor? WTF IS THIS SHIT?!!?!
        super(sourceBlock.getBlock(), sourceBlock.getMetadata());
        this.sourceBlock = sourceBlock;
    }

    @Override
    public String getUnlocalizedName() {
        final IMetaBlock mBlock = (IMetaBlock) this.sourceBlock.getBlock();
        if (mBlock != null) {
            return "tile." + mBlock.getSubBlock(this.sourceBlock.getMetadata()).getUnlocalizedName() + ".stairs";
        }
        return "tile." + this.sourceBlock.getBlock().getUnlocalizedName() + ".stairs";
    }

    public void register() {
        GameRegistry.registerBlock(this, ItemBlock.class, this.getUnlocalizedName());

        this.setHarvestLevel(
                this.sourceBlock.getBlock().getHarvestTool(this.sourceBlock.getMetadata()),
                this.sourceBlock.getBlock().getHarvestLevel(this.sourceBlock.getMetadata()));

    }

    @SideOnly(Side.CLIENT)
    @Override
    public CreativeTabs getCreativeTabToDisplayOn() {
        return AmunRa.arTab;
    }

    @Override
    public float getMass(final World w, final int x, final int y, final int z, final int meta) {
        // 4/6 = 2/3, because stairs
        return BlockMassHelper.getBlockMass(w, this.sourceBlock.getBlock(), this.sourceBlock.getMetadata(), x, y, z)
                * 2.0F
                / 3.0F;
    }

    @Override
    public float getBlockHardness(World worldIn, int x, int y, int z) {
        return this.getSourceBlock().getBlockHardness(worldIn, x, y, z);
    }

    public Block getSourceBlock() {
        Block mainBlock = this.sourceBlock.getBlock();
        if (mainBlock instanceof IMetaBlock) {
            mainBlock = ((IMetaBlock) this.sourceBlock.getBlock()).getSubBlock(this.sourceBlock.getMetadata());
        }
        return mainBlock;
    }

    @Override
    public String getHarvestTool(int metadata) {
        return this.sourceBlock.getBlock().getHarvestTool(metadata);
    }

    @Override
    public int getHarvestLevel(int metadata) {
        return this.sourceBlock.getBlock().getHarvestLevel(metadata);
    }

    @Override
    public boolean isToolEffective(String type, int metadata) {
        return this.getHarvestTool(metadata).equals(type);
    }

    @Override
    public float getExplosionResistance(Entity par1Entity, World world, int x, int y, int z, double explosionX,
            double explosionY, double explosionZ) {
        return this.getSourceBlock()
                .getExplosionResistance(par1Entity, world, x, y, z, explosionX, explosionY, explosionZ);
    }

    /// block-dependent functions

    @Override
    public void onBlockClicked(World worldIn, int x, int y, int z, EntityPlayer player) {
        // I don't see any reason for this to be proxied to the source block, but meh
        this.getSourceBlock().onBlockClicked(worldIn, x, y, z, player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World worldIn, int x, int y, int z, Random random) {
        this.getSourceBlock().randomDisplayTick(worldIn, x, y, z, random);
    }

    @Override
    public void onBlockDestroyedByPlayer(World worldIn, int x, int y, int z, int meta) {
        this.getSourceBlock().onBlockDestroyedByPlayer(worldIn, x, y, z, meta);
    }

    @Override
    public float getExplosionResistance(Entity exploder) {
        return this.getSourceBlock().getExplosionResistance(exploder);
    }

    @Override
    public int tickRate(World worldIn) {
        return this.getSourceBlock().tickRate(worldIn);
    }

    @Override
    public void velocityToAddToEntity(World worldIn, int x, int y, int z, Entity entityIn, Vec3 velocity) {
        this.getSourceBlock().velocityToAddToEntity(worldIn, x, y, z, entityIn, velocity);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getMixedBrightnessForBlock(IBlockAccess worldIn, int x, int y, int z) {
        return this.getSourceBlock().getMixedBrightnessForBlock(worldIn, x, y, z);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderBlockPass() {
        return this.getSourceBlock().getRenderBlockPass();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.sourceBlock.getBlock().getIcon(side, this.sourceBlock.getMetadata());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        return this.getSourceBlock().getSelectedBoundingBoxFromPool(worldIn, x, y, z);
    }

    @Override
    public boolean isCollidable() {
        return this.getSourceBlock().isCollidable();
    }

    @Override
    public boolean canCollideCheck(int meta, boolean includeLiquid) {
        return this.getSourceBlock().canCollideCheck(meta, includeLiquid);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, int x, int y, int z) {
        return this.getSourceBlock().canPlaceBlockAt(worldIn, x, y, z);
    }

    @Override
    public void onBlockAdded(World worldIn, int x, int y, int z) {
        this.onNeighborBlockChange(worldIn, x, y, z, Blocks.air);
        this.getSourceBlock().onBlockAdded(worldIn, x, y, z);
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        this.getSourceBlock().breakBlock(worldIn, x, y, z, blockBroken, meta);
    }

    @Override
    public void onEntityWalking(World worldIn, int x, int y, int z, Entity entityIn) {
        this.getSourceBlock().onEntityWalking(worldIn, x, y, z, entityIn);
    }

    @Override
    public void updateTick(World worldIn, int x, int y, int z, Random random) {
        this.getSourceBlock().updateTick(worldIn, x, y, z, random);
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
            float subY, float subZ) {
        return this.getSourceBlock().onBlockActivated(worldIn, x, y, z, player, 0, 0.0F, 0.0F, 0.0F);
    }

    @Override
    public void onBlockDestroyedByExplosion(World worldIn, int x, int y, int z, Explosion explosionIn) {
        this.getSourceBlock().onBlockDestroyedByExplosion(worldIn, x, y, z, explosionIn);
    }

    @Override
    public MapColor getMapColor(int meta) {
        return this.sourceBlock.getBlock().getMapColor(this.sourceBlock.getMetadata());
    }
}
