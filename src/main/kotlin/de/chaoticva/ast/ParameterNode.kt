package de.chaoticva.ast

class ParameterNode(val name: String, val type: String?, lineStart: Int, lineEnd: Int, line: Int) : ASTNode(lineStart, lineEnd, line) {
    override fun toString(): String {
        return name
    }
}