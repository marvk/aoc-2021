fun main(args: Array<String>) {
    dayClasses()[args.first().toInt() - 1].invoke("run")
}