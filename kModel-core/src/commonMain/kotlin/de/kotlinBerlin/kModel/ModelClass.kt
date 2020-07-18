@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package de.kotlinBerlin.kModel

import kotlin.reflect.KClass

/**
 * Represents a [kotlinClass] instance with its [attributes], its [superClass] and its [relations] to other
 * [ModelClass] instances.
 */
class ModelClass<T : Any>(
    /** The [KClass] instance which this [ModelClass] represents. */
    val kotlinClass: KClass<T>
) : ModelElement() {

    internal var internalSuperClass: KClass<in T> = Any::class
    internal val internalAttributes = mutableSetOf<ModelAttribute<T, *>>()
    internal val internalRelations = mutableSetOf<ModelRelation<T, *, *, *, *, *>>()

    override val id: String get() = kotlinClass.simpleName!!

    /** The attributes of this [ModelClass]. */
    val attributes: Set<ModelAttribute<T, *>> get() = internalAttributes

    /** The super class of this [ModelClass]. */
    @Suppress("UNCHECKED_CAST")
    val superClass: ModelClass<in T>?
        get() = MODEL_MANAGER.getModelClassFor(internalSuperClass as KClass<Any>)

    /** The relations of this [ModelClass]. */
    val relations: Set<ModelRelation<T, *, *, *, *, *>> get() = internalRelations

    init {
        if (kotlinClass.simpleName == null) throw IllegalArgumentException("Can not create ModelClass of type $kotlinClass as it has no simple name available.")
    }
}