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

    public static RedisManager get() {
        if (INSTANCE == null) INSTANCE = new RedisManager();
        return INSTANCE;
    }

    // --- GUARDAR (Async recomendado, pero aquí simplificado) ---
    public void saveParty(UUID playerUuid, NbtCompound nbt) {
        // Ejecutamos en un hilo nuevo para no congelar el servidor
        new Thread(() -> {
            try (Jedis jedis = pool.getResource()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                NbtIo.writeCompressed(nbt, baos);
                byte[] data = baos.toByteArray();

                // Usamos un prefijo para no chocar con otros plugins
                String key = "battlelink:" + playerUuid.toString();

                jedis.set(key.getBytes(), data);
                jedis.expire(key.getBytes(), 60); // 60 segundos de vida

                System.out.println("[Redis] Party guardada para " + playerUuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // --- CARGAR ---
    public NbtCompound loadParty(UUID playerUuid) {
        try (Jedis jedis = pool.getResource()) {
            String key = "battlelink:" + playerUuid.toString();
            byte[] data = jedis.get(key.getBytes());

            if (data != null) {
                jedis.del(key.getBytes()); // Borrar tras leer (limpieza)

                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                // En 1.21.1 es obligatorio pasar el NbtSizeTracker
                return NbtIo.readCompressed(bais, NbtSizeTracker.ofUnlimitedBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}