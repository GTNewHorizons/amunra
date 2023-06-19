package de.katzenpapst.amunra.tick;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerConnectionFromClientEvent;
import de.katzenpapst.amunra.network.packet.ConnectionPacketAR;
import micdoodle8.mods.galacticraft.core.util.MapUtil;

public class ConnectionEvents {

    private boolean clientConnected;

    @SubscribeEvent
    public void onConnectionReceived(final ServerConnectionFromClientEvent event) {
        event.manager.scheduleOutboundPacket(ConnectionPacketAR.createMothershipPacket());
        // config packet
        event.manager.scheduleOutboundPacket(ConnectionPacketAR.createConfigPacket());
    }

    @SubscribeEvent
    public void onConnectionOpened(final ClientConnectedToServerEvent event) {
        // stolen from GC...
        if (!event.isLocal) {
            this.clientConnected = true;
        }
        MapUtil.resetClient();
    }

    @SubscribeEvent
    public void onConnectionClosed(final ClientDisconnectionFromServerEvent event) {
        if (this.clientConnected) {
            this.clientConnected = false;
            // unregister motherships here
            if (TickHandlerServer.mothershipData != null) {
                TickHandlerServer.mothershipData.unregisterAllMotherships();
                TickHandlerServer.mothershipData = null;
            }
            /*
             * WorldUtil.unregisterPlanets(); WorldUtil.unregisterSpaceStations();
             * ConfigManagerCore.restoreClientConfigOverrideable();
             */
        }
    }

}
