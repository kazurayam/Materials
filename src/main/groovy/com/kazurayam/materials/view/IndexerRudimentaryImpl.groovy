package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Indexer
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.repository.RepositoryFileScanner
import com.kazurayam.materials.repository.RepositoryRoot

/**
 * This class is NOT used at all. Should be removed.
 * 
 * @author kazurayam
 *
 */
final class IndexerRudimentaryImpl implements Indexer {

    static Logger logger_ = LoggerFactory.getLogger(IndexerRudimentaryImpl.class)

    private Path baseDir_
    private Path reportsDir_
    private Path output_
    
    private VisualTestingLogger vtLogger_

    IndexerRudimentaryImpl() {
        baseDir_ = null
        reportsDir_ = null
        output_ = null
    }
    
    @Override
    Path getOutput() {
        return this.output_
    }

    @Override
    void setBaseDir(Path baseDir) {
        if (baseDir == null) {
            def msg = "#setBaseDir baseDir argument is null"
            logger_.error(msg)
            throw new IllegalArgumentException(msg)
        }
        if (Files.notExists(baseDir)) {
            def msg = "#setBaseDir basedir ${baseDir.toString()} does not exist"
            logger_.error(msg)
            throw new IllegalArgumentException(msg)
        }
        baseDir_ = baseDir
    }

    @Override
    void setReportsDir(Path reportsDir) {
        if (reportsDir == null) {
            def msg = "#setReportsDir reportsDir argument is null"
            logger_.error(msg)
            throw new IllegalArgumentException(msg)
        }
        if (Files.notExists(reportsDir)) {
            def msg = "#setReportsDir reportsDir ${reportsDir.toString()} does not exist"
            logger_.error(msg)
            throw new IllegalArgumentException(msg)
        }
        reportsDir_ = reportsDir
    }

    @Override
    void setOutput(Path outputFile) {
        output_ = outputFile
        Helpers.ensureDirs(outputFile.getParent())
    }
    
    @Override
    void setVisualTestingLogger(VisualTestingLogger vtLogger) {
        this.vtLogger_ = vtLogger
    }

    /**
     *
     * @return
     * @throws IOException
     */
    @Override
    void execute() throws IOException {
        if (baseDir_ == null) {
            def msg = "#execute baseDir_ is null"
            logger_.error(msg)
            throw new IllegalStateException(msg)
        }
        if (output_ == null) {
            def msg = "#execute output_ is null"
            logger_.error(msg)
            throw new IllegalStateException(msg)
        }
        RepositoryFileScanner scanner = new RepositoryFileScanner(baseDir_)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        OutputStream os = output_.toFile().newOutputStream()
        this.generate(repoRoot, os)
        logger_.info("generated ${output_.toString()}")
    }

    /**
     *
     * @param repoRoot
     * @param os
     * @throws IOException
     */
    void generate(RepositoryRoot repoRoot, OutputStream os) throws IOException {
        def dir = repoRoot.getBaseDir().resolve('../..').normalize().toAbsolutePath()
        def title = dir.relativize(repoRoot.getBaseDir().normalize().toAbsolutePath()).toString()
        def writer = new OutputStreamWriter(os, 'UTF-8')
        StringBuilder sb = new StringBuilder()
        sb.append('<html>'                                                                    + "\n")
        sb.append('  <head>'                                                                  + "\n")
        sb.append('    <meta http-equiv="X-UA-Compatible" content="IE=edge" />'               + "\n")
        sb.append('    <title>' + title + '</title>' + "\n")
        sb.append('    <meta charset="utf-8" />'                                              + "\n")
        sb.append('    <meta name="description" content="" />'                                + "\n")
        sb.append('    <meta name="author" content="" />'                                     + "\n")
        sb.append('    <meta name="viewport" content="width=device-width, initial-scale=1" />'+ "\n")
        sb.append('    <link rel="stylesheet" href="" />'                                     + "\n")
        sb.append('<!-- [if lt IE 9]'                                                         + "\n")
        sb.append('<script src="//cdn.jsdelivr.net/html5shiv/3.7.2/html5shiv.min.js"></script>' + "\n")
        sb.append('<script src="//cdnjs.cloudflare.com/ajax/libs/respond.js/1.4.2/respond.min.js"></script>' + "\n")
        sb.append('<![endif] -->'                                                             + "\n")
        sb.append('    <link rel="shortcut icon" href="" />'                                  + "\n")
        sb.append('    <link href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css"')
        sb.append(' rel="stylesheet"')
        sb.append(' integrity="sha384-WskhaSGFgHYWDcbwN70/dfYBj47jz9qbsMId/iRN3ewGhXQFZCSftd1LZCfmhktB"')
        sb.append(' crossorigin="anonymous" />'                                               + "\n")
        sb.append('    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-treeview/1.2.0/bootstrap-treeview.min.css"')
        sb.append(' rel="stylesheet" />'                                                      + "\n")
        sb.append('    <style>'                                                               + "\n")
        sb.append('.list-group-item > .badge {'                                               + "\n")
        sb.append('    float: right;'                                                         + "\n")
        sb.append('}'                                                                         + "\n")
        sb.append('    </style>'                                                              + "\n")
        sb.append('  </head>'                                                                 + "\n")
        sb.append('  <body>'                                                                  + "\n")
        sb.append('    <div class="container">'                                               + "\n")
        sb.append('      <h3>' + title + '</h3>'                                              + "\n")
        sb.append('      <div id="tree"></div>'                                               + "\n")
        sb.append('      <div id="footer"></div>'                                             + "\n")
        sb.append('      <div id="modal-windows">'                                            + "\n")
        sb.append('<!-- here reporoot.htmlFragmentsOfMaterialsAsModal() is inserted -->'  + "\n\n\n")
        //sb.append(         htmlFragments.toString()                                           + "\n\n\n")
        sb.append('<!-- end of reporoot.htmlFragmentsOfMaterialsAsModal() -->'                + "\n")
        sb.append('      </div>'                                                              + "\n")
        sb.append('    </div>'                                                                + "\n")
        sb.append('    <!-- SCRIPTS -->'                                                      + "\n")
        sb.append('    <script src="https://code.jquery.com/jquery-3.3.1.slim.min.js"'              )
        sb.append(' integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo"')
        sb.append(' crossorigin="anonymous"></script>'                                        + "\n")
        sb.append('    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js"')
        sb.append(' integrity="sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49"')
        sb.append(' crossorigin="anonymous"></script>'                                        + "\n")
        sb.append('    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/js/bootstrap.min.js"')
        sb.append(' integrity="sha384-smHYKdLADwkXOn1EmN1qk/HfnUcbVRZyYmZ4qpPea6sjB/pTJ0euyQp0Mk8ck+5T"')
        sb.append(' crossorigin="anonymous"></script>'                                        + "\n")
        sb.append('    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-treeview/1.2.0/bootstrap-treeview.min.js"')
        sb.append('></script>'                                                                + "\n")
        sb.append('    <script type="text/javascript"><!--'                                       + "\n")
        sb.append('''
function getTree() {
    var data = ''' /* + JsonOutput.prettyPrint(repoRoot.toBootstrapTreeviewData()) */ + ''';
    return data;
}
//
function modalize() {
    $('#tree a').each(function() {
        if ($(this).attr('href') && $(this).attr('href') != '#') {
            $(this).attr('data-toggle', 'modal');
            $(this).attr('data-target', $(this).attr('href'));
            $(this).attr('href', '#');
        }
    });
}
//
$('#tree').treeview({
    data: getTree(),
    enableLinks: true,
    levels: 1,
    multiSelect: false,
    showTags: true,
    onNodeSelected: function(event, data) {
        modalize();
    }
});
//
modalize();
''')
        sb.append('--></script>'                                                              + "\n")
        sb.append('  </body>'                                                                 + "\n")
        sb.append('</html>'                                                                   + "\n")

        //
        String html = sb.toString()
        writer.write(html)
        writer.flush()
        writer.close()

    }

}
