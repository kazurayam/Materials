/**
 * This is the Geb configuration file
 */
import java.nio.file.Path
import java.nio.file.Paths

import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.FirefoxProfile
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.remote.DesiredCapabilities

import com.kazurayam.materials.FileType

waiting {
    presets {
        slow {
            timeout = 15
            retryInterval = 1
        }
        quick {
            timeout = 1
        }
    }
}

waiting {
    timeout = 60
    retryInterval = 0.5
}

atCheckWaiting = true

environments {

    // run via "./gradlew chromeTest"
    chrome {
        //driver = { new ChromeDriver() }
        driver = {
            Map<String, Object> chromePreferences = new HashMap<>()
            // Below two preference settings will disable popup dialog when download file
            chromePreferences.put('profile.default_content_settings.popups', 0)
            chromePreferences.put('download.prompt_for_download', false)
            // set directory to save files
            Path downloads = Paths.get(System.getProperty('user.home'), 'Downloads')
            chromePreferences.put('download.default_directory', downloads.toString())
            // disable flash and pdf viewer
            chromePreferences.put('plugins.plugins_disabled',
                ['Adobe Flash Player', 'Chrome PDF Viewer'])
            //
            ChromeOptions chromeOptions = new ChromeOptions()
            chromeOptions.setExperimentalOption('prefs', chromePreferences)
            //
            DesiredCapabilities cap = DesiredCapabilities.chrome()
            cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true)
            cap.setCapability(ChromeOptions.CAPABILITY, chromeOptions)
            new ChromeDriver(cap)
        }
    }

    // run via "./gradlew chromeHeadlessTest"
    chromeHeadless {
        driver = {
            ChromeOptions o = new ChromeOptions()
            o.addArguments('headless')
            new ChromeDriver(o)
        }
    }

    // run via "./gradlew firefoxTest"
    firefox {
        driver = {
            /**
             * see https://stackoverflow.com/questions/36309314/set-firefox-profile-to-download-files-automatically-using-selenium-and-java
             *
             * see https://developer.mozilla.org/en-US/docs/Archive/Mozilla/Download_Manager_preferences
             * - browser.download.useDownloadDir : A boolean value that indicates whether or not the user's preference is to automatically save files into the download directory. If this value is false, the user is asked what to do. In Thunderbird and SeaMonkey the default is false. In Other Applications the default is true.
             * - browser.download.folderList : Indicates the default folder to download a file to. 0 indicates the Desktop; 1 indicates the systems default downloads location; 2 indicates a custom (see: browser.download.dir) folder.
             * - browser.download.dir : A local folder the user may have selected for downloaded files to be saved. Migration of other browser settings may also set this path. This folder is enabled when browser.download.folderList equals 2.
             * - browser.download.manager.showWhenStarting : A boolean value that indicates whether or not to show the Downloads window when a download begins. The default value is true.
             *
             * - browser.helperApps.neverAsk.saveToDisk :
             *
             */

            FirefoxProfile profile = new FirefoxProfile()
            // set location to store files after downloading
            profile.setPreference("browser.download.useDownloadDir", true)
            profile.setPreference("browser.download.folderList", 2)
            Path downloads = Paths.get(System.getProperty('user.home'), 'Downloads')
            profile.setPreference("browser.download.dir", downloads.toString())
            // set preference not to show file download donfirmation dialog
            def mimeTypes = FileType.getAllMimeTypesAsString()
            println "mimeTypes=${mimeTypes}"
            profile.setPreference("browser.helperApps.neverAsk.saveToDisk", mimeTypes)
            profile.setPreference("browser.helperApps.neverAsk.openFile", mimeTypes)
            // profile.setPreference("browser.download.manager.showWhenStarting", false) // you can not modify this particular profile any more
            profile.setPreference("pdfjs.disable", true)
            FirefoxOptions options = new FirefoxOptions()
            options.setProfile(profile)
            new FirefoxDriver(options)
        }
    }
}
