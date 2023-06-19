package de.katzenpapst.amunra.client.gui.tabs;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import micdoodle8.mods.galacticraft.core.client.gui.container.GuiContainerGC;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementTextBox;

abstract public class AbstractTab {

    protected final List<GuiButton> buttonList = new ArrayList<>();

    protected final List<GuiLabel> labelList = new ArrayList<>();

    protected final List<GuiElementTextBox> textBoxList = new ArrayList<>();

    /** Reference to the Minecraft object. */
    protected Minecraft mc;
    /** The width of the screen object. */
    protected int width;
    /** The height of the screen object. */
    protected int height;

    /** The width of the window itself */
    protected int xSize;
    /** The height of the window itself */
    protected int ySize;

    private int field_146298_h;

    private GuiButton selectedButton;
    private int eventButton;
    private long lastMouseEvent;

    private Slot theSlot;

    protected FontRenderer fontRendererObj;

    protected GuiContainerGC parent;

    public AbstractTab(final GuiContainerGC parent, final Minecraft mc, final int width, final int height,
            final int xSize, final int ySize) {
        this.setWorldAndResolution(mc, width, height, xSize, ySize);
        this.parent = parent;
    }

    abstract public void initGui();

    public void setWorldAndResolution(final Minecraft mc, final int width, final int height, final int xSize,
            final int ySize) {
        this.mc = mc;
        this.fontRendererObj = mc.fontRenderer;
        this.width = width;
        this.height = height;
        this.xSize = xSize;
        this.ySize = ySize;
    }

    public void drawScreen(final int mouseX, final int mouseY, final float ticks) {
        RenderHelper.disableStandardItemLighting();

        for (final GuiButton box : this.buttonList) {
            box.drawButton(this.mc, mouseX, mouseY);
        }

        for (final GuiLabel box : this.labelList) {
            box.func_146159_a(this.mc, mouseX, mouseY);
        }

        this.drawExtraScreenElements(mouseX, mouseY, ticks);
        RenderHelper.enableGUIStandardItemLighting();
    }

    protected void drawExtraScreenElements(final int mouseX, final int mouseY, final float ticks) {

    }

    public void addButton(final GuiButton btn) {
        this.buttonList.add(btn);
    }

    public void addLabel(final GuiLabel label) {
        this.labelList.add(label);
    }

    public void addTextBox(final GuiElementTextBox box) {
        this.textBoxList.add(box);
        this.buttonList.add(box);
    }

    public boolean actionPerformed(final GuiButton btn) {
        return false;
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() {
        final int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        final int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        final int eventButton = Mouse.getEventButton();

        if (Mouse.getEventButtonState()) {
            if (this.mc.gameSettings.touchscreen && this.field_146298_h++ > 0) {
                return;
            }

            this.eventButton = eventButton;
            this.lastMouseEvent = Minecraft.getSystemTime();
            this.mouseClicked(mouseX, mouseY, this.eventButton);
        } else if (eventButton != -1) {
            if (this.mc.gameSettings.touchscreen && --this.field_146298_h > 0) {
                return;
            }

            this.eventButton = -1;
            this.mouseMovedOrUp(mouseX, mouseY, eventButton);
        } else if (this.eventButton != -1 && this.lastMouseEvent > 0L) {
            final long l = Minecraft.getSystemTime() - this.lastMouseEvent;
            this.mouseClickMove(mouseX, mouseY, this.eventButton, l);
        }
    }

    protected void mouseClickMove(final int mouseX, final int mouseY, final int clickedMouseButton,
            final long timeSinceLastClick) {}

    protected void mouseMovedOrUp(final int mouseX, final int mouseY, final int state) {
        if (this.selectedButton != null && state == 0) {
            this.selectedButton.mouseReleased(mouseX, mouseY);
            this.selectedButton = null;
        }
    }

    /**
     * Handles keyboard input. / public void handleKeyboardInput() { if (Keyboard.getEventKeyState()) {
     * this.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey()); } }
     */

    /**
     * This function is what controls the hotbar shortcut check when you press a number key when hovering a stack.
     */
    protected boolean checkHotbarKeys(final int keyCode) {
        if (this.mc.thePlayer.inventory.getItemStack() == null && this.theSlot != null) {
            for (int j = 0; j < 9; ++j) {
                if (keyCode == this.mc.gameSettings.keyBindsHotbar[j].getKeyCode()) {
                    this.handleMouseClick(this.theSlot, this.theSlot.slotNumber, j, 2);
                    return true;
                }
            }
        }

        return false;
    }

    protected void handleMouseClick(final Slot slotIn, final int slotId, final int clickedButton,
            final int clickType) {}

    public boolean keyTyped(final char keyChar, final int keyID) {
        if (keyID != Keyboard.KEY_ESCAPE) {
            // do the fields
            for (final GuiElementTextBox box : this.textBoxList) {
                if (box.keyTyped(keyChar, keyID)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Called when the mouse is clicked.
     */
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        if (mouseButton == 0) {
            for (GuiButton guibutton : this.buttonList) {
                if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
                    final ActionPerformedEvent.Pre event = new ActionPerformedEvent.Pre(
                            this.parent,
                            guibutton,
                            this.buttonList);
                    if (MinecraftForge.EVENT_BUS.post(event)) break;
                    this.selectedButton = event.button;
                    event.button.func_146113_a(this.mc.getSoundHandler());
                    this.actionPerformed(event.button);
                    if (this.parent.equals(this.mc.currentScreen)) MinecraftForge.EVENT_BUS
                            .post(new ActionPerformedEvent.Post(this.parent, event.button, this.buttonList));
                }
            }
        }
    }

    public void onTabActivated() {}

    abstract public ResourceLocation getIcon();

    abstract public String getTooltip();

    public String getTooltipDescription() {
        return null;
    }
}
