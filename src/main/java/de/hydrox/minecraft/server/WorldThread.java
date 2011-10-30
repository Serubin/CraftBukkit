package de.hydrox.minecraft.server;

import net.minecraft.server.WorldServer;

public class WorldThread extends Thread {
	
	private WorldServer worldserver = null;
	public long onTicktime = 0;
	public long onEntitytime = 0;

	public WorldThread(WorldServer server) {
		worldserver = server;
	}
	public void run() {
        /* Drop global timeupdates
        if (this.ticks % 20 == 0) {
            this.serverConfigurationManager.a(new Packet4UpdateTime(worldserver.getTime()), worldserver.worldProvider.dimension);
        }
        // CraftBukkit end */

        long time = System.currentTimeMillis();
        worldserver.doTick();
        onTicktime += (System.currentTimeMillis()-time);

        while (worldserver.v()) {
            ;
        }

        time = System.currentTimeMillis();
        worldserver.tickEntities();
        onEntitytime += (System.currentTimeMillis()-time);
	}

}
