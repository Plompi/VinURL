package urlmusicdiscs;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


public class ServerConfig {
    public ConfigData currentData;

    public ServerConfig() {
        Path path = FabricLoader.getInstance().getConfigDir();
        File configFile = new File(path.resolve("urlmusicdiscs/urlmusicdiscs.json").toUri());
        Gson gsonConverter = new GsonBuilder().setPrettyPrinting().create();

        try (FileReader file = new FileReader(configFile)) {
            currentData = gsonConverter.fromJson(new JsonReader(file), ConfigData.class);
        } catch (FileNotFoundException e) {
            currentData = new ConfigData();
            String result = gsonConverter.toJson(currentData);
            try (FileWriter fileOut = new FileWriter(configFile)) {
                fileOut.write(result);
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
