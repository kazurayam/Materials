package com.kazurayam.materials.model.storage

import com.kazurayam.materials.TSuiteTimestamp

class SelectBy {
    
    static SelectBy tSuiteTimestamp(TSuiteTimestamp tSuiteTimestamp) {
        
    }
    
    static SelectBy latest() {
        
    }

    /**
     * 
     * @param beforeHours
     * @return a SelectBy which selects a TSuiteExcecutionRecord which is 1st latest amongst
     * those are X hours before now. 
     */
    static SelectBy beforeHours(int beforeHours) {
        
    }
    
    static SelectBy beforeMinutes(int beforeMinutes) {
        
    }
    
}
