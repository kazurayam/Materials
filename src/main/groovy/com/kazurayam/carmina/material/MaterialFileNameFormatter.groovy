package com.kazurayam.carmina.material

import java.util.regex.Matcher
import java.util.regex.Pattern

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MaterialFileNameFormatter {

    static Logger logger_ = LoggerFactory.getLogger(MaterialFileNameFormatter.class)

    protected static final Pattern PTN_SUFFIX = Pattern.compile(/(.+\s*)\((\d+)\)$/)

    /**
     * When '<pre>http:%3A%2F%2Fdemoaut.katalon.com%2F (1).png</pre>' is given
     * as fileName argument, then returns com.kazurayam.carmina.FileType.PNG
     *
     * @param fileName
     * @return
     */
    static FileType parseFileNameForFileType(String fileName) {
        String[] arr = fileName.split('\\.')
        if (arr.length < 2) {
            return FileType.NULL
        } else {
            String candidate = arr[arr.length - 1]
            FileType ft = FileType.getByExtension(candidate)
        }
    }

    /**
     * When '<pre>http:%3A%2F%2Fdemoaut.katalon.com%2F (1).png</pre>' is given
     * as fileName argument, then returns an instance of com.kazurayam.carmina.Suffix of '<pre>(1)</pre>'
     *
     * When '<pre>http:%3A%2F%2Fdemoaut.katalon.com%2F.png</pre>' is given
     * as fileName argument, then returns com.kazurayam.carmina.Suffix.NULL
     *
     * @param fileName
     * @return
     */
    static Suffix parseFileNameForSuffix(String fileName) {
        FileType ft = parseFileNameForFileType(fileName)
        if (ft != FileType.NULL) {
            String str = fileName.substring(0, fileName.lastIndexOf('.'))  // 'abc (1).txt' => 'abc (1)'
            Matcher m = PTN_SUFFIX.matcher(str)
            if (m.matches()) {
                int v = Integer.parseInt(m.group(2))
                return new Suffix(v)
            } else {
                return Suffix.NULL
            }
        } else {
            return Suffix.NULL
        }
    }

    /**
     * if 'abc def (1).txt' is given as fileName, then return 'abc def'.
     *
     * - chomp the file name extension '.txt'
     * - chomp the suffix. E.g., '(1)'
     *
     * @param fileName
     * @return
     */
    static String parseFileNameForBody(String fileName) {
        FileType ft = parseFileNameForFileType(fileName)
        if (ft != FileType.NULL) {
            String str = fileName.substring(0, fileName.lastIndexOf('.'))
            Matcher m = PTN_SUFFIX.matcher(str)
            if (m.matches()) {
                return m.group(1)
            } else {
                return str
            }
        } else {
            return fileName
        }
    }

    /**
     * When '<pre>http:%3A%2F%2Fdemoaut.katalon.com%2F(1).png</pre>' is given
     * as fileName argument, then returns an instance of java.net.URL of
     * '<pre>http://demoauto.katalon.com/</pre>'
     *
     * @param fileName
     * @return
     */
    static URL parseFileNameForURL(String fileName) {
        String fileNameBody = parseFileNameForBody(fileName)
        String decoded = URLDecoder.decode(fileNameBody, 'UTF-8')
        try {
            URL url = new URL(decoded)
            return url
        } catch (MalformedURLException e) {
            //logger_.debug("#parseFileNameForURL failed to instanciate URL with arg='${decoded}'")
            return null
        }
    }


    /**
     * Determines the file name of a Material. The file name is in the format:
     *
     * <pre>&lt;encoded URL string&gt;.&lt;file extension&gt;</pre>
     *
     * for example:
     *
     * <pre>http:%3A%2F%2Fdemoaut.katalon.com%2F.png</pre>
     *
     * or
     *
     * <pre>&lt;encoded URL string&gt;§&lt;suffix string&gt;.&lt;file extension&gt;</pre>
     *
     * for example:
     *
     * <pre>http:%3A%2F%2Fdemoaut.katalon.com%2F§atoz.png</pre>
     *
     * @param url
     * @param suffix
     * @param fileType
     * @return
     */
    static String resolveMaterialFileName(URL url, Suffix suffix, FileType fileType) {
        String encodedUrl = URLEncoder.encode(url.toExternalForm(), 'UTF-8')
        if (suffix != Suffix.NULL) {
            return "${encodedUrl}${suffix.toString()}.${fileType.getExtension()}"
        } else {
            return "${encodedUrl}.${fileType.getExtension()}"
        }
    }

    static String resolveEncodedMaterialFileName(URL url, Suffix suffix, FileType fileType ) {
        String doubleEncodedUrl = URLEncoder.encode(URLEncoder.encode(url.toExternalForm(), 'UTF-8'), 'UTF-8')
        if (suffix != Suffix.NULL) {
            return "${doubleEncodedUrl}${suffix.toString()}.${fileType.getExtension()}"
        } else {
            return "${doubleEncodedUrl}.${fileType.getExtension()}"
        }
    }

}
