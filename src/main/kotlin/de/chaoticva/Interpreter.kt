package de.chaoticva

import de.chaoticva.ast.*
import kotlin.system.exitProcess

class Interpreter(private val file: FileNode) {
    private val variables = hashMapOf<String, Any>()

    fun interpret() {
        for (statement in file.statements) {
            interpret(statement)
        }
    }

    private fun interpretVarNode(node: VarNode) {
        variables[node.name] = interpret(node.value)!!
    }

    private fun interpretStringNode(node: StringNode): String {
        return node.value
    }

    private fun interpretNumberNode(node: NumberNode): Int {
        return node.value
    }

    private fun interpretBooleanNode(node: BooleanNode): Boolean {
        return node.value
    }

    private fun interpretIdentifierNode(node: IdentifierNode): Any {
        return variables[node.name]?: error("Undefined variable: ${node.name}")
    }

    private fun interpretPrintFunction(node: CallNode) {
        if (node.args.size > 1) error("Too many arguments for print function")
        if (node.args.size < 1) error("Too few arguments for print function")

        println(interpret(node.args[0]))
    }

    private fun interpretExitFunction(node: CallNode) {
        if (node.args.size > 1) error("Too many arguments for print function")
        if (node.args.size < 1) error("Too few arguments for print function")

        exitProcess(interpret(node.args[0]) as Int)
    }

    private fun interpretCallNode(node: CallNode): Any? {
        if (node.name == "print") interpretPrintFunction(node)
        if (node.name == "exit") interpretExitFunction(node)
        return null
    }

    private fun interpretReassignmentNode(node: ReassignmentNode) {
        if (!variables.containsKey(node.name)) error("Undefined variable: ${node.name}")
        val value = interpret(node.value)!!
        variables[node.name] = value
    }

    private fun interpretIfNode(node: IfNode): Any? {
        val conditionMet = interpret(node.condition) as Boolean

        if (conditionMet) {
            node.body.statements.forEach{ interpret(it) }
        } else {
            if (node.elseIf != null) {
                interpretIfNode(node.elseIf)
            } else {
                if (node.elseBody!= null)
                    node.elseBody.statements.forEach { interpret(it) }
            }
        }

        return null
    }

    private fun interpretBinOpNode(node: BinOpNode): Any? {
        val left = interpret(node.left) as Int
        val right = interpret(node.right) as Int

        return when (node.op) {
            "+" -> left + right
            "-" -> left - right
            "*" -> left * right
            "/" -> left / right
            else -> error("Invalid operator: ${node.op}")
        }
    }

    private fun interpret(node: ASTNode): Any? {
        when (node) {
            is VarNode -> interpretVarNode(node)
            is StringNode -> return interpretStringNode(node)
            is NumberNode -> return interpretNumberNode(node)
            is BooleanNode -> return interpretBooleanNode(node)
            is IdentifierNode -> return interpretIdentifierNode(node)
            is ReassignmentNode -> return interpretReassignmentNode(node)
            is BinOpNode -> return interpretBinOpNode(node)
            is CallNode -> return interpretCallNode(node)
            is IfNode -> return interpretIfNode(node)
        }

        return Any()
    }
}