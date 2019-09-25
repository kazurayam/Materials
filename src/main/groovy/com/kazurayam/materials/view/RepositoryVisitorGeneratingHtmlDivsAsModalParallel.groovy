package com.kazurayam.materials.view

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.imagedifference.ComparisonResult


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
            /*
            mkbuilder.div(['class':'carousel slide', 'data-ride':'carousel', 'id': "${mate.hashCode()}carousel"]) {
                mkbuilder.div(['class':'carousel-inner']) {
                    mkbuilder.div(['class':'carousel-item']) {
                        mkbuilder.div(['class':'carousel-caption d-none d-md-block']) {
                            mkbuilder.p "Back ${cr.getExpectedMaterial().getDescription() ?: ''}"
                        }
                        mkbuilder.img(['src': "${cr.getExpectedMaterial().getEncodedHrefRelativeToRepositoryRoot()}",
                                    'class': 'img-fluid d-block w-100',
                                    'style': 'border: 1px solid #ddd',
                                    'alt' : "Back"])
                    }
                    mkbuilder.div(['class':'carousel-item active']) {
                        mkbuilder.div(['class':'carousel-caption d-none d-md-block']) {
                            String eval = (cr.imagesAreSimilar()) ? "Images are similar." : "Images are different."
                            String rel = (cr.getDiffRatio() <= cr.getCriteriaPercentage()) ? '<=' : '>'
                            mkbuilder.p "${eval} diffRatio(${cr.getDiffRatio()}) ${rel} criteria(${cr.getCriteriaPercentage()})"
                        }
                        mkbuilder.img(['src': "${cr.getDiffMaterial().getEncodedHrefRelativeToRepositoryRoot()}",
                                    'class': 'img-fluid d-block w-100',
                                    'style': 'border: 1px solid #ddd',
                                    'alt' : "Diff"])
                    }
                    mkbuilder.div(['class':'carousel-item']) {
                        mkbuilder.div(['class':'carousel-caption d-none d-md-block']) {
                            mkbuilder.p "Forth ${cr.getActualMaterial().getDescription() ?: ''}"
                        }
                        mkbuilder.img(['src': "${cr.getActualMaterial().getEncodedHrefRelativeToRepositoryRoot()}",
                                    'class': 'img-fluid d-block w-100',
                                    'style': 'border: 1px solid #ddd',
                                    'alt' : "Forth"])
                    }
                    mkbuilder.a(['class':'carousel-control-prev',
                                'href':"#${mate.hashCode()}carousel",
                                'role':'button',
                                'data-slide':'prev']) {
                        mkbuilder.span(['class':'carousel-control-prev-icon',
                                        'area-hidden':'true'], '')
                        mkbuilder.span(['class':'sr-only'], 'Back')
                    }
                    mkbuilder.a(['class':'carousel-control-next',
                                'href':"#${mate.hashCode()}carousel",
                                'role':'button',
                                'data-slide':'next']) {
                        mkbuilder.span(['class':'carousel-control-next-icon',
                                        'area-hidden':'true'], '')
                        mkbuilder.span(['class':'sr-only'], 'Forth')
                    }
                }
            }
             */
        } else {
            mkbuilder_.img(['src': mate.getEncodedHrefRelativeToRepositoryRoot(),
                'class':'img-fluid', 'style':'border: 1px solid #ddd', 'alt':'material'])
        }
    }

    
}
