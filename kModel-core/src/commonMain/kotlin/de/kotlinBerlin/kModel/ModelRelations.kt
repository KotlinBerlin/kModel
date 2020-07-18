package de.kotlinBerlin.kModel

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

/** Defines a relation between two [ModelClass] instances. */
sealed class ModelRelation<S : Any, T : Any, SP, TP, ME, REV>(
    /** The source field, if the relation is navigable from the source side or null otherwise. */
    val sourceField: KProperty1<S, TP>?,
    /** The target field, if the relation is navigable from the target side or null otherwise. */
    val targetField: KProperty1<T, SP>?,
    /** The source [ModelClass]. */
    val sourceClass: ModelClass<S>,
    /** The target [ModelClass]. */
    val targetClass: ModelClass<T>,
) : ModelElement() where ME : ModelRelation<S, T, SP, TP, ME, REV>, REV : ModelRelation<T, S, TP, SP, REV, ME> {

    override val id: String get() = "${sourceClass.id}.${sourceField?.name} - ${targetClass.id}.${targetField?.name}"

    internal lateinit var internalReverseRelation: REV

    /** Returns the reverse relation to this one. */
    val reverseRelation: REV get() = internalReverseRelation
}

/**
 * Defines an unidirectional  1 to ? relationship.
 */
class OneToRelation<S : Any, T : Any, TP : T?>(
    sourceField: KMutableProperty1<S, out TP>?,
    sourceClass: ModelClass<S>,
    targetClass: ModelClass<T>
) : ModelRelation<S, T, Nothing, TP, OneToRelation<S, T, TP>, ToOneRelation<T, S, TP>>(
    sourceField,
    null,
    sourceClass,
    targetClass
)

/**
 * Defines an unidirectional  ? to 1 relationship.
 */
class ToOneRelation<S : Any, T : Any, SP : S?>(
    targetField: KMutableProperty1<T, out SP>?,
    sourceClass: ModelClass<S>,
    targetClass: ModelClass<T>
) : ModelRelation<S, T, SP, Nothing, ToOneRelation<S, T, SP>, OneToRelation<T, S, SP>>(
    null,
    targetField,
    sourceClass,
    targetClass
)

/**
 * Defines a bidirectional 1 to 1 relationship.
 */
class OneToOneRelation<S : Any, T : Any, SP : S?, TP : T?>(
    sourceField: KMutableProperty1<S, out TP>?,
    targetField: KMutableProperty1<T, out SP>?,
    sourceClass: ModelClass<S>,
    targetClass: ModelClass<T>
) : ModelRelation<S, T, SP, TP, OneToOneRelation<S, T, SP, TP>, OneToOneRelation<T, S, TP, SP>>(
    sourceField,
    targetField,
    sourceClass,
    targetClass
)

/** Defines a one to many relation. */
class OneToManyRelation<S : Any, T : Any, SP : S?, TP : MutableCollection<T>>(
    sourceField: KMutableProperty1<S, out TP>?,
    targetField: KProperty1<T, SP>?,
    sourceClass: ModelClass<S>,
    targetClass: ModelClass<T>
) : ModelRelation<S, T, SP, TP, OneToManyRelation<S, T, SP, TP>, ManyToOneRelation<T, S, TP, SP>>(
    sourceField,
    targetField,
    sourceClass,
    targetClass
)

/** Defines a many to one relation. */
class ManyToOneRelation<S : Any, T : Any, SP : MutableCollection<S>, TP : T?>(
    sourceField: KProperty1<S, TP>?,
    targetField: KMutableProperty1<T, out SP>?,
    sourceClass: ModelClass<S>,
    targetClass: ModelClass<T>
) : ModelRelation<S, T, SP, TP, ManyToOneRelation<S, T, SP, TP>, OneToManyRelation<T, S, TP, SP>>(
    sourceField,
    targetField,
    sourceClass,
    targetClass
)