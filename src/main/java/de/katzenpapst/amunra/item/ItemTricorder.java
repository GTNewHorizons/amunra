package de.katzenpapst.amunra.item;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import de.katzenpapst.amunra.helper.GuiHelper;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldProviderSpace;
import micdoodle8.mods.galacticraft.api.world.IAtmosphericGas;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.api.world.ISolarLevel;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;

public class ItemTricorder extends SubItem {

    public ItemTricorder(final String name, final String assetName) {
        super(name, assetName);
    }

    public ItemTricorder(final String name, final String assetName, final String info) {
        super(name, assetName, info);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
        if (!worldIn.isRemote) {
            return itemStackIn;
        }
        final DecimalFormat twoDForm = new DecimalFormat("#.##");

        float gravity = 1;
        float thermalLevel = 0;
        double solarLevel = 1;
        int dayLength = -1;
        final List<String> atmospheres = new ArrayList<>();

        if (worldIn.provider instanceof IGalacticraftWorldProvider) {
            gravity = ((IGalacticraftWorldProvider) worldIn.provider).getGravity();
            // convert
            gravity = 1.0F - gravity / 0.08F;
            thermalLevel = ((IGalacticraftWorldProvider) worldIn.provider).getThermalLevelModifier();
        }

        if (worldIn.provider instanceof ISolarLevel) {
            solarLevel = ((ISolarLevel) worldIn.provider).getSolarEnergyMultiplier();
        }

        if (worldIn.provider instanceof WorldProviderSpace) {
            dayLength = (int) ((WorldProviderSpace) worldIn.provider).getDayLength();
            final CelestialBody curBody = ((WorldProviderSpace) worldIn.provider).getCelestialBody();
            for (final IAtmosphericGas gas : curBody.atmosphere) {
                atmospheres.add(GuiHelper.getGasName(gas));
            }
        } else if (worldIn.provider.dimensionId == 0) {
            dayLength = 24000;
            for (final IAtmosphericGas gas : GalacticraftCore.planetOverworld.atmosphere) {
                atmospheres.add(GuiHelper.getGasName(gas));
            }
        }

        gravity *= 9.81F;

        // output stuff
        player.addChatComponentMessage(
                new ChatComponentTranslation("item.baseItem.tricorder.message.gravity", twoDForm.format(gravity)));
        player.addChatComponentMessage(
                new ChatComponentTranslation(
                        "item.baseItem.tricorder.message.temperature",
                        twoDForm.format(thermalLevel)));
        player.addChatComponentMessage(
                new ChatComponentTranslation("item.baseItem.tricorder.message.solar", twoDForm.format(solarLevel)));
        if (dayLength == -1) {
            player.addChatComponentMessage(
                    new ChatComponentTranslation(
                            "item.baseItem.tricorder.message.daylength",
                            new ChatComponentTranslation("item.baseItem.tricorder.message.unknown")));
        } else {
            player.addChatComponentMessage(
                    new ChatComponentTranslation(
                            "item.baseItem.tricorder.message.daylength",
                            GuiHelper.formatTime(dayLength, false)));
        }

        if (atmospheres.isEmpty()) {
            player.addChatComponentMessage(
                    new ChatComponentTranslation(
                            "item.baseItem.tricorder.message.atmosphere",
                            new ChatComponentTranslation("item.baseItem.tricorder.message.none")));
        } else {
            final StringBuilder builder = new StringBuilder();
            boolean isFirst = true;
            for (final String str : atmospheres) {
                if (!isFirst) {
                    builder.append(", ");
                }
                isFirst = false;
                builder.append(str);
            }
            player.addChatComponentMessage(
                    new ChatComponentTranslation("item.baseItem.tricorder.message.atmosphere", builder.toString()));
        }

        return itemStackIn;
    }

}
