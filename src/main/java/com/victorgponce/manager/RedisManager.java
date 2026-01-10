package com.victorgponce.manager;

import com.victorgponce.config.ModConfig;
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

import static com.victorgponce.CobblegorBattleLinker.LOGGER;

/**
 * Singleton manager responsible for handling Redis connections via a {@link JedisPool}.
 * <p>
 * This class abstracts the low-level Redis operations, managing connection pooling,
 * serialization, and asynchronous I/O for party data storage.
 */
public class RedisManager {

    private static RedisManager INSTANCE;
    private final JedisPool pool;

    private RedisManager() {
        ModConfig config = ModConfig.get();

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(16);
        poolConfig.setMaxIdle(8);

        this.pool = new JedisPool(poolConfig, config.getRedisHost(), config.getRedisPort(), 2000, config.getRedisPassword());

        // Test Redis connection before initiation the server
        try (Jedis jedis = pool.getResource()) {
            String response = jedis.ping();
            if (!response.equals("PONG")) {
                throw new RuntimeException("[CobblegorBattleLinker] Redis no ha podido conectarse: " + response);
            }
            LOGGER.info("[CobblegorBattleLinker] Conexion a Redis exitosa en {}:{}", config.getRedisHost(), config.getRedisPort());
        } catch (Exception e) {
            LOGGER.info("[CobblegorBattleLinker] Critical: Error al conectar a Redis. Deteniendo Servidor");
            throw new RuntimeException("[CobblegorBattleLinker] Fallo crítico de conexión a Redis", e);
        }

        LOGGER.info("[CobblegorBattleLinker] Server ID actual: {}", config.getServer());
    }

    /**
     * Returns the singleton instance of RedisManager.
     * <br><br>
     * The instance is created lazily on first access.
     * Note: Initialization performs a connection test; if it fails, a RuntimeException is thrown.
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
     * The NbtCompound is compressed, serialized to bytes, and stored under a key prefixed
     * with "battlelink:". The key is assigned a TTL (Time To Live) defined in the configuration.
     *
     * @param playerUuid the UUID of the player whose party will be saved
     * @param nbt the NbtCompound representing the player's party
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

                int ttl = ModConfig.get().getRedisTtl();

                jedis.set(key.getBytes(), data);
                jedis.expire(key.getBytes(), ttl);

                LOGGER.info("[Redis] Party guardada para {}", playerUuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Loads a player's party NBT from Redis.
     * <br><br>
     * Fetches the compressed byte array associated with the player's UUID. If found, the data
     * is deleted from Redis (one-time fetch) and deserialized into an NbtCompound.
     *
     * @param playerUuid the UUID of the player whose party will be loaded
     * @return the deserialized NbtCompound if found, or null if missing or on error
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