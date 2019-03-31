package com.kazurayam.materials.resolution

enum InvokedMethodName {
    
    RESOLVE_SCREENSHOT_PATH_BY_URL_PATH_COMPONENTS('resolveScreenshotPathByUrlPathComponents'),
    RESOLVE_SCREENSHOT_PATH('resolveScreenshotPath'),
    RESOLVE_MATERIAL_PATH('resolveMaterialPath');
    
    private final String methodName_
    
    InvokedMethodName(String methodName) {
        this.methodName_ = methodName
    }
    
    String getMethodName() {
        return methodName_
    }
    
    @Override
    String toString() {
        return methodName_
    }
}
