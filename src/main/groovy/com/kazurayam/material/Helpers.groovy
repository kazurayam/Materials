package com.kazurayam.material

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

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.StringEscapeUtils

final class Helpers {

    static Logger logger_ = LoggerFactory.getLogger(Helpers.class)



    /**
     * Constructor is hidden as this class is not supposed to be instanciated
     */
    private Helpers() {}

    /**
     * utility method which stringifies a java.time.LocalDateTime object
     * into a String of 'yyyyMMdd_HHmmss' format
     *
     * @param timestamp
     * @return string in 'yyyyMMdd_HHmmss'
     */
    static String getTimestampAsString(LocalDateTime timestamp) {
        return DateTimeFormatter
                .ofPattern("yyyyMMdd_HHmmss")
                .format(timestamp)
    }

    /**
     * utility method which creates the directory and its ancestors if not present
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
     * Check if a file is present or not. If not present,
     * create a file of 0 bytes at the specified Path with the current timestamp.
     * This simulate UNIX touch command for a Path
     *
     * @param filePath
     * @throws IOException
     */
    static void touch(Path filePath) throws IOException {
        if (Files.notExists(filePath)) {
            filePath.toFile().createNewFile()
            filePath.toFile().setLastModified(System.currentTimeMillis())
        }
    }

    /**
     * Copies descendent files and directories recursively
     * from the source directory into the target directory.
     *
     * @param source a directory from which files and directories are copied
     * @param target a directory into which files and directories are copied
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
     * returns a short name of the Class stripping the package.
     * For example, if
     * <pre>com.kazurayam.ksbackyard.screenshotsupport.ScreenshotRespsitoryImpl</pre> is given as
     * <pre>clazz</pre>, then <pre>ScreenshotRepositoryImpl</pre> is returned.
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
     * This method converts a Java string into a JSON string.
     * This method is implemented with groovy.json.StringEscapeUtils#escapeJava(String)
     *
     * @see <a href="http://docs.groovy-lang.org/latest/html/gapi/groovy/json/StringEscapeUtils.html#StringEscapeUtils()">escapeJava(String)</a>
     * @param string
     * @return
     */
    static String escapeAsJsonText(String string) {
        return StringEscapeUtils.escapeJava(string)
    }

    /**
     * returns the current time stamp in the format of 'yyyyMMdd_HHmmss'
     *
     * @return e.g., '20180616_070237'
     */
    static String now() {
        return DateTimeFormatter.ofPattern(TSuiteTimestamp.DATE_TIME_PATTERN).format(LocalDateTime.now())
    }



}
