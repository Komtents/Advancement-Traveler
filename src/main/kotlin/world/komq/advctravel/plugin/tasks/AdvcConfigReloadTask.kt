/*
 * Copyright (c) 2021 ContentManager
 *
 *  Licensed under the General Public License, Version 3.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/gpl-3.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package world.komq.advctravel.plugin.tasks

import world.komq.advctravel.plugin.AdvcTravelMain
import org.bukkit.plugin.Plugin
import java.io.File

/**
 * @author ContentManager
 */

class AdvcConfigReloadTask : Runnable {
    private fun getInstance(): Plugin {
        return AdvcTravelMain.instance
    }

    private lateinit var allows: Set<String>

    private val logger = getInstance().logger

    private val configFile = File(getInstance().dataFolder, "config.yml")

    private var configFileLastModified = configFile.lastModified()

    override fun run() {
        if (configFileLastModified != configFile.lastModified()) {
            getInstance().reloadConfig()
            getInstance().saveConfig()

            configFileLastModified = configFile.lastModified()

//            logger.info("Config reloaded.")
//            logger.info("Config Administrator Settings: ${config.getString("administrator").toString()}")
//            logger.info("Config Runner Settings: ${config.getString("runner").toString()}")
        }
    }
}
