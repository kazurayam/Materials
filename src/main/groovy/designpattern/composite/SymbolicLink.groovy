package designpattern.composite

class SymbolicLink implements DirectoryEntry {
    private String name = null
    SymbolicLink(String name) {
        this.name = name
    }
    public void remove() {
        System.out.println("removed $name")
    }
}
