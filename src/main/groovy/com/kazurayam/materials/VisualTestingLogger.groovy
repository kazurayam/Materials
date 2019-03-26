package com.kazurayam.materials

/**
 *
 * @author kazurayam
 *
 */
interface VisualTestingLogger {

    void info(String message)

    void failed(String message)

    void fatal(String message)

}