package de.chaoticva.ast

class IdentifierNode(val name: String, lineStart: Int, lineEnd: Int, line: Int): ASTNode(lineStart, lineEnd, line) {
    override fun toString(): String {
        return name
    }
}