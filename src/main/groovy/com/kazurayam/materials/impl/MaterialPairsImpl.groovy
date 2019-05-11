package com.kazurayam.materials.impl

import java.nio.file.Path

import com.kazurayam.materials.MaterialPair
import com.kazurayam.materials.MaterialPairs

class MaterialPairsImpl implements MaterialPairs {

    private Map<Path, MaterialPair> map
    
    private MaterialPairsImpl() {
        map = new HashMap<Path, MaterialPair>()
    }
    
    public static MaterialPairs() {
        return new MaterialPairsImpl()
    }

    @Override
    public MaterialPair put(Path pathRelativeToTSuiteTimestamp, MaterialPair materialPair) {
        map.put(pathRelativeToTSuiteTimestamp, materialPair)
        return materialPair
    }

    @Override
    public MaterialPair get(Path pathRelativeToTSuiteTimestamp) {
        return map.get(pathRelativeToTSuiteTimestamp)
    }

    @Override
    public Set<Path> keySet() {
        return map.keySet()
    }

    @Override
    public boolean containsKey(Path pathRelativeToTSuiteTimestamp) {
        return map.containsKey(pathRelativeToTSuiteTimestamp)
    }

    @Override
    public int size() {
        return map.size()
    }
}
