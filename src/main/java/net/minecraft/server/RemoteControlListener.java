package net.minecraft.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class RemoteControlListener extends RemoteConnectionThread
{
  private int h;
  private int i;
  private String j;
  private ServerSocket k;
  private String l;
  private Map m;

  public RemoteControlListener(IMinecraftServer paramIMinecraftServer)
  {
    super(paramIMinecraftServer, "RCON Listener");
    this.h = paramIMinecraftServer.a("rcon.port", 0);
    this.l = paramIMinecraftServer.a("rcon.password", "");
    this.j = paramIMinecraftServer.a("rcon.ip", "");
    this.i = paramIMinecraftServer.y();
    if (0 == this.h)
    {
      this.h = (this.i + 10);
      info("Setting default rcon port to " + this.h);
      paramIMinecraftServer.a("rcon.port", Integer.valueOf(this.h));
      if (0 == this.l.length()) {
        paramIMinecraftServer.a("rcon.password", "");
      }
      paramIMinecraftServer.a();
    }

    if (0 == this.j.length()) {
      this.j = "0.0.0.0";
    }

    f();
    this.k = null;
  }

  private void f() {
    this.m = new HashMap();
  }

  private void g() {
    Iterator localIterator = this.m.entrySet().iterator();
    while (localIterator.hasNext()) {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      if (!((RemoteControlSession)localEntry.getValue()).c())
        localIterator.remove();
    }
  }

  public void run()
  {
    info("RCON running on " + this.j + ":" + this.h);
    try {
      while (this.running)
        try
        {
          Socket localSocket = this.k.accept();
          localSocket.setSoTimeout(500);
          RemoteControlSession localRemoteControlSession = new RemoteControlSession(this.server, localSocket);
          localRemoteControlSession.a();
          this.m.put(localSocket.getRemoteSocketAddress(), localRemoteControlSession);

          g();
        }
        catch (SocketTimeoutException localSocketTimeoutException) {
          g();
        } catch (IOException localIOException) {
          if (this.running)
            info("IO: " + localIOException.getMessage());
        }
    }
    finally
    {
      b(this.k);
    }
  }

  public void a()
  {
    if (0 == this.l.length()) {
      warning("No rcon password set in '" + this.server.b() + "', rcon disabled!");
      return;
    }

    if ((0 >= this.h) || (65535 < this.h)) {
      warning("Invalid rcon port " + this.h + " found in '" + this.server.b() + "', rcon disabled!");
      return;
    }

    if (this.running) {
      return;
    }
    try
    {
      this.k = new ServerSocket(this.h, 0, InetAddress.getByName(this.j));
      this.k.setSoTimeout(500);
      super.a();
    } catch (IOException localIOException) {
      warning("Unable to initialise rcon on " + this.j + ":" + this.h + " : " + localIOException.getMessage());
    }
  }
}