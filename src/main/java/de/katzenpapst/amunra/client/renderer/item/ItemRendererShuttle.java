package de.katzenpapst.amunra.client.renderer.item;

import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.entity.spaceship.EntityShuttle;

public class ItemRendererShuttle implements IItemRenderer {

    protected static final ResourceLocation chestTexture = new ResourceLocation("textures/entity/chest/normal.png");
    protected static final ResourceLocation tankTexture = new ResourceLocation(
            AmunRa.ASSETPREFIX,
            "textures/items/tank-thumb.png");

    protected IModelCustom modelSpaceship;
    protected final ModelChest chestModel = new ModelChest();

    protected static RenderItem drawItems = new RenderItem();

    protected ResourceLocation texture = new ResourceLocation(AmunRa.ASSETPREFIX, "textures/model/shuttle.png");

    public ItemRendererShuttle(final IModelCustom model) {
        this.modelSpaceship = model;
    }

    protected void renderSpaceship(final ItemRenderType type, final RenderBlocks render, final ItemStack item,
            final float translateX, final float translateY, final float translateZ) {
        GL11.glPushMatrix();

        this.transform(item, type);

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(this.texture);
        this.modelSpaceship.renderAll();
        GL11.glPopMatrix();

        if (type == ItemRenderType.INVENTORY) {
            final int numChests = EntityShuttle.getNumChestsFromDamage(item.getItemDamage());
            final int numTanks = EntityShuttle.getNumTanksFromDamage(item.getItemDamage());
            if (numChests > 0) {
                final ModelChest modelChest = this.chestModel;
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(chestTexture);

                GL11.glPushMatrix();
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glScalef(0.5F, -0.5F, -0.5F);

                modelChest.chestLid.rotateAngleX = 0.0F;
                modelChest.chestBelow.render(0.0625F);
                modelChest.chestLid.render(0.0625F);
                modelChest.chestKnob.render(0.0625F);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glPopMatrix();
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
            if (numTanks > 0) {
                GL11.glPushMatrix();
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(tankTexture);
                final Tessellator tessellator = Tessellator.instance;

                GL11.glTranslatef(1.0F, 0.98F, 1.7F);

                GL11.glDisable(GL11.GL_DEPTH_TEST);

                GL11.glRotatef(45, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(180, 1.0F, 0.0F, 0.0F);

                if (numChests == 0) {
                    GL11.glTranslatef(0.7F, 0.0F, 0.0F);
                }

                GL11.glScalef(0.61F, 0.71F, 1.0F);

                tessellator.startDrawingQuads();
                tessellator.addVertexWithUV(0, 1, 0, 0, 1);
                tessellator.addVertexWithUV(1, 1, 0, 1, 1);
                tessellator.addVertexWithUV(1, 0, 0, 1, 0);
                tessellator.addVertexWithUV(0, 0, 0, 0, 0);
                tessellator.draw();

                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glPopMatrix();
            }
        }
    }

    public void transform(final ItemStack itemstack, final ItemRenderType type) {
        if (type == ItemRenderType.EQUIPPED) {
            GL11.glRotatef(70, 1.0F, 0, 0);
            GL11.glRotatef(-10, 0.0F, 1, 0);
            GL11.glRotatef(50, 0.0F, 1, 1);
            GL11.glTranslatef(-0.8F, -2.2F, 0F);
            GL11.glScalef(5.2F, 5.2F, 5.2F);
        } else if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
            GL11.glTranslatef(2.5F, 5.9F, 1F);
            GL11.glRotatef(28, 0.0F, 0, 1);
            GL11.glRotatef(230, 0.0F, 1, 0);
            GL11.glRotatef(73, 1.0F, 0, 0);
            GL11.glScalef(5.2F, 5.2F, 5.2F);
        }
        GL11.glTranslatef(0, 0.1F, 0);
        GL11.glScalef(-0.4F, -0.4F, 0.4F);

        if (type == ItemRenderType.INVENTORY || type == ItemRenderType.ENTITY) {
            if (type == ItemRenderType.INVENTORY) {
                GL11.glRotatef(85F, 1F, 0F, 1F);
                GL11.glRotatef(20F, 1F, 0F, 0F);
                GL11.glScalef(0.7F, 0.7F, 0.7F);
                GL11.glTranslatef(0.0F, 1.6F, -0.4F);
            } else {
                GL11.glTranslatef(0, -0.9F, 0);
                GL11.glScalef(0.5F, 0.5F, 0.5F);
            }

            GL11.glScalef(1.3F, 1.3F, 1.3F);
            GL11.glTranslatef(0, -0.6F, 0);
            GL11.glRotatef(Sys.getTime() / 30F % 360F + 45, 0F, 1F, 0F);
        }

        GL11.glRotatef(180, 0, 0, 1);
    }

    /**
     * IItemRenderer implementation *
     */

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return switch (type) {
            case ENTITY -> true;
            case EQUIPPED -> true;
            case EQUIPPED_FIRST_PERSON -> true;
            case INVENTORY -> true;
            default -> false;
        };
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, final ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        switch (type) {
            case EQUIPPED -> this.renderSpaceship(type, (RenderBlocks) data[0], item, -0.5f, -0.5f, -0.5f);
            case EQUIPPED_FIRST_PERSON -> this.renderSpaceship(type, (RenderBlocks) data[0], item, -0.5f, -0.5f, -0.5f);
            case INVENTORY -> this.renderSpaceship(type, (RenderBlocks) data[0], item, -0.5f, -0.5f, -0.5f);
            case ENTITY -> this.renderSpaceship(type, (RenderBlocks) data[0], item, -0.5f, -0.5f, -0.5f);
            default -> {}
        }
    }

}
