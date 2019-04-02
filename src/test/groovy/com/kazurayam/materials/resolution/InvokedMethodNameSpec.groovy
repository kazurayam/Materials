package com.kazurayam.materials.resolution

import com.kazurayam.materials.resolution.InvokedMethodNameSpec

import spock.lang.Specification

class InvokedMethodNameSpec extends Specification {

	def test_get_RESOLVE_MATERIAL_PATH() {
		expect:
			InvokedMethodName.get(InvokedMethodName.RESOLVE_MATERIAL_PATH.toString()) == 
					InvokedMethodName.RESOLVE_MATERIAL_PATH
	}
	
	def test_get_RESOLVE_SCREENSHOT_PATH() {
		expect:
			InvokedMethodName.get(InvokedMethodName.RESOLVE_SCREENSHOT_PATH.toString()) == 
					InvokedMethodName.RESOLVE_SCREENSHOT_PATH
	}
	
	def test_get_RESOLVE_SCREENSHOT_PATH_BY_URL_COMPONENTS() {
		expect:
			InvokedMethodName.get(InvokedMethodName.RESOLVE_SCREENSHOT_PATH_BY_URL_PATH_COMPONENTS.toString()) == 
					InvokedMethodName.RESOLVE_SCREENSHOT_PATH_BY_URL_PATH_COMPONENTS
	}
	
}
