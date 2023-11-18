package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FFmpeg {
    static void checkForExecutable() throws IOException {
        File FFmpegDirectory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/ffmpeg/").toAbsolutePath().toFile();

        if (!FFmpegDirectory.exists()) {
            FFmpegDirectory.mkdirs();
        }

        String fileName = SystemUtils.IS_OS_WINDOWS ? "ffmpeg.exe" : "ffmpeg";

        System.out.println(FFmpegDirectory.toString());

        if (!FFmpegDirectory.toPath().resolve(fileName).toFile().exists()) {
            System.out.println("Installing FFmpeg!");

            File zipFile = null;

            if (SystemUtils.IS_OS_LINUX) {
                System.out.println("Linux");

                if (!FFmpegDirectory.toPath().resolve("ffmpeg.zip").toFile().exists()) {
                    InputStream in = new URL("https://cdn.discordapp.com/attachments/1067144249612714036/1175188765711552592/ffmpeg.zip").openStream();
                    Files.copy(in, FFmpegDirectory.toPath().resolve("ffmpeg.zip"), StandardCopyOption.REPLACE_EXISTING);
                }

                zipFile = FFmpegDirectory.toPath().resolve("ffmpeg.zip").toFile();;
            } else if (SystemUtils.IS_OS_MAC) {
                System.out.println("Mac");

                // TODO
            } else if (SystemUtils.IS_OS_WINDOWS) {
                System.out.println("Windows");

                if (!FFmpegDirectory.toPath().resolve("ffmpeg.zip").toFile().exists()) {
                    InputStream in = new URL("https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip").openStream();
                    Files.copy(in, FFmpegDirectory.toPath().resolve("ffmpeg.zip"), StandardCopyOption.REPLACE_EXISTING);
                }

                zipFile = FFmpegDirectory.toPath().resolve("ffmpeg.zip").toFile();;
            } else {
                System.out.println("Unknown Operating System");
            }

            if (zipFile == null || !zipFile.exists()) {
                return;
            }


            ZipInputStream zipInput = new ZipInputStream(new FileInputStream(zipFile));

            ZipEntry zipEntry = zipInput.getNextEntry();

            while (zipEntry != null) {
                System.out.println(zipEntry.getName());
                if (zipEntry.getName().endsWith("ffmpeg.exe") || zipEntry.getName().endsWith("ffmpeg")) {
                    Files.copy(zipInput, FFmpegDirectory.toPath().resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                }
                zipEntry = zipInput.getNextEntry();
            }

            zipFile.delete();
        }
    }

    static String executeFFmpegCommand(String arguments) throws IOException, InterruptedException {
        File FFmpegDirectory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/ffmpeg/").toAbsolutePath().toFile();

        String fileName = SystemUtils.IS_OS_WINDOWS ? "ffmpeg.exe" : "ffmpeg";

        String FFmpeg = FFmpegDirectory.toPath().resolve(fileName).toAbsolutePath().toString();

        // https://stackoverflow.com/questions/5711084/java-runtime-getruntime-getting-output-from-executing-a-command-line-program

        System.out.println("Running FFmpeg!");

        if (SystemUtils.IS_OS_LINUX) {
            Process resultProcess0 = Runtime.getRuntime().exec("chmod +x \"" + FFmpeg + "\"");
            InputStream resultStream0 = resultProcess0.getInputStream();
            Scanner s0 = new Scanner(resultStream0).useDelimiter("\\A");

            resultProcess0.waitFor();

            System.out.println(resultProcess0.exitValue() + " " + (s0.hasNext() ? s0.next() : null));
        }

        System.out.println(FFmpeg + " " + arguments);

        Process resultProcess = Runtime.getRuntime().exec(FFmpeg + " " + arguments);
        InputStream resultStream = resultProcess.getInputStream();

        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(resultProcess.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(resultProcess.getErrorStream()));

// Read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

// Read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }


        resultProcess.waitFor();

        System.out.println(resultProcess.exitValue());

        return "";
    }
}
