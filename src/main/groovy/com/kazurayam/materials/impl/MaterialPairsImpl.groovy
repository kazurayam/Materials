package com.kazurayam.materials.impl

import java.nio.file.Path

import com.kazurayam.materials.Material
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
    public MaterialPair putExpectedMaterial(Material expectedMaterial) {
        Path p = expectedMaterial.getPathRelativeToTSuiteTimestamp()
        MaterialPair pair
        if (map.containsKey(p)) {
            pair = map.get(p)
        } else {
            pair = MaterialPairImpl.MaterialPair()
        }
        pair.setExpected(expectedMaterial)
        map.put(p, pair)
        return pair
    }

    @Override
    public MaterialPair putActualMaterial(Material actualMaterial) {
        Path p = actualMaterial.getPathRelativeToTSuiteTimestamp()
        MaterialPair pair
        if (map.containsKey(p)) {
            pair = map.get(p)
        } else {
            pair = MaterialPairImpl.MaterialPair()
        }
        pair.setActual(actualMaterial)
        map.put(p, pair)
        return pair
    }

    @Override
    public MaterialPair get(Path pathRelativeToTSuiteTimestamp) {
        return map.get(pathRelativeToTSuiteTimestamp)
    }
    
    @Override
    public List<MaterialPair> getList() {
        Set<Path> keySet = this.keySet()
        List<Path> keyList = new ArrayList<Path>(keySet)
        Collections.sort(keyList)
        List<MaterialPair> result = new ArrayList<MaterialPair>()
        for (Path key : keyList) {
            result.add(this.get(key))
        }
        return result
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
