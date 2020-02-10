/*
 * Copyright 2020 HeartPattern
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

package kr.heartpattern.spikot.serialization.serializer

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import org.bukkit.Bukkit
import org.bukkit.World
import java.util.*

@Serializer(forClass = World::class)
object WorldSerializer : KSerializer<World> {
    override val descriptor: SerialDescriptor = StringDescriptor.withName("World")

    override fun serialize(encoder: Encoder, obj: World) {
        encoder.encodeString(obj.uid.toString())
    }

    override fun deserialize(decoder: Decoder): World {
        return Bukkit.getWorld(UUID.fromString(decoder.decodeString()))
    }
}