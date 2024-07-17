package de.chaoticva

import de.chaoticva.ast.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.system.exitProcess

class Compiler(private val parser: Parser, private val output: String, private val mode: String) {
    private val file = parser.parse()
    private val builder = StringBuilder()
    private var sp = 0
    private var labels = 0
    private val functions = hashMapOf<String, DefNode>()
    private val variables = arrayListOf<Variable>()
    private val scopes = arrayListOf<Int>()

    fun compile() {
        builder.appendLine("section .text")
        builder.appendLine("global _start")
        builder.appendLine("_start:")

        file.statements.forEach { compile(it) }

        builder.appendLine("    mov rax, 60")
        builder.appendLine("    xor rdi, rdi")
        builder.appendLine("    syscall")

        val name = File(parser.lexer.path).name.replace(".lc", "")
        File("$output$name.asm").writeText(builder.toString())
        runCommand("nasm -f elf64 $output$name.asm")
        runCommand("ld $output$name.o -o $output$name")
//        File("$output$name.asm").delete()
//        File("$output$name.o").delete()
        if (mode == "cr") runCommand("$output$name")
    }

    private fun runCommand(command: String): Int {
        val process = ProcessBuilder()
            .command("sh", "-c", command)
            .start()

        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            println(line)
        }
        reader.close()

        val exitCode = process.waitFor()
        return exitCode
    }

    private fun compileVarNode(node: VarNode) {
        if (variables.find { it.name == node.name } != null) {
            System.err.println("Variable '${node.name}' already defined")
            exitProcess(1)
        }

        val variable = Variable(node.name, sp, "")
        val type = compile(node.value) as String
        variable.type = type
        variables.add(variable)
    }

    private fun compileCallNode(node: CallNode) {
        when (node.name) {
            "print" -> {
                node.args.forEach { compile(it) }
                builder.appendLine("    mov rax, 1")
                builder.appendLine("    mov rdi, 1")
                pop("rsi")
                pop("rdx")
                builder.appendLine("    syscall")
                return
            }

            "exit" -> {
                node.args.forEach { compile(it) }
                builder.appendLine("    mov rax, 60")
                pop("rdi")
                builder.appendLine("    syscall")
                return
            }
        }

        val function = functions[node.name]
        if (function == null) {
            System.err.println("Undefined function '${node.name}'")
            exitProcess(1)
        }

        if (function.parameters.size > node.args.size) {
            System.err.println("Too few arguments for function '${node.name}'")
            exitProcess(1)
        }
        if (function.parameters.size < node.args.size) {
            System.err.println("Too many arguments for function '${node.name}'")
            exitProcess(1)
        }

        beginScope()
        for (i in 0..<function.parameters.size) {
            val param = function.parameters[i]
            val arg = node.args[i]
            val variable = Variable(param.name, sp, "")
            val type = compile(arg) as String
            variable.type = type
            variables.add(variable)
        }

        function.parameters.forEach {
            run {
                variables.add(Variable(it.name, sp, it.type ?: "any"))
                sp++
            }
        }

        function.body.statements.forEach { compile(it) }

        sp--
        endScope()
    }

    private fun compileIdentifierNode(node: IdentifierNode): String {
        val variable = variables.find { it.name == node.name }
        if (variable == null) {
            System.err.println("Undefined variable '${node.name}'")
            exitProcess(1)
        }

        val pointer = (sp - variable.sp - 1) * 8
        if (pointer > 0) {
            if (variable.type == "number") {
                push("QWORD [rsp + ${pointer}]")
            }
            if (variable.type == "string") {
                push("QWORD [rsp + ${pointer}]")
                push("QWORD [rsp + ${pointer}]")
            }
            if (variable.type == "boolean") {
                push("QWORD [rsp + ${pointer}]")
            }
        } else {
            if (variable.type == "number") {
                push("QWORD [rsp]")
            }
            if (variable.type == "string") {
                push("QWORD [rsp]")
                push("QWORD [rsp]")
            }
            if (variable.type == "boolean") {
                push("QWORD [rsp]")
            }
        }

        return variable.type
    }

    private fun compileNumberNode(node: NumberNode): String {
        builder.appendLine("    mov rax, ${node.value}")
        push("rax")

        return "number"
    }

    private fun compileBooleanNode(node: BooleanNode): String {
        builder.appendLine("    mov rax, ${if (node.value) 1 else 0}")
        push("rax")

        return "boolean"
    }

    private fun compileStringNode(node: StringNode): String {
        val idx = sp

        val text = if (node.value.endsWith("\\n")) node.value.substring(0, node.value.length - 2) else node.value

        builder.appendLine("section .data")
        builder.appendLine("    string$idx db \"$text\", ${if (node.value.endsWith("\\n")) "10, " else ""} 0")
        builder.appendLine("    len_string$idx equ $ - string$idx")
        builder.appendLine("section .text")
        builder.appendLine("    mov rax, len_string$idx")
        push("rax")
        builder.appendLine("    mov rax, string$idx")
        push("rax")

        return "string"
    }

    private fun createLabel(): String {
        return "label${labels++}"
    }

    private fun compileReassignmentNode(node: ReassignmentNode): String {
        val variable = variables.find { it.name == node.name }
        if (variable == null) {
            System.err.println("Undefined variable '${node.name}'")
            exitProcess(1)
        }

        val type = compile(node.value) as String
        pop("rax")
        val pointer = (sp - variable.sp - 1) * 8
        builder.appendLine("    mov [rsp + ${pointer}], rax")

        return type
    }

    private fun compileIfPred(node: IfNode, endLabel: String) {
        if (node.elseIf != null) { // else if
            compile(node.elseIf.condition)
            pop("rax")
            val label = createLabel()
            builder.appendLine("    test rax, rax")
            builder.appendLine("    jz $label")
            compile(node.elseIf.body)
            builder.appendLine("    jmp $endLabel")
            builder.appendLine("$label:")
            compileIfPred(node.elseIf, endLabel)
        } else if (node.elseBody != null) { // else
            compile(node.elseBody)
        }
    }

    private fun compileIfNode(node: IfNode) {
        compile(node.condition)
        pop("rax")
        val label = createLabel()
        if (node.condition !is BinOpNode) {
            builder.appendLine("    test rax, rax")
            builder.appendLine("    jz $label")
        } else {
            val op = node.condition.op
            val left = node.condition.left
            val right = node.condition.right
            if (op == "==") {
                compile(left)
                compile(right)
                pop("rax")
                pop("rbx")
                builder.appendLine("    cmp rax, rbx")
                builder.appendLine("    jnz $label")
            }
            if (op == "<=") {
                compile(right)
                compile(left)
                pop("rax")
                pop("rbx")
                builder.appendLine("    cmp rax, rbx")
                builder.appendLine("    jnle $label")
            }
            if (op == ">=") {
                compile(right)
                compile(left)
                pop("rax")
                pop("rbx")
                builder.appendLine("    cmp rax, rbx")
                builder.appendLine("    jnge $label")
            }
            if (op == "<") {
                compile(right)
                compile(left)
                pop("rax")
                pop("rbx")
                builder.appendLine("    cmp rax, rbx")
                builder.appendLine("    jnl $label")
            }
            if (op == ">") {
                compile(right)
                compile(left)
                pop("rax")
                pop("rbx")
                builder.appendLine("    cmp rax, rbx")
                builder.appendLine("    jng $label")
            }
            if (op == "!=") {
                compile(right)
                compile(left)
                pop("rax")
                pop("rbx")
                builder.appendLine("    cmp rax, rbx")
                builder.appendLine("    je $label")
            }
        }
        compile(node.body)
        if (node.elseIf != null || node.elseBody != null) {
            val endLabel = createLabel()
            builder.appendLine("    jmp $endLabel")
            builder.appendLine("$label:")
            compileIfPred(node, endLabel)
            builder.appendLine("$endLabel:")
        } else {
            builder.appendLine("$label:")
        }
    }

    private fun beginScope() {
        scopes.add(variables.size)
    }

    private fun endScope() {
        val popCount = variables.size - scopes.last()
        if (popCount != 0) {
            builder.appendLine("    add rsp, ${popCount * 8}")
        }
        sp -= popCount
        for (i in 0..<popCount) {
            variables.removeLast()
        }
        scopes.removeLast()
    }

    private fun compileScopeNode(node: ScopeNode) {
        beginScope()

        node.statements.forEach { compile(it) }

        endScope()
    }

    private fun compileBinOpNode(node: BinOpNode): String {
        if (node.op == "+") {
            compile(node.right)
            compile(node.left)
            pop("rax")
            pop("rbx")
            builder.appendLine("    add rax, rbx")
            push("rax")
        }
        if (node.op == "-") {
            compile(node.right)
            compile(node.left)
            pop("rax")
            pop("rbx")
            builder.appendLine("    sub rax, rbx")
            push("rax")
        }
        if (node.op == "*") {
            compile(node.right)
            compile(node.left)
            pop("rax")
            pop("rbx")
            builder.appendLine("    mul rbx")
            push("rax")
        }
        if (node.op == "/") {
            compile(node.right)
            compile(node.left)
            pop("rax")
            pop("rbx")
            builder.appendLine("    div rbx")
            push("rax")
        }

        return "number"
    }

    private fun compileDefNode(node: DefNode): String {
        functions[node.name] = node
        return "void"
    }

    private fun compileParameterNode(node: ParameterNode) {
        val variable = Variable(node.name, sp, node.type ?: "any")
        variables.add(variable)
    }

    private fun compile(node: ASTNode): Any? {
        when (node) {
            is IdentifierNode -> return compileIdentifierNode(node)
            is CallNode -> return compileCallNode(node)
            is NumberNode -> return compileNumberNode(node)
            is StringNode -> return compileStringNode(node)
            is BooleanNode -> return compileBooleanNode(node)
            is ReassignmentNode -> return compileReassignmentNode(node)
            is DefNode -> return compileDefNode(node)
            is BinOpNode -> return compileBinOpNode(node)
            is ScopeNode -> compileScopeNode(node)
            is VarNode -> compileVarNode(node)
            is IfNode -> compileIfNode(node)
            is ParameterNode -> compileParameterNode(node)
        }

        return null
    }

    private fun push(register: String) {
        builder.appendLine("    push $register")
        sp++
    }

    private fun pop(register: String) {
        builder.appendLine("    pop $register")
        sp--
    }
}