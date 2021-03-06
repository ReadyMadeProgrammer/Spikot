/*
 * Copyright 2020 Spikot project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

@file:Suppress("unused")

package kr.heartpattern.spikot.menu

import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

typealias ClickHandler = (SlotPosition, ClickType) -> Unit

/**
 * DSL Marker for MenuDSL
 */
@DslMarker
@Target(AnnotationTarget.CLASS)
annotation class MenuDsl

/**
 * Represent position of slot
 * @param x x-position
 * @param y y-position
 */
data class SlotPosition(val x: Int, val y: Int) {
    /**
     * Create SlotPosition from raw index
     * @param index Raw index
     */
    constructor(index: Int) : this(index % 9, index / 9)

    /**
     * Raw index
     */
    val index: Int
        get() = y * 9 + x

    /**
     * Get responding slot for given inventory
     */
    fun getSlot(inventory: Inventory): ItemStack = inventory.getItem(index)
}

/**
 * Configure and update inventory menu
 */
@MenuDsl
class MenuBuilder {
    internal val slot: MutableMap<SlotPosition, Slot> = HashMap()
    internal var clear: Boolean = false
    /**
     * Title of menu
     */
    var title: String = ""
    /**
     * Size of menu
     */
    var size: Int = 54

    /**
     * Set slot
     * @param x X coordinate in inventory
     * @param y Y coordinate in inventory
     * @param build lambda that configure slot
     */
    fun slot(x: Int, y: Int, build: SlotBuilder.() -> Unit) {
        slot(SlotPosition(x, y), build)
    }

    /**
     * Set slot
     * @param x X coordinate in inventory
     * @param y Set of Y coordinate in inventory
     * @param build lambda that configure slot
     */
    fun slot(x: Int, y: Iterable<Int>, build: SlotBuilder.() -> Unit) {
        y.forEach { slot(x, it, build) }
    }

    /**
     * Set slot
     * @param x Set of X coordinate in inventory
     * @param y Y coordinate in inventory
     * @param build lambda that configure slot
     */
    fun slot(x: Iterable<Int>, y: Int, build: SlotBuilder.() -> Unit) {
        x.forEach { slot(it, y, build) }
    }

    /**
     * Set slot
     * @param x Set of X coordinate in inventory
     * @param y Set of Y coordinate in inventory
     * @param build lambda that configure slot
     */
    fun slot(x: Iterable<Int>, y: Iterable<Int>, build: SlotBuilder.() -> Unit) {
        x.forEach { xe -> y.forEach { ye -> slot(xe, ye, build) } }
    }

    /**
     * Set slot
     * @param i Raw slot in inventory
     * @param build lambda that configure slot
     */
    fun slot(i: Int, build: SlotBuilder.() -> Unit) {
        slot(i % 9, i / 9, build)
    }

    /**
     * Set slot
     * @param i Set of raw location in inventory
     * @param build lambda that configure slot
     */
    fun slot(i: Iterable<Int>, build: SlotBuilder.() -> Unit) {
        i.forEach { slot(it, build) }
    }

    /**
     * Set slot
     * @param point Slot position
     * @param build lambda that configure slot
     */
    fun slot(point: SlotPosition, build: SlotBuilder.() -> Unit) {
        val builder = SlotBuilder(point)
        builder.build()
        slot[point] = Slot(builder.display, builder.eventHandlers)
    }

    /**
     * Clear slots
     */
    fun clear() {
        clear = true
    }
}