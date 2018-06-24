package com.kazurayam.carmina.material

import java.nio.file.Files
import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import groovy.xml.MarkupBuilder

class Indexer {

    static Logger logger_ = LoggerFactory.getLogger(Indexer.class)

    private Path baseDir_

    Indexer(Path baseDir) {
        if (baseDir == null) {
            def msg = "baseDir argument is null"
            logger_.error(msg)
            throw new IllegalArgumentException(msg)
        }
        if (Files.notExists(baseDir)) {
            def msg = "basedir ${baseDir.toString()} does not exist"
            logger_.error(msg)
            throw new IllegalArgumentException(msg)
        }
        baseDir_ = baseDir
    }

    Path makeIndex() throws IOException {
        RepositoryScanner scanner = new RepositoryScanner(baseDir_)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        Path index = baseDir_.resolve('index.html')
        OutputStream os = index.toFile().newOutputStream()
        this.generate(repoRoot, os)
        logger_.info("generated ${index.toString()}")
        return index
    }

    /**
     * creates a HTML which displays a hierarchical tree structures of the Materials of the specified TSuiteResult.
     *
     * using Bootstrap-treeview (https://github.com/jonmiles/bootstrap-treeview)
     *
     * @param tSuiteResult
     * @param os
     * @throws IOException
     */
    void generate(RepositoryRoot repoRoot, OutputStream os) throws IOException {
        def writer = new OutputStreamWriter(os, 'UTF-8')
        def builder = new MarkupBuilder(writer)
        builder.doubleQuotes = true
        builder.html {
            head {
                meta('http-equiv':'X-UA-Compatible', content:'IE=edge')
                title("${repoRoot.getBaseDir().getFileName().toString()}")
                meta('charset':'utf-8')
                meta('name':'description', 'content':'')
                meta('name':'author', 'content':'')
                meta('name':'viewport', 'content':'width=device-width, initial-scale=1')
                link('rel':'stylesheet', 'href':'')
                mkp.comment('''[if lt IE 9]
<script src="//cdn.jsdelivr.net/html5shiv/3.7.2/html5shiv.min.js"></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/respond.js/1.4.2/respond.min.js"></script>
<![endif]''')
                link('rel':'shortcut icon', 'href':'')
                link('href':'https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css',
                        'rel':'stylesheet',
                        'integrity':'sha384-WskhaSGFgHYWDcbwN70/dfYBj47jz9qbsMId/iRN3ewGhXQFZCSftd1LZCfmhktB',
                        'crossorigin':'anonymous')
                link('href':'https://cdnjs.cloudflare.com/ajax/libs/bootstrap-treeview/1.2.0/bootstrap-treeview.min.css',
                    'rel':'stylesheet')
            }
            body() {
                div('class':'container') {
                    h3('Materials')
                    div('id':'tree') { mkp.yield('') }
                    div('id':'footer') { mkp.yield('') }
                    div('id': 'modal-windows') { mkp.yield('') }
                }

                mkp.comment('SCRIPTS')
                script('src':'https://code.jquery.com/jquery-3.3.1.slim.min.js',
                    'integrity':'sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo',
                    'crossorigin':'anonymous') {mkp.comment('')}
                script('src':'https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js',
                    'integrity':'sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49',
                    'crossorigin':'anonymous') {mkp.comment('')}
                script('src':'https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/js/bootstrap.min.js',
                    'integrity':'sha384-smHYKdLADwkXOn1EmN1qk/HfnUcbVRZyYmZ4qpPea6sjB/pTJ0euyQp0Mk8ck+5T',
                    'crossorigin':'anonymous') {mkp.comment('')}
                script('src': 'https://cdnjs.cloudflare.com/ajax/libs/bootstrap-treeview/1.2.0/bootstrap-treeview.min.js'
                    ) {mkp.comment('')}
                script('type':'text/javascript') {
                    mkp.comment(
'''
function getTree() {
    // Some logic to retrieve, or generate tree structure
    var data = ''' + JsonOutput.prettyPrint(repoRoot.toBootstrapTreeviewData()) + ''';
    return data;
}
$('#tree').treeview({
    data: getTree(),
    enableLinks: true,
    levels: 3,
    multiSelect: false,
    showTags: true
});
''')
                }
                script('type':'text/javascript') {
                    mkp.comment('''
// insert modal windows
$('#modal-windows').append($(` ''' + repoRoot.htmlFragmensOfMaterialsAsModal() + ''' `));

// modify attributes in the anchor in the treeview
$(function() {
    $('#tree a').attr('data-toggle','modal');
    $('#tree a').attr('data-target', $(this).attr('href'));
    //$('#tree a').attr('href','#');
});

//$(function() {
//    $('.pop').on('click', function() {
//        $('.imagepreview').attr('src', $(this).find('img').attr('src'));
//        $('#imagemodal').modal('show');   
//    });

'''
                    )
                }
            }
        }
        writer.flush()
        writer.close()

    }
}
