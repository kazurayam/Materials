package designpattern.composite

class XFile implements DirectoryEntry {

    private String name = null

    XFile(String name) {
        this.name = name
    }

    @Override
    void remove() {
        System.out.println("removed ${name}")
    }
}
