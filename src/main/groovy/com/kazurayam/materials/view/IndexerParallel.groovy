package com.kazurayam.materials.view

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Indexer
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.impl.VisualTestingLoggerDefaultImpl
import com.kazurayam.materials.repository.RepositoryVisitor

import groovy.xml.MarkupBuilder

class IndexerParallel extends IndexerBase implements Indexer {
    
    static Logger logger_ = LoggerFactory.getLogger(IndexerParallel.class)
    
    @Override
    RepositoryVisitor createRepositoryVisitorGeneratingHtmlDivs(MarkupBuilder mkbuilder) {
        return new RepositoryVisitorGeneratingHtmlDivsAsModalParallel(mkbuilder)
    }
}
