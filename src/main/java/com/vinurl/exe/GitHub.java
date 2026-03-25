package com.vinurl.exe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import static com.vinurl.util.Constants.MOD_ID;
import static com.vinurl.util.Constants.MOD_VERSION;

public class GitHub {
	private static final String USER_AGENT = "Java/%s %s/%s".formatted(System.getProperty("java.version"), MOD_ID, MOD_VERSION);
	private static final String API_VERSION = "2022-11-28";

	public record ReleaseInfo(String version, @Nullable String digest) {
		public static final ReleaseInfo EMPTY = new ReleaseInfo("", null);

		public boolean isEmpty() {
			return version.isEmpty();
		}
	}

	public static ReleaseInfo fetchLatestRelease(String repository, String assetName) {
		String url = "https://api.github.com/repos/%s/releases/latest".formatted(repository);
		try (InputStream stream = openApiStream(url);
			 InputStreamReader reader = new InputStreamReader(stream)) {
			JsonObject release = JsonParser.parseReader(reader).getAsJsonObject();

			String version = release.get("tag_name").getAsString();

			String digest = null;
			JsonArray assets = release.getAsJsonArray("assets");
			for (JsonElement asset : assets) {
				JsonObject assetObj = asset.getAsJsonObject();
				if (assetName.equals(assetObj.get("name").getAsString())) {
					JsonElement digestElement = assetObj.get("digest");
					if (digestElement != null && !digestElement.isJsonNull()) {
						digest = digestElement.getAsString();
					}
					break;
				}
			}

			return new ReleaseInfo(version, digest);
		} catch (Exception e) {
			return ReleaseInfo.EMPTY;
		}
	}

	public static InputStream openAssetStream(String repository, String assetName) throws IOException {
		String url = "https://github.com/%s/releases/latest/download/%s".formatted(repository, assetName);
		return openStream(url);
	}

	private static InputStream openApiStream(String url) throws IOException {
		HttpURLConnection conn = openConnection(url);
		conn.setRequestProperty("Accept", "application/vnd.github+json");
		conn.setRequestProperty("X-GitHub-Api-Version", API_VERSION);
		return conn.getInputStream();
	}

	private static InputStream openStream(String url) throws IOException {
		return openConnection(url).getInputStream();
	}

	private static HttpURLConnection openConnection(String url) throws IOException {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URI(url).toURL().openConnection(Minecraft.getInstance().getProxy());
			conn.setRequestProperty("User-Agent", USER_AGENT);
			return conn;
		} catch (URISyntaxException e) {
			throw new IOException("Invalid URL: " + url, e);
		}
	}
}
