package com.kazurayam.materials

import java.awt.image.BufferedImage
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.regex.Pattern

import javax.imageio.ImageIO

import org.apache.commons.lang3.time.StopWatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.IgnoreRest
import spock.lang.Specification

class ImageMetricsSpec extends Specification {

    // fields
    private static Path workdir_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(ImageMetricsSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testSmoke() {
        setup:
        Path sourceDir = Paths.get('./src/test/fixture/Materials')
        Path targetDir = workdir_.resolve('testSmoke')
        boolean skipIfExisting = false
        int count = Helpers.copyDirectory(sourceDir, targetDir, skipIfExisting)
        Path png = targetDir.resolve('main.TS1/20180530_130419/main.TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png')
        assert Files.exists(png)
        when:
        BufferedImage bi = ImageIO.read(png.toFile())
        ImageMetrics im = new ImageMetrics(bi)
        then:
        im.getWidth() > 0
        im.getHeight() > 0
    }
}
