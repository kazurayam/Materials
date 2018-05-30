package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Collectors

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
        if (!(Files.exists(directory))) {
            try {
                Files.createDirectories(directory)
            }
            catch (IOException e) {
                System.err.println(e)
            }
        }
    }

    /**
     * force-delete the directory and its contents(files and directories)
     *
     * @param directoryToBeDeleted
     * @return
     */
    static boolean deleteDirectory(Path directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.toFile().listFiles()
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file.toPath())
            }
        }
        return directoryToBeDeleted.toFile().delete()
    }

    /**
     * simulate UNIX touch command for a Path
     *
     * @param filePath
     * @throws IOException
     */
    static void touch(Path filePath) throws IOException{
        filePath.toFile().createNewFile()
        filePath.toFile().setLastModified(System.currentTimeMillis())
    }

    /**
     *
     * @param dirFrom
     * @param dirTo
     * @return
     */
    static boolean copyDirectory(Path dirFrom, Path dirTo) {
        if (dirFrom == null) {
            throw new IllegalArgumentException('dirFrom is null')
        }
        if (!Files.exists(dirFrom)) {
            throw new IllegalArgumentException("${dirFrom.normalize().toAbsolutePath()} does not exist")
        }
        if (!Files.isDirectory(dirFrom)) {
            throw new IllegalArgumentException("${dirFrom.normalize().toAbsolutePath()} is not a directory")
        }
        if (!Files.isReadable(dirFrom)) {
            throw new IllegalArgumentException("${dirFrom.normalize().toAbsolutePath()} is not readable")
        }
        if (dirTo == null) {
            throw new IllegalArgumentException('dirTo is null')
        }
        try {
            ensureDirs(dirTo)
            List<Path> childrenFrom = Files.list(dirFrom).collect(Collectors.toList())
            for (Path childFrom : childrenFrom) {
                if (Files.isDirectory(childFrom)) {
                    // childFrom is a directory
                    // make a directoy with same name in dirTo
                    Path childTo = dirTo.resolve(childFrom.getFileName())
                    ensureDirs(childTo)
                    // and recurse
                    copyDirectory(childFrom, childTo)
                } else {
                    // childFrom is a file
                    Path childTo = dirTo.resolve(childFrom.getFileName())
                    if (Files.exists(childTo)) {
                        if (Files.isDirectory(childTo)) {
                            deleteDirectory(childTo)
                        } else {
                            Files.delete(childTo)
                        }
                    }
                    // copy the file
                    Files.copy(childFrom, childTo)
                }
            }
            return true
        } catch (IOException ex) {
            ex.printStackTrace()
            return false
        }
    }

}
