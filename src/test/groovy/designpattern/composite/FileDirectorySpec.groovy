package designpattern.composite

import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class FileDirectorySpec extends Specification {

    def testSmoke() {
        when:
        XFile file1 = new XFile("file1")
        XFile file2 = new XFile("file2")
        XFile file3 = new XFile("file3")
        XFile file4 = new XFile("file4")
        XDirectory dir1 = new XDirectory("dir1")
        dir1.add(file1)
        XDirectory dir2 = new XDirectory("dir2")
        dir2.add(file2)
        dir2.add(file3)
        dir1.add(dir2)
        dir1.add(file4)
        dir1.remove()
        then:
        true == true
    }
}
