package de.chaoticva.ast

class CallNode(val name: String, val args: ArrayList<ASTNode>, lineStart: Int, lineEnd: Int, line: Int): ASTNode(lineStart, lineEnd, line) {
    override fun toString(): String {
        val builder = StringBuilder()
        for (arg in args) {
            builder.append("$arg, ")
        }
        val args = if (builder.isNotEmpty()) builder.substring(0, builder.length - 2) else ""
        return "$name($args)"
    }
}