package de.katzenpapst.amunra.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.block.BlockARChest;
import de.katzenpapst.amunra.client.renderer.model.ModelARChest;

public class BlockRendererARChest implements ISimpleBlockRenderingHandler {

    private final ModelARChest chestModel = new ModelARChest();

    public BlockRendererARChest() {
        AmunRa.chestRenderId = RenderingRegistry.getNextAvailableRenderId();
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        this.renderChest(block, metadata, modelId);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
            RenderBlocks renderer) {
        // this happens in the tileentity
        return false;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return AmunRa.chestRenderId;
    }

    public void renderChest(final Block par1Block, final int par2, final float par3) {
        if (par1Block instanceof BlockARChest blockChest) {
            this.chestModel.render(blockChest, false, 0, -0.1F, 0);
        }
    }
}
