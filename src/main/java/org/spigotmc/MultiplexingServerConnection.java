package org.spigotmc;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import net.minecraft.server.DedicatedServerConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PendingConnection;
import net.minecraft.server.ServerConnection;
import org.bukkit.Bukkit;

public class MultiplexingServerConnection extends ServerConnection {

    private static final boolean NETTY_DISABLED = Boolean.getBoolean("org.spigotmc.netty.disabled");
    private final Collection<ServerConnection> children = new HashSet<ServerConnection>();
    private final List<PendingConnection> pending = Collections.synchronizedList(new ArrayList<PendingConnection>());
    private final HashMap<InetAddress, Long> throttle = new HashMap<InetAddress, Long>();

    public MultiplexingServerConnection(MinecraftServer ms) {
        super(ms);

        // Add primary connection
        start(ms.server.getIp(), ms.server.getPort());
        // Add all other connections
        for (InetSocketAddress address : ms.server.getSecondaryHosts()) {
            start(address.getAddress().getHostAddress(), address.getPort());
        }
    }

    private void start(String ipAddress, int port) {
        try {
            // Calculate address, can't use isEmpty due to Java 5
            InetAddress socketAddress = (ipAddress.length() == 0) ? null : InetAddress.getByName(ipAddress);
            // Say hello to the log
            d().getLogger().info("Starting listener #" + children.size() + " on " + (socketAddress == null ? "*" : ipAddress) + ":" + port);
            // Start connection: Netty / non Netty
            ServerConnection listener = (NETTY_DISABLED) ? new DedicatedServerConnection(d(), socketAddress, port) : new org.spigotmc.netty.NettyServerConnection(d(), socketAddress, port);
            // Register with other connections
            children.add(listener);
            // Gotta catch em all
        } catch (Throwable t) {
            // Just print some info to the log
            t.printStackTrace();
            d().getLogger().warning("**** FAILED TO BIND TO PORT!");
            d().getLogger().warning("The exception was: {0}", t);
            d().getLogger().warning("Perhaps a server is already running on that port?");
        }
    }

    /**
     * close.
     */
    @Override
    public void a() {
        for (ServerConnection child : children) {
            child.a();
        }
    }

    /**
     * Pulse. This method pulses all connections causing them to update. It is
     * called from the main server thread a few times a tick.
     */
    @Override
    public void b() {
        super.b(); // pulse PlayerConnections
        for (int i = 0; i < pending.size(); ++i) {
            PendingConnection connection = pending.get(i);

            try {
                connection.c();
            } catch (Exception ex) {
                connection.disconnect("Internal server error");
                Bukkit.getServer().getLogger().log(Level.WARNING, "Failed to handle packet: " + ex, ex);
            }

            if (connection.b) {
                pending.remove(i--);
            }
        }
    }

    /**
     * Remove the user from connection throttle. This should fix the server ping
     * bugs.
     *
     * @param address the address to remove
     */
    public void unThrottle(InetAddress address) {
        if (address != null) {
            synchronized (throttle) {
                throttle.remove(address);
            }
        }
    }

    /**
     * Add a connection to the throttle list.
     *
     * @param address
     * @return Whether they must be disconnected
     */
    public boolean throttle(InetAddress address) {
        long currentTime = System.currentTimeMillis();
        synchronized (throttle) {
            Long value = throttle.get(address);
            if (value != null && !address.isLoopbackAddress() && currentTime - value < d().server.getConnectionThrottle()) {
                throttle.put(address, currentTime);
                return true;
            }

            throttle.put(address, currentTime);
        }
        return false;
    }

    public void register(PendingConnection conn) {
        pending.add(conn);
    }
}
