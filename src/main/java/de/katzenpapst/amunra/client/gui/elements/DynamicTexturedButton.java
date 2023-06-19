package de.katzenpapst.amunra.client.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class DynamicTexturedButton extends GuiButton {

    protected ResourceLocation texture;
    protected boolean isSelected;

    public DynamicTexturedButton(final int id, final int x, final int y, final ResourceLocation initialTexture) {
        super(id, x, y, "");
        this.texture = initialTexture;
    }

    public DynamicTexturedButton(final int id, final int x, final int y, final int width, final int height,
            final ResourceLocation initialTexture) {
        super(id, x, y, width, height, "");
        this.texture = initialTexture;
    }

    public void setTexture(final ResourceLocation texture) {
        this.texture = texture;
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }

    public void setSelected(final boolean set) {
        this.isSelected = set;
    }

    public boolean getSelected() {
        return this.isSelected;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            // FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(buttonTextures);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            if (this.isSelected) {
                GL11.glColor4f(1.0F, 0.5F, 0.5F, 1.0F);
            }
            this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height;
            final int hoverState = this.getHoverState(this.field_146123_n);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect(
                    this.xPosition,
                    this.yPosition,
                    0,
                    46 + hoverState * 20,
                    this.width / 2,
                    this.height);
            this.drawTexturedModalRect(
                    this.xPosition + this.width / 2,
                    this.yPosition,
                    200 - this.width / 2,
                    46 + hoverState * 20,
                    this.width / 2,
                    this.height);
            this.mouseDragged(mc, mouseX, mouseY);

            if (this.texture != null) {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(this.texture);
                this.drawFullSizedTexturedRect(this.xPosition + 2, this.yPosition + 2, this.width - 4, this.height - 4);
            }
        }
    }

    protected void drawFullSizedTexturedRect(final int x, final int y, final int width, final int height) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        final Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, this.zLevel, 0, 1);
        tessellator.addVertexWithUV(x + width, y + height, this.zLevel, 1, 1);
        tessellator.addVertexWithUV(x + width, y, this.zLevel, 1, 0);
        tessellator.addVertexWithUV(x, y, this.zLevel, 0, 0);
        tessellator.draw();
    }

}
