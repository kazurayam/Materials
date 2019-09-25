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

class RepositoryVisitorGeneratingHtmlDivsAsModalConcise
    extends RepositoryVisitorGeneratingHtmlDivsAsModalBase
    implements RepositoryVisitor, VTLoggerEnabled {

    static Logger logger_ = LoggerFactory.getLogger(
        RepositoryVisitorGeneratingHtmlDivsAsModalConcise.class)

    String classShortName = Helpers.getClassShortName(
        RepositoryVisitorGeneratingHtmlDivsAsModalConcise.class)

    String bootstrapModalSize = 'modal-xl'

    /**
    * Constructor
    *
    * @param mkbuilder
    */
    RepositoryVisitorGeneratingHtmlDivsAsModalConcise(MarkupBuilder mkbuilder) {
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
        if (this.comparisonResultBundle_ != null &&
            this.comparisonResultBundle_.containsImageDiff(mate.getPath())) {
            // This material is a diff image, so render it in Carousel format of Back > Diff > Forth
            ComparisonResult cr = comparisonResultBundle_.get(mate.getPath())
            Path repoRoot = mate.getParent().getParent().getParent().getBaseDir()
            mkbuilder_.div(['class':'container-fluid']) {
                mkbuilder_.div(['class':'row']) {
                    mkbuilder_.div(['class':'col']) {
                        mkbuilder_.p "Back ${cr.getExpectedMaterial().getDescription() ?: ''}"
                        mkbuilder_.img(['src': "${cr.getExpectedMaterial().getEncodedHrefRelativeToRepositoryRoot()}",
                            'class': 'img-fluid d-block w-100',
                            'style': 'border: 1px solid #ddd',
                            'alt' : "Back"])
                    }
                    mkbuilder_.div(['class':'col']) {
                        mkbuilder_.p "Forth ${cr.getActualMaterial().getDescription() ?: ''}"
                        mkbuilder_.img(['src': "${cr.getActualMaterial().getEncodedHrefRelativeToRepositoryRoot()}",
                            'class': 'img-fluid d-block w-100',
                            'style': 'border: 1px solid #ddd',
                            'alt' : "Forth"])
                    }
                }
            }
        } else {
            mkbuilder_.img(['src': mate.getEncodedHrefRelativeToRepositoryRoot(),
                'class':'img-fluid', 'style':'border: 1px solid #ddd', 'alt':'material'])
        }
    }
}
