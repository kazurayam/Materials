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

    Path makeIndex(String testSuiteId, String testSuiteTimestamp) {
        return this.makeIndex(new TSuiteName(testSuiteId), new TSuiteTimestamp(testSuiteTimestamp))
    }

    Path makeIndex(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) throws IOException {
        RepositoryScanner scanner = new RepositoryScanner(baseDir_)
        scanner.scan()
        RepositoryRoot repoRoot = scanner.getRepositoryRoot()
        TSuiteResult tsr = repoRoot.getTSuiteResult(tSuiteName, tSuiteTimestamp)
        if (tsr != null) {
            Path index = tsr.getTSuiteTimestampDirectory().resolve('index.html')
            OutputStream os = index.toFile().newOutputStream()
            this.generate(tsr, os)
            logger_.info("generated ${index.toString()}")
            return index
        } else {
            logger_.error("${tSuiteName.toString()}/${tSuiteTimestamp.toString()} is not found in ${baseDir_.toString()}")
            return null
        }
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
    void generate(TSuiteResult tSuiteResult, OutputStream os) throws IOException {
        def writer = new OutputStreamWriter(os, 'UTF-8')
        def builder = new MarkupBuilder(writer)
        builder.doubleQuotes = true
        builder.html {
            head {
                meta('http-equiv':'X-UA-Compatible', content:'IE=edge')
                title("${tSuiteResult.getTSuiteName().toString()}/${tSuiteResult.getTSuiteTimestamp().format()}")
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
                    div('id':'tree')
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
    var data = ''' + JsonOutput.prettyPrint(tSuiteResult.toBootstrapTreeviewData()) + ''';
    return data;
}
$('#tree').treeview({
    data: getTree(),
    enableLinks: true,
    levels: 3,
    multiSelect: true,
    showTags: true
});
''')
                }
            }
        }
        writer.flush()
        writer.close()

    }

    /**
     * load javascript and css file as resource from the JVM classpath
     *
     * @param resourceName
     * @return
     */
    String getResource(String resourceName) {
        ClassLoader classLoader = getClass().getClassLoader()
        File file = new File(classLoader.getResource(resourceName).getFile())
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), 'UTF-8'))
        StringBuilder sb = new StringBuilder()
        try {
            for (String line; (line = br.readLine()) != null; ) {
                sb.append(line)
                sb.append('\n')
            }
        } catch (IOException ex) {
            logger_.error(ex.getMessage())
        }
        return sb.toString()
    }
}
