package com.kazurayam.materials

class MaterialsException extends Exception {
    
    MaterialsException(String message) {
        super(message)
    }
    
    MaterialsException(String message, Throwable cause) {
        super(message, cause)
    }
    
    MaterialsException(Throwable cause) {
        super(cause)
    }

}
