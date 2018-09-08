Materials
====

Document needs to be written yet.

## Abstract

'Materials' means files created by your WebDriver/Appium-based tests such as Screenshot image files, downloaded PDF files, or Excel files created on the fly, etc. This project provides a 'MaterialRepository' where you can store materials with well-defined path. The 'MaterialsRepositon#resolvePath(String fileName)' method call returns java.nio.file.Path object to you can write bytes: the method resolve the Path for you, you need not worry about where to locate the file. The 'MaterialRepository' provides methods to get access to the files stored. The project can generate HTML view (index.html) of the repository.


## Copyright and Licensing

Copyright 2018 kazurayam

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied.
See the License for the specific language governing permissions and limitations
under the License.
