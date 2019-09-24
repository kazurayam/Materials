package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Indexer
import com.kazurayam.materials.ReportsAccessor
import com.kazurayam.materials.ReportsAccessorFactory
import com.kazurayam.materials.VisualTestingLogger
import com.kazurayam.materials.impl.VisualTestingLoggerDefaultImpl
import com.kazurayam.materials.repository.RepositoryFileScanner
import com.kazurayam.materials.repository.RepositoryRoot
import com.kazurayam.materials.repository.RepositoryVisitor
import com.kazurayam.materials.repository.RepositoryWalker

import groovy.json.JsonOutput
import groovy.xml.MarkupBuilder

/**
 * AbstractIndexer is an abstract class that implements the Indexer interface.
 * AbstractIndexer implements shared properites and methods called by the extending classes.
 * AbstractIndexer is extended by CarouselIndexer and ParallelIndexer.
 * 
 * @author kazurayam
 */
abstract class AbstractIndexer implements Indexer {
	
	protected static Logger logger_ = LoggerFactory.getLogger(AbstractIndexer.class)
	protected VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()
	
	private Path baseDir_
	private Path reportsDir_
	private Path output_
	
	// implementing com.kazurayam.materials.Indexer interface
	@Override
	void execute() throws IOException {
		Objects.requireNonNull(baseDir_, "baseDir_ must not be null")
		Objects.requireNonNull(reportsDir_, "reportsDir_ must not be null")
		Objects.requireNonNull(output_, "output_ must not be null")
		vtLogger_.info(this.class.getSimpleName() + "#execute baseDir is ${baseDir_}")
		vtLogger_.info(this.class.getSimpleName() + "#execute reportsDir is ${reportsDir_}")
		vtLogger_.info(this.class.getSimpleName() + "#execute output is ${output_}")
		//
		RepositoryFileScanner scanner = new RepositoryFileScanner(baseDir_)
		scanner.scan()
		RepositoryRoot repoRoot = scanner.getRepositoryRoot()
		//
		ReportsAccessor reportsAccessor = ReportsAccessorFactory.createInstance(reportsDir_)
		//
		Writer w = new OutputStreamWriter(
			new FileOutputStream(output_.toFile()), 'utf-8')
		MarkupBuilder mb = new MarkupBuilder(w)
		//
		generate(repoRoot, reportsAccessor, mb)
		logger_.info("generated ${output_.toString()}")
	}
	
	@Override
	Path getOutput() {
		return this.output_
	}
	
	@Override
	void setBaseDir(Path baseDir) {
		Objects.requireNonNull(baseDir, "baseDir must not be null")
		if (Files.notExists(baseDir)) {
			throw new IllegalArgumentException(
				"#setBaseDir baseDir ${baseDir} does not exist")
		}
		this.baseDir_ = baseDir
	}
	
	@Override
	void setOutput(Path output) {
		Objects.requireNonNull(output, "output must not be null")
		this.output_ = output
		Helpers.ensureDirs(output.getParent())
	}
	
	@Override
	void setReportsDir(Path reportsDir) {
		Objects.requireNonNull(reportsDir, "reportsDir must not be null")
		if (Files.notExists(reportsDir)) {
			throw new IllegalArgumentException(
				"#setReportsDir reportsDir ${reportsDir} does not exist")
		}
		this.reportsDir_ = reportsDir
	}
	
	@Override
	void setVisualTestingLogger(VisualTestingLogger vtLogger) {
		Objects.requireNonNull(vtLogger, "vtLogger must not be null")
		this.vtLogger_ = vtLogger
	}
	
	/**
	 * generate a HTML file as "index.html" using Groovy's MarkupBuilder
	 * 
	 * @param repoRoot
	 * @param reportsAccessor
	 * @param markupBuilder
	 */
	private void generate(
					RepositoryRoot repositoryRoot,
					ReportsAccessor reportsAccessor,
					MarkupBuilder markupBuilder) {
			
		def closureHD = htmlDivsGenerator(repositoryRoot, reportsAccessor)
		closureHD.delegate = markupBuilder
		
		//closureHD.call()
		
		def closureJSTD = jsAsBootstrapTreeviewDataGenerator(repositoryRoot, reportsAccessor)
		closureJSTD.delegate = markupBuilder
		
		//closureJSTD.call()
		
		def closureJSMEH = jsAsModalEventHandlerGenerator(repositoryRoot, reportsAccessor)
		closureJSMEH.delegate = markupBuilder
		
		
	}
	
	/**
	 * returns a Closure that generates HTML <div> elements as Bootstrap Modal
	 * for each Materials. Child classes are supposed to implement detail by
	 * overriding the createRepositoryVisitorGenerationgHtmlDivs(MarkupBuilder) method.
	 * 
	 * @param mb
	 * @return a Groovy closure
	 */
	private def htmlDivsGenerator(
					RepositoryRoot repositoryRoot,
					ReportsAccessor reportsAccessor) {
		{ ->
			RepositoryVisitorExtended visitor = 
					createRepositoryVisitorGeneratingHtmlDivs(delegate)
			visitor.setReportsAccessor(reportsAccessor)
			visitor.setVisualTestingLogger(vtLogger_)
			RepositoryWalker.walkRepository(repositoryRoot, visitor)
		}
	}
	
	/**
	 * This method must be overridden by child classes
	 * 
	 * @param mb
	 * @return
	 */
	abstract protected RepositoryVisitorExtended createRepositoryVisitorGeneratingHtmlDivs(MarkupBuilder mb)
	
	
	/**
	 * returns a closure that generates JavaScript code for utilizing Bootstrap Treeview
	 * @param repositoryRoot
	 * @param reportsAccessor
	 * @return
	 */
	private def jsAsBootstrapTreeviewDataGenerator(
					RepositoryRoot repositoryRoot,
					ReportsAccessor reportsAccessor) {
		{ ->
			StringWriter jsonSnippet = new StringWriter()
			RepositoryVisitorExtended visitor =
				new RepositoryVisitorGeneratingBootstrapTreeviewData(jsonSnippet)
			visitor.setReportsAccessor(reportsAccessor)
			visitor.setVisualTestingLogger(vtLogger_)
			RepositoryWalker.walkRepository(repositoryRoot, visitor)
			// here we generate JavaScript code using Groovy MarkupBuilder
			delegate.script(['type':'text/javascript']) {
				delegate.mkp.yieldUnescaped('''
function getTree() {
    var data = ''' + JsonOutput.prettyPrint(jsonSnippet.toString()) + ''';
	return data;
}
//
function modalize() {
	$('#tree a').each(function() {
		if ($(this).attr('href') && $(this).attr('href') != '#') {
			$(this).attr('data-toggle', 'modal');
			$(this).attr('data-target', $(this).attr('href'));
			$(this).attr('href', '#');
		}
	});
}
//
$('#tree').treeview({
	data: getTree(),
	enableLinks: true,
	levels: 1,
	multiSelect: false,
	showTags: true,
	onNodeSelected: function(event, data) {
		modalize();
	}
});
//
modalize();
			''')
			} // end of delegate.script()
		}
	}
	
	
	/**
	 * returns a closure that generates JavaScript code that handles 
	 * Bootstrap Modal Events
	 * 
	 * @param repositoryRoot
	 * @param reportsAccessor
	 * @return
	 */
	private def jsAsModalEventHandlerGenerator(
					RepositoryRoot repositoryRoot,
					ReportsAccessor reportsAccessor) {
		{ ->
			StringWriter sw = new StringWriter()
			RepositoryVisitorExtended visitor = new RepositoryVisitorGeneratingModalEventHandler(sw)
			visitor.setReportsAccessor(reportsAccessor)
			visitor.setVisualTestingLogger(vtLogger_)
			RepositoryWalker.walkRepository(repositoryRoot, visitor)
			//
			delegate.script(['type':'text/javascript'],
				//$("#-46441868").on('shown.bs.modal', function (e) {
				//    $(this).find("div.modal-body").scrollTop(4087);
				//});
				// ...
				sw.toString());
		}
	}
		
}
