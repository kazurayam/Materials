package com.kazurayam.materials.view


import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Indexer

class ParallelIndexer 
        extends BaseIndexer
        implements Indexer {
    
    static Logger logger_ = LoggerFactory.getLogger(ParallelIndexer.class)
            
    BaseIndexer.ModalDesign modalDesign = BaseIndexer.ModalDesign.PARALLEL
    
}
