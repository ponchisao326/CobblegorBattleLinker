package com.victorgponce.config;

import com.victorgponce.CobblegorBattleLinker;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ModConfig {

    private static ModConfig INSTANCE;

    // Default values
    private String redisHost = "localhost";
    private int redisPort = 6379;
    private int redisTtl = 120;
    private String redisPassword = "Password";
    private String server = "Server1";

    // PATH: config/CobblegorBattleLinker/config.json
    private static final Path CONFIG_DIR = Paths.get("config", CobblegorBattleLinker.MOD_ID);
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");

    private ModConfig() {
        load();
    }

    public static ModConfig get() {
        if (INSTANCE == null) {
            INSTANCE = new ModConfig();
        }
        return INSTANCE;
    }

    public void load() {
        try {
            // If the directory doesn't exist, create it
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }

            // If the file does not exist, create one with default values
            if (!Files.exists(CONFIG_FILE)) {
                save();
                return;
            }

            // Read and parse JSON
            String content = new String(Files.readAllBytes(CONFIG_FILE));
            JSONObject json = new JSONObject(content);

            this.redisHost = json.optString("redis_host", "localhost");
            this.redisPort = json.optInt("redis_port", 6379);
            this.redisTtl = json.optInt("redis_ttl", 120);
            this.server = json.optString("server_name", "Server1");
            this.redisPassword = json.optString("redis_password", "Password");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            JSONObject json = new JSONObject();
            json.put("redis_host", redisHost);
            json.put("redis_port", redisPort);
            json.put("redis_ttl", redisTtl);
            json.put("server_name", server);
            json.put("redis_password", redisPassword);

            // Save with indentation of 4 spaces for readability
            Files.write(CONFIG_FILE, json.toString(4).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getters
    public String getRedisHost() { return redisHost; }
    public int getRedisPort() { return redisPort; }
    public int getRedisTtl() { return redisTtl; }
    public String getServer() { return server; }
    public String getRedisPassword() { return redisPassword; }

}