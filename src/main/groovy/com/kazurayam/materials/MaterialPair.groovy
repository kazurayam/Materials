package com.kazurayam.materials

interface MaterialPair extends Comparable<MaterialPair> {

    MaterialPair setLeft(Material left)

    MaterialPair setExpected(Material expected)

    MaterialPair setRight(Material right)

    MaterialPair setActual(Material actual)

    Material getLeft()

    Material getExpected()

    Material getRight()

    Material getActual()

}
