package io.github.ReadyMadeProgrammer.Spikot.persistence.datacontroller

import io.github.ReadyMadeProgrammer.Spikot.persistence.gson.gson
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import kotlin.reflect.KClass

open class SIngletonDataController<V : Any> : DataController<Unit, V> {
    private lateinit var value: V
    private lateinit var file: File
    private lateinit var valueType: KClass<*>

    final override fun initialize(directory: File, valueType: KClass<*>) {
        file = File(directory, valueType.qualifiedName)
        try {
            file.createNewFile()
            val reader = FileReader(file)
            value = gson.fromJson(reader, valueType.java) as V
            reader.close()
            this.valueType = valueType
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    final override fun destroy() {
        try {
            val writer = FileWriter(file)
            gson.toJson(value, valueType.java, writer)
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun get(): V {
        return value
    }

    fun set(value: V) {
        this.value = value
    }

    final override val size: Int = 1
    final override fun containsKey(key: Unit): Boolean = this::value.isInitialized

    final override fun containsValue(value: V): Boolean = this.value == value

    final override fun get(key: Unit): V = value

    final override fun isEmpty(): Boolean = this::value.isInitialized

    final override val entries: MutableSet<MutableMap.MutableEntry<Unit, V>> = mutableSetOf(AbstractMap.SimpleEntry(Unit, value))

    final override val keys: MutableSet<Unit> = mutableSetOf(Unit)

    final override val values: MutableCollection<V> = mutableSetOf(value)
}