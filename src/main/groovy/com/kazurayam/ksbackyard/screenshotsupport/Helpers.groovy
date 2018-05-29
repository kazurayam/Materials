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

}
