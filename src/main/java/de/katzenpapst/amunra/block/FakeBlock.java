package de.katzenpapst.amunra.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.tile.TileEntityShuttleDockFake;
import micdoodle8.mods.galacticraft.api.block.IPartialSealableBlock;
import micdoodle8.mods.galacticraft.api.vector.BlockVec3;
import micdoodle8.mods.galacticraft.core.tile.TileEntityMulti;

public class FakeBlock extends SubBlock implements IPartialSealableBlock, IMassiveBlock {

    public FakeBlock(final String name, final String texture) {
        super(name, texture);
        this.setHardness(1.0F);
        this.setStepSound(Block.soundTypeMetal);
        this.setResistance(1000000000000000.0F);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn) {
        return false;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public float getBlockHardness(World worldIn, int x, int y, int z) {
        if (worldIn.getTileEntity(x, y, z) instanceof TileEntityMulti tileEntity) {
            final BlockVec3 mainBlockPosition = tileEntity.mainBlockPosition;
            if (mainBlockPosition != null) {
                return mainBlockPosition.getBlock(worldIn).getBlockHardness(worldIn, x, y, z);
            }
        }
        return this.blockHardness;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public CreativeTabs getCreativeTabToDisplayOn() {
        return null;
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        if (worldIn.getTileEntity(x, y, z) instanceof TileEntityMulti tileMulti) {
            tileMulti.onBlockRemoval();
        }
        super.breakBlock(worldIn, x, y, z, blockBroken, meta);
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
            float subY, float subZ) {
        if (worldIn.getTileEntity(x, y, z) instanceof TileEntityMulti tileEntity) {
            return tileEntity.onBlockActivated(worldIn, x, y, z, player);
        }
        return super.onBlockActivated(worldIn, x, y, z, player, side, subX, subY, subZ);
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityShuttleDockFake();
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        if (world.getTileEntity(x, y, z) instanceof TileEntityMulti tileMulti) {
            final BlockVec3 mainBlockPosition = tileMulti.mainBlockPosition;

            if (mainBlockPosition != null) {
                final Block mainBlockID = world.getBlock(mainBlockPosition.x, mainBlockPosition.y, mainBlockPosition.z);

                if (Blocks.air != mainBlockID) {
                    return mainBlockID.getPickBlock(
                            target,
                            world,
                            mainBlockPosition.x,
                            mainBlockPosition.y,
                            mainBlockPosition.z,
                            player);
                }
            }
        }
        return null;
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        return this.getPickBlock(target, world, x, y, z, null);
    }

    @Override
    public boolean isSealed(final World world, final int x, final int y, final int z, final ForgeDirection direction) {
        return true;
    }

    @Override
    public float getMass(final World w, final int x, final int y, final int z, final int meta) {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer) {
        if (worldObj.getTileEntity(target.blockX, target.blockY, target.blockZ) instanceof TileEntityMulti tileMulti) {
            final BlockVec3 mainBlockPosition = tileMulti.mainBlockPosition;
            if (mainBlockPosition != null) {
                effectRenderer
                        .addBlockHitEffects(mainBlockPosition.x, mainBlockPosition.y, mainBlockPosition.z, target);
            }
        }
        return super.addHitEffects(worldObj, target, effectRenderer);
    }

}
