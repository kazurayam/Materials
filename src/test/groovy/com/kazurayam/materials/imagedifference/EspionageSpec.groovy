package com.kazurayam.materials.imagedifference

import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import javax.imageio.ImageIO

import com.kazurayam.materials.Helpers

import spock.lang.Specification

class EspionageSpec extends Specification {

    private static Path specFixtureDir
    private static Path specOutputDir
    
    def setupSpec() {
        Path projectDir = Paths.get(".")
        specFixtureDir = projectDir.resolve("src/test/fixtures/${EspionageSpec.class.getName()}")
        Path testOutputDir = projectDir.resolve("build/tmp/testOutput")
        specOutputDir = testOutputDir.resolve(Helpers.getClassShortName(EspionageSpec.class))
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    def test_ternaryOnImageDifference() {
        setup:
        Path caseOutputDir = specOutputDir.resolve("testTernaryOnImageDifference")
        Files.createDirectories(caseOutputDir)
        Helpers.copyDirectory(specFixtureDir, caseOutputDir)
        Path materialsDir = caseOutputDir.resolve('Materials')
        File f1 = materialsDir.resolve('TS1/20190624_092807/TC1/date.png').toFile()
        File f2 = materialsDir.resolve('TS1/20190703_152349/TC1/date.png').toFile()
        BufferedImage img1 = ImageIO.read(f1)
        BufferedImage img2 = ImageIO.read(f2)
        when:
        ImageDifference imgDiff = new ImageDifference(img1, img2)
        Object result = Espionage.ternary(
            imgDiff,
            { 
                println "${f1} and ${f2} are identical";
                return true
            },
            { 
                println "${f1} and ${f2} are different";
                return false
            }
        )
        then:
        result == false
    }
}
