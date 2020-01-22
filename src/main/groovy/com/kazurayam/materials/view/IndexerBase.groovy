package com.kazurayam.materials.view

import java.nio.file.Files
import java.nio.file.Path

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers
import com.kazurayam.materials.Indexer
import com.kazurayam.materials.ReportsAccessor
import com.kazurayam.materials.ReportsAccessorFactory
import com.kazurayam.materials.VTLoggerEnabled
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
abstract class IndexerBase implements Indexer {
	
	protected static Logger logger_ = LoggerFactory.getLogger(IndexerBase.class)
	protected VisualTestingLogger vtLogger_ = new VisualTestingLoggerDefaultImpl()
	
	private Path baseDir_
	private Path reportsDir_
	private Path output_
    
    private RepositoryRoot repoRoot_
	
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
		repoRoot_ = scanner.getRepositoryRoot()
		//
		ReportsAccessor reportsAccessor = ReportsAccessorFactory.createInstance(reportsDir_)
		//
		Writer w = new OutputStreamWriter(
			new FileOutputStream(output_.toFile()), 'utf-8')
		MarkupBuilder mb = new MarkupBuilder(w)
		//
		generate(repoRoot_, reportsAccessor, mb)
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
		Objects.requireNonNull(repositoryRoot, "repositoryRoot must not be null")
		Objects.requireNonNull(reportsAccessor, "reportsAccessor must not be null")
		Objects.requireNonNull(markupBuilder, "markupBuilder must not be null")
		
		// HTML title
		Path currDir = repositoryRoot.getBaseDir().getParent().getParent().normalize().toAbsolutePath()
		String titleStr = currDir.relativize(repositoryRoot.getBaseDir().normalize().toAbsolutePath()).toString()
		
		def closureHD = htmlDivsGenerator(repositoryRoot, reportsAccessor)
		closureHD.delegate = markupBuilder
		
		def closureJSTD = jsAsBootstrapTreeviewDataGenerator(repositoryRoot, reportsAccessor)
		closureJSTD.delegate = markupBuilder
		
		def closureJSMEH = jsAsModalEventHandlerGenerator(repositoryRoot, reportsAccessor)
		closureJSMEH.delegate = markupBuilder
		
		// now we drive the MarkupBuilder to generate HTML
		markupBuilder.doubleQuotes = true	// use "value" rather than 'value'
		markupBuilder.html {
			head {
				meta(['http-equiv':'X-UA-Compatible', 'content': 'IE=edge'])
				title "${titleStr}"
				meta(['charset':'utf-8'])
				meta(['name':'descrition', 'content':''])
				meta(['name':'author', 'content':''])
				meta(['name':'viewport', 'content':'width=device-width, initial-scale=1, shrink-to-fit=no'])
				link(['rel':'stylesheet', 'href':''])
				mkp.comment(''' [if lt IE 9]
<script src="//cdn.jsdelivr.net/html5shiv/3.7.2/html5shiv.min.js"></script>
<script src="//cdnjs.cloudflare.com/ajax/libs/respond.js/1.4.2/respond.min.js"></script>
<![endif] ''')
				link(['rel':'shortcut icon', 'href':''])
				
				/* Bootstrap 4.1.1 -> 4.3.1
				link(['href':'https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css',
					'rel':'stylesheet',
					'integrity':'sha384-WskhaSGFgHYWDcbwN70/dfYBj47jz9qbsMId/iRN3ewGhXQFZCSftd1LZCfmhktB',
					'crossorigin':'anonymous'
					])
				 */
				link(['rel':'stylesheet',
					'href':'https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css',
					'integrity':'sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T',
					'crossorigin':'anonymous'
					])
				
				link(['href':'https://cdnjs.cloudflare.com/ajax/libs/bootstrap-treeview/1.2.0/bootstrap-treeview.min.css',
					'rel':'stylesheet'
					])
				style {
					mkp.yieldUnescaped('''
.list-group-item > .badge {
    float: right;
}
''')
				}
				// style for Carousel
				style {
					mkp.yieldUnescaped('''
     .carousel-item img {
         margin-top: 30px;
     }
     .carousel-control-next, .carousel-control-prev {
         align-items: flex-start;
     }
     .carousel-control-next-icon, .carousel-control-prev-icon {
         background-color: #666;
     }
     .carousel-caption {
         position: absolute;
         top: 0px;
         bottom: initial;
         padding-top: 0px;
         padding-bottom: 0px;
     }
     .carousel-caption p {
         color: #999;
     }
''')
				}
			}
			body {
				div(['class':'container']) {
					h3 "${titleStr}"
					div(['id':'tree'], '')
					div(['id':'footer'], '')
					div(['id':'modal-windows']) {
						
						// generate <div> elements as Modal for each materials
						closureHD.call()
						
					}
				}
				mkp.comment('SCRIPTS')
                
				script(['src':'https://code.jquery.com/jquery-3.3.1.slim.min.js',
						'integrity':'sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo',
						'crossorigin':'anonymous'], '')
				script(['src':'https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js',
						'integrity':'sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49',
						'crossorigin':'anonymous'], '')
				script(['src':'https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/js/bootstrap.min.js',
						'integrity':'sha384-smHYKdLADwkXOn1EmN1qk/HfnUcbVRZyYmZ4qpPea6sjB/pTJ0euyQp0Mk8ck+5T',
						'crossorigin':'anonymous'], '')
				
				/* once tried to use the js of Bootstrap 4.3.1 but it did not worked. 
				 * When I clciked <a> to open a Modal, the Modal did not show up.
				 * The js of 4.1.1 worked. Strange! 
				script(['src':'https://code.jquery.com/jquery-3.3.1.slim.min.js',
						'integrity':'sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo',
						'crossorigin':'anonymous'], '')
				script(['src':'https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.7/umd/popper.min.js',
						'integrity':'sha384-UO2eT0CpHqdSJQ6hJty5KVphtPhzWj9WO1clHTMGa3JDZwrnQq4sF86dIHNDz0W1',
						'crossorigin':'anonymous'], '')
				script(['src':'https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js',
						'integrity':'sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM',
						'crossorigin':'anonymous'], '')
				*/
				
				script(['src':'https://cdnjs.cloudflare.com/ajax/libs/bootstrap-treeview/1.2.0/bootstrap-treeview.min.js'], '')
				
				// generate <script type="text/javascript"> that 
				// drives Bootstrap Treeview 
				closureJSTD.call()
				
                // generate <script type="text/javascript"> that
                // registers event handlers for Bootstrap4 Modal on shown events
                /* FIXME
                closureJSMEH.call()
                */
			}
		}
	}
	
	/**
	 * returns a Closure that generates HTML <div> elements as Bootstrap Modal for each Materials.
	 * Classes that extend IndexerBase are supposed to implement its details by overriding
	 * createRepositoryVisitorGeneratigHtmlDivs(RepositoryRoot, MarkupBuilder) method.
	 * 
	 * @param mb
	 * @return a Groovy closure
	 */
	private def htmlDivsGenerator(
					RepositoryRoot repoRoot,
					ReportsAccessor reportsAccessor) {
		{ ->
			RepositoryVisitor visitor = 
					createRepositoryVisitorGeneratingHtmlDivs(delegate)
			visitor.setReportsAccessor(reportsAccessor)
			visitor.setVisualTestingLogger(vtLogger_)
			RepositoryWalker.walkRepository(repoRoot, visitor)
		}
	}
	
	/**
	 * This method must be overridden by child classes
	 * 
	 * @param mb
	 * @return
	 */
	abstract protected RepositoryVisitor createRepositoryVisitorGeneratingHtmlDivs(RepositoryRoot repoRoot, MarkupBuilder mb)
	
	
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
			VTLoggerEnabled visitor =
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
			VTLoggerEnabled visitor = new RepositoryVisitorGeneratingModalEventHandler(sw)
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
