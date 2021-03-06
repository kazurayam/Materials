package com.kazurayam.materials.imagedifference

import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.TSuiteName

import ru.yandex.qatools.ashot.Screenshot
import ru.yandex.qatools.ashot.comparison.ImageDiff
import ru.yandex.qatools.ashot.comparison.ImageDiffer

/**
 * accepts 2 BufferedImages as input, compare them, make a difference image,
 * and calculate the ratio of difference of the 2 input images.
 */
class ImageDifference {
    
    static Logger logger_ = LoggerFactory.getLogger(ImageDifference.class)
    
    private BufferedImage expectedImage_
    private BufferedImage actualImage_
    private BufferedImage diffImage_
    private Double ratio_ = 0.0        // percentage
    
    ImageDifference()
    {
        expectedImage_ = null
        actualImage_ = null
    }

    ImageDifference(BufferedImage expected, BufferedImage actual)
    {
        expectedImage_ = expected
        actualImage_ = actual
        ImageDiff imgDiff = makeImageDiff(expectedImage_, actualImage_)
        ratio_ = calculateRatioPercent(imgDiff)
        diffImage_ = imgDiff.getMarkedImage()
    }

    private ImageDiff makeImageDiff(BufferedImage expected, BufferedImage actual)
    {
        Screenshot expectedScreenshot = new Screenshot(expected)
        Screenshot actualScreenshot = new Screenshot(actual)
        ImageDiff imgDiff = new ImageDiffer().makeDiff(expectedScreenshot, actualScreenshot)
        return imgDiff
    }

    BufferedImage getExpectedImage() {
        expectedImage_
    }

    BufferedImage getActualImage() {
        actualImage_
    }

    BufferedImage getDiffImage() {
        return diffImage_
    }

    /**
     *
     * @return e.g. 0.23% or 90.0%
     */
    Double getRatio() {
        return ratio_
    }

    /**
     * @return e.g. "0.23" or "90.00"
     */
    String getRatioAsString(String fmt = '%1$.2f') {
        return String.format(fmt, this.getRatio())
    }

    /**
     *
     * Round up 0.0001 to 0.01
     *
     * @param diff
     * @return
     */
    private Double calculateRatioPercent(ImageDiff diff) {
        boolean hasDiff = diff.hasDiff()
        if (!hasDiff) {
            return 0.0
        }
        int diffSize = diff.getDiffSize()
        int area = diff.getMarkedImage().getWidth() * diff.getMarkedImage().getHeight()
        Double diffRatio = diffSize / area * 100
        BigDecimal bd = new BigDecimal(diffRatio)
        BigDecimal bdUP = bd.setScale(2, BigDecimal.ROUND_UP);  // 0.001 -> 0.01
        return bdUP.doubleValue()
    }


    /**
     * @return true if the expected image and the actual image pair has
     *         greater difference than the criteria = these are different enough,
     *         otherwise false.
     */
    Boolean imagesAreDifferent(double criteria) {
        return (ratio_ > criteria)
    }

    /**
     * @return true if the expected image and the actual image pair has
     *         smaller difference than or equal to the criteria in percentage
     *         (e.g, 5.0 means five point zero percentage); 
     *         this means they are similar enough.
     *         false otherwise.
     */
    Boolean imagesAreSimilar(double criteria) {
        return (ratio_ <= criteria)
    }

    /**
     * free the memory occupied by the BufferedImages
     */
    void flush() {
        if (expectedImage_ != null) expectedImage_.flush()
        if (actualImage_ != null) actualImage_.flush()
        if (diffImage_ != null) diffImage_.flush()
    }

    /**
     * deep copy the source BufferedImage to create a new one
     *
     * @param source
     * @return
     */
    static BufferedImage copyImage(BufferedImage source) {
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType())
        Graphics2D g = b.getGraphics()
        g.drawImage(source, 0, 0, null)
        g.dispose()
        return b
    }
        
}