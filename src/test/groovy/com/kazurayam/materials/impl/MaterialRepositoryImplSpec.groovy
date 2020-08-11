package com.kazurayam.materials.impl

import com.kazurayam.materials.TExecutionProfile

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.model.MaterialFileName
import com.kazurayam.materials.model.Suffix

import spock.lang.Specification

//@Ignore
class MaterialRepositoryImplSpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(MaterialRepositoryImplSpec.class)

    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static String classShortName_ = Helpers.getClassShortName(MaterialRepositoryImplSpec.class)

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/testOutput/${classShortName_}")
        if (Files.exists(workdir_)) {
            Helpers.deleteDirectoryContents(workdir_)
        }
        Files.createDirectories(workdir_)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testGetBaseDir() {
        setup:
        Path casedir = workdir_.resolve('testGetBaseDir')
        Helpers.copyDirectory(fixture_, casedir)
        Path materialsDir = casedir.resolve('Materials')
        when:
        MaterialRepositoryImpl mri = MaterialRepositoryImpl.newInstance(materialsDir)
        then:
        mri.getBaseDir() == materialsDir
    }

    def testResolveScreenshotPath() {
        setup:
            Path casedir = workdir_.resolve('testResolveScreenshotPath')
            Helpers.copyDirectory(fixture_, casedir)
            Path materialsDir = casedir.resolve('Materials')
            MaterialRepositoryImpl mri = MaterialRepositoryImpl.newInstance(materialsDir)
            mri.markAsCurrent('Test Suites/main/TS1',
                    'CURA_ProductionEnv', '20180530_130604')
            TSuiteResult tsr = mri.ensureTSuiteResultPresent('Test Suites/main/TS1',
                    'CURA_ProductionEnv', '20180530_130604')
        when:
            Path p1 = mri.resolveScreenshotPath('TC1',
                new URL('https://my.home.net/gn/issueList.html?corp=abcd'))
        then:
            p1.getFileName().toString() == 'https%3A%2F%2Fmy.home.net%2Fgn%2FissueList.html%3Fcorp%3Dabcd.png'
        when:
            Path p2 = mri.resolveScreenshotPath('TC1', '.',
                new URL('https://foo:bar@dev.home.net/gnc/issueList.html?corp=abcd'))
        then:
            p2.getFileName().toString() == 'https%3A%2F%2Ffoo%3Abar%40dev.home.net%2Fgnc%2FissueList.html%3Fcorp%3Dabcd.png'
        when:
            Path metadataBundle = mri.locateMaterialMetadataBundle(tsr)
        then:
            Files.exists(metadataBundle)
    }
    
    
    def testURL() {
        setup:
        URL url = new URL('https://my.home.net/gn/issueList.html?corp=abcd&foo=bar#top')
        expect:
        url.getPath() == '/gn/issueList.html'
        when:
        Path p = Paths.get(url.getPath())
        then:
        p.getNameCount() == 2
        p.getName(0).toString() == 'gn'
        p.getName(1).toString() == 'issueList.html'
        expect:
        url.getQuery() == 'corp=abcd&foo=bar'
        url.getRef() == 'top'
        when:
        Map<String, String> queries = MaterialRepositoryImpl.parseQuery(url.getQuery())
        then:
        queries.size() == 2
        queries.containsKey('corp')
        queries.containsKey('foo')
        queries.get('corp') == 'abcd'
        queries.get('foo') == 'bar'
        when:
        url = new URL('https://www.google.com')
        then:
        url.getPath() == ''
    }
    
    def testParseQuery() {
        setup:
        String query = 'corp=abcd&foo=bar'
        when:
        Map<String, String> queries = MaterialRepositoryImpl.parseQuery(query)
        then:
        queries.size() == 2
        queries.containsKey('corp')
        queries.containsKey('foo')
        queries.get('corp') == 'abcd'
        queries.get('foo') == 'bar'    
    }
    
    
    def testResolveScreenshotPathByURLPathComponents() {
        setup:
            Path casedir = workdir_.resolve('testResolveScreenshotPathByURLPathComponents')
            Helpers.copyDirectory(fixture_, casedir)
            Path materialsDir = casedir.resolve('Materials')
            MaterialRepositoryImpl mri = MaterialRepositoryImpl.newInstance(materialsDir)
            mri.markAsCurrent('Test Suites/main/TS1',
                    'CURA_ProductionEnv', '20180530_130604')
            TSuiteResult tsr = mri.ensureTSuiteResultPresent('Test Suites/main/TS1',
                    'CURA_ProductionEnv','20180530_130604')
        when:
            Path p = mri.resolveScreenshotPathByURLPathComponents('TC1',
                        new URL('https://my.home.net/gn/issueList.html?corp=abcd'))
        then:
            p.getName(p.getNameCount() - 1).toString() == 'gn%2FissueList.html%3Fcorp%3Dabcd.png'
            p.getFileName().toString() == 'gn%2FissueList.html%3Fcorp%3Dabcd.png'
            //
        when:
            Path p0 = mri.resolveScreenshotPathByURLPathComponents('TC1', '.',
                        new URL('https://my.home.net/gn/issueList.html?corp=abcd'), 0)
        then:
            p0.getName(p0.getNameCount() - 1).toString() == 'gn%2FissueList.html%3Fcorp%3Dabcd.png'
            p0.getFileName().toString() == 'gn%2FissueList.html%3Fcorp%3Dabcd.png'
        //
        when:
            Path p1 = mri.resolveScreenshotPathByURLPathComponents('TC1',
                        new URL('https://my.home.net/gn/issueList.html?corp=abcd'), 1)
        then:
            p1.getFileName().toString() == 'issueList.html%3Fcorp%3Dabcd.png'
        when:
            Path p2 = mri.resolveScreenshotPathByURLPathComponents('TC1', '.',
                        new URL('https://my.home.net/gn/issueList.html?corp=abcd'), 2)
        then:
            p2.getFileName().toString() == 'default%3Fcorp%3Dabcd.png'
        //
        when:
            Path google = mri.resolveScreenshotPathByURLPathComponents('TC1',
                new URL('https://www.google.com'))
        then:
            google.getFileName().toString() == 'https%3A%2F%2Fwww.google.com.png'
        when:
            Path metadataBundle = mri.locateMaterialMetadataBundle(tsr)
        then:
            Files.exists(metadataBundle)
    }




    /**
MaterialRepositoryImpl DEBUG #resolveMaterialPath count=1
MaterialImpl DEBUG #getPath parentTCR_.getTCaseDirectory()=build\tmp\testOutput\MaterialRepositoryImplSpec\testResolveMaterialPath\Materials\TS1\20180530_130604\TC1
MaterialImpl DEBUG #getPath subpath_=..\..\..\..\..\..\..\..\..\
MaterialImpl DEBUG #getPath parentTCR_.getTCaseDirectory().resolve(subpath_)=build\tmp\testOutput\MaterialRepositoryImplSpec\testResolveMaterialPath\Materials\TS1\20180530_130604\TC1\..\..\..\..\..\..\..\..\..\
MaterialImpl DEBUG #getPath materialFileName_.getFileName()=http%3A%2F%2Fdemoaut.katalon.com%2F.png
MaterialImpl DEBUG #getPath p=http%3A%2F%2Fdemoaut.katalon.com%2F.png
MaterialRepositoryImpl DEBUG #resolveMaterialPath material={"Material":{"url":"http://demoaut.katalon.com/","suffix":"","fileType":{"FileType":{"extension":"png","mimeTypes":["image/png"]}},"path":"http%3A%2F%2Fdemoaut.katalon.com%2F.png","lastModified":"null"}}
MaterialRepositoryImpl DEBUG #resolveMaterialPath material.getParent()={"TCaseResult":{"tCaseName":{"id": "TC1","abbreviatedId": "TC1","value": "TC1"},"tCaseDir":"build\\tmp\\testOutput\\MaterialRepositoryImplSpec\\testResolveMaterialPath\\Materials\\TS1\\20180530_130604\\TC1","lastModified":"-999999999-01-01T00:00","length":0,"materials":[]}}
MaterialImpl DEBUG #getPath parentTCR_.getTCaseDirectory()=build\tmp\testOutput\MaterialRepositoryImplSpec\testResolveMaterialPath\Materials\TS1\20180530_130604\TC1
MaterialImpl DEBUG #getPath subpath_=..\..\..\..\..\..\..\..\..\
MaterialImpl DEBUG #getPath parentTCR_.getTCaseDirectory().resolve(subpath_)=build\tmp\testOutput\MaterialRepositoryImplSpec\testResolveMaterialPath\Materials\TS1\20180530_130604\TC1\..\..\..\..\..\..\..\..\..\
MaterialImpl DEBUG #getPath materialFileName_.getFileName()=http%3A%2F%2Fdemoaut.katalon.com%2F.png
MaterialImpl DEBUG #getPath p=http%3A%2F%2Fdemoaut.katalon.com%2F.png
MaterialRepositoryImpl DEBUG #resolveMaterialPath material.getPath()=http%3A%2F%2Fdemoaut.katalon.com%2F.png
MaterialImpl DEBUG #getPath parentTCR_.getTCaseDirectory()=build\tmp\testOutput\MaterialRepositoryImplSpec\testResolveMaterialPath\Materials\TS1\20180530_130604\TC1
MaterialImpl DEBUG #getPath subpath_=..\..\..\..\..\..\..\..\..\
MaterialImpl DEBUG #getPath parentTCR_.getTCaseDirectory().resolve(subpath_)=build\tmp\testOutput\MaterialRepositoryImplSpec\testResolveMaterialPath\Materials\TS1\20180530_130604\TC1\..\..\..\..\..\..\..\..\..\
MaterialImpl DEBUG #getPath materialFileName_.getFileName()=http%3A%2F%2Fdemoaut.katalon.com%2F.png
MaterialImpl DEBUG #getPath p=http%3A%2F%2Fdemoaut.katalon.com%2F.png
MaterialRepositoryImpl DEBUG #resolveMaterialPath material.getPath().getParent()=null
MaterialImpl DEBUG #getPath parentTCR_.getTCaseDirectory()=build\tmp\testOutput\MaterialRepositoryImplSpec\testResolveMaterialPath\Materials\TS1\20180530_130604\TC1
MaterialImpl DEBUG #getPath subpath_=..\..\..\..\..\..\..\..\..\
MaterialImpl DEBUG #getPath parentTCR_.getTCaseDirectory().resolve(subpath_)=build\tmp\testOutput\MaterialRepositoryImplSpec\testResolveMaterialPath\Materials\TS1\20180530_130604\TC1\..\..\..\..\..\..\..\..\..\
MaterialImpl DEBUG #getPath materialFileName_.getFileName()=http%3A%2F%2Fdemoaut.katalon.com%2F.png
MaterialImpl DEBUG #getPath p=http%3A%2F%2Fdemoaut.katalon.com%2F.png
	 */
    def testResolveMaterialPath() {
        setup:
            def methodName ='testResolveMaterialPath'
            Path casedir = workdir_.resolve(methodName)
            Helpers.copyDirectory(fixture_, casedir)
            Path materialsDir = casedir.resolve('Materials')
            MaterialRepositoryImpl mri = MaterialRepositoryImpl.newInstance(materialsDir)
            mri.markAsCurrent('Test Suites/main/TS1',
                    'CURA_ProductionEnv', '20180530_130604')
            TSuiteResult tsr = mri.ensureTSuiteResultPresent('Test Suites/main/TS1',
                    'CURA_ProductionEnv', '20180530_130604')
        when:
            String materialFileName = MaterialFileName.format(
                new URL('http://demoaut.katalon.com/'),
                Suffix.NULL,
                FileType.PNG)
            Path p = mri.resolveMaterialPath('TC1', materialFileName)
        then:
            p != null
            p.toString().replace('\\', '/') ==
                "build/tmp/testOutput/${classShortName_}/${methodName}/Materials/main.TS1/CURA_ProductionEnv/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
        when:
            Path metadataBundle = mri.locateMaterialMetadataBundle(tsr)
        then:
            Files.exists(metadataBundle)
    }

    def testResolveMaterialPath_whenCurrentTestSuiteNotMarked() {
        setup:
            def methodName = 'testResolveMaterialPath_whenCurrentTestSuiteNotMarked'
            Path casedir = workdir_.resolve(methodName)
            Helpers.copyDirectory(fixture_, casedir)
            Path materialsDir = casedir.resolve('Materials')
            MaterialRepositoryImpl mri = MaterialRepositoryImpl.newInstance(materialsDir)
            
            // We will intentionally leave the MaterialRepositoryImpl object
            // not marked with current TSuiteResultId.
            // Then we will see a Materials/_/_/_ directory created.
            //mri.markAsCurrent('Test Suites/main/TS1', 'CURA_ProductionEnv', '20180530_130604')
            TSuiteResult tsr = mri.ensureTSuiteResultPresent('Test Suites/main/TS1',
                    'CURA_ProductionEnv', '20180530_130604')
        when:
            String materialFileName = MaterialFileName.format(
                new URL('http://demoaut.katalon.com/'),
                Suffix.NULL,
                FileType.PNG)
            Path p = mri.resolveMaterialPath('TC1', materialFileName)
        then:
            p != null
            p.toString().replace('\\', '/') ==
                "build/tmp/testOutput/${classShortName_}/${methodName}/Materials/_/_/_/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
        when:
            Path metadataBundle = mri.locateMaterialMetadataBundle(tsr)
        then:
            // the file is created when mri.resolveMaterialPath() was invoked
            metadataBundle != null
    }
    
    def testResolveMaterialPath_withSuffix() {
        setup:
        def methodName = 'testResolveMaterialPath_withSuffix'
        Path casedir = workdir_.resolve(methodName)
        Helpers.copyDirectory(fixture_, casedir)
        Path materialsDir = casedir.resolve('Materials')
        MaterialRepositoryImpl mri = MaterialRepositoryImpl.newInstance(materialsDir)
        mri.markAsCurrent('Test Suites/main/TS1',
                'CURA_ProductionEnv', '20180530_130604')
        def r = mri.ensureTSuiteResultPresent('Test Suites/main/TS1',
                'CURA_ProductionEnv', '20180530_130604')
        when:
        String materialFileName = MaterialFileName.format(
            new URL('http://demoaut.katalon.com/'),
            new Suffix(1),
            FileType.PNG)
        Path p = mri.resolveMaterialPath('TC1', materialFileName)
        then:
        p != null
        p.toString().replace('\\', '/') ==
            "build/tmp/testOutput/${classShortName_}/${methodName}/Materials/main.TS1/CURA_ProductionEnv/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png"
    }

    def testResolveMaterialPath_new() {
        setup:
        def methodName = 'testResolveMaterialPath_new'
        Path casedir = workdir_.resolve(methodName)
        Helpers.copyDirectory(fixture_, casedir)
        Path materialsDir = casedir.resolve('Materials')
        MaterialRepositoryImpl mri = MaterialRepositoryImpl.newInstance(materialsDir)
        mri.markAsCurrent('Test Suites/main/TS3',
                'default', '20180614_152000')
        def r = mri.ensureTSuiteResultPresent('TS3',
                'default', '20180614_152000')
        when:
        String materialFileName = MaterialFileName.format(new URL('http://demoaut.katalon.com/'),
            Suffix.NULL,
            FileType.PNG)
        Path p = mri.resolveMaterialPath('TC1', materialFileName)
        then:
        p != null
        p.toString().replace('\\', '/') ==
            "build/tmp/testOutput/${classShortName_}/${methodName}/Materials/main.TS3/default/20180614_152000/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
        Files.exists(p.getParent())
    }

    def testResolveMaterialPath_withSuffix_new() {
        setup:
        def methodName = 'testResolveMaterialPath_withSuffix_new'
        Path casedir = workdir_.resolve(methodName)
        Helpers.copyDirectory(fixture_, casedir)
        Path materialsDir = casedir.resolve('Materials')
        MaterialRepositoryImpl mri = MaterialRepositoryImpl.newInstance(materialsDir)
        mri.markAsCurrent('Test Suites/main/TS3', 'default', '20180614_152000')
        def r = mri.ensureTSuiteResultPresent('Test Suites/main/TS3',
                'default', '20180614_152000')
        when:
        String materialFileName = MaterialFileName.format(new URL('http://demoaut.katalon.com/'),
            new Suffix(1),
            FileType.PNG)
        Path p = mri.resolveMaterialPath('TC1', materialFileName)
        then:
        p != null
        p.toString().replace('\\', '/') ==
            "build/tmp/testOutput/${classShortName_}/${methodName}/Materials/main.TS3/default/20180614_152000/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png"
        Files.exists(p.getParent())
    }

    def testResolveMaterial_png_SuitelessTimeless() {
        setup:
        def methodName = 'testResolveMaterial_png_SuitelessTimeless'
        Path casedir = workdir_.resolve(methodName)
        Helpers.copyDirectory(fixture_, casedir)
        Path materialsDir = casedir.resolve('Materials')
        MaterialRepositoryImpl mri = MaterialRepositoryImpl.newInstance(materialsDir)
        mri.markAsCurrent(TSuiteName.SUITELESS,
                TExecutionProfile.getUNUSED(), TSuiteTimestamp.TIMELESS)
        def r = mri.ensureTSuiteResultPresent(TSuiteName.SUITELESS,
                TExecutionProfile.getUNUSED(), TSuiteTimestamp.TIMELESS)
        when:
        String materialFileName = MaterialFileName.format(new URL('http://demoaut.katalon.com/'), new Suffix(1), FileType.PNG)
        Path p = mri.resolveMaterialPath('TC1', materialFileName)
        then:
        p != null
        p.toString().replace('\\', '/') == "build/tmp/testOutput/${classShortName_}/${methodName}/Materials/_/_/_/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png"
    }

    def testToJsonText() {
        setup:
        Path casedir = workdir_.resolve('testToJsonText')
        Helpers.copyDirectory(fixture_, casedir)
        Path materialsDir = casedir.resolve('Materials')
        MaterialRepositoryImpl mri = MaterialRepositoryImpl.newInstance(materialsDir)
        mri.markAsCurrent('Test Suites/TS1',
                'CURA_ProductionEnv', '20180810_140105')
        def r = mri.ensureTSuiteResultPresent('Test Suites/TS1',
        'CURA_ProductionEnv', '20180910_140105')
        when:
        def str = mri.toJsonText()
        then:
        str != null
        str.contains('{"MaterialRepository":{')
        str.contains(Helpers.escapeAsJsonText(casedir.toString()))
        str.contains('}}')
    }

    def test_resolveFileNameByURLPathComponents_simple() {
        setup:
        Path casedir = workdir_.resolve('test_resolveFileNameByURLPathComponents_simple')
        Helpers.copyDirectory(fixture_, casedir)
        Path materialsDir = casedir.resolve('Materials')
        MaterialRepositoryImpl mri = MaterialRepositoryImpl.newInstance(materialsDir)
        mri.markAsCurrent('Test Suites/TS1',
                'CURA_ProductionEnv', '20180810_140105')
        def tsr = mri.ensureTSuiteResultPresent('Test Suites/TS1',
                'CURA_ProductionEnv', '20180910_140105')
        when:
        URL url = new URL("http://demoaut.katalon.com/")
        int startingDepth = 0
        String defaultName = "top"
        String result = mri.resolveFileNameByURLPathComponents(
                url, startingDepth, defaultName)
        then:
        assert "top.png" == result
    }

}
