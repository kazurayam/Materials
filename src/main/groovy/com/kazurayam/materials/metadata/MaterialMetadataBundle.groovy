package com.kazurayam.materials.metadata

import com.kazurayam.materials.Helpers

import java.nio.file.Files
import java.nio.file.Path

import com.kazurayam.materials.MaterialDescription

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class MaterialMetadataBundle {

    static Logger logger_ = LoggerFactory.getLogger(MaterialMetadataBundle.class)
    
    static final String SERIALIZED_FILE_NAME    = 'material-metadata-bundle.json'
    static final String URLS_MARKDOWN_FILE_NAME = 'visited-urls.md'
    static final String URLS_TSV_FILE_NAME      = 'visited-urls.tsv'

    static final String TOP_PROPERTY_NAME = 'MaterialMetadataBundle'
    
    private static List<MaterialMetadata> materialMetadataList_
    
    MaterialMetadataBundle() {
        materialMetadataList_ = new ArrayList<MaterialMetadata>()
    }

	/**
	 * 
	 * @param jsonPath
	 * @return may return null if jsonPath does not exists
	 */
    static MaterialMetadataBundle deserialize(Path jsonPath){
		if (Files.exists(jsonPath)) {
			return deserialize(jsonPath.toFile().text)
		} else {
			return null
		}
    }
    
    static MaterialMetadataBundle deserialize(String jsonText) {
		def jsonObject = new JsonSlurper().parseText(jsonText)
        if (jsonObject[TOP_PROPERTY_NAME]) {
            return deserialize((Map)jsonObject)
        } else {
            throw new IllegalArgumentException("No \'${TOP_PROPERTY_NAME}\' found in ${jsonText}")
        }
    }
    
    static MaterialMetadataBundle deserialize(Map jsonObject) {
        MaterialMetadataBundle instance = new MaterialMetadataBundle()
        def bundle = jsonObject[TOP_PROPERTY_NAME]
        if (bundle == null) {
            throw new IllegalArgumentException("No \'${TOP_PROPERTY_NAME}\' found in ${jsonObject}")
        }
        for (def metadataJsonObj : bundle) {
            MaterialMetadata metadata = MaterialMetadataImpl.deserialize((Map)metadataJsonObj)
            instance.add(metadata)
        }
        return instance
    }

    /**
     *
     * @param writer
     */
    void serialize(Writer writer) {
        writer.print(JsonOutput.prettyPrint(this.toJsonText()))
        writer.flush()
    }

    /**
     * Compile a report with the list of URL - material path in Markdown format.
     * The report is designed for documentation purpose.
     *
     * @param writer
     */
    void serializeAsMarkdown(Writer writer) {
        List<String> categories = this.findCategories(this.materialMetadataList_)
        PrintWriter pw = new PrintWriter(writer)
        for (String category in categories.toSorted()) {
            pw.println("### ${category}")
            pw.println("| description | URL | material path |")
            pw.println("|:---|:---|:---|")
			for (MaterialMetadata mm in this.materialMetadataList_.toSorted()) {
                if (mm.getMaterialDescription().getCategory() == category) {
                    StringBuilder sb = new StringBuilder()
                    sb.append("| ")
                    sb.append(mm.getMaterialDescription().getDescription())
                    sb.append(" | ")
                    sb.append(mm.getUrl() ?: '')
                    sb.append(" | ")
                    sb.append(mm.getMaterialPath())
                    sb.append(" |")
                    pw.println(sb.toString())
                }
            }
            pw.println("")
        }
        pw.flush()
    }

    /**
     * Compile a report with the list of URL - material path in TAB-Seperated Value format.
     * The report is designed for copy&pasting into E-mail for sharing info.
     *
     * @param writer
     */
    void serializeAsTSV(Writer writer) {
        List<String> categories = this.findCategories(this.materialMetadataList_)
        PrintWriter pw = new PrintWriter(writer)
        for (String category in categories.sort()) {
            pw.println("### ${category}")
            for (MaterialMetadata mm in this.materialMetadataList_.toSorted()) {
                if (mm.getMaterialDescription().getCategory() == category) {
                    StringBuilder sb = new StringBuilder()
                    sb.append(mm.getMaterialDescription().getDescription())
                    sb.append("\t")
                    sb.append(mm.getUrl() ?: '')
                    sb.append("\t")
                    sb.append(mm.getMaterialPath())
                    sb.append("\t")
                    pw.println(sb.toString())
                }
            }
            pw.println("")
        }
        pw.flush()
    }

    /**
     * Look into the MaterialMetadataBundle to find the Categories
     * in MaterialDescriptions recorded in each MaterialMetadata.
     *
     * @param bundle
     * @return
     */
    private List<String> findCategories(List<MaterialMetadata> bundle) {
        Set<String> categories = new HashSet<String>()
        for (MaterialMetadata mm in bundle) {
            categories.add(mm.getMaterialDescription().getCategory())
        }
        return categories.toList().sort()
    }

    void add(MaterialMetadata pathResolutionLog) {
        this.materialMetadataList_.add(pathResolutionLog)
    }
    
    int size() {
        return this.materialMetadataList_.size()
    }
    
    MaterialMetadata get(int index) {
        return this.materialMetadataList_.get(index)
    }
    
    List<MaterialMetadata> findByMaterialPath(String materialPath) {
        List<MaterialMetadata> list = new ArrayList<MaterialMetadata>()
        for (MaterialMetadata entry : materialMetadataList_) {
            if (entry.getMaterialPath() == materialPath) {
                list.add(entry)
            }
        }
        return list
    }
    
    /**
     * 
     * @param materialPath
     * @return
     */
    MaterialMetadata findLastByMaterialPath(String materialPath) {
        List<MaterialMetadata> list = this.findByMaterialPath(materialPath)
        if (list.size() > 0) {
            Collections.sort(list)
            list.get(list.size() - 1)
        } else {
            return null
        }
    }
    
    String toJsonText() {
        StringBuilder sb = new StringBuilder()
        sb.append('{')
        sb.append("\"")
        sb.append(Helpers.escapeAsJsonText(TOP_PROPERTY_NAME))
        sb.append("\": [")
        int count = 0
        for (MaterialMetadata resolution: this.materialMetadataList_) {
            if (count > 0) {
                sb.append(',')
            }
            count += 1
            sb.append(resolution.toJsonText())
        }
        sb.append(']')
        sb.append('}')
        return sb.toString()
    }
    
    @Override
    String toString() {
        return this.toJsonText()
    }


}
