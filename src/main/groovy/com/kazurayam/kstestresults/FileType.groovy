package com.kazurayam.kstestresults

enum FileType {

    PLAIN('txt',   'text/plain'),
    CSV('csv',     'text/plain'),
    HTML('html',   'text/html'),
    CSS('css',     'text/css'),
    JS('js',       'text/javascript'),
    GIF('gif',     'image/gif'),
    JPG('jpg',     'image/jpeg'),
    JPEG('jpeg',   'image/jpeg'),
    PNG('png' ,    'image/png'),
    BMP('bmp',     'image/bmp'),
    SVG('svg',     'image/svg+xml'),
    OCTET('bin',   'application/octet-stream'),
    JSON('json',   'application/json'),
    XML('xml',     'application/xml' ),
    XHTML('xhtml', 'application/xhtml+html'),
    PDF('pdf',     'application/pdf');

    private final String extension
    private final String mimeType

    FileType(String extension, String mimeType) {
        this.extension = extension
        this.mimeType  = mimeType
    }

    String toString() {
        return this.getExtension()
    }

    String getExtension() {
        return this.extension
    }

    String getMimeType() {
        return this.mimeType
    }

    static FileType getByExtension(String ext) {
        for (FileType v : values()) {
            if (v.getExtension() == ext) {
                return v
            }
        }
        System.err.println("FileType#getByExtension: undefined: ${ext}")
        return null
    }

    static FileType getByMimeType(String mime) {
        for (FileType v : values()) {
            if (v.getMimeType() == mime) {
                return v
            }
        }
        System.err.println("FileType#getByMimeType: undefined: ${mime}")
        return null
    }

}
