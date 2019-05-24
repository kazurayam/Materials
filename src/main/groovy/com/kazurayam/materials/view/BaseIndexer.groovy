package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Indexer
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.impl.VisualTestingLoggerDefaultImpl
import com.kazurayam.materials.repository.RepositoryFileScanner
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.RepositoryWalker

import groovy.json.JsonOutput
import groovy.xml.MarkupBuilder

class BaseIndexer implements Indexer {
    
    static Logger logger_ = LoggerFactory.getLogger(BaseIndexer.class)
    private VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()
    
    private Path baseDir_
    private Path reportsDir_
    private Path output_
    
    @Override
    Path getOutput() {
        return this.output_
    }

    @Override
    void setBaseDir(Path baseDir) {
        if (baseDir == null) {
            def msg = "#setBaseDir baseDir argument is null"
            throw new IllegalArgumentException(msg)
        }
        if (Files.notExists(baseDir)) {
            def msg = "#setBaseDir basedir ${baseDir.toString()} does not exist"
            throw new IllegalArgumentException(msg)
        }
        this.baseDir_ = baseDir
    }
    
    @Override
    void setReportsDir(Path reportsDir) {
        if (reportsDir == null) {
            def msg = "#setReportsDir reportsDir argument is null"
            throw new IllegalArgumentException(msg)
        }
        if (Files.notExists(reportsDir)) {
            def msg = "#setReportsDir reportsDir ${reportsDir.toString()} does not exist"
            throw new IllegalArgumentException(msg)
        }
        this.reportsDir_ = reportsDir
    }
    
    @Override
    void setOutput(Path output) {
        Objects.requireNonNull(output)
        output_ = output
        Helpers.ensureDirs(output.getParent())
    }
    
    @Override
    void setVisualTestingLogger(VisualTestingLogger vtLogger) {
        this.vtLogger_ = vtLogger
    }
    
    @Override
    void execute() throws IOException {
        if (baseDir_ == null) {
            def msg = "#execute baseDir_ is null"
            throw new IllegalStateException(msg)
        }
        if (reportsDir_ == null) {
            def msg = "#execute reportsDir_ is null"
            throw new IllegalStateException(msg)
        }
        if (output_ == null) {
            def msg = "#execute output_ is null"
            throw new IllegalStateException(msg)
        }
        RepositoryFileScanner scanner = new RepositoryFileScanner(baseDir_, reportsDir_)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        Writer w = new OutputStreamWriter(new FileOutputStream(output_.toFile()), 'utf-8')
        MarkupBuilder mb = new MarkupBuilder(w)
        generate(repoRoot, mb)
        logger_.info("generated ${output_.toString()}")
    }
    

    /**
     * generate the Materials/index.html using Groovy's MarkupBuilder
     * 
     * @param repoRoot
     * @param mb
     */
    void generate(RepositoryRoot repoRoot, MarkupBuilder markupBuilder) {
        Objects.requireNonNull(repoRoot, "repoRoot must not be null")
        Objects.requireNonNull(markupBuilder, "markupBuilder must not be null")
        // title
        Path currDir = repoRoot.getBaseDir().getParent().getParent().
                                            normalize().toAbsolutePath()
        def titleStr = currDir.relativize(
                        repoRoot.getBaseDir().normalize().toAbsolutePath()).
                            toString()
        
        def generateHtmlDivsAsModal = { RepositoryRoot rp ->
            // generate HTML <div> tags as Modal window
            def htmlVisitor = new RepositoryVisitorGeneratingHtmlDivsAsModal(delegate)
            if (vtLogger_ != null) {
                htmlVisitor.setVisualTestingLogger(vtLogger_)
            }
            RepositoryWalker.walkRepository(repoRoot, htmlVisitor)
        }
        generateHtmlDivsAsModal.delegate = markupBuilder
        
        // closure which generates javascript code for utilizing Bootstrap Treeview
        def generateJsAsBootstrapTreeviewData = { RepositoryRoot rp ->
            // generate the data for Bootstrap Treeview
            StringWriter jsonSnippet = new StringWriter()
            def jsonVisitor = new RepositoryVisitorGeneratingBootstrapTreeviewData(jsonSnippet)
            if (vtLogger_ != null) {
                jsonVisitor.setVisualTestingLogger(vtLogger_)
            }
            RepositoryWalker.walkRepository(repoRoot, jsonVisitor)
            //
            delegate.script(['type':'text/javascript']) {
                delegate.mkp.yieldUnescaped('''
function getTree() {
    var data = ''' + JsonOutput.prettyPrint(jsonSnippet.toString()) + ''';
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
            }
        }
        generateJsAsBootstrapTreeviewData.delegate = markupBuilder  // important!
        
        // now drive the MarkeupBuilder
        markupBuilder.doubleQuotes = true   // use "value" rather than 'value'
        markupBuilder.html {
            head {
                meta(['http-equiv':'X-UA-Compatible', 'content': 'IE=edge'])
                title "${titleStr}"
                meta(['charset':'utf-8'])
                meta(['name':'descrition', 'content':''])
                meta(['name':'author', 'content':''])
                meta(['name':'viewport', 'content':'width=device-width, iniital-scale=1'])
                link(['rel':'stylesheet', 'href':''])
                mkp.comment(''' [if lt IE 9]
<script src="//cdn.jsdelivr.net/html5shiv/3.7.2/html5shiv.min.js"></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/respond.js/1.4.2/respond.min.js"></script>
<![endif] ''')
                link(['rel':'shortcut icon', 'href':''])
                link(['href':'https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css',
                    'rel':'stylesheet',
                    'integrity':'sha384-WskhaSGFgHYWDcbwN70/dfYBj47jz9qbsMId/iRN3ewGhXQFZCSftd1LZCfmhktB',
                    'crossorigin':'anonymous'
                    ])
                link(['href':'https://cdnjs.cloudflare.com/ajax/libs/bootstrap-treeview/1.2.0/bootstrap-treeview.min.css',
                    'rel':'stylesheet'
                    ])
                style {
                    mkp.yieldUnescaped('''
.list-group-item > .badge {
    float: right;
}
''')
                }
                // style for Carousel
                style {
                    mkp.yieldUnescaped('''
     .carousel-item img {
         margin-top: 30px;
     }
     .carousel-control-next, .carousel-control-prev {
         align-items: flex-start;
     }
     .carousel-control-next-icon, .carousel-control-prev-icon {
         background-color: #666;
     }
     .carousel-caption {
         position: absolute;
         top: -28px;
         bottom: initial;
         padding-top: 0px;
         padding-bottom: 0px;
     }
     .carousel-caption p {
         color: #999;
     }
''')
                }
            }
            body {
                div(['class':'container']) {
                    h3 "${titleStr}"
                    div(['id':'tree'], '')
                    div(['id':'footer'], '')
                    div(['id':'modal-windows']) {
                        generateHtmlDivsAsModal()
                    }
                }
                mkp.comment('SCRIPTS')
                script(['src':'https://code.jquery.com/jquery-3.3.1.slim.min.js',
                        'integrity':'sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo',
                        'crossorigin':'anonymous'], '')
                script(['src':'https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js',
                        'integrity':'sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49',
                        'crossorigin':'anonymous'], '')
                script(['src':'https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/js/bootstrap.min.js',
                        'integrity':'sha384-smHYKdLADwkXOn1EmN1qk/HfnUcbVRZyYmZ4qpPea6sjB/pTJ0euyQp0Mk8ck+5T',
                        'crossorigin':'anonymous'], '')
                script(['src':'https://cdnjs.cloudflare.com/ajax/libs/bootstrap-treeview/1.2.0/bootstrap-treeview.min.js'], '')
                //
                generateJsAsBootstrapTreeviewData(repoRoot)
            }
        }
    }
}
