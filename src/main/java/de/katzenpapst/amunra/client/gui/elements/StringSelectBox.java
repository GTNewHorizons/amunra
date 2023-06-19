package de.katzenpapst.amunra.client.gui.elements;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import de.katzenpapst.amunra.AmunRa;
import micdoodle8.mods.galacticraft.core.client.gui.screen.SmallFontRenderer;

public class StringSelectBox extends GuiButton {

    protected static final ResourceLocation textures = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/gui/gui-extra.png");

    protected List<String> strings = new ArrayList<>();

    protected int selectedStringIndex = -1;

    protected final int textSize = 8;

    protected int scrollOffset = 0;

    protected int maxLines = 0;

    protected final ISelectBoxCallback parent;

    public SmallFontRenderer font;

    public StringSelectBox(final ISelectBoxCallback parent, final int id, final int xPos, final int yPos,
            final int width, final int height) {
        super(id, xPos, yPos, width, height, "");

        this.parent = parent;

        final Minecraft mc = FMLClientHandler.instance().getClient();

        this.font = new SmallFontRenderer(
                mc.gameSettings,
                new ResourceLocation("textures/font/ascii.png"),
                mc.renderEngine,
                false);

        // find out how many lines we can have
        this.maxLines = height / this.textSize;
    }

    public void clear() {
        this.strings.clear();
        this.clearSelection();
    }

    public void addString(final String str) {
        this.strings.add(str);
    }

    public void setSelection(final int selection) {
        if (selection >= 0 && selection < this.strings.size() && this.selectedStringIndex != selection) {
            this.selectedStringIndex = selection;
            this.parent.onSelectionChanged(this, this.selectedStringIndex);
        }
    }

    public void clearSelection() {
        if (this.selectedStringIndex != -1) {
            this.selectedStringIndex = -1;
            this.parent.onSelectionChanged(this, this.selectedStringIndex);
        }
    }

    public boolean hasSelection() {
        return this.selectedStringIndex != -1;
    }

    public int getSelectedStringIndex() {
        return this.selectedStringIndex;
    }

    public String getSelectedString() {
        if (this.selectedStringIndex >= 0) {
            return this.strings.get(this.selectedStringIndex);
        }
        return null;
    }

    public void deleteStringAt(final int index) {
        this.strings.remove(index);
        this.clearSelection();
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            final int color = 0xFFa6a6a6;
            // outer box
            this.drawGradientRect(
                    this.xPosition,
                    this.yPosition,
                    this.xPosition + this.width,
                    this.yPosition + this.height,
                    color,
                    color);
            final int colorBlack = 0xFF000000;
            // inner box
            this.drawGradientRect(
                    this.xPosition + 1,
                    this.yPosition + 1,
                    this.xPosition + this.width - 1,
                    this.yPosition + this.height - 1,
                    colorBlack,
                    colorBlack);

            // strings
            final int colorGreen = 0xFF00FF00;
            // selectedStringIndex = 1;
            final int colorSelection = 0x99008AFF;
            final int displayLines = Math.min(this.strings.size(), this.maxLines);
            for (int i = 0; i < displayLines; i++) {
                final int curYoffset = i * this.textSize;
                final int actualIndex = i + this.scrollOffset;

                if (actualIndex == this.selectedStringIndex) {
                    this.drawGradientRect(
                            this.xPosition + 1,
                            curYoffset + this.yPosition + 1,
                            this.xPosition + this.width - 1,
                            curYoffset + this.yPosition + 1 + this.textSize,
                            colorSelection,
                            colorSelection);
                }

                this.font.drawStringWithShadow(
                        this.strings.get(actualIndex),
                        this.xPosition + 2,
                        curYoffset + this.yPosition,
                        colorGreen);
            }

            if (this.maxLines < this.strings.size()) {
                // draw arrows
                mc.getTextureManager().bindTexture(textures);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                if (this.scrollOffset == 0) {
                    this.drawTexturedModalRect(
                            this.xPosition + this.width - 1 - 8,
                            this.yPosition + 1,
                            8,
                            84 + 8,
                            8,
                            8);
                } else {
                    this.drawTexturedModalRect(this.xPosition + this.width - 1 - 8, this.yPosition + 1, 8, 84, 8, 8);
                }
                if (this.scrollOffset >= this.strings.size() - this.maxLines) {
                    this.drawTexturedModalRect(
                            this.xPosition + this.width - 1 - 8,
                            this.yPosition + this.height - 1 - 8,
                            0,
                            84 + 8,
                            8,
                            8);
                } else {
                    this.drawTexturedModalRect(
                            this.xPosition + this.width - 1 - 8,
                            this.yPosition + this.height - 1 - 8,
                            0,
                            84,
                            8,
                            8);
                }
            }

        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (!super.mousePressed(mc, mouseX, mouseY)) {
            return false;
        }
        // otherwise do stuff
        // first, try the arrows
        if (this.maxLines < this.strings.size()) {
            final int btnMaxX = this.xPosition + this.width - 1;
            final int btnMinX = this.xPosition + this.width - 1 - 8;

            if (btnMinX <= mouseX && mouseX <= btnMaxX) {

                int btnTopMinY = this.yPosition + 1;
                int btnTopMaxY = this.yPosition + 8 + 1;

                if (btnTopMinY <= mouseY && mouseY <= btnTopMaxY) {
                    // top button
                    if (this.scrollOffset > 0) {
                        this.scrollOffset--;
                    }
                    return true;
                }

                btnTopMinY = this.yPosition + this.height - 1 - 8;
                btnTopMaxY = this.yPosition + this.height - 1;
                if (btnTopMinY <= mouseY && mouseY <= btnTopMaxY) {
                    // bottom button
                    if (this.scrollOffset < this.strings.size() - this.maxLines) {
                        this.scrollOffset++;
                    }
                    return true;
                }

                // buttons are 8x8
                // top right
            }

        }

        // are we changing selection?
        final int relativeY = mouseY - this.yPosition;
        final int lineClicked = relativeY / this.textSize;
        if (lineClicked < this.strings.size()) {
            final int newIndex = lineClicked + this.scrollOffset;
            this.setSelection(newIndex);
        }
        return true;
    }

    public interface ISelectBoxCallback {

        void onSelectionChanged(StringSelectBox box, int selection);
    }

}
