package de.katzenpapst.amunra.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.crafting.RecipeHelper;
import de.katzenpapst.amunra.helper.AstronomyHelper;
import de.katzenpapst.amunra.helper.ShuttleTeleportHelper;
import de.katzenpapst.amunra.mothership.Mothership;
import de.katzenpapst.amunra.network.packet.PacketSimpleAR;
import de.katzenpapst.amunra.vec.BoxInt2D;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.IChildBody;
import micdoodle8.mods.galacticraft.api.galaxies.Satellite;
import micdoodle8.mods.galacticraft.api.recipe.SpaceStationRecipe;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import micdoodle8.mods.galacticraft.core.util.ColorUtil;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class GuiShuttleSelection extends GuiARCelestialSelection {

    protected boolean createMothershipButtonDisabled = false;

    protected BoxInt2D exitBtnArea = new BoxInt2D();
    protected BoxInt2D buildMsBtnArea = new BoxInt2D();

    public GuiShuttleSelection(final MapMode mapMode, final List<CelestialBody> possibleBodies) {
        super(mapMode, possibleBodies);
    }

    protected CelestialBody getParent(final CelestialBody body) {
        if (body instanceof IChildBody child) {// satellite apparently implements this already?
            return child.getParentPlanet();
        }
        if (body instanceof Mothership ship) {
            return ship.getParent();
        }
        return body;
    }

    @Override
    public void initGui() {
        super.initGui();

        final CelestialBody currentPlayerBody = ShuttleTeleportHelper
                .getCelestialBodyForDimensionID(this.mc.thePlayer.dimension);
        if (currentPlayerBody != null) {
            this.selectAndZoom(currentPlayerBody);
        }
    }

    @Override
    public void drawButtons(int mousePosX, int mousePosY) {
        this.possibleBodies = this.shuttlePossibleBodies;
        super.drawButtons(mousePosX, mousePosY);
        if (this.selectionState != EnumSelectionState.PROFILE && this.selectedBody != null
                && this.canCreateMothership(this.selectedBody)) {
            this.drawMothershipButton(mousePosX, mousePosY);
        }

        // exit button

        GL11.glColor4f(0.0F, 1.0F, 0.1F, 1);
        this.mc.renderEngine.bindTexture(GuiCelestialSelection.guiMain0);

        final int exitWidth = this.width - GuiCelestialSelection.BORDER_WIDTH
                - GuiCelestialSelection.BORDER_EDGE_WIDTH
                - 74;
        final int exitHeight = this.height - GuiCelestialSelection.BORDER_WIDTH
                - GuiCelestialSelection.BORDER_EDGE_WIDTH
                - 11;

        this.exitBtnArea.setPositionSize(exitWidth, exitHeight, 74, 11);

        this.drawTexturedModalRect(
                this.exitBtnArea.minX,
                this.exitBtnArea.minY,
                this.exitBtnArea.getWidth(),
                this.exitBtnArea.getHeight(),
                0,
                392,
                148,
                22,
                true,
                true);
        final String str = GCCoreUtil.translate("gui.message.cancel.name").toUpperCase();
        this.fontRendererObj.drawString(
                str,
                this.exitBtnArea.minX + (this.exitBtnArea.getWidth() - this.fontRendererObj.getStringWidth(str)) / 2,
                this.exitBtnArea.minY + 2,
                0xFFFFFFFF);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);

    }

    @Override
    protected boolean canCreateSpaceStation(final CelestialBody atBody) {
        // no stations can be built from the shuttle, because there's not enough space on the screen
        return false;
    }

    protected boolean canCreateMothership(final CelestialBody atBody) {
        // important! check where the player started from
        if (this.numPlayersMotherships < 0 || this.playerParent == null) {
            return false;
        }

        return (AmunRa.config.maxNumMotherships == -1 || this.numPlayersMotherships < AmunRa.config.maxNumMotherships)
                && this.playerParent == this.selectedBody
                && Mothership.canBeOrbited(atBody);
    }

    protected void drawItemForRecipe(final ItemStack item, final int amount, final int requiredAmount, final int xPos,
            final int yPos, final int mousePosX, final int mousePosY) {
        RenderHelper.enableGUIStandardItemLighting();
        GuiScreen.itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.renderEngine, item, xPos, yPos);
        RenderHelper.disableStandardItemLighting();
        GL11.glEnable(GL11.GL_BLEND);

        if (this.isMouseWithin(mousePosX, mousePosY, xPos, yPos, 16, 16)) {
            this.showTooltip(item.getDisplayName(), mousePosX, mousePosY);
        }

        final String str = "" + amount + "/" + requiredAmount;
        final boolean valid = amount >= requiredAmount;

        final int color = valid | this.mc.thePlayer.capabilities.isCreativeMode ? 0xFF00FF00 : 0xFFFF0000;
        this.smallFontRenderer
                .drawString(str, xPos + 8 - this.smallFontRenderer.getStringWidth(str) / 2, yPos + 16, color);
    }

    /*
     * TODO find a way to do this
     * @Override protected int getAmountInInventory(ItemStack stack) { int amountInInv =
     * super.getAmountInInventory(stack); EntityClientPlayerMP player =
     * FMLClientHandler.instance().getClientPlayerEntity(); Entity rocket = player.ridingEntity; //GCPlayerStats
     * if(rocket instanceof EntityAutoRocket) { EntityAutoRocket realRocket = (EntityAutoRocket)rocket; for (int x = 0;
     * x < realRocket.getSizeInventory(); x++) { final ItemStack slot = realRocket.getStackInSlot(x); if (slot != null)
     * { if (SpaceStationRecipe.checkItemEquals(stack, slot)) { amountInInv += slot.stackSize; } } } } // now also try
     * to check the ship's inventory return amountInInv; }
     */
    protected void drawMothershipButton(final int mousePosX, final int mousePosY) {
        final int offset = 0;

        GL11.glColor4f(0.0F, 0.6F, 1.0F, 1);
        this.mc.renderEngine.bindTexture(GuiCelestialSelection.guiMain1);
        final int canCreateLength = Math.max(
                0,
                this.drawSplitString(
                        GCCoreUtil.translate("gui.message.canCreateMothership.name"),
                        0,
                        0,
                        91,
                        0,
                        true,
                        true) - 2);
        final int canCreateOffset = canCreateLength * this.smallFontRenderer.FONT_HEIGHT;

        /*
         * x > width - GuiCelestialSelection.BORDER_WIDTH - GuiCelestialSelection.BORDER_EDGE_WIDTH - 96 && x < width -
         * GuiCelestialSelection.BORDER_WIDTH - GuiCelestialSelection.BORDER_EDGE_WIDTH && y >
         * GuiCelestialSelection.BORDER_WIDTH + GuiCelestialSelection.BORDER_EDGE_WIDTH + 182 && y <
         * GuiCelestialSelection.BORDER_WIDTH + GuiCelestialSelection.BORDER_EDGE_WIDTH + 182 + 12
         */

        this.drawTexturedModalRect(
                this.width - GuiCelestialSelection.BORDER_WIDTH - GuiCelestialSelection.BORDER_EDGE_WIDTH - 95, // x
                offset + GuiCelestialSelection.BORDER_WIDTH + GuiCelestialSelection.BORDER_EDGE_WIDTH + 134, // y
                93, // w
                4, // h
                159, // u
                102, // v
                93, // uWidth
                4, // uHeight
                false,
                false);
        for (int barY = 0; barY < canCreateLength; ++barY) {
            this.drawTexturedModalRect(
                    this.width - GuiCelestialSelection.BORDER_WIDTH - GuiCelestialSelection.BORDER_EDGE_WIDTH - 95,
                    offset + GuiCelestialSelection.BORDER_WIDTH
                            + GuiCelestialSelection.BORDER_EDGE_WIDTH
                            + 138
                            + barY * this.smallFontRenderer.FONT_HEIGHT,
                    93,
                    this.smallFontRenderer.FONT_HEIGHT,
                    159,
                    106,
                    93,
                    this.smallFontRenderer.FONT_HEIGHT,
                    false,
                    false);
        }
        this.drawTexturedModalRect(
                this.width - GuiCelestialSelection.BORDER_WIDTH - GuiCelestialSelection.BORDER_EDGE_WIDTH - 95,
                offset + GuiCelestialSelection.BORDER_WIDTH
                        + GuiCelestialSelection.BORDER_EDGE_WIDTH
                        + 138
                        + canCreateOffset,
                93,
                43,
                159,
                106,
                93,
                43,
                false,
                false);
        this.drawTexturedModalRect(
                this.width - GuiCelestialSelection.BORDER_WIDTH - GuiCelestialSelection.BORDER_EDGE_WIDTH - 79,
                offset + GuiCelestialSelection.BORDER_WIDTH + GuiCelestialSelection.BORDER_EDGE_WIDTH + 129,
                61,
                4,
                0,
                170,
                61,
                4,
                false,
                false);

        int xPos = 0;
        final int yPos = offset + GuiCelestialSelection.BORDER_WIDTH
                + GuiCelestialSelection.BORDER_EDGE_WIDTH
                + 154
                + canCreateOffset;
        //
        final SpaceStationRecipe recipe = RecipeHelper.mothershipRecipe;
        if (recipe != null) {
            GL11.glColor4f(0.0F, 1.0F, 0.1F, 1);
            boolean validInputMaterials = true;

            int i = 0;
            for (final Map.Entry<Object, Integer> e : recipe.getInput().entrySet()) {
                final Object next = e.getKey();
                xPos = (int) (this.width - GuiCelestialSelection.BORDER_WIDTH
                        - GuiCelestialSelection.BORDER_EDGE_WIDTH
                        - 95
                        + i * 93 / (double) recipe.getInput().size()
                        + 5);
                final int requiredAmount = e.getValue();

                if (next instanceof ItemStack) {
                    final int amount = this.getAmountInInventory((ItemStack) next);
                    this.drawItemForRecipe(
                            ((ItemStack) next).copy(),
                            amount,
                            requiredAmount,
                            xPos,
                            yPos,
                            mousePosX,
                            mousePosY);
                    validInputMaterials = amount >= requiredAmount && validInputMaterials;

                } // if itemstack
                else if (next instanceof ArrayList) {
                    @SuppressWarnings("unchecked")
                    final ArrayList<ItemStack> items = (ArrayList<ItemStack>) next;

                    int amount = 0;

                    for (final ItemStack stack : items) {
                        amount += this.getAmountInInventory(stack);
                    }
                    final ItemStack stack = items.get(this.ticksSinceMenuOpen / 20 % items.size()).copy();
                    this.drawItemForRecipe(stack, amount, requiredAmount, xPos, yPos, mousePosX, mousePosY);
                    validInputMaterials = amount >= requiredAmount && validInputMaterials;
                }

                i++;
            }

            if ((validInputMaterials || this.mc.thePlayer.capabilities.isCreativeMode)
                    && !this.createMothershipButtonDisabled) {
                GL11.glColor4f(0.0F, 1.0F, 0.1F, 1);
            } else {
                GL11.glColor4f(1.0F, 0.0F, 0.0F, 1);
            }

            this.mc.renderEngine.bindTexture(GuiCelestialSelection.guiMain1);

            this.buildMsBtnArea.setPositionSize(
                    this.width - GuiCelestialSelection.BORDER_WIDTH - GuiCelestialSelection.BORDER_EDGE_WIDTH - 95,
                    offset + GuiCelestialSelection.BORDER_WIDTH
                            + GuiCelestialSelection.BORDER_EDGE_WIDTH
                            + 182
                            + canCreateOffset,
                    93,
                    12);

            if (this.mapMode != MapMode.VIEW && this.buildMsBtnArea.isWithin(mousePosX, mousePosY)) {
                this.drawTexturedModalRect(
                        this.buildMsBtnArea.minX,
                        this.buildMsBtnArea.minY,
                        this.buildMsBtnArea.getWidth(),
                        this.buildMsBtnArea.getHeight(),
                        0,
                        174,
                        93,
                        12,
                        false,
                        false);
            }

            this.drawTexturedModalRect(
                    this.buildMsBtnArea.minX,
                    this.buildMsBtnArea.minY,
                    this.buildMsBtnArea.getWidth(),
                    this.buildMsBtnArea.getHeight(),
                    0,
                    174,
                    93,
                    12,
                    false,
                    false);

            final int color = (int) ((Math.sin(this.ticksSinceMenuOpen / 5.0) * 0.5 + 0.5) * 255);
            this.drawSplitString(
                    GCCoreUtil.translate("gui.message.canCreateMothership.name"),
                    this.width - GuiCelestialSelection.BORDER_WIDTH - GuiCelestialSelection.BORDER_EDGE_WIDTH - 48,
                    offset + GuiCelestialSelection.BORDER_WIDTH + GuiCelestialSelection.BORDER_EDGE_WIDTH + 137,
                    91,
                    ColorUtil.to32BitColor(255, color, 255, color),
                    true,
                    false);

            if (this.mapMode != MapMode.VIEW) {
                this.drawSplitString(
                        GCCoreUtil.translate("gui.message.createSS.name").toUpperCase(),
                        this.width - GuiCelestialSelection.BORDER_WIDTH - GuiCelestialSelection.BORDER_EDGE_WIDTH - 48,
                        offset + GuiCelestialSelection.BORDER_WIDTH
                                + GuiCelestialSelection.BORDER_EDGE_WIDTH
                                + 185
                                + canCreateOffset,
                        91,
                        0xFFFFFFFF,
                        false,
                        false);
            }
        } // if (recipe != null)
        else {
            this.drawSplitString(
                    GCCoreUtil.translate("gui.message.cannotCreateSpaceStation.name"),
                    this.width - GuiCelestialSelection.BORDER_WIDTH - GuiCelestialSelection.BORDER_EDGE_WIDTH - 48,
                    offset + GuiCelestialSelection.BORDER_WIDTH + GuiCelestialSelection.BORDER_EDGE_WIDTH + 138,
                    91,
                    0xFFFFFFFF,
                    true,
                    false);
        }

    }

    @Override
    protected boolean teleportToSelectedBody() {
        this.possibleBodies = this.shuttlePossibleBodies;
        if (this.selectedBody != null && this.selectedBody.getReachable()
                && this.possibleBodies != null
                && this.possibleBodies.contains(this.selectedBody)) {
            try {
                Integer dimensionID = null;

                if (this.selectedBody instanceof Satellite) {
                    if (this.spaceStationMap == null) {
                        AmunRa.LOGGER.error("Please report as a BUG: spaceStationIDs was null.");
                        return false;
                    }
                    final Satellite selectedSatellite = (Satellite) this.selectedBody;
                    final Integer mapping = this.spaceStationMap.get(this.getSatelliteParentID(selectedSatellite))
                            .get(this.selectedStationOwner).getStationDimensionID();
                    // No need to check lowercase as selectedStationOwner is taken from keys.
                    if (mapping == null) {
                        AmunRa.LOGGER.error(
                                "Problem matching player name in space station check: {}",
                                this.selectedStationOwner);
                        return false;
                    }
                    dimensionID = mapping;
                } else {
                    dimensionID = this.selectedBody.getDimensionID();
                }
                /*
                 * if (dimension.contains("$")) { this.mc.gameSettings.thirdPersonView = 0; } if(dimensionID == null) {
                 * return false; }
                 */
                AmunRa.packetPipeline.sendToServer(
                        new PacketSimpleAR(PacketSimpleAR.EnumSimplePacket.S_TELEPORT_SHUTTLE, dimensionID));
                this.mc.displayGuiScreen(null);
                return true;
            } catch (final Exception e) {
                AmunRa.LOGGER.warn("Failed to teleport to selected body", e);
            }
        }
        return false;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // exitBtnArea

        boolean clickHandled = false;
        // CelestialBody curSelection = this.selectedBody;

        if (this.mapMode != MapMode.VIEW) {
            if (this.exitBtnArea.isWithin(mouseX, mouseY)) {
                this.cancelLaunch();
                clickHandled = true;
            } else if (this.buildMsBtnArea.isWithin(mouseX, mouseY) && this.selectedBody != null) {
                final SpaceStationRecipe recipe = RecipeHelper.mothershipRecipe;
                if (recipe != null && this.canCreateMothership(this.selectedBody)
                        && !this.createMothershipButtonDisabled) {
                    if (recipe.matches(this.mc.thePlayer, false) || this.mc.thePlayer.capabilities.isCreativeMode) {
                        this.createMothershipButtonDisabled = true;
                        AmunRa.packetPipeline.sendToServer(
                                new PacketSimpleAR(
                                        PacketSimpleAR.EnumSimplePacket.S_CREATE_MOTHERSHIP,
                                        AstronomyHelper.getOrbitableBodyName(this.selectedBody)));
                    }
                    clickHandled = true;
                }
            }
        }

        if (!clickHandled) {
            super.mouseClicked(mouseX, mouseY, mouseButton);

        }
    }

    protected void cancelLaunch() {
        AmunRa.packetPipeline.sendToServer(new PacketSimpleAR(PacketSimpleAR.EnumSimplePacket.S_CANCEL_SHUTTLE));
        /*
         * if(mc.thePlayer.ridingEntity != null) { System.out.print("yes, riding"); } mc.displayGuiScreen(null);
         */
    }

    @Override
    public void mothershipCreationFailed() {

        this.createMothershipButtonDisabled = false;
    }

    @Override
    public void newMothershipCreated(final Mothership ship) {
        super.newMothershipCreated(ship);

        if (ship.isPlayerOwner(this.mc.thePlayer)) {
            this.selectAndZoom(ship);
        }

        this.createMothershipButtonDisabled = false;
    }
}
