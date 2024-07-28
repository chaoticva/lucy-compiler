package de.chaoticva.ast

class VarNode(val name: String, val type: String, val value: ASTNode, val const: Boolean = false, override val lineStart: Int, override val lineEnd: Int, line: Int): ASTNode(lineStart, lineEnd, line) {
    override fun toString(): String {
        return "${if (const) "const " else ""}var $type $name = $value"
    }
}