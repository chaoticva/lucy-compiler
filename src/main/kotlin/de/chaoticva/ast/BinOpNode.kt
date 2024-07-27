package de.chaoticva.ast

class BinOpNode(val op: String, val left: ASTNode, val right: ASTNode, lineStart: Int, lineEnd: Int, line: Int): ASTNode(lineStart, lineEnd, line)
