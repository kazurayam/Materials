package com.kazurayam.carmina.material

class MaterialFileNameFormatter {

    protected static final String MAGIC_DELIMITER = '§'

    /**
     * When '<pre>http:%3A%2F%2Fdemoaut.katalon.com%2F.§atoz.png</pre>' is given
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
     * When '<pre>http:%3A%2F%2Fdemoaut.katalon.com%2F§atoz.png</pre>' is given
     * as fileName argument, then returns an instance of com.kazurayam.carmina.Suffix of '<pre>atoz</pre>'
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
            String str = fileName.substring(0, fileName.lastIndexOf('.'))
            String[] arr = str.split(MAGIC_DELIMITER)
            if (arr.length < 2) {
                return Suffix.NULL
            }
            if (arr.length > 3) {
                Material.logger_.warn("#parseFileNameForSuffix ${fileName} contains 2 or " +
                    "more ${MAGIC_DELIMITER} character. " +
                    "Valid but unexpected.")
            }
            return new Suffix(arr[arr.length - 1])
        } else {
            return Suffix.NULL
        }
    }

    /**
     * When '<pre>http:%3A%2F%2Fdemoaut.katalon.com%2F§atoz.png</pre>' is given
     * as fileName argument, then returns an instance of java.net.URL of
     * '<pre>http://demoauto.katalon.com</pre>'
     *
     * @param fileName
     * @return
     */
    static URL parseFileNameForURL(String fileName) {
        FileType ft = parseFileNameForFileType(fileName)
        if (ft != FileType.NULL) {
            Suffix suffix = parseFileNameForSuffix(fileName)
            String urlstr
            if (suffix != Suffix.NULL) {
                urlstr = fileName.substring(0, fileName.lastIndexOf(MAGIC_DELIMITER))
            } else {
                urlstr = fileName.substring(0, fileName.lastIndexOf('.'))
            }
            String decoded = URLDecoder.decode(urlstr, 'UTF-8')
            try {
                URL url = new URL(decoded)
                return url
            } catch (MalformedURLException e) {
                Material.logger_.warn("#parseFileNameForURL unknown protocol in the var decoded='${decoded}'")
                return null
            }
        } else {
            return null
        }
    }
}
