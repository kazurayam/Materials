package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class Helpers {

    private static Logger log = LoggerFactory.getLogger(ScreenshotRepository.class)

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
        if (args.length == 0) {
            throw new IllegalArgumentException("Usage: <imagemagick command> args...")
        }

        def envVarName = 'IMAGEMAGICK_HOME'
        if (System.getenv(envVarName) != null) {
            args[0] = "${System.getenv(envVarName)}/${args[0]}"
        }
        log.info("args=${args}")

        try {
            Process process = new ProcessBuilder(args).start()

            // 外部プロセスが返って来ない場合にそなえて外部プロセスの標準出力と標準エラー出力を別スレッドでsqueezeする。
            // https://qiita.com/shintaness/items/6dd91260726e555c49e5
            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    transfer(squeeze(process.getErrorStream()), err)
                }
            })
            th.start()
            transfer(squeeze(process.getInputStream()), out)

            int ret = process.waitFor()
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
