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
  private int g;
  private int h;
  private String i;
  private ServerSocket j = null;
  private String k;
  private Map l;

  public RemoteControlListener(IMinecraftServer paramIMinecraftServer)
  {
    super(paramIMinecraftServer);
    this.g = paramIMinecraftServer.a("rcon.port", 0);
    this.k = paramIMinecraftServer.a("rcon.password", "");
    this.i = paramIMinecraftServer.a("rcon.ip", "");
    this.h = paramIMinecraftServer.v();
    if (0 == this.g)
    {
      this.g = (this.h + 10);
      info("Setting default rcon port to " + this.g);
      paramIMinecraftServer.a("rcon.port", Integer.valueOf(this.g));
      if (0 == this.k.length()) {
        paramIMinecraftServer.a("rcon.password", "");
      }
      paramIMinecraftServer.a();
    }

    if (0 == this.i.length()) {
      this.i = "0.0.0.0";
    }

    f();
    this.j = null;
  }

  private void f() {
    this.l = new HashMap();
  }

  private void g() {
    Iterator localIterator = this.l.entrySet().iterator();
    while (localIterator.hasNext()) {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      if (!((RemoteControlSession)localEntry.getValue()).c())
        localIterator.remove();
    }
  }

  public void run()
  {
    info("RCON running on " + this.i + ":" + this.g);
    try {
      while (this.running)
        try
        {
          Socket localSocket = this.j.accept();
          localSocket.setSoTimeout(500);
          RemoteControlSession localRemoteControlSession = new RemoteControlSession(this.server, localSocket);
          localRemoteControlSession.a();
          this.l.put(localSocket.getRemoteSocketAddress(), localRemoteControlSession);

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
      b(this.j);
    }
  }

  public void a()
  {
    if (0 == this.k.length()) {
      warning("No rcon password set in '" + this.server.b_() + "', rcon disabled!");
      return;
    }

    if ((0 >= this.g) || (65535 < this.g)) {
      warning("Invalid rcon port " + this.g + " found in '" + this.server.b_() + "', rcon disabled!");
      return;
    }

    if (this.running) {
      return;
    }
    try
    {
      this.j = new ServerSocket(this.g, 0, InetAddress.getByName(this.i));
      this.j.setSoTimeout(500);
      super.a();
    } catch (IOException localIOException) {
      warning("Unable to initialise rcon on " + this.i + ":" + this.g + " : " + localIOException.getMessage());
    }
  }
}