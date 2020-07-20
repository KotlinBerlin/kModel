@file:Suppress("MemberVisibilityCanBePrivate")

package de.kotlinBerlin.kModel

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

/** A single attribute of a [ModelClass] that can be read. */
sealed class ModelAttribute<T : Any, V : Any?>(
    /** The [ModelClass] that this attribute belongs to. */
    val modelClass: ModelClass<T>,
    internal open val property: KProperty1<T, V>
) : ModelElement() {
    override val id: String get() = "${modelClass.id}.${property.name}"

    /** The simple name of this attribute. */
    val name: String get() = property.name

    /** receives the value of this attribute from the given object. */
    operator fun get(anObject: T): V = property(anObject)
}

/** An immutable attribute. */
class ImmutableModelAttribute<T : Any, V : Any?>(modelClass: ModelClass<T>, property: KProperty1<T, V>) :
    ModelAttribute<T, V>(modelClass, property)

/** A mutable attribute. */
class MutableModelAttribute<T : Any, V : Any?>(
    modelClass: ModelClass<T>,
    override val property: KMutableProperty1<T, V>
) : ModelAttribute<T, V>(modelClass, property) {
    /** modifies the value of this attribute in the given object. */
    operator fun set(anObject: T, aValue: V): Unit = property.set(anObject, aValue)
}