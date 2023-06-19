package de.katzenpapst.amunra.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.inventory.ContainerHydroponics;
import de.katzenpapst.amunra.network.packet.PacketSimpleAR;
import de.katzenpapst.amunra.tile.TileEntityHydroponics;
import micdoodle8.mods.galacticraft.core.client.gui.container.GuiContainerGC;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementInfoRegion;
import micdoodle8.mods.galacticraft.core.energy.EnergyDisplayHelper;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;

public class GuiHydroponics extends GuiContainerGC {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/gui/hydroponics.png");

    private final GuiElementInfoRegion oxygenInfoRegion = new GuiElementInfoRegion(
            (this.width - this.xSize) / 2 + 112,
            (this.height - this.ySize) / 2 + 24,
            56,
            9,
            new ArrayList<>(),
            this.width,
            this.height,
            this);
    private final GuiElementInfoRegion electricInfoRegion = new GuiElementInfoRegion(
            (this.width - this.xSize) / 2 + 112,
            (this.height - this.ySize) / 2 + 37,
            56,
            9,
            new ArrayList<>(),
            this.width,
            this.height,
            this);

    private final TileEntityHydroponics tile;

    private GuiButton button;

    public GuiHydroponics(final InventoryPlayer player, final TileEntityHydroponics tile) {
        super(new ContainerHydroponics(player, tile));
        this.ySize = 201;
        this.xSize = 176;
        this.tile = tile;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            // do the stuff
            final float growthStatus = this.tile.getPlantGrowthStatus();
            if (growthStatus < 0) {
                AmunRa.packetPipeline.sendToServer(
                        new PacketSimpleAR(
                                PacketSimpleAR.EnumSimplePacket.S_HYDROPONICS_OPERATION,
                                this.tile.xCoord,
                                this.tile.yCoord,
                                this.tile.zCoord,
                                TileEntityHydroponics.OperationType.PLANT_SEED.ordinal()));
            } else if (growthStatus < 1.0F) {
                AmunRa.packetPipeline.sendToServer(
                        new PacketSimpleAR(
                                PacketSimpleAR.EnumSimplePacket.S_HYDROPONICS_OPERATION,
                                this.tile.xCoord,
                                this.tile.yCoord,
                                this.tile.zCoord,
                                TileEntityHydroponics.OperationType.FERTILIZE.ordinal()));
            } else if (growthStatus == 1) {
                AmunRa.packetPipeline.sendToServer(
                        new PacketSimpleAR(
                                PacketSimpleAR.EnumSimplePacket.S_HYDROPONICS_OPERATION,
                                this.tile.xCoord,
                                this.tile.yCoord,
                                this.tile.zCoord,
                                TileEntityHydroponics.OperationType.HARVEST.ordinal()));
            }
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
                        (this.width - this.xSize) / 2 + 31,
                        (this.height - this.ySize) / 2 + 26,
                        18,
                        18,
                        batterySlotDesc,
                        this.width,
                        this.height,
                        this));
        this.oxygenInfoRegion.xPosition = (this.width - this.xSize) / 2 + 112;
        this.oxygenInfoRegion.yPosition = (this.height - this.ySize) / 2 + 24;
        this.oxygenInfoRegion.parentWidth = this.width;
        this.oxygenInfoRegion.parentHeight = this.height;
        this.infoRegions.add(this.oxygenInfoRegion);
        this.electricInfoRegion.xPosition = (this.width - this.xSize) / 2 + 112;
        this.electricInfoRegion.yPosition = (this.height - this.ySize) / 2 + 37;
        this.electricInfoRegion.parentWidth = this.width;
        this.electricInfoRegion.parentHeight = this.height;
        this.infoRegions.add(this.electricInfoRegion);

        final float growStatus = this.tile.getPlantGrowthStatus();

        this.button = new GuiButton(
                0,
                (this.width - this.xSize) / 2 + 82,
                (this.height - this.ySize) / 2 + 88,
                72,
                20,
                GCCoreUtil.translate("tile.hydroponics.plant"));

        this.updateTheButton(growStatus);

        this.buttonList.add(this.button);
    }

    private void updateTheButton(final float growStatus) {
        // tile.hydroponics.fertilize
        if (growStatus < 0) {
            this.button.displayString = GCCoreUtil.translate("tile.hydroponics.plant");
            final ItemStack stack = this.tile.getStackInSlot(1);
            this.button.enabled = stack != null && stack.stackSize > 0 && TileEntityHydroponics.seeds.isSameItem(stack);
        } else if (growStatus < 1.0F) {
            this.button.displayString = GCCoreUtil.translate("tile.hydroponics.fertilize");
            final ItemStack stack = this.tile.getStackInSlot(1);
            this.button.enabled = stack != null && stack.stackSize > 0
                    && TileEntityHydroponics.bonemeal.isSameItem(stack);
        } else {
            this.button.displayString = GCCoreUtil.translate("tile.hydroponics.harvest");
            this.button.enabled = true;
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        final float growStatus = this.tile.getPlantGrowthStatus();
        this.fontRendererObj.drawString(this.tile.getInventoryName(), 8, 10, 0x404040);
        GCCoreUtil.drawStringRightAligned(
                GCCoreUtil.translate("gui.message.out.name") + ":",
                99,
                25,
                0x404040,
                this.fontRendererObj);
        GCCoreUtil.drawStringRightAligned(
                GCCoreUtil.translate("gui.message.in.name") + ":",
                99,
                37,
                0x404040,
                this.fontRendererObj);

        final String plantStatus = this.getPlantStatus(growStatus);
        if (growStatus < 0) {
            GCCoreUtil.drawStringCentered(
                    GCCoreUtil.translate("gui.message.status.name") + ": " + plantStatus,
                    this.xSize / 2,
                    50,
                    0x404040,
                    this.fontRendererObj);

        } else {
            GCCoreUtil.drawStringCentered(
                    GCCoreUtil.translate("gui.message.status.name") + ": " + this.getStatus(),
                    this.xSize / 2,
                    50,
                    0x404040,
                    this.fontRendererObj);
            GCCoreUtil.drawStringCentered(
                    GCCoreUtil.translate("tile.hydroponics.plantstatus") + ": " + plantStatus,
                    this.xSize / 2,
                    60,
                    0x404040,
                    this.fontRendererObj);
            final String status = GCCoreUtil.translate("gui.status.collecting.name") + ": "
                    + (int) (0.5F + Math
                            .min(this.tile.lastOxygenCollected * 20F, TileEntityHydroponics.OUTPUT_PER_TICK * 20F))
                    + GCCoreUtil.translate("gui.perSecond");
            GCCoreUtil.drawStringCentered(status, this.xSize / 2, 70, 0x404040, this.fontRendererObj);
        }

        this.updateTheButton(growStatus);

        this.fontRendererObj.drawString(GCCoreUtil.translate("container.inventory"), 8, this.ySize - 90 + 2, 0x404040);
    }

    private String getPlantStatus(final float growStatus) {
        if (growStatus < 0) {
            return EnumColor.DARK_RED + GCCoreUtil.translate("tile.hydroponics.noplant");
        }
        if (growStatus < 1.0F) {
            return EnumColor.YELLOW.getCode() + Math.floor(this.tile.getPlantGrowthStatus() * 100) + "%";
        }
        return EnumColor.DARK_GREEN + "100%";
    }

    private String getStatus() {
        return this.tile.getGUIstatus();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        final int x = (this.width - this.xSize) / 2;
        final int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y + 5, 0, 0, this.xSize, this.ySize);

        if (this.tile != null) {
            int scale = this.tile.getCappedScaledOxygenLevel(54);
            this.drawTexturedModalRect(x + 113, y + 25, 197, 7, Math.min(scale, 54), 7);
            scale = this.tile.getScaledElecticalLevel(54);
            this.drawTexturedModalRect(x + 113, y + 38, 197, 0, Math.min(scale, 54), 7);

            if (this.tile.getEnergyStoredGC() > 0) {
                this.drawTexturedModalRect(x + 99, y + 37, 176, 0, 11, 10);
            }

            if (this.tile.storedOxygen > 0) {
                this.drawTexturedModalRect(x + 100, y + 24, 187, 0, 10, 10);
            }

            final List<String> oxygenDesc = new ArrayList<>();
            oxygenDesc.add(GCCoreUtil.translate("gui.oxygenStorage.desc.0"));
            oxygenDesc.add(
                    EnumColor.YELLOW + GCCoreUtil.translate("gui.oxygenStorage.desc.1")
                            + ": "
                            + ((int) Math.floor(this.tile.storedOxygen) + " / "
                                    + (int) Math.floor(this.tile.maxOxygen)));
            this.oxygenInfoRegion.tooltipStrings = oxygenDesc;

            final List<String> electricityDesc = new ArrayList<>();
            electricityDesc.add(GCCoreUtil.translate("gui.energyStorage.desc.0"));
            EnergyDisplayHelper.getEnergyDisplayTooltip(
                    this.tile.getEnergyStoredGC(),
                    this.tile.getMaxEnergyStoredGC(),
                    electricityDesc);
            this.electricInfoRegion.tooltipStrings = electricityDesc;
        }
    }

}
