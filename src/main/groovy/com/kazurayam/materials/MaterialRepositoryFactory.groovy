package com.kazurayam.materials

import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.impl.MaterialRepositoryImpl

final class MaterialRepositoryFactory {

    static Logger logger_ = LoggerFactory.getLogger(MaterialRepositoryFactory.class)

    private MaterialRepositoryFactory() {}
    
    /**
     * creates the baseDir and the reportsDir, then instanciate a MaterialRepository object
     * 
     * @param baseDir
     * @param reportsDir
     * @return
     */
    static MaterialRepository createInstance(Path baseDir) {
        Objects.requireNonNull(baseDir, "baseDir must not be null")
        if ( !baseDir.normalize().toString().endsWith("Materials") &&
             !baseDir.normalize().toString().endsWith("Storage")     ) {
            throw new IllegalArgumentException(
                this.class.getSimpleName() + 
                "#createInstance baseDir(${baseDir}) is expected to have a dir name 'Materials' or 'Storage' but not")
        }
        Helpers.ensureDirs(baseDir)
        
        return (MaterialRepository)MaterialRepositoryImpl.newInstance(baseDir)
    }

}
