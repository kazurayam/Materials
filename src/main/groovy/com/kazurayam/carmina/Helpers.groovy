package com.kazurayam.carmina

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static java.nio.file.FileVisitResult.*

import java.nio.file.FileAlreadyExistsException
import java.nio.file.FileVisitOption
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

final class Helpers {

    static Logger logger = LoggerFactory.getLogger(Helpers.class)

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
    static boolean copyDirectory(Path source, Path target) {
        if (source == null) {
            throw new IllegalArgumentException('source is null')
        }
        if (!Files.exists(source)) {
            throw new IllegalArgumentException("${source.normalize().toAbsolutePath()} does not exist")
        }
        if (!Files.isDirectory(source)) {
            throw new IllegalArgumentException("${source.normalize().toAbsolutePath()} is not a directory")
        }
        if (!Files.isReadable(source)) {
            throw new IllegalArgumentException("${source.normalize().toAbsolutePath()} is not readable")
        }
        if (target == null) {
            throw new IllegalArgumentException('target is null')
        }
        Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS),
            Integer.MAX_VALUE,
            new SimpleFileVisitor<Path>() {
                @Override
                FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) throws IOException {
                    Path targetdir = target.resolve(source.relativize(dir))
                    try {
                        Files.copy(dir, targetdir)
                    } catch (FileAlreadyExistsException e) {
                        if (!Files.isDirectory(targetdir))
                            throw e
                    }
                    return CONTINUE
                }
                @Override
                FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
                    Path targetFile = target.resolve(source.relativize(file))
                    if (Files.exists(targetFile)) {
                        Files.delete(targetFile)
                    }
                    Files.copy(file, targetFile)
                    return CONTINUE
                }
            }
        )
    }

    /**
     * clazzが com.kazurayam.ksbackyard.screenshotsupport.ScreenshotRespsitoryImpl であるとき
     * packageを除外した短い名前すなわち ScreenshotRepositoryImpl を返す
     *
     * @param clazz
     * @return
     */
    static String getClassShortName(Class clazz) {
        String fqdn = clazz.getName()
        String packageStr = clazz.getPackage().getName()
        String shortName = fqdn.replaceFirst(packageStr + '.', '')
        return shortName
    }


    /**
     * JSONの値として含まれることになるstringのなかに含まれる文字を適切にエスケープする
     *
     * @param string
     * @return
     */
    static String escapeAsJsonText(String string) {
        char[] chars = string.toCharArray()
        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '/') { sb.append('\\/') }
            else if (chars[i] == '\\') { sb.append('\\\\') }
            else if (chars[i] == '"') { sb.append('\\"') }
            else { sb.append(chars[i]) }
        }
        return sb.toString()
    }

}
