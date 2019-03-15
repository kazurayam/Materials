package com.kazurayam.materials.imagedifference

import java.awt.image.BufferedImage
import java.nio.file.Path
import java.nio.file.Paths

import javax.imageio.ImageIO

import com.kazurayam.materials.imagedifference.ImageDifference

import spock.lang.Specification

class ImageDifferenceEvaluationSpec extends Specification {
    
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
    
    def testConstructor() {
        setup:
        BufferedImage bix = ImageIO.read(image6_)
        BufferedImage biy = ImageIO.read(image6_)
        when:
        ImageDifference difference = new ImageDifference(bix, biy)
        double criteria = 15.0
        ImageDifferenceEvaluation evaluation = new ImageDifferenceEvaluation(difference, criteria)
        then:
        evaluation.getImageDifference().getRatio() <= criteria
        evaluation.getImageDifference().imagesAreSimilar(criteria)
        evaluation.getCriteriaPercentage() == criteria
        evaluation.imagesAreSimilar()
    }
}

