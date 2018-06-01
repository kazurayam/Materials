package com.kazurayam.ksbackyard.screenshotsupport

import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors

class TargetPage {

    private TestCaseResult parentTestCaseResult
    private URL url
    private List<ScreenshotWrapper> screenshotWrappers

    // ---------------------- constructors & initializers ---------------------
    protected TargetPage(TestCaseResult parent, URL url) {
        this.parentTestCaseResult = parent
        this.url = url
        this.screenshotWrappers = new ArrayList<ScreenshotWrapper>()
    }

    // --------------------- properties getter & setter -----------------------
    TestCaseResult getParentTestCaseResult() {
        return this.parentTestCaseResult
    }

    URL getUrl() {
        return this.url
    }

    // --------------------- create/add/get child nodes -----------------------

    /**
     * This is the core trick.
     *
     * (1) scan the Test Case result directory for image files.
     * (2) if an image file which corresponds to the targetPageUrl found existing,
     *     then generate another unique Path. Finally return a ScreenshotWrapper of the Path.
     * (3)
     *
     * @param targetPageUrl
     * @return
     */
    ScreenshotWrapper uniqueScreenshotWrapper() {
        String encodedUrl = URLEncoder.encode(url.toExternalForm(), 'UTF-8')
        Path testCaseDir = getParentTestCaseResult().getTestCaseDir()

        System.out.println("testCaseDir=${testCaseDir}")

        List<Path> imageFilePaths = Files.list(testCaseDir)
                                            //.filter({ p -> Files.isRegularFile(p) })
                                            //.filter({ p -> p.getFileName().toString().endsWith('.png') })
                                            .collect(Collectors.toList())
        List<Map<String, String>> existingFileNames = new ArrayList<Map<String, String>>()
        // for example, parsedFileNames can be
        // [['encodedUrl':'http%3A%2F%2Fdemoaut.katalon.com%2F'],
        //  ['encodedUrl':'http%3A%2F%2Fdemoaut.katalon.com%2F', 'seq':'1'],
        //  ['encodedUrl':'http%3A%2F%2Fdemoaut.katalon.com%2F', 'seq':'3'] ]
        for (Path imageFilePath : imageFilePaths) {
            List<String> parsedFileName = parseScreenshotFileName(imageFilePath.getFileName().toString())
            if (parsedFileName.size() > 0) {
                if (encodedUrl == parsedFileName[0]) {  // excluded image files of other URLs
                    Map<String, String> entry = new HashMap<String, String>()
                    entry.put('encodedUrl', parsedFileName[0])
                    if (parsedFileName.size()> 1) {
                        entry.put('seq', parsedFileName[1])
                    }
                    existingFileNames.add(entry)
                }
            }
        }
        System.out.println("existingFileNames : ${existingFileNames}")

        // もしもexistingFileNamesが空っぽだったら
        //      http%3A%2F%2Fdemoaut.katalon.com%2F.png
        // を候補として決定してScreenshotWrapperを生成して返す。
        if (existingFileNames.size() == 0) {
            Path imageFilePath = testCaseDir.resolve("${encodedUrl}.png")
            return new ScreenshotWrapper(this, imageFilePath)
        }

        // 下記のファイル名の列を順番に生成しそれがexistingFileNamesと重複しているかどうかを試す。
        // もし重複していなければそのファイル名を使ってScreeshotWrapperを生成して返す。
        // 1から999まで試してまだ重複していたら（そんなことはありそうもないが）例外を投げておしまいにする。
        for (int i = 0; i < 999; i++) {
            String seqStr = String.valueOf(i)
            // もしもparseFileNamesの内容が
            // [['encodedUrl':'http%3A%2F%2Fdemoaut.katalon.com%2F'],
            //  ['encodedUrl':'http%3A%2F%2Fdemoaut.katalon.com%2F', 'seq':'1'],
            //  ['encodedUrl':'http%3A%2F%2Fdemoaut.katalon.com%2F', 'seq':'3'] ]
            // であったなら
            //  ['encodedUrl':'http%3A%2F%2Fdemoaut.katalon.com%2F', 'seq':'2'] ]
            // を候補として決定してScreenshotWrapperを生成して返す。
            boolean duplicating = false
            for (Map m : existingFileNames) {
                if (m.get('seq') == seqStr) {
                    duplicating = true
                }
            }
            if (duplicating == false) {
                Path imageFilePath = testCaseDir.resolve("${encodedUrl}.{seqStr}.png")
                return new ScreenshotWrapper(this, imageFilePath)
            }
        }
        throw new IllegalStateException("unable to generate a unique ScreenshotWrapper for ${this.url}")
    }

    ScreenshotWrapper findOrNewScreenshotWrapper(Path imageFilePath) {
        ScreenshotWrapper sw = this.getScreenshotWrapper(imageFilePath)
        if (sw == null) {
            sw = new ScreenshotWrapper(this, imageFilePath)
        }
        return sw
    }

    void addScreenshotWrapper(ScreenshotWrapper screenshotWrapper) {
        boolean found = false
        for (ScreenshotWrapper sw : this.screenshotWrappers) {
            if (sw == screenshotWrapper) {
                found = true
            }
        }
        if (!found) {
            this.screenshotWrappers.add(screenshotWrapper)
        }
    }

    ScreenshotWrapper getScreenshotWrapper(Path imageFilePath) {
        for (ScreenshotWrapper sw : this.screenshotWrappers) {
            if (sw.getScreenshotFilePath() == imageFilePath) {
                return sw
            }
        }
        return null
    }

    // --------------------- helpers ------------------------------------------

    /**
     * accept a string in a format (<any string>[/\])(<enocoded URL string>)(.[0-9]+)?(.png)
     * and returns a List<String> of ['<decoded URL>', '[1-9][0-9]*'] or ['<decoded URL>']
     * @param screenshotFileName
     * @return empty List<String> if unmatched
     */
    static final int flag = Pattern.CASE_INSENSITIVE
    static final String EXTENSION_PART_REGEX = '(\\.([0-9]+))?\\.png$'
    static final Pattern EXTENSION_PART_PATTERN = Pattern.compile(EXTENSION_PART_REGEX, flag)
    static List<String> parseScreenshotFileName(String screenshotFileName) {
        List<String> values = new ArrayList<String>()
        String preprocessed = screenshotFileName.replaceAll('\\\\', '/')  // Windows File path separator -> UNIX
        List<String> elements = preprocessed.split('[/]')
        if (elements.size() > 0) {
            String fileName = elements.getAt(elements.size() - 1)
            Matcher m = EXTENSION_PART_PATTERN.matcher(fileName)
            boolean b = m.find()
            if (b) {
                String encodedUrl = fileName.replaceFirst(EXTENSION_PART_REGEX, '')
                String decodedUrl = URLDecoder.decode(encodedUrl, 'UTF-8')
                values.add(decodedUrl)
                if (m.group(2) != null) {
                    values.add(m.group(2))
                }
            }
        }
        return values
    }

    // ------------------------ equals, hashCode ------------------------------
    @Override
    boolean equals(Object obj) {
        if (this == obj) { return true }
        if (!(obj instanceof TargetPage)) { return false }
        TargetPage other = (TargetPage)obj
        if (this.parentTestCaseResult == other.getParentTestCaseResult()
            && this.url == other.getUrl()) {
            return true
        } else {
            return false
        }
    }

    @Override
    int hashCode() {
        final int prime = 31
        int result = 1
        result = prime * result + this.getParentTestCaseResult().hashCode()
        result = prime * result + this.getUrl().hashCode()
        return result
    }
}