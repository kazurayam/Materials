package com.kazurayam.materials.metadata

enum InvokedMethodName {
    
    RESOLVE_SCREENSHOT_PATH_BY_URL_PATH_COMPONENTS('resolveScreenshotPathByUrlPathComponents'),
    RESOLVE_SCREENSHOT_PATH('resolveScreenshotPath'),
    RESOLVE_MATERIAL_PATH('resolveMaterialPath');
    
    private final String methodName_
    
    // Reverse-lookup map for getting a InvokeMethodName from an string value
    private static final Map<String, InvokedMethodName> lookup = new HashMap<String, InvokedMethodName>()
    
    static {
        for (InvokedMethodName n : InvokedMethodName.values()) {
            lookup.putAt(n.getMethodName(), n)
        }
    }
    
    InvokedMethodName(String methodName) {
        this.methodName_ = methodName
    }
    
    String getMethodName() {
        return methodName_
    }
    
    @Override
    String toString() {
        return this.getMethodName()
    }
    
    static InvokedMethodName get(String methodName) {
        return lookup.get(methodName)
    }
}
