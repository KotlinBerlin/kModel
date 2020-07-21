@file:Suppress("unused")

package de.kotlinBerlin.kModel.dsl

import de.kotlinBerlin.kModel.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

/** Builder function to define [ModelClass] instances. */
inline fun model(anInitBlock: ModelBuilder.() -> Unit) {
    ModelBuilder().anInitBlock()
}

/** Builder for [ModelClass] instances. */
class ModelBuilder {

    /** Defines a new [ModelClass]. */
    inline fun <reified T : Any> modelClass(crossinline anInitBlock: ModelClassBuilder<T>.() -> Unit): ModelClass<T> =
        modelClass(T::class) { anInitBlock() }

    /** Defines a new [ModelClass]. */
    fun <T : Any> modelClass(aKotlinClass: KClass<T>, anInitBlock: ModelClassBuilder<T>.() -> Unit): ModelClass<T> {
        val tempModelClass = getOrCreateModelClass(aKotlinClass)
        ModelClassBuilder(tempModelClass).anInitBlock()
        return tempModelClass
    }
}

/** Base class for all builders that build a [ModelElement]. */
@ModelDsl
sealed class ModelElementBuilder<M : ModelElement>(internal open val modelElement: M) {

    /** Defines a property on the current [ModelElement]. */
    fun property(aKey: String, aValue: Any?) {
        modelElement.internalModelProperties[aKey] = aValue
    }
}

/** Builder for a [ModelClass]. */
class ModelClassBuilder<T : Any>(modelClass: ModelClass<T>) : ModelElementBuilder<ModelClass<T>>(modelClass) {

    /** Defines the super class of this [ModelClass]. */
    fun superClass(aSuperClass: KClass<in T>) {
        modelElement.internalSuperClass = aSuperClass
    }

    /** Defines a new [ImmutablePropertyAttribute] for this [ModelClass]. */
    @Suppress("UNCHECKED_CAST")
    fun <V : Any?> attribute(
        anAttribute: KProperty1<T, V>,
        anInitBlock: ModelAttributeBuilder<T, V>.() -> Unit = {}
    ): ModelAttribute<T, V> {
        val tempExistingAttribute =
            modelElement.internalAttributes.find { it is ImmutablePropertyAttribute && it.property == anAttribute } as? ModelAttribute<T, V>
        val tempAttribute = tempExistingAttribute ?: ImmutablePropertyAttribute(
            modelElement,
            anAttribute
        ).also { modelElement.internalAttributes.add(it) }
        val tempBuilder = ModelAttributeBuilder(tempAttribute)
        tempBuilder.anInitBlock()
        return tempAttribute
    }

    /** Defines a new [MutablePropertyAttribute] for this [ModelClass]. */
    @Suppress("UNCHECKED_CAST")
    fun <V : Any?> attribute(
        anAttribute: KMutableProperty1<T, V>,
        anInitBlock: ModelAttributeBuilder<T, V>.() -> Unit = {}
    ): ModelAttribute<T, V> {
        val tempExistingAttribute =
            modelElement.internalAttributes.find { it is MutablePropertyAttribute && it.property == anAttribute } as? ModelAttribute<T, V>
        val tempAttribute = tempExistingAttribute ?: MutablePropertyAttribute(
            modelElement,
            anAttribute
        ).also { modelElement.internalAttributes.add(it) }
        val tempBuilder = ModelAttributeBuilder(tempAttribute)
        tempBuilder.anInitBlock()
        return tempAttribute
    }

    /** Defines a new [FunctionAttribute] for this [ModelClass]. */
    @Suppress("UNCHECKED_CAST")
    fun <V : Any?> attribute(
        aFunction: KFunction1<T, V>,
        anInitBlock: ModelAttributeBuilder<T, V>.() -> Unit = {}
    ): ModelAttribute<T, V> {
        val tempExistingAttribute =
            modelElement.internalAttributes.find { it is FunctionAttribute && it.function == aFunction } as? ModelAttribute<T, V>
        val tempAttribute = tempExistingAttribute ?: FunctionAttribute(
            modelElement,
            aFunction
        ).also { modelElement.internalAttributes.add(it) }
        val tempBuilder = ModelAttributeBuilder(tempAttribute)
        tempBuilder.anInitBlock()
        return tempAttribute
    }

    /** Bidirectional 1 to ? relation. */
    @Suppress("UNCHECKED_CAST")
    fun <S : Any, T : Any, TP : T?> KMutableProperty1<S, out TP>.references(
        aSourceClass: KClass<S>,
        aTargetClass: KClass<T>,
        anInitBlock: ModelRelationBuilder<S, T, Nothing, TP, OneToRelation<S, T, TP>, ToOneRelation<T, S, TP>>.() -> Unit = {}
    ): OneToRelation<S, T, TP> {
        val tempSourceClass = getOrCreateModelClass(aSourceClass)
        val tempTargetClass = getOrCreateModelClass(aTargetClass)
        val tempExistingRelation: OneToRelation<S, T, TP>? =
            findExistingRelationFor(
                tempSourceClass,
                tempTargetClass,
                this,
                null,
                OneToRelation::class
            ) as OneToRelation<S, T, TP>?

        val tempRelation = if (tempExistingRelation == null) {
            val tempRelation = OneToRelation(this, tempSourceClass, tempTargetClass)
            val tempReverseRelation = ToOneRelation(this, tempTargetClass, tempSourceClass)

            tempRelation.internalReverseRelation = tempReverseRelation
            tempReverseRelation.internalReverseRelation = tempRelation

            tempSourceClass.internalRelations.add(tempRelation)
            tempTargetClass.internalRelations.add(tempReverseRelation)

            tempRelation
        } else {
            tempExistingRelation
        }
        ModelRelationBuilder(tempRelation, tempRelation.reverseRelation).anInitBlock()
        return tempRelation
    }

    /** Bidirectional 1 to ? relation. */
    inline fun <reified S : Any, reified T : Any, TP : T?> KMutableProperty1<S, out TP>.references(
        crossinline anInitBlock: ModelRelationBuilder<S, T, Nothing, TP, OneToRelation<S, T, TP>, ToOneRelation<T, S, TP>>.() -> Unit = {}
    ): OneToRelation<S, T, TP> = this.references(S::class, T::class) { anInitBlock() }

    /** Bidirectional 1 to 1 relation. */
    @Suppress("UNCHECKED_CAST")
    fun <S : Any, T : Any, SP : S?, TP : T?> KMutableProperty1<S, out TP>.references(
        aSourceClass: KClass<S>,
        aTargetClass: KClass<T>,
        aReverseAttribute: KMutableProperty1<T, out SP>,
        anInitBlock: ModelRelationBuilder<S, T, SP, TP, OneToOneRelation<S, T, SP, TP>, OneToOneRelation<T, S, TP, SP>>.() -> Unit = {}
    ): OneToOneRelation<S, T, SP, TP> {
        val tempSourceClass = getOrCreateModelClass(aSourceClass)
        val tempTargetClass = getOrCreateModelClass(aTargetClass)
        val tempExistingRelation: OneToOneRelation<S, T, SP, TP>? =
            findExistingRelationFor(
                tempSourceClass,
                tempTargetClass,
                this,
                aReverseAttribute,
                OneToOneRelation::class
            ) as OneToOneRelation<S, T, SP, TP>?

        val tempRelation = if (tempExistingRelation == null) {
            val tempRelation = OneToOneRelation(this, aReverseAttribute, tempSourceClass, tempTargetClass)
            val tempReverseRelation = OneToOneRelation(aReverseAttribute, this, tempTargetClass, tempSourceClass)

            tempRelation.internalReverseRelation = tempReverseRelation
            tempReverseRelation.internalReverseRelation = tempRelation

            tempSourceClass.internalRelations.add(tempRelation)
            tempTargetClass.internalRelations.add(tempReverseRelation)

            tempRelation
        } else {
            tempExistingRelation
        }
        ModelRelationBuilder(tempRelation, tempRelation.reverseRelation).anInitBlock()
        return tempRelation
    }

    /** Bidirectional 1 to 1 relation. */
    inline fun <reified S : Any, reified T : Any, SP : S?, TP : T?> KMutableProperty1<S, out TP>.references(
        aReverseAttribute: KMutableProperty1<T, out SP>,
        crossinline anInitBlock: ModelRelationBuilder<S, T, SP, TP, OneToOneRelation<S, T, SP, TP>, OneToOneRelation<T, S, TP, SP>>.() -> Unit = {}
    ): OneToOneRelation<S, T, SP, TP> = this.references(S::class, T::class, aReverseAttribute) { anInitBlock() }

    /** Bidirectional 1 to many relation. */
    @Suppress("UNCHECKED_CAST")
    fun <S : Any, T : Any, SP : S?, TP : MutableCollection<T>> KMutableProperty1<S, out TP>.references(
        aSourceClass: KClass<S>,
        aTargetClass: KClass<T>,
        aReverseAttribute: KMutableProperty1<T, out SP>,
        anInitBlock: ModelRelationBuilder<S, T, SP, TP, OneToManyRelation<S, T, SP, TP>, ManyToOneRelation<T, S, TP, SP>>.() -> Unit = {}
    ): OneToManyRelation<S, T, SP, TP> {
        val tempSourceClass = getOrCreateModelClass(aSourceClass)
        val tempTargetClass = getOrCreateModelClass(aTargetClass)
        val tempExistingRelation: OneToManyRelation<S, T, SP, TP>? =
            findExistingRelationFor(
                tempSourceClass,
                tempTargetClass,
                this,
                aReverseAttribute,
                OneToManyRelation::class
            ) as OneToManyRelation<S, T, SP, TP>?

        val tempRelation = if (tempExistingRelation == null) {
            val tempRelation = OneToManyRelation(this, aReverseAttribute, tempSourceClass, tempTargetClass)
            val tempReverseRelation = ManyToOneRelation(aReverseAttribute, this, tempTargetClass, tempSourceClass)

            tempRelation.internalReverseRelation = tempReverseRelation
            tempReverseRelation.internalReverseRelation = tempRelation

            tempSourceClass.internalRelations.add(tempRelation)
            tempTargetClass.internalRelations.add(tempReverseRelation)

            tempRelation
        } else {
            tempExistingRelation
        }
        ModelRelationBuilder(tempRelation, tempRelation.reverseRelation).anInitBlock()
        return tempRelation
    }

    /** Bidirectional 1 to many relation. */
    inline fun <reified S : Any, reified T : Any, SP : S?, TP : MutableCollection<T>> KMutableProperty1<S, out TP>.references(
        aReverseAttribute: KMutableProperty1<T, out SP>,
        crossinline anInitBlock: ModelRelationBuilder<S, T, SP, TP, OneToManyRelation<S, T, SP, TP>, ManyToOneRelation<T, S, TP, SP>>.() -> Unit = {}
    ): OneToManyRelation<S, T, SP, TP> = this.references(S::class, T::class, aReverseAttribute) { anInitBlock() }

    /** Bidirectional many to 1 relation. */
    @Suppress("UNCHECKED_CAST")
    fun <S : Any, T : Any, SP : MutableCollection<S>, TP : T?> KMutableProperty1<S, out TP>.references(
        aSourceClass: KClass<S>,
        aTargetClass: KClass<T>,
        aReverseAttribute: KMutableProperty1<T, out SP>,
        anInitBlock: ModelRelationBuilder<S, T, SP, TP, ManyToOneRelation<S, T, SP, TP>, OneToManyRelation<T, S, TP, SP>>.() -> Unit = {}
    ): ManyToOneRelation<S, T, SP, TP> {
        val tempSourceClass = getOrCreateModelClass(aSourceClass)
        val tempTargetClass = getOrCreateModelClass(aTargetClass)
        val tempExistingRelation: ManyToOneRelation<S, T, SP, TP>? =
            findExistingRelationFor(
                tempSourceClass,
                tempTargetClass,
                this,
                aReverseAttribute,
                ManyToOneRelation::class
            ) as ManyToOneRelation<S, T, SP, TP>?

        val tempRelation = if (tempExistingRelation == null) {
            val tempRelation = ManyToOneRelation(this, aReverseAttribute, tempSourceClass, tempTargetClass)
            val tempReverseRelation = OneToManyRelation(aReverseAttribute, this, tempTargetClass, tempSourceClass)

            tempRelation.internalReverseRelation = tempReverseRelation
            tempReverseRelation.internalReverseRelation = tempRelation

            tempSourceClass.internalRelations.add(tempRelation)
            tempTargetClass.internalRelations.add(tempReverseRelation)

            tempRelation
        } else {
            tempExistingRelation
        }
        ModelRelationBuilder(tempRelation, tempRelation.reverseRelation).anInitBlock()
        return tempRelation
    }

    /** Bidirectional many to 1 relation. */
    inline fun <reified S : Any, reified T : Any, SP : MutableCollection<S>, TP : T?> KMutableProperty1<S, out TP>.references(
        aReverseAttribute: KMutableProperty1<T, out SP>,
        crossinline anInitBlock: ModelRelationBuilder<S, T, SP, TP, ManyToOneRelation<S, T, SP, TP>, OneToManyRelation<T, S, TP, SP>>.() -> Unit = {}
    ): ManyToOneRelation<S, T, SP, TP> = this.references(S::class, T::class, aReverseAttribute) { anInitBlock() }
}

/** Builder for a [ModelAttribute]. */
class ModelAttributeBuilder<T : Any, V : Any?>(modelElement: ModelAttribute<T, V>) :
    ModelElementBuilder<ModelAttribute<T, V>>(modelElement)

/** Builder for a [ModelRelation] */
class ModelRelationBuilder<S : Any, T : Any, SP, TP, ME, REV>(relation: ME, private val reverseRelation: REV) :
    ModelElementBuilder<ME>(relation)
        where ME : ModelRelation<S, T, SP, TP, ME, REV>, REV : ModelRelation<T, S, TP, SP, REV, ME> {

    /** Enables to modify the reverse relation. */
    fun reverse(anInitBlock: ModelReverseRelationBuilder<T, S, TP, SP, REV, ME>.() -> Unit) {
        ModelReverseRelationBuilder(reverseRelation).anInitBlock()
    }
}

/** Builder for a reverse [ModelRelation] */
class ModelReverseRelationBuilder<S : Any, T : Any, SP, TP, ME, REV>(relation: ME) :
    ModelElementBuilder<ModelRelation<S, T, SP, TP, ME, REV>>(relation)
        where ME : ModelRelation<S, T, SP, TP, ME, REV>, REV : ModelRelation<T, S, TP, SP, REV, ME>

private fun <T : Any> getOrCreateModelClass(aKotlinClass: KClass<T>): ModelClass<T> {
    return MODEL_MANAGER.getModelClassFor(aKotlinClass).let {
        if (it == null) {
            val tempNewModelClass = ModelClass(aKotlinClass)
            MODEL_MANAGER.addModelClass(tempNewModelClass)
            tempNewModelClass
        } else {
            it
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun <S : Any, T : Any, R : ModelRelation<*, *, *, *, *, *>> findExistingRelationFor(
    aSourceClass: ModelClass<S>,
    aTargetClass: ModelClass<T>,
    aSourceProperty: KMutableProperty1<S, *>,
    aTargetProperty: KMutableProperty1<T, *>?,
    anExpectedClass: KClass<R>
): R? {
    val tempExistingRelation =
        aSourceClass.internalRelations.find { it.sourceClass == aSourceClass && it.sourceField == aSourceProperty }

    if (tempExistingRelation != null &&
        (tempExistingRelation.targetClass != aTargetClass || tempExistingRelation.targetField != aTargetProperty || !anExpectedClass.isInstance(
            tempExistingRelation
        ))
    ) throw IllegalStateException("Can not define 2 relations with same source and different targets!")

    return tempExistingRelation as R?
}

@DslMarker
@Target(AnnotationTarget.CLASS)
internal annotation class ModelDsl