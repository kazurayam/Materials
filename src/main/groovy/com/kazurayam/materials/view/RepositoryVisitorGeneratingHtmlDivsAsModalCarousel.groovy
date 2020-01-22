package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.ReportsAccessor
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.VTLoggerEnabled
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.imagedifference.ComparisonResult
import com.kazurayam.materials.imagedifference.ComparisonResultBundle
import com.kazurayam.materials.impl.VisualTestingLoggerDefaultImpl
import com.kazurayam.materials.metadata.MaterialMetadata
import com.kazurayam.materials.metadata.MaterialMetadataBundle
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.RepositoryVisitResult
import com.kazurayam.materials.repository.RepositoryVisitor

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder
import groovy.xml.XmlUtil

/**
 * @author kazurayam
 */
class RepositoryVisitorGeneratingHtmlDivsAsModalCarousel
    extends RepositoryVisitorGeneratingHtmlDivsAsModalBase {
                       
    protected static Logger logger_ = LoggerFactory.getLogger(
                            RepositoryVisitorGeneratingHtmlDivsAsModalCarousel.class)
    
    protected String classShortName_ = Helpers.getClassShortName(
                            RepositoryVisitorGeneratingHtmlDivsAsModalCarousel.class)

    /**
     * Constructor
     * 
     * @param mkbuilder
     */
    RepositoryVisitorGeneratingHtmlDivsAsModalCarousel(RepositoryRoot repoRoot, MarkupBuilder mkbuilder) {
        super(repoRoot, mkbuilder)
    }
    
    @Override String getBootstrapModalSize() {
        return 'modal-lg'
    }
    
    /**
     * generate HTML <div>s which presents 3 images in Carousel format
     */
    @Override
    void generateImgTags(Material mate) {
        if (this.comparisonResultBundle_ != null &&
            this.comparisonResultBundle_.containsImageDiff(mate.getPath())) {
            // This material is a diff image, so render it in Carousel format of Back > Diff > Forth
            ComparisonResult cr = comparisonResultBundle_.get(mate.getPath())
            Path repoRoot = mate.getParent().getParent().getParent().getBaseDir()
            mkbuilder_.div(['class':'carousel slide', 'data-ride':'carousel', 'id': "${mate.hashCode()}carousel"]) {
                mkbuilder_.div(['class':'carousel-inner']) {
					// Back
                    mkbuilder_.div(['class':'carousel-item']) {
                        mkbuilder_.div(['class':'carousel-caption d-block']) {
                            mkbuilder_.p "Back ${cr.getExpectedMaterial().getDescription() ?: ''}"
                        }
                        mkbuilder_.img(['src': "${cr.getExpectedMaterial().getEncodedHrefRelativeToRepositoryRoot()}",
                                    'class': 'img-fluid d-block mx-auto',
                                    'style': 'border: 1px solid #ddd',
                                    'alt' : "Back"])
                        
                    }
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
                                    'style': 'border: 1px solid #ddd',
                                    'alt' : "Diff"])
                        
                    }
					// Forth
                    mkbuilder_.div(['class':'carousel-item']) {
                        mkbuilder_.div(['class':'carousel-caption d-block']) {
                            mkbuilder_.p "Forth ${cr.getActualMaterial().getDescription() ?: ''}"
                        }
                        mkbuilder_.img(['src': "${cr.getActualMaterial().getEncodedHrefRelativeToRepositoryRoot()}",
                                    'class': 'img-fluid d-block mx-auto',
                                    'style': 'border: 1px solid #ddd',
                                    'alt' : "Forth"])
                        
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
