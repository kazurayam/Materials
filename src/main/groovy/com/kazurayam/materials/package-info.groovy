/**
 * What does a 'material' mean? --- A material is a file created by WebDrvier/Appium-based
 * test scripts on the fly. A screen shot of web page is a typical 'material'. 'Materials' include
 * other types of files: a PDF file downloaded from web site, an Excel file created by test script,
 * XML/JSON document responded by RESTful API call.
 *
 * One-off file path for a screen shot is easy to make; e.g., <code>C:¥Users¥myname¥tmp¥sample.png</code>
 * would be enough. However, if we want to reused the file after interacting with web, we soon realized
 * that the one-off file path is not enough. We need to design a file path format. Both of program that
 * writes a file and program that reads the file need to share the format and respect it. In order to
 * simplify the writers and readers, we need a class library 'Material' which provides the
 * 'MaterialRepository' object which implement easy access methods to the files contained there.
 *
 */
package com.kazurayam.materials