package com.kazurayam.materials.impl

import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TExecutionProfile
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class MaterialPairImplSpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(MaterialPairImplSpec.class)

    static MaterialRepository mr_

    // fixture methods
    def setupSpec() {
        Path projectDir = Paths.get(".")
        Path fixtureDir = projectDir.resolve("src/test/fixture")
        Path materials = fixtureDir.resolve('Materials').normalize()
        assert Files.exists(materials)
        mr_ = MaterialRepositoryFactory.createInstance(materials)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def test_getActualBufferedImage() {
        setup:
        TCaseResult tCaseResult = mr_.getTCaseResult(
                new TSuiteName('CURA/twins_capture'),
                new TExecutionProfile("CURA_DevelopmentEnv"),
                new TSuiteTimestamp('20190412_161621'),
                new TCaseName('CURA/visitSite'))
        assert tCaseResult != null

        Material mat= tCaseResult.getMaterial(Paths.get("top.png"))
        assert mat != null
        assert mat.fileExists()

        when:
        MaterialPairImpl mpi = new MaterialPairImpl()
        mpi.setActual(mat)
        BufferedImage bi = mpi.getActualBufferedImage()
        then:
        assert bi != null
        assert bi.getWidth() == 1012
        assert bi.getHeight() == 1644
    }

    def test_getExpectedBufferedImage() {
        setup:
        TCaseResult tCaseResult = mr_.getTCaseResult(
                new TSuiteName('CURA/twins_capture'),
                new TExecutionProfile("CURA_ProductionEnv"),
                new TSuiteTimestamp('20190412_161620'),
                new TCaseName('CURA/visitSite'))
        assert tCaseResult != null

        Material mat= tCaseResult.getMaterial(Paths.get("top.png"))
        assert mat != null
        assert mat.fileExists()

        when:
        MaterialPairImpl mpi = new MaterialPairImpl()
        mpi.setExpected(mat)
        BufferedImage bi = mpi.getExpectedBufferedImage()
        then:
        assert bi != null
        assert bi.getWidth() == 1012
        assert bi.getHeight() == 1644
    }

}
