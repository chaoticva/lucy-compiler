package de.chaoticva.ast

class IfNode(val condition: ASTNode, val body: ScopeNode, val elseBody: ScopeNode?, val elseIf: IfNode?, lineStart: Int, lineEnd: Int, line: Int): ASTNode(lineStart, lineEnd, line)