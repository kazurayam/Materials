package com.kazurayam.materials.imagedifference

import java.nio.file.Files
import java.nio.file.Path

import javax.imageio.ImageIO

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * encloses 3 Path objects; the expected image, the actual image, and the diff image.
 * resolves 3 file paths using the outputDirectory param and the identifier param,
 * and writes images into 3 files
 * 
 * For example,
 * - &lt;outputDir&gt;/samplePage.exected.png
 * - &lt;outputDir&gt;/samplePage.actual.png
 * - &lt;outputDir&gt;/samplePage.diff(0.57).png
 *
 * @author kazurayam
 *
 */
class ImageDifferenceSerializer {
    
    static Logger logger_ = LoggerFactory.getLogger(ImageDifferenceSerializer.class)
    
    private ImageDifference imgDifference_
    private Path outputDirectory_
    private Path expected_
    private Path actual_
    private Path diff_

    ImageDifferenceSerializer(ImageDifference imgDifference, Path outputDirectory, String identifier)
    {
        imgDifference_ = imgDifference
        outputDirectory_ = outputDirectory
        expected_ = outputDirectory.resolve(identifier + ".expected.png")
        actual_   = outputDirectory.resolve(identifier + ".actual.png")
        diff_     = outputDirectory.resolve(identifier + ".diff(${imgDifference.getRatioAsString()}).png")
    }

    Path getExpected() {
        return expected_
    }

    Path getActual() {
        return actual_
    }

    Path getDiff() {
        return diff_
    }

    void serialize() {
        Files.createDirectories(outputDirectory_)
        ImageIO.write(imgDifference_.getExpectedImage(), "PNG", expected_.toFile())
        ImageIO.write(imgDifference_.getActualImage(),   "PNG", actual_.toFile())
        ImageIO.write(imgDifference_.getDiffImage(),     "PNG", diff_.toFile())
    }
}