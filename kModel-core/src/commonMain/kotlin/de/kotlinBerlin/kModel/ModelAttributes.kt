@file:Suppress("MemberVisibilityCanBePrivate")

package de.kotlinBerlin.kModel

import kotlin.reflect.KFunction1
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

/** A single attribute of a [ModelClass] that can be read. */
sealed class ModelAttribute<T : Any, V : Any?>(
    /** The [ModelClass] that this attribute belongs to. */
    val modelClass: ModelClass<T>,
    /** The property wrapped by this attribute. */
    val name: String,
    private val getter: (T) -> V
) : ModelElement() {
    override val id: String get() = "${modelClass.id}.${name}"

    /** receives the value of this attribute from the given object. */
    operator fun get(anObject: T): V = getter(anObject)
}

/** An immutable attribute that represents a [KProperty1] */
class ImmutablePropertyAttribute<T : Any, V : Any?>(
    modelClass: ModelClass<T>,
    /** The property wrapped by this attribute. */
    val property: KProperty1<T, V>
) :
    ModelAttribute<T, V>(modelClass, property.name, property::get)

/** A mutable attribute that represents a [KMutableProperty1] */
class MutablePropertyAttribute<T : Any, V : Any?>(
    modelClass: ModelClass<T>,
    /** The mutable property wrapped by this attribute. */
    val property: KMutableProperty1<T, V>
) : ModelAttribute<T, V>(modelClass, property.name, property::get) {
    /** modifies the value of this attribute in the given object. */
    operator fun set(anObject: T, aValue: V): Unit = property.set(anObject, aValue)
}

/** A immutable attribute that represents a function result */
class FunctionAttribute<T : Any, V : Any?>(
    modelClass: ModelClass<T>,
    /** The function wrapped by this attribute. */
    val function: KFunction1<T, V>
) : ModelAttribute<T, V>(modelClass, function.name, function)