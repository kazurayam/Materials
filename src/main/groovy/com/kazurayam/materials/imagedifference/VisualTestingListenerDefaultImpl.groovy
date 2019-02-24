package com.kazurayam.materials.imagedifference

//import com.kms.katalon.core.logging.KeywordLogger
//import com.kms.katalon.core.util.KeywordUtil

/**
 * The instance of DefaulVisualTestingListener registered into ImageCollectionDiffer will
 * print messages emitted by ImageCollectionDiffer
 * 
 * @author kazurayam
 */
class VisualTestingListenerDefaultImpl implements VisualTestingListener {

    void info(String message) {
        System.out.println("INFO " + message)
    }
    
    void failed(String message) {
        System.err.println("FAILED " + message)
    }
    
    void fatal(String message) {
        System.err.println("FATAL " + message)
    }
    
    //private KeywordLogger logger = new KeywordLogger()
    //void info(String message) {
    //    logger.logWarning(message)
    //}
    //void failed(String message) {
    //    logger.logFailed(message)
    //    KeywordUtil.markFailed(message)
    //}
    //void fatal(String message) {
    //    logger.logFailed(message)
    //    KeywordUtil.markFailedAndStop(message)
    //}
}