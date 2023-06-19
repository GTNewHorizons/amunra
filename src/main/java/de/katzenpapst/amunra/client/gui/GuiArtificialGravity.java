package de.katzenpapst.amunra.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.inventory.ContainerArtificalGravity;
import de.katzenpapst.amunra.network.packet.PacketSimpleAR;
import de.katzenpapst.amunra.tile.TileEntityGravitation;
import micdoodle8.mods.galacticraft.api.vector.BlockVec3;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.client.gui.container.GuiContainerGC;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementCheckbox;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementCheckbox.ICheckBoxCallback;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementInfoRegion;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementTextBox;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementTextBox.ITextBoxCallback;
import micdoodle8.mods.galacticraft.core.energy.EnergyDisplayHelper;
import micdoodle8.mods.galacticraft.core.network.PacketSimple;
import micdoodle8.mods.galacticraft.core.network.PacketSimple.EnumSimplePacket;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class GuiArtificialGravity extends GuiContainerGC implements ITextBoxCallback, ICheckBoxCallback {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/gui/gravity.png");

    protected final List<GuiElementTextBox> inputFieldList = new ArrayList<>();

    private GuiElementTextBox leftValueField;
    private GuiElementTextBox rightValueField;
    private GuiElementTextBox frontValueField;
    private GuiElementTextBox backValueField;
    private GuiElementTextBox topValueField;
    private GuiElementTextBox bottomValueField;

    private GuiElementTextBox strengthField;
    private GuiElementCheckbox checkboxInvert;

    private GuiElementCheckbox checkboxVisualGuide;

    private GuiButton disableButton;

    private AxisAlignedBB tempBox;
    private double tempGravityStrength;
    private boolean tempIsInverted;

    public static final int FIELD_TOP = 0;
    public static final int FIELD_LEFT = 1;
    public static final int FIELD_FRONT = 2;

    public static final int FIELD_BACK = 3;
    public static final int FIELD_RIGHT = 4;
    public static final int FIELD_BOTTOM = 5;

    public static final int FIELD_STRENGTH = 10;

    public static final int BTN_ENABLE = 6;
    public static final int CHECKBOX_VISUAL = 8;
    public static final int CHECKBOX_INVERT = 11;

    private final GuiElementInfoRegion electricInfoRegion = new GuiElementInfoRegion(
            (this.width - this.xSize) / 2 + 112,
            (this.height - this.ySize) / 2 + 87,
            52,
            9,
            new ArrayList<>(),
            this.width,
            this.height,
            this);

    private final TileEntityGravitation tile;

    public GuiArtificialGravity(final InventoryPlayer player, final TileEntityGravitation tile) {
        super(new ContainerArtificalGravity(player, tile));
        this.xSize = 176;
        this.ySize = 231;
        this.tile = tile;

        this.tempGravityStrength = tile.getGravityForce() * 100.0;
        this.tempIsInverted = this.tempGravityStrength > 0;
        this.tempGravityStrength = Math.abs(this.tempGravityStrength);

    }

    private AxisAlignedBB cloneAABB(final AxisAlignedBB box) {
        return AxisAlignedBB.getBoundingBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    protected void sendDataToServer() {
        final BlockVec3 pos = new BlockVec3(this.tile);
        final BlockVec3 min = new BlockVec3((int) this.tempBox.minX, (int) this.tempBox.minY, (int) this.tempBox.minZ);
        final BlockVec3 max = new BlockVec3((int) this.tempBox.maxX, (int) this.tempBox.maxY, (int) this.tempBox.maxZ);
        double actualStrength = this.tempGravityStrength / 100;
        if (!this.tempIsInverted) {
            actualStrength *= -1;
        }
        AmunRa.packetPipeline.sendToServer(
                new PacketSimpleAR(
                        PacketSimpleAR.EnumSimplePacket.S_ARTIFICIAL_GRAVITY_SETTINGS,
                        pos,
                        min,
                        max,
                        actualStrength));
        this.tile.setGravityBox(this.cloneAABB(this.tempBox));

        this.tile.setGravityForce(actualStrength);
    }

    protected void resetDataFromTile() {
        this.tempBox = this.cloneAABB(this.tile.getGravityBox());
        this.topValueField.text = Integer.toString((int) this.tempBox.maxY);
        this.backValueField.text = Integer.toString((int) this.tempBox.maxZ);
        this.rightValueField.text = Integer.toString((int) this.tempBox.maxX);

        this.bottomValueField.text = Integer.toString((int) this.tempBox.minY * -1);
        this.frontValueField.text = Integer.toString((int) this.tempBox.minZ * -1);
        this.leftValueField.text = Integer.toString((int) this.tempBox.minX * -1);

        this.tempGravityStrength = this.tile.getGravityForce() * 100.0;
        this.tempIsInverted = this.tempGravityStrength > 0;
        this.tempGravityStrength = Math.abs(this.tempGravityStrength);
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.id == BTN_ENABLE) {
            GalacticraftCore.packetPipeline.sendToServer(
                    new PacketSimple(
                            EnumSimplePacket.S_UPDATE_DISABLEABLE_BUTTON,
                            new Object[] { this.tile.xCoord, this.tile.yCoord, this.tile.zCoord, 0 }));
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        final List<String> batterySlotDesc = new ArrayList<>();
        batterySlotDesc.add(GCCoreUtil.translate("gui.batterySlot.desc.0"));
        batterySlotDesc.add(GCCoreUtil.translate("gui.batterySlot.desc.1"));
        this.infoRegions.add(
                new GuiElementInfoRegion(
                        (this.width - this.xSize) / 2 + 152,
                        (this.height - this.ySize) / 2 + 134,
                        18,
                        18,
                        batterySlotDesc,
                        this.width,
                        this.height,
                        this));

        this.electricInfoRegion.xPosition = (this.width - this.xSize) / 2 + 98;
        this.electricInfoRegion.yPosition = (this.height - this.ySize) / 2 + 118 + 22;
        this.electricInfoRegion.parentWidth = this.width;
        this.electricInfoRegion.parentHeight = this.height;
        this.infoRegions.add(this.electricInfoRegion);

        final int guiX = (this.width - this.xSize) / 2;
        final int guiY = (this.height - this.ySize) / 2;

        final int offsetX1 = 10;
        final int offsetX2 = 50;
        this.topValueField = new GuiElementTextBox(
                FIELD_TOP,
                this,
                offsetX1 + guiX + 32,
                guiY + 28 - 4,
                28,
                18,
                "0",
                true,
                2,
                true);
        this.leftValueField = new GuiElementTextBox(
                FIELD_LEFT,
                this,
                offsetX1 + guiX + 2,
                guiY + 44,
                28,
                18,
                "0",
                true,
                2,
                true);
        this.frontValueField = new GuiElementTextBox(
                FIELD_FRONT,
                this,
                offsetX2 + guiX + 62 - 40,
                guiY + 28 + 16,
                28,
                18,
                "0",
                true,
                2,
                true);

        this.backValueField = new GuiElementTextBox(
                FIELD_BACK,
                this,
                offsetX1 + guiX + 2,
                guiY + 64,
                28,
                18,
                "0",
                true,
                2,
                true);
        this.rightValueField = new GuiElementTextBox(
                FIELD_RIGHT,
                this,
                offsetX2 + guiX + 22,
                guiY + 48 + 16,
                28,
                18,
                "0",
                true,
                2,
                true);
        this.bottomValueField = new GuiElementTextBox(
                FIELD_BOTTOM,
                this,
                offsetX1 + guiX + 32,
                guiY + 68 + 16,
                28,
                18,
                "0",
                true,
                2,
                true);

        this.addInputField(this.leftValueField);
        this.addInputField(this.backValueField);
        this.addInputField(this.bottomValueField);

        this.addInputField(this.rightValueField);
        this.addInputField(this.frontValueField);
        this.addInputField(this.topValueField);

        // buttons
        final int yOffsetBtns = -10 + 3;
        this.disableButton = new GuiButton(
                BTN_ENABLE,
                guiX + 110,
                guiY + 90 + yOffsetBtns,
                50,
                20,
                GCCoreUtil.translate("gui.button.disable.name"));

        this.checkboxVisualGuide = new GuiElementCheckbox(
                CHECKBOX_VISUAL,
                this,
                guiX + 80,
                guiY + 24,
                GCCoreUtil.translate("gui.checkbox.show_visual_guide"));

        this.buttonList.add(this.disableButton);
        this.buttonList.add(this.checkboxVisualGuide);

        this.strengthField = new GuiElementTextBox(
                FIELD_STRENGTH,
                this,
                guiX + 60,
                guiY + 110,
                38,
                18,
                "0",
                true,
                2,
                true);
        this.addInputField(this.strengthField);

        this.checkboxInvert = new GuiElementCheckbox(
                CHECKBOX_INVERT,
                this,
                guiX + 100,
                guiY + 112,
                GCCoreUtil.translate("gui.checkbox.invert_force"));
        this.buttonList.add(this.checkboxInvert);
    }

    protected void addInputField(final GuiElementTextBox box) {
        this.buttonList.add(box);
        this.inputFieldList.add(box);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString(this.tile.getInventoryName(), 8, 10, 0x404040);
        this.fontRendererObj.drawString(GCCoreUtil.translate("container.inventory"), 8, this.ySize - 90 + 2, 0x404040);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        final int xOffset = (this.width - this.xSize) / 2;
        final int yOffset = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(xOffset, yOffset + 5, 0, 0, this.xSize, this.ySize);

        if (this.tile != null) {
            if (this.tile.getDisabled(0)) {
                this.disableButton.displayString = GCCoreUtil.translate("gui.button.enable.name");
            } else {
                this.disableButton.displayString = GCCoreUtil.translate("gui.button.disable.name");
            }

            final int scale = this.tile.getScaledElecticalLevel(54);
            this.drawTexturedModalRect(xOffset + 99, yOffset + 119 + 22, 176, 0, Math.min(scale, 54), 7);

            final List<String> electricityDesc = new ArrayList<>();
            electricityDesc.add(GCCoreUtil.translate("gui.energyStorage.desc.0"));
            EnergyDisplayHelper.getEnergyDisplayTooltip(
                    this.tile.getEnergyStoredGC(),
                    this.tile.getMaxEnergyStoredGC(),
                    electricityDesc);
            electricityDesc.add(
                    EnumChatFormatting.AQUA + GCCoreUtil.translate("gui.message.energy_usage")
                            + ": "
                            + EnergyDisplayHelper.getEnergyDisplayS(this.tile.storage.getMaxExtract())
                            + "/t");
            this.electricInfoRegion.tooltipStrings = electricityDesc;

            this.fontRendererObj.drawString(
                    GCCoreUtil.translate("gui.message.status.name") + ": " + this.getStatus(),
                    xOffset + 8,
                    yOffset + 130,
                    0x404040);

            this.fontRendererObj.drawString(
                    GCCoreUtil.translate("gui.message.force.strength") + ": ",
                    xOffset + 8,
                    yOffset + 116,
                    0x404040);

        }
    }

    @Override
    public boolean canPlayerEdit(final GuiElementTextBox textBox, final EntityPlayer player) {
        return true;
    }

    @Override
    public void onTextChanged(final GuiElementTextBox textBox, final String newText) {

        if (newText == null) {
            // don't do anything
            return;
        }
        double newValue;
        try {
            newValue = Double.parseDouble(newText);
            // newValue = Integer.parseInt(newText);
        } catch (final NumberFormatException wat) {
            // this is ridiculous
            return;
        }
        if (newValue < 0) {
            return;
        }

        switch (textBox.id) {
            case FIELD_TOP:
                this.tempBox.maxY = (int) newValue;
                break;
            case FIELD_BACK:
                this.tempBox.maxZ = (int) newValue;
                break;
            case FIELD_RIGHT:
                this.tempBox.maxX = (int) newValue;
                break;
            case FIELD_BOTTOM:
                this.tempBox.minY = (int) newValue * -1;
                break;
            case FIELD_FRONT:
                this.tempBox.minZ = (int) newValue * -1;
                break;
            case FIELD_LEFT:
                this.tempBox.minX = (int) newValue * -1;
                break;
            case FIELD_STRENGTH:
                this.tempGravityStrength = Math.abs(newValue);
                break;
            default:
                return;
        }
        this.sendDataToServer();
    }

    @Override
    public String getInitialText(final GuiElementTextBox textBox) {
        this.tempBox = this.tile.getGravityBox();

        switch (textBox.id) {
            case FIELD_TOP:
                return Integer.toString((int) this.tempBox.maxY);
            case FIELD_BACK:
                return Integer.toString((int) this.tempBox.maxZ);
            case FIELD_RIGHT:
                return Integer.toString((int) this.tempBox.maxX);

            case FIELD_BOTTOM:
                return Integer.toString((int) this.tempBox.minY * -1);
            case FIELD_FRONT:
                return Integer.toString((int) this.tempBox.minZ * -1);
            case FIELD_LEFT:
                return Integer.toString((int) this.tempBox.minX * -1);
            case FIELD_STRENGTH:
                return Integer.toString((int) Math.abs(this.tempGravityStrength));
        }
        return Integer.toString(textBox.id);

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode != Keyboard.KEY_ESCAPE /* && keyID != this.mc.gameSettings.keyBindInventory.getKeyCode() */) {
            // do the fields
            for (final GuiElementTextBox box : this.inputFieldList) {
                if (box.keyTyped(typedChar, keyCode)) {
                    return;
                }
            }
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public int getTextColor(final GuiElementTextBox textBox) {
        return 0xFF14FF14;
    }

    @Override
    public void onIntruderInteraction(final GuiElementTextBox textBox) {

    }

    @Override
    public void onSelectionChanged(final GuiElementCheckbox checkbox, final boolean newSelected) {
        switch (checkbox.id) {
            case CHECKBOX_VISUAL:
                this.tile.isBoxShown = newSelected;
                this.sendDataToServer();
                break;
            case CHECKBOX_INVERT:
                this.tempIsInverted = newSelected;
                this.sendDataToServer();
                break;
        }
    }

    @Override
    public boolean canPlayerEdit(final GuiElementCheckbox checkbox, final EntityPlayer player) {
        return true;
    }

    @Override
    public boolean getInitiallySelected(final GuiElementCheckbox checkbox) {
        switch (checkbox.id) {
            case CHECKBOX_VISUAL:
                return this.tile.isBoxShown;
            case CHECKBOX_INVERT:
                return this.tempIsInverted;
        }
        return false;
    }

    @Override
    public void onIntruderInteraction() {

    }

    protected String getStatus() {
        return this.tile.getGUIstatus();
    }

}
