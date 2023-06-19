package de.katzenpapst.amunra.inventory.schematic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import de.katzenpapst.amunra.item.ItemDamagePair;
import de.katzenpapst.amunra.vec.Vector3int;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.network.PacketSimple;
import micdoodle8.mods.galacticraft.core.network.PacketSimple.EnumSimplePacket;

public class SlotSchematicShuttle extends Slot {

    protected final Vector3int pos;
    protected final EntityPlayer player;
    protected final ItemDamagePair[] validItem;

    public SlotSchematicShuttle(final IInventory craftMatrix, final int slotIndex, final int xDisplay,
            final int yDisplay, final Vector3int sparkPosition, final EntityPlayer player) {
        this(craftMatrix, slotIndex, xDisplay, yDisplay, sparkPosition, player, new ItemDamagePair[] {});
    }

    public SlotSchematicShuttle(final IInventory craftMatrix, final int slotIndex, final int xDisplay,
            final int yDisplay, final Vector3int sparkPosition, final EntityPlayer player,
            final ItemDamagePair... validItems) {
        super(craftMatrix, slotIndex, xDisplay, yDisplay);
        // these coords are only for sparks, I think
        this.pos = sparkPosition;
        this.player = player;
        this.validItem = validItems;
    }

    @Override
    public void onSlotChanged() {
        if (this.player instanceof EntityPlayerMP) {
            for (EntityPlayer element : this.player.worldObj.playerEntities) {
                final EntityPlayerMP curPlayer = (EntityPlayerMP) element;

                if (curPlayer.dimension == this.player.worldObj.provider.dimensionId) {
                    final double distX = this.pos.x - curPlayer.posX;
                    final double distY = this.pos.y - curPlayer.posY;
                    final double distZ = this.pos.z - curPlayer.posZ;

                    if (distX * distX + distY * distY + distZ * distZ < 20 * 20) {
                        GalacticraftCore.packetPipeline.sendTo(
                                new PacketSimple(
                                        EnumSimplePacket.C_SPAWN_SPARK_PARTICLES,
                                        new Object[] { this.pos.x, this.pos.y, this.pos.z }),
                                curPlayer);
                    }
                }
            }
        }
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        if (this.validItem.length == 0) {
            return true; // all are valid
        }

        for (final ItemDamagePair item : this.validItem) {
            if (item.isSameItem(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }
}
