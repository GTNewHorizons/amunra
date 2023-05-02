package de.katzenpapst.amunra.client.gui.tabs;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.client.gui.GuiMothershipSettings;
import de.katzenpapst.amunra.client.gui.GuiMothershipSettings.IMothershipSettingsTab;
import de.katzenpapst.amunra.client.gui.elements.DynamicTexturedButton;
import de.katzenpapst.amunra.mothership.Mothership;
import de.katzenpapst.amunra.tile.TileEntityMothershipSettings;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementTextBox;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementTextBox.ITextBoxCallback;
import micdoodle8.mods.galacticraft.core.util.ColorUtil;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class TabMothershipCustom extends AbstractTab implements ITextBoxCallback, IMothershipSettingsTab {

    private final ResourceLocation icontexture = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/gui/mothership_icons/0.png");

    private final Mothership ship;

    /*
     * private GuiButton applyButton; private GuiButton resetButton;
     */
    private GuiButton texturesPrev;
    private GuiButton texturesNext;

    private int texButtonOffset = 0;

    private GuiElementTextBox nameField;

    // private String changedName;

    // private ResourceLocation changedIcon;

    private final DynamicTexturedButton[] textureButtons = new DynamicTexturedButton[6];

    protected List<ResourceLocation> mothershipTextures;

    public TabMothershipCustom(final TileEntityMothershipSettings tile, final GuiMothershipSettings parent, final Minecraft mc, final int width,
            final int height, final int xSize, final int ySize) {
        super(parent, mc, width, height, xSize, ySize);
        this.ship = tile.getMothership();
        this.mothershipTextures = AmunRa.instance.getPossibleMothershipTextures();

        // changedName = ship.getLocalizedName();
        // changedIcon = ship.getBodyIcon();
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float ticks) {
        for (final DynamicTexturedButton btn : this.textureButtons) {
            if (btn == null) continue;
            if (btn.getTexture().equals(this.ship.getBodyIcon())) {
                btn.setSelected(true);
            } else {
                btn.setSelected(false);
            }
        }

        super.drawScreen(mouseX, mouseY, ticks);
    }

    @Override
    protected void drawExtraScreenElements(final int mouseX, final int mouseY, final float ticks) {
        final int guiX = (this.width - this.xSize) / 2;
        final int guiY = (this.height - this.ySize) / 2;

        this.fontRendererObj.drawString(this.getTooltip(), guiX + 5, guiY + 5, 4210752);

        this.fontRendererObj
                .drawString(GCCoreUtil.translate("container.inventory"), guiX + 8, guiY + this.ySize - 94, 4210752);
    }

    @Override
    public void initGui() {

        this.buttonList.clear();
        final int guiX = (this.width - this.xSize) / 2;
        final int guiY = (this.height - this.ySize) / 2;

        // this.applyButton = new GuiButton(0, guiX + 120 - 50, guiY + 95, 48, 20,
        // GCCoreUtil.translate("gui.message.mothership.apply"));
        // this.resetButton = new GuiButton(1, guiX + 120, guiY + 95, 48, 20,
        // GCCoreUtil.translate("gui.message.mothership.reset"));

        // int id, ITextBoxCallback parentGui, int x, int y, int width, int height, String initialText, boolean
        // numericOnly, int maxLength, boolean centered
        this.nameField = new GuiElementTextBox(2, this, guiX + 4, guiY + 4 + 20, 168, 20, "", false, 14, true);

        this.texturesPrev = new GuiButton(3, guiX + 6, guiY + 26 + 20, 20, 20, GCCoreUtil.translate("<"));
        this.texturesNext = new GuiButton(4, guiX + 150, guiY + 26 + 20, 20, 20, GCCoreUtil.translate(">"));

        // testBtn = new DynamicTexturedButton(5, guiX + 6 + 20, guiY+26, 20, 20, mothershipTextures.get(0));

        // addButton(this.applyButton);
        // addButton(this.resetButton);
        this.addTextBox(this.nameField);
        this.addButton(this.texturesPrev);
        this.addButton(this.texturesNext);
        //
        this.initTextureButtons(5, guiX + 2, guiY + 20);

    }

    protected int initTextureButtons(final int startId, final int guiX, final int guiY) {
        int curId = startId;
        for (int i = 0; i < this.textureButtons.length; i++) {

            final DynamicTexturedButton btn = new DynamicTexturedButton(
                    curId,
                    guiX + 6 + 20 + 20 * i,
                    guiY + 26,
                    20,
                    20,
                    null);
            curId++;
            if (this.mothershipTextures.size() > i) {
                btn.setTexture(this.mothershipTextures.get(i));
            } else {
                btn.enabled = false;
            }
            // btn.setd
            this.buttonList.add(btn);
            this.textureButtons[i] = btn;
        }
        return curId;
    }

    @Override
    public void mothershipResponsePacketRecieved() {
        this.resetData();
        this.setGuiEnabled(true);
    }

    protected boolean isValidName(final String name) {
        return name != null && !name.trim().isEmpty();
    }

    @Override
    public boolean canPlayerEdit(final GuiElementTextBox textBox, final EntityPlayer player) {
        return true;
    }

    @Override
    public void onTextChanged(final GuiElementTextBox textBox, final String newText) {
        if (textBox.equals(this.nameField) && this.isValidName(newText) && !newText.equals(this.ship.getLocalizedName())) {
            this.ship.setLocalizedName(newText);
            ((GuiMothershipSettings) this.parent).sendMothershipSettingsPacket();
        }
    }

    @Override
    public String getInitialText(final GuiElementTextBox textBox) {
        if (textBox.equals(this.nameField)) {
            return this.ship.getLocalizedName();
        }
        return "";
    }

    @Override
    public int getTextColor(final GuiElementTextBox textBox) {
        return ColorUtil.to32BitColor(255, 20, 255, 20);
    }

    @Override
    public void onIntruderInteraction(final GuiElementTextBox textBox) {
        // TODO Auto-generated method stub
    }

    public void setGuiEnabled(final boolean set) {
        // applyButton.enabled = set;
        // resetButton.enabled = set;
        this.texturesPrev.enabled = set;
        this.texturesNext.enabled = set;
        for (final DynamicTexturedButton btn : this.textureButtons) {
            btn.enabled = set;
        }

        this.nameField.enabled = set;
    }

    public void resetData() {
        // this.changedIcon = this.tile.getMothership().getBodyIcon();
        // this.changedName = this.tile.getMothership().getLocalizedName();
    }

    @Override
    public boolean actionPerformed(final GuiButton btn) {
        /*
         * if(btn.equals(applyButton)) { NBTTagCompound nbt = new NBTTagCompound (); nbt.setString("name", changedName);
         * nbt.setString("bodyIcon", changedIcon.toString()); this.setGuiEnabled(false);
         * AmunRa.packetPipeline.sendToServer(new PacketSimpleAR(EnumSimplePacket.S_SET_MOTHERSHIP_SETTINGS,
         * ship.getID(), nbt)); return true; }
         */
        /*
         * if(btn.equals(resetButton)) { resetData(); return true; }
         */
        if (btn.equals(this.texturesNext) && this.texButtonOffset + this.textureButtons.length < this.mothershipTextures.size()) {
            this.texButtonOffset++;
            this.updateTextureButtons();
            return true;
        }
        if (btn.equals(this.texturesPrev) && this.texButtonOffset > 0) {
            this.texButtonOffset--;
            this.updateTextureButtons();
            return true;
        }
        for (final DynamicTexturedButton texButton : this.textureButtons) {
            if (btn.equals(texButton)) {
                this.ship.setBodyIcon(texButton.getTexture());
                ((GuiMothershipSettings) this.parent).sendMothershipSettingsPacket();
                // changedIcon = texButton.getTexture();

                return true;
            }
        }
        return false;
    }

    @Override
    public void onTabActivated() {
        this.resetData();
    }

    protected void updateTextureButtons() {

        for (int i = 0; i < this.textureButtons.length; i++) {

            final int textureOffset = i + this.texButtonOffset;
            if (textureOffset < 0 || textureOffset >= this.mothershipTextures.size()) {
                this.textureButtons[i].enabled = false;
            } else {
                this.textureButtons[i].enabled = true;
                this.textureButtons[i].setTexture(this.mothershipTextures.get(textureOffset));
            }
        }
    }

    @Override
    public String getTooltip() {
        return GCCoreUtil.translate("tile.mothershipSettings.customize");
    }

    @Override
    public ResourceLocation getIcon() {
        return this.icontexture;
    }

    @Override
    public void mothershipOperationFailed(final String message) {
        // TODO Auto-generated method stub

    }
}
