package kr.heartpattern.spikot.event

import kr.heartpattern.spikot.module.AbstractModule
import kr.heartpattern.spikot.module.LoadOrder
import kr.heartpattern.spikot.module.Module
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

@Module(LoadOrder.API)
object TickEventEmitter : AbstractModule() {
    override fun onEnable() {
        var tickCount = 0
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, {
            TickEvent(tickCount++).execute()
        }, 1L, 1L)
    }
}

class TickEvent(val tick: Int) : Event() {
    companion object {
        @JvmField
        val handlerList: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }
}