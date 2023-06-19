package de.katzenpapst.amunra.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.helper.GuiHelper;
import de.katzenpapst.amunra.inventory.ContainerShuttleDock;
import de.katzenpapst.amunra.item.ItemShuttle;
import de.katzenpapst.amunra.tile.TileEntityShuttleDock;
import de.katzenpapst.amunra.tile.TileEntityShuttleDock.DockOperation;
import micdoodle8.mods.galacticraft.core.client.gui.container.GuiContainerGC;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementInfoRegion;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class GuiShuttleDock extends GuiContainerGC {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/gui/dock-gui.png");

    private final TileEntityShuttleDock tile;

    private GuiButton buttonGetShuttle;
    private GuiButton buttonPutShuttle;
    private GuiButton buttonEnterShuttle;

    private boolean isObstructed = true;

    private GuiElementInfoRegion shuttleInfoRegion;

    public GuiShuttleDock(final InventoryPlayer player, final TileEntityShuttleDock tile) {
        super(new ContainerShuttleDock(player, tile));
        this.tile = tile;
        this.ySize = 210;
        this.xSize = 176;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                this.tile.performDockOperationClient(DockOperation.GET_SHUTTLE);
            case 1:
                this.tile.performDockOperationClient(DockOperation.MOUNT_SHUTTLE);
            case 2:
                this.tile.performDockOperationClient(DockOperation.DEPLOY_SHUTTLE);
                break;
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        this.isObstructed = this.tile.isObstructed();

        this.shuttleInfoRegion = new GuiElementInfoRegion(
                (this.width - this.xSize) / 2 + 23,
                (this.height - this.ySize) / 2 + 57,
                18,
                27,
                new ArrayList<>(),
                this.width,
                this.height,
                this);

        final List<String> descrStrings = new ArrayList<>();
        descrStrings.add("foobar");

        this.shuttleInfoRegion.tooltipStrings = descrStrings;
        this.shuttleInfoRegion.xPosition = (this.width - this.xSize) / 2 + 23;
        this.shuttleInfoRegion.yPosition = (this.height - this.ySize) / 2 + 57;
        this.shuttleInfoRegion.parentWidth = this.width;
        this.shuttleInfoRegion.parentHeight = this.height;
        this.infoRegions.add(this.shuttleInfoRegion);

        this.buttonGetShuttle = new GuiButton(
                0,
                (this.width - this.xSize) / 2 + 52,
                (this.height - this.ySize) / 2 + 20,
                72,
                20,
                GCCoreUtil.translate("gui.message.dock.action.get"));

        this.buttonEnterShuttle = new GuiButton(
                1,
                (this.width - this.xSize) / 2 + 52,
                (this.height - this.ySize) / 2 + 56,
                72,
                20,
                GCCoreUtil.translate("gui.message.dock.action.enter"));

        this.buttonPutShuttle = new GuiButton(
                2,
                (this.width - this.xSize) / 2 + 52,
                (this.height - this.ySize) / 2 + 93,
                72,
                20,
                GCCoreUtil.translate("gui.message.dock.action.deploy"));

        this.buttonList.add(this.buttonGetShuttle);
        this.buttonList.add(this.buttonEnterShuttle);
        this.buttonList.add(this.buttonPutShuttle);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int par1, final int par2) {
        final String displayString = this.tile.getInventoryName();
        this.fontRendererObj.drawString(
                displayString,
                this.xSize / 2 - this.fontRendererObj.getStringWidth(displayString) / 2,
                7,
                0x404040);
        this.fontRendererObj.drawString(GCCoreUtil.translate("container.inventory"), 8, this.ySize - 94, 0x404040);

        this.shuttleInfoRegion.tooltipStrings.clear();
        this.shuttleInfoRegion.tooltipStrings.addAll(this.getStatus());

        final ItemStack stack = this.tile.getStackInSlot(0);
        final boolean hasShuttle = this.tile.hasShuttle();
        boolean hasItem = false;
        if (stack != null) {
            hasItem = stack.stackSize > 0 && stack.getItem() instanceof ItemShuttle;
        }
        this.buttonGetShuttle.enabled = !hasItem && hasShuttle;
        this.buttonPutShuttle.enabled = hasItem && !hasShuttle && !this.isObstructed;

        this.buttonEnterShuttle.enabled = hasShuttle;

    }

    protected List<String> getStatus() {
        /*
         * gui.message.dock.status.obstructed=There are blocks in the way. Shuttles cannot dock here.
         * gui.message.dock.status.occupied=A shuttle is docked here. gui.message.dock.status.free=This dock is vacant.
         */
        if (this.tile.hasShuttle()) {
            return GCCoreUtil.translateWithSplit("gui.message.dock.status.occupied");
        }
        if (this.isObstructed) {
            return GuiHelper.translateWithSplitColor("gui.message.dock.status.obstructed", EnumColor.DARK_RED);
        }

        return GCCoreUtil.translateWithSplit("gui.message.dock.status.free");
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        final int xPos = (this.width - this.xSize) / 2;
        final int yPos = (this.height - this.ySize) / 2;
        this.mc.renderEngine.bindTexture(GUI_TEXTURE);
        final int xOffset = (this.width - this.xSize) / 2;
        final int yOffset = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(xOffset, yOffset, 0, 0, this.xSize, this.ySize);

        if (this.isObstructed) {
            this.drawTexturedModalRect(xPos + 24, yPos + 59, 176, 28, 16, 16);
        }

        if (this.tile.hasShuttle()) {
            this.drawTexturedModalRect(xPos + 23, yPos + 53, 176, 1, 18, 27);
        }
    }

}
