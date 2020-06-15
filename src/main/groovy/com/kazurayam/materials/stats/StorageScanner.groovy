package com.kazurayam.materials.stats

import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

import javax.imageio.ImageIO

import org.apache.commons.lang3.time.StopWatch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.FileType
import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TCaseResult
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteResult
import com.kazurayam.materials.TSuiteResultId
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.imagedifference.ImageDifference

/**
 * 
 * @author kazurayam
 */
class StorageScanner {
    
    static Logger logger_ = LoggerFactory.getLogger(StorageScanner.class)
    
    private VisualTestingLogger vtLogger_ = null
    
    private MaterialStorage materialStorage_
    private Options options_

    private BufferedImageBuffer biBuffer_
    
    private ImageDeltaStats previousImageDeltaStats_
    
    
    public StorageScanner(MaterialStorage materialStorage) {
        this(materialStorage, new Options.Builder().build())
    }
    
    public StorageScanner(MaterialStorage materialStorage, Options options) {
        this.materialStorage_ = materialStorage
        // reflesh it.
        // this may heavy. i am not very sure if it is a good idea to call MaterialRepository.scan() here.
        this.materialStorage_.scan()
        //
        this.options_ = options

        // speed up ImageIO!
        ImageIO.setUseCache(false)
        //
        //println "#StorageScanner options_getPreviousImageDeltaStats()=\"${options_.getPreviousImageDeltaStats()}\""
        //println "#StorageScanner Options.NULL_PREVIOUS_IMAGE_DELTA_STATS=\"${Options.USERDIR}\""
        if ( ! options_.getPreviousImageDeltaStats().equals(Options.USERDIR) ) {
            /*
             * We will try to open the previos image-delta-stats.json file.
             * Even if failed to open, we will just ignore it and continue.
             */
            Path path = options_.getPreviousImageDeltaStats()
            try {
                previousImageDeltaStats_ = ImageDeltaStats.fromJsonFile(path)
                String msg = "Successfully loaded previousImageDeltaStats(${path.toString()})"
                //println msg
                logger_.info(msg)
            } catch (FileNotFoundException ex) {
                String msg = "File not found: previousImageDeltaStats(${path.toString()});" + 
                    " will ignore and continue"
                //println msg
                logger_.warn(msg)
                previousImageDeltaStats_ = null
            } catch (IOException ex) {
                String msg = "IOException for previousImageDeltaStats(${path.toString()});" +
                    " will ignore and continue"
                //println msg
                logger_.warn(msg)
                ex.printStackTrace()
                previousImageDeltaStats_ = null
            } catch (Exception ex) {
                String msg = "${ex.class.getName()} was raised for previousImageDeltaStats(${path.toString()});" +
                    " will ignore and continue"
                //println msg
                logger_.warn(msg)
                ex.printStackTrace()
                previousImageDeltaStats_ = null
            }
        } else {
            previousImageDeltaStats_ = null
        }
    }
    
    /**
     * 
     * @return Options object which is in use
     */
    Options getOptions() {
        return this.options_
    }
    
    /**
     * Caller can set a VisualTestingListener to hear from the StorageScanner
     * 
     * @param listener
     */
    void setVisualTestingLogger(VisualTestingLogger logger) {
        this.vtLogger_ = logger
    }
    
    /**
     * This will return
     * <PRE>
     * {
     *  "defaultCriteriaPercentage":5.0,
     *  "statsEntryList":[
     *      // a StatsEntry object of the TSuiteName specified
     *  ]
     * }
     * </PRE>
     *
     * @param materialStorage
     * @return a ImageDeltaStats object
     */
    ImageDeltaStats scan(TSuiteName tSuiteName) {
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()
        
        ImageDeltaStatsImpl.Builder builder = 
            new ImageDeltaStatsImpl.Builder().storageScannerOptions(options_)
        //
        if (materialStorage_.getTSuiteNameList().contains(tSuiteName)) {
            StatsEntry se = this.makeStatsEntry(tSuiteName)
            builder.addImageDeltaStatsEntry(se)
        } else {
            logger_.warn("No ${tSuiteName} is found in ${materialStorage_}")
        }
        ImageDeltaStats ids = builder.build()
        
        stopWatch.stop()
        String msg = "#scan took ${stopWatch.getTime(TimeUnit.MILLISECONDS)} milliseconds for ${tSuiteName}"
        logger_.debug(msg)
        return ids
    }
        
    /**
     * This will return
     * <PRE>
     *     {
     *          "TSuiteName": "47News_chronos_capture",
     *          "materialStatsList": [
     *              // list of MaterialStats objects like
     *              // 
     *          ]
     *     }
     * </PRE>
     * 
     * @param ms
     * @param tSuiteName
     * @return a StatsEntry object
     */
    StatsEntry makeStatsEntry(TSuiteName tSuiteName) {
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()
        StatsEntry statsEntry = new StatsEntry(tSuiteName)
        Set<Path> set = materialStorage_.getSetOfMaterialPathRelativeToTSuiteName(tSuiteName)

        for (Path path : set) {

            // initialize BufferedImageBuffer for this particular (tSuiteName, path) pair
            this.biBuffer_ = new BufferedImageBuffer()

            MaterialStats materialStats = this.makeMaterialStats(tSuiteName, path)
            statsEntry.addMaterialStats(materialStats)
        }

        stopWatch.stop()
        String msg = "#makeStatsEntry took ${stopWatch.getTime(TimeUnit.MILLISECONDS)} milliseconds for ${tSuiteName}"
        logger_.debug(msg)
        if (vtLogger_ != null) {
            vtLogger_.info(msg)
        }
        logger_.debug("#makeStatsEntry statsEntry=${statsEntry}")
        return statsEntry
    }

    
    /**
     * This will return
     * <PRE>
     *              {
     *                  "path: "main.TC_47News.visitSite/47NEWS_TOP.png",
     *                  "imageDeltaList": [
     *                      // list of ImageDelta objects
     *                      {
     *                          "path": "47news.visitSite/47reporters.png",
     *                          "degree": 9,
     *                          "sum": 49.339999999999996,
     *                          "mean": 5.482222222222222,
     *                          "variance": 2.2697232411502397,
     *                          "standardDeviation": 1.506560068882167,
     *                          "tDistribution": 1.8595480375401174,
     *                          "confidenceInterval": {
     *                              "lowerBound": 4.548381949046843,
     *                              "uppderBound": 6.416062495397601,
     *                              "confidenceLevel": 0.95
     *                          },
     *                          "criteriaPercentage": 7.42,
     *                          "data": [
     *                              33.99,
     *                              ...
     *                              1.95
     *                          ],
     *                          "imageDeltaList": [
     *                              {
     *                                  "a": "20190401_142150",
     *                                  "b": "20190401_141839",
     *                                  "d": 0.00,
     *                                  "cached": false
     *                              },
     *                              ...
     *                          ]
     *                      },
     *                  ],
     *              }
     * </PRE>
     * 
     * @param ms
     * @param pathRelativeToTSuiteTimestamp
     * @return
     */
    MaterialStats makeMaterialStats(TSuiteName tSuiteName,
                                Path pathRelativeToTSuiteTimestampDir) {
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()
        
        // At first, look up materials of FileType.PNG 
        //     within the TSuiteName across multiple TSuiteTimestamps
        // This list is sorted by descending order of TSuiteTimestamp
        List<Material> materials = this.getMaterialsOfARelativePathInATSuiteName(
                                        tSuiteName,
                                        pathRelativeToTSuiteTimestampDir)
        
        // build the MaterialStats object while calculating the diff ratio 
        // of two PNG files
        List<ImageDelta> imageDeltaList = new ArrayList<ImageDelta>()
        if (materials.size() > 1) {
            for (int i = 0;
                    i < materials.size() - 1 &&
                    i < options_.getMaximumNumberOfImageDeltas();
                    i++) {
            
                ImageDelta imageDelta
                //println "#makeMaterialStats previousImageDeltaStats_ is not null: ${previousImageDeltaStats_ != null}"
                if (previousImageDeltaStats_ != null) {
                    boolean condition = previousImageDeltaStats_.hasImageDelta(tSuiteName,
                                                    pathRelativeToTSuiteTimestampDir,
                                                    materials.get(i).getParent().getParent().getTSuiteTimestamp(),
                                                    materials.get(i + 1).getParent().getParent().getTSuiteTimestamp())
                    
                    //println "#makeMaterialStats previousImageDeltaStats_.hasImageDelta() returned ${condition}"
                    if (condition) {
                        imageDelta = previousImageDeltaStats_.getImageDelta(tSuiteName, 
                                                pathRelativeToTSuiteTimestampDir,
                                                materials.get(i).getParent().getParent().getTSuiteTimestamp(),
                                                materials.get(i + 1).getParent().getParent().getTSuiteTimestamp())
                        // turn this imageDelta marked isCached
                        imageDelta.setCached(true)
                    
                    } else {
                        //println "#makeMaterialStats tSuiteName=${tSuiteName}"
                        //println "#makeMaterialStats pathRelativeToTSuiteTimestamp=${pathRelativeToTSuiteTimestampDir}"
                        //println "#makeMaterialStats i=${i}"
                        //println "#makeMaterialStats materials.get(i)..TSuiteTimestamp=${materials.get(i).getParent().getParent().getTSuiteTimestamp()}"
                        //println "#makeMaterialStats materials.get(i+1)..TSuiteTimestamp=${materials.get(i+1).getParent().getParent().getTSuiteTimestamp()}"
                        //println ""
                        imageDelta = this.makeImageDelta(materials.get(i), materials.get(i + 1))
                    }
                } else {
                    // the following 1 line causes many ImageIO and significant amount of calcuration,
                    // will require many seconds of processing
                    imageDelta = this.makeImageDelta(materials.get(i), materials.get(i + 1))
                }
                //
                imageDeltaList.add(imageDelta)
            }
        }
        MaterialStats materialStats  = new MaterialStats(
                    pathRelativeToTSuiteTimestampDir, imageDeltaList)
        
        // configure parameters
        materialStats.setFilterDataLessThan(options_.getFilterDataLessThan())
        materialStats.setShiftCriteriaPercentageBy(options_.getShiftCriteriaPercentageBy())
        
        //
        stopWatch.stop()
        String msg = "#makeMaterialStats " + 
            "took ${stopWatch.getTime(TimeUnit.MILLISECONDS)} milliseconds for " +
            "${tSuiteName},${pathRelativeToTSuiteTimestampDir}"
        //logger_.debug(msg)
        if (vtLogger_ != null) {
            vtLogger_.info(msg)
        }
        return materialStats
    }

    /**
     *
     * @param tSuiteName
     * @param pathRelativeToTSuiteTimestamp
     * @return
     */
    List<Material> getMaterialsOfARelativePathInATSuiteName(
                                TSuiteName tSuiteName,
                                Path pathRelativeToTSuiteTimestamp) {
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()
        List<Material> materialList = new ArrayList<Material>()
        // construct a list of Materials
        List<TSuiteResultId> idsOfTSuiteName = materialStorage_.getTSuiteResultIdList(tSuiteName)
        for (TSuiteResultId tSuiteResultId : idsOfTSuiteName) {
            TSuiteResult tSuiteResult = materialStorage_.getTSuiteResult(tSuiteResultId)
            for (Material mate: tSuiteResult.getMaterialList()) {
                if (mate.fileType.equals(FileType.PNG) &&
                    mate.getPathRelativeToTSuiteTimestamp() == pathRelativeToTSuiteTimestamp) {
                    if (this.isInRangeOfTSuiteTimestamp(mate)) {
                        materialList.add(mate)
                    }
                }
            }
        }
        // sort the Material list by the descending order of TSuiteTimestamp
        Collections.sort(materialList, new Comparator<Material>() {
            public int compare(Material materialA, Material materialB) {
                TSuiteResult tsrA = materialA.getTCaseResult().getParent()
                TSuiteResult tsrB = materialB.getTCaseResult().getParent()
                if (tsrA > tsrB) {
                    return -1
                } else if (tsrA == tsrB) {
                    Path pathA = materialA.getPath()
                    Path pathB = materialB.getPath()
                    return pathA.compareTo(pathB)
                } else {
                    return 1
                }
            }
        })
        stopWatch.stop()
        String msg = "#getMaterialsOfARelativePathInATSuiteName " +
            "took ${stopWatch.getTime(TimeUnit.MILLISECONDS)} milliseconds for "
            "${tSuiteName},${pathRelativeToTSuiteTimestamp}"
        //logger_.debug(msg)
        //if (listener_ != null) {
        //    listener_.info(msg)
        //}
        return materialList
    }

    /**
     * Read 2 PNG files to get image difference, calculate the diff ratio,
     * and will return a ImageDelta object.
     * 
     * Please note that this method call takes fairly long processing time (2 to 4 seconds).
     * 
     * <PRE>
     *      { "a": "20190216_064354", "b": "20190216_064149", "delta": 0.10 }
     * </PRE>
     * 
     * @param a
     * @param b
     * @return a ImageDelta object
     */
    ImageDelta makeImageDelta(Material a, Material b) {
        StopWatch stopWatch = new StopWatch()
        stopWatch.start()
        Objects.requireNonNull(a, "Material a must not be null")
        Objects.requireNonNull(b, "Material b must not be null")
        if (a.getFileType() != FileType.PNG) {
            throw new IllegalArgumentException("${a.path()} is not a PNG file")
        }
        if (b.getFileType() != FileType.PNG) {
            throw new IllegalArgumentException("${b.path()} is not a PNG file")
        }
        //
        TSuiteTimestamp tSuiteTimestampA = a.getParent().getParent().getTSuiteTimestamp()
        TSuiteTimestamp tSuiteTimestampB = b.getParent().getParent().getTSuiteTimestamp()
        // read PNG files and
        // create ImageDifference of the 2 given images to calculate the diff ratio
        BufferedImage biA = biBuffer_.read(a)
        BufferedImage biB = biBuffer_.read(b)
        // Here we use our greatest magic!
        ImageDifference diff = new ImageDifference(biA, biB)

        // make the delta
        boolean cached = false
        ImageDelta imageDelta = new ImageDelta(tSuiteTimestampA, tSuiteTimestampB,
                                                diff.getRatio(), cached)
        biBuffer_.remove(a)    // a will be no longer used, b will be reused once again
        
        stopWatch.stop()
        String msg = "#makeImageDelta took ${stopWatch.getTime(TimeUnit.MILLISECONDS)} milliseconds for " + 
            "(${a.getPathRelativeToRepositoryRoot().toString()}, ${b.getPathRelativeToRepositoryRoot()})"
        //logger_.debug(msg)
        if (vtLogger_ != null) {
            vtLogger_.info(msg)
        }
        return imageDelta
    }
    
    /**
     * Check if the Material is newer than the TSuiteTimestamp specified by the onlySince property 
     * in the Options object passed to the StorageScanner constructor.
     * The boolean property onlySinceInclusive is respected.
     *  
     * @param material
     * @return
     */
    boolean isInRangeOfTSuiteTimestamp(Material material) {
        TSuiteTimestamp onlySince = options_.getOnlySince()
        boolean onlySinceInclusive = options_.getOnlySinceInclusive()
        if (onlySince != TSuiteTimestamp.NULL) {
            TCaseResult tcr = material.getParent()
            TSuiteResult tsr = tcr.getParent()
            TSuiteTimestamp tst = tsr.getTSuiteTimestamp()
            logger_.debug("#isInRangeOfTSuiteTimestamp onlySinceInclusive=${onlySinceInclusive}, tst=${tst.toString()}, onlySince=${onlySince.toString()}")
            if (onlySinceInclusive) {
                return tst >= onlySince    
            } else {
                return tst > onlySince
            }
        } else {
            // if onlySince is not specified, return true
            return true
        }
    }
    
    /**
     *
     */
    Path persist(ImageDeltaStats imageDeltaStats,
            TSuiteName tSuiteNameExam, TSuiteTimestamp tSuiteTimestampExam, TCaseName tCaseNameExam) {
        Path inStorage = materialStorage_.getBaseDir().
                                resolve(tSuiteNameExam.getValue()).
                                resolve(tSuiteTimestampExam.format()).
                                resolve(tCaseNameExam.getValue()).
                                resolve(ImageDeltaStats.IMAGE_DELTA_STATS_FILE_NAME)
        Files.createDirectories(inStorage.getParent())
        imageDeltaStats.write(inStorage)
        // important to call scan() of the MaterialStorage to let it be aware of the added file
        materialStorage_.scan()
        //
        return inStorage
    }

    /**
     *
     * @return
     */
    Path findLatestImageDeltaStats(TSuiteName tSuiteNameExam, TCaseName tCaseNameExam) {
        return StorageScanner.findLatestImageDeltaStats(materialStorage_, tSuiteNameExam, tCaseNameExam)
    }
    
    /**
     * 
     * @param materialStorage
     * @param tSuiteNameExam
     * @param tCaseNameExam
     * @return
     */
    static Path findLatestImageDeltaStats(MaterialStorage materialStorage, TSuiteName tSuiteNameExam, TCaseName tCaseNameExam) {
        Objects.requireNonNull(tSuiteNameExam, "tSuiteNameExam must not be null")
        Objects.requireNonNull(tCaseNameExam, "tCaseNameExam must not be null")
        List<TSuiteResultId> tSuiteResultIdList = materialStorage.getTSuiteResultIdList(tSuiteNameExam)
        List<TSuiteResult> tSuiteResultList = materialStorage.getTSuiteResultList(tSuiteResultIdList)
        Collections.sort(tSuiteResultList, new com.kazurayam.materials.TSuiteResult.TimestampFirstTSuiteResultComparator())
        // logger_.debug("#findLatestImageDeltaStats tSuiteNameExam=${tSuiteNameExam}")
        // logger_.debug("#findLatestImageDeltaStats tCaseNameExam=${tCaseNameExam}")
        // logger_.debug("#findLatestImageDeltaStats tSuiteResultIdList=${tSuiteResultIdList}")
        // logger_.debug("#findLatestImageDeltaStats tSuiteResultList=${tSuiteResultList}")
        if (tSuiteResultList.size() > 0) {
            // sort the list as required
            for (TSuiteResult tsr : tSuiteResultList) {
                TCaseResult tcr = tsr.getTCaseResult(tCaseNameExam)
                if (tcr != null) {
                    List<Material> materials = tcr.getMaterialList()
                    for (Material mate : materials) {
                        if (mate.getFileName().equals(ImageDeltaStats.IMAGE_DELTA_STATS_FILE_NAME)) {
                            return mate.getPath()
                        }
                    }
                }
            }
            return StorageScanner.Options.USERDIR
        } else {
            logger_.warn("No TSuiteName=${tSuiteNameExam} is found in ${materialStorage.toString()}")
            return StorageScanner.Options.USERDIR
        }
    }

    /**
     * This class maintains a buffer of BufferedImage to make I/O to PNG files efficient.
     */
    static class BufferedImageBuffer {
        private Map<Material, BufferedImage> buffer
        BufferedImageBuffer() {
            buffer = new HashMap<Material, BufferedImage>()
        }
        BufferedImage read(Material material) {
            if (!buffer.containsKey(material)) {
                BufferedImage bi = ImageIO.read(material.getPath().toFile())
                buffer.put(material, bi)
            }
            return buffer.get(material)
        }
        BufferedImage remove(Material material) {
            return buffer.remove(material)
        }
        int size() {
            return buffer.size()
        }
    }
    
    /**
     * Options to control the behavior of StroageScanner
     */
    static class Options {
        
        public static Path USERDIR = new File(System.getProperty('user.dir')).toPath()
        
        private double shiftCriteriaPercentageBy
        private double filterDataLessThan
        private double probability
        private int maximumNumberOfImageDeltas
        private TSuiteTimestamp onlySince
        private boolean onlySinceInclusive
        private Path previousImageDeltaStats
        private Path projectDirectory
        
        static class Builder {
            private double shiftCriteriaPercentageBy
            private double filterDataLessThan
            private double probability
            private int maximumNumberOfImageDeltas
            private TSuiteTimestamp onlySince
            private boolean onlySinceInclusive
            private Path previousImageDeltaStats
            private Path projectDirectory
            
            /*
             * constructor, where we set the default values
             */
            Builder() {
                this.shiftCriteriaPercentageBy = MaterialStats.SUGGESTED_SHIFT_CRITERIA_PERCENTAGE_BY
                this.filterDataLessThan = MaterialStats.DEFAULT_FILTER_DATA_LESS_THAN
                this.probability = MaterialStats.DEFAULT_PROBABILITY
                this.maximumNumberOfImageDeltas = MaterialStats.DEFAULT_MAXIMUM_NUMBER_OF_IMAGEDELTAS
                this.onlySince = new TSuiteTimestamp('19990101_000000')
                this.onlySinceInclusive = true
                this.previousImageDeltaStats = Options.USERDIR
                this.projectDirectory = null
            }
            
            Builder shiftCriteriaPercentageBy(double value) {
                if (value < 0.0) {
                    throw new IllegalArgumentException("shiftCriteriaPercentageBy must not be negative")
                }
                if (value > 100.0) {
                    throw new IllegalArgumentException("shiftCriteriaPercentageBy must not be > 100.0")
                }
                this.shiftCriteriaPercentageBy = value
                return this
            }
            
            Builder filterDataLessThan(double value) {
                if (value < 0.0) {
                    throw new IllegalArgumentException("filterDataLessThan must not be negative")
                }
                if (value > 100.0) {
                    throw new IllegalArgumentException("filterDataLessThan must not be  > 100.0")
                }
                this.filterDataLessThan = value
                return this
            }
            
            Builder probability(double value) {
                if (value < 0.0) {
                    throw new IllegalArgumentException("probability must not be negative")
                }
                if (value > 1.0) {
                    throw new IllegalArgumentException("probability must not be > 1.0")
                }
                this.probability = value
                return this
            }
            
            Builder maximumNumberOfImageDeltas(int value) {
                if (value < 1) {
                    throw new IllegalArgumentException("maximumNumberOfImageDeltas must not be less than 1")
                }
                this.maximumNumberOfImageDeltas = value
                return this
            }
            
            Builder onlySince(TSuiteTimestamp tSuiteTimestamp, boolean inclusive = true) {
                this.onlySince = tSuiteTimestamp
                this.onlySinceInclusive = inclusive
                return this
            }
            
            /**
             * 
             * @param path to image-delta-stats.json file in the Storage dir. 
             *        You can pass a path either absolute or relative to the project dir.
             * @return
             */
            Builder previousImageDeltaStats(Path path) {
                this.previousImageDeltaStats = path.toAbsolutePath().normalize()
                return this
            }
            
            Builder projectDirectory(Path path) {
                this.projectDirectory = path.toAbsolutePath().normalize()
                return this
            }
            
            Options build() {
                return new Options(this)
            }
        }
        
        private Options(Builder builder) {
            this.shiftCriteriaPercentageBy = builder.shiftCriteriaPercentageBy
            this.filterDataLessThan = builder.filterDataLessThan
            this.probability = builder.probability
            this.maximumNumberOfImageDeltas = builder.maximumNumberOfImageDeltas
            this.onlySince = builder.onlySince
            this.onlySinceInclusive = builder.onlySinceInclusive
            this.previousImageDeltaStats = builder.previousImageDeltaStats
            this.projectDirectory = builder.projectDirectory
        }
        
        double getShiftCriteriaPercentageBy() {
            return this.shiftCriteriaPercentageBy
        }
        
        double getFilterDataLessThan() {
            return this.filterDataLessThan
        }
        
        double getProbability() {
            return this.probability
        }
        
        int getMaximumNumberOfImageDeltas() {
            return this.maximumNumberOfImageDeltas
        }
        
        TSuiteTimestamp getOnlySince() {
            return this.onlySince
        }
        
        boolean getOnlySinceInclusive() {
            return this.onlySinceInclusive    
        }
        
        Path getPreviousImageDeltaStats() {
            return this.previousImageDeltaStats
        }
        
        Path getPreviousImageDeltaStatsRelativeToProjectDirectory() {
            if (this.projectDirectory != null) {
                logger_.debug("#getPreviousImageDeltaStatsRelativeToProjectDirectory this.projectDirectory=${this.projectDirectory}")
                logger_.debug("#getPreviousImageDeltaStatsRelativeToProjectDirectory this.previousImageDeltaStats=${this.previousImageDeltaStats}")
                return this.projectDirectory.relativize(this.previousImageDeltaStats).normalize()
            } else {
                return this.previousImageDeltaStats
            }       
        }
        
        Path getProjectDirectory() {
            return this.projectDirectory
        }
        
        @Override
        String toString() {
            return this.toJsonText()
        }
        
        String toJsonText() {
            StringBuilder sb = new StringBuilder()
            sb.append("{")
            //
            sb.append("\"shiftCriteriaPercentageBy\":")
            sb.append(this.getShiftCriteriaPercentageBy())
            sb.append(",")
            //
            sb.append("\"filterDataLessThan\":")
            sb.append(this.getFilterDataLessThan())
            sb.append(",")
            //
            sb.append("\"maximumNumberOfImageDeltas\":")
            sb.append(this.getMaximumNumberOfImageDeltas())
            sb.append(",")
            //
            sb.append("\"onlySince\":")
            sb.append("\"" + this.getOnlySince().format() + "\"")
            sb.append(",")
            //
            sb.append("\"onlySinceInclusive\":")
            sb.append(this.getOnlySinceInclusive())
            sb.append(",")
            //
            sb.append("\"probability\":")
            sb.append(this.getProbability())
            sb.append(",")
            //
            sb.append("\"projectDirectory\":")
            sb.append("\"")
            if (this.projectDirectory != null) {
                sb.append(Helpers.escapeAsJsonText(this.getProjectDirectory().toString()))
            } else {
                sb.append('')
            }
            sb.append("\",")
            //
            sb.append("\"previousImageDeltaStats\":")
            if (this.getPreviousImageDeltaStats() != null) {
                sb.append("\"")
                sb.append(Helpers.escapeAsJsonText(this.getPreviousImageDeltaStatsRelativeToProjectDirectory().toString()))
                sb.append("\"")
            } else {
                sb.append("\"\"")
            }
            //
            sb.append("}")
            sb.toString()
        }
    }
}
