package com.kazurayam.materials.view

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.xml.MarkupBuilder

class ConciseIndexer extends AbstractIndexer {
	
	static Logger logger_ = LoggerFactory.getLogger(ConciseIndexer.class)

	@Override
	RepositoryVisitorExtended createRepositoryVisitorGeneratingHtmlDivs(MarkupBuilder mb) {
		return new RepositoryVisitorGeneratingHtmlDivsParallel(mb)
	}

}
