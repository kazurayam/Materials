package com.kazurayam.carmina.material

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DownloadsDirectoryHelper {

    static Logger logger_ = LoggerFactory.getLogger(DownloadsDirectoryHelper.class)

    /**
     * Following files has a similar filename with common body part, common extension part, and different suffixes:
     *
     * - smilechart.xls
     * - smilechart(1).xls
     * - smilechart(2).xls
     * - smilechart(3).xls
     * - smilechart (1).xls
     * - smilechart (2).xls
     *
     * Given with 'smilechart.xls' as argument, this method retrieves those files and return a List<Path>
     */
    static List<Path> listSuffixedFiles(Path directory, String baseFileName) {
        if (!Files.exists(directory)) {
            throw new IllegalArgumentException("${directory.toString()} does not exist")
        }
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("${directory.toString()} is not a directory")
        }
        if (baseFileName == null) {
            throw new IllegalArgumentException("materialFileName argument is required")
        }
        MaterialFileName base = new MaterialFileName(baseFileName)
        List<Path> suffixedFiles = new ArrayList<Path>()
        for (Path file : Files.list(directory).collect(Collectors.toList())) {
            MaterialFileName mfn = new MaterialFileName(file.getFileName().toString())
            if (base.parts[1].trim() == mfn.parts[1].trim() &&
                base.parts[3].toLowerCase() == mfn.parts[3].toLowerCase()) {
                suffixedFiles.add(file)
            }
        }
        return suffixedFiles
    }

    static List<Path> listSuffixedFiles(String baseFileName) {
        Path downloadsDir = Paths.get(System.getProperty('user.home'), 'Downloads')
        return listSuffixedFiles(downloadsDir, baseFileName)
    }

    /**
     * Select all files whose file name resemples to materialFileName, regardless Suffix part, and
     * delete them all.
     *
     * This method is inteded to scavenge the $home/Downloads directory just before downloading files from Web.
     *
     * Browsers creates new files with incremented Suffix when a file name is already used. This trick makes
     * file reuse processing very complicated. A simple and easy to understand countermeasure is NOT TO MAKE FAMILY files.
     * And best way to prevent Browsers create a family file is to removed the file in the Downlods directory just before
     * downloading.
     *
     * @returns number of files deleted
     */
    static int deleteSuffixedFiles(Path directory, String baseFileName) {
        List<Path> files = listSuffixedFiles(directory, baseFileName)
        int count = 0
        for (Path file : files) {
            Files.delete(file)
            count += 1
            logger_.info("deleteded file ${file.toString()}")
        }
        return count
    }

    static int deleteSuffixedFiles(String baseFileName) {
        Path downloadsDir = Paths.get(System.getProperty('user.home'), 'Downloads')
        return deleteSuffixedFiles(downloadsDir, baseFileName)
    }
}
