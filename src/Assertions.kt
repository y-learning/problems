import fn.result.Result

private const val TAG = "Assertion error:"

private fun boolFailureMsg(s: String): String = "$TAG condition should be $s"

private fun intFailureMsg(value: Int, s: String): String =
    "$TAG value $value should be $s"

fun <T> assertCondition(value: T, failMsg: String, f: (T) -> Boolean):
        Result<T> = Result.of(f, value, failMsg)

fun <T> assertCondition(value: T, f: (T) -> Boolean): Result<T> =
    assertCondition(value, boolFailureMsg("true"), f)

fun assertTrue(condition: Boolean, failMsg: String = boolFailureMsg("true")):
        Result<Boolean> = assertCondition(condition, failMsg) { it }

fun assertFalse(condition: Boolean, failMsg: String = boolFailureMsg("false")):
        Result<Boolean> = assertCondition(condition, failMsg) { !it }

fun <T> assertNotNull(t: T, failMsg: String): Result<T> =
    assertCondition(t, failMsg) { it != null }

fun <T> assertNotNull(t: T): Result<T> =
    assertCondition(t, boolFailureMsg("not be null")) { it != null }

fun assertPositive(value: Int,
                   failMsg: String = intFailureMsg(value, "positive")):
        Result<Int> = assertCondition(value, failMsg) { it > 0 }

fun assertInRange(value: Int, min: Int, max: Int): Result<Int> =
    assertCondition(value, intFailureMsg(value, "> $min and <$max")) {
        it in min until max
    }

fun assertPositiveOrZero(value: Int,
                         failMsg: String = intFailureMsg(value, ">= 0")):
        Result<Int> = assertCondition(value, failMsg) { it >= 0 }


//TODO: Reimplement these two functions
//fun <A : Any> assertType(element: A, clazz: Class<*>, failMsg: String):
//        Result<A> = assertCondition(element, failMsg) { it.javaClass == clazz }
//
//fun <A : Any> assertType(element: A, clazz: Class<*>): Result<A> =
//    assertType(
//        element,
//        clazz,
//        "Wrong type: ${element.javaClass}, expected: ${clazz.name}")

fun assertValidName(name: String,
                    failMsg: String = "Invalid name"): Result<String> =
    assertCondition(name, failMsg) { _name: String ->
        _name[0].toInt() in 65..91
    }

data class Person(val id: Int, val firstName: String, val lastName: String) {
    companion object {
        fun of(id: Int, firstName: String, lastName: String) =
            assertPositive(id)
                .flatMap { _id: Int ->
                    assertValidName(firstName)
                        .flatMap { _firstName: String ->
                            assertValidName(lastName)
                                .map { _lastName: String ->
                                    Person(_id, _firstName, _lastName)
                                }
                        }
                }
    }
}
