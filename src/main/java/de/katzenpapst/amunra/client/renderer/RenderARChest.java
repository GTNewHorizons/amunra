package de.katzenpapst.amunra.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.model.ModelLargeChest;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import de.katzenpapst.amunra.block.BlockARChest;
import de.katzenpapst.amunra.tile.TileEntityARChest;

public class RenderARChest extends TileEntitySpecialRenderer {

    private final ModelChest chestModel = new ModelChest();
    private final ModelLargeChest largeChestModel = new ModelLargeChest();

    public void renderARChestAt(final TileEntityARChest chest, final double x, final double y, final double z,
            final float partialTickTime) {
        int chestMetadata;

        final Block chestBlock = chest.getBlockType();
        if (!(chestBlock instanceof BlockARChest)) {
            return;
        }

        if (!chest.hasWorldObj()) {
            chestMetadata = 0;
        } else {
            chestMetadata = chest.getBlockMetadata();

            if (chestBlock != null && chestMetadata == 0) {
                ((BlockARChest) chestBlock)
                        .unifyAdjacentChests(chest.getWorldObj(), chest.xCoord, chest.yCoord, chest.zCoord);
                chestMetadata = chest.getBlockMetadata();
            }

            chest.checkForAdjacentChests();
        }

        if (chest.adjacentChestZNeg == null && chest.adjacentChestXNeg == null) {
            ModelChest smallModel = null;
            ModelLargeChest largeModel = null;

            if (chest.adjacentChestXPos == null && chest.adjacentChestZPos == null) {
                smallModel = this.chestModel;
                this.bindTexture(((BlockARChest) chestBlock).getSmallTexture());
            } else {
                largeModel = this.largeChestModel;
                this.bindTexture(((BlockARChest) chestBlock).getLargeTexture());
            }

            GL11.glPushMatrix();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glTranslatef((float) x, (float) y + 1.0F, (float) z + 1.0F);
            GL11.glScalef(1.0F, -1.0F, -1.0F);
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);

            if (chestMetadata == 2 && chest.adjacentChestXPos != null) {
                GL11.glTranslatef(1.0F, 0.0F, 0.0F);
            }

            if (chestMetadata == 5 && chest.adjacentChestZPos != null) {
                GL11.glTranslatef(0.0F, 0.0F, -1.0F);
            }

            final float angle = switch (chestMetadata) {
                case 2 -> 180.0f;
                case 4 -> 90.0f;
                case 5 -> -90.0f;
                default -> 0.0f;
            };

            GL11.glRotatef(angle, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            float var12 = chest.prevLidAngle + (chest.lidAngle - chest.prevLidAngle) * partialTickTime;

            float var13;

            if (chest.adjacentChestZNeg != null) {
                var13 = chest.adjacentChestZNeg.prevLidAngle
                        + (chest.adjacentChestZNeg.lidAngle - chest.adjacentChestZNeg.prevLidAngle) * partialTickTime;

                if (var13 > var12) {
                    var12 = var13;
                }
            }

            if (chest.adjacentChestXNeg != null) {
                var13 = chest.adjacentChestXNeg.prevLidAngle
                        + (chest.adjacentChestXNeg.lidAngle - chest.adjacentChestXNeg.prevLidAngle) * partialTickTime;

                if (var13 > var12) {
                    var12 = var13;
                }
            }

            var12 = 1.0F - (float) Math.pow(1.0 - var12, 3.0);

            if (smallModel != null) {
                smallModel.chestLid.rotateAngleX = -(var12 * (float) Math.PI / 4.0F);
                smallModel.renderAll();
            }

            if (largeModel != null) {
                largeModel.chestLid.rotateAngleX = -(var12 * (float) Math.PI / 4.0F);
                largeModel.renderAll();
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glPopMatrix();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity p_147500_1_, double p_147500_2_, double p_147500_4_, double p_147500_6_,
            float p_147500_8_) {
        this.renderARChestAt((TileEntityARChest) p_147500_1_, p_147500_2_, p_147500_4_, p_147500_6_, p_147500_8_);
    }

}
