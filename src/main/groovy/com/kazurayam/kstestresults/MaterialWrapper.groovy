package com.kazurayam.kstestresults

import java.nio.file.Path

class MaterialWrapper {

    private TargetURL parentTargetURL
    private Path materialFilePath

    MaterialWrapper(TargetURL parent, Path materialFilePath) {
        this.parentTargetURL = parent
        this.materialFilePath = materialFilePath
    }

    TargetURL getTargetURL() {
        return parentTargetURL
    }

    Path getMaterialFilePath() {
        return materialFilePath
    }

    // ---------------- overriding Object properties --------------------------
    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof MaterialWrapper)) { return false }
        MaterialWrapper other = (MaterialWrapper)obj
        if (this.materialFilePath == other.getMaterialFilePath()) {
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        final int prime = 31
        int result = 1
        result = prime * result + this.getTargetURL().hashCode()
        result = prime * result + this.getMaterialFilePath().hashCode()
        return result
    }

    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append('"MaterialWrapper":')
        sb.append('{"materialFilePath":"' + Helpers.escapeAsJsonText(materialFilePath.toString()) + '"}')
        sb.append('}')
        return sb.toString()
    }
}
