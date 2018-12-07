package com.kazurayam.materials.model

import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialPair

class MaterialPairImpl implements MaterialPair {
    
    private Material left_
    private Material right_

    private MaterialPairImpl() {}

    public static MaterialPair() {
        return new MaterialPairImpl()
    }
    
    MaterialPair setLeft(Material left) {
        left_ = left
        return this
    }

    MaterialPair setExpected(Material expected) {
        return this.setLeft(expected)
    }

    MaterialPair setRight(Material right) {
        right_ = right
        return this
    }

    MaterialPair setActual(Material actual) {
        return this.setRight(actual)
    }

    Material getLeft() {
        return left_
    }

    Material getExpected() {
        return this.getLeft()
    }

    Material getRight() {
        return right_
    }

    Material getActual() {
        return this.getRight()
    }

    // ---------------- overriding Object properties --------------------------
    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('"left": ')
        sb.append(left_.toJson())
        sb.append(',')
        sb.append('"right": ')
        sb.append(right_.toJson())
        sb.append('')
        sb.append('}')
        return sb.toString()
    }

    @Override
    public boolean equals(Object obj) {
        //if (this == obj)
        //    return true
        if (!(obj instanceof MaterialPair))
            return false
        MaterialPair other = (MaterialPair)obj
        return this.getLeft().equals(other.getLeft()) &&
                this.getRight().equals(other.getRight())
    }

    @Override
    public int hashCode() {
        return this.getLeft().hashCode() + this.getRight().hashCode()
    }

    @Override
    int compareTo(MaterialPair other) {
        int leftEval = this.getLeft().compareTo(other.getLeft())
        int rightEval = this.getRight().compareTo(other.getRight())
        if (leftEval < 0 || leftEval > 0) {
            return leftEval
        } else {
            return rightEval
        }
    }

    
    
    
}
