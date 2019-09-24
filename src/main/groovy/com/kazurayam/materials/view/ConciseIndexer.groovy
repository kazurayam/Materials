package com.kazurayam.materials.view

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.xml.MarkupBuilder

class ConciseIndexer extends IndexerBase {
	
	static Logger logger_ = LoggerFactory.getLogger(ConciseIndexer.class)

	@Override
	RepositoryVisitorExtention createRepositoryVisitorGeneratingHtmlDivs(MarkupBuilder mb) {
		return new RepositoryVisitorGeneratingHtmlDivsAsModalParallel(mb)
	}

}
