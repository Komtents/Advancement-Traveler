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

package world.komq.advctravel.plugin.objects

import world.komq.advctravel.plugin.AdvcTravelMain
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.plugin.Plugin

/**
 * @author ContentManager
 */

object AdvcRecipeObject {
    private fun getInstance(): Plugin {
        return AdvcTravelMain.instance
    }

    fun elytra(): Recipe{
        val key = NamespacedKey(getInstance(), "hunter_elytra")

        val item = ItemStack(Material.ELYTRA)
        val meta = item.itemMeta as Damageable

        meta.damage = Material.ELYTRA.maxDurability - 1

        item.itemMeta = meta

        val recipe = ShapedRecipe(key, item)

        recipe.shape("PAP", "PPP", "APA")

        recipe.setIngredient('A', Material.AIR)
        recipe.setIngredient('P', Material.PHANTOM_MEMBRANE)


        return recipe
    }
}