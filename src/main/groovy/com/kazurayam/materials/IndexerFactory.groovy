package com.kazurayam.materials

import com.kazurayam.materials.view.BaseIndexer

final class IndexerFactory {

    private IndexerFactory() {}
    
    static Indexer newIndexer() {
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
