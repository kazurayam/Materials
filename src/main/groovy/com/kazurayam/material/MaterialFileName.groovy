package com.kazurayam.material

import java.util.regex.Matcher
import java.util.regex.Pattern

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This class wrapps the file name applying the file name format rule for Material.
 *
 * <pre>
 * MaterialFileName mfn = new MaterialFileName('abc.def (1).csv')
 * assert mfn.parts[0] == 'abc.def (1).csv'
 * assert mfn.parts[1] == 'abc.def '
 * assert mfn.parts[2] == '(1)'
 * assert mfn.parts[3] == '.csv'
 * assert mfn.getSuffix()   == new Suffix(1)
 * assert mfn.getFileType() == FileType.CSV
 * </pre>
 */
class MaterialFileName {

    static Logger logger_ = LoggerFactory.getLogger(MaterialFileName.class)

    String[] parts = new String[4]
    // abc(1).cde  -> parts[0]=='abc.def (1).csv'
    //             -> parts[1]=='abc.def '
    //             -> parts[2]=='(1)'
    //             -> parts[3]=='.csv'
    private URL url_ = null
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
        def decoded = ''
        try {
            decoded = URLDecoder.decode(parts[1], 'UTF-8')
            url_ = new URL(decoded)
        } catch (MalformedURLException e) {
            logger_.debug("unable to instanciate a URL object from parts[1] '${parts[1]}'")
        }
    }

    String getFileName() {
        return parts[0]
    }

    URL getURL() {
        return url_
    }

    Suffix getSuffix() {
        return suffix_
    }

    FileType getFileType() {
        return fileType_
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
    static String format(URL url, Suffix suffix, FileType fileType) {
        if (url != null) {
            String encodedUrl = URLEncoder.encode(url.toExternalForm(), 'UTF-8')
            if (suffix != Suffix.NULL) {
                return "${encodedUrl}${suffix.toString()}.${fileType.getExtension()}"
            } else {
                return "${encodedUrl}.${fileType.getExtension()}"
            }
        } else {
            return null
        }
    }

    static String formatEncoded(URL url, Suffix suffix, FileType fileType ) {
        if (url != null) {
            String doubleEncodedUrl = URLEncoder.encode(URLEncoder.encode(url.toExternalForm(), 'UTF-8'), 'UTF-8')
            if (suffix != Suffix.NULL) {
                return "${doubleEncodedUrl}${suffix.toString()}.${fileType.getExtension()}"
            } else {
                return "${doubleEncodedUrl}.${fileType.getExtension()}"
            }
        } else {
            return null
        }
    }

}
