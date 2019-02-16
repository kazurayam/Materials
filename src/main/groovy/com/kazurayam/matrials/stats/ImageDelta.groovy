package com.kazurayam.matrials.stats

class ImageDelta {
    
    private String a
    private String b
    private double d
    
    ImageDelta(String a, String b, double d) {
        this.a = a
        this.b = b
        this.d = d
    }
    
    String getA() {
        return a
    }
    
    String getB() {
        return b
    }
    
    double getD() {
        return d
    }
}
