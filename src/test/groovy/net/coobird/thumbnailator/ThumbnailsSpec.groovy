package net.coobird.thumbnailator

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp

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
    
        // fixture methods
        def setupSpec() {
            workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(ThumbnailsSpec.class)}")
            if (!workdir_.toFile().exists()) {
                workdir_.toFile().mkdirs()
            }
            Helpers.copyDirectory(fixture_, workdir_)
            Path materialsDir = workdir_.resolve('Materials')
            Path reportsDir   = workdir_.resolve('Reports')
            mr_ = MaterialRepositoryFactory.createInstance(materialsDir, reportsDir)
        }
        def setup() {}
        def cleanup() {}
        def cleanupSpec() {}
    
        // feature methods
        def testSimplest() {
            setup:
            Path caseOutput = workdir_.resolve("testSimplest")
            Files.createDirectories(caseOutput)
            when:
            List<Material> materials = this.findMaterials(
                                            new TSuiteName('main/TS1'),
                                            new TSuiteTimestamp('20180530_130419'),
                                            new TCaseName('main/TC1'))
            then:
            materials.size() > 0
        }
        
        /**
         * 
         * @param tSuiteName
         * @param tSuiteTimestamp
         * @param tCaseName
         * @return
         */
        private List<Material> findMaterials(
                                    TSuiteName tSuiteName,
                                    TSuiteTimestamp tSuiteTimestamp,
                                    TCaseName tCaseName) {
            List<Material> materials = new ArrayList<Material>()
            // TODO
            return materials
        }
    
}
