package net.coobird.thumbnailator

import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import javax.imageio.ImageIO

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.model.Suffix

import net.coobird.thumbnailator.name.Rename
import spock.lang.Specification

/**
 * Trying the Thumbnailator libary
 * https://github.com/coobird/thumbnailator
 * 
 * @author kazurayam
 */
class ThumbnailsSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(ThumbnailsSpec.class);
    
        // fields
        private static Path workdir_
        private static Path fixture_ = Paths.get("./src/test/fixture")
        private static MaterialRepository mr_
        private static Material material_

        // fixture methods
        def setupSpec() {
            workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(ThumbnailsSpec.class)}")
            if (Files.exists(workdir_)) {
                Helpers.deleteDirectoryContents(workdir_)
            }
            if (!workdir_.toFile().exists()) {
                workdir_.toFile().mkdirs()
            }
            Helpers.copyDirectory(fixture_, workdir_)
            Path materialsDir = workdir_.resolve('Materials')
            Path reportsDir   = workdir_.resolve('Reports')
            mr_ = MaterialRepositoryFactory.createInstance(materialsDir, reportsDir)
            //
            TCaseResult tCaseResult = mr_.getTCaseResult(
                new TSuiteName('main/TS1'),
                new TSuiteTimestamp('20180530_130419'),
                new TCaseName('main/TC1'))
            assert tCaseResult != null
            material_ = tCaseResult.getMaterial(Paths.get('main', 'TC1'),
                        new URL('http://demoaut.katalon.com/'),
                        Suffix.NULL, FileType.PNG)
            assert material_ != null
        }
        def setup() {
            
        }
        def cleanup() {}
        def cleanupSpec() {}
    
        // feature methods
        def testFileToFile() {
            setup:
            Path caseOutput = workdir_.resolve("testSimplest")
            Files.createDirectories(caseOutput)
            when:
            File source = material_.getPath().toFile()
            Thumbnails.of(source).
                size(400, 2000).
                outputFormat("png").
                asFiles(caseOutput.toFile(), Rename.PREFIX_DOT_THUMBNAIL)
            Path thumbnail = caseOutput.resolve('thumbnail.' + source.getName())
            then:
            Files.exists(thumbnail)
        }
        
        def testRetainingOriginalWidthHeight_compareSize() {
            setup:
            Path caseOutput = workdir_.resolve("testRetainingOriginalWidthHeight_compareSize")
            Files.createDirectories(caseOutput)
            File sourceFile = material_.getPath().toFile()
            BufferedImage sourceBI = ImageIO.read(sourceFile)
            int width  = sourceBI.getWidth()
            int height = sourceBI.getHeight()
            when:
            BufferedImage targetBI = Thumbnails.of(sourceBI).
                                        size(width, height).
                                        asBufferedImage()
            assert targetBI != null
            File targetFile = caseOutput.resolve('thumbnail.http%3A%2A%2Fdemoaut.katalon.com%2F.png').toFile()
            ImageIO.write(targetBI, 'PNG', targetFile)
            then:
            targetFile.exists()
            when:
            long sourceLength = sourceFile.length()
            long targetLength = targetFile.length()
            then:
            sourceLength > targetLength
            sourceLength * 0.9 > targetLength
        }
 
        def testFixedWidth() {
            setup:
            Path caseOutput = workdir_.resolve("testFixedWidth")
            Files.createDirectories(caseOutput)
            File sourceFile = material_.getPath().toFile()
            BufferedImage sourceBI = ImageIO.read(sourceFile)
            int width  = sourceBI.getWidth()
            int height = sourceBI.getHeight()
            when:
            int targetWidth = 640
            int targetHeight = (int)Math.round((height * targetWidth * 1.0) / width)
            BufferedImage targetBI = Thumbnails.of(sourceBI).
                                        size(targetWidth, targetHeight).
                                        asBufferedImage()
            assert targetBI != null
            File targetFile = caseOutput.resolve('thumbnail.http%3A%2A%2Fdemoaut.katalon.com%2F.png').toFile()
            ImageIO.write(targetBI, 'PNG', targetFile)
            then:
            targetFile.exists()
            when:
            long sourceLength = sourceFile.length()
            long targetLength = targetFile.length()
            then:
            sourceLength > targetLength
            sourceLength * 0.5 > targetLength
            
             
            
        }       
}
