package de.katzenpapst.amunra.client.gui;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.helper.ShuttleTeleportHelper;
import de.katzenpapst.amunra.mothership.Mothership;
import de.katzenpapst.amunra.mothership.MothershipWorldData;
import de.katzenpapst.amunra.tick.TickHandlerServer;
import de.katzenpapst.amunra.vec.BoxInt2D;
import micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.IChildBody;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import micdoodle8.mods.galacticraft.api.galaxies.Star;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class GuiARCelestialSelection extends GuiCelestialSelection {

    public static ResourceLocation guiExtra = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/gui/celestialselection_extra.png");

    protected int numPlayersMotherships = -1;

    // the body where the player has started from, or the mothership's parent, if player started from an MS
    // can be null
    protected CelestialBody playerParent = null;

    protected CelestialBody lastSelectedBodyMS;

    protected CelestialBody nextSelectedBody = null;

    protected List<CelestialBody> shuttlePossibleBodies;

    protected boolean isMessageShown = false;

    protected String messageCaption = "";
    protected String messageText = "";

    protected BoxInt2D messageButtonBox = new BoxInt2D();

    public GuiARCelestialSelection(final MapMode mapMode, final List<CelestialBody> possibleBodies) {
        super(mapMode, possibleBodies);
        this.shuttlePossibleBodies = possibleBodies;
    }

    public void setPossibleBodies(final List<CelestialBody> possibleBodies) {
        this.possibleBodies = possibleBodies;
        this.shuttlePossibleBodies = possibleBodies;
    }

    @Override
    public void initGui() {
        super.initGui();

        // do stuff
        final MothershipWorldData msData = TickHandlerServer.mothershipData;
        for (final Mothership ms : msData.getMotherships().values()) {
            this.celestialBodyTicks.put(ms, 0);
        }

        this.updateNumPlayerMotherships();
        this.updatePlayerParent();
    }

    protected boolean isMouseWithin(final int mouseX, final int mouseY, final int rectX, final int rectY,
            final int rectW, final int rectH) {
        return mouseX >= rectX && mouseX <= rectX + rectW && mouseY >= rectY && mouseY <= rectY + rectH;
    }

    @Override
    public void drawButtons(int mousePosX, int mousePosY) {
        super.drawButtons(mousePosX, mousePosY);
        if (this.isMessageShown) {
            this.drawMessageBox();
        }
    }

    protected void drawMessageBox() {
        GL11.glPushMatrix();

        GL11.glColor4f(0.0F, 0.6F, 1.0F, 1);

        this.mc.renderEngine.bindTexture(guiExtra);

        // box
        final int boxWidth = 179;
        final int boxHeight = 67;
        this.drawTexturedModalRect(
                (this.width - boxWidth) / 2,
                (this.height - boxHeight) / 2,
                boxWidth,
                boxHeight,
                0,
                135, // u/v position
                boxWidth,
                boxHeight, // u/v size
                true,
                false);

        final int btnOffset = 24;

        final int btnWidth = 93;
        final int btnHeight = 12;

        this.messageButtonBox.setPositionSize(
                (this.width - btnWidth) / 2,
                (this.height - btnHeight) / 2 + btnOffset,
                btnWidth,
                btnHeight);

        GL11.glColor4f(0.0F, 1.0F, 0.0F, 1);
        this.drawTexturedModalRect(
                this.messageButtonBox.minX,
                this.messageButtonBox.minY,
                this.messageButtonBox.getWidth(),
                this.messageButtonBox.getHeight(),
                0,
                202, // u/v position
                93,
                12, // u/v size
                true,
                false);
        String str = this.messageCaption;
        this.fontRendererObj.drawString(
                str,
                (this.width - this.fontRendererObj.getStringWidth(str)) / 2,
                (this.height - boxHeight) / 2 + 3,
                0xFFFFFFFF);

        str = GCCoreUtil.translate("gui.message.mothership.okay");
        this.fontRendererObj.drawString(
                str,
                (this.width - this.fontRendererObj.getStringWidth(str)) / 2,
                (this.height - btnHeight) / 2 + 2 + btnOffset,
                0xFFFFFFFF);

        this.fontRendererObj.drawSplitString(
                this.messageText,
                (this.width - boxWidth) / 2 + 4,
                (this.height - boxHeight) / 2 + 14,
                boxWidth - 8,
                0xFFFFFFFF);

        GL11.glPopMatrix();
    }

    protected void showTooltip(final String text, final int mousePosX, final int mousePosY) {
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0, 300);
        final int stringWidth = this.smallFontRenderer.getStringWidth(text);
        int tooltipX = mousePosX - stringWidth / 2;
        int tooltipY = mousePosY - 12;
        final int widhtOffsetOrSo = 8;

        if (tooltipX + stringWidth > this.width) {
            tooltipX -= tooltipX - this.width + stringWidth;
        }

        if (tooltipY + widhtOffsetOrSo + 6 > this.height) {
            tooltipY = this.height - widhtOffsetOrSo - 6;
        }

        final int colorBG = 0xBE0099FF;
        this.drawGradientRect(tooltipX - 3, tooltipY - 4, tooltipX + stringWidth + 3, tooltipY - 3, colorBG, colorBG);
        this.drawGradientRect(
                tooltipX - 3,
                tooltipY + widhtOffsetOrSo + 3,
                tooltipX + stringWidth + 3,
                tooltipY + widhtOffsetOrSo + 4,
                colorBG,
                colorBG);
        this.drawGradientRect(
                tooltipX - 3,
                tooltipY - 3,
                tooltipX + stringWidth + 3,
                tooltipY + widhtOffsetOrSo + 3,
                colorBG,
                colorBG);
        this.drawGradientRect(
                tooltipX - 4,
                tooltipY - 3,
                tooltipX - 3,
                tooltipY + widhtOffsetOrSo + 3,
                colorBG,
                colorBG);
        this.drawGradientRect(
                tooltipX + stringWidth + 3,
                tooltipY - 3,
                tooltipX + stringWidth + 4,
                tooltipY + widhtOffsetOrSo + 3,
                colorBG,
                colorBG);
        final int colorBorderStart = 0xAA0099FF;
        final int colorBorderEnd = 0xAA004C7F;
        this.drawGradientRect(
                tooltipX - 3,
                tooltipY - 3 + 1,
                tooltipX - 3 + 1,
                tooltipY + widhtOffsetOrSo + 3 - 1,
                colorBorderStart,
                colorBorderEnd);
        this.drawGradientRect(
                tooltipX + stringWidth + 2,
                tooltipY - 3 + 1,
                tooltipX + stringWidth + 3,
                tooltipY + widhtOffsetOrSo + 3 - 1,
                colorBorderStart,
                colorBorderEnd);
        this.drawGradientRect(
                tooltipX - 3,
                tooltipY - 3,
                tooltipX + stringWidth + 3,
                tooltipY - 3 + 1,
                colorBorderStart,
                colorBorderStart);
        this.drawGradientRect(
                tooltipX - 3,
                tooltipY + widhtOffsetOrSo + 2,
                tooltipX + stringWidth + 3,
                tooltipY + widhtOffsetOrSo + 3,
                colorBorderEnd,
                colorBorderEnd);

        this.smallFontRenderer.drawString(text, tooltipX, tooltipY, 0xFFFFFFFF);

        GL11.glPopMatrix();
    }

    protected boolean isSiblingOf(final CelestialBody celestialBody, final Mothership ship) {
        if (celestialBody instanceof Planet) {
            final SolarSystem solarSystem = ((Planet) celestialBody).getParentSolarSystem();
            return solarSystem.getMainStar().equals(ship.getParent());
        }
        if (celestialBody instanceof IChildBody childBody) {
            return childBody.getParentPlanet().equals(ship.getParent());
        }
        if (celestialBody instanceof Mothership otherShip) {
            return otherShip.getParent().equals(ship.getParent());
        }

        return false;
    }

    protected List<Mothership> getMothershipListToRender() {
        final List<Mothership> result = new LinkedList<>();

        if (this.selectedBody != null) {
            final MothershipWorldData msData = TickHandlerServer.mothershipData;

            for (final Mothership ms : msData.getMotherships().values()) {
                if ((ms == this.selectedBody || ms.getParent() == this.selectedBody && this.selectionCount != 1)
                        && (this.ticksSinceSelection > 35 || this.selectedBody == ms
                                || this.lastSelectedBody instanceof Mothership ship && ship.getParent().equals(ms))
                        || this.isSiblingOf(this.selectedBody, ms)) {
                    result.add(ms);
                }
            }
        }

        return result;
    }

    protected CelestialBody getBodyToRenderMothershipsAround() {

        if (this.selectedBody instanceof Star || this.selectedBody instanceof Planet) {
            if (this.selectionCount != 1 && this.ticksSinceSelection > 35) {
                return this.selectedBody;
            }
            return null;
        }
        if (this.selectedBody instanceof IChildBody) {
            if (this.selectionCount != 1 && this.ticksSinceSelection > 35) {
                return this.selectedBody;
            }
        } else if (this.selectedBody instanceof Mothership ship) {
            return ship.getParent();
        }
        return null;
    }

    protected void _workaroundDrawMoon(final Matrix4f worldMatrix0, final Moon moon, final FloatBuffer fb,
            final Map<CelestialBody, Matrix4f> matrixMap) {
        GL11.glPushMatrix();
        final Matrix4f worldMatrix1 = new Matrix4f(worldMatrix0);
        Matrix4f.translate(this.getCelestialBodyPosition(moon), worldMatrix1, worldMatrix1);

        Matrix4f worldMatrix2 = new Matrix4f();
        Matrix4f.rotate((float) Math.toRadians(45), new Vector3f(0, 0, 1), worldMatrix2, worldMatrix2);
        Matrix4f.rotate((float) Math.toRadians(-55), new Vector3f(1, 0, 0), worldMatrix2, worldMatrix2);
        Matrix4f.scale(new Vector3f(0.25F, 0.25F, 1.0F), worldMatrix2, worldMatrix2);
        worldMatrix2 = Matrix4f.mul(worldMatrix1, worldMatrix2, worldMatrix2);

        fb.rewind();
        worldMatrix2.store(fb);
        fb.flip();
        GL11.glMultMatrix(fb);

        final CelestialBodyRenderEvent.Pre preEvent = new CelestialBodyRenderEvent.Pre(moon, moon.getBodyIcon(), 8);
        MinecraftForge.EVENT_BUS.post(preEvent);

        GL11.glColor4f(1, 1, 1, 1);
        if (preEvent.celestialBodyTexture != null) {
            this.mc.renderEngine.bindTexture(preEvent.celestialBodyTexture);
        }

        if (!preEvent.isCanceled()) {
            final int size = getWidthForCelestialBodyStatic(moon);
            this.drawTexturedModalRect(
                    -size / 2,
                    -size / 2,
                    size,
                    size,
                    0,
                    0,
                    preEvent.textureSize,
                    preEvent.textureSize,
                    false,
                    false,
                    preEvent.textureSize,
                    preEvent.textureSize);
            matrixMap.put(moon, worldMatrix1);
        }

        final CelestialBodyRenderEvent.Post postEvent = new CelestialBodyRenderEvent.Post(moon);
        MinecraftForge.EVENT_BUS.post(postEvent);
        fb.clear();
        GL11.glPopMatrix();
    }

    protected void _workaroundDrawMoonCircle(final Moon moon, final float sin, final float cos) {
        float x = this.getScale(moon);
        float y = 0;

        float alpha = 1;

        GL11.glPushMatrix();
        final Vector3f planetPos = this.getCelestialBodyPosition(moon.getParentPlanet());
        GL11.glTranslatef(planetPos.x, planetPos.y, 0);

        if (this.selectionCount >= 2) {
            alpha = this.selectedBody instanceof IChildBody ? 1.0F
                    : Math.min(Math.max((this.ticksSinceSelection - 30) / 15.0F, 0.0F), 1.0F);

            if (this.lastSelectedBody instanceof Moon && GalaxyRegistry
                    .getMoonsForPlanet(((Moon) this.lastSelectedBody).getParentPlanet()).contains(moon)) {
                alpha = 1.0F;
            }
        }

        if (alpha != 0) {
            /*
             * switch (count % 2) { case 0: GL11.glColor4f(0.0F, 0.6F, 1.0F, alpha); break; case 1:
             */
            GL11.glColor4f(0.4F, 0.9F, 1.0F, alpha);
            /*
             * break; }
             */

            final CelestialBodyRenderEvent.CelestialRingRenderEvent.Pre preEvent = new CelestialBodyRenderEvent.CelestialRingRenderEvent.Pre(
                    moon,
                    new Vector3f(0.0F, 0.0F, 0.0F));
            MinecraftForge.EVENT_BUS.post(preEvent);

            if (!preEvent.isCanceled()) {
                GL11.glBegin(GL11.GL_LINE_LOOP);

                float temp;
                for (int i = 0; i < 90; i++) {
                    GL11.glVertex2f(x, y);

                    temp = x;
                    x = cos * x - sin * y;
                    y = sin * temp + cos * y;
                }

                GL11.glEnd();

                // count++;
            }

            final CelestialBodyRenderEvent.CelestialRingRenderEvent.Post postEvent = new CelestialBodyRenderEvent.CelestialRingRenderEvent.Post(
                    moon);
            MinecraftForge.EVENT_BUS.post(postEvent);
        }
        GL11.glPopMatrix();
    }

    @Override
    public void drawCircles() {

        // do the motherships first, because I have no idea where the matrix will be after the super call
        GL11.glPushMatrix();

        GL11.glLineWidth(3);

        final float theta = (float) (2 * Math.PI / 90);
        final float cos = (float) Math.cos(theta);
        final float sin = (float) Math.sin(theta);

        final CelestialBody body = this.getBodyToRenderMothershipsAround();
        if (body instanceof Moon moon && this.selectionCount >= 1) { // TODO add condition to figure out if stuff
            this._workaroundDrawMoonCircle(moon, sin, cos);
        }
        GL11.glColor4f(0.6F, 0.2F, 0.2F, 0.8F);
        if (body != null && TickHandlerServer.mothershipData.hasMothershipsInOrbit(body)) {

            final float dist = TickHandlerServer.mothershipData.getMothershipOrbitDistanceFor(body);
            float scale = 3.0F * dist * (1.0F / 5.0F);

            if (body instanceof Star) {
                scale *= 3;
            }

            final Vector3f planetPos = this.getCelestialBodyPosition(body);
            GL11.glTranslatef(planetPos.x, planetPos.y, 0);

            float x = scale;
            float y = 0;

            GL11.glBegin(GL11.GL_LINE_LOOP);

            float temp;
            for (int i = 0; i < 90; i++) {
                GL11.glVertex2f(x, y);

                temp = x;
                x = cos * x - sin * y;
                y = sin * temp + cos * y;
            }

            GL11.glEnd();
        }
        GL11.glLineWidth(1);
        GL11.glPopMatrix();

        super.drawCircles();

    }

    @Override
    public HashMap<CelestialBody, Matrix4f> drawCelestialBodies(final Matrix4f worldMatrix) {
        final HashMap<CelestialBody, Matrix4f> result = super.drawCelestialBodies(worldMatrix);
        final FloatBuffer fb = BufferUtils.createFloatBuffer(16 * Float.SIZE);

        /*
         * protected CelestialBody lastSelectedBodyMS; protected float mothershipsOrbit; protected int numMotherships;
         * protected float mothershipPhase;
         */

        if (this.selectedBody != null) {
            final Matrix4f worldMatrix0 = new Matrix4f(worldMatrix);

            CelestialBody renderShipsAround = this.getBodyToRenderMothershipsAround();

            if (renderShipsAround != null) {

                final List<Mothership> msList = TickHandlerServer.mothershipData
                        .getMothershipsForParent(renderShipsAround);

                if (this.selectionCount > 0 && renderShipsAround instanceof Moon
                        && this.selectedBody instanceof Mothership) {
                    this._workaroundDrawMoon(worldMatrix0, (Moon) renderShipsAround, fb, result);
                }

                for (final Mothership ms : msList) {
                    GL11.glPushMatrix();
                    final Matrix4f worldMatrix1 = new Matrix4f(worldMatrix0);
                    Matrix4f.translate(this.getCelestialBodyPosition(ms), worldMatrix1, worldMatrix1);

                    Matrix4f worldMatrix2 = new Matrix4f();
                    Matrix4f.rotate((float) Math.toRadians(45), new Vector3f(0, 0, 1), worldMatrix2, worldMatrix2);
                    Matrix4f.rotate((float) Math.toRadians(-55), new Vector3f(1, 0, 0), worldMatrix2, worldMatrix2);
                    Matrix4f.scale(new Vector3f(0.25F, 0.25F, 1.0F), worldMatrix2, worldMatrix2);
                    worldMatrix2 = Matrix4f.mul(worldMatrix1, worldMatrix2, worldMatrix2);

                    fb.rewind();
                    worldMatrix2.store(fb);
                    fb.flip();
                    GL11.glMultMatrix(fb);

                    final CelestialBodyRenderEvent.Pre preEvent = new CelestialBodyRenderEvent.Pre(
                            ms,
                            ms.getBodyIcon(),
                            8);
                    MinecraftForge.EVENT_BUS.post(preEvent);

                    GL11.glColor4f(1, 1, 1, 1);
                    if (preEvent.celestialBodyTexture != null) {
                        this.mc.renderEngine.bindTexture(preEvent.celestialBodyTexture);
                    }

                    if (!preEvent.isCanceled()) {
                        final int size = getWidthForMothershipStatic(ms);
                        this.drawTexturedModalRect(
                                -size / 2,
                                -size / 2,
                                size,
                                size,
                                0,
                                0,
                                preEvent.textureSize,
                                preEvent.textureSize,
                                false,
                                false,
                                preEvent.textureSize,
                                preEvent.textureSize);
                        result.put(ms, worldMatrix1);
                    }

                    final CelestialBodyRenderEvent.Post postEvent = new CelestialBodyRenderEvent.Post(ms);
                    MinecraftForge.EVENT_BUS.post(postEvent);
                    fb.clear();
                    GL11.glPopMatrix();

                }
            }
        }

        return result;
    }

    public static int getWidthForMothershipStatic(final Mothership celestialBody) {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiShuttleSelection
                && (celestialBody != ((GuiShuttleSelection) Minecraft.getMinecraft().currentScreen).selectedBody
                        || ((GuiShuttleSelection) Minecraft.getMinecraft().currentScreen).selectionCount != 1)) {
            return 4;
        }
        return 6;
    }

    public void mothershipListUpdated() {
        // remove all the ships from the tick list
        this.celestialBodyTicks.entrySet().removeIf(entry -> entry.getKey() instanceof Mothership);
        // fill the new data in
        final MothershipWorldData msData = TickHandlerServer.mothershipData;
        for (final Mothership ms : msData.getMotherships().values()) {
            this.celestialBodyTicks.put(ms, 0);
        }

        this.updateNumPlayerMotherships();
    }

    public void mothershipCreationFailed() {}

    public void newMothershipCreated(final Mothership ship) {
        this.celestialBodyTicks.put(ship, 0);
        this.shuttlePossibleBodies.add(ship);
        this.updateNumPlayerMotherships();
    }

    public void mothershipPositionChanged(final Mothership ship) {
        // check if the ship just arrived or left
        if (ship.isInTransit()) {
            // left
            // check if it even affects me
            // apparently it did. do more stuff
            if (this.shuttlePossibleBodies.remove(ship) && this.selectedBody.equals(ship)) {
                this.unselectCelestialBody();
            }
        } else // arrived
               // I think it SHOULD be in celestialBodyTicks already...
            if (!this.shuttlePossibleBodies.contains(ship)) {
                this.shuttlePossibleBodies.add(ship);
            }
    }

    @Override
    protected Vector3f getCelestialBodyPosition(final CelestialBody cBody) {
        if (cBody instanceof Mothership) {
            // failsafe against a possible race condition
            int cBodyTicks = 0;
            if (this.celestialBodyTicks.get(cBody) != null) {

                cBodyTicks = this.celestialBodyTicks.get(cBody);
            }
            final float timeScale = 2.0F;
            float distanceFromCenter = this.getScale(cBody);
            final float orbitTime = 1 / 0.01F;

            final CelestialBody msParent = ((Mothership) cBody).getParent();
            if (msParent instanceof Star) {
                distanceFromCenter *= 3;
            }

            final Vector3f cBodyPos = new Vector3f(
                    (float) Math.sin(cBodyTicks / (timeScale * orbitTime) + cBody.getPhaseShift()) * distanceFromCenter,
                    (float) Math.cos(cBodyTicks / (timeScale * orbitTime) + cBody.getPhaseShift()) * distanceFromCenter,
                    0);

            final Vector3f parentVec = this.getCelestialBodyPosition(((Mothership) cBody).getParent());
            return Vector3f.add(cBodyPos, parentVec, null);
        }

        return super.getCelestialBodyPosition(cBody);
    }

    protected void updatePlayerParent() {
        CelestialBody body = ShuttleTeleportHelper
                .getCelestialBodyForDimensionID(this.mc.thePlayer.worldObj.provider.dimensionId);
        if (body instanceof Mothership) {
            body = ((Mothership) body).getParent();
        }
        this.playerParent = body;
    }

    protected void updateNumPlayerMotherships() {
        this.numPlayersMotherships = TickHandlerServer.mothershipData.getNumMothershipsForPlayer(this.mc.thePlayer);
    }

    @Override
    protected Vector2f getTranslationAdvanced(final float partialTicks) {
        /*
         * if (this.selectedBody instanceof Planet && this.lastSelectedBody instanceof IChildBody && ((IChildBody)
         * this.lastSelectedBody).getParentPlanet() == this.selectedBody) { Vector3f posVec =
         * this.getCelestialBodyPosition(this.selectedBody); return new Vector2f(posVec.x, posVec.y); }
         */
        if (this.selectedBody != null) {
            if (this.selectedBody instanceof Mothership) {
                final CelestialBody parent = ((Mothership) this.selectedBody).getParent();
                Vector3f result;
                if (parent instanceof IChildBody) {
                    result = this.getCelestialBodyPosition(((IChildBody) parent).getParentPlanet());
                    return new Vector2f(result.x, result.y);
                }
                result = this.getCelestialBodyPosition(parent);
                return new Vector2f(result.x, result.y);
            }
            if (this.selectedBody instanceof Planet && this.lastSelectedBody instanceof Mothership lastSelectedShip
                    && lastSelectedShip.getParent() == this.selectedBody) {
                final Vector3f posVec = this.getCelestialBodyPosition(this.selectedBody);
                return new Vector2f(posVec.x, posVec.y);
            }
        }
        return super.getTranslationAdvanced(partialTicks);
    }

    public void selectAndZoom(final CelestialBody target) {
        this.lastSelectedBody = this.selectedBody;
        this.selectedBody = target;
        if (this.lastSelectedBody instanceof IChildBody) {
            this.selectionCount = 1;
        } else {
            this.selectionCount = 2;
        }
        this.preSelectZoom = this.zoom;
        this.preSelectPosition = this.position;
        this.ticksSinceSelection = 0;
    }

    public void selectAndZoomNextTick(final CelestialBody target) {
        this.nextSelectedBody = target;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.isMessageShown) {
            if (this.messageButtonBox.isWithin(mouseX, mouseY)) {
                this.hideMessage();
            }
            return;
        }
        // hackfix for mothership parent selection
        final CelestialBody prevSelection = this.selectedBody;
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (prevSelection instanceof Mothership && this.selectedBody != prevSelection) {
            // not sure why, but...
            if (prevSelection instanceof IChildBody) {
                this.selectionCount = 1;
            } else {
                this.selectionCount = 2;
            }
            this.lastSelectedBody = prevSelection;
            this.preSelectZoom = this.zoom;
            this.preSelectPosition = this.position;
            this.ticksSinceSelection = 0;
            this.doneZooming = false;
        }

    }

    @Override
    public void updateScreen() {
        if (this.nextSelectedBody != null) {
            this.selectAndZoom(this.nextSelectedBody);
            this.nextSelectedBody = null;
        }
        super.updateScreen();
    }

    public void showMessageBox(final String caption, final String text) {
        this.isMessageShown = true;
        this.messageCaption = caption;
        this.messageText = text;
    }

    public void hideMessage() {
        this.isMessageShown = false;
    }

}
