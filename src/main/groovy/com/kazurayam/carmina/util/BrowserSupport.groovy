package com.kazurayam.carmina.util

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BrowserSupport {

    static Logger logger_ = LoggerFactory.getLogger(BrowserSupport.class)

    /**
     *
     * @param downloadsDirectory
     * @param totalTimeoutInMillis
     * @return
     * @throws IOException
     */
    static Path waitForFileDownloaded(Path downloadsDirectory, int requiredStabilityInMillis, int totalTimeoutInMillis) throws IOException {
        checkArguments(downloadsDirectory, requiredStabilityInMillis, totalTimeoutInMillis)
        long startTimeInMillis = System.currentTimeMillis()
        long startToNowPassedInMillis = 0
        long stablePeriodSinceInMillis = 0
        long stablePeriodUntilInMillis = 0
        long stablePeriodInMillis = 0
        FileBeingDownloaded fbd = null
        while (startToNowPassedInMillis <= totalTimeoutInMillis) {
            Path f = findLastModifiedFileInDirectory(downloadsDirectory)
            FileBeingDownloaded lastModifiedFile = new FileBeingDownloaded(f)
            logger_.debug("#waitForFileDownloaded fbd=${fbd} lastModifiedFile=${lastModifiedFile}")
            logger_.debug("#waitForFileDownloaded stablePeriodSinceInMillis=${stablePeriodSinceInMillis}" +
                " stablePeriodUntilInMillis=${stablePeriodUntilInMillis}")
            if (fbd == null || fbd != lastModifiedFile) {
                logger_.debug("#waitForFileDownloaded fbd != lastModifiedFile")
                fbd = lastModifiedFile
                stablePeriodSinceInMillis = System.currentTimeMillis()
                stablePeriodUntilInMillis = stablePeriodSinceInMillis
            } else {
                logger_.debug("#waitForFileDownloaded fbd == lastModifiedFile")
                stablePeriodUntilInMillis = System.currentTimeMillis()
                stablePeriodInMillis = stablePeriodUntilInMillis - stablePeriodSinceInMillis
                logger_.debug("#waitForFileDownloaded period=${stablePeriodInMillis} " +
                    ((stablePeriodInMillis < requiredStabilityInMillis) ? '<' : '>') +
                    " requiredStabilityInMillis=${requiredStabilityInMillis}")
                if (stablePeriodInMillis > requiredStabilityInMillis) {
                    return fbd.getPath()
                }
            }
            // wait
            Thread.sleep(1000)
            //
            startToNowPassedInMillis = System.currentTimeMillis() - startTimeInMillis
            logger_.debug("#waitForFileDownloaded startToNowPassedInMillis=${startToNowPassedInMillis}")
        }
        logger_.debug("#waitForFileDownload timePassedInMillis=${startToNowPassedInMillis}")
        return (fbd != null) ? fbd.getPath() : null
    }

    private static void checkArguments(Path downloadsDirectory, int requiredStabilityInMillis, int totalTimeoutInMillis) {
        if (!Files.isDirectory(downloadsDirectory)) {
            throw new IllegalArgumentException("${downloadsDirectory} is not a directory")
        }
        if (requiredStabilityInMillis < 3000) {
            throw new IllegalArgumentException("requiredStabilityInMillis=${requiredStabilityInMillis} should be >= 3000")
        }
        if (totalTimeoutInMillis < 3000) {
            throw new IllegalArgumentException("totalTimeoutInMillis=${totalTimeoutInMillis} should be >= 3000")
        }
        if (requiredStabilityInMillis > totalTimeoutInMillis) {
            throw new IllegalArgumentException("requiredStabilityInMillis=${requiredStabilityInMillis} should be" +
                " less than or equal to totalTimeoutInMillis=${totalTimeoutInMillis}")
        }
    }

    /**
     * scan a directory to find out a File which is last modified in the directory
     *
     * @param directory
     * @return
     * @throws IOException
     */
    static Path findLastModifiedFileInDirectory(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IOException("${directory.toString()} is not a directory")
        }
        List<Path> files = Files.list(directory).filter({p -> Files.isRegularFile(p)}).collect(Collectors.toList())
        FileTime lastFt = FileTime.from(0, TimeUnit.MILLISECONDS)
        Path lastModifiedFile = null
        for (Path f : files) {
            FileTime ft = Files.getLastModifiedTime(f)
            if (ft > lastFt) {
                lastFt = ft
                lastModifiedFile = f
            }
        }
        return lastModifiedFile
    }


}
