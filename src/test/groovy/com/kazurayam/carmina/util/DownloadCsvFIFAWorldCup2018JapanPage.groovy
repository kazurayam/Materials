package com.kazurayam.carmina.util

import geb.Page

class DownloadCsvFIFAWorldCup2018JapanPage extends Page {

    static url = 'https://fixturedownload.com/download/csv/fifa-world-cup-2018/japan'

    static at = {
        title == 'Your download | Fixture Download'
    }

    static content = {
        yourDownload { $('h1', 0, text: ~/Your download/) }
        backToList { $('a', 0, text: ~/Back to List/) }
    }

}
