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
    println("~~~~~~~~{ \u001B[33m${this.name.chunked(3).joinToString(" ")}\u001B[0m }~~~~~~~~")
    getMethod(name).invoke(kotlin.objectInstance)
}