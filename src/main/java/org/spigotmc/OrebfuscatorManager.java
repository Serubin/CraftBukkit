package org.spigotmc;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.World;
import org.bukkit.CustomTimingsHandler;

public class OrebfuscatorManager {

    // Used to keep track of which blocks to obfuscate
    private static final boolean[] obfuscateBlocks = new boolean[Short.MAX_VALUE];
    private static Byte[] ores;
    private static final CustomTimingsHandler obfuscate = new CustomTimingsHandler("xray - obfuscate");
    private static final CustomTimingsHandler update = new CustomTimingsHandler("xray - update");
    private static int ITERATOR = 0;

    // Default blocks
    static {
        for (short id : MinecraftServer.getServer().server.orebfuscatorBlocks) {
            obfuscateBlocks[id] = true;
        }

        List<Byte> blocks = new ArrayList<Byte>();
        for (int i = 0; i < obfuscateBlocks.length; i++) {
            if (obfuscateBlocks[i]) {
                if (i != Block.STONE.id && i != Block.CHEST.id && i != Block.ENDER_CHEST.id) {
                    blocks.add((byte) i);
                }
            }
        }
        ores = blocks.toArray(new Byte[blocks.size()]);
    }

    public static void updateNearbyBlocks(World world, int x, int y, int z) {
        update.startTiming();
        updateNearbyBlocks(world, x, y, z, world.getServer().orebfuscatorUpdateRadius);
        update.stopTiming();
    }

    public static void obfuscateSync(int chunkX, int chunkY, int bitmask, byte[] buffer, World world, int initRadius) {
        obfuscate.startTiming();
        obfuscate(chunkX, chunkY, bitmask, buffer, world, initRadius);
        obfuscate.stopTiming();
    }
    
    public static void obfuscateSync(int chunkX, int chunkY, int bitmask, byte[] buffer, World world) {
        obfuscateSync(chunkX, chunkY, bitmask, buffer, world, 1);
    }
    
    public static void obfuscate(int chunkX, int chunkY, int bitmask, byte[] buffer, World world) {
        obfuscate(chunkX, chunkY, bitmask, buffer, world, 1);
    }

    public static void obfuscate(int chunkX, int chunkY, int bitmask, byte[] buffer, World world, int initialRadius) {
        if (world.getServer().orebfuscatorEnabled && world.getWorld().obfuscated) {
            int index = 0;
            int startX = chunkX << 4;
            int startZ = chunkY << 4;
            for (int i = 0; i < 16; i++) {
                // If the bitmask indicates this chunk is sent...
                if ((bitmask & 1 << i) != 0) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                                byte data = buffer[index];
                                // Check if the block should be obfuscated for the default engine modes
                                if (obfuscateBlocks[data & 0xFF]) {
                                    if (initialRadius != 0 && !isWorldLoaded(world, startX + x, (i << 4) + y, startZ + z, initialRadius)) {
                                        continue;
                                    }
                                    if (initialRadius == 0 || !areAjacentBlocksTransparent(world, startX + x, (i << 4) + y, startZ + z, initialRadius)) {
                                        if (world.getServer().orebfuscatorEngineMode == 2) {
                                            // Replace with random ore.
                                            if (ITERATOR >= ores.length) {
                                                ITERATOR = 0;
                                            }
                                            buffer[index] = (byte) (int) ores[ITERATOR++];
                                        } else {
                                            if (world.getServer().orebfuscatorEngineMode == 1) {
                                                // Replace with stone
                                                buffer[index] = (byte) Block.STONE.id;
                                            }
                                        }
                                    }
                                }
                                if (++index >= buffer.length) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void updateNearbyBlocks(World world, int x, int y, int z, int radius) {
        if (world.getServer().orebfuscatorEnabled && world.getWorld().obfuscated && world.isLoaded(x, y, z)) {
            // Get block id
            int id = world.getTypeId(x, y, z);

            // See if it needs update
            if (obfuscateBlocks[id]) {
                // Send the update
                world.notify(x, y, z);
            }

            // Check other blocks for updates
            if (radius != 0) {
                updateNearbyBlocks(world, x + 1, y, z, radius - 1);
                updateNearbyBlocks(world, x - 1, y, z, radius - 1);
                updateNearbyBlocks(world, x, y + 1, z, radius - 1);
                updateNearbyBlocks(world, x, y - 1, z, radius - 1);
                updateNearbyBlocks(world, x, y, z + 1, radius - 1);
                updateNearbyBlocks(world, x, y, z - 1, radius - 1);
            }
        }
    }
    
    private static boolean isWorldLoaded(World world, int x, int y, int z, int radius) {
        boolean toret = (y > 0 && y <= world.getHeight() && world.isLoaded(x, y, z));
        if (toret) {
            return toret || (radius > 0 && (isWorldLoaded(world, x, y + 1, z, radius - 1)
                    || isWorldLoaded(world, x, y - 1, z, radius - 1)
                    || isWorldLoaded(world, x + 1, y, z, radius - 1)
                    || isWorldLoaded(world, x - 1, y, z, radius - 1)
                    || isWorldLoaded(world, x, y, z + 1, radius - 1)
                    || isWorldLoaded(world, x, y, z - 1, radius - 1)));
        }
        
        return false;
    }

    private static boolean areAjacentBlocksTransparent(World world, int x, int y, int z, int radius) {
        return y > 0 && y <= world.getHeight()
                && !Block.l(world.getTypeId(x, y, z))
                || (radius > 0 && (areAjacentBlocksTransparent(world, x, y + 1, z, radius - 1)
                || areAjacentBlocksTransparent(world, x, y - 1, z, radius - 1)
                || areAjacentBlocksTransparent(world, x + 1, y, z, radius - 1)
                || areAjacentBlocksTransparent(world, x - 1, y, z, radius - 1)
                || areAjacentBlocksTransparent(world, x, y, z + 1, radius - 1)
                || areAjacentBlocksTransparent(world, x, y, z - 1, radius - 1)));
    }
}
