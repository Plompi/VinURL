package com.vinurl;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ServerConfig {
    public ConfigData currentData;

    public ServerConfig() {
        File configFile = VinURL.CONFIGPATH.resolve("VinURLConfig.json").toFile();
        Gson gsonConverter = new GsonBuilder().setPrettyPrinting().create();

        try {
            if (configFile.getParentFile().mkdirs() || !configFile.exists()) {
                currentData = new ConfigData();
                try (FileWriter fileOut = new FileWriter(configFile)) {
                    gsonConverter.toJson(currentData, fileOut);
                }
            } else {
                try (FileReader file = new FileReader(configFile)) {
                    currentData = gsonConverter.fromJson(new JsonReader(file), ConfigData.class);
                }
            }
        } catch (IOException e) {
            VinURL.LOGGER.error("Error while initializing configuration", e);
        }
    }

    public static class ConfigData {
        public Map<String, String[]> whitelistedUrls = new HashMap<>() {{
            put("Youtube", new String[]{"https://youtu.be", "https://www.youtube.com", "https://youtube.com"});
            put("Discord", new String[]{"https://cdn.discordapp.com"});
            put("Dropbox", new String[]{"https://www.dropbox.com/scl","https://dropbox.com/scl"});
            put("GDrive", new String[]{"https://drive.google.com/uc"});
        }};
        public Boolean UpdateCheckingOnStartup = true;
        public Integer MaxAudioLengthInSeconds = 3600;
        public Integer AudioBitrate = 64;
    }
}