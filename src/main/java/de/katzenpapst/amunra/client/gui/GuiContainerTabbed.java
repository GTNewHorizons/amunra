package de.katzenpapst.amunra.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;

import de.katzenpapst.amunra.client.gui.elements.TabButton;
import de.katzenpapst.amunra.client.gui.tabs.AbstractTab;
import micdoodle8.mods.galacticraft.core.client.gui.container.GuiContainerGC;

abstract public class GuiContainerTabbed extends GuiContainerGC {

    protected final List<AbstractTab> tabList = new ArrayList<>();

    protected final List<TabButton> tabButtons = new ArrayList<>();

    protected int activeTab = -1;

    public static final int TAB_BTN_OFFSET = 10000;

    public GuiContainerTabbed(final Container container) {
        super(container);
    }

    @Override
    public void initGui() {
        this.tabList.clear();
        this.tabButtons.clear();
        super.initGui();
    }

    public int addTab(final AbstractTab tab) {
        if (this.tabList.add(tab)) {
            tab.initGui();

            final int newIndex = this.tabList.size() - 1;

            final int guiX = (this.width - this.xSize) / 2;
            final int guiY = (this.height - this.ySize) / 2;

            // add button
            final TabButton test = new TabButton(
                    TAB_BTN_OFFSET + newIndex,
                    guiX - 27,
                    guiY + 6 + newIndex * 28,
                    tab.getTooltip(),
                    tab.getTooltipDescription(),
                    tab.getIcon());
            this.buttonList.add(test);
            this.tabButtons.add(test);

            this.setActiveTab(0);

            return newIndex;
        }
        return -1;
    }

    public AbstractTab getTab(final int index) {
        return this.tabList.get(index);
    }

    public void setActiveTab(final int newIndex) {
        if (newIndex >= 0 && newIndex < this.tabList.size() && newIndex != this.activeTab) {
            this.activeTab = newIndex;
            final int btnIndex = TAB_BTN_OFFSET + newIndex;
            this.getActiveTab().onTabActivated();
            for (final TabButton btn : this.tabButtons) {
                btn.isActive = btn.id == btnIndex;
            }
        }
    }

    public int getActiveTabIndex() {
        return this.activeTab;
    }

    public AbstractTab getActiveTab() {
        return this.tabList.get(this.activeTab);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        // getActiveTab().
        this.getActiveTab().drawScreen(mouseX, mouseY, partialTicks);

        for (final TabButton tb : this.tabButtons) {
            tb.drawTooltip(mouseX, mouseY);
        }
    }

    protected void drawTabs() {}

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.id >= TAB_BTN_OFFSET) {
            final int index = btn.id - TAB_BTN_OFFSET;
            this.setActiveTab(index);
        }
        // TODO handle my own stuff first
        this.getActiveTab().actionPerformed(btn);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (!this.getActiveTab().keyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void handleMouseInput() {
        this.getActiveTab().handleMouseInput();
        super.handleMouseInput();
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
        this.getActiveTab().setWorldAndResolution(mc, width, height, this.xSize, this.ySize);
    }
}
