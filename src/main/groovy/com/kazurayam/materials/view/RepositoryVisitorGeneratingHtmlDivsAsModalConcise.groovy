package com.kazurayam.materials.view


import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.imagedifference.ComparisonResult
import com.kazurayam.materials.repository.RepositoryRoot

import groovy.xml.MarkupBuilder

import java.nio.file.Path

class RepositoryVisitorGeneratingHtmlDivsAsModalConcise
    extends RepositoryVisitorGeneratingHtmlDivsAsModalBase {

    static Logger logger_ = LoggerFactory.getLogger(
        RepositoryVisitorGeneratingHtmlDivsAsModalConcise.class)

    String classShortName = Helpers.getClassShortName(
        RepositoryVisitorGeneratingHtmlDivsAsModalConcise.class)

    /**
    * Constructor
    *
    * @param mkbuilder
    */
    RepositoryVisitorGeneratingHtmlDivsAsModalConcise(RepositoryRoot repoRoot, MarkupBuilder mkbuilder) {
        super(repoRoot, mkbuilder)
    }

    @Override String getBootstrapModalSize() {
        return 'modal-xl'
    }
    
    String getImgWidth() {
        return '502px'
    }

    /**
    * generate HTML <div>s which presents 2 images (Back and Forth) in parallel format
    */
    @Override
    void generateImgTags(Material mate) {
        println "${this.getClass().getName()}#generateImgTags(${mate}) was invoked"
        if (this.comparisonResultBundle_ != null &&
            this.comparisonResultBundle_.containsImageDiff(mate.getPath())) {
            // This material is a diff image, so render it in Carousel format of Diff > Expected + Actual
            ComparisonResult cr = comparisonResultBundle_.get(mate.getPath())
            Path repoRoot = mate.getParent().getParent().getParent().getBaseDir()
            mkbuilder_.div(['class':'carousel slide', 'data-ride':'carousel', 'id': "${mate.hashCode()}carousel"]) {
                mkbuilder_.div(['class':'carousel-inner']) {
                    // Diff
                    mkbuilder_.div(['class':'carousel-item active']) {
                        mkbuilder_.div(['class':'carousel-caption d-block']) {
                            String eval = (cr.imagesAreSimilar()) ? "Images are similar." : "Images are different."
                            String rel = (cr.getDiffRatio() <= cr.getCriteriaPercentage()) ? '<=' : '>'
                            String diag = "${eval} diffRatio(${cr.getDiffRatio()}) ${rel} criteria(${cr.getCriteriaPercentage()})"
                            if (cr.imagesAreSimilar()) {
                                mkbuilder_.p diag
                            } else {
                                mkbuilder_.p(['style':'color:red;'], diag)
                            }
                        }
                        mkbuilder_.img(['src': "${cr.getDiffMaterial().getEncodedHrefRelativeToRepositoryRoot()}",
                            'class': 'img-fluid d-block mx-auto',
                            'style': "border: 1px solid #ddd; width: ${this.getImgWidth()};",
                            'alt' : "Diff"])
                    }
                    // Expected + Actual
                    mkbuilder_.div(['class':'carousel-item']) {
                        mkbuilder_.div(['class':'carousel-caption d-block']) {

                            assert this.findExecutionProfileName(repoRoot_, cr.getExpectedMaterial()) != null
                            assert this.findExecutionProfileName(repoRoot_, cr.getActualMaterial()) != null

                            mkbuilder_.p "${this.findExecutionProfileName(repoRoot_, cr.getExpectedMaterial()) ?: 'profile?'}" +
                                        " ${this.findTestSuiteTimestamp(repoRoot_, cr.getExpectedMaterial()) ?: 'timestamp?'}" +
                                        " | " +
                                        " ${this.findExecutionProfileName(repoRoot_, cr.getActualMaterial()) ?: 'profile?'}" +
                                        " ${this.findTestSuiteTimestamp(repoRoot_, cr.getActualMaterial()) ?: 'timestamp?'}"
                        }
                        mkbuilder_.div(['class':'container-fluid']) {
                            mkbuilder_.div(['class':'row']) {
                                mkbuilder_.div(['class':'col']) {
                                    mkbuilder_.img(['src': "${cr.getExpectedMaterial().getEncodedHrefRelativeToRepositoryRoot()}",
                                        'class': 'img-fluid d-block mx-auto',
                                        'style': "border: 1px solid #ddd; width: ${this.getImgWidth();}",
                                        'alt' : "Expected"])
                                }
                                mkbuilder_.div(['class':'col']) {
                                    mkbuilder_.img(['src': "${cr.getActualMaterial().getEncodedHrefRelativeToRepositoryRoot()}",
                                        'class': 'img-fluid d-block mx-auto',
                                        'style': "border: 1px solid #ddd; width: ${this.getImgWidth()};",
                                        'alt' : "Actual"])
                                }
                            }
                        }
                        
                    }
                    mkbuilder_.a(['class':'carousel-control-prev',
                            'href':"#${mate.hashCode()}carousel",
                            'role':'button',
                            'data-slide':'prev']) {
                        mkbuilder_.span(['class':'carousel-control-prev-icon',
                            'area-hidden':'true'], '')
                        mkbuilder_.span(['class':'sr-only'], 'Back')
                    }
                    mkbuilder_.a(['class':'carousel-control-next',
                            'href':"#${mate.hashCode()}carousel",
                            'role':'button',
                            'data-slide':'next']) {
                        mkbuilder_.span(['class':'carousel-control-next-icon',
                            'area-hidden':'true'], '')
                        mkbuilder_.span(['class':'sr-only'], 'Forth')
                    }
                }
            }
        } else {
            mkbuilder_.img(['src': mate.getEncodedHrefRelativeToRepositoryRoot(),
                'class':'img-fluid', 'style':'border: 1px solid #ddd', 'alt':'material'])
        }
    }
}
