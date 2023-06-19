package de.katzenpapst.amunra.client.renderer.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import de.katzenpapst.amunra.item.ARItems;

public class ItemRendererJet implements IItemRenderer {

    protected IModelCustom[] models;
    protected ResourceLocation[] textures;

    public ItemRendererJet(final IModelCustom[] models, final ResourceLocation[] textures) {
        // TODO find a better idea
        this.models = models;
        this.textures = textures;
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        if (item.getItem() != ARItems.jetItemMeta || item.getItemDamage() >= this.models.length
                || item.getItemDamage() >= this.textures.length) {
            return false;
        }
        return switch (type) {
            case ENTITY, EQUIPPED, EQUIPPED_FIRST_PERSON, INVENTORY -> true;
            default -> false;
        };
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    protected void renderJet(final ItemRenderType type, final RenderBlocks render, final ItemStack item,
            final float translateX, final float translateY, final float translateZ) {
        // TODO get a better idea how to do this
        final ResourceLocation texture = this.textures[item.getItemDamage()];
        final IModelCustom model = this.models[item.getItemDamage()];

        switch (type) {
            case ENTITY -> {
                GL11.glScaled(0.53, 0.53, 0.53);
                GL11.glTranslated(0.0, 1.0, 1.0);
            }
            case EQUIPPED -> {
                GL11.glScaled(0.53, 0.53, 0.53);
                GL11.glTranslated(2.0, 0.8, 1.5);
                GL11.glRotated(90, 0, 1, 0);
            }
            case EQUIPPED_FIRST_PERSON -> {
                GL11.glScaled(0.53, 0.53, 0.53);
                GL11.glTranslated(1.9, 1.0, 1.0);
                GL11.glRotated(90, 0, 1, 0);
            }
            case INVENTORY -> {
                GL11.glTranslated(0.5, 0.35, 0.0);
                GL11.glScaled(0.53, 0.53, 0.53);
                GL11.glRotated(180, 0, 1, 0);
            }
            default -> {}
        }
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        model.renderAll();
        GL11.glPopMatrix();
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        if (this.handleRenderType(item, type)) {
            switch (type) {
                case EQUIPPED, EQUIPPED_FIRST_PERSON, INVENTORY, ENTITY -> this
                        .renderJet(type, (RenderBlocks) data[0], item, -0.5f, -0.5f, -0.5f);
                default -> {}
            }
        }
    }

}
