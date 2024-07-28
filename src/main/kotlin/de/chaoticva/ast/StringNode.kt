package de.chaoticva.ast

class StringNode(val value: String, lineStart: Int, lineEnd: Int, line: Int): ASTNode(lineStart, lineEnd, line) {
    override fun toString(): String {
        return "\"$value\""
    }
}