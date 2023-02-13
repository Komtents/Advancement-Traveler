/*
 * Copyright (c) 2021 Komtents Dev Team
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

package world.komq.advctravel.plugin.config

import io.github.monun.tap.config.Config
import io.github.monun.tap.config.ConfigSupport
import java.io.File

/**
 * @author ContentManager
 */

object AdvcTravelConfig {
    @Config
    var maxplayers = 1

    @Config
    var administrator = arrayListOf(
        "389c4c9b-6342-42fc-beb3-922a7d7a72f9",
        "762dea11-9c45-4b18-95fc-a86aab3b39ee",
        "63e8e8a6-4104-4abf-811b-2ed277a02738",
        "ad524e9e-acf5-4977-9c12-938212663361",
        "3013e38a-74a7-41d4-8e68-71ee440c0e20"
    )

    @Config
    var runner = arrayListOf(
        "389c4c9b-6342-42fc-beb3-922a7d7a72f9"
    )

    @Config
    var enabled = true

    @Config
    var fadein = 0

    @Config
    var stay = 150

    @Config
    var fadeout = 0

    fun load(configFile: File) {
        ConfigSupport.compute(this, configFile)
    }
}