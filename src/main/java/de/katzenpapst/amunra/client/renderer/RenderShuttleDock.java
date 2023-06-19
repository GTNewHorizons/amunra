package de.katzenpapst.amunra.client.renderer;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import de.katzenpapst.amunra.client.renderer.model.ModelShuttleDock;
import de.katzenpapst.amunra.tile.TileEntityShuttleDock;

public class RenderShuttleDock extends TileEntitySpecialRenderer {

    // private ResourceLocation texture = new ResourceLocation(AmunRa.instance.ASSETPREFIX, "textures/model/dock.png");
    private final ModelShuttleDock model = new ModelShuttleDock();

    @Override
    public void renderTileEntityAt(TileEntity p_147500_1_, double p_147500_2_, double p_147500_4_, double p_147500_6_,
            float p_147500_8_) {

        if (!(p_147500_1_ instanceof TileEntityShuttleDock dock)) {
            return;
        }
        // render the stuff
        GL11.glPushMatrix();
        GL11.glTranslated(p_147500_2_, p_147500_4_, p_147500_6_);

        // TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;
        /*
         * GL11.glEnable(GL11.GL_TEXTURE_2D); GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
         */

        final Tessellator tessellator = Tessellator.instance;

        // Minecraft.getMinecraft().renderEngine.bindTexture(texture);

        float rotation = 0.0F;

        /*
         * 2 -> -Z 1 -> -X 3 -> +X 0 -> +Z
         */

        switch (dock.getRotationMeta()) {
            case 0:
                rotation = 90.0F;// 180.0F;// -> Z
                break;
            case 1:
                rotation = 270.0F;// 90.0F;// -> -X
                break;
            case 2:
                rotation = 180.0F;// 0;// -> -Z
                break;
            case 3:
                rotation = 0.0F;// 270.0F;// -> X
                break;
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTranslatef(0.5F, 0.0F, 0.5F);
        GL11.glRotatef(rotation, 0, 1, 0);

        GL11.glRotatef(90.0F, 0, 1, 0);
        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        this.model.render(tessellator, dock.hasShuttle());

        /*
         * Block block = dock.getWorldObj().getBlock(dock.xCoord, dock.yCoord, dock.zCoord);
         * tessellator.setBrightness(block.getMixedBrightnessForBlock(dock.getWorldObj(), dock.xCoord, dock.yCoord,
         * dock.zCoord));
         */

        GL11.glPopMatrix();
    }

}
