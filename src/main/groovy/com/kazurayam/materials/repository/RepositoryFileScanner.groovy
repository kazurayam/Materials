package com.kazurayam.materials.repository

import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput

/**
 * RepositoryScanner scans a file system tree under the baseDir directory.
 * It assumes the file tree is organized in the format as this:
 *
 * <pre>
 * baseDir
 * |
 * +-§A
 * |  +-20180616_170941
 * |  　　　　  +-§C
 * |  　         　　　　　　　 http%3A%2F%2Fdemoaut.katalon.com%2F.png
 * |
 * +-TS1
 * |  +-20180530_130419
 * |  |  +-TC1
 * |  |          http%3A%2F%2Fdemoaut.katalon.com%2F.png
 * |  |          http%3A%2F%2Fdemoaut.katalon.com%2F(1).png
 * |  |
 * |  +-20180530_130604
 * |      +-TC1
 * |      |      http%3A%2F%2Fdemoaut.katalon.com%2F.bmp
 * |      |      http%3A%2F%2Fdemoaut.katalon.com%2F.gif
 * |      |      http%3A%2F%2Fdemoaut.katalon.com%2F.jpeg
 * |      |      http%3A%2F%2Fdemoaut.katalon.com%2F.jpg
 * |      |      http%3A%2F%2Fdemoaut.katalon.com%2F.png
 * |      |
 * |      +-TC2
 * |             http%3A%2F%2Fdemoaut.katalon.com%2F(1).png
 * |
 * +-TS2
 * |  +-20180612_111256
 * |      |
 * |      +-TC1
 * |             http%3A%2F%2Fdemoaut.katalon.com%2F.png
 * +-TS3
 * |  +-20180627_140853
 * |      |
 * |      +-TC1
 * |             http%3A%2F%2Fdemoaut.katalon.com%2F.png
 * |             http%3A%2F%2Ffiles.shareholder.com%2Fdownloads%2FAAPL%2F6323171818x0xS320193-17-70%2F320193%2Ffiling.pdf.pdf
 * |             http%3A%2F%2Fweather.livedoor.com%2Fforecast%2Fwebservice%2Fjson%2Fv1%3Fcity%3D130010.json
 * |             http%3A%2F%2Fwww.kazurayam.com%2Fcarmina%2Fexample%2FBook1.xlsm.xlsm
 * |             http%3A%2F%2Fwww.kazurayam.com%2Fcarmina%2Fexample%2FBook1.xlsx.xlsx
 * |             http%3A%2F%2Fwww.kazurayam.com%2Fcarmina%2Fexample%2FRunMelos.txt
 * |             https%3A%2F%2Ffixturedownload.com%2Fdownload%2Fcsv%2Ffifa-world-cup-2018%2Fjapan.csv
 * |             https%3A%2F%2Ffixturedownload.com%2Fdownload%2Fxlsx%2Ffifa-world-cup-2018%2Fjapan.xlsx
 * |             https%3A%2F%2Fnews.yahoo.co.jp%2Fpickup%2Fscience%2Frss.xml.xml
 * +-TS4
 * |  +-20180712_142755
 * |      |
 * |      +-TC1
 * |             'http%3A%2F%2Fdemoaut.katalon.com%2F (1).png'
 * |             'http%3A%2F%2Fdemoaut.katalon.com%2F(1).png'
 * |             http%3A%2F%2Fdemoaut.katalon.com%2F.png
 * |             'smilechart (1).xls'
 * |             'smilechart (2).xls'
 * |             'smilechart(1).xls'
 * |             'smilechart(2).xls'
 * |             'smilechart(3).xls'
 * |             smilechart.xls
 * |
 * +-_
 *    +-_
 *        +-TC1
 *               http%3A%2F%2Fdemoaut.katalon.com%2F.png
 * </pre>
 *
 * It makes a List of TSuiteResult which containes TCaseResult and  Material
 * as found in the baseDir.
 *
 * @author kazurayam
 */
final class RepositoryFileScanner {

    static Logger logger_ = LoggerFactory.getLogger(RepositoryFileScanner.class)

    private RepositoryRoot repoRoot_

    RepositoryFileScanner(Path baseDir, Path reportsDir) {
        Objects.requireNonNull(baseDir, "baseDir must not be null")
        Objects.requireNonNull(reportsDir, "reportsDir must not be null")
        if (!Files.exists(baseDir)) {
            throw new IllegalArgumentException("${baseDir} does not exist")
        }
        if (!Files.isDirectory(baseDir)) {
            throw new IllegalArgumentException("${baseDir} is not a directory")
        }
        if (!Files.exists(reportsDir)) {
            throw new IllegalArgumentException("${reportsDir} does not exist")
        }
        if (!Files.isDirectory(reportsDir)) {
            throw new IllegalArgumentException("${reportsDir} is not a directory")
        }
        repoRoot_ = new RepositoryRoot(baseDir)
        repoRoot_.setReportsDir(reportsDir)
    }

    /**
     * scan the baseDir to return an instance of RepositoryRoot
     */
    void scan() {
        Files.walkFileTree(
                repoRoot_.getBaseDir(),
                EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                new RepositoryFileVisitor(repoRoot_)
        )
        //
        if (repoRoot_.getLatestModifiedTSuiteResult() != null) {
            repoRoot_.getLatestModifiedTSuiteResult().setLatestModified(true)
        }
    }

    RepositoryRoot getRepositoryRoot() {
        return repoRoot_
    }


    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"RepositoryScanner":{')
        sb.append('"repoRoot":' + repoRoot_.toJsonText() + '"')
        sb.append('}}')
        return sb.toString()
    }

    /**
     * entry point for performance profiling
     *
     * @param args
     */
    public static void main(String[] args) {
        logger_.info("#main " + ("Hello, I am Carmina RepositoryScanner."))
        Path baseDir = Paths.get(System.getProperty('user.dir') + '/src/test/fixture/Materials')
        RepositoryFileScanner scanner = new RepositoryFileScanner(baseDir)
        scanner.scan()
        logger_.info("#main " + JsonOutput.prettyPrint(scanner.toJsonText()))
    }

}
