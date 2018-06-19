package com.kazurayam.carmina

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import groovy.xml.MarkupBuilder

class Indexer {

    static Logger logger_ = LoggerFactory.getLogger(Indexer.class)

    Indexer() {}

    /**
     * creates a HTML which displays a hierarchical tree structures of the Materials of the specified TSuiteResult.
     *
     * using Bootstrap-treeview (https://github.com/jonmiles/bootstrap-treeview)
     *
     * @param tSuiteResult
     * @param os
     * @throws IOException
     */
    void makeIndex2(TSuiteResult tSuiteResult, OutputStream os) throws IOException {
        def writer = new OutputStreamWriter(os, 'UTF-8')
        def builder = new MarkupBuilder(writer)
        builder.doubleQuotes = true
        builder.html {
            head {
                meta('http-equiv':'X-UA-Compatible', content:'IE=edge')
                title("Test Materials ${tSuiteResult.getTSuiteName().toString()}/${tSuiteResult.getTSuiteTimestamp().format()}")
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
                style('type':'text/css') {
                    mkp.comment("\n" + this.getResource('bootstrap-treeview/bootstrap-treeview.css'))
                }
            }
            body() {
                div('class':'container') {
                    h3('Test Materials')
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
                script('type':'text/javascript') {
                    mkp.comment("\n" + this.getResource('bootstrap-treeview/bootstrap-treeview.js'))
                }
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
    enableLinks: false,
    levels: 3,
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
     *
     * @param tSuiteResult
     * @param os
     * @throws IOException
     */
    static void makeIndex(TSuiteResult tSuiteResult, OutputStream os) throws IOException {
        def writer = new OutputStreamWriter(os, 'UTF-8')
        def builder = new MarkupBuilder(writer)
        builder.doubleQuotes = true
        builder.html {
            head {
                meta('http-equiv':'X-UA-Compatible', content:'IE=edge')
                title("Test Materials ${tSuiteResult.getTSuiteName().toString()}/${tSuiteResult.getTSuiteTimestamp().toString()}")
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
                        'rel':'stylesheet')

                /*
                style(type:'text/css') {
                    mkp.yieldUnescaped('''
                        <!--
                        body {
                            color      : #000000;
                            background : #E6DED9;
                            font-size  : 200%;
                        }
                        //-->
                    ''')
                }
                */
            }
            body() {
                mkp.comment('Place your content here')
                div('class':'container') {
                    h1('Katalon Studio Test Materials')
                    h3("Test Suite : ${tSuiteResult.getTSuiteName().toString()}/${tSuiteResult.getTSuiteTimestamp().format()}")
                    // Slideshow
                    div('id':'carousel0', 'class':'carousel slide', 'data-ride':'carousel') {
                        ol('class':'carousel-indicators') {
                            // TODO このTSuiteResultのなかにScreenshotが百個もあったらどうしよう?
                            List<Material> mwList = tSuiteResult.getMaterials()
                            // TODO 画像じゃないPDFやJSONやXMLファイルを除外したい
                            def count = 0
                            for (Material mw: mwList) {
                                if (count == 0) {
                                    li('data-target':'#carousel0',
                                            'data-slide-to':"${count}",
                                            'class': 'active')
                                } else {
                                    li('data-target':'#carousel0',
                                            'data-slide-to':"${count}")
                                }
                                count += 1
                            }
                        }
                    }
                    div('class':'carousel-inner') {
                        List<Material> mwList = tSuiteResult.getMaterials()
                        def count = 0
                        for (Material mw: mwList) {
                            if (count == 0) {
                                div('class': 'item active') {

                                }
                            }
                            count += 1
                        }
                    }
                    //
                    List<TCaseResult> tcResults = tSuiteResult.getTCaseResults()
                    for (TCaseResult tcResult : tcResults) {
                        div('class':'row') {
                            div('class':'col-sm-12') {
                                h4("Test Case name : ${tcResult.getTCaseName().toString()}")
                                List<TargetURL> targetURLs = tcResult.getTargetURLs()
                                for (TargetURL targetURL : targetURLs) {
                                    h5("URL : ${targetURL.getUrl().toExternalForm()}")
                                    List<Material> materials = targetURL.getMaterials()
                                    for (Material material : materials) {
                                        Path file = material.getMaterialFilePath()
                                        Path relative = tSuiteResult.getTSuiteTimestampDirectory().relativize(file).normalize()
                                        h6("src:${relative.toString()}")
                                        img(src:"${relative.toString().replace('\\','/').replace('%','%25')}",
                                                alt:"${targetURL.getUrl().toExternalForm()}",
                                                border:"0",
                                                width:"96%")
                                    }
                                }
                            }
                        }
                    }
                }
                mkp.comment('SCRIPTS')
                script('src':'https://code.jquery.com/jquery-1.12.4.min.js') {mkp.comment('')}
                script('src':'https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/js/bootstrap.min.js') {mkp.comment('')}
                /*
                 mkp.comment('''
                     mkp is required to call helper methods such as yieldUnescaped, yield, comment
                 ''')
                 mkp.yield('testing MarkupBuilder'); br()
                 a(href:'http://d.hatena.ne.jp/fumokmm/', 'No Programming, No Life'); br()
                 a(href:'http://d.hatena.ne.jp/fumokmm/20090131/1233428513', 'MarkupBuilderでHTML生成を試してみた'); br()
                 mkp.yield('↑entry'); br()
                 */

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
