package org.bukkit.craftbukkit;

import java.util.ArrayList;
import net.minecraft.server.*;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.YamlConfiguration;
import java.util.List;

public class Spigot {
    public static boolean tabPing = false;
    public static void initialize(CraftServer server, SimpleCommandMap commandMap, YamlConfiguration configuration) {
        commandMap.register("bukkit", new org.bukkit.craftbukkit.command.RestartCommand("restart"));
        commandMap.register("bukkit", new org.bukkit.craftbukkit.command.TicksPerSecondCommand("tps"));

        int timeout = configuration.getInt("settings.timeout-time", 300);
        if (timeout == 180) {
            timeout = 300;
            server.getLogger().info("Migrating to new timeout time of 300");
            configuration.set("settings.timeout-time", timeout);
            server.saveConfig();
        }
        org.bukkit.craftbukkit.util.WatchdogThread.startThread(timeout, configuration.getBoolean("settings.restart-on-crash", false));

        server.whitelistMessage = configuration.getString("settings.whitelist-message", server.whitelistMessage);
        server.stopMessage = configuration.getString("settings.stop-message", server.stopMessage);
        server.logCommands = configuration.getBoolean("settings.log-commands", true);
        server.ipFilter = configuration.getBoolean("settings.filter-unsafe-ips", false);
        server.commandComplete = configuration.getBoolean("settings.command-complete", true);
        server.spamGuardExclusions = configuration.getStringList("settings.spam-exclusions");
        server.mapSendInterval = configuration.getInt("settings.map-send-interval", server.mapSendInterval);

        server.orebfuscatorEnabled = configuration.getBoolean("orebfuscator.enable", false);
        server.orebfuscatorEngineMode = configuration.getInt("orebfuscator.engine-mode", 1);
        server.orebfuscatorUpdateRadius = configuration.getInt("orebfuscator.update-radius", 2);
        server.orebfuscatorDisabledWorlds = configuration.getStringList("orebfuscator.disabled-worlds");
        if (server.orebfuscatorEngineMode != 1 && server.orebfuscatorEngineMode != 2) {
        	server.orebfuscatorEngineMode = 1;
        }

        if (server.chunkGCPeriod == 0) {
            server.getLogger().severe("[Spigot] You should not disable chunk-gc. Resetting period-in-ticks to 600 ticks.");
            server.chunkGCPeriod = 600;
        }

        tabPing = configuration.getBoolean("settings.tab-ping", tabPing);
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
     * @return boolean If it should always tick.
     */
    public static boolean initializeEntityActivationState(Entity entity) {
        if (entity instanceof EntityHuman
                || entity instanceof EntityArrow
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
        final int miscActivationRange = world.getWorld().miscEntityActivationRange;
        final int animalActivationRange = world.getWorld().animalEntityActivationRange;
        final int monsterActivationRange = world.getWorld().monsterEntityActivationRange;


        world.timings.activationCheck.startTiming();
        int maxRange = Math.max(monsterActivationRange, animalActivationRange);
        maxRange = Math.max(maxRange, miscActivationRange);
        if (miscActivationRange == 0 || animalActivationRange == 0 || monsterActivationRange == 0) {
            // One of them is disabled, set to view-distance
            maxRange = world.getWorld().viewDistance << 4;
        } else {
            maxRange = Math.min(world.getWorld().viewDistance << 4, maxRange); // Do not tick on edge of unloaded chunks - vanilla behavior.
        }

        AxisAlignedBB maxBB = AxisAlignedBB.a(0, 0, 0, 0, 0, 0);
        AxisAlignedBB miscBB = AxisAlignedBB.a(0, 0, 0, 0, 0, 0);
        AxisAlignedBB animalBB = AxisAlignedBB.a(0, 0, 0, 0, 0, 0);
        AxisAlignedBB monsterBB = AxisAlignedBB.a(0, 0, 0, 0, 0, 0);

        for (Entity player : new ArrayList<Entity>(world.players)) {
            growBB(maxBB, player.boundingBox, maxRange, 256, maxRange);
            growBB(miscBB, player.boundingBox, miscActivationRange, 256, miscActivationRange);
            growBB(animalBB, player.boundingBox, animalActivationRange, 256, animalActivationRange);
            growBB(monsterBB, player.boundingBox, monsterActivationRange, 256, monsterActivationRange);

            final List<Entity> list = world.getEntities(player, maxBB);
            for (Entity entity : list) {
                if (!entity.defaultActivationState) {
                    boolean isInRange = false;
                    switch (entity.activationType) {
                        case 1:
                            if (monsterActivationRange == 0 || monsterBB.a(entity.boundingBox)) {
                                isInRange = true;
                            }
                            break;
                        case 2:
                            if (animalActivationRange == 0 || animalBB.a(entity.boundingBox)) {
                                isInRange = true;
                            }
                            break;
                        case 3:
                        default:
                            if (miscActivationRange == 0 || miscBB.a(entity.boundingBox)) {
                                isInRange = true;
                            }
                    }

                    entity.isActivated = isInRange;
                }
            }
        }
        world.timings.activationCheck.stopTiming();
    }

    /**
     * If an entity is not in range, do some more checks to see if we should
     * give it a shot.
     *
     * @param entity
     * @return
     */
    public static boolean checkIfActive(Entity entity) {
        // quick checks.
        if (entity.ticksLived % 20 == 0 || !entity.onGround || entity.inWater || entity.passenger != null || entity.vehicle != null) {
            return true;
        }
        // special cases.
        if (entity instanceof EntityAnimal) {
            EntityAnimal animal = (EntityAnimal) entity;
            if (animal.isBaby() || animal.r() /*love*/) {
                return true;
            }
            return (entity instanceof EntitySheep && ((EntitySheep) entity).isSheared());
        }
        return (entity instanceof EntityArrow && !((EntityArrow) entity).inGround);

    }
}
