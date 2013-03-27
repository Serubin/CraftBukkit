package org.bukkit.craftbukkit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import net.minecraft.server.*;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.logging.Level;
import net.minecraft.server.EntityPlayer;
import org.bukkit.Bukkit;
import org.spigotmc.Metrics;
import org.spigotmc.RestartCommand;
import org.spigotmc.WatchdogThread;

public class Spigot {

    static AxisAlignedBB maxBB = AxisAlignedBB.a(0, 0, 0, 0, 0, 0);
    static AxisAlignedBB miscBB = AxisAlignedBB.a(0, 0, 0, 0, 0, 0);
    static AxisAlignedBB animalBB = AxisAlignedBB.a(0, 0, 0, 0, 0, 0);
    static AxisAlignedBB monsterBB = AxisAlignedBB.a(0, 0, 0, 0, 0, 0);
    public static boolean tabPing = false;
    private static Metrics metrics;
    public static List<String> bungeeIPs;
    public static int textureResolution = 16;

    public static void initialize(CraftServer server, SimpleCommandMap commandMap, YamlConfiguration configuration) {
        commandMap.register("bukkit", new org.bukkit.craftbukkit.command.TicksPerSecondCommand("tps"));
        commandMap.register("restart", new RestartCommand("restart"));

        server.whitelistMessage = configuration.getString("settings.whitelist-message", server.whitelistMessage);
        server.stopMessage = configuration.getString("settings.stop-message", server.stopMessage);
        server.logCommands = configuration.getBoolean("settings.log-commands", true);
        server.ipFilter = configuration.getBoolean("settings.filter-unsafe-ips", false);
        server.commandComplete = configuration.getBoolean("settings.command-complete", true);
        server.spamGuardExclusions = configuration.getStringList("settings.spam-exclusions");

        int configVersion = configuration.getInt("config-version");
        switch (configVersion) {
            case 0:
                configuration.set("settings.timeout-time", 30);
            case 1:
                configuration.set("settings.timeout-time", 60);
        }
        configuration.set("config-version", 2);

        WatchdogThread.doStart(configuration.getInt("settings.timeout-time", 60), configuration.getBoolean("settings.restart-on-crash", false));

        server.orebfuscatorEnabled = configuration.getBoolean("orebfuscator.enable", false);
        server.orebfuscatorEngineMode = configuration.getInt("orebfuscator.engine-mode", 1);
        server.orebfuscatorUpdateRadius = configuration.getInt("orebfuscator.update-radius", 2);
        server.orebfuscatorDisabledWorlds = configuration.getStringList("orebfuscator.disabled-worlds");
        server.orebfuscatorBlocks = configuration.getShortList("orebfuscator.blocks");
        if (server.orebfuscatorEngineMode != 1 && server.orebfuscatorEngineMode != 2) {
            server.orebfuscatorEngineMode = 1;
        }

        if (server.chunkGCPeriod == 0) {
            server.getLogger().severe("[Spigot] You should not disable chunk-gc, unexpected behaviour may occur!");
        }

        tabPing = configuration.getBoolean("settings.tab-ping", tabPing);
        bungeeIPs = configuration.getStringList("settings.bungee-proxies");
        textureResolution = configuration.getInt("settings.texture-resolution", textureResolution);

        if (metrics == null) {
            try {
                metrics = new Metrics();
                metrics.start();
            } catch (IOException ex) {
                Bukkit.getServer().getLogger().log(Level.SEVERE, "Could not start metrics service", ex);
            }
        }
    }

    /**
     * Initializes an entities type on construction to specify what group this
     * entity is in for activation ranges.
     *
     * @param entity
     * @return group id
     */
    public static byte initializeEntityActivationType(Entity entity) {
        if (entity instanceof EntityMonster || entity instanceof EntitySlime) {
            return 1; // Monster
        } else if (entity instanceof EntityCreature || entity instanceof EntityAmbient) {
            return 2; // Animal
        } else {
            return 3; // Misc
        }
    }

    /**
     * These entities are excluded from Activation range checks.
     *
     * @param entity
     * @param world
     * @return boolean If it should always tick.
     */
    public static boolean initializeEntityActivationState(Entity entity, CraftWorld world) {
        if ((entity.activationType == 3 && world.miscEntityActivationRange == 0)
                || (entity.activationType == 2 && world.animalEntityActivationRange == 0)
                || (entity.activationType == 1 && world.monsterEntityActivationRange == 0)
                || entity instanceof EntityHuman
                || entity instanceof EntityItemFrame
                || entity instanceof EntityProjectile
                || entity instanceof EntityEnderDragon
                || entity instanceof EntityComplexPart
                || entity instanceof EntityWither
                || entity instanceof EntityFireball
                || entity instanceof EntityWeather
                || entity instanceof EntityTNTPrimed
                || entity instanceof EntityEnderCrystal
                || entity instanceof EntityFireworks) {
            return true;
        }

        return false;
    }

    /**
     * Utility method to grow an AABB without creating a new AABB or touching
     * the pool, so we can re-use ones we have.
     *
     * @param target
     * @param source
     * @param x
     * @param y
     * @param z
     */
    public static void growBB(AxisAlignedBB target, AxisAlignedBB source, int x, int y, int z) {
        target.a = source.a - x;
        target.b = source.b - y;
        target.c = source.c - z;
        target.d = source.d + x;
        target.e = source.e + y;
        target.f = source.f + z;
    }

    /**
     * Find what entities are in range of the players in the world and set
     * active if in range.
     *
     * @param world
     */
    public static void activateEntities(World world) {
        SpigotTimings.entityActivationCheckTimer.startTiming();
        final int miscActivationRange = world.getWorld().miscEntityActivationRange;
        final int animalActivationRange = world.getWorld().animalEntityActivationRange;
        final int monsterActivationRange = world.getWorld().monsterEntityActivationRange;

        int maxRange = Math.max(monsterActivationRange, animalActivationRange);
        maxRange = Math.max(maxRange, miscActivationRange);
        maxRange = Math.min((world.getWorld().viewDistance << 4) - 8, maxRange);

        for (Entity player : new ArrayList<Entity>(world.players)) {

            player.activatedTick = MinecraftServer.currentTick;
            growBB(maxBB, player.boundingBox, maxRange, 256, maxRange);
            growBB(miscBB, player.boundingBox, miscActivationRange, 256, miscActivationRange);
            growBB(animalBB, player.boundingBox, animalActivationRange, 256, animalActivationRange);
            growBB(monsterBB, player.boundingBox, monsterActivationRange, 256, monsterActivationRange);

            int i = MathHelper.floor(maxBB.a / 16.0D);
            int j = MathHelper.floor(maxBB.d / 16.0D);
            int k = MathHelper.floor(maxBB.c / 16.0D);
            int l = MathHelper.floor(maxBB.f / 16.0D);

            for (int i1 = i; i1 <= j; ++i1) {
                for (int j1 = k; j1 <= l; ++j1) {
                    if (world.getWorld().isChunkLoaded(i1, j1)) {
                        activateChunkEntities(world.getChunkAt(i1, j1));
                    }
                }
            }
        }
        SpigotTimings.entityActivationCheckTimer.stopTiming();
    }

    /**
     * Checks for the activation state of all entities in this chunk.
     *
     * @param chunk
     */
    private static void activateChunkEntities(Chunk chunk) {
        for (List<Entity> slice : chunk.entitySlices) {
            for (Entity entity : slice) {
                if (MinecraftServer.currentTick > entity.activatedTick) {
                    if (entity.defaultActivationState) {
                        entity.activatedTick = MinecraftServer.currentTick;
                        continue;
                    }
                    switch (entity.activationType) {
                        case 1:
                            if (monsterBB.a(entity.boundingBox)) {
                                entity.activatedTick = MinecraftServer.currentTick;
                            }
                            break;
                        case 2:
                            if (animalBB.a(entity.boundingBox)) {
                                entity.activatedTick = MinecraftServer.currentTick;
                            }
                            break;
                        case 3:
                        default:
                            if (miscBB.a(entity.boundingBox)) {
                                entity.activatedTick = MinecraftServer.currentTick;
                            }
                    }
                }
            }
        }
    }

    /**
     * If an entity is not in range, do some more checks to see if we should
     * give it a shot.
     *
     * @param entity
     * @return
     */
    public static boolean checkEntityImmunities(Entity entity) {
        // quick checks.
        if (entity.inWater /* isInWater */ || entity.fireTicks > 0) {
            return true;
        }
        if (!(entity instanceof EntityArrow)) {
            if (!entity.onGround || entity.passenger != null
                    || entity.vehicle != null) {
                return true;
            }
        } else if (!((EntityArrow) entity).inGround) {
            return true;
        }
        // special cases.
        if (entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;
            if (living.attackTicks > 0 || living.hurtTicks > 0 || living.effects.size() > 0) {
                return true;
            }
            if (entity instanceof EntityCreature && ((EntityCreature) entity).target != null) {
                return true;
            }
            if (entity instanceof EntityAnimal) {
                EntityAnimal animal = (EntityAnimal) entity;
                if (animal.isBaby() || animal.r() /*love*/) {
                    return true;
                }
                if (entity instanceof EntitySheep && ((EntitySheep) entity).isSheared()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the entity is active for this tick.
     *
     * @param entity
     * @return
     */
    public static boolean checkIfActive(Entity entity) {
        SpigotTimings.checkIfActiveTimer.startTiming();
        boolean isActive = entity.activatedTick >= MinecraftServer.currentTick || entity.defaultActivationState;

        // Should this entity tick?
        if (!isActive) {
            if ((MinecraftServer.currentTick - entity.activatedTick - 1) % 20 == 0) {
                // Check immunities every 20 ticks.
                if (checkEntityImmunities(entity)) {
                    // Triggered some sort of immunity, give 20 full ticks before we check again.
                    entity.activatedTick = MinecraftServer.currentTick + 20;
                }
                isActive = true;
            }
            // Add a little performance juice to active entities. Skip 1/4 if not immune.
        } else if (!entity.defaultActivationState && entity.ticksLived % 4 == 0 && !checkEntityImmunities(entity)) {
            isActive = false;
        }
        int x = MathHelper.floor(entity.locX);
        int z = MathHelper.floor(entity.locZ);
        // Make sure not on edge of unloaded chunk
        if (isActive && !entity.world.areChunksLoaded(x, 0, z, 16)) {
            isActive = false;
        }
        SpigotTimings.checkIfActiveTimer.stopTiming();
        return isActive;
    }

    public static void restart() {
        try {
            String startupScript = MinecraftServer.getServer().server.configuration.getString("settings.restart-script-location", "");
            final File file = new File(startupScript);
            if (file.isFile()) {
                System.out.println("Attempting to restart with " + startupScript);

                // Kick all players
                for (EntityPlayer p : (List< EntityPlayer>) MinecraftServer.getServer().getPlayerList().players) {
                    p.playerConnection.disconnect("Server is restarting");
                }
                // Give the socket a chance to send the packets
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
                // Close the socket so we can rebind with the new process
                MinecraftServer.getServer().ae().a();

                // Give time for it to kick in
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }

                // Actually shutdown
                try {
                    MinecraftServer.getServer().stop();
                } catch (Throwable t) {
                }

                // This will be done AFTER the server has completely halted
                Thread shutdownHook = new Thread() {
                    @Override
                    public void run() {
                        try {
                            String os = System.getProperty("os.name").toLowerCase();
                            if (os.contains("win")) {
                                Runtime.getRuntime().exec("cmd /c start " + file.getPath());
                            } else {
                                Runtime.getRuntime().exec(new String[]{"sh", file.getPath()});
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                shutdownHook.setDaemon(true);
                Runtime.getRuntime().addShutdownHook(shutdownHook);
            } else {
                System.out.println("Startup script '" + startupScript + "' does not exist! Stopping server.");
            }
            System.exit(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Gets the range an entity should be 'tracked' by players and visible in the client.
     * @param entity
     * @param defaultRange Default range defined by Mojang
     * @return
     */
    public static int getEntityTrackingRange(Entity entity, int defaultRange) {
        CraftWorld world = entity.world.getWorld();
        int range = defaultRange;
        if (entity instanceof EntityPlayer) {
            range = world.playerTrackingRange;
        } else if (entity.defaultActivationState || entity instanceof EntityGhast) {
            range = defaultRange;
        } else if (entity.activationType == 1) {
            range = world.monsterEntityActivationRange;
        } else if (entity.activationType == 2) {
            range = world.animalTrackingRange;
        } else if (entity instanceof EntityItemFrame || entity instanceof EntityPainting || entity instanceof EntityItem || entity instanceof EntityExperienceOrb) {
            range = world.miscTrackingRange;
        }
        if (range == 0) {
            return defaultRange;
        }
        return Math.min(world.maxTrackingRange, range);
    }
}
