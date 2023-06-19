package de.katzenpapst.amunra.block;

import java.util.Random;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.tile.TileEntityARChest;
import micdoodle8.mods.galacticraft.core.items.ItemBlockDesc;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class BlockARChest extends BlockContainer
        implements ITileEntityProvider, ItemBlockDesc.IBlockShiftDesc, IMassiveBlock {

    protected final Random random = new Random();

    protected float mass = 1.0F;

    protected final ResourceLocation smallChestTexture;
    protected final ResourceLocation bigChestTexture;
    protected final String fallbackTexture;

    protected boolean canDoublechest = true;

    protected String shiftDescription = null;

    public BlockARChest(final Material material, final String blockName, final ResourceLocation smallChestTexture,
            final ResourceLocation bigChestTexture, final String fallbackTexture) {
        super(material);

        this.setHardness(2.5F);
        this.setResistance(100.0F);
        this.setStepSound(Block.soundTypeStone);
        this.setBlockName(blockName);

        this.smallChestTexture = smallChestTexture;
        this.bigChestTexture = bigChestTexture;
        this.fallbackTexture = fallbackTexture;
    }

    public BlockARChest(final Material material, final String blockName, final ResourceLocation smallChestTexture,
            final String fallbackTexture) {
        super(material);

        this.setHardness(2.5F);
        this.setResistance(100.0F);
        this.setStepSound(Block.soundTypeStone);
        this.setBlockName(blockName);

        this.smallChestTexture = smallChestTexture;
        this.bigChestTexture = null;
        this.fallbackTexture = fallbackTexture;
    }

    public ResourceLocation getSmallTexture() {
        return this.smallChestTexture;
    }

    public ResourceLocation getLargeTexture() {
        return this.bigChestTexture;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.blockIcon = reg.registerIcon(this.fallbackTexture);
    }

    public void setShiftDescription(final String str) {
        this.shiftDescription = str;
    }

    @Override
    public String getShiftDescription(int meta) {
        if (this.shiftDescription != null) {
            return GCCoreUtil.translate(this.shiftDescription);
        }
        return null;
    }

    @Override
    public boolean showDescription(int meta) {
        return this.shiftDescription != null;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityARChest();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public CreativeTabs getCreativeTabToDisplayOn() {
        return AmunRa.arTab;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return AmunRa.chestRenderId;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, int x, int y, int z) {
        if (!this.canDoublechest) {
            this.setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
            return;
        }
        if (this.isSameBlock(worldIn, x, y, z - 1)) {
            this.setBlockBounds(0.0625F, 0.0F, 0.0F, 0.9375F, 0.875F, 0.9375F);
        } else if (this.isSameBlock(worldIn, x, y, z + 1)) {
            this.setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 1.0F);
        } else if (this.isSameBlock(worldIn, x - 1, y, z)) {
            this.setBlockBounds(0.0F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
        } else if (this.isSameBlock(worldIn, x + 1, y, z)) {
            this.setBlockBounds(0.0625F, 0.0F, 0.0625F, 1.0F, 0.875F, 0.9375F);
        } else {
            this.setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
        }
    }

    @Override
    public void onBlockAdded(World worldIn, int x, int y, int z) {
        super.onBlockAdded(worldIn, x, y, z);
        if (!this.canDoublechest) {
            return;
        }

        this.unifyAdjacentChests(worldIn, x, y, z);

        if (this.isSameBlock(worldIn, x, y, z - 1)) {
            this.unifyAdjacentChests(worldIn, x, y, z - 1);
        } else if (this.isSameBlock(worldIn, x, y, z + 1)) {
            this.unifyAdjacentChests(worldIn, x, y, z + 1);
        } else if (this.isSameBlock(worldIn, x - 1, y, z)) {
            this.unifyAdjacentChests(worldIn, x - 1, y, z);
        } else if (this.isSameBlock(worldIn, x + 1, y, z)) {
            this.unifyAdjacentChests(worldIn, x + 1, y, z);
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        final int userRotation = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

        int meta = switch (userRotation) {
            case 0 -> 2;
            case 1 -> 5;
            case 2 -> 3;
            case 3 -> 4;
            default -> 0;
        };

        if (!this.canDoublechest) {
            worldIn.setBlockMetadataWithNotify(x, y, z, meta, 3);
            return;
        }

        final boolean zNegSame = this.isSameBlock(worldIn, x, y, z - 1);
        final boolean zPosSame = this.isSameBlock(worldIn, x, y, z + 1);
        final boolean xNegSame = this.isSameBlock(worldIn, x - 1, y, z);
        final boolean xPosSame = this.isSameBlock(worldIn, x + 1, y, z);

        if (!zNegSame && !zPosSame && !xNegSame && !xPosSame) {
            worldIn.setBlockMetadataWithNotify(x, y, z, meta, 3);
        } else {
            if ((zNegSame || zPosSame) && (meta == 4 || meta == 5)) {
                if (zNegSame) {
                    worldIn.setBlockMetadataWithNotify(x, y, z - 1, meta, 3);
                } else {
                    worldIn.setBlockMetadataWithNotify(x, y, z + 1, meta, 3);
                }

                worldIn.setBlockMetadataWithNotify(x, y, z, meta, 3);
            }

            if ((xNegSame || xPosSame) && (meta == 2 || meta == 3)) {
                if (xNegSame) {
                    worldIn.setBlockMetadataWithNotify(x - 1, y, z, meta, 3);
                } else {
                    worldIn.setBlockMetadataWithNotify(x + 1, y, z, meta, 3);
                }

                worldIn.setBlockMetadataWithNotify(x, y, z, meta, 3);
            }
        }
    }

    public void unifyAdjacentChests(final World world, final int x, final int y, final int z) {
        if (!this.canDoublechest || world.isRemote) {
            return;
        }

        final boolean zNegSame = this.isSameBlock(world, x, y, z - 1);
        final boolean zPosSame = this.isSameBlock(world, x, y, z + 1);
        final boolean xNegSame = this.isSameBlock(world, x - 1, y, z);
        final boolean xPosSame = this.isSameBlock(world, x + 1, y, z);

        final Block nZNeg = world.getBlock(x, y, z - 1);
        final Block nZPos = world.getBlock(x, y, z + 1);
        final Block nXNeg = world.getBlock(x - 1, y, z);
        final Block nXPos = world.getBlock(x + 1, y, z);
        Block otherNeighbour1;
        Block otherNeighbour2;
        byte meta;
        int otherMeta;

        if (!zNegSame && !zPosSame) {
            if (!xNegSame && !xPosSame) {
                meta = 3;

                if (nZNeg.func_149730_j() && !nZPos.func_149730_j()) {
                    meta = 3;
                }

                if (nZPos.func_149730_j() && !nZNeg.func_149730_j()) {
                    meta = 2;
                }

                if (nXNeg.func_149730_j() && !nXPos.func_149730_j()) {
                    meta = 5;
                }

                if (nXPos.func_149730_j() && !nXNeg.func_149730_j()) {
                    meta = 4;
                }
            } else {
                otherNeighbour1 = world.getBlock(xNegSame ? x - 1 : x + 1, y, z - 1);
                otherNeighbour2 = world.getBlock(xNegSame ? x - 1 : x + 1, y, z + 1);
                meta = 3;
                if (xNegSame) {
                    otherMeta = world.getBlockMetadata(x - 1, y, z);
                } else {
                    otherMeta = world.getBlockMetadata(x + 1, y, z);
                }

                if (otherMeta == 2) {
                    meta = 2;
                }

                if ((nZNeg.func_149730_j() || otherNeighbour1.func_149730_j()) && !nZPos.func_149730_j()
                        && !otherNeighbour2.func_149730_j()) {
                    meta = 3;
                }

                if ((nZPos.func_149730_j() || otherNeighbour2.func_149730_j()) && !nZNeg.func_149730_j()
                        && !otherNeighbour1.func_149730_j()) {
                    meta = 2;
                }
            }
        } else {
            otherNeighbour1 = world.getBlock(x - 1, y, nZNeg == this ? z - 1 : z + 1);
            otherNeighbour2 = world.getBlock(x + 1, y, nZNeg == this ? z - 1 : z + 1);
            meta = 5;
            if (nZNeg == this) {
                otherMeta = world.getBlockMetadata(x, y, z - 1);
            } else {
                otherMeta = world.getBlockMetadata(x, y, z + 1);
            }

            if (otherMeta == 4) {
                meta = 4;
            }

            if ((nXNeg.func_149730_j() || otherNeighbour1.func_149730_j()) && !nXPos.func_149730_j()
                    && !otherNeighbour2.func_149730_j()) {
                meta = 5;
            }

            if ((nXPos.func_149730_j() || otherNeighbour2.func_149730_j()) && !nXNeg.func_149730_j()
                    && !otherNeighbour1.func_149730_j()) {
                meta = 4;
            }
        }

        world.setBlockMetadataWithNotify(x, y, z, meta, 3);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, int x, int y, int z) {
        if (!this.canDoublechest) {
            return super.canPlaceBlockAt(worldIn, x, y, z);
        }

        final long neighborChestCount = Stream.of(
                this.isSameBlock(worldIn, x - 1, y, z),
                this.isSameBlock(worldIn, x + 1, y, z),
                this.isSameBlock(worldIn, x, y, z - 1),
                this.isSameBlock(worldIn, x, y, z + 1)).filter(c -> c).count();

        final boolean neighboursHaveNeighborChest = this.isThereANeighborChest(worldIn, x - 1, y, z)
                || this.isThereANeighborChest(worldIn, x + 1, y, z)
                || this.isThereANeighborChest(worldIn, x, y, z - 1)
                || this.isThereANeighborChest(worldIn, x, y, z + 1);

        return neighborChestCount < 2 && !neighboursHaveNeighborChest;
    }

    /**
     * Checks the neighbor blocks to see if there is a chest there. Args: world, x, y, z
     */
    private boolean isThereANeighborChest(final World world, final int x, final int y, final int z) {
        if (!this.canDoublechest || !this.isSameBlock(world, x, y, z)) {
            return false;
        }

        return this.isSameBlock(world, x - 1, y, z) || this.isSameBlock(world, x + 1, y, z)
                || this.isSameBlock(world, x, y, z - 1)
                || this.isSameBlock(world, x, y, z + 1);
    }

    @Override
    public void onNeighborBlockChange(World worldIn, int x, int y, int z, Block neighbor) {
        super.onNeighborBlockChange(worldIn, x, y, z, neighbor);

        final TileEntityARChest tileEntity = (TileEntityARChest) worldIn.getTileEntity(x, y, z);

        if (tileEntity != null) {
            tileEntity.updateContainingBlockInfo();
        }
    }

    protected boolean isSameBlock(final IBlockAccess world, final int x, final int y, final int z) {
        return world.getBlock(x, y, z) == this;
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        final TileEntityARChest tileEntity = (TileEntityARChest) worldIn.getTileEntity(x, y, z);

        if (tileEntity != null) {
            for (int i = 0; i < tileEntity.getSizeInventory(); ++i) {
                final ItemStack stack = tileEntity.getStackInSlot(i);

                if (stack != null) {
                    final float rand1 = this.random.nextFloat() * 0.8F + 0.1F;
                    final float rand2 = this.random.nextFloat() * 0.8F + 0.1F;
                    EntityItem itemEntity;

                    for (final float randOffset = this.random.nextFloat() * 0.8F + 0.1F; stack.stackSize > 0; worldIn
                            .spawnEntityInWorld(itemEntity)) {
                        int droppedStackSize = this.random.nextInt(21) + 10;

                        if (droppedStackSize > stack.stackSize) {
                            droppedStackSize = stack.stackSize;
                        }

                        stack.stackSize -= droppedStackSize;
                        itemEntity = new EntityItem(
                                worldIn,
                                x + rand1,
                                y + rand2,
                                z + randOffset,
                                new ItemStack(stack.getItem(), droppedStackSize, stack.getItemDamage()));
                        final float yOffset = 0.05F;
                        itemEntity.motionX = (float) this.random.nextGaussian() * yOffset;
                        itemEntity.motionY = (float) this.random.nextGaussian() * yOffset + 0.2F;
                        itemEntity.motionZ = (float) this.random.nextGaussian() * yOffset;

                        if (stack.hasTagCompound()) {
                            itemEntity.getEntityItem().setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
                        }
                    }
                }
            }
        }

        super.breakBlock(worldIn, x, y, z, blockBroken, meta);
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
            float subY, float subZ) {
        Object tileEntity = worldIn.getTileEntity(x, y, z);

        if (tileEntity == null || worldIn.isSideSolid(x, y + 1, z, ForgeDirection.DOWN)
                || TileEntityARChest.isOcelotBlockingChest(worldIn, x, y, z)) {
            return true;
        }
        if (this.canDoublechest) {
            if (worldIn.getBlock(x - 1, y, z) == this && (worldIn.isSideSolid(x - 1, y + 1, z, ForgeDirection.DOWN)
                    || TileEntityARChest.isOcelotBlockingChest(worldIn, x - 1, y, z))) {
                return true;
            }
            if (worldIn.getBlock(x + 1, y, z) == this && (worldIn.isSideSolid(x + 1, y + 1, z, ForgeDirection.DOWN)
                    || TileEntityARChest.isOcelotBlockingChest(worldIn, x + 1, y, z))) {
                return true;
            }
            if (worldIn.getBlock(x, y, z - 1) == this && (worldIn.isSideSolid(x, y + 1, z - 1, ForgeDirection.DOWN)
                    || TileEntityARChest.isOcelotBlockingChest(worldIn, x, y, z - 1))) {
                return true;
            }
            if (worldIn.getBlock(x, y, z + 1) == this && (worldIn.isSideSolid(x, y + 1, z + 1, ForgeDirection.DOWN)
                    || TileEntityARChest.isOcelotBlockingChest(worldIn, x, y, z + 1))) {
                return true;
            }

            if (this.isSameBlock(worldIn, x - 1, y, z)) {
                tileEntity = new InventoryLargeChest(
                        "container.chestDouble",
                        (TileEntityARChest) worldIn.getTileEntity(x - 1, y, z),
                        (IInventory) tileEntity);
            }

            if (this.isSameBlock(worldIn, x + 1, y, z)) {
                tileEntity = new InventoryLargeChest(
                        "container.chestDouble",
                        (IInventory) tileEntity,
                        (TileEntityARChest) worldIn.getTileEntity(x + 1, y, z));
            }

            if (this.isSameBlock(worldIn, x, y, z - 1)) {
                tileEntity = new InventoryLargeChest(
                        "container.chestDouble",
                        (TileEntityARChest) worldIn.getTileEntity(x, y, z - 1),
                        (IInventory) tileEntity);
            }

            if (this.isSameBlock(worldIn, x, y, z + 1)) {
                tileEntity = new InventoryLargeChest(
                        "container.chestDouble",
                        (IInventory) tileEntity,
                        (TileEntityARChest) worldIn.getTileEntity(x, y, z + 1));
            }
        }

        if (worldIn.isRemote) {
            return true;
        }
        player.displayGUIChest((IInventory) tileEntity);
        return true;
    }

    public void setMass(final float mass) {
        this.mass = mass;
    }

    @Override
    public float getMass(final World w, final int x, final int y, final int z, final int meta) {
        return this.mass;
    }
}
