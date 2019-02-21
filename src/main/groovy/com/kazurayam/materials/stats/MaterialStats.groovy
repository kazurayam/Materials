package com.kazurayam.materials.stats

import java.nio.file.Path

import org.apache.commons.math3.distribution.TDistribution
import org.apache.commons.math3.stat.interval.ConfidenceInterval
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers

/**
 * referrences:
 * - https://blog.apar.jp/data-analysis/4632/
 * - https://stackoverflow.com/questions/14758509/calculating-t-inverse-in-apache-commons
 *
 */
class MaterialStats {
    
    static Logger logger_ = LoggerFactory.getLogger(MaterialStats.class)
    
    static final MaterialStats NULL = new MaterialStats(null, new ArrayList<ImageDelta>())
    
    static final String CRITERIA_PERCENTAGE_FORMAT = '%1$.2f'
    
    // following default values may be overridden by StorageScanner.Options object
    static final double DEFAULT_FILTER_DATA_LESS_THAN = 1.00
    static final double DEFAULT_PROBABILITY = 0.95
    static final int DEFAULT_MAXIMUM_NUMBER_OF_IMAGEDELTAS = 10
    
    private Path path
    private List<ImageDelta> imageDeltaList
    private double filterDataLessThan
    private double probability
    
    MaterialStats(Path path, List<ImageDelta> imageDeltaList) {
        this.path = path
        this.imageDeltaList = imageDeltaList
        this.filterDataLessThan = DEFAULT_FILTER_DATA_LESS_THAN
        this.probability = DEFAULT_PROBABILITY
    }

    Path getPath() {
        return path
    }
    
    String getPathAsStringInUNIX() {
        return path.toString().replace('\\', '/')
    }
    
    void setFilterDataLessThan(double value) {
        this.filterDataLessThan = value
    }
    
    void setProbability(double value) {
        this.probability = value
    }
    
    double[] data() {
        List<Double> list = new ArrayList<Double>()
        for (ImageDelta delta : this.getImageDeltaList()) {
            if (delta.getD() >= this.filterDataLessThan) {
                list.add(delta.getD())
            }
        }
        double[] data = list.toArray(new double[list.size()])
        return data
    }
    
    /**
     * number of sample data
     * @return
     */
    int degree() {
        return this.data().length
    }
    
    double sum() {
        double sum = 0.0
        for (int i = 0; i < this.degree(); i++) {
            sum += this.data()[i]
        }
        return sum
    }
    
    double mean() {
        if (this.degree() > 0) {
            double mean = this.sum()/ this.degree()
            return mean
        } else {
            logger_.warn("mean() returned 0 because this.degree() returned 0")
            return 0
        }
    }
    
    double variance() {
        if (this.degree() > 0) {
            // ssum
            double ssum = 0.0
            for (int i = 0; i < this.degree(); i++) {
                ssum += Math.sqrt(Math.abs(this.data()[i] - this.mean()))
            }
            // variance
            double variance = ssum / this.degree()
            return variance
        } else {
            logger_.warn("variance() returned 0 because this.degree() returned 0")
            return 0
        }
    }
    
    double standardDeviation() {
        return Math.sqrt(this.variance())
    }
    
    /**
     * @return calculate t-inverse with this.degree() degrees of FREEDOM %
     */
    double tDistribution() {
        if (this.degree() > 1) {
            TDistribution tdist = new org.apache.commons.math3.distribution.TDistribution(this.degree() - 1)
            return tdist.inverseCumulativeProbability(this.probability)
        } else {
            logger_.warn("tDistribution() returned 0 because this.degree() was ${this.degree()}")
            return 0
        }
    }
    
    ConfidenceInterval getConfidenceInterval() {
        if (this.degree() > 0) {
            double lowerBound = this.mean() - this.tDistribution() * this.standardDeviation() / Math.sqrt(this.degree())
            double upperBound = this.mean() + this.tDistribution() * this.standardDeviation() / Math.sqrt(this.degree())
            double confidenceLevel = this.probability
            return new ConfidenceInterval(lowerBound, upperBound, confidenceLevel)
        } else {
            logger_.warn("getConfidenceInterval() returned meaningless result because this.degree() returned 0")
            return new ConfidenceInterval(0.0, 0.0, 0.0)
        }
    }
    
    double getCalculatedCriteriaPercentage() {
        return this.getConfidenceInterval().getUpperBound()
    }
   
    String getCalculatedCriteriaPercentageAsString(String fmt = CRITERIA_PERCENTAGE_FORMAT) {
        return String.format(CRITERIA_PERCENTAGE_FORMAT, this.getCalculatedCriteriaPercentage())
    }
    
    List<ImageDelta> getImageDeltaList() {
        return imageDeltaList
    }
    
    /**
     * FIXME: too simple? should consider data?
     */
    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof MaterialStats))
            return false
        MaterialStats other = (MaterialStats)obj
        return this.getPath().equals(other.getPath())
    }
    
    @Override
    public int hashCode() {
        return this.getPath().hashCode()
    }
    
    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"path\":")
        sb.append("\"${Helpers.escapeAsJsonText(this.getPathAsStringInUNIX())}\",")
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
        sb.append("\"degree\":${this.degree().toString()},")
        sb.append("\"sum\":${this.sum()},")
        sb.append("\"mean\":${this.mean()},")
        sb.append("\"variance\":${this.variance()},")
        sb.append("\"standardDeviation\":${this.standardDeviation()},")
        sb.append("\"tDistribution\":${this.tDistribution()},")
        sb.append("\"calculatedCriteriaPercentage\":")
        sb.append(this.getCalculatedCriteriaPercentageAsString())
        sb.append("}")
        return sb.toString()
    }
}
