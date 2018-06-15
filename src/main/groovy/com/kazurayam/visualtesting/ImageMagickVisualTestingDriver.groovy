package com.kazurayam.visualtesting

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path

import com.kazurayam.carmina.TestResultsRepository

/**
 * Visual Testing engine using ImageMagick
 *
 * @author kazurayam
 *
 */
class ImageMagickVisualTestingDriver implements VisualTestingDriver {

    static Logger logger_ = LoggerFactory.getLogger(ImageMagickVisualTestingDriver.class)

    private TestResultsRepository target_
    private TestResultsRepository basis_
    private Path outputDir_

    ImageMagickVisualTestingDriver(TestResultsRepository target) {
        this.target_ = target
    }

    void setOutput(Path outputDir) {
        this.outputDir_ = outputDir
    }

    void execute() {
        //throw new UnsupportedOperationException("TODO")
    }

    /**
     * Run a ImageMagick command from Java, wait for the command to finish, and
     * squeeze the standard output and the standard error into the output streams given.
     *
     * Will check for the Environement Variable 'IMAGEMAGICK_HOME' to find the absolute
     * location of the ImageMagick commands executables.
     *
     * @param args
     * @param out
     * @param err
     * @return
     */
    static int runImageMagickCommand(String[] args, OutputStream out, OutputStream err) {
        if (args == null) { throw new IllegalArgumentException("1st argument arg is null") }
        if (out == null) { throw new IllegalArgumentException("2nd argument out is null") }
        if (err == null) { throw new IllegalArgumentException("3rd argument err is null") }
        if (args.length == 0) {
            throw new IllegalArgumentException("Usage: <imagemagick command> args...")
        }
        def envVarName = 'IMAGEMAGICK_HOME'
        def envVar = System.getenv(envVarName)
        if (envVar != null) {
            if (envVar.endsWith('/') || envVar.endsWith('\\')) {
                args[0] = "${envVar}${args[0]}"
            } else {
                args[0] = "${envVar}/${args[0]}"
            }
        }
        CommandRunner.runCommand(args, out, err)
    }


}
