/*
 * Copyright (c) 2021 PyBsh
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

package world.komq.advctravel.plugin.commands

import world.komq.advctravel.plugin.AdvcTravelMain
import world.komq.advctravel.plugin.objects.AdvcTpaObject
import world.komq.advctravel.plugin.tasks.AdvcTpaTask
import io.github.monun.kommand.getValue
import io.github.monun.kommand.kommand
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author PyBsh
 */

object AdvcTpaKommand {
    private fun getInstance(): Plugin {
        return AdvcTravelMain.instance
    }

    val tpaMap: HashMap<Player, AdvcTpaObject> = HashMap()

    var UUID.sendTpaDelay: Long
        get() {
            return sendTimestamps[this] ?: 0
        }
        set(value) {
            sendTimestamps[this] = value
        }
    private val sendTimestamps = HashMap<UUID, Long>()

    var UUID.receiveTpaDelay: Long
        get() {
            return receiveTimestamps[this] ?: 0
        }
        set(value) {
            receiveTimestamps[this] = value
        }
    private val receiveTimestamps = HashMap<UUID, Long>()

    fun advcTpaKommand() {
        getInstance().kommand {
            register("tpa") {
                requires { playerOrNull != null }
                then("receiver" to player()) {
                    executes {
                        val sender = player
                        val receiver: Player by it

                        val sm = Bukkit.getScoreboardManager()
                        val sc = sm.mainScoreboard
                        val runner = sc.getTeam("Runner")

                        if (sender == receiver) sender.sendMessage(text("얘! 너 자신에게는 텔레포트를 왜 하니?",NamedTextColor.RED))
                        else if (tpaMap.containsKey(sender)) sender.sendMessage(text("얘! 이미 다른사람에게 텔레포트 요청을 했단다, 욕심쟁이니?",NamedTextColor.RED))
                        else if (System.currentTimeMillis() - sender.uniqueId.sendTpaDelay < 1200000) sender.sendMessage(text("얘! ${20 - TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - sender.uniqueId.sendTpaDelay)}분 뒤에 할 수 있단다!",NamedTextColor.RED))
                        else if (runner?.entries?.contains(receiver.name) == true) sender.sendMessage(text("애! 러너한테 텔레포트하면 그게 데스런이니?", NamedTextColor.RED))
                        else if (runner?.entries?.contains(sender.name) == true) sender.sendMessage(text("얘! 러너가 텔레포트하면 그게 데스런이니?", NamedTextColor.RED))
                        else if (getInstance().config.getString("administrator").toString().contains(receiver.uniqueId.toString())) sender.sendMessage(text("얘! 관리자한테 텔레포트하면 뭉탱이로 유리게슝 당한단다!", NamedTextColor.RED))

                        else {
                            receiver.sendMessage(
                                text(
                                    "${sender.name}님으로부터 텔레포트 요청이 들어왔습니다.\n" +
                                            "/tpaccept로 요청을 수락 할 수 있습니다.\n" +
                                            "/tpadeny로 요청을 거절 할 수 있습니다.\n" +
                                            "이 요청은 120초 후에 자동으로 만료됩니다.", NamedTextColor.GOLD
                                )
                            )

                            sender.sendMessage(
                                text(
                                    "${receiver.name}님에게 텔레포트 요청을 하였습니다.\n" +
                                            "/tpacancel 명령으로 요청을 취소 할 수 있습니다.\n" +
                                            "이 요청은 120초 후에 자동으로 만료됩니다.", NamedTextColor.GOLD
                                )
                            )

                            AdvcTpaTask.waitTask(sender, receiver)
                        }
                    }

                }
            }

            register("tpaaccept", "tpaccept") {
                requires { playerOrNull != null }
                executes {
                    val receiver = player

                    if (System.currentTimeMillis() - receiver.uniqueId.receiveTpaDelay < 180000) sender.sendMessage(text("얘! ${3 - TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - receiver.uniqueId.receiveTpaDelay)}분 뒤에 할 수 있단다!",NamedTextColor.RED))
                    else if (!tpaMap.values.any { x -> x.receiver == receiver }) receiver.sendMessage(text("얘! 받은 요청이 없잖아!!", NamedTextColor.RED))
                    else if (tpaMap.values.count { x -> x.receiver == receiver } > 1) receiver.sendMessage(text("얘! 받은 요청이 너무 많아! /tpaaccept <Player>로 다시 받으렴", NamedTextColor.RED))
                    else if (tpaMap.values.any { x -> x.receiver == receiver && x.isAccepted }) receiver.sendMessage(text("얘! 이미 요청을 받고있잖니! 하나만 하렴!",NamedTextColor.RED))
                    else {
                        val sender = tpaMap.values.first { x -> x.receiver == receiver}.sender
                        val accepted = tpaMap.values.first { x -> x.receiver == receiver}.isAccepted

                        if (accepted) receiver.sendMessage(text("얘! 이미 요청을 받았잖아!! 또 받게?", NamedTextColor.RED))
                        else {
                            tpaMap.values.first { x -> x.receiver == receiver }.isAccepted = true
                            tpaMap.values.first { x -> x.receiver == receiver }.expiredTask.cancel()

                            AdvcTpaTask.acceptTask(sender, receiver)

                            sender.sendMessage(text("요청이 수락되었습니다. 30초간 움직이지 마세요", NamedTextColor.GOLD))
                            receiver.sendMessage(text("요청을 수락하셨습니다. 30초간 움직이지 마세요", NamedTextColor.GOLD))
                            //AdvcTpaTask.countdown(sender)
                        }
                    }

                }
                then("sender" to player()){
                    requires { playerOrNull != null }
                    executes {
                        val receiver = player
                        val sender: Player by it

                        if (System.currentTimeMillis() - receiver.uniqueId.receiveTpaDelay < 180000) sender.sendMessage(text("얘! ${3 - TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - receiver.uniqueId.receiveTpaDelay)}분 뒤에 할 수 있단다!",NamedTextColor.RED))
                        else if (!tpaMap.values.any { x -> x.receiver == receiver && x.sender == sender }) receiver.sendMessage(text("얘! ${sender.name}한테서 받은 요청이 없잖아!!", NamedTextColor.RED))
                        else if (tpaMap.values.first { x -> x.receiver == receiver && x.sender == sender }.isAccepted) receiver.sendMessage(text("얘! 이미 요청을 받았잖아!! 또 받게?", NamedTextColor.RED))
                        else if (tpaMap.values.any { x -> x.receiver == receiver && x.isAccepted }) receiver.sendMessage(text("얘! 이미 요청을 받고있잖니! 하나만 하렴!",NamedTextColor.RED))
                        else {
                            tpaMap[sender]?.isAccepted = true
                            tpaMap[sender]?.expiredTask?.cancel()

                            AdvcTpaTask.acceptTask(sender, receiver)

                            sender.sendMessage(text("요청이 수락되었습니다. 30초간 움직이지 마세요", NamedTextColor.GOLD))
                            receiver.sendMessage(text("요청을 수락하셨습니다. 30초간 움직이지 마세요", NamedTextColor.GOLD))
                            //AdvcTpaTask.countdown(sender)
                        }
                    }
                }
            }

            register("tpadeny") {
                requires { playerOrNull != null }
                executes {
                    val receiver = player

                    if (!tpaMap.values.any { x -> x.receiver == receiver }) receiver.sendMessage(text("얘! 받은 요청이 없잖아!!", NamedTextColor.RED))
                    else if (tpaMap.values.count { x -> x.receiver == receiver } > 1) receiver.sendMessage(text("얘! 받은 요청이 너무 많아! /tpadeny <Player>로 다시 거절하렴", NamedTextColor.RED))
                    else {
                        val sender = tpaMap.values.first { x -> x.receiver == receiver}.sender

                        tpaMap[sender]?.expiredTask?.cancel()
                        tpaMap[sender]?.waitTask?.cancel()
                        tpaMap.remove(sender)

                        sender.sendMessage(text("${receiver.name}님에게 보낸 요청이 거절되었습니다.", NamedTextColor.GOLD))
                        receiver.sendMessage(text("${sender.name}님의 요청을 거절하였습니다.", NamedTextColor.GOLD))
                    }
                }
                then("sender" to player()){
                    requires { playerOrNull != null }
                    executes {
                        val receiver = player
                        val sender: Player by it

                        if (!tpaMap.values.any { x -> x.receiver == receiver && x.sender == sender }) receiver.sendMessage(text("얘! ${sender.name}한테서 받은 요청이 없잖아!!", NamedTextColor.RED))
                        else{
                            tpaMap[sender]?.expiredTask?.cancel()
                            tpaMap[sender]?.waitTask?.cancel()
                            tpaMap.remove(sender)

                            sender.sendMessage(text("${receiver.name}님에게 보낸 요청이 거절되었습니다.", NamedTextColor.GOLD))
                            receiver.sendMessage(text("${sender.name}님의 요청을 거절하였습니다.", NamedTextColor.GOLD))
                        }
                    }
                }
            }

            register("tpacancel"){
                requires { playerOrNull != null }
                executes {
                    val sender = player

                    if(!tpaMap.keys.any { x -> x == sender }) sender.sendMessage(text("얘! 없는 요청을 취소할 순 없어!", NamedTextColor.RED))
                    else{
                        val receiver = tpaMap[sender]?.receiver

                        sender.sendMessage(text("${receiver?.name}님에게 보낸 요청을 취소하였습니다.",NamedTextColor.GOLD))
                        receiver?.sendMessage(text("${sender.name}님이 보낸 요청을 취소하였습니다. ", NamedTextColor.GOLD))

                        tpaMap[sender]?.expiredTask?.cancel()
                        tpaMap[sender]?.waitTask?.cancel()
                        tpaMap.remove(sender)
                    }
                }
            }
        }
    }
}