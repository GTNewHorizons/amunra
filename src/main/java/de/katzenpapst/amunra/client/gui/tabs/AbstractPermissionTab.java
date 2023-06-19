package de.katzenpapst.amunra.client.gui.tabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

import de.katzenpapst.amunra.client.gui.GuiMothershipSettings;
import de.katzenpapst.amunra.client.gui.GuiMothershipSettings.IMothershipSettingsTab;
import de.katzenpapst.amunra.client.gui.elements.StringSelectBox;
import de.katzenpapst.amunra.client.gui.elements.StringSelectBox.ISelectBoxCallback;
import de.katzenpapst.amunra.helper.PlayerID;
import de.katzenpapst.amunra.mothership.Mothership;
import de.katzenpapst.amunra.tile.TileEntityMothershipSettings;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementDropdown;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementDropdown.IDropboxCallback;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementTextBox;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementTextBox.ITextBoxCallback;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

abstract public class AbstractPermissionTab extends AbstractTab
        implements IDropboxCallback, ITextBoxCallback, ISelectBoxCallback, IMothershipSettingsTab {

    protected final TileEntityMothershipSettings tile;

    protected GuiElementDropdown modeDropdown;
    protected GuiElementTextBox textBoxUsername;
    protected StringSelectBox selectBox;

    protected GuiButton addBtn;
    protected GuiButton rmBtn;
    protected final List<PlayerID> playerIdList = new ArrayList<>();

    protected final Map<Mothership.PermissionMode, String> permissionModeMap = new HashMap<>();

    protected String error = "";
    protected float errorTime = 0;

    public AbstractPermissionTab(final TileEntityMothershipSettings tile, final GuiMothershipSettings parent,
            final Minecraft mc, final int width, final int height, final int xSize, final int ySize) {
        super(parent, mc, width, height, xSize, ySize);
        this.tile = tile;

        this.permissionModeMap.put(
                Mothership.PermissionMode.ALL,
                GCCoreUtil.translate("tile.mothershipSettings.permission.allowAll"));
        this.permissionModeMap.put(
                Mothership.PermissionMode.NONE,
                GCCoreUtil.translate("tile.mothershipSettings.permission.allowNone"));
        this.permissionModeMap.put(
                Mothership.PermissionMode.WHITELIST,
                GCCoreUtil.translate("tile.mothershipSettings.permission.whitelist"));
        this.permissionModeMap.put(
                Mothership.PermissionMode.BLACKLIST,
                GCCoreUtil.translate("tile.mothershipSettings.permission.blacklist"));
    }

    protected abstract void addUsername(Mothership mothership, String userName);

    protected abstract void removeUsernameFromList(int position);

    @Override
    public boolean actionPerformed(final GuiButton btn) {
        if (btn == this.addBtn) {
            this.addUsername(this.tile.getMothership(), this.textBoxUsername.text);
            this.textBoxUsername.text = "";
            this.addBtn.enabled = false;
            return true;
        }
        if (btn == this.rmBtn) {
            final int selection = this.selectBox.getSelectedStringIndex();
            if (selection != -1) {
                this.removeUsernameFromList(selection);
                this.selectBox.clearSelection();
                this.applyData();
            }
            return true;
        }
        return false;
    }

    abstract public void resetData();

    @Override
    public void mothershipResponsePacketRecieved() {
        this.resetData();
    }

    protected String[] getDropdownOptions() {
        final int num = Mothership.PermissionMode.values().length;
        final String[] result = new String[num];

        for (int i = 0; i < num; i++) {
            result[i] = this.permissionModeMap.get(Mothership.PermissionMode.values()[i]);
        }

        return result;
    }

    protected void applyData() {
        final GuiMothershipSettings actualParent = (GuiMothershipSettings) this.parent;
        actualParent.sendMothershipSettingsPacket();
    }

    @Override
    public void initGui() {

        final int guiX = (this.width - this.xSize) / 2;
        final int guiY = (this.height - this.ySize) / 2;

        this.modeDropdown = new GuiElementDropdown(1, this, guiX + 90, guiY + 14, this.getDropdownOptions());

        this.textBoxUsername = new GuiElementTextBox(2, this, guiX + 5, guiY + 30, 95, 20, "", false, 50, false);

        this.selectBox = new StringSelectBox(this, 3, guiX + 5, guiY + 50, 95, 50);

        this.addBtn = new GuiButton(
                4,
                guiX + 100,
                guiY + 30,
                70,
                20,
                GCCoreUtil.translate("tile.mothershipSettings.permission.addUser"));
        this.rmBtn = new GuiButton(
                5,
                guiX + 100,
                guiY + 50,
                70,
                20,
                GCCoreUtil.translate("tile.mothershipSettings.permission.removeUser"));
        this.rmBtn.enabled = false;
        this.addBtn.enabled = false;

        this.addButton(this.modeDropdown);
        this.addButton(this.selectBox);
        this.addButton(this.addBtn);
        this.addButton(this.rmBtn);
        this.addTextBox(this.textBoxUsername);

        this.resetData();
    }

    @Override
    public void onTabActivated() {
        this.resetData();
    }

    @Override
    protected void drawExtraScreenElements(final int mouseX, final int mouseY, final float ticks) {
        final int guiX = (this.width - this.xSize) / 2;
        final int guiY = (this.height - this.ySize) / 2;

        this.fontRendererObj.drawString(this.getTooltip(), guiX + 5, guiY + 5, 0x404040);

        this.fontRendererObj.drawString(
                GCCoreUtil.translate("tile.mothershipSettings.permission.allowLabel") + ":",
                guiX + 9,
                guiY + 16,
                0x404040);

        if (this.errorTime > 0) {
            this.fontRendererObj.drawSplitString(this.error, guiX + 102, guiY + 80, 70, 0x404040);
            this.errorTime -= ticks;
        }

        this.fontRendererObj
                .drawString(GCCoreUtil.translate("container.inventory"), guiX + 8, guiY + this.ySize - 94, 0x404040);
    }

    // DROPDOWN
    @Override
    public boolean canBeClickedBy(final GuiElementDropdown dropdown, final EntityPlayer player) {
        return true;
    }

    @Override
    abstract public int getInitialSelection(GuiElementDropdown dropdown);

    @Override
    public void onIntruderInteraction() {}

    // TEXTBOX
    @Override
    public boolean canPlayerEdit(final GuiElementTextBox textBox, final EntityPlayer player) {
        return true;
    }

    @Override
    public void onTextChanged(final GuiElementTextBox textBox, final String newText) {
        this.addBtn.enabled = newText != null && !newText.isEmpty();

    }

    @Override
    public String getInitialText(final GuiElementTextBox textBox) {
        return "";
    }

    @Override
    public int getTextColor(final GuiElementTextBox textBox) {
        return 0xFF14FF14;
    }

    @Override
    public void onIntruderInteraction(final GuiElementTextBox textBox) {}

    // STRINGSELECTBOX
    @Override
    public void onSelectionChanged(final StringSelectBox box, final int selection) {
        this.rmBtn.enabled = box.hasSelection();
    }

    @Override
    public void mothershipOperationFailed(final String message) {
        this.error = message;
        this.errorTime = 60.0F;
    }

}
