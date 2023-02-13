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

package world.komq.advctravel.plugin

import world.komq.advctravel.plugin.commands.AdvcTpaKommand
import world.komq.advctravel.plugin.commands.AdvcTravelKommand
import world.komq.advctravel.plugin.config.AdvcTravelConfig
import world.komq.advctravel.plugin.events.AdvcBanItemEvent
import world.komq.advctravel.plugin.events.AdvcTpaEvent
import world.komq.advctravel.plugin.events.AdvcTravelEvent
import world.komq.advctravel.plugin.objects.AdvcRecipeObject
import world.komq.advctravel.plugin.tasks.AdvcConfigReloadTask
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.DisplaySlot
import java.io.File

/**
 * @author ContentManager
 */

class AdvcTravelMain : JavaPlugin() {
    companion object {
        lateinit var instance: AdvcTravelMain
            private set
    }

    private val configFile = File(dataFolder, "config.yml")

    private val whitelistFile = File(dataFolder, "Whitelist.txt")

    override fun onEnable() {
        instance = this

        // Config and logger settings output
        AdvcTravelConfig.load(configFile)
        
        logger.info("Config Administrator Settings: ${config.getString("administrator")}")
        logger.info("Config Runner Settings: ${config.getString("runner")}")

        // Player count bug on reload (Fixed with below code)
        server.maxPlayers = config.getInt("maxplayers") + server.onlinePlayers.asSequence().filter {
            config.getString("administrator").toString().contains(it.uniqueId.toString())
        }.toMutableList().size

        // Registering
        server.pluginManager.registerEvents(AdvcTravelEvent(), this)
        server.pluginManager.registerEvents(AdvcTpaEvent(), this)
        server.pluginManager.registerEvents(AdvcBanItemEvent(), this)
        server.scheduler.runTaskTimer(this, AdvcConfigReloadTask(), 0L, 1L)
        AdvcTravelKommand.advcTravelKommand()
        AdvcTpaKommand.advcTpaKommand()

        // ScoreboardManager
        val sm = server.scoreboardManager
        val sc = sm.mainScoreboard

        val health = sc.getObjective("Health")
        if (health == null) sc.registerNewObjective("Health", "heatlh", text("♥", NamedTextColor.RED))

        health?.displaySlot = DisplaySlot.BELOW_NAME

        val hunter = sc.getTeam("Hunter")
        if (hunter == null) sc.registerNewTeam("Hunter")

        hunter?.color(NamedTextColor.RED)

        val runner = sc.getTeam("Runner")
        if (runner == null) sc.registerNewTeam("Runner")

        runner?.color(NamedTextColor.AQUA)

        val admin = sc.getTeam("Admin")
        if (admin == null) sc.registerNewTeam("Admin")

        admin?.color(NamedTextColor.DARK_RED)

        // Team Settings
        for (teams in sc.teams) {
            teams.setCanSeeFriendlyInvisibles(false)
            teams.setAllowFriendlyFire(false)
        }

        // Recipe Settings
        val elytra = AdvcRecipeObject.elytra()
        server.addRecipe(elytra)
    }

    override fun onDisable() {
        // Player count bug on close (Fixed with below code)
        val adminCount = server.onlinePlayers.asSequence().filter {
            config.getString("administrator").toString().contains(it.uniqueId.toString())
        }.toMutableList().size

        config.set("maxplayers", server.maxPlayers - adminCount)
        saveConfig()
    }
}