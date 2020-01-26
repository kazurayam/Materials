package com.kazurayam.materials.repository

import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialCore
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.impl.MaterialCoreImpl

import groovy.json.JsonOutput
import spock.lang.IgnoreRest
import spock.lang.Specification

class RepositoryRootSpec2 extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryRootSpec2.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture_origin")

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(RepositoryRootSpec2.class)}")
        if (workdir_.toFile().exists()) {
            Helpers.deleteDirectoryContents(workdir_)
        } else {
            workdir_.toFile().mkdirs()
        }
        
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    
    @IgnoreRest
    def test_getMaterial_withMaterialCore() {
        setup:
        Path testCaseDir = workdir_.resolve("test_getMaterial_withMaterialCore")
        Helpers.copyDirectory(fixture_, testCaseDir)
        Path materialsDir = testCaseDir.resolve('Materials')
        RepositoryFileScanner scanner = new RepositoryFileScanner(materialsDir)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        String jsonText = '''
{
    "Material": {
        "path": "build/tmp/testOutput/RepositoryRootSpec/testGetMaterial_withMaterialCore/Materials/47news.chronos_capture/20190404_111956/47news.visitSite/top.png",
        "hrefRelativeToRepositoryRoot": "47news.chronos_capture/20190404_111956/47news.visitSite/top.png",
        "description": "20190404_111956"
     }
}
'''
        MaterialCore mateCore = new MaterialCoreImpl(repoRoot.getBaseDir(), jsonText)
        when:
        Material mate = repoRoot.getMaterial(mateCore)
        then:
        mate != null
        mate.getFileName() == 'top.png'
        mate.getParent().getTCaseName().getValue() == '47news.visitSite'
        mate.getParent().getParent().getTSuiteName().getValue() == '47news.chronos_capture'
        mate.getParent().getParent().getTSuiteTimestamp().format() == '20190404_111956'
    }
}
