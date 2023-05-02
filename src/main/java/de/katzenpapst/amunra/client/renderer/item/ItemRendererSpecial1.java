package de.katzenpapst.amunra.client.renderer.item;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import de.katzenpapst.amunra.block.ARBlocks;
import de.katzenpapst.amunra.client.renderer.model.ModelHydroponics;
import de.katzenpapst.amunra.client.renderer.model.ModelShuttleDock;

/**
 * Dock, Hydroponics...
 * 
 * @author katzenpapst
 *
 */
public class ItemRendererSpecial1 implements IItemRenderer {

    private final ModelShuttleDock model;
    private final ModelHydroponics modelHydro;

    public ItemRendererSpecial1() {
        model = new ModelShuttleDock();
        modelHydro = new ModelHydroponics();
    }

    @Override
    public boolean handleRenderType(final ItemStack item, final ItemRenderType type) {

        // I think this prevents this thing from doing other items
        if ((item.getItemDamage() != ARBlocks.blockShuttleDock.getMetadata()) && (item.getItemDamage() != ARBlocks.blockHydro.getMetadata())) {
            return false;
        }
        switch (type) {
            case ENTITY:
            case EQUIPPED:
            case EQUIPPED_FIRST_PERSON:
            case INVENTORY:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean shouldUseRenderHelper(final ItemRenderType type, final ItemStack item, final ItemRendererHelper helper) {
        return true;
    }

    protected void renderHydroponics(final ItemRenderType type, final RenderBlocks render, final ItemStack item) {
        GL11.glPushMatrix();
        switch (type) {
            case ENTITY:
                GL11.glTranslated(-0.5, -0.5, -0.5);
                break;
            case EQUIPPED:
                // GL11.glTranslated(0.5, -0.0, 0.5);
                // GL11.glScaled(0.65, 0.65, 0.65);
                // GL11.glRotated(90, 0, 1, 0);
                break;
            case EQUIPPED_FIRST_PERSON:
                // GL11.glTranslated(0.0, -0.7, 0.0);
                // GL11.glScaled(0.65, 0.65, 0.65);
                // GL11.glRotated(90, 0, 1, 0);
                break;
            case INVENTORY:
                GL11.glTranslated(0.0, -0.12, 0.0);
                // GL11.glScaled(0.65, 0.65, 0.65);
                // GL11.glRotated(180, 0, 1, 0);
                break;
            default:
                break;
        }
        modelHydro.render(Tessellator.instance, 1.0F, true, true, false, false);
        GL11.glPopMatrix();
    }

    protected void renderDock(final ItemRenderType type, final RenderBlocks render, final ItemStack item) {
        GL11.glPushMatrix();
        switch (type) {
            case ENTITY:
                break;
            case EQUIPPED:
                GL11.glTranslated(0.5, -0.0, 0.5);
                // GL11.glScaled(0.65, 0.65, 0.65);
                GL11.glRotated(90, 0, 1, 0);
                break;
            case EQUIPPED_FIRST_PERSON:
                GL11.glTranslated(0.0, -0.7, 0.0);
                // GL11.glScaled(0.65, 0.65, 0.65);
                GL11.glRotated(90, 0, 1, 0);
                break;
            case INVENTORY:
                GL11.glTranslated(0.0, -0.7, 0.0);
                GL11.glScaled(0.65, 0.65, 0.65);
                GL11.glRotated(180, 0, 1, 0);
                break;
            default:
                break;
        }

        model.render(Tessellator.instance, false);
        GL11.glPopMatrix();
    }

    @Override
    public void renderItem(final ItemRenderType type, final ItemStack item, final Object... data) {
        if (this.handleRenderType(item, type)) {
            switch (type) {
                case EQUIPPED:
                case EQUIPPED_FIRST_PERSON:
                case INVENTORY:
                case ENTITY:
                    if (item.getItemDamage() == ARBlocks.blockShuttleDock.getMetadata()) {
                        this.renderDock(type, (RenderBlocks) data[0], item);
                    } else if (item.getItemDamage() == ARBlocks.blockHydro.getMetadata()) {
                        this.renderHydroponics(type, (RenderBlocks) data[0], item);
                    }

                    break;
                default:
                    break;
            }
        }

    }

}
