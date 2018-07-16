package com.kazurayam.carmina.material

import java.util.regex.Pattern
import java.util.regex.Matcher

class MaterialFileName {

    static Pattern MATERIAL_FILENAME_PATTERN = Pattern.compile(/([ 0-9a-zA-Z%_\-\.]+)(\((\d+)\))?(\.([0-9a-zA-Z]+))?$/)

    static List<String> parse(String fileName) {
        Matcher m = MATERIAL_FILENAME_PATTERN.matcher(fileName)
        if (m.matches()) {
            List<String> groups = new ArrayList<>()
            for (int i = 0; i <= m.groupCount(); i++) {
                groups.add(m.group(i))
            }
            return groups
        } else {
            throw new IllegalArgumentException("fileName '${fileName}' does not match Pattern ${MATERIAL_FILENAME_PATTERN.toString()}")
        }
    }


}
