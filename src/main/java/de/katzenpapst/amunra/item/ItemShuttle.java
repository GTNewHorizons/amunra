package de.katzenpapst.amunra.item;

import java.util.List;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.entity.spaceship.EntityShuttle;
import micdoodle8.mods.galacticraft.api.entity.IRocketType.EnumRocketType;
import micdoodle8.mods.galacticraft.api.item.IHoldableItem;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.blocks.GCBlocks;
import micdoodle8.mods.galacticraft.core.proxy.ClientProxyCore;
import micdoodle8.mods.galacticraft.core.tile.TileEntityLandingPad;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class ItemShuttle extends Item implements IHoldableItem {

    public ItemShuttle(final String assetName) {
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
        this.setTextureName("arrow");
        this.setUnlocalizedName(assetName);
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return AmunRa.arTab;
    }

    public EntityShuttle spawnRocketEntity(final ItemStack stack, final World world, final double centerX, final double centerY,
            final double centerZ) {
        final EntityShuttle spaceship = new EntityShuttle(world, centerX, centerY, centerZ, stack.getItemDamage());

        spaceship.setPosition(spaceship.posX, spaceship.posY + spaceship.getOnPadYOffset(), spaceship.posZ);
        world.spawnEntityInWorld(spaceship);

        if (EntityShuttle.isPreFueled(stack.getItemDamage())) {
            spaceship.fuelTank.fill(new FluidStack(GalacticraftCore.fluidFuel, spaceship.fuelTank.getCapacity()), true);
        } else if (stack.hasTagCompound() && stack.getTagCompound().hasKey("RocketFuel")) {
            spaceship.fuelTank.fill(
                    new FluidStack(GalacticraftCore.fluidFuel, stack.getTagCompound().getInteger("RocketFuel")),
                    true);
        }

        // TODO inventory

        return spaceship;
    }

    /**
     * itemstack, player, world, x, y, z, side, hitX, hitY, hitZ
     */
    @Override
    public boolean onItemUse(ItemStack itemStack, final EntityPlayer player, final World world, final int x, final int y, final int z, final int side,
            final float hitX, final float hitY, final float hitZ) {
        boolean padFound = false;
        TileEntity tile = null;

        if (world.isRemote && player instanceof EntityPlayerSP) {
            // TODO FIX THIS, or figure out what it does
            ClientProxyCore.playerClientHandler.onBuild(8, (EntityPlayerSP) player);
            return false;
        }
        float centerX = -1;
        float centerY = -1;
        float centerZ = -1;

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                final net.minecraft.block.Block id = world.getBlock(x + i, y, z + j);
                final int meta = world.getBlockMetadata(x + i, y, z + j);

                if (id == GCBlocks.landingPadFull && meta == 0) {
                    padFound = true;
                    tile = world.getTileEntity(x + i, y, z + j);

                    centerX = x + i + 0.5F;
                    centerY = y + 0.4F;
                    centerZ = z + j + 0.5F;

                    break;
                }
            }

            if (padFound) break;
        }

        if (padFound) {
            // Check whether there is already a rocket on the pad
            if (tile instanceof TileEntityLandingPad) {
                if (((TileEntityLandingPad) tile).getDockedEntity() != null) return false;
            } else {
                return false;
            }

        } else {
            centerX = x + 0.5F;
            centerY = y + 0.4F;
            centerZ = z + 0.5F;

        }
        this.spawnRocketEntity(itemStack, world, centerX, centerY, centerZ);
        if (!player.capabilities.isCreativeMode) {
            itemStack.stackSize--;

            if (itemStack.stackSize <= 0) {
                itemStack = null;
            }
        }
        return true;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void getSubItems(final Item par1, final CreativeTabs par2CreativeTabs, final List par3List) {
        // par3List.add(new ItemStack(par1, 1, 0));

        for (int numTanks = 0; numTanks <= 3; numTanks++) {
            for (int numChests = 0; numChests <= 3; numChests++) {
                if (numChests + numTanks > 3) {
                    continue; // do it later
                }
                final int dmg = numChests | numTanks << 2;
                par3List.add(new ItemStack(par1, 1, dmg));
            }
        }

        // lastly
        par3List.add(new ItemStack(par1, 1, 3 | 3 << 2));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(final ItemStack par1ItemStack) {
        return ClientProxyCore.galacticraftItem;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final EntityPlayer player, final List list, final boolean b) {
        final int dmg = stack.getItemDamage();
        final EnumRocketType type = EntityShuttle.getRocketTypeFromDamage(dmg);

        if (!type.getTooltip().isEmpty()) {
            list.add(type.getTooltip());
        }

        final int fuelTotal = EntityShuttle.getFuelCapacityFromDamage(dmg);
        if (EntityShuttle.isPreFueled(dmg)) {
            list.add(GCCoreUtil.translate("gui.message.fuel.name") + ": " + fuelTotal + " / " + fuelTotal);
            list.add(EnumColor.RED + "\u00a7o" + GCCoreUtil.translate("gui.creativeOnly.desc"));
        } else {
            int fuelContained = 0;
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("RocketFuel")) {
                fuelContained = stack.getTagCompound().getInteger("RocketFuel");
            }
            list.add(GCCoreUtil.translate("gui.message.fuel.name") + ": " + fuelContained + " / " + fuelTotal);
        }
    }

    @Override
    public boolean shouldHoldLeftHandUp(final EntityPlayer player) {
        return true;
    }

    @Override
    public boolean shouldHoldRightHandUp(final EntityPlayer player) {
        return true;
    }

    @Override
    public boolean shouldCrouch(final EntityPlayer player) {
        return true;
    }
}
