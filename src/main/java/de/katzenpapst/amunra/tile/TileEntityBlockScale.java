package de.katzenpapst.amunra.tile;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import de.katzenpapst.amunra.block.IMetaBlock;
import de.katzenpapst.amunra.helper.BlockMassHelper;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;

public class TileEntityBlockScale extends TileEntity {

    protected long ticks = 0;
    protected float massToDisplay = 0;
    protected BlockMetaPair lastFoundBlock = null;

    public int getRotationMeta() {
        final Block b = this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord);
        final int meta = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
        if (b instanceof IMetaBlock) {
            return ((IMetaBlock) b).getRotationMeta(meta);
        }
        return 0;
    }

    @Override
    public Packet getDescriptionPacket() {
        final NBTTagCompound data = new NBTTagCompound();
        this.writeToNBT(data);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 2, data);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setFloat("mass", this.massToDisplay);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.massToDisplay = compound.getFloat("mass");
    }

    @Override
    public void updateEntity() {
        if (this.worldObj.isRemote) {
            return;
        }
        this.ticks++;

        if (this.ticks % 80 == 0) {
            this.doUpdate();
        }
    }

    public void doUpdate() {
        final Block b = this.worldObj.getBlock(this.xCoord, this.yCoord + 1, this.zCoord);
        final int meta = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord + 1, this.zCoord);

        if (this.lastFoundBlock != null && this.lastFoundBlock.getBlock() == b
                && this.lastFoundBlock.getMetadata() == meta) {
            // nothing changed
            return;
        }

        this.lastFoundBlock = new BlockMetaPair(b, (byte) meta);

        // mass
        this.massToDisplay = BlockMassHelper
                .getBlockMass(this.worldObj, b, meta, this.xCoord, this.yCoord + 1, this.zCoord);
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    public float getCurrentMass() {
        return this.massToDisplay;
    }

}
