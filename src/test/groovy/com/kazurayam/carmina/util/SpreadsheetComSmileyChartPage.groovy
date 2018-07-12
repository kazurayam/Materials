package com.kazurayam.carmina.util

import geb.Page

class SpreadsheetComSmileyChartPage extends Page {

    static url = 'http://spreadsheetpage.com/index.php/file/smiley_chart/'

    static at = {
        title == 'Excel Downloads From John Walkenbach: Smiley Chart'
    }

    static content = {
        anchorDownloadXls { $('a', 0, text: ~/smilechart.xls/) }
    }
}
