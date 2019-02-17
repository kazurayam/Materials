package com.kazurayam.materials.stats

import com.kazurayam.materials.TSuiteTimestamp

class ImageDelta {
    
    private TSuiteTimestamp a
    private TSuiteTimestamp b
    private double d
    
    ImageDelta(TSuiteTimestamp a, TSuiteTimestamp b, double d) {
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
    
    @Override
    String toString() {
        return this.toJson()
    }
    
    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"a\":")
        sb.append("\"${a.format()}\",")
        sb.append("\"b\":")
        sb.append("\"${b.format()}\",")
        sb.append("\"d\":")
        sb.append(String.format('%1$.2f', this.getD()))
        sb.append("}")
        return sb.toString()
    }
}
