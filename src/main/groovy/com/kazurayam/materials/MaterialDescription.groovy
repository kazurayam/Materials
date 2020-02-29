package com.kazurayam.materials

class MaterialDescription {

    public static final EMPTY = new MaterialDescription("", "")

    private final String category_
    private final String description_

    MaterialDescription(String category, String description) {
        Objects.requireNonNull(category, "category must not be null")
        Objects.requireNonNull(description, "description must not be null")
        this.category_ = category
        this.description_ = description
    }

    String getCategory() {
        return this.category_
    }

    String getDescription() {
        return this.description_
    }

    @Override
    String toString() {
        return this.toJsonText()
    }

    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append('"category":"')
        sb.append(this.getCategory())
        sb.append('",')
        sb.append('"description":"')
        sb.append(this.getDescription())
        sb.append('"')
        sb.append("}")
        return sb.toString()
    }

    static MaterialDescription newInstance(Map map) {
        Objects.requireNonNull(map, "map must not be null")
        if (map['category'] == null) {
            throw new IllegalArgumentException("category not found")
        }
        if (map['description'] == null) {
            throw new IllegalArgumentException("description not found")
        }
        return new MaterialDescription(map['category'], map['description'])
    }

    @Override
    boolean equals(Object obj) {
        if (obj == null) return false
        if (!(obj instanceof MaterialDescription)) return false
        MaterialDescription other = (MaterialDescription)obj
        return this.getCategory() == other.getCategory() &&
                this.getDescription() == other.getDescription()
    }

    @Override
    int hashCode() {
        int result = 17
        result = result * 31 + this.getCategory().hashCode()
        result = result * 31 + this.getDescription().hashCode()
        return result
    }
}
