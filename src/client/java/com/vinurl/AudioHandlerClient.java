package com.vinurl;

import com.vinurl.exe.YoutubeDL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class AudioHandlerClient {
	public static CompletableFuture<Boolean> downloadAudio(String url, String fileName) {
		return CompletableFuture.supplyAsync(() -> YoutubeDL.getInstance().executeCommand(url, "-x", "--no-progress", "--no-playlist", "--break-match-filter", "ext~=3gp|aac|flv|m4a|mov|mp3|mp4|ogg|wav|webm|opus", "--audio-format", "vorbis", "--audio-quality", VinURLClient.CONFIG.AudioBitrate().getValue(), "--postprocessor-args", String.format("ffmpeg:-ac 1 -t %s", VinURLClient.CONFIG.MaxAudioInMinutes() * 60), "--ffmpeg-location", VinURL.VINURLPATH.resolve("ffmpeg").toString(), "-o", fileNameToFile(fileName).toString()));
	}

	public static InputStream getAudioInputStream(String fileName) {
		try {
			return new FileInputStream(fileNameToFile(fileName));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public static File fileNameToFile(String fileName) {
		return new File(VinURL.VINURLPATH.resolve("client_downloads/" + fileName).toString());
	}
}