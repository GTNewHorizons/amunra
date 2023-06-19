package de.katzenpapst.amunra.client.gui.schematic;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.inventory.schematic.ContainerSchematicShuttle;
import de.katzenpapst.amunra.item.ARItems;
import micdoodle8.mods.galacticraft.api.recipe.ISchematicResultPage;
import micdoodle8.mods.galacticraft.api.recipe.SchematicRegistry;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class GuiSchematicShuttle extends GuiContainer implements ISchematicResultPage {

    // for now, copypasta from the t2 rocket
    /*
     * So it seems like the relevant data is: - the texture - the ySize - the item to build - the Container Should be
     * easy enough to abstract that
     */
    public static final ResourceLocation shuttleSchematicTexture = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/gui/crafting_shuttle_rocket.png");

    private int pageIndex;

    public GuiSchematicShuttle(final InventoryPlayer par1InventoryPlayer, final int x, final int y, final int z) {
        super(new ContainerSchematicShuttle(par1InventoryPlayer, x, y, z));
        this.ySize = 220;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.buttonList.add(
                new GuiButton(
                        0,
                        this.width / 2 - 130,
                        this.height / 2 - 30 + 27 - 12,
                        40,
                        20,
                        GCCoreUtil.translate("gui.button.back.name")));
        this.buttonList.add(
                new GuiButton(
                        1,
                        this.width / 2 - 130,
                        this.height / 2 - 30 + 27 + 12,
                        40,
                        20,
                        GCCoreUtil.translate("gui.button.next.name")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            switch (button.id) {
                case 0 -> SchematicRegistry.flipToLastPage(this.pageIndex);
                case 1 -> SchematicRegistry.flipToNextPage(this.pageIndex);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString(
                ARItems.shuttleItem.getItemStackDisplayName(new ItemStack(ARItems.shuttleItem, 1, 0)),
                7,
                7,
                0x404040);
        this.fontRendererObj.drawString(GCCoreUtil.translate("container.inventory"), 8, 127, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(shuttleSchematicTexture);
        final int x = (this.width - this.xSize) / 2;
        final int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public void setPageIndex(final int index) {
        this.pageIndex = index;
    }

}
