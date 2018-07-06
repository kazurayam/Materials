package com.kazurayam.carmina.util

import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

/**
 *
 */
class FileBeingDownloaded {

    Path path_
    long lastModifiedTime_
    long size_

    static private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS")

    FileBeingDownloaded(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            throw new IOException("${path.toString()} is not a regular file")
        }
        if (!Files.isReadable(path)) {
            throw new IOException("${path.toString()} is not readable")
        }
        path_ = path
        lastModifiedTime_ = Files.getLastModifiedTime(path).to(TimeUnit.MILLISECONDS)
        size_ = Files.size(path)
    }

    Path getPath() {
        return path_
    }

    long getLastModifiedTime() {
        return lastModifiedTime_
    }

    long size() {
        return size_
    }

    @Override
    boolean equals(Object obj) {
        //if (this == obj) { return true }
        if (!(obj instanceof FileBeingDownloaded)) { return false }
        FileBeingDownloaded other = (FileBeingDownloaded)obj
        return (this.path_ == other.getPath() &&
            this.lastModifiedTime_ == other.getLastModifiedTime() &&
            this.size_ == other.size() )
    }

    @Override
    int hashCode() {
        return path_.hashCode()
    }

    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"path\":\"${path_.toString()}\",")
        sb.append("\"lastModifiedTime\":${lastModifiedTime_},")
        sb.append("\"size\":${size_}")
        sb.append("}")
        return sb.toString()
    }
}