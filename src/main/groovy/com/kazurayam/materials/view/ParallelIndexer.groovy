package com.kazurayam.materials.view

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Indexer
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.impl.VisualTestingLoggerDefaultImpl

import groovy.xml.MarkupBuilder

class ParallelIndexer extends IndexerBase implements Indexer {
    
    static Logger logger_ = LoggerFactory.getLogger(ParallelIndexer.class)
    VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()
            
    CarouselIndexer.ModalDesign modalDesign = CarouselIndexer.ModalDesign.PARALLEL
    
    @Override
    void setVisualTestingLogger(VisualTestingLogger vtLogger) {
        this.vtLogger_ = vtLogger
    }
    
    @Override
    RepositoryVisitorExtention createRepositoryVisitorGeneratingHtmlDivs(MarkupBuilder mkbuilder) {
        return new RepositoryVisitorGeneratingHtmlDivsAsModalParallel(mkbuilder)
    }
}
