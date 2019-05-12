package com.kazurayam.materials

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.impl.MaterialPairImpl
import com.kazurayam.materials.impl.MaterialPairsImpl
import com.kazurayam.materials.repository.RepositoryFileScanner
import com.kazurayam.materials.repository.RepositoryRoot

import groovy.json.JsonOutput
import spock.lang.Ignore
import spock.lang.Specification

class MaterialPairsSpec extends Specification {
    
    static Logger logger_ = LoggerFactory.getLogger(MaterialPairs.class)
    
    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static RepositoryRoot repoRoot_
    private TSuiteResult expectedTsr_
    private TSuiteResult actualTsr_
    private Material expectedMaterial_
    private Material actualMaterial_
    
    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(MaterialPairs.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        Path materialsDir = workdir_.resolve('Materials')
        Path reportsDir   = workdir_.resolve('Reports')
        RepositoryFileScanner scanner = new RepositoryFileScanner(materialsDir, reportsDir)
        scanner.scan()
        repoRoot_ = scanner.getRepositoryRoot()
    }
    def setup() {
        TSuiteName tsn = new TSuiteName('Test Suites/main/TS1')
        //
        expectedTsr_ = repoRoot_.getTSuiteResult(tsn, TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult expectedTcr = expectedTsr_.getTCaseResult(new TCaseName('main.TC1'))
        expectedMaterial_ = expectedTcr.getMaterial(Paths.get('http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
        //
        actualTsr_ = repoRoot_.getTSuiteResult(tsn, TSuiteTimestamp.newInstance('20180530_130604'))
        TCaseResult actualTcr = actualTsr_.getTCaseResult(new TCaseName('main.TC1'))
        actualMaterial_   = actualTcr.getMaterial(Paths.get('http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
    }
    def cleanup() {}
    def cleanupSpec() {}
    
    // feature methods
    def test_put() {
        setup:
        MaterialPair mp = MaterialPairImpl.newInstance().setLeft(expectedMaterial_).setRight(actualMaterial_)
        MaterialPairs mps = MaterialPairsImpl.MaterialPairs(expectedTsr_, actualTsr_)
        when:
        MaterialPair pair = mps.put(actualMaterial_.getPathRelativeToTSuiteTimestamp(), mp)
        then:
        pair == mp
    }
    
    def test_get() {
        setup:
        MaterialPair mp = MaterialPairImpl.newInstance().setLeft(expectedMaterial_).setRight(actualMaterial_)
        MaterialPairs mps = MaterialPairsImpl.MaterialPairs(expectedTsr_, actualTsr_)
        mps.put(actualMaterial_.getPathRelativeToTSuiteTimestamp(), mp)
        when:
        MaterialPair pair = mps.get(actualMaterial_.getPathRelativeToTSuiteTimestamp())
        then:
        pair == mp
    }
    
    def test_getList() {
        setup:
        MaterialPair mp = MaterialPairImpl.newInstance().setLeft(expectedMaterial_).setRight(actualMaterial_)
        MaterialPairs mps = MaterialPairsImpl.MaterialPairs(expectedTsr_, actualTsr_)
        mps.put(actualMaterial_.getPathRelativeToTSuiteTimestamp(), mp)
        when:
        List<MaterialPair> list = mps.getList()
        then:
        list != null
        list.size() == 1
    }
    
    def test_keySet() {
        setup:
        MaterialPair mp = MaterialPairImpl.newInstance().setLeft(expectedMaterial_).setRight(actualMaterial_)
        MaterialPairs mps = MaterialPairsImpl.MaterialPairs(expectedTsr_, actualTsr_)
        mps.put(actualMaterial_.getPathRelativeToTSuiteTimestamp(), mp)
        when:
        Set<Path> ks = mps.keySet()
        then:
        ks.size() == 1
    }
    
    def test_containsKey() {
        setup:
        MaterialPair mp = MaterialPairImpl.newInstance().setLeft(expectedMaterial_).setRight(actualMaterial_)
        MaterialPairs mps = MaterialPairsImpl.MaterialPairs(expectedTsr_, actualTsr_)
        mps.put(actualMaterial_.getPathRelativeToTSuiteTimestamp(), mp)
        expect:
        mps.containsKey(actualMaterial_.getPathRelativeToTSuiteTimestamp())
    }
    
    def test_size() {
        setup:
        MaterialPair mp = MaterialPairImpl.newInstance().setLeft(expectedMaterial_).setRight(actualMaterial_)
        MaterialPairs mps = MaterialPairsImpl.MaterialPairs(expectedTsr_, actualTsr_)
        mps.put(actualMaterial_.getPathRelativeToTSuiteTimestamp(), mp)
        expect:
        mps.size() == 1
    }
    
    def test_setExpectedMaterial() {
        setup:
        MaterialPairs mps = MaterialPairsImpl.MaterialPairs(expectedTsr_, actualTsr_)
        when:
        mps.putExpectedMaterial(expectedMaterial_)
        then:
        mps.size() == 1
        mps.containsKey(expectedMaterial_.getPathRelativeToTSuiteTimestamp())
        when:
        MaterialPair pair = mps.get(expectedMaterial_.getPathRelativeToTSuiteTimestamp())
        then:
        pair.hasExpected()
    }
    
    def test_putActualMaterial() {
        setup:
        MaterialPairs mps = MaterialPairsImpl.MaterialPairs(expectedTsr_, actualTsr_)
        when:
        mps.putActualMaterial(actualMaterial_)
        then:
        mps.size() == 1
        mps.containsKey(actualMaterial_.getPathRelativeToTSuiteTimestamp())
        when:
        MaterialPair pair = mps.get(actualMaterial_.getPathRelativeToTSuiteTimestamp())
        then:
        pair.hasActual()
    }
    
    def test_toJsonText() {
        setup:
        MaterialPair mp = MaterialPairImpl.newInstance().setLeft(expectedMaterial_).setRight(actualMaterial_)
        MaterialPairs mps = MaterialPairsImpl.MaterialPairs(expectedTsr_, actualTsr_)
        mps.put(actualMaterial_.getPathRelativeToTSuiteTimestamp(), mp)
        when:
        String json = mps.toJsonText()
        println "#test_toJsonText json=${json}"
        String pretty = JsonOutput.prettyPrint(json)
        println "#test_toJsonText pretty=${pretty}"
        then:
        pretty != null
    }
    
    @Ignore
    def testIgnoring() {}
    
    // helper methods
    def void anything() {}

}
