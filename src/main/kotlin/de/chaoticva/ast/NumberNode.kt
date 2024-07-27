package de.chaoticva.ast

class NumberNode(val value: Int, lineStart: Int, lineEnd: Int, line: Int): ASTNode(lineStart, lineEnd, line) {
    override fun toString(): String {
        return value.toString()
    }
}