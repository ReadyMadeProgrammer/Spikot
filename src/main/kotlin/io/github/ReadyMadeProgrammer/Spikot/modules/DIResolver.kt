package io.github.ReadyMadeProgrammer.Spikot.modules

import io.github.ReadyMadeProgrammer.Spikot.ServerVersion
import io.github.ReadyMadeProgrammer.Spikot.Version
import io.github.ReadyMadeProgrammer.Spikot.logger
import io.github.ReadyMadeProgrammer.Spikot.utils.version
import org.koin.dsl.module.Module
import org.koin.dsl.module.applicationContext
import org.koin.standalone.StandAloneContext.startKoin
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmName

/**
 * This make us use koin without defining applicationContext
 * DIResolver scan class annotated with Service or Contract and define applicationContext itself.
 */
object DIResolver {
    private data class ServiceWrapper(val klass: KClass<*>, val name: String, val singleton: Boolean)

    private var module: Module? = null
    private val externalModule = mutableSetOf<Module>()
    internal val moduleInstances = mutableSetOf<io.github.ReadyMadeProgrammer.Spikot.modules.Module>()
    internal val modules = mutableSetOf<KClass<out io.github.ReadyMadeProgrammer.Spikot.modules.Module>>()
    internal val feature = mutableSetOf<String>()
    internal fun load() {
        logger.info { "DI loading start" }
        val reflections = Reflections()
        val contracts = Reflections().getTypesAnnotatedWith(Contract::class.java).map { it.kotlin }
        val services = mutableMapOf<String, ServiceWrapper>()
        val standalone = mutableSetOf<ServiceWrapper>()
        reflections.getTypesAnnotatedWith(Service::class.java).map { it.kotlin }.filter {
            it.isSubclassOf(Component::class)
        }.filter {
            val feature = it.findAnnotation<Feature>()
            if (feature != null) {
                this.feature.contains(feature.feature) == feature.enable
            } else {
                true
            }
        }.filter {
            it.annotations.filter { it is Adapter }
                    .any { version.match((it as Adapter).platform, it.version) == ServerVersion.Result.COMPACT }
        }.forEach { k ->
            val singleton = k.findAnnotation<Singleton>() != null
            val name = k.findAnnotation<Service>()!!.name
            val contract = contracts.find { it.isSuperclassOf(k) }
            val serviceWrapper = ServiceWrapper(k, name, singleton)
            if (contract != null) {
                val registered = services[contract.jvmName]
                if (registered != null) {
                    val registeredAdapter = registered.klass.findAnnotation<Adapter>()
                    val adapter = k.findAnnotation<Adapter>()
                    if (adapter == null) {
                        standalone.add(serviceWrapper)
                    } else {
                        if (registeredAdapter == null) {
                            standalone.add(registered)
                            services[contract.jvmName] = serviceWrapper
                        } else {
                            var registeredBest = Version(registeredAdapter.version[0])
                            registeredAdapter.version.map { Version(it) }.forEach {
                                if (version.version.closer(registeredBest, it) < 0) {
                                    registeredBest = it
                                }
                            }
                            var best = Version(adapter.version[0])
                            adapter.version.map { Version(it) }.forEach {
                                if (version.version.closer(best, it) < 0) {
                                    best = it
                                }
                            }
                            if (version.version.closer(best, registeredBest) > 0) {
                                standalone.add(registered)
                                services[contract.jvmName] = serviceWrapper
                            } else {
                                standalone.add(serviceWrapper)
                            }
                        }
                    }
                } else {
                    services[contract.jvmName] = serviceWrapper
                }
            } else {
                standalone.add(serviceWrapper)
            }
        }
        modules.addAll(reflections.getSubTypesOf(io.github.ReadyMadeProgrammer.Spikot.modules.Module::class.java).map { it.kotlin })
        externalModule.addAll(reflections.getSubTypesOf(ModuleConfig::class.java)
                .map { it.kotlin.createInstance().module })
        logger.info { "Load ${contracts.size} contracts, ${services.size + standalone.size} services, ${modules.size} modules, ${externalModule.size} module configs" }
        module = applicationContext {
            contracts.forEach { k ->
                val service = services[k.jvmName]
                if (service == null) {
                    logger.warn { "Cannot find service match to contract: ${k.jvmName}" }
                    return@forEach
                }
                if (service.singleton) {
                    bean(name = name) { k.createInstance() } bind k
                } else {
                    factory(name = name) { k.createInstance() } bind k
                }
            }
            standalone.forEach { s ->
                if (s.singleton) {
                    bean(name = s.name) { s.klass.createInstance() }
                } else {
                    factory(name = name) { s.klass.createInstance() }
                }
            }
        }
        val all = mutableListOf(module!!)
        all.addAll(externalModule)
        startKoin(all)
    }
}