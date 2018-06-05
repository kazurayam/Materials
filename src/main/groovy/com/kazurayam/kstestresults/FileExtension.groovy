package com.kazurayam.kstestresults

enum FileExtension {

    PLAIN('txt',   'text/plain'),
    HTML('html',   'text/html'),
    CSS('css',     'text/css'),
    JS('js',       'text/javascript'),
    GIF('gif',     'image/gif'),
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

    FileExtension(String extension, String mimeType) {
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
    
    static FileExtension getByExtension(String ext) {
        for (FileExtension v : values()) {
            if (v.getExtension() == ext) {
                return v
            }
        }
        throw new IllegalArgumentException("undefined: ${ext}")
    }
    
    static FileExtension getByMimeType(String mime) {
        for (FileExtension v : values()) {
            if (v.getMimeType() == mime) {
                return v
            }
        }
        throw new IllegalArgumentException("undefined: ${mime}")
    }

}
