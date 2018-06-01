package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.ksbackyard.screenshotsupport.ImageMagickVisualTestingDriver

import spock.lang.Ignore
import spock.lang.Specification

/**
 * http://spockframework.org/spock/docs/1.0/spock_primer.html
 */
@Ignore
class ImageMagickVisualTestingDriverSpec extends Specification {

    // fields
    private static Path workdir
    private static Path mask
    private static Path resourcesdir
    private static OutputStream out
    private static OutputStream err

    // fixture methods
    def setup() {
        out = new ByteArrayOutputStream()
        err = new ByteArrayOutputStream()
    }

    def cleanup() {
        System.out.println("out=${out.toString()}")
        System.err.println("err=${err.toString()}")
        out.close()
        err.close()
    }

    def setupSpec() {
        resourcesdir = Paths.get("./src/test/fixture")
        workdir = Paths.get("./build/tmp/${ImageMagickVisualTestingDriver.getName()}")
        Helpers.deleteDirectory(workdir)
        workdir.toFile().mkdirs()
        mask = workdir.resolve('mask.png')
    }

    def cleanupSpec() {}

    // feature methods
    @Ignore
    def testRunImagemagickCommandConvertNoArg() {
        setup:
        String[] args = ['convert'] as String[]
        when:
        int ret = ImageMagickVisualTestingDriver.runImagemagickCommand(args, out, err)
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
        Path photo1 = resourcesdir.resolve('photo1.png')
        String[] args = ['convert',
            "${photo1.toString()}",
            '-fill', 'white', '-colorize', '100%',
            "${mask.toString()}"
            ] as String[]
        when:
        int ret = ImageMagickVisualTestingDriver.runImagemagickCommand(args, out, err)
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
            "${mask.toString()}",
            '-fill', 'black', '-draw', 'rectangle 0,0 480,32',
            "${mask.toString()}"] as String[]
        when:
        int ret = ImageMagickVisualTestingDriver.runImagemagickCommand(args, out, err)
        then:
        ret == 0
    }

    /**
     * # Apply the mask image as an opacity mask to both baseline and new image
     * convert ./resources/photo1.png ./tmp/mask.png -compose copy-opacity -composite ./tmp/photo1.png
     */
    def testRunImagemagickCommand_step3() {
        setup:
        Path photo1 = resourcesdir.resolve('photo1.png')
        Path masked = workdir.resolve('photo1.png')
        String[] args = ['convert',
            "${photo1.toString()}",
            "${mask.toString()}",
            '-compose', 'copy-opacity', '-composite',
            "${masked.toString()}"] as String[]
        when:
        int ret = ImageMagickVisualTestingDriver.runImagemagickCommand(args, out, err)
        then:
        ret == 0
    }

    /**
     * # Apply the mask image as an opacity mask to both baseline and new image
     * convert ./resources/photo2.png ./tmp/mask.png -compose copy-opacity -composite ./tmp/photo2.png
     */
    def testRunImagemagickCommand_step4() {
        setup:
        Path photo2 = resourcesdir.resolve('photo2.png')
        Path masked = workdir.resolve('photo2.png')
        String[] args = ['convert',
            "${photo2.toString()}",
            "${mask.toString()}",
            '-compose', 'copy-opacity', '-composite',
            "${masked.toString()}"] as String[]
        when:
        int ret = ImageMagickVisualTestingDriver.runImagemagickCommand(args, out, err)
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
        Path masked1 = workdir.resolve('photo1.png')
        Path masked2 = workdir.resolve('photo2.png')
        Path delta = workdir.resolve('delta.png')
        String[] args = ['compare',
            "${masked1.toString()}",
            "${masked2.toString()}",
            "${delta.toString()}"] as String[]
        when:
        int ret = ImageMagickVisualTestingDriver.runImagemagickCommand(args, out, err)
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
        Path masked1 = workdir.resolve('photo1.png')
        String[] args = ['convert',
            "${masked1}",
            '-alpha', 'off',
            "${masked1}"
            ] as String[]
        when:
        int ret = ImageMagickVisualTestingDriver.runImagemagickCommand(args, out, err)
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
        Path masked2 = workdir.resolve('photo2.png')
        String[] args = ['convert',
            "${masked2}",
            '-alpha', 'off',
            "${masked2}"
            ] as String[]
        when:
        int ret = ImageMagickVisualTestingDriver.runImagemagickCommand(args, out, err)
        then:
        ret == 0
    }




    // helper methods

}
