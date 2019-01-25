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
import com.kazurayam.materials.model.storage.GroupBy
import com.kazurayam.materials.model.storage.SelectBy

class MaterialStorageImpl implements MaterialStorage {
    
    static Logger logger_ = LoggerFactory.getLogger(MaterialStorageImpl.class)
    
    private Path baseDir_
    private MaterialRepository externalRepos_
    
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
        
        externalRepos_ = MaterialRepositoryFactory.createInstance(baseDir_)
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
    int backup(MaterialRepository fromMR, TSuiteName tSuiteName,
        TSuiteTimestamp tSuiteTimestamp) throws IOException {
        //
        externalRepos_.putCurrentTestSuite(tSuiteName, tSuiteTimestamp)
        //
        List<Material> sourceList = fromMR.getMaterials(tSuiteName, tSuiteTimestamp)
        int count = 0
        for (Material sourceMate : sourceList) {
            TCaseName tcn = sourceMate.getTCaseName()
            Path subpath = sourceMate.getSubpath()
            String fileName = sourceMate.getFileName()
            Path copyTo
            if (subpath != null) {
                copyTo = externalRepos_.resolveMaterialPath(tcn, subpath, fileName)
            } else {
                copyTo = externalRepos_.resolveMaterialPath(tcn, fileName)
            }
            CopyOption[] options = [ StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES ]
            Files.copy(sourceMate.getPath(), copyTo, options)
            count += 1
        }
        return count
    }
    
    int backup(MaterialRepository fromMR, TSuiteName tSuiteName,
        SelectBy selectBy) throws IOException {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }
    
    int restore(MaterialRepository intoMR, TSuiteName tSuiteName,
        TSuiteTimestamp tSuiteTimestamp) throws IOException {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }
    
    int restore(MaterialRepository intoMR, TSuiteName tSuiteName,
        SelectBy selectBy) throws IOException {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }
    
    int empty() throws IOException {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }
    
    int expire(TSuiteName tSuiteName,
        TSuiteTimestamp tSuiteTimestamp) throws IOException {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }
    
    int expire(TSuiteName tSuiteName,
        GroupBy groupBy) throws IOException {
        throw new UnsupportedOperationException("TO BE IMPLEMENTED")
    }
    
}
