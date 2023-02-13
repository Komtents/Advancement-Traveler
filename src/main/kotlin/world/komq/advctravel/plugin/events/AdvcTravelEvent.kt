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

package world.komq.advctravel.plugin.events

import world.komq.advctravel.plugin.AdvcTravelMain
import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.*
import org.bukkit.event.player.PlayerLoginEvent.Result
import org.bukkit.plugin.Plugin
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.random.Random


/**
 * @author ContentManager
 */

class AdvcTravelEvent : Listener {
    private fun getInstance(): Plugin {
        return AdvcTravelMain.instance
    }

    private val server = getInstance().server

    private val adminTeam = server.scoreboardManager.mainScoreboard.getTeam("Admin")

    private fun randomTeleport(player: Player) {
        val randomX = Random.nextInt(-1000, 1000)
        val randomZ = Random.nextInt(-1000, 1000)

        player.teleport(player.location.set(randomX.toDouble(), player.world.getHighestBlockYAt(randomX, randomZ).toDouble() + 1, randomZ.toDouble()))
    }

    private fun teamMsgTask(player: Player, messageContent: String) {
        server.scheduler.runTask(getInstance(), Runnable {
                server.dispatchCommand(player as CommandSender, "teammsg $messageContent")
            })
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val p = e.player
        p.noDamageTicks = 0

        val sm = Bukkit.getScoreboardManager()
        val sc = sm.mainScoreboard

        if (p.uniqueId.toString() in getInstance().config.getString("runner").toString()){
            val runner = sc.getTeam("runner")
            runner?.addEntry(p.name)
        } else if (p.uniqueId.toString() !in getInstance().config.getString("runner").toString() && p.uniqueId.toString() !in getInstance().config.getString("administrator").toString()) {
            val hunter = sc.getTeam("Hunter")
            hunter?.addEntry(p.name)
        }
        else if (p.uniqueId.toString() in getInstance().config.getString("administrator").toString()) {
            val admin = sc.getTeam("Admin")
            admin?.addEntry(p.name)
        }

        if (!p.hasPlayedBefore()) {
            server.scheduler.runTaskLater(getInstance(), Runnable {
                randomTeleport(p)
            }, 0)
        }
    }

    @EventHandler
    fun onPlayerAdvancementDone(e: PlayerAdvancementDoneEvent) {
        val p = e.player
        val advancement = e.advancement

        if (p.uniqueId.toString() in getInstance().config.getString("runner").toString()) {
            if (!advancement.key.toString().startsWith("minecraft:recipes") && !advancement.key.toString().endsWith("root")) {
                ++server.maxPlayers
                getInstance().config.set("maxplayers", server.maxPlayers)
                getInstance().saveConfig()
            }
        }
    }

    @EventHandler
    fun onAsyncChat(e: AsyncChatEvent) {
        val p = e.player
        val msgComponent = e.message()
        val msg = (msgComponent as TextComponent).content()

        if (p.uniqueId.toString() in getInstance().config.getString("administrator").toString() || (adminTeam != null) && adminTeam.hasEntry(p.name)) {
            teamMsgTask(p, msg)
            e.isCancelled = true

            server.consoleSender.sendMessage(text("<${p.name}> $msg"))
        }
        else {
            teamMsgTask(p, msg)

            e.isCancelled = true
            server.consoleSender.sendMessage(text("${p.name} issued server command: /teammsg $msg"))
        }
    }

    @EventHandler
    fun onPlayerCommandPreprocess(e: PlayerCommandPreprocessEvent) {
        val p = e.player
        val c = e.message

        if(c.startsWith("/tpa") || c.startsWith("/teammsg") || c.startsWith("/tm")) e.isCancelled = false
        else e.isCancelled = p.uniqueId.toString() !in getInstance().config.getString("administrator").toString()
    }

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        val player = e.player
        val block = e.block

        if (Tag.BEDS.isTagged(block.type) && player.world.environment != World.Environment.NORMAL) {
            e.isCancelled = true
            player.sendMessage(text("지옥과 엔더에서는 침대가 막혀있습니다!", NamedTextColor.RED))
        }
        if (block.type == Material.RESPAWN_ANCHOR && player.world.environment != World.Environment.NETHER) {
            e.isCancelled = true
            player.sendMessage(text("오버월드와 엔더에서는 리스폰 정박기가 막혀있습니다!", NamedTextColor.RED))
        }
        if (block.type == Material.PISTON || block.type == Material.STICKY_PISTON || block.type == Material.HOPPER) {
            e.isCancelled = true
            player.sendMessage(text("NO.", NamedTextColor.RED))
        }
    }

    @EventHandler
    fun onPlayerLogin(e: PlayerLoginEvent) {
        val p = e.player

        if (p.uniqueId.toString() in getInstance().config.getString("administrator").toString()) {
            if (!p.isBanned) {
                if (e.result == Result.KICK_FULL) {
                    e.allow()
                    ++server.maxPlayers
                }
            }
        } else if (p.uniqueId.toString() in getInstance().config.getString("runner").toString()) {
            if (!p.isBanned) {
                if (e.result == Result.KICK_FULL) {
                    e.allow()
                    ++server.maxPlayers
                }
            }
        }

        getInstance().config.set("maxplayers", server.maxPlayers)
        getInstance().saveConfig()

    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        val p = e.player

        if (p.uniqueId.toString() in getInstance().config.getString("administrator").toString()) {
            val currentPlayers = server.onlinePlayers.size

            if (server.maxPlayers == currentPlayers) --server.maxPlayers
        }
        else if (p.uniqueId.toString() in getInstance().config.getString("runner").toString()) {
            val currentPlayers = server.onlinePlayers.size

            if (server.maxPlayers == currentPlayers) --server.maxPlayers
        }

        getInstance().config.set("maxplayers", server.maxPlayers)
        getInstance().saveConfig()
    }

    @EventHandler
    fun onPlayerRespawn(e: PlayerRespawnEvent) {
        val p = e.player

        if (p.bedSpawnLocation == null) {
            server.scheduler.runTaskLater(getInstance(), Runnable {
                randomTeleport(p)
            }, 4)
        }
    }



    @EventHandler
    fun onPaperServerListPing(e: PaperServerListPingEvent) {
        val file = File("${getInstance().dataFolder}/server-icon.png")

        if (!file.exists()) {
            val imgURL =
                URL("https://cdn.discordapp.com/attachments/882830728382013480/886135047005011968/server-icon.png")
            val dest = ("${getInstance().dataFolder}/server-icon.png")
            val inputStream = imgURL.openStream()
            val outputStream = FileOutputStream(dest)

            val byte = ByteArray(2048)
            var length: Int

            while (inputStream.read(byte).also { length = it } != -1) {
                outputStream.write(byte, 0, length)
            }

            inputStream.close()
            outputStream.close()
        }

        val icon = server.loadServerIcon(file)

        e.motd(text("ADVANCEMENT TRAVELER", NamedTextColor.RED, TextDecoration.BOLD))
        e.playerSample.clear()
        e.serverIcon = icon
    }

    @EventHandler
    fun onPlayerInteractEvent(e: PlayerInteractEvent) {
        val p = e.player
        val block = e.clickedBlock?.type

        if (p.uniqueId.toString() !in getInstance().config.getString("runner").toString()) {
            if (e.action == Action.LEFT_CLICK_BLOCK || e.action == Action.RIGHT_CLICK_BLOCK) {
                if (block != null && !block.isAir && block == Material.END_PORTAL_FRAME || block == Material.DRAGON_EGG) {
                    e.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onPlayerAttemptItemPickup(e: PlayerAttemptPickupItemEvent) {
        val p = e.player
        val item = e.item.itemStack

        if (item.type == Material.DRAGON_EGG && p.uniqueId.toString() !in getInstance().config.getString("runner").toString()) {
            e.isCancelled = true
        }
    }
}