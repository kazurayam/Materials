package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Indexer
import com.kazurayam.materials.repository.RepositoryFileScanner
import com.kazurayam.materials.repository.RepositoryRoot

import groovy.xml.MarkupBuilder

class CarouselIndexer implements Indexer {
    
    static Logger logger_ = LoggerFactory.getLogger(CarouselIndexer.class)
    
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
            logger_.error(msg)
            throw new IllegalArgumentException(msg)
        }
        if (Files.notExists(baseDir)) {
            def msg = "#setBaseDir basedir ${baseDir.toString()} does not exist"
            logger_.error(msg)
            throw new IllegalArgumentException(msg)
        }
        this.baseDir_ = baseDir
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
        this.reportsDir_ = reportsDir
    }
    
    @Override
    void setOutput(Path output) {
        Objects.requireNonNull(output)
        output_ = output
        Helpers.ensureDirs(output.getParent())
    }
    
    @Override
    void execute() throws IOException {
        if (baseDir_ == null) {
            def msg = "#execute baseDir_ is null"
            logger_.error(msg)
            throw new IllegalStateException(msg)
        }
        if (reportsDir_ == null) {
            def msg = "#execute reportsDir_ is null"
            logger_.error(msg)
            throw new IllegalStateException(msg)
        }
        if (output_ == null) {
            def msg = "#execute output_ is null"
            logger_.error(msg)
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
    
    void generate(RepositoryRoot repoRoot, MarkupBuilder mb) {
        Objects.requireNonNull(repoRoot, "repoRoot must not be null")
        Objects.requireNonNull(mb, "mb must not be null")
        Path currDir = repoRoot.getBaseDir().getParent().getParent().normalize().toAbsolutePath()
        mb.html {
            head {
                meta(['http-equiv':'X-UA-Compatible', 'content': 'IE=edge'])
                title currDir.relativize(
                        repoRoot.getBaseDir().normalize().toAbsolutePath()).toString()
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
                style '''
.list-group-item > .badge {
    float: right;
}
'''
            }
            body {
                p 'Hey this is a simple HTML paragraph created with a Groovy Builder!'
            }
        }
    }
}
