package com.kazurayam.materials.imagedifference

import java.awt.image.BufferedImage
import java.nio.file.Path
import java.nio.file.Paths

import javax.imageio.ImageIO

import com.kazurayam.materials.imagedifference.ImageDifference

import spock.lang.Specification

class ImageDifferenceSpec extends Specification {

    private static File image1_
    private static File image6_
    
    def setupSpec() {
        Path projectDir = Paths.get(".")
        Path fixtureImagesDir = projectDir.resolve("src/test/fixture/images")
        image1_ = fixtureImagesDir.resolve("andrej.png").toFile()
        image6_ = fixtureImagesDir.resolve("kazurayam.png").toFile()
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    
    def testSimilarImages() {
        setup:
        BufferedImage bix = ImageIO.read(image6_)
        BufferedImage biy = ImageIO.read(image6_)
        when:
        ImageDifference difference = new ImageDifference(bix, biy)
        double criteria = 15.0
        then:
        difference.getRatio() <= criteria
        difference.imagesAreSimilar(criteria)
        ! difference.imagesAreDifferent(criteria)
    }

    
    def testDifferentImages() {
        setup:
        BufferedImage bi1 = ImageIO.read(image1_)
        BufferedImage bi6 = ImageIO.read(image6_)
        when:
        ImageDifference difference = new ImageDifference(bi1, bi6)
        double criteria = 15.0
        then:
        difference.getRatio() > criteria
        difference.imagesAreDifferent(criteria)
        ! difference.imagesAreSimilar(criteria)
    }
    
}
