package com.kazurayam.materials.view


import java.nio.file.Path

import com.kazurayam.materials.TExecutionProfile

import groovy.json.JsonSlurper

/**
 * This class wraps a JSON file
 * <project>/Reports/<test suite subdirs>/<test suite timestamp>/execution.properties
 *
 * Why we need this file? --- I want to find out which Execution Profile was applied
 * to each Test Suite execution; default, demo, product. I want to display the value in
 * the ./Materials/index.html
 */
final class ExecutionPropertiesWrapper {

    private def jsonObject

    /**
     * 
     * @param path Path to a 'execution.properties' file of a TSuiteResult
     */
    ExecutionPropertiesWrapper(Path path) {
        this(path.toFile())
    }

    ExecutionPropertiesWrapper(File file) {
        Objects.requireNonNull(file)
        jsonObject = new JsonSlurper().parse(file)
    }

    ExecutionPropertiesWrapper(String text) {
        Objects.requireNonNull(text)
        jsonObject = new JsonSlurper().parseText(text)
    }

    TExecutionProfile getTExecutionProfile() {
        return new TExecutionProfile(jsonObject.execution.general.executionProfile)
    }
    
    String getDriverName() {
        return jsonObject.Name
    }
}
