package com.kazurayam.materials.view


import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Indexer

class ParallelIndexer 
        extends CarouselIndexer
        implements Indexer {
    
    static Logger logger_ = LoggerFactory.getLogger(ParallelIndexer.class)
            
    CarouselIndexer.ModalDesign modalDesign = CarouselIndexer.ModalDesign.PARALLEL
    
}
