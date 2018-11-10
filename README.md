Materials
====


## Abstract

The workd 'Material' here means a file created by your WebDriver/Appium-based tests such as Screenshot image file, downloaded PDF file, or Excel file created on the fly. This project provides a 'MaterialRepository' where you can store materials with well-defined path. The 'MaterialRepositon#resolvePath(String fileName)' method call returns java.nio.file.Path object to you can write bytes: the method resolve the Path for you, you need not worry about where to locate the file. The 'MaterialRepository' provides methods to get access to the files stored. The project can generate HTML view (index.html) of the repository.

## API document

Groovydoc is [here](https://kazurayam.github.io/Materials/)

## How to use

see another repository https://github.com/kazurayam/UsingMaterialsInKatalonStudio

## Copyright and Licensing

Please see [LICENSE](./LICENSE)
