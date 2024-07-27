package de.chaoticva

fun main(args: Array<String>) {
    var fileArg = ""
    var outputArg = ""
    var modeArg = ""

    for (arg in args) {
        if (arg == "-f") fileArg = args[args.indexOf(arg) + 1]
        if (arg == "-o") outputArg = args[args.indexOf(arg) + 1]
        if (arg == "-m") modeArg = args[args.indexOf(arg) + 1]
    }

    if (fileArg.isEmpty() || outputArg.isEmpty() || modeArg.isEmpty()) {
        println("Please provide all required arguments: -f <script_file> -o <output_folder> and -m <execution_mode>")
        return
    }

    val lexer = Lexer(fileArg)
    val parser = Parser(lexer)
    val compiler = Compiler(parser, outputArg, modeArg)
    compiler.compile()
}