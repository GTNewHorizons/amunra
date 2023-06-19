package de.katzenpapst.amunra.network.packet;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.mothership.MothershipWorldData;
import de.katzenpapst.amunra.tick.TickHandlerServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import micdoodle8.mods.galacticraft.core.network.NetworkUtil;

public class ConnectionPacketAR {

    public static final String CHANNEL = "amunra$connection";
    public static FMLEventChannel bus;

    public static final byte ID_MOTHERSHIP_LIST = (byte) 150;
    public static final byte ID_CONFIG_OVERRIDE = (byte) 151;

    public void handle(final ByteBuf payload, final EntityPlayer player) {
        final int packetId = payload.readByte();
        NBTTagCompound nbt;
        // now try this
        try {
            nbt = NetworkUtil.readNBTTagCompound(payload);
        } catch (final IOException e) {
            AmunRa.LOGGER.error("Could not read NBT data from payload", e);
            return;
        }

        switch (packetId) {
            case ID_MOTHERSHIP_LIST -> {
                if (TickHandlerServer.mothershipData == null) {
                    TickHandlerServer.mothershipData = new MothershipWorldData(MothershipWorldData.saveDataID);
                }
                TickHandlerServer.mothershipData.readFromNBT(nbt);
            }
            case ID_CONFIG_OVERRIDE -> AmunRa.config.setServerOverrideData(nbt);
            default -> {}
        }
        /*
         * if (payload.readInt() != 3519) { GCLog.severe("Packet completion problem for connection packet " + packetId +
         * " - maybe the player's Galacticraft version does not match the server version?"); }
         */
    }

    public static FMLProxyPacket createConfigPacket() {
        final ByteBuf payload = Unpooled.buffer();

        payload.writeByte(ID_CONFIG_OVERRIDE);

        final NBTTagCompound nbt = AmunRa.config.getServerOverrideData();

        try {
            NetworkUtil.writeNBTTagCompound(nbt, payload);
        } catch (final IOException e) {
            AmunRa.LOGGER.error("Could not write NBT data to payload", e);
        }

        return new FMLProxyPacket(payload, CHANNEL);
    }

    public static FMLProxyPacket createMothershipPacket() {
        final ByteBuf payload = Unpooled.buffer();

        payload.writeByte(ID_MOTHERSHIP_LIST);

        final NBTTagCompound nbt = new NBTTagCompound();
        TickHandlerServer.mothershipData.writeToNBT(nbt);

        try {
            NetworkUtil.writeNBTTagCompound(nbt, payload);
        } catch (final IOException e) {
            AmunRa.LOGGER.error("Could not write NBT data to payload", e);
        }

        return new FMLProxyPacket(payload, CHANNEL);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPacketData(final FMLNetworkEvent.ClientCustomPacketEvent event) {
        final FMLProxyPacket pkt = event.packet;

        this.onFMLProxyPacketData(event.manager, pkt, Minecraft.getMinecraft().thePlayer);
    }

    @SubscribeEvent
    public void onPacketData(final FMLNetworkEvent.ServerCustomPacketEvent event) {
        final FMLProxyPacket pkt = event.packet;

        this.onFMLProxyPacketData(event.manager, pkt, ((NetHandlerPlayServer) event.handler).playerEntity);
    }

    public void onFMLProxyPacketData(final NetworkManager manager, final FMLProxyPacket packet,
            final EntityPlayer player) {
        try {
            if (packet == null || packet.payload() == null)
                throw new RuntimeException("Empty packet sent to Amunra channel");
            final ByteBuf data = packet.payload();
            this.handle(data, player);
        } catch (final Exception e) {
            AmunRa.LOGGER.error("Amunra login packet handler: Failed to read packet", e);
        }
    }

}
