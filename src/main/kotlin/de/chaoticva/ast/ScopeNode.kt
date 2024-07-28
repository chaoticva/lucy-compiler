package de.chaoticva.ast

class ScopeNode(val statements: List<ASTNode> = emptyList(), lineStart: Int, lineEnd: Int, line: Int): ASTNode(lineStart, lineEnd, line)