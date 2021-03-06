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

package kr.heartpattern.spikot.nbt

import kotlin.reflect.KClass

/**
 * Represent nbt tag type
 * @param T Corresponding java type
 * @param id NMS internal type id
 * @param type Corresponding java class
 */
sealed class TagType<T : Any>(val id: Int, val type: KClass<T>) {
    companion object {
        /**
         * All nbt types
         */
        val TYPES = listOf(
            END,
            BYTE,
            SHORT,
            INT,
            LONG,
            FLOAT,
            DOUBLE,
            BYTE_ARRAY,
            STRING,
            LIST,
            COMPOUND,
            INT_ARRAY,
            LONG_ARRAY,
            MISC
        )

        /**
         * Get nbt type from id
         * @param id NMS internal id
         * @return Corresponding tag type
         */
        fun ofId(id: Int): TagType<*> {
            for (type in TYPES)
                if (type.id == id)
                    return type

            throw IllegalArgumentException("Cannot find matching tag type for id $id")
        }

        /**
         * Get nbt type from java class
         * @param type Java class
         * @return Corresponding tag type
         */
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> ofType(type: KClass<T>): TagType<T> {
            for (tagType in TYPES)
                if (tagType.type == type)
                    return tagType as TagType<T>

            throw IllegalArgumentException("Cannot find matching tag type for type $type")
        }
    }

    object END : TagType<Unit>(0, Unit::class)
    object BYTE : TagType<Byte>(1, Byte::class)
    object SHORT : TagType<Short>(2, Short::class)
    object INT : TagType<Int>(3, Int::class)
    object LONG : TagType<Long>(4, Long::class)
    object FLOAT : TagType<Float>(5, Float::class)
    object DOUBLE : TagType<Double>(6, Double::class)

    @Suppress("ClassName")
    object BYTE_ARRAY : TagType<ByteArray>(7, ByteArray::class)
    object STRING : TagType<String>(8, String::class)
    object LIST : TagType<MutableList<*>>(9, MutableList::class)
    object COMPOUND : TagType<MutableMap<*, *>>(10, MutableMap::class)

    @Suppress("ClassName")
    object INT_ARRAY : TagType<IntArray>(11, IntArray::class)

    @Suppress("ClassName")
    object LONG_ARRAY : TagType<LongArray>(12, LongArray::class)
    object MISC : TagType<Any>(99, Any::class)
}