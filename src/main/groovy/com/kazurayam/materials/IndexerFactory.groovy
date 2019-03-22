package com.kazurayam.materials

import com.kazurayam.materials.view.BaseIndexer
import com.kazurayam.materials.view.IndexerByVisitorImpl
import com.kazurayam.materials.view.IndexerRudimentaryImpl

final class IndexerFactory {

    private IndexerFactory() {}
    
    static Indexer newIndexer() {
        //return new IndexerRudimentaryImpl()
        //return new IndexerByVisitorImpl()     // commented out at 2019/03/19
        
        return new BaseIndexer()
    }

    static Indexer newIndexer(String indexerClassName)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> c
        c = Class.forName(indexerClassName)
        Indexer indexer = (Indexer)c.newInstance()
        return indexer
    }
}
