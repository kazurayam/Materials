package com.kazurayam.materials.impl

import java.awt.image.BufferedImage

import javax.imageio.ImageIO

import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialPair

final class MaterialPairImpl implements MaterialPair {
    
    private Material left_
    private Material right_

    private MaterialPairImpl() {
        left_ = null
        right_ = null
    }

    public static MaterialPair() {
        return new MaterialPairImpl()
    }
    
    MaterialPair clone() {
        MaterialPair mp = new MaterialPairImpl()
        if (this.hasLeft()) {
            mp.setLeft(this.getLeft())
        }
        if (this.hasRight()) {
            mp.setRight(this.getRight())
        }
        return mp
    }
    
    MaterialPair setLeft(Material left) {
        Objects.requireNonNull(left)
        left_ = left
        return this
    }

    MaterialPair setExpected(Material expected) {
        return this.setLeft(expected)
    }

    MaterialPair setRight(Material right) {
        Objects.requireNonNull(right)
        right_ = right
        return this
    }

    MaterialPair setActual(Material actual) {
        return this.setRight(actual)
    }
    
    @Override
    boolean hasLeft() {
        return this.getLeft() != null
    }
    
    @Override
    boolean hasExpected() {
        return this.hasLeft()
    }
    
    @Override
    boolean hasRight() {
        return this.getRight() != null
    }
    
    @Override
    boolean hasActual() {
        return this.hasRight()
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
    
    @Override
    BufferedImage getExpectedBufferedImage() {
        BufferedImage bufferedImage
        new FileInputStream(this.getExpected().getPath().toFile()).withCloseable { res ->
            bufferedImage = ImageIO.read(res)
        }
        return bufferedImage
    }

    @Override
    BufferedImage getActualBufferedImage() {
        //return ImageIO.read(this.getActual().getPath().toFile())
        BufferedImage bufferedImage
        new FileInputStream(this.getActual().getPath().toFile()).withCloseable { res ->
            bufferedImage = ImageIO.read(res)
        }
        return bufferedImage
    }
    // ---------------- overriding Object properties --------------------------
    @Override
    String toString() {
        return this.toJsonText()
    }

    @Override
    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('"left": ')
        if (left_ != null) {
            sb.append(left_.toJsonText())
        } else {
            sb.append('null')
        }
        sb.append(',')
        sb.append('"right": ')
        if (right_ != null) {
            sb.append(right_.toJsonText())
        } else {
            sb.append('null')
        }
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
