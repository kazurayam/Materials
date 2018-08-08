package com.kazurayam.material

import java.nio.file.Path
import groovy.json.JsonSlurper

/**
 * This class wrapps the JSON file
 * <project>/Reports/<test suite subdirs>/<test suite timestamp>/execution.properties
 *
 * Why we need this file? --- I want to find out which executionProperties were applied
 * to each Test Suite exection; default, demo, product. I want to display the value in
 * the ./Materials/index.html
 */
class ExecutionPropertiesWrapper {

    private def jsonObject
    private static slurper = new JsonSlurper()

    ExecutionPropertiesWrapper(Path path) {
        this(path.toFile())
    }
    ExecutionPropertiesWrapper(File file) {
        jsonObject = slurper.parse(file)
    }
    ExecutionPropertiesWrapper(String text) {
        jsonObject = slurper.parseText(text)
    }

    String getExecutionProfile() {
        return jsonObject.execution.general.executionProfile
    }
}
