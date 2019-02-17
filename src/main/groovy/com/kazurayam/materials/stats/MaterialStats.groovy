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
    
    double[] data() {
        List<Double> list = new ArrayList<Double>()
        for (ImageDelta delta : this.getImageDeltaList()) {
            list.add(delta.getD())
        }
        double[] data = list.toArray(new double[list.size()])
        return data
    }
    
    int dataLength() {
        return this.data().length
    }
    
    double sum() {
        double[] data = this.data()
        // sum
        double sum = 0.0
        for (int i = 0; i < data.length; i++) {
            sum += data[i]
        }
        return sum
    }
    
    double mean() {
        double mean = this.sum()/ this.data().length
        return mean
    }
    
    double variance() {
        double[] data = this.data()
        // ssum
        double ssum = 0.0
        for (int i = 0; i < data.length; i++) {
            ssum += Math.sqrt(data[i] - mean)
        }
        // variance
        double variance = ssum / data.length
        return variance
    }
    
    double standardDeviation() {
        return Math.sqrt(this.variance())
    }
    
    double getCalculatedCriteriaPercentage() {
        // mean
        double mean = this.mean()
        // standard deviation
        double sd = this.standardDeviation()
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
        sb.append("\"data\":${this.data().toString()},")
        sb.append("\"sum\":${this.sum()},")
        sb.append("\"mean\":${this.mean()},")
        sb.append("\"variance\":${this.variance()},")
        sb.append("\"standardDeviation\":${this.standardDeviation()},")
        sb.append("\"calculatedCriteriaPercentage\":")
        //sb.append(String.format('%1$.2f', this.getCalculatedCriteriaPercentage()))
        sb.append(this.getCalculatedCriteriaPercentage())
        sb.append("}")
        return sb.toString()
    }
}
