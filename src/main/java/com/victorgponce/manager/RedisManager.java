package com.victorgponce.manager;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class RedisManager {

    private static RedisManager INSTANCE;
    private final JedisPool pool;

    private RedisManager() {
        // Pool para manejar múltiples conexiones
        // En un futuro se hara mediante archivo de config
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(16);
        config.setMaxIdle(8);

        this.pool = new JedisPool(config, "localhost", 6379);
    }

    /**
     * Returns the singleton instance of RedisManager.
     * <br><br>
     * The instance is created lazily on first access.
     * Note: initialization is not synchronized; concurrent initialization
     * from multiple threads may produce a race condition.
     *
     * @return the single RedisManager instance
     */
    public static RedisManager get() {
        if (INSTANCE == null) INSTANCE = new RedisManager();
        return INSTANCE;
    }

    /**
     * Saves a player's party NBT to Redis asynchronously.
     * <br><br>
     * The NbtCompound is written in compressed form and stored as a byte array under a key
     * prefixed with "battlelink:". The stored value is set to expire after 120 seconds.
     * This method returns immediately; the actual Redis I/O is executed on a new background
     * thread to avoid blocking the main server thread.
     * <br><br>
     * @param playerUuid the UUID of the player whose party will be saved
     * @param nbt the NbtCompound representing the player's party to serialize and store
     */
    public void saveParty(UUID playerUuid, NbtCompound nbt) {
        // Execute in a new thread to avoid freezing the Main Server Thread
        new Thread(() -> {
            try (Jedis jedis = pool.getResource()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                NbtIo.writeCompressed(nbt, baos);
                byte[] data = baos.toByteArray();

                // Use a prefix to avoid collisions with other plugins
                String key = "battlelink:" + playerUuid.toString();

                jedis.set(key.getBytes(), data);
                jedis.expire(key.getBytes(), 120); // TTL: 120 seconds

                System.out.println("[Redis] Party guardada para " + playerUuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Loads a player's party NBT from Redis.
     * <br><br>
     * The method reads a compressed NBT byte array stored under the key prefixed with "battlelink:" and the player's UUID,
     * deletes the key after reading (cleanup), and deserializes the bytes into an NbtCompound using NbtIo.readCompressed
     * with an unlimited NbtSizeTracker. If the key is not present or an error occurs, the method returns null.
     * <br><br>
     * @param playerUuid the UUID of the player whose party will be loaded
     * @return the deserialized NbtCompound if found, or null if the key does not exist or on error
     */
    public NbtCompound loadParty(UUID playerUuid) {
        try (Jedis jedis = pool.getResource()) {
            String key = "battlelink:" + playerUuid.toString();
            byte[] data = jedis.get(key.getBytes());

            if (data != null) {
                jedis.del(key.getBytes()); // Delete after reading (Clean up)

                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                // NbtSizeTracker is required in 1.21+
                return NbtIo.readCompressed(bais, NbtSizeTracker.ofUnlimitedBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}