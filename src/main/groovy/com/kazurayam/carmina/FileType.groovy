package com.kazurayam.carmina

enum FileType {

    TXT('txt',     'text/plain'),
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
    XLS('.xls',    'application/vnd.ms-excel'),
    XLSM('.xlsm',  'application/vnd.ms-excel'),
    XLSX('.xlsx',  'application/vnd.ms-excel'),
    XML('xml',     'application/xml' ),
    XHTML('xhtml', 'application/xhtml+html'),
    PDF('pdf',     'application/pdf'),

    NULL('', '') ;

    private final String extension_
    private final String mimeType_

    FileType(String extension, String mimeType) {
        this.extension_ = extension
        this.mimeType_  = mimeType
    }

    String getExtension() {
        return this.extension_
    }

    String getMimeType() {
        return this.mimeType_
    }

    @Override
    String toString() {
        return toJson() //this.getExtension()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"FileType":{')
        sb.append('"extension":"' + this.getExtension() + '","mimeType":"' + this.getMimeType() + '"')
        sb.append('}}')
        return sb.toString()
    }
    static FileType getByExtension(String ext) {
        for (FileType v : values()) {
            if (v.getExtension().toLowerCase() == ext.toLowerCase()) {
                return v
            }
        }
        return FileType.NULL
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
