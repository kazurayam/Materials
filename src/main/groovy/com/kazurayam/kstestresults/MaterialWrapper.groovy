package com.kazurayam.kstestresults

import java.nio.file.Path

class MaterialWrapper {

    private TargetURL parent
    private Path materialFilePath
    private FileType fileType

    MaterialWrapper(Path materialFilePath, FileType fileType) {
        this.materialFilePath = materialFilePath
        this.fileType = fileType
    }

    MaterialWrapper setParent(TargetURL parent) {
        this.parent = parent
        return this
    }

    TargetURL getParent() {
        return this.parent
    }

    TargetURL getTargetURL() {
        return this.getParent()
    }

    Path getMaterialFilePath() {
        return materialFilePath
    }

    FileType getFileType() {
        return fileType
    }

    Path getRelativePathToTsTimestampDir() {
        if (parent != null) {
            Path tsTimestampDir =
                this.getTargetURL()
                    .getTcResult().getTsResult().getTsTimestampDir()
                    Path path = tsTimestampDir.relativize(this.materialFilePath).normalize()
        return path
        } else {
            throw new IllegalStateException('parent TargetURL is null')
        }
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

    // ---------------- helpers -----------------------------------------------


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
