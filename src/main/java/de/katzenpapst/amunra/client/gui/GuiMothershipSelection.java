package de.katzenpapst.amunra.client.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.helper.AstronomyHelper;
import de.katzenpapst.amunra.helper.GuiHelper;
import de.katzenpapst.amunra.mothership.Mothership;
import de.katzenpapst.amunra.mothership.MothershipWorldProvider;
import de.katzenpapst.amunra.mothership.MothershipWorldProvider.TransitData;
import de.katzenpapst.amunra.mothership.MothershipWorldProvider.TransitDataWithDuration;
import de.katzenpapst.amunra.mothership.fueldisplay.MothershipFuelDisplay;
import de.katzenpapst.amunra.mothership.fueldisplay.MothershipFuelRequirements;
import de.katzenpapst.amunra.network.packet.PacketSimpleAR;
import de.katzenpapst.amunra.tile.TileEntityMothershipController;
import de.katzenpapst.amunra.vec.BlockVector;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class GuiMothershipSelection extends GuiARCelestialSelection {

    // block from which the gui was originally opened
    protected BlockVector triggerBlock = null;
    protected Mothership curMothership;
    protected World world;
    protected MothershipWorldProvider provider;

    // x coordinate of the rightmost screen edge before the borders
    protected int offsetX;
    // y coordinate of the topmost screen edge after the borders
    protected int offsetY;

    // launch button
    protected static final int LAUNCHBUTTON_X = -95;
    protected static final int LAUNCHBUTTON_Y = 133;
    protected static final int LAUNCHBUTTON_W = 93;
    protected static final int LAUNCHBUTTON_H = 12;

    // rename button
    protected static final int RENAMEBUTTON_X = -95;
    protected static final int RENAMEBUTTON_Y = 53;
    protected static final int RENAMEBUTTON_W = 93;
    protected static final int RENAMEBUTTON_H = 12;

    // transit info box
    protected static final int TRANSIT_INFO_U = 0;
    protected static final int TRANSIT_INFO_V = 16;
    protected static final int TRANSIT_INFO_W = 179;
    protected static final int TRANSIT_INFO_H = 20;
    /*
     * /*int TRANSIT_INFO_U = 0; int TRANSIT_INFO_V = 16; int TRANSIT_INFO_W = 179; int TRANSIT_INFO_H = 20;
     */

    protected float ticksSinceLaunch = -1;

    protected boolean hasMothershipStats = false;

    public enum TravelFailReason {
        NONE,
        NOT_ENOUGH_FUEL,
        NOT_ENOUGH_THRUST,
        ALREADY_ORBITING,
        NOT_ORBITABLE,
        TRAVEL_TOO_LONG
    }

    protected Map<CelestialBody, MothershipWorldProvider.TransitDataWithDuration> transitDataCache;

    public GuiMothershipSelection(final List<CelestialBody> possibleBodies, final TileEntityMothershipController title,
            final World world) {
        // possibleBodies should be largely irrelevant here
        // hack
        super(MapMode.VIEW, possibleBodies);
        // triggerBlock = blockToReturn;

        this.world = world;
        this.provider = (MothershipWorldProvider) world.provider;
        this.curMothership = (Mothership) this.provider.getCelestialBody();
        // this.travelTimeCache = new HashMap<CelestialBody, Double>();
        this.transitDataCache = new HashMap<>();
        // this.travelTimeCache = new HashMap<CelestialBody, Long>();
    }

    /*
     * protected long getTravelTimeFor(CelestialBody body) { if(this.travelTimeCache.containsKey(body)) { return
     * this.travelTimeCache.get(body); } TransitData tData = this.getTransitDataFor(body); double shipThrust =
     * !tData.isEmpty() ? tData.thrust : provider.getTheoreticalTransitData().thrust; double travelDistance =
     * curMothership.getTravelDistanceTo(selectedBody); long travelTime =
     * AstronomyHelper.getTravelTimeAU(provider.getTotalMass(), shipThrust, travelDistance);
     * this.travelTimeCache.put(body, travelTime); return travelTime; }
     */

    @Override
    public void drawButtons(int mousePosX, int mousePosY) {
        this.possibleBodies = this.shuttlePossibleBodies;
        super.drawButtons(mousePosX, mousePosY);

        // meh
        this.drawMothershipGuiParts(mousePosX, mousePosY);
    }

    public void mothershipUpdateRecieved() {
        this.hasMothershipStats = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.selectAndZoom(this.curMothership.getDestination());

        // if(!curMothership.isInTransit()) {
        AmunRa.packetPipeline.sendToServer(
                new PacketSimpleAR(
                        PacketSimpleAR.EnumSimplePacket.S_MOTHERSHIP_UPDATE,
                        this.world.provider.dimensionId));
        // }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (this.ticksSinceLaunch >= 0) {
            this.ticksSinceLaunch += partialTicks;
            if (this.ticksSinceLaunch > 1.0F) {
                this.ticksSinceLaunch = -1;
            }
        }
    }

    protected void drawBodyOnGUI(final CelestialBody body, final int x, final int y, final int w, final int h) {
        if (body == null) {
            return;
        }
        this.mc.renderEngine.bindTexture(body.getBodyIcon());

        this.drawFullSizedTexturedRect(x, y, w, h);
    }

    public void drawFullSizedTexturedRect(final int x, final int y, final int width, final int height) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        final Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, this.zLevel, 0, 0);
        tessellator.addVertexWithUV(x + width, y + height, this.zLevel, 1, 0);
        tessellator.addVertexWithUV(x + width, y, this.zLevel, 1, 1);
        tessellator.addVertexWithUV(x, y, this.zLevel, 0, 1);
        tessellator.draw();
    }

    protected void drawTransitBar(final int length) {
        // max length = 124
        this.drawTexturedModalRect(
                this.width / 2 - 90 + 27,
                this.height - 11 - GuiCelestialSelection.BORDER_WIDTH + 2,
                1, // displayed w
                4, // displayed h
                0, // u aka x in tex
                0, // v
                1, // w in texture
                4, // h in texture
                false,
                false);
        // bar
        this.drawTexturedModalRect(
                this.width / 2 - 90 + 28,
                this.height - 11 - GuiCelestialSelection.BORDER_WIDTH + 2,
                length, // displayed w
                4, // displayed h
                1, // u aka x in tex
                0, // v
                43, // w in texture
                4, // h in texture
                false,
                false);
        // right cap
        this.drawTexturedModalRect(
                this.width / 2 - 90 + 28 + length,
                this.height - 11 - GuiCelestialSelection.BORDER_WIDTH + 2,
                1, // displayed w
                4, // displayed h
                56, // u aka x in tex
                0, // v
                1, // w in texture
                4, // h in texture
                false,
                false);
    }

    protected void drawTransitInfo(final int mousePosX, final int mousePosY) {
        GL11.glColor4f(0.0F, 0.6F, 1.0F, 1);
        this.mc.renderEngine.bindTexture(guiExtra);

        this.drawTexturedModalRect(
                this.width / 2 - 90,
                this.height - 11 - GuiCelestialSelection.BORDER_WIDTH - 4,
                TRANSIT_INFO_W, // w
                TRANSIT_INFO_H, // h
                TRANSIT_INFO_U, // u
                TRANSIT_INFO_V, // v
                TRANSIT_INFO_W,
                TRANSIT_INFO_H,
                false,
                false);

        // bar
        // this.mc.renderEngine.bindTexture(GuiCelestialSelection.guiMain0);

        GL11.glColor4f(0.35F, 0.01F, 0.01F, 1);
        // GL11.glEnable(GL11.GL_BLEND);

        this.drawTransitBar(124);

        GL11.glColor4f(0.95F, 0.01F, 0.01F, 1);
        this.drawTransitBar(this.curMothership.getScaledTravelTime(124));

        /*
         * this.drawTexturedModalRect( width/2-90+28, height-11-GuiCelestialSelection.BORDER_WIDTH+2, length,
         * //displayed w 4, //displayed h 1, // u aka x in tex 0, // v 43, // w in texture 4, // h in texture false,
         * false);
         */
        if (this.isMouseWithin(
                mousePosX,
                mousePosY,
                this.width / 2 - 90 + 28,
                this.height - 11 - GuiCelestialSelection.BORDER_WIDTH + 2,
                124,
                4)) {
            this.showTooltip(
                    GCCoreUtil.translateWithFormat(
                            "gui.message.mothership.travelTimeRemain",
                            GuiHelper.formatTime(this.curMothership.getRemainingTravelTime())),
                    mousePosX,
                    mousePosY);
        }

        // test place planet symbols
        // source
        int bodyX = this.width / 2 - 90 + 14;
        int bodyY = this.height - 11 - GuiCelestialSelection.BORDER_WIDTH;
        this.drawBodyOnGUI(this.curMothership.getSource(), bodyX, bodyY, 8, 8);
        if (this.isMouseWithin(mousePosX, mousePosY, bodyX, bodyY, 8, 8)) {
            this.showTooltip(this.curMothership.getSource().getLocalizedName(), mousePosX, mousePosY);
        }
        // dest
        bodyX = this.width / 2 + 90 - 14 - 8;
        bodyY = this.height - 11 - GuiCelestialSelection.BORDER_WIDTH;
        this.drawBodyOnGUI(this.curMothership.getDestination(), bodyX, bodyY, 8, 8);
        if (this.isMouseWithin(mousePosX, mousePosY, bodyX, bodyY, 8, 8)) {
            this.showTooltip(this.curMothership.getDestination().getLocalizedName(), mousePosX, mousePosY);
        }
    }

    protected int getScaledTravelTime(final Mothership ship, final int barLength) {
        final float remain = ship.getRemainingTravelTime();
        final float total = ship.getTotalTravelTime();
        final float relative = remain / total;
        final float scaled = (1 - relative) * barLength;
        return (int) scaled;
    }

    protected void drawMothershipGuiParts(final int mousePosX, final int mousePosY) {

        GL11.glColor4f(0.0F, 0.6F, 1.0F, 1);
        /*
         * int canCreateLength = Math.max(0,
         * this.drawSplitString(GCCoreUtil.translate("gui.message.canCreateMothership.name"), 0, 0, 91, 0, true, true) -
         * 2); int canCreateOffset = canCreateLength * this.smallFontRenderer.FONT_HEIGHT;
         */

        this.offsetX = this.width - GuiCelestialSelection.BORDER_WIDTH - GuiCelestialSelection.BORDER_EDGE_WIDTH;
        this.offsetY = GuiCelestialSelection.BORDER_WIDTH + GuiCelestialSelection.BORDER_EDGE_WIDTH;

        if (this.curMothership.isInTransit()) {
            this.drawTransitInfo(mousePosX, mousePosY);
        }
        if (this.hasMothershipStats) {
            this.drawMothershipInfo();
        }
        if (this.selectedBody != null) {
            this.drawTargetBodyInfo(mousePosX, mousePosY);
        }
    }

    protected void drawMothershipInfo() {

        GL11.glColor4f(0.0F, 0.6F, 1.0F, 1);

        this.mc.renderEngine.bindTexture(guiExtra);

        final int boxWidth = 95;
        final int boxHeight = 65;

        final int bottomOffset = this.height - this.offsetY;
        final float totalMass = this.provider.getTotalMass();

        final TransitData tData = this.provider.getTheoreticalTransitData();

        // draw the main texture
        int totalOffset = boxHeight + 1;
        this.drawTexturedModalRect(
                this.offsetX - boxWidth,
                bottomOffset - totalOffset,
                boxWidth, // displayed w
                boxHeight, // displayed h
                0, // u aka x in tex
                70, // v
                boxWidth, // w in texture
                boxHeight, // h in texture
                false,
                false);

        totalOffset = boxHeight - 12;

        // draw the ship's name
        this.drawSplitString(
                this.curMothership.getLocalizedName(),
                this.offsetX - boxWidth / 2 - 2, // x?
                bottomOffset - totalOffset, // y
                91, // width?
                0xFFFFFFFF,
                false,
                false);

        /*
         * protected final int RENAMEBUTTON_X = -95; protected final int RENAMEBUTTON_Y = 53; protected final int
         * RENAMEBUTTON_W = 93; protected final int RENAMEBUTTON_H = 12;
         */

        totalOffset -= 10;

        this.smallFontRenderer.drawString(
                GCCoreUtil.translate("gui.message.mothership.totalMass") + ": " + GuiHelper.formatKilogram(totalMass),
                this.offsetX - 90,
                bottomOffset - totalOffset,
                0xFFFFFFFF,
                false);

        totalOffset -= 10;

        this.smallFontRenderer.drawString(
                GCCoreUtil.translate("gui.message.mothership.totalBlocks") + ": "
                        + GuiHelper.formatMetric(this.provider.getNumBlocks()),
                this.offsetX - 90,
                bottomOffset - totalOffset,
                0xFFFFFFFF,
                false);

        totalOffset -= 10;
        final int thrustColor = tData.thrust <= 0 ? 0xFFFFFFFF : 0xFFFF7E7E;

        this.smallFontRenderer.drawString(
                GCCoreUtil.translate("gui.message.mothership.travelThrust") + ": "
                        + GuiHelper.formatMetric(tData.thrust, "N"),
                this.offsetX - 90,
                bottomOffset - totalOffset,
                thrustColor,
                false);

        if (this.curMothership.isInTransit()) {
            totalOffset -= 10;
            // figure out our speed
            final double distance = AstronomyHelper
                    .getDistance(this.curMothership.getSource(), this.curMothership.getDestination());
            final double speed = distance / this.curMothership.getTotalTravelTime();
            this.smallFontRenderer.drawString(
                    GCCoreUtil.translate("gui.message.mothership.travelSpeed") + ": " + GuiHelper.formatSpeed(speed),
                    this.offsetX - 90,
                    bottomOffset - totalOffset,
                    thrustColor,
                    false);
        }
    }

    protected void drawTargetBodyInfo(final int mousePosX, final int mousePosY) {
        int offset = 17;

        GL11.glColor4f(0.0F, 0.6F, 1.0F, 1);
        this.mc.renderEngine.bindTexture(guiExtra);

        final MothershipWorldProvider.TransitDataWithDuration tData = this.getTransitDataFor(this.selectedBody);

        final TravelFailReason failReason = this.getTravelFailReason(this.selectedBody, tData);

        if (failReason == TravelFailReason.NONE) {
            // green
            GL11.glColor4f(0.0F, 1.0F, 0.1F, 1);
        } else {
            // red
            GL11.glColor4f(1.0F, 0.0F, 0.0F, 1);
        }

        this.drawTexturedModalRect(
                this.offsetX + LAUNCHBUTTON_X,
                this.offsetY + LAUNCHBUTTON_Y,
                LAUNCHBUTTON_W,
                LAUNCHBUTTON_H,
                0, // u
                4, // v
                LAUNCHBUTTON_W,
                LAUNCHBUTTON_H,
                false,
                false);

        this.drawSplitString(
                GCCoreUtil.translate("gui.message.mothership.launchbutton").toUpperCase(),
                this.offsetX - 48,
                this.offsetY + 135,
                91,
                0xFFFFFFFF,
                false,
                false);

        this.drawSplitString(
                this.selectedBody.getLocalizedName(),
                this.offsetX - 48,
                offset + this.offsetY,
                91,
                0xFFFFFFFF,
                false,
                false);

        offset += 12;

        final double travelDistance = this.curMothership.getTravelDistanceTo(this.selectedBody);
        // double shipThrust = !tData.isEmpty() ? tData.thrust : provider.getTheoreticalTransitData().thrust;

        final long travelTime = !tData.isEmpty() ? tData.duration : -1;
        // double shipSpeed = !tData.isEmpty() ? tData.speed : provider.getTheoreticalTransitData().speed;

        switch (failReason) {
            case ALREADY_ORBITING:
                this.smallFontRenderer.drawSplitString(
                        GCCoreUtil.translate("gui.message.mothership.alreadyOrbiting"),
                        this.offsetX - 90,
                        this.offsetY + offset,
                        90,
                        0xFFFFFFFF);
                offset += 10;
                break;
            case NONE:
                offset = this.drawTravelDistance(offset, travelDistance);
                offset = this.drawTravelTime(offset, travelDistance, travelTime);
                offset = this.drawFuelReqs(offset, mousePosX, mousePosY, tData.fuelReqData);
                break;
            case NOT_ENOUGH_FUEL:
                offset = this.drawTravelDistance(offset, travelDistance);
                offset = this.drawTravelTime(offset, travelDistance, travelTime);
                offset = this.drawFuelReqs(offset, mousePosX, mousePosY, tData.fuelReqData);
                this.smallFontRenderer.drawSplitString(
                        GCCoreUtil.translate("gui.message.mothership.notEnoughFuel"),
                        this.offsetX - 90,
                        this.offsetY + offset,
                        90,
                        0xFFFF8080);
                offset += 10;
                break;
            case NOT_ENOUGH_THRUST:
                offset = this.drawTravelDistance(offset, travelDistance);
                offset = this.drawTravelTime(offset, travelDistance, travelTime);
                offset = this.drawFuelReqs(offset, mousePosX, mousePosY, tData.fuelReqData);
                this.smallFontRenderer.drawSplitString(
                        GCCoreUtil.translate("gui.message.mothership.notEnoughThrust"),
                        this.offsetX - 90,
                        this.offsetY + offset,
                        90,
                        0xFFFF8080);
                offset += 10;
                break;
            case NOT_ORBITABLE:
                this.smallFontRenderer.drawSplitString(
                        GCCoreUtil.translate("gui.message.mothership.unreachableBody"),
                        this.offsetX - 90,
                        this.offsetY + offset,
                        90,
                        0xFFFF8080);
                offset += 10;
                break;
            case TRAVEL_TOO_LONG:
                offset = this.drawTravelDistance(offset, travelDistance);
                offset = this.drawTravelTime(offset, travelDistance, travelTime);
                offset = this.drawFuelReqs(offset, mousePosX, mousePosY, tData.fuelReqData);
                this.smallFontRenderer.drawSplitString(
                        GCCoreUtil.translate("gui.message.mothership.travelTooLong"),
                        this.offsetX - 90,
                        this.offsetY + offset,
                        90,
                        0xFFFF8080);
                offset += 10;

                break;
            default:
                break;
        }
    }

    protected int drawFuelReqs(int offset, final int mousePosX, final int mousePosY,
            final MothershipFuelRequirements fuelReqs) {

        if (fuelReqs != null && !fuelReqs.isEmpty()) {
            // offset += 10;
            this.smallFontRenderer.drawSplitString(
                    GCCoreUtil.translate("gui.message.mothership.fuelReqs") + ":",
                    this.offsetX - 90,
                    this.offsetY + offset,
                    90,
                    0xFFFFFFFF);
            for (final MothershipFuelDisplay f : fuelReqs.getData().keySet()) {
                offset += 10;

                this.drawIcon(this.offsetX - 90, this.offsetY + offset, f);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1);
                if (this.isMouseWithin(mousePosX, mousePosY, this.offsetX - 90, this.offsetY + offset, 10, 10)) {
                    this.showTooltip(f.getDisplayName(), mousePosX, mousePosY);
                }

                this.smallFontRenderer.drawSplitString(
                        f.formatValue(fuelReqs.getData().get(f)),
                        this.offsetX - 80,
                        this.offsetY + offset,
                        90,
                        0xFFFFFFFF);
            }
            offset += 10;
        }
        return offset;
    }

    protected int drawTravelDistance(int offset, final double travelDistance) {
        // travel distance
        this.smallFontRenderer.drawString(
                GCCoreUtil.translate("gui.message.mothership.travelDistance") + ": "
                        + GuiHelper.formatMetric(travelDistance, "AU"),
                this.offsetX - 90,
                this.offsetY + offset,
                0xFFFFFFFF,
                false);
        offset += 10;
        return offset;
    }

    protected int drawTravelTime(final int offset, final double travelDistance, final long travelTime) {

        String travelTimeStr;

        if (travelTime > 0) {
            travelTimeStr = GuiHelper.formatTime(travelTime, true);
        } else {
            travelTimeStr = GCCoreUtil.translate("gui.message.misc.n_a");
        }

        // travel time
        this.smallFontRenderer.drawString(
                GCCoreUtil.translate("gui.message.mothership.travelTime") + ": " + travelTimeStr,
                this.offsetX - 90,
                this.offsetY + offset,
                0xFFFFFFFF,
                false);
        return offset + 10;
    }

    protected void drawItemForFuel(final int x, final int y, final ItemStack item) {
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glPushMatrix();
        final float factor = 0.5F;
        GL11.glScalef(factor, factor, factor);
        GuiScreen.itemRender.renderItemAndEffectIntoGUI(
                this.fontRendererObj,
                this.mc.renderEngine,
                item,
                (int) (x / factor),
                (int) (y / factor));
        RenderHelper.disableStandardItemLighting();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    protected void drawIcon(final int x, final int y, final MothershipFuelDisplay fuelType) {

        final ResourceLocation resourcelocation = this.mc.renderEngine.getResourceLocation(fuelType.getSpriteNumber());// 0
                                                                                                                       // is
        // correct
        this.mc.renderEngine.bindTexture(resourcelocation);

        GL11.glDisable(GL11.GL_LIGHTING); // Forge: Make sure that render states are reset, a renderEffect can derp them
                                          // up.
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);

        GuiScreen.itemRender.renderIcon(x, y, fuelType.getIcon(), 8, 8);

    }

    protected TransitDataWithDuration getTransitDataFor(final CelestialBody body) {
        TransitDataWithDuration result;
        if (!this.transitDataCache.containsKey(this.selectedBody)) {
            result = this.provider.getTransitDataTo(this.selectedBody);
            if (result.fuelReqData == null) {
                // try getting potential fuel req
                result.fuelReqData = this.provider.getPotentialFuelReqs(body);
            }
            this.transitDataCache.put(this.selectedBody, result);
        } else {
            result = this.transitDataCache.get(this.selectedBody);
        }
        return result;
    }

    protected TravelFailReason getTravelFailReason(final CelestialBody body, TransitDataWithDuration tData) {

        if (body == null || !Mothership.canBeOrbited(body)) {
            return TravelFailReason.NOT_ORBITABLE;
        }

        if (body == this.curMothership.getParent() || body == this.curMothership.getDestination()) {
            return TravelFailReason.ALREADY_ORBITING;
        }

        if (tData == null) {
            tData = this.getTransitDataFor(this.selectedBody);
        }

        final long travelTime = tData.duration;
        if (travelTime > AmunRa.config.mothershipMaxTravelTime) {
            return TravelFailReason.TRAVEL_TOO_LONG;
        }

        if (tData.isEmpty()) {
            // either not enough fuel, or not enough thrust
            // TransitData theoreticalData = provider.getTheoreticalTransitData();
            /*
             * float mass = provider.getTotalMass(); if(theoreticalData.thrust < mass) { return
             * TravelFailReason.NOT_ENOUGH_THRUST; }
             */
            // seems like not enough fuel
            return TravelFailReason.NOT_ENOUGH_FUEL;
        }

        // double distance = curMothership.getTravelDistanceTo(body);

        return TravelFailReason.NONE;
    }

    /*
     * protected boolean canTravelTo(CelestialBody body, TransitData tData) { // simple stuff if ( body == null || body
     * == curMothership.getParent() || !Mothership.canBeOrbited(body) ) { return false; } // more complicated stuff
     * if(tData == null) { tData = getTransitDataFor(this.selectedBody); } if(tData.isEmpty()) { return false; } // most
     * complicated stuff double distance = curMothership.getTravelDistanceTo(body); int travelTime =
     * curMothership.getTravelTimeTo(distance, tData.speed); if(travelTime >
     * AmunRa.instance.confMaxMothershipTravelTime) { return false; } return true; }
     */

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        // simple checks first
        int actualBtnX = this.offsetX + LAUNCHBUTTON_X;
        int actualBtnY = this.offsetY + LAUNCHBUTTON_Y;
        if (actualBtnX <= mouseX && mouseX <= actualBtnX + LAUNCHBUTTON_W
                && actualBtnY <= mouseY
                && mouseY <= actualBtnY + LAUNCHBUTTON_H) {
            if (this.getTravelFailReason(this.selectedBody, null) == TravelFailReason.NONE) {

                // spam protection?
                if (this.ticksSinceLaunch > -1) {
                    return;
                }
                this.ticksSinceLaunch = 0;
                // send packet?
                AmunRa.packetPipeline.sendToServer(
                        new PacketSimpleAR(
                                PacketSimpleAR.EnumSimplePacket.S_MOTHERSHIP_TRANSIT_START,
                                this.curMothership.getID(),
                                AstronomyHelper.getOrbitableBodyName(this.selectedBody)));

            }
            return; // return anyway
        }

        if (this.hasMothershipStats) {
            actualBtnX = this.offsetX + RENAMEBUTTON_X;
            actualBtnY = this.height - this.offsetY - RENAMEBUTTON_Y;
            if (actualBtnX <= mouseX && mouseX <= actualBtnX + RENAMEBUTTON_W
                    && actualBtnY <= mouseY
                    && mouseY <= actualBtnY + RENAMEBUTTON_H) {
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

}
