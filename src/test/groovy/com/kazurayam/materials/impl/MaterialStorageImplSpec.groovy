package com.kazurayam.materials.impl

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes

import com.kazurayam.materials.Helpers
import spock.lang.Specification

class MaterialStorageImplSpec extends Specification {

	private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static String classShortName_ = Helpers.getClassShortName(MaterialStorageImplSpec.class)

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
    /**
     * Reproduce the problem reported by
     * https://forum.katalon.com/t/cannot-invoke-method-gettcaseresultlist-on-null-object/37890/
     * 
     * see also com.kazurayam.materials.repository.RepositoryFileVisitor.visitFile(Path file, BasicFileAttributes attributes)
     */
    def test_StorageWithIrregularContent_at_LayerROOT() {
        setup:
        Path caseDir = workdir_.resolve('test_StorageWithIrregularContent_at_LayerROOT')
        Path storageDir = caseDir.resolve('Storage')
        // insert a file at Layer.ROOT
        Path hello = storageDir.resolve('hello.txt')
        Files.createDirectories(hello.getParent())
        hello.toFile().text = "Hello!"
        when:
        MaterialStorageImpl msi = MaterialStorageImpl.newInstance(storageDir)
        then:
        msi.getBaseDir() == storageDir
    }
    
    def test_StorageWithIrregularContent_at_LayerTESTSUITE() {
        setup:
        Path caseDir = workdir_.resolve('test_StorageWithIrregularContent_at_LayerTESTSUITE')
        Path storageDir = caseDir.resolve('Storage')
        // insert a file at Layer.ROOT
        Path hello = storageDir.resolve('foo/hello.txt')
        Files.createDirectories(hello.getParent())
        hello.toFile().text = "Hello!"
        when:
        MaterialStorageImpl msi = MaterialStorageImpl.newInstance(storageDir)
        then:
        msi.getBaseDir() == storageDir
    }
    
    def test_StorageWithIrregularContent_at_LayerTIMESTAMP() {
        setup:
        Path caseDir = workdir_.resolve('test_StorageWithIrregularContent_at_LayerTIMESTAMP')
        Path storageDir = caseDir.resolve('Storage')
        // insert a file at Layer.ROOT
        Path hello = storageDir.resolve('foo/bar/hello.txt')
        Files.createDirectories(hello.getParent())
        hello.toFile().text = "Hello!"
        when:
        MaterialStorageImpl msi = MaterialStorageImpl.newInstance(storageDir)
        then:
        msi.getBaseDir() == storageDir
    }
    
    def test_StorageWithIrregularContent_at_LayerTESTCASE() {
        setup:
        Path caseDir = workdir_.resolve('test_StorageWithIrregularContent_at_LayerTESTCASE')
        Path storageDir = caseDir.resolve('Storage')
        // insert a file at Layer.ROOT
        Path hello = storageDir.resolve('foo/bar/baz/hello.txt')
        Files.createDirectories(hello.getParent())
        hello.toFile().text = "Hello!"
        when:
        MaterialStorageImpl msi = MaterialStorageImpl.newInstance(storageDir)
        then:
        msi.getBaseDir() == storageDir
    }

}
