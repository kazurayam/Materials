package com.kazurayam.materials.stats

import com.kazurayam.materials.TSuiteTimestamp

class Delta {
    
    private TSuiteTimestamp a
    private TSuiteTimestamp b
    private double d
    
    Delta(TSuiteTimestamp a, TSuiteTimestamp b, double d) {
        this.a = a
        this.b = b
        this.d = d
    }
    
    TSuiteTimestamp getA() {
        return a
    }
    
    TSuiteTimestamp getB() {
        return b
    }
    
    double getD() {
        return d
    }
}
