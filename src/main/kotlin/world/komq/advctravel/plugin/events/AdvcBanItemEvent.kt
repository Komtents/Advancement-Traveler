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
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.EntityResurrectEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import kotlin.random.Random

/**
 * @author ContentManager
 */

class AdvcBanItemEvent: Listener {
    private fun getInstance(): Plugin {
        return AdvcTravelMain.instance
    }

    private val administrator = requireNotNull(getInstance().config.getString("administrator").toString())
    private val runner = requireNotNull(getInstance().config.getString("runner").toString())

    @EventHandler
    fun onPlayerCraft (e: CraftItemEvent) {
        val p = e.whoClicked
        val item = e.recipe.result

        if (p.uniqueId.toString() !in runner && p.uniqueId.toString() !in administrator) {
            when (item.type){
                Material.FIREWORK_ROCKET -> e.isCancelled = true
                Material.END_CRYSTAL -> e.isCancelled = true
                else -> e.isCancelled = false
            }
        }
    }

    @EventHandler
    fun onBreakBlock (e: BlockBreakEvent) {
        val p = e.player
        val b = e.block

        if (p.uniqueId.toString() !in runner && p.uniqueId.toString() !in administrator) {
            if(b.type == Material.ENDER_CHEST ||  b.type.toString().endsWith("SHULKER_BOX")){
                e.isDropItems = false
            }
        }
    }

    @EventHandler
    fun onPlaceBlock (e: BlockPlaceEvent) {
        val p = e.player
        val b = e.block

        if (p.uniqueId.toString() !in runner && p.uniqueId.toString() !in administrator) {
            if(p.world.environment == World.Environment.THE_END){
                if(b.type == Material.OBSIDIAN){
                    e.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onEntityExplode (e: EntityExplodeEvent) {
        e.blockList().removeAll { x -> x.blockData.material.toString().endsWith("SHULKER_BOX") }
    }

    @EventHandler
    fun onPlayerUseBucket (e: PlayerBucketEmptyEvent) {
        val p = e.player
        val b = e.bucket

        if (p.uniqueId.toString() !in runner && p.uniqueId.toString() !in administrator) {
            if(p.world.environment == World.Environment.THE_END) {
                if (b == Material.LAVA_BUCKET) {
                    e.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onPlayerResurrect(e: EntityResurrectEvent){
        val p = e.entity

        if (p is Player) {
            if (p.uniqueId.toString() !in runner && p.uniqueId.toString() !in administrator) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onEntityDeath(e: EntityDeathEvent){
        val dead = e.entity
        val p = dead.killer

        if (p is Player) {
            if (p.uniqueId.toString() !in runner && p.uniqueId.toString() !in administrator) {
                if (dead.type == EntityType.EVOKER) {
                    e.drops.clear()
                    e.drops.add(ItemStack(Material.EMERALD, Random.nextInt(3) +1))
                }
            }
        }
    }

    @EventHandler
    fun onEntityDamagedByEntity(e: EntityDamageByEntityEvent){
        val entity = e.entity
        val damager = e.damager

        if (damager is Player) {
            if (damager.uniqueId.toString() !in runner && damager.uniqueId.toString() !in administrator) {
                if(entity.type == EntityType.ENDER_CRYSTAL){
                    e.isCancelled = true
                }
            }
        }
        else if(damager is Arrow && damager.shooter is Player){
            if ((damager.shooter as Player).uniqueId.toString() !in runner && (damager.shooter as Player).uniqueId.toString() !in administrator) {
                if(entity.type == EntityType.ENDER_CRYSTAL){
                    e.isCancelled = true
                }
            }
        }
        else if(damager is Trident && damager.shooter is Player){
            if ((damager.shooter as Player).uniqueId.toString() !in runner && (damager.shooter as Player).uniqueId.toString() !in administrator) {
                if(entity.type == EntityType.ENDER_CRYSTAL){
                    e.isCancelled = true
                }
            }
        }
        else if(damager is SpectralArrow && damager.shooter is Player){
            if ((damager.shooter as Player).uniqueId.toString() !in runner && (damager.shooter as Player).uniqueId.toString() !in administrator) {
                if(entity.type == EntityType.ENDER_CRYSTAL){
                    e.isCancelled = true
                }
            }
        }
        else if(damager is Egg && damager.shooter is Player){
            if ((damager.shooter as Player).uniqueId.toString() !in runner && (damager.shooter as Player).uniqueId.toString() !in administrator) {
                if(entity.type == EntityType.ENDER_CRYSTAL){
                    e.isCancelled = true
                }
            }
        }
        else if(damager is Snowball && damager.shooter is Player){
            if ((damager.shooter as Player).uniqueId.toString() !in runner && (damager.shooter as Player).uniqueId.toString() !in administrator) {
                if(entity.type == EntityType.ENDER_CRYSTAL){
                    e.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent){
        val p = e.player
        val a = e.action
        if (p.uniqueId.toString() !in runner && p.uniqueId.toString() !in administrator) {
            if(a == Action.RIGHT_CLICK_BLOCK || a == Action.RIGHT_CLICK_AIR){
                if(p.inventory.itemInMainHand.type.toString().endsWith("POTION") || p.inventory.itemInOffHand.type.toString().endsWith("POTION")){
                    e.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun onPlayerElytraBoost(e: PlayerElytraBoostEvent){
        val p = e.player
        if (p.uniqueId.toString() !in runner && p.uniqueId.toString() !in administrator) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onDispenseEvent(e: BlockDispenseEvent){
        e.isCancelled = true
    }

}