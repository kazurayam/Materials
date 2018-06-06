package com.kazurayam.kstestresults

import java.nio.file.Path

class MaterialWrapper {

    private TargetURL parentTargetURL
    private Path materialFilePath
    private FileType fileType

    MaterialWrapper(TargetURL parent, Path materialFilePath, FileType fileType) {
        this.parentTargetURL = parent
        this.materialFilePath = materialFilePath
        this.fileType = fileType
    }

    TargetURL getTargetURL() {
        return parentTargetURL
    }

    Path getMaterialFilePath() {
        return materialFilePath
    }

    FileType getFileType() {
        return fileType
    }

    Path getRelativePathToTsTimestampDir() {
        Path tsTimestampDir =
            this.getTargetURL()
                .getParentTcResult().getParentTsResult().getTsTimestampDir()
        Path path = tsTimestampDir.relativize(this.materialFilePath).normalize()
        return path
    }

    /**
     * relative path to the TestSuiteName/Timestamp directory
     */
    String getRelativePathAsString() {
        return this.getRelativePathToTsTimestampDir().toString()
    }

    /**
     *
     * @return
     */
    String getRelativeUrlAsString() {
        return this.getRelativePathAsString().replace('\\','/').replace('%','%25')
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
