package org.spigotmc.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.net.InetAddress;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PendingConnection;
import net.minecraft.server.ServerConnection;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bukkit.Bukkit;

/**
 * This is the NettyServerConnection class. It implements
 * {@link ServerConnection} and is the main interface between the Minecraft
 * server and this NIO implementation. It handles starting, stopping and
 * processing the Netty backend.
 */
public class NettyServerConnection extends ServerConnection {

    private final ChannelFuture socket;
    final List<PendingConnection> pendingConnections = Collections.synchronizedList(new ArrayList<PendingConnection>());

    public NettyServerConnection(MinecraftServer ms, InetAddress host, int port) {
        super(ms);
        socket = new ServerBootstrap().channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer() {
            @Override
            public void initChannel(Channel ch) throws Exception {
                try {
                    ch.config().setOption(ChannelOption.IP_TOS, 0x18);
                } catch (ChannelException ex) {
                    // IP_TOS is not supported (Windows XP / Windows Server 2003)
                }

                ch.pipeline()
                        .addLast("timer", new ReadTimeoutHandler(30))
                        .addLast("decoder", new PacketDecoder())
                        .addLast("encoder", new PacketEncoder())
                        .addLast("manager", new NettyNetworkManager());
            }
        }).group(new NioEventLoopGroup(3)).localAddress(host, port).bind();
    }

    /**
     * Pulse. This method pulses all connections causing them to update. It is
     * called from the main server thread a few times a tick.
     */
    @Override
    public void b() {
        super.b(); // pulse PlayerConnections
        for (int i = 0; i < pendingConnections.size(); ++i) {
            PendingConnection connection = pendingConnections.get(i);

            try {
                connection.c();
            } catch (Exception ex) {
                connection.disconnect("Internal server error");
                Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to handle packet: " + ex, ex);
            }

            if (connection.c) {
                pendingConnections.remove(i--);
            }
        }
    }

    /**
     * Shutdown. This method is called when the server is shutting down and the
     * server socket and all clients should be terminated with no further
     * action.
     */
    @Override
    public void a() {
        socket.channel().close().syncUninterruptibly();
    }

    /**
     * Return a Minecraft compatible cipher instance from the specified key.
     *
     * @param forEncryption whether the returned cipher shall be usable for
     * encryption or decryption
     * @param key to use as the initial vector
     * @return the initialized cipher
     */
    public static BufferedBlockCipher getCipher(boolean forEncryption, Key key) {
        BufferedBlockCipher cip = new BufferedBlockCipher(new CFBBlockCipher(new AESFastEngine(), 8));
        cip.a(forEncryption, new ParametersWithIV(new KeyParameter(key.getEncoded()), key.getEncoded(), 0, 16));
        return cip;
    }
}
