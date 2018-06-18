package com.kazurayam.carmina

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.xml.MarkupBuilder

class Reporter {

    static Logger logger_ = LoggerFactory.getLogger(Reporter.class)

    private Reporter() {}

    /**
     *
     * @param tSuiteResult
     * @param os
     * @throws IOException
     */
    static void report(TSuiteResult tSuiteResult, OutputStream os) throws IOException {
        def writer = new OutputStreamWriter(os, 'UTF-8')
        def builder = new MarkupBuilder(writer)
        builder.doubleQuotes = true
        builder.html {
            head {
                meta('http-equiv':'X-UA-Compatible', content:'IE=edge')
                title("Test Results ${tSuiteResult.getTSuiteName().toString()}/${tSuiteResult.getTSuiteTimestamp().toString()}")
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
                    h1('Katalon Studio Test Results')
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
                                h4("Test Case status : ${tcResult.getTestCaseStatus()}")
                                List<TargetURL> targetURLs = tcResult.getTargetURLs()
                                for (TargetURL targetURL : targetURLs) {
                                    h5("URL : ${targetURL.getUrl().toExternalForm()}")
                                    List<Material> materials = targetURL.getMaterials()
                                    logger_.info("#report materials=${materials.toString()}")
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
}
