package de.katzenpapst.amunra.client.renderer.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.model.ModelLargeChest;

import org.lwjgl.opengl.GL11;

import de.katzenpapst.amunra.block.BlockARChest;

public class ModelARChest {

    private final ModelChest chestModel = new ModelChest();
    private final ModelLargeChest largeChestModel = new ModelLargeChest();

    public void render(final BlockARChest chestBlock, final boolean isDouble, final double x, final double y,
            final double z) {
        ModelChest chestModel = null;

        if (!isDouble) {
            chestModel = this.chestModel;
            Minecraft.getMinecraft().renderEngine.bindTexture(chestBlock.getSmallTexture());
        } else {
            chestModel = this.largeChestModel;
            Minecraft.getMinecraft().renderEngine.bindTexture(chestBlock.getLargeTexture());
        }

        GL11.glPushMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTranslatef((float) x, (float) y + 1.0F, (float) z + 1.0F);
        GL11.glScalef(1.0F, -1.0F, -1.0F);
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        GL11.glRotated(-90, 0, 1, 0);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

        chestModel.renderAll();

        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

}
