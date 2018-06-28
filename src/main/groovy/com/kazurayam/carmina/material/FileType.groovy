package com.kazurayam.carmina.material

enum FileType {

    TXT  ('txt',    'text/plain',               'Text'                            ),
    CSV  ('csv',    'text/plain',               'Comma Separated Values'          ),
    BMP  ('bmp',    'image/bmp',                'Bitmap'                          ),
    GIF  ('gif',    'image/gif',                'Graphics Interchange Format'     ),
    JPG  ('jpg',    'image/jpeg',               'Joint Photographic Experts Group'),
    JPEG ('jpeg',   'image/jpeg',               'Joint Photographic Experts Group'),
    PNG  ('png' ,   'image/png',                'Portable Network Graphics'       ),
    JSON ('json',   'application/json',         'Javascript Object Notation'      ),
    PDF  ('pdf',    'application/pdf',          'Portable Document Format'        ),
    XLS  ('xls',    'application/vnd.ms-excel', 'Microsoft Excel (-> Excel 2003)' ),
    XLSM ('xlsm',   'application/vnd.ms-excel', 'Microsoft Excel (Excel 2007 ->)' ),
    XLSX ('xlsx',   'application/vnd.ms-excel', 'Microsoft Excel with Macro (Excel 2007 ->)'),
    XML  ('xml',    'application/xml',          'Extensible Markup Language'      ),

    NULL ('',       '',                         'Null object'                     ) ;

    private final String extension_
    private final String mimeType_
    private final String description_

    FileType(String extension, String mimeType, String description) {
        this.extension_ = extension
        this.mimeType_  = mimeType
        this.description_ = description
    }

    String getExtension() {
        return this.extension_
    }

    String getMimeType() {
        return this.mimeType_
    }

    String getDescription() {
        return this.description_
    }

    @Override
    String toString() {
        return toJson() //this.getExtension()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"FileType":{')
        sb.append('"extension":"' + this.getExtension() + '","mimeType":"' + this.getMimeType() +
            '","description":"' + this.getDescription() + '"')
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
