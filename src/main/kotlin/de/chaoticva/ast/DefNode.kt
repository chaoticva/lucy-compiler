package de.chaoticva.ast

class DefNode(val name: String, val type: String, val parameters: ArrayList<ParameterNode>, val body: ScopeNode, lineStart: Int, lineEnd: Int, line: Int): ASTNode(lineStart, lineEnd, line)