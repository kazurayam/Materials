package com.kazurayam.carmina.util

import geb.Page

class FIFAWorldCup2018JapanPage extends Page {

    static url = 'https://fixturedownload.com/results/fifa-world-cup-2018/japan'

    static at = {
        title == 'Download the Japan FIFA World Cup 2018 fixture as CSV, XLSX and ICS | Fixture Download'
    }
    
    static content = {
        anchorDownloadCsv { $('a', 0, text: ~/Download fixture as CSV/) }
        anchorDownloadXlsx { $('a', 0, text: ~/Download Fixture as XLSX/) }
    }

}
