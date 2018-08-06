package com.kazurayam.material

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Specification

class DownloadsDirectoryHelperSpec extends Specification {
    static Logger logger_ = LoggerFactory.getLogger(DownloadsDirectoryHelperSpec.class)
    // fields
    private static Path workdir_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(DownloadsDirectoryHelperSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
    }

    def testListSuffixedFiles() {
        setup:
        Path sourceDir = Paths.get('./src/test/fixture/Materials/main.TS4/20180712_142755/main.TC1')
        Path targetDir = workdir_.resolve('testListSuffixedFiles')
        when:
        Helpers.copyDirectory(sourceDir, targetDir)
        then:
        Files.exists(targetDir.resolve('http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
        Files.exists(targetDir.resolve('smilechart.xls'))
        //
        when:
        List<Path> pngFiles = DownloadsDirectoryHelper.listSuffixedFiles(targetDir, 'http%3A%2F%2Fdemoaut.katalon.com%2F.png')
        then:
        pngFiles.size() == 3
        //
        when:
        List<Path> xlsFiles = DownloadsDirectoryHelper.listSuffixedFiles(targetDir, 'smilechart.xls')
        then:
        xlsFiles.size() == 6
    }

    def testListSuffixedFiles_DownloadsDir() {
        setup:
        Path sourceDir = Paths.get('./src/test/fixture/Materials/main.TS4/20180712_142755/main.TC1')
        Path downloads = Paths.get(System.getProperty('user.home'), 'Downloads')
        when:
        Helpers.copyDirectory(sourceDir, downloads)
        then:
        Files.exists(downloads.resolve('http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
        Files.exists(downloads.resolve('smilechart.xls'))
        //
        when:
        List<Path> pngFiles = DownloadsDirectoryHelper.listSuffixedFiles('http%3A%2F%2Fdemoaut.katalon.com%2F.png')
        then:
        pngFiles.size() >= 3
        //
        when:
        List<Path> xlsFiles = DownloadsDirectoryHelper.listSuffixedFiles('smilechart.xls')
        then:
        xlsFiles.size() >= 6
    }

    def testDeleteSuffixedFiles() {
        setup:
        Path sourceDir = Paths.get('./src/test/fixture/Materials/main.TS4/20180712_142755/main.TC1')
        Path targetDir = workdir_.resolve('testDeleteSuffixedFiles')
        String pngFileName = 'http%3A%2F%2Fdemoaut.katalon.com%2F.png'
        String xlsFileName = 'smilechart.xls'
        when:
        Helpers.copyDirectory(sourceDir, targetDir)
        then:
        Files.exists(targetDir.resolve(pngFileName))
        Files.exists(targetDir.resolve(xlsFileName))
        //
        when:
        List<Path> pngFiles = DownloadsDirectoryHelper.listSuffixedFiles(targetDir, pngFileName)
        then:
        pngFiles.size() == 3
        when:
        DownloadsDirectoryHelper.deleteSuffixedFiles(targetDir, pngFileName)
        pngFiles = DownloadsDirectoryHelper.listSuffixedFiles(targetDir, pngFileName)
        then:
        pngFiles.size() == 0
        //
        when:
        List<Path> xlsFiles = DownloadsDirectoryHelper.listSuffixedFiles(targetDir, xlsFileName)
        then:
        xlsFiles.size() == 6
        when:
        DownloadsDirectoryHelper.deleteSuffixedFiles(targetDir, xlsFileName)
        pngFiles = DownloadsDirectoryHelper.listSuffixedFiles(targetDir, xlsFileName)
        then:
        pngFiles.size() == 0
    }

    def testDeleteSuffixedFiles_DownloadsDir() {
        setup:
        Path sourceDir = Paths.get('./src/test/fixture/Materials/main.TS4/20180712_142755/main.TC1')
        Path downloads = Paths.get(System.getProperty('user.home'), 'Downloads')
        String pngFileName = 'http%3A%2F%2Fdemoaut.katalon.com%2F.png'
        String xlsFileName = 'smilechart.xls'
        when:
        Helpers.copyDirectory(sourceDir, downloads)
        then:
        Files.exists(downloads.resolve(pngFileName))
        Files.exists(downloads.resolve(xlsFileName))
        //
        when:
        List<Path> pngFiles = DownloadsDirectoryHelper.listSuffixedFiles(pngFileName)
        then:
        pngFiles.size() >= 3
        when:
        DownloadsDirectoryHelper.deleteSuffixedFiles(pngFileName)
        pngFiles = DownloadsDirectoryHelper.listSuffixedFiles(pngFileName)
        then:
        pngFiles.size() == 0
        //
        when:
        List<Path> xlsFiles = DownloadsDirectoryHelper.listSuffixedFiles(xlsFileName)
        then:
        xlsFiles.size() >= 6
        when:
        DownloadsDirectoryHelper.deleteSuffixedFiles(xlsFileName)
        pngFiles = DownloadsDirectoryHelper.listSuffixedFiles(xlsFileName)
        then:
        pngFiles.size() == 0
    }
}
