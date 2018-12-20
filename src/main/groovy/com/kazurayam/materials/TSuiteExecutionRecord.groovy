package com.kazurayam.materials

interface TSuiteExecutionRecord extends Comparable<TSuiteExecutionRecord> {

    TSuiteName getTSuiteName()
    
    TSuiteTimestamp getTSuiteTimestamp()

}
