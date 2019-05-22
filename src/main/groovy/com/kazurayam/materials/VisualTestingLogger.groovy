package com.kazurayam.materials

/**
 * Sample implemantation using Katalon Studio API:
 * <PRE>
 * import com.kazurayam.materials.VisualTestingLogger
 * import com.kms.katalon.core.util.KeywordUtil
 * 
 * class VisualTestingLoggerImpl implements VisualTestingLogger {
 *     void info(String message) {
 *         KeywordUtil.logInfo(message)
 *     }
 *     void failed(String message) {
 *         KeywordUtil.markFailed(message)
 *     }
 *     void fatal(String message) {
 *         KeywordUtil.markFailedAndStop(message)
 *     }
 * }
 * </PRE>
 * 
 * @author kazurayam
 *
 */
interface VisualTestingLogger {

    void info(String message)

    void failed(String message)

    void fatal(String message)

}