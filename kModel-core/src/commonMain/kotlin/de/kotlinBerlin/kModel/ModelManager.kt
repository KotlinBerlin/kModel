package de.kotlinBerlin.kModel

import kotlin.reflect.KClass

/** The singleton [ModelManager]. */
val MODEL_MANAGER: ModelManager = ModelManager()

/** A model manager holds all defined [ModelClass] instances. */
class ModelManager internal constructor() {

    private val modelClasses = mutableMapOf<KClass<*>, ModelClass<*>>()

    /** Returns a previously defined [ModelClass] for the given [KClass] or null if non was defined. */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getModelClassFor(aKotlinClass: KClass<T>): ModelClass<T>? =
        modelClasses[aKotlinClass] as? ModelClass<T>

    internal fun addModelClass(aModelClass: ModelClass<*>) {
        modelClasses[aModelClass.kotlinClass] = aModelClass
    }
}