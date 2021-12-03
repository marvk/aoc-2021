import kotlin.reflect.KClass

fun classForNameOrNull(name: String): Class<*>? {
    return try {
        Class.forName(name)
    } catch (_: ClassNotFoundException) {
        null
    }
}

fun dayClasses() =
    IntRange(0, 24)
        .mapNotNull { classForNameOrNull("Day%02d".format(it)) }

fun Class<*>.invoke(name: String) {
    println("~~~${this.name}~~~")
    getMethod(name).invoke(kotlin.objectInstance)
}