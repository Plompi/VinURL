package urlmusicdiscs;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class ServerConfig {
    public static ConfigData currentData;

    static {
        File configFile = new File(URLMusicDiscs.CONFIGPATH.resolve("urlmusicdiscs/urlmusicdiscs.json").toUri());
        URLMusicDiscs.CONFIGPATH.resolve("urlmusicdiscs").toFile().mkdirs();
        Gson gsonConverter = new GsonBuilder().setPrettyPrinting().create();

        try (FileReader file = new FileReader(configFile)) {
            currentData = gsonConverter.fromJson(new JsonReader(file), ConfigData.class);
        } catch (FileNotFoundException e) {
            currentData = new ConfigData();
            try (FileWriter fileOut = new FileWriter(configFile)) {
                fileOut.write(gsonConverter.toJson(currentData));
            } catch (IOException ignored) {}
        } catch (IOException ignored) {}
    }

    public static class ConfigData {
        Map<String, String[]> whitelistedUrls = new HashMap<>() {{
            put("Youtube", new String[]{"https://youtu.be", "https://www.youtube.com", "https://youtube.com"});
            put("Discord", new String[]{"https://cdn.discordapp.com"});
            put("Dropbox", new String[]{"https://www.dropbox.com/scl","https://dropbox.com/scl"});
            put("GDrive", new String[]{"https://drive.google.com/uc"});
        }};
    }
}
