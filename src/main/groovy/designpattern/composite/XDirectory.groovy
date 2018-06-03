package designpattern.composite

class XDirectory implements DirectoryEntry {
    private List<DirectoryEntry> list = null
    private String name = null

    XDirectory(String name) {
        this.name = name
        list = new ArrayList<DirectoryEntry>()
    }
    void add(DirectoryEntry entry) {
        list.add(entry)
    }

    @Override
    void remove() {
        Iterator<DirectoryEntry> iter = list.iterator()
        while (iter.hasNext()) {
            DirectoryEntry entry = iter.next()
            entry.remove()
        }
        System.out.println("removed ${name}")
    }
}
