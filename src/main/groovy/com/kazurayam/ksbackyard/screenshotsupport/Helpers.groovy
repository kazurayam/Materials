package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

final class Helpers {

    /**
     * not to be instanciated
     */
    private Helpers() {}

    /**
     * utility method which stringifies LocalDateTime into 'yyyyMMdd_hhmmss' format
     * @param timestamp
     * @return
     */
    static String getTimestampAsString(LocalDateTime timestamp) {
        return DateTimeFormatter
                .ofPattern("yyyyMMdd_HHmmss")
                .format(timestamp)
    }

    /**
     * utility method which creates parent directory tree if not present
     *
     * @param directory
     */
    static void ensureDirs(Path directory) {
        if (!(directory.toFile().exists())) {
            try {
                Files.createDirectories(directory)
            }
            catch (IOException e) {
                System.err.println(e)
            }
        }
    }

    /**
     * JavaでShellScriptの実行 https://qiita.com/itoa06/items/9e761d53c58eeb20490e
     *
     * @param args
     * @param out
     * @return returns the valued returned by ImageMagick
     */
    static int runImagemagickCommand(String[] args, OutputStream out, OutputStream err) {
        def envVarName = 'IMAGEMAGICK_HOME'
        def path

        if (System.getenv(envVarName) != null)
            path="${System.getenv(envVarName)}/"
        else
            path=''
        if (args.length == 0)
            throw new IllegalArgumentException("Usage: <imagemagick command> args...")

        try {
            Process process = new ProcessBuilder(args[0]).start()
            int ret = process.waitFor()
            String oText = squeeze(process.getInputStream())
            String eText = squeeze(process.getErrorStream())
            transfer(oText, out)
            transfer(eText, err)
            return ret
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace()
            return -1
        }
    }

    /**
     * squeeze the output from the given InputStream, return it as a String
     *
     * @param is
     * @return
     */
    private static String squeeze(InputStream is) {
        InputStreamReader isr = new InputStreamReader(is, System.getProperty("file.encoding"))
        BufferedReader reader = new BufferedReader(isr)
        StringBuilder sb = new StringBuilder()
        int c
        while ((c = reader.read()) != -1) {
            sb.append((char)c)
        }
        return sb.toString()
    }

    private static void transfer(String str, OutputStream os) {
        OutputStreamWriter osw = new OutputStreamWriter(os, System.getProperty("file.encoding"))
        BufferedWriter writer = new BufferedWriter(osw)
        writer.write(str)
        writer.flush()
        writer.close()
    }
}
