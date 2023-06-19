package de.katzenpapst.amunra.item;

import java.util.List;

import net.minecraft.block.Block;
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

    public EntityShuttle spawnRocketEntity(final ItemStack stack, final World world, final double centerX,
            final double centerY, final double centerZ) {
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

    @Override
    public boolean onItemUse(ItemStack p_77648_1_, EntityPlayer p_77648_2_, World p_77648_3_, int p_77648_4_,
            int p_77648_5_, int p_77648_6_, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
        boolean padFound = false;
        TileEntity tile = null;

        if (p_77648_3_.isRemote && p_77648_2_ instanceof EntityPlayerSP playerSP) {
            // TODO FIX THIS, or figure out what it does
            ClientProxyCore.playerClientHandler.onBuild(8, playerSP);
            return false;
        }
        float centerX = -1;
        float centerY = -1;
        float centerZ = -1;

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                final Block id = p_77648_3_.getBlock(p_77648_4_ + i, p_77648_5_, p_77648_6_ + j);
                final int meta = p_77648_3_.getBlockMetadata(p_77648_4_ + i, p_77648_5_, p_77648_6_ + j);

                if (id == GCBlocks.landingPadFull && meta == 0) {
                    padFound = true;
                    tile = p_77648_3_.getTileEntity(p_77648_4_ + i, p_77648_5_, p_77648_6_ + j);

                    centerX = p_77648_4_ + i + 0.5F;
                    centerY = p_77648_5_ + 0.4F;
                    centerZ = p_77648_6_ + j + 0.5F;

                    break;
                }
            }

            if (padFound) break;
        }

        if (padFound) {
            // Check whether there is already a rocket on the pad
            if (!(tile instanceof TileEntityLandingPad tilePad)) {
                return false;
            }
            if (tilePad.getDockedEntity() != null) return false;

        } else {
            centerX = p_77648_4_ + 0.5F;
            centerY = p_77648_5_ + 0.4F;
            centerZ = p_77648_6_ + 0.5F;

        }
        this.spawnRocketEntity(p_77648_1_, p_77648_3_, centerX, centerY, centerZ);
        if (!p_77648_2_.capabilities.isCreativeMode) {
            p_77648_1_.stackSize--;

            if (p_77648_1_.stackSize <= 0) {
                p_77648_1_ = null;
            }
        }
        return true;
    }

    @Override
    public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List<ItemStack> p_150895_3_) {
        // par3List.add(new ItemStack(par1, 1, 0));

        for (int numTanks = 0; numTanks <= 3; numTanks++) {
            for (int numChests = 0; numChests <= 3; numChests++) {
                if (numChests + numTanks > 3) {
                    continue; // do it later
                }
                final int dmg = numChests | numTanks << 2;
                p_150895_3_.add(new ItemStack(p_150895_1_, 1, dmg));
            }
        }

        // lastly
        p_150895_3_.add(new ItemStack(p_150895_1_, 1, 3 | 3 << 2));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack p_77613_1_) {
        return ClientProxyCore.galacticraftItem;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List<String> p_77624_3_,
            boolean p_77624_4_) {
        final int dmg = p_77624_1_.getItemDamage();
        final EnumRocketType type = EntityShuttle.getRocketTypeFromDamage(dmg);

        if (!type.getTooltip().isEmpty()) {
            p_77624_3_.add(type.getTooltip());
        }

        final int fuelTotal = EntityShuttle.getFuelCapacityFromDamage(dmg);
        if (EntityShuttle.isPreFueled(dmg)) {
            p_77624_3_.add(GCCoreUtil.translate("gui.message.fuel.name") + ": " + fuelTotal + " / " + fuelTotal);
            p_77624_3_.add(EnumColor.RED + "§o" + GCCoreUtil.translate("gui.creativeOnly.desc"));
        } else {
            int fuelContained = 0;
            if (p_77624_1_.hasTagCompound() && p_77624_1_.getTagCompound().hasKey("RocketFuel")) {
                fuelContained = p_77624_1_.getTagCompound().getInteger("RocketFuel");
            }
            p_77624_3_.add(GCCoreUtil.translate("gui.message.fuel.name") + ": " + fuelContained + " / " + fuelTotal);
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
