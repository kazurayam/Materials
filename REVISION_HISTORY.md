Revision History of materials
=============================

This project was abandoned. The last version is 0.73.2. I will no longer maintain this.

The project was succeeded by another project at 

- [materialstore](https://github.com/kazurayam/materialstore)



## Changes made at 0.73.0 -> 0.73.1

Date: 02,Dec 2019
Changes: https://github.com/kazurayam/Materials/compare/0.73.0...0.73.1
Subjects:
1. In 0.73.0 found that the Materials/index.html failed to show image diff files named with Japanese characters (e.g, トップ(0.0).png). This problem has been fixed. 

## Changes made at 0.72.2 -> 0.72.3

Date: 14,Sept 2019
Changes: https://github.com/kazurayam/Materials/compare/0.72.1...0.72.2
Subjects:
1. Fixed a problem of MaterialRepository class: Exception was raised when mr.resolveMaterialPath() was called
without calling mr.markAsCurrent(TSuiteName, TSuiteTimestamp).

## Changes made at 0.28 -> 0.30

Date: 15,Feb 2019
Changes: https://github.com/kazurayam/Materials/compare/0.28.0...0.30.0
Subjects:
1. Add `com.kazurayam.materials.MaterialStorage`, which was necessary for [Chronological Visual Testing in Katalon Studio](https://github.com/kazurayam/ChronologicalVisualTestingInKatalonStudio).
2. Add `com.kazurayam.materials.impl` package.
3. Refactored `TCaseResult` and other interfaces/classes to fit to the new package structure.
