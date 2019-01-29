package com.kazurayam.materials.model

import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Material
import com.kazurayam.materials.MaterialRepository
import com.kazurayam.materials.MaterialRepositoryFactory
import com.kazurayam.materials.MaterialStorage
import com.kazurayam.materials.TCaseName
import com.kazurayam.materials.TSuiteName
import com.kazurayam.materials.TSuiteTimestamp
import com.kazurayam.materials.SelectBy
import com.kazurayam.materials.model.repository.RepositoryRoot

class MaterialStorageImpl implements MaterialStorage {
    
    static Logger logger_ = LoggerFactory.getLogger(MaterialStorageImpl.class)
    
    private Path baseDir_
    private MaterialRepository componentMR_
    
    /**
     * constructor is hidden
     * 
     * @param baseDir
     */
    private MaterialStorageImpl(Path baseDir) {
        Objects.requireNonNull(baseDir)
        //
        if (!baseDir.toFile().exists()) {
            throw new IllegalArgumentException("${baseDir} does not exist")
        }
        baseDir_ = baseDir
        // create the directory if not present
        Helpers.ensureDirs(baseDir_)
        //
        componentMR_ = MaterialRepositoryFactory.createInstance(baseDir_)
    }
    
    /**
     * create a new instance of MaterialStorage at the baseDir
     * 
     * @param baseDir
     * @return a MaterialStorage object
     */
    static MaterialStorage newInstance(Path baseDir) {
        return new MaterialStorageImpl(baseDir)
    }
    
    /**
     * copy Material files belonging to the tSuiteName + tSuiteTimestamp 
     * from the Materials dir of the project into the external Storage directory
     */
    @Override
    int backup(MaterialRepository fromMR, TSuiteName tSuiteName,
        TSuiteTimestamp tSuiteTimestamp) throws IOException {
        //
        componentMR_.putCurrentTestSuite(tSuiteName, tSuiteTimestamp)
        //
        List<Material> sourceList = fromMR.getMaterials(tSuiteName, tSuiteTimestamp)
        int count = 0
        for (Material sourceMate : sourceList) {
            TCaseName tcn = sourceMate.getTCaseName()
            Path subpath = sourceMate.getSubpath()
            String fileName = sourceMate.getFileName()
            Path copyTo
            if (subpath != null) {
                copyTo = componentMR_.resolveMaterialPath(tcn, subpath, fileName)
            } else {
                copyTo = componentMR_.resolveMaterialPath(tcn, fileName)
            }
            CopyOption[] options = [ StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES ]
            Files.copy(sourceMate.getPath(), copyTo, options)
            count += 1
        }
        // scan the directories/files to update the internal status of componentMR
        componentMR_.scan()
        // done
        return count
    }
    
    @Override
    int backup(MaterialRepository fromMR, TSuiteName tSuiteName,
        SelectBy selectBy) throws IOException {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }
    
    @Override
    int clear(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) throws IOException {
        int count = componentMR_.clear(tSuiteName, tSuiteTimestamp)
        return count
    }

    @Override
    int clear(TSuiteName tSuiteName) throws IOException {
        int count = componentMR_.clear(tSuiteName)
        return count
    }


    @Override
    void empty() throws IOException {
        componentMR_.deleteBaseDirContents()
    }
    
    @Override
    int expire(TSuiteName tSuiteName,
        TSuiteTimestamp tSuiteTimestamp) throws IOException {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }
    
    @Override
    int expire(TSuiteName tSuiteName,
        SelectBy selectBy) throws IOException {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }
    
    @Override
    RepositoryRoot getRepositoryRoot() {
        return this.componentMR_.getRepositoryRoot()    
    }
    
    /**
     * 
     * @return
     */
    @Override
    List<Material> getMaterials() {
        return componentMR_.getMaterials()
    }
    
    /**
     * 
     */
    @Override
    List<Material> getMaterials(TSuiteName tSuiteName, TSuiteTimestamp tSuiteTimestamp) {
       return componentMR_.getMaterials(tSuiteName, tSuiteTimestamp) 
    }
    
    /**
     *
     */
    int restore(MaterialRepository intoMR, TSuiteName tSuiteName,
        TSuiteTimestamp tSuiteTimestamp) throws IOException {
        //
        intoMR.putCurrentTestSuite(tSuiteName, tSuiteTimestamp)
        //
        List<Material> sourceList = componentMR_.getMaterials(tSuiteName, tSuiteTimestamp)
        logger_.info("sourceList.size()=${sourceList.size()}")
        int count = 0
        for (Material sourceMate : sourceList) {
            TCaseName tcn = sourceMate.getTCaseName()
            Path subpath = sourceMate.getSubpath()
            String fileName = sourceMate.getFileName()
            Path copyTo
            if (subpath != null) {
                copyTo = intoMR.resolveMaterialPath(tcn, subpath, fileName)
            } else {
                copyTo = intoMR.resolveMaterialPath(tcn, fileName)
            }
            CopyOption[] options = [ StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES ]
            Files.copy(sourceMate.getPath(), copyTo, options)
            count += 1
        }
        // Is it ok to do this? not sure.
        intoMR.scan()
        // done
        return count
    }
    
    int restore(MaterialRepository intoMR, TSuiteName tSuiteName,
        SelectBy selectBy) throws IOException {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }

    // ---------------------- overriding Object properties --------------------
    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append('{"MaterialStorageImpl":{')
        sb.append('"baseDir":"' +
            Helpers.escapeAsJsonText(baseDir_.toString()) + '"')
        sb.append('}}')
        return sb.toString()
    }    
}
