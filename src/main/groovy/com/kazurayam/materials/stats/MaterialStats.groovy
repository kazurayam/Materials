package com.kazurayam.materials.stats

import java.nio.file.Path

class MaterialStats {

    private Path path
    private List<ImageDelta> imageDeltaList
    
    MaterialStats(Path path, List<ImageDelta> imageDeltaList) {
        this.path = path
        this.imageDeltaList = imageDeltaList
    }

    Path getPath() {
        return path
    }
    
    double getCalculatedCriteriaPercentage() {
        List<Double> list = new ArrayList<Double>()
        for (ImageDelta delta : this.getImageDeltaList()) {
            list.add(delta.getD())
        }
        double[] data = list.toArray(new double[list.size()])
        // sum
        double sum = 0.0 
        for (int i = 0; i < data.length; i++) {
            sum += data[i]
        }
        // mean
        double mean = sum / data.length
        // ssum
        double ssum = 0.0
        for (int i = 0; i < data.length; i++) {
            ssum += Math.sqrt(data[i] - mean)
        }
        // variance
        double variance = ssum / data.length
        // standard deviation
        double sd = Math.sqrt(variance)
        //
        return mean + sd * 2
    }
    
    List<ImageDelta> getImageDeltaList() {
        return imageDeltaList
    }
    
    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"path\":")
        sb.append("\"${this.getPath().toString()}\",")
        sb.append("\"imageDeltaList\":[")
        int count = 0
        for (ImageDelta id : this.getImageDeltaList()) {
            if (count > 0) {
                sb.append(",")
            }
            sb.append(id.toJson())
            count += 1
        }
        sb.append("],")
        sb.append("\"calculatedCriteriaPercentage\":")
        //sb.append(String.format('%1$.2f', this.getCalculatedCriteriaPercentage()))
        sb.append(this.getCalculatedCriteriaPercentage())
        sb.append("}")
        return sb.toString()
    }
}
