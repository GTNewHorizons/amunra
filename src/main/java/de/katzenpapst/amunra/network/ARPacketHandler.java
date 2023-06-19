package de.katzenpapst.amunra.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.network.IPacket;

@Sharable
public class ARPacketHandler extends SimpleChannelInboundHandler<IPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IPacket msg) {
        final INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
        final EntityPlayer player = GalacticraftCore.proxy.getPlayerFromNetHandler(netHandler);

        switch (FMLCommonHandler.instance().getEffectiveSide()) {
            case CLIENT -> msg.handleClientSide(player);
            case SERVER -> msg.handleServerSide(player);
            default -> {}
        }
    }

}
