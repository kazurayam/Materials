package com.kazurayam.carmina.visualtesting

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.material.Helpers

import spock.lang.Ignore
import spock.lang.Specification

/**
 * http://spockframework.org/spock/docs/1.0/spock_primer.html
 */
class ImageMagickVisualTestingDriverSpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(ImageMagickVisualTestingDriverSpec.class)

    private static Path workdir_
    private static Path mask_
    private static Path resourcesdir_
    private static OutputStream out_
    private static OutputStream err_

    private ImageMagickVisualTestingDriver imvtd

    // fixture methods
    def setup() {
        out_ = new ByteArrayOutputStream()
        err_ = new ByteArrayOutputStream()
        imvtd = new ImageMagickVisualTestingDriver()
    }

    def cleanup() {
        out_.close()
        err_.close()
    }

    def setupSpec() {
        resourcesdir_ = Paths.get("./src/test/fixture")
        workdir_ = Paths.get("./build/tmp/${ImageMagickVisualTestingDriver.getName()}")
        Helpers.deleteDirectory(workdir_)
        workdir_.toFile().mkdirs()
        mask_ = workdir_.resolve('mask.png')
    }

    def cleanupSpec() {}

    // feature methods
    @Ignore
    def testRunImagemagickCommandConvertNoArg() {
        setup:
        String[] args = ['convert'] as String[]
        when:
        int ret = ImageMagickVisualTestingDriver.runImageMagickCommand(args, out_, err_)
        then:
        ret == 1
    }

    /**
     * ImageMagickVisualTester#runImagemagickCommandをテストする
     *
     * see http://techblog.hotwire.com/2016/05/19/image-comparison-in-automated-testing/
     *
     * # copy the baseline image into a new file and
     * # paint the while mask image with white color
     * convert ./resources/photo1.jpg -fill white -colorize 100% ./tmp/mask.png
     *
     * @return
     */
    def testRunImagemagickCommand_step1() {
        setup:
        Path photo1 = resourcesdir_.resolve('photo1.png')
        String[] args = ['convert',
            "${photo1.toString()}",
            '-fill', 'white', '-colorize', '100%',
            "${mask_.toString()}"
            ] as String[]
        when:
        int ret = ImageMagickVisualTestingDriver.runImageMagickCommand(args, out_, err_)
        then:
        ret == 0
    }

    /**
     * # Add rectangle representing ignored areas to the mask image
     * # rectangles must be filled with black color
     * convert ./tmp/mask.png -fill black -draw "rectangle 0.0 480,32" ./tmp/mask.png
     */
    def testRunImagemagickCommand_step2() {
        setup:
        String[] args = ['convert',
            "${mask_.toString()}",
            '-fill', 'black', '-draw', 'rectangle 0,0 480,32',
            "${mask_.toString()}"] as String[]
        when:
        int ret = ImageMagickVisualTestingDriver.runImageMagickCommand(args, out_, err_)
        then:
        ret == 0
    }

    /**
     * # Apply the mask image as an opacity mask to both baseline and new image
     * convert ./resources/photo1.png ./tmp/mask.png -compose copy-opacity -composite ./tmp/photo1.png
     */
    def testRunImagemagickCommand_step3() {
        setup:
        Path photo1 = resourcesdir_.resolve('photo1.png')
        Path masked = workdir_.resolve('photo1.png')
        String[] args = ['convert',
            "${photo1.toString()}",
            "${mask_.toString()}",
            '-compose', 'copy-opacity', '-composite',
            "${masked.toString()}"] as String[]
        when:
        int ret = ImageMagickVisualTestingDriver.runImageMagickCommand(args, out_, err_)
        then:
        ret == 0
    }

    /**
     * # Apply the mask image as an opacity mask to both baseline and new image
     * convert ./resources/photo2.png ./tmp/mask.png -compose copy-opacity -composite ./tmp/photo2.png
     */
    def testRunImagemagickCommand_step4() {
        setup:
        Path photo2 = resourcesdir_.resolve('photo2.png')
        Path masked = workdir_.resolve('photo2.png')
        String[] args = ['convert',
            "${photo2.toString()}",
            "${mask_.toString()}",
            '-compose', 'copy-opacity', '-composite',
            "${masked.toString()}"] as String[]
        when:
        int ret = ImageMagickVisualTestingDriver.runImageMagickCommand(args, out_, err_)
        then:
        ret == 0
    }

    /**
     * # compare the two images and produce delta image
     * compare ./tmp/photo1.png ./tmp/photo2.png ./tmp/delta.png
     * ret=$?
     */
    def testRunImagemagickCommand_step5() {
        setup:
        Path masked1 = workdir_.resolve('photo1.png')
        Path masked2 = workdir_.resolve('photo2.png')
        Path delta = workdir_.resolve('delta.png')
        String[] args = ['compare',
            "${masked1.toString()}",
            "${masked2.toString()}",
            "${delta.toString()}"] as String[]
        when:
        int ret = ImageMagickVisualTestingDriver.runImageMagickCommand(args, out_, err_)
        then:
        // ふたつの画像を比較して差異があるので1がreturnされるはず。
        ret == 1
    }

    /**
     * # remove opacity (this extra step is needed, otherwise ignored
     * # area will not be visible)
     * convert ./tmp/photo1.png -alpha off ./tmp/photo1.png
     */
    def testRunImagemagickCommand_step6() {
        setup:
        Path masked1 = workdir_.resolve('photo1.png')
        String[] args = ['convert',
            "${masked1}",
            '-alpha', 'off',
            "${masked1}"
            ] as String[]
        when:
        int ret = ImageMagickVisualTestingDriver.runImageMagickCommand(args, out_, err_)
        then:
        ret == 0
    }

    /**
     * # remove opacity (this extra step is needed, otherwise ignored
     * # area will not be visible)
     * convert ./tmp/photo2.png -alpha off ./tmp/photo2.png
     */
    def testRunImagemagickCommand_step7() {
        setup:
        Path masked2 = workdir_.resolve('photo2.png')
        String[] args = ['convert',
            "${masked2}",
            '-alpha', 'off',
            "${masked2}"
            ] as String[]
        when:
        int ret = ImageMagickVisualTestingDriver.runImageMagickCommand(args, out_, err_)
        then:
        ret == 0
    }




    // helper methods

}
