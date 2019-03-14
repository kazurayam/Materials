package com.kazurayam.materials.stats

import com.kazurayam.materials.TSuiteTimestamp

class ImageDelta {
    
    private TSuiteTimestamp a
    private TSuiteTimestamp b
    private double d
    private boolean cached
    
    ImageDelta(TSuiteTimestamp a, TSuiteTimestamp b, double d) {
        this(a, b, d, false)    
    }
    
    ImageDelta(TSuiteTimestamp a, TSuiteTimestamp b, double d, boolean cached) {
        this.a = a
        this.b = b
        this.d = d
        this.cached = cached
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
    
    boolean isCached() {
        return cached    
    }
    
    void setCached(boolean cached) {
        this.cached = cached
    }
    
    @Override
    String toString() {
        return this.toJsonText()
    }
    
    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"a\":")
        sb.append("\"${a.format()}\",")
        sb.append("\"b\":")
        sb.append("\"${b.format()}\",")
        sb.append("\"d\":")
        sb.append(String.format('%1$.2f', this.getD()))
        sb.append(",")
        sb.append("\"cached\":")
        sb.append(this.cached)
        sb.append("}")
        return sb.toString()
    }
    
    static ImageDelta fromJsonObject(Object jsonObject) {
        Objects.requireNonNull(jsonObject, "jsonObject must not be null")
        if (jsonObject instanceof Map) {
            Map imageDeltaJsonObject = (Map)jsonObject
            if (imageDeltaJsonObject.a == null) {
                throw new IllegalArgumentException("json.a must not be null")
            }
            if (imageDeltaJsonObject.b == null) {
                throw new IllegalArgumentException("json.b must not be null")
            }
            TSuiteTimestamp a = new TSuiteTimestamp(imageDeltaJsonObject.a)
            TSuiteTimestamp b = new TSuiteTimestamp(imageDeltaJsonObject.b)
            double d = imageDeltaJsonObject.d
            boolean cached = true
            return new ImageDelta(a, b, d, cached)
        } else {
            throw new IllegalArgumentException("#fromJsonObject ")
        }          
    }
    
    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof ImageDelta)) { return false }
        ImageDelta other = (ImageDelta)obj
        return this.getA().equals(other.getA()) &&
                this.getB().equals(other.getB()) &&
                this.getD() == other.getD() &&
                this.cached == other.cached
    }
    
    @Override
    int hashCode() {
        int hash = 7
        hash = 31 * hash + this.getA().hashCode()
        hash = 31 * hash + this.getB().hashCode()
        hash = 31 * hash + (int)Math.round(this.getD())
        return hash
    }
    
}
