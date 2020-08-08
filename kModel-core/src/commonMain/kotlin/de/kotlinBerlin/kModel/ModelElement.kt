@file:Suppress("MemberVisibilityCanBePrivate")

package de.kotlinBerlin.kModel

/**
 * A model element can hold [modelProperties] and has an [id] that identifies it.
 */
abstract class ModelElement {

    /** Identifies this [ModelElement]. */
    abstract val id: String

    /** The model properties of this [ModelElement] */
    val modelProperties: MutableMap<String, Any?> get() = mutableMapOf()

    /** Two [ModelElement] instances are equal, if their [id] properties are equal. */
    override fun equals(other: Any?): Boolean = other is ModelElement && id == other.id

    /** The hashCode of the [id]. */
    override fun hashCode(): Int = id.hashCode()
}