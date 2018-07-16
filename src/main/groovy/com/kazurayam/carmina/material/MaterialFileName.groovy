package com.kazurayam.carmina.material

import java.util.regex.Pattern
import java.util.regex.Matcher

class MaterialFileName {

    String[] parts = new String[4]
    // abc(1).cde  -> parts[0]=='abc.def (1).csv'
    //             -> parts[1]=='abc.def '
    //             -> parts[2]=='(1)'
    //             -> parts[3]=='.csv'
    private Suffix suffix_ = Suffix.NULL         // (1)
    private FileType fileType_ = FileType.NULL   // FileType.csv

    protected static final Pattern PTN_SUFFIX = Pattern.compile(/(.+\s*)(\((\d+)\))$/)

    MaterialFileName(String fileName) {
        parts[0] = fileName
        // for Extension
        String[] arr = fileName.split('\\.')
        String fileName2   // 'abc.def (1).csv' => 'abc.def (1)'
        if (arr.length < 2) {
            parts[3] = null
            fileName2 = fileName
        } else {
            parts[3] = '.' + arr[arr.length - 1]
            fileType_ = FileType.getByExtension(arr[arr.length - 1])
            fileName2 = fileName.substring(0, fileName.lastIndexOf('.'))
        }
        // for Suffix
        Matcher m = PTN_SUFFIX.matcher(fileName2)
        if (m.matches()) {
            parts[2] = m.group(2)
            int v = Integer.parseInt(m.group(3))
            suffix_ = new Suffix(v)
            parts[1] = m.group(1)
        } else {
            parts[2] = null
            parts[1] = fileName2
        }
    }

    Suffix getSuffix() {
        return suffix_
    }

    FileType getFileType() {
        return fileType_
    }
}
