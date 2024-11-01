/*
    MIT License
    Copyright (c) 2018 - 2019 Pierre-Yves Lapersonne (Mail: dev@pylapersonne.info)
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */
// ✿✿✿✿ ʕ •ᴥ•ʔ/ ︻デ═一

package pylapp.tapster.client.android.ui.settings

import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity

import pylapp.tapster.client.android.tools.Config
import pylapp.tapster.client.android.tools.FeaturesFactory
import pylapp.tapster.client.android.tools.properties.PropertiesReaderStub

import android.content.Intent
import android.net.Uri
import pylapp.tapster.client.android.R


/**
 * The activity for the settings and the preferences
 *
 * @author Pierre-Yves Lapersonne
 * @since 06/02/2018
 * @version 2.1.0
 */
class SettingsActivity : AppCompatActivity() {


    /* **************** *
     * COMPANION OBJECT *
     * **************** */

    /**
     * A simple companion object
     */
    companion object {

        /**
         * The release
         */
        private var mVersionRelease: String? = null

    }

    /* ****************************** *
     * METHODS FROM AppCompatActivity *
     * ****************************** */

    /**
     * Triggered to create the view
     *
     * @param savedInstanceState -
     */
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Builds the release string
        val sb = StringBuilder()
        try {
            val pi = packageManager.getPackageInfo(packageName, 0)
            val versionName = pi.versionName
            sb.append("v").append(versionName)
            val versionCode = pi.versionCode
            sb.append(" - ").append(versionCode)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        mVersionRelease = sb.toString()

        // Display the fragment as the main content.
        fragmentManager
                .beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()

    }


    /* *********** *
     * INNER CLASS *
     * *********** */

    /**
     * The Fragment for preferences.
     * See http://developer.android.com/guide/topics/ui/settings.html
     */
    class SettingsFragment : PreferenceFragment() {

        /**
         * @param savedInstanceState -
         */
        override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)

            // Add widgets
            addPreferencesFromResource(R.xml.preferences)

            // Update the version details
            val versionPreference = findPreference(Config.PREFERENCES_APP_VERSION)
            versionPreference.summary = mVersionRelease

            // Update on the fly fields
            updateFieldsAboutRobotConfig()

            // Deal with licences
            if (isDisplayLicensesEnabled()) {
                val licensesPreferences = findPreference(Config.PREFERENCES_APP_LICENSES)
                licensesPreferences.setOnPreferenceClickListener {
                    displayLicenses()
                }
            } else {
                hideRelatedLicensesFields()
            }

            // Deal with author section
            val authorField = findPreference(Config.PREFERENCES_APP_AUTHOR)
            authorField.setOnPreferenceClickListener {
                displayAuthorPage()
            }

            // Deal with project section
            val projectField = findPreference(Config.PREFERENCES_APP_PROJECT)
            projectField.setOnPreferenceClickListener {
                displayProjectPage()
            }

        } // End of public void onCreate( Bundle savedInstanceState )

        /**
         * Triggers the display of the widget which contains the licenses
         */
        private fun displayLicenses(): Boolean {
            FeaturesFactory().buildLicensesDisplayer().displayLicenses(activity)
            return true
        }

        /**
         * Triggers the display of the author's page, i.e. its website
         */
        private fun displayAuthorPage(): Boolean {
            val webPageIntent = Intent(Intent.ACTION_VIEW)
            webPageIntent.data = Uri.parse("https://pylapersonne.info")
            startActivity(webPageIntent)
            return true
        }

        /**
         * Triggers the display of the author's page, i.e. its website
         */
        private fun displayProjectPage(): Boolean {
            val webPageIntent = Intent(Intent.ACTION_VIEW)
            webPageIntent.data = Uri.parse("https://github.com/pylapp/tapsterbot")
            startActivity(webPageIntent)
            return true
        }

        /**
         * Returns if the feature about licenses display is enabled or not
         *
         * @return [Boolean] - False if disabled, true if enabled
         */
        private fun isDisplayLicensesEnabled(): Boolean {
            val properties = FeaturesFactory().buildPropertiesReader()
            properties.loadProperties(activity)
            return properties.readProperty(PropertiesReaderStub.ENABLE_GUI_DISPLAY_LICENSES)!!.toBoolean()
        }

        /**
         * Hides in the GUI the fields related to licenses
         */
        private fun hideRelatedLicensesFields() {
            val category = findPreference(Config.PREFERENCES_CATEGORY_ABOUT) as PreferenceCategory
            val preferenceToRemove = findPreference(Config.PREFERENCES_APP_LICENSES)
            category.removePreference(preferenceToRemove)
        }

        /**
         * Fills the fields in the screen about the robot so as to display, if defined, the values
         */
        private fun updateFieldsAboutRobotConfig(){

            // Get stored values
            val preferences = PreferenceManager.getDefaultSharedPreferences(activity)

            // Update summaries when values have been changed
            val prefProtocol = findPreference(Config.PREFERENCES_ROBOT_PROTOCOL)
            prefProtocol.onPreferenceChangeListener = Preference.OnPreferenceChangeListener{
                _, _ ->
                val serverProtocol = preferences.getString(Config.PREFERENCES_ROBOT_PROTOCOL,
                        activity!!.getString(R.string.default_value_server_protocol))
                prefProtocol.summary = serverProtocol
                true
            }
            val prefIp = findPreference(Config.PREFERENCES_ROBOT_IP)
            prefIp.onPreferenceChangeListener = Preference.OnPreferenceChangeListener{
                _, _ ->
                val serverIp = preferences.getString(Config.PREFERENCES_ROBOT_IP,
                activity!!.getString(R.string.default_value_server_ip_address))
                prefIp.summary = serverIp
                true
            }
            val prefPort = findPreference(Config.PREFERENCES_ROBOT_PORT)
            prefPort.onPreferenceChangeListener = Preference.OnPreferenceChangeListener{
                _, _ ->
                val serverPort = preferences.getString(Config.PREFERENCES_ROBOT_PORT,
                        activity!!.getString(R.string.default_value_server_port))
                prefPort.summary = serverPort
                true
            }

            // Update values now, prevent to display values only when they have been changed
            prefProtocol.summary = preferences.getString(Config.PREFERENCES_ROBOT_PROTOCOL,
                    activity!!.getString(R.string.default_value_server_protocol))
            prefIp.summary = preferences.getString(Config.PREFERENCES_ROBOT_IP,
                    activity!!.getString(R.string.default_value_server_ip_address))
            prefPort.summary = preferences.getString(Config.PREFERENCES_ROBOT_PORT,
                    activity!!.getString(R.string.default_value_server_port))
        }

    } // End of  class SettingsFragment : PreferenceFragment()

}
