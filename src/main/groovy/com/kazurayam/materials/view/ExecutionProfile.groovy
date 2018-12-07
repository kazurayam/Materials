package com.kazurayam.materials.view

class ExecutionProfile implements Comparable<ExecutionProfile> {

    private String profileName_

    ExecutionProfile(String profileName) {
        profileName_ = profileName
    }

    String getName() {
        return profileName_
    }

    // ---------------- overriding Object properties --------------------------
    @Override
    String toString() {
        return profileName_
    }

    @Override
    public boolean equals(Object obj) {
        //if (this == obj)
        //    return true
        if (!(obj instanceof ExecutionProfile))
            return false
        ExecutionProfile other = (ExecutionProfile)obj
        return this.getName().equals(other.getName())
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode()
    }

    @Override
    int compareTo(ExecutionProfile other) {
        return this.getName().compareTo(other.getName())
    }

}
