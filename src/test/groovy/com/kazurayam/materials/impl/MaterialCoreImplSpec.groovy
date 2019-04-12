package com.kazurayam.materials.impl

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.MaterialCore

import spock.lang.Specification

class MaterialCoreImplSpec extends Specification {
    
    // fields
    static Logger logger_ = LoggerFactory.getLogger(MaterialCoreImplSpec.class)

    private static Path fixtureDir
    private static Path specOutputDir
    

    // fixture methods
    def setupSpec() {
        Path projectDir = Paths.get(".")
        fixtureDir = projectDir.resolve("src/test/fixture")
        Path testOutputDir = projectDir.resolve("build/tmp/testOutput")
        specOutputDir = testOutputDir.resolve(Helpers.getClassShortName(MaterialCoreImplSpec.class))
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    def testSmoke() {
        setup:
            Path caseOutputDir = specOutputDir.resolve('testSmoke')
            Path materials = caseOutputDir.resolve('Materials').normalize()
            Files.createDirectories(materials)
            Helpers.copyDirectory(fixtureDir.resolve('Materials'), materials)
        when:
            String jsonText = '''
{
    "Material": {
        "path": "build/tmp/testOutput/MaterialCoreImplSpec/testSmoke/Materials/47News_chronos_capture/20190216_064354/main.TC_47News.visitSite/47NEWS_TOP.png",
        "hrefRelativeToRepositoryRoot": "47News_chronos_capture/20190216_064354/main.TC_47News.visitSite/47NEWS_TOP.png",
        "description": "20190322_130000"
     }
}
'''
            MaterialCore matec = new MaterialCoreImpl(materials, jsonText)
        then:
            matec != null
            matec.getBaseDir().equals(materials)
            matec.getPath().equals(
                Paths.get("build/tmp/testOutput/MaterialCoreImplSpec/testSmoke/Materials/47News_chronos_capture/20190216_064354/main.TC_47News.visitSite/47NEWS_TOP.png"))
            matec.getPathRelativeToRepositoryRoot().equals(
                Paths.get("47News_chronos_capture/20190216_064354/main.TC_47News.visitSite/47NEWS_TOP.png"))
            matec.getHrefRelativeToRepositoryRoot().equals(
                "47News_chronos_capture/20190216_064354/main.TC_47News.visitSite/47NEWS_TOP.png")
            matec.getDescription() == '20190322_130000'
    }
    
    /**
     * Reproducing a problem when
     *     com.kazurayam.materials.impl.MaterialCoreImpl DEBUG #getPathRelativeToRepositoryRoot 
     *         baseDir_ is build\tmp\testOutput\BaseIndexerSpec\testSmoke\Materials, 
     *         path_ is C:\Users\qcq0264\katalon-workspace\VisualTestingInKatalonStudio\Materials\CURA.twins_capture\20190411_130900\CURA.visitSite\appointment.php%23summary.png
     * then
     *     java.lang.IllegalArgumentException: 'other' is different type of Path
     *         at sun.nio.fs.WindowsPath.relativize(WindowsPath.java:388)
     *         at sun.nio.fs.WindowsPath.relativize(WindowsPath.java:44)
     *         at com.kazurayam.materials.impl.MaterialCoreImpl.getPathRelativeToRepositoryRoot(MaterialCoreImpl.groovy:93)
     *         ...
     * @return
     */
    def test_getPathRelativeToRepositoryRoot() {
        setup:
            String jsonText = '''{
    "Material": {
        "hrefRelativeToRepositoryRoot": "CURA.twins_exam/20190411_130902/CURA.ImageDiff_twins/CURA.visitSite/appointment.php%23summary.20190411_130900_ProductionEnv-20190411_130901_DevelopmentEnv.(0.00).png"
    }
}'''
            // I got rid of this:
            //     "path": "C:\\Users\\qcq0264\\katalon-workspace\\VisualTestingInKatalonStudio\\Materials\\CURA.twins_exam\\20190411_130902\\CURA.ImageDiff_twins\\CURA.visitSite\\appointment.php%23summary.20190411_130900_ProductionEnv-20190411_130901_DevelopmentEnv.(0.00).png",
            // because JsonSlurper found it enable to decode due to UNICODE escaping

            Path baseDir = Paths.get('build', 'tmp', 'testOutput', 'BaseIndexerSpec', 'testSmoke', 'Materials')
            MaterialCore mc = new MaterialCoreImpl(baseDir, jsonText)
        when:
            Path p = mc.getPathRelativeToRepositoryRoot()
        then:
            p != null
            p == Paths.get('CURA.twins_exam', '20190411_130902', 'CURA.ImageDiff_twins', 'CURA.visitSite',
                            'appointment.php%23summary.20190411_130900_ProductionEnv-20190411_130901_DevelopmentEnv.(0.00).png')
    }
    
    
    
    
    /**
     * 
     * @return
     */
    def test_getEncodedHrefRelativeToRepositoryRoot() {
        setup:
        String jsonText = '''{
    "Material": {
        "hrefRelativeToRepositoryRoot": "CURA.twins_exam/20190411_130902/CURA.ImageDiff_twins/CURA.visitSite/appointment.php%23summary.20190411_130900_ProductionEnv-20190411_130901_DevelopmentEnv.(0.00).png"
    }
}'''
        Path baseDir = Paths.get('build', 'tmp', 'testOutput', 'BaseIndexerSpec', 'testSmoke', 'Materials')
        MaterialCore mc = new MaterialCoreImpl(baseDir, jsonText)
    when:
        String enc = mc.getEncodedHrefRelativeToRepositoryRoot()
    then:
        enc != null
        enc == 'CURA.twins_exam/20190411_130902/CURA.ImageDiff_twins/CURA.visitSite/appointment.php%2523summary.20190411_130900_ProductionEnv-20190411_130901_DevelopmentEnv.%280.00%29.png'
    }
    
}
