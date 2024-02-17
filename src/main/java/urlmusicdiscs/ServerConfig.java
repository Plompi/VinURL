package urlmusicdiscs;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;


public class ServerConfig {
    public ConfigData currentData = new ConfigData();

    private Gson gsonConverter = new Gson();

    public ServerConfig() {
        Path path = FabricLoader.getInstance().getConfigDir();
        File configFile = new File(path.resolve("urlmusicdiscs.json").toUri());
        FileReader file;

        try {
            file = new FileReader(configFile);
        } catch (FileNotFoundException e) {
            String result = gsonConverter.toJson(currentData);

            try {
                FileWriter fileOut = new FileWriter(configFile);
                fileOut.write(result);
                fileOut.close();
            } catch (IOException ex) {}

            return;
        }

        currentData = gsonConverter.fromJson(new JsonReader(file), ConfigData.class);

        try {
            file.close();
        } catch (IOException e) {}
    }

    public class ConfigData {
        String whitelistedUrls[] = {
                "https://www.youtube.com"
        };
    }
}
