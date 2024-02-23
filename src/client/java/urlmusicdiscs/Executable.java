package urlmusicdiscs;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Executable {

    static void checkForExecutable(String DownloadURL, String FileName ,File Directory, Path FilePath) throws IOException, URISyntaxException {

        if(!Directory.exists() && !Directory.mkdirs()) {
            throw new IOException();
        }
        if (!FilePath.toFile().exists()) {downloadExecutable(DownloadURL, FileName,FilePath);}
    }

    static void downloadExecutable(String DownloadURL,String FileName, Path FilePath) throws IOException, URISyntaxException {
        if (DownloadURL.endsWith(".zip")){
            try (ZipInputStream zipInput = new ZipInputStream(getDownloadInputStream(DownloadURL))) {

                ZipEntry zipEntry = zipInput.getNextEntry();
                while (zipEntry != null) {
                    if (zipEntry.getName().endsWith(FileName)) {
                        Files.copy(zipInput, FilePath, StandardCopyOption.REPLACE_EXISTING);
                        break;
                    }
                    zipEntry = zipInput.getNextEntry();
                }

                if (SystemUtils.IS_OS_UNIX) {
                    Runtime.getRuntime().exec(new String[]{"chmod", "+x", FilePath.toString()});
                }
            }
        }
        else{
            try (InputStream in = getDownloadInputStream(DownloadURL)) {
                Files.copy(in, FilePath, StandardCopyOption.REPLACE_EXISTING);

                if (SystemUtils.IS_OS_UNIX) {
                    Runtime.getRuntime().exec(new String[]{"chmod", "+x", FilePath.toString()});
                }
            }
        }
    }

    private static InputStream getDownloadInputStream(String DownloadURL) throws IOException, URISyntaxException {
        return new URI(DownloadURL).toURL().openStream();
    }
}
