@file:Suppress("UNUSED_VARIABLE")

import de.kotlinBerlin.kModel.MODEL_MANAGER
import de.kotlinBerlin.kModel.dsl.ModelAttributeBuilder
import de.kotlinBerlin.kModel.dsl.model
import kotlin.test.Test

class Test {

    @Test
    fun test1() {
        model {
            val modelClassA = this.modelClass<A> {
                property("", null)
                superClass(Any::class)
                attribute(A::t) {
                    property("0", 0)
                }
                attribute(A::t1) {
                    max(10)
                }
                attribute(A::t) {
                    property("1", 1)
                }
            }

            val modelClassB = this.modelClass<B> {
                superClass(A::class)
                B::c.references(C::b) {
                    property("1", 1)
                }
            }

            modelClass<C> {
                C::b.references(B::c) {
                    property("3", 3)
                }
            }

            this.modelClass<A> {
                property("", null)
                superClass(Any::class)
                attribute(A::t) {
                    property("2", 2)
                }
                attribute(A::t1)
                attribute(A::t) {
                    property("3", 3)
                }
            }
        }
        val modelClassForA = MODEL_MANAGER.getModelClassFor(A::class)
        val modelClassForB = MODEL_MANAGER.getModelClassFor(B::class)
        val modelClassForC = MODEL_MANAGER.getModelClassFor(C::class)
        println("TEST: " + modelClassForA?.id)
        println("TEST: " + modelClassForB?.id)
        println("TEST: " + modelClassForC?.id)
    }
}


open class A {
    val t: String = ""
    var t1: Long = 5
}

class B : A() {
    lateinit var c: C
}

class C {
    lateinit var b: B
}

fun <T : Any, V : Number?> ModelAttributeBuilder<T, V>.max(aMaxValue: Number) {
    property("maxValue", aMaxValue)
}