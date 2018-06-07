package filevisitor

import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import com.kazurayam.kstestresults.Helpers

import spock.lang.Specification

class DeletingFileVisitorSpec extends Specification {

    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")

    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(DeletingFileVisitorSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
    }

    def testSoke() {
        when:
        Path start = workdir.resolve('TS1')
        FileVisitor<Path> visitor = new DeletingFileVisitor()
        Files.walkFileTree(start, visitor)
        then:
        true == true
    }

}
