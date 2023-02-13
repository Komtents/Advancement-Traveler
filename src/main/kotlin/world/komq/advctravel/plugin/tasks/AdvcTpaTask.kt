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

package world.komq.advctravel.plugin.tasks

import world.komq.advctravel.plugin.AdvcTravelMain
import world.komq.advctravel.plugin.commands.AdvcTpaKommand
import world.komq.advctravel.plugin.commands.AdvcTpaKommand.receiveTpaDelay
import world.komq.advctravel.plugin.commands.AdvcTpaKommand.sendTpaDelay
import world.komq.advctravel.plugin.objects.AdvcTpaObject
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * @author ContentManager
 */

object AdvcTpaTask {

    private fun getInstance(): Plugin {
        return AdvcTravelMain.instance
    }

    private val server = getInstance().server

    private val scheduler = server.scheduler

    /*fun countdown(sender: Player) {
        val receiver = AdvcTpaKommand.tpaMap[sender]?.receiver
        val startTime = System.currentTimeMillis()

        val scheudle = scheduler.scheduleSyncRepeatingTask(
            getInstance(), {
                if (AdvcTpaKommand.tpaMap[sender]?.isAccepted != true) {
                    sender.sendActionBar(text("텔레포트가 취소되었습니다", NamedTextColor.RED))
                    receiver?.sendActionBar(text("텔레포트가 취소되었습니다", NamedTextColor.RED))

                } else {
                    sender.sendActionBar(
                        text(
                            "${29 - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)}초 후 텔레포트합니다...",
                            NamedTextColor.GOLD
                        )
                    )
                    receiver?.sendActionBar(
                        text(
                            "${29 - TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)}초 후 텔레포트합니다...",
                            NamedTextColor.GOLD
                        )
                    )
                }
            },
            0L, 0L)
    }*/

    fun acceptTask(sender: Player, receiver: Player) {
        val acceptTask = scheduler.runTaskLater(getInstance(), Runnable {
            sender.uniqueId.sendTpaDelay = System.currentTimeMillis()
            receiver.uniqueId.receiveTpaDelay = System.currentTimeMillis()
            sender.sendMessage(text("텔레포트중입니다...", NamedTextColor.GOLD))
            sender.teleport(receiver.location)
            AdvcTpaKommand.tpaMap.remove(sender)
        }, 600L)

        AdvcTpaKommand.tpaMap[sender]?.waitTask = acceptTask
    }

    fun waitTask(sender: Player, receiver: Player) {
        val waitTask = scheduler.runTaskLater(
            getInstance(),
            Runnable {
                AdvcTpaKommand.tpaMap.remove(sender)
                sender.sendMessage(text("얘! 요청이 만료되었단다!", NamedTextColor.GOLD))
                receiver.sendMessage(text("얘! ${sender.name} 에게 온 요청이 만료되었단다!", NamedTextColor.GOLD))
            },
            2400L
        )

        AdvcTpaKommand.tpaMap[sender] = AdvcTpaObject(sender, receiver, false, waitTask, null)
    }
}