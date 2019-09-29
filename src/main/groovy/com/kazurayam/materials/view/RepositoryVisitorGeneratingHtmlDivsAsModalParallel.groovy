package com.kazurayam.materials.view

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.VTLoggerEnabled
import com.kazurayam.materials.imagedifference.ComparisonResult
import com.kazurayam.materials.repository.RepositoryVisitor


import groovy.xml.MarkupBuilder

import java.nio.file.Path

class RepositoryVisitorGeneratingHtmlDivsAsModalParallel 
    extends RepositoryVisitorGeneratingHtmlDivsAsModalBase {
    
    static Logger logger_ = LoggerFactory.getLogger(
                            RepositoryVisitorGeneratingHtmlDivsAsModalParallel.class)

    String classShortName = Helpers.getClassShortName(
        RepositoryVisitorGeneratingHtmlDivsAsModalParallel.class)

    /**
     * Constructor
     * 
     * @param mkbuilder
     */
    RepositoryVisitorGeneratingHtmlDivsAsModalParallel(MarkupBuilder mkbuilder) {
        super(mkbuilder)
    }

    @Override String getBootstrapModalSize() {
        return 'modal-xl'
    }
    
    /**
     * generate HTML <div>s which presents 2 images (Back and Forth) in parallel format
     */
    @Override
    void generateImgTags(Material mate) {
        println "${this.getClass().getName()}#generateImgTags(${mate}) was invoked"
        if (this.comparisonResultBundle_ != null &&
            this.comparisonResultBundle_.containsImageDiff(mate.getPath())) {
            // This material is a diff image, so render it in Carousel format of Back > Diff > Forth
            ComparisonResult cr = comparisonResultBundle_.get(mate.getPath())
            Path repoRoot = mate.getParent().getParent().getParent().getBaseDir()
            mkbuilder_.div(['class':'container-fluid']) {
                mkbuilder_.div(['class':'row']) {
                    mkbuilder_.div(['class':'col']) {
                        mkbuilder_.p(['class':'text-right'],
                            "Expected: ${cr.getExpectedMaterial().getDescription() ?: ''}")
                        mkbuilder_.img(['src': "${cr.getExpectedMaterial().getEncodedHrefRelativeToRepositoryRoot()}",
                            'class': 'img-fluid d-block mx-auto',
                            'style': 'border: 1px solid #ddd',
                            'alt' : "Expected"])
                    }
                    mkbuilder_.div(['class':'col']) {
                        mkbuilder_.p(['class':'text-left'],
                            "Actual: ${cr.getActualMaterial().getDescription() ?: ''}")
                        mkbuilder_.img(['src': "${cr.getActualMaterial().getEncodedHrefRelativeToRepositoryRoot()}",
                            'class': 'img-fluid d-block mx-auto',
                            'style': 'border: 1px solid #ddd',
                            'alt' : "Actual"])
                    }
                }
            }
        } else {
            mkbuilder_.img(['src': mate.getEncodedHrefRelativeToRepositoryRoot(),
                'class':'img-fluid', 'style':'border: 1px solid #ddd', 'alt':'material'])
        }
    }

}
