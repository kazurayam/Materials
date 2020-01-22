package com.kazurayam.materials.view

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.RepositoryVisitor

import groovy.xml.MarkupBuilder

/**
 * 
 * @author kazurayam
 *
 */
class IndexerCarousel extends IndexerBase {
    
    protected static Logger logger_ = LoggerFactory.getLogger(IndexerCarousel.class)

    @Override
    RepositoryVisitor createRepositoryVisitorGeneratingHtmlDivs(RepositoryRoot repoRoot, MarkupBuilder mb) {
        return new RepositoryVisitorGeneratingHtmlDivsAsModalCarousel(repoRoot, mb)
    }
    
}
