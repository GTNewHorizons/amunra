package de.katzenpapst.amunra.world.mapgen.populator;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;

import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class FillChest extends AbstractPopulator {

    protected BlockMetaPair chestBlock;
    protected String chestGenName;

    public FillChest(final int x, final int y, final int z, final BlockMetaPair chestBlock, final String chestGenName) {
        super(x, y, z);
        this.chestBlock = chestBlock;
        this.chestGenName = chestGenName;
    }

    @Override
    public boolean populate(final World world) {
        final IInventory chest = (IInventory) world.getTileEntity(this.x, this.y, this.z);

        if (chest != null) {
            // this clears the chest
            for (int i = 0; i < chest.getSizeInventory(); i++) {
                chest.setInventorySlotContents(i, null);
            }

            // hmm that is an interesting concept
            final ChestGenHooks info = ChestGenHooks.getInfo(this.chestGenName);

            WeightedRandomChestContent
                    .generateChestContents(world.rand, info.getItems(world.rand), chest, info.getCount(world.rand));
            return true;
        }
        return false;
    }

}
