fun String.colored(code: Byte): String {
    return "\u001B[${code}m$this\u001B[0m"
}