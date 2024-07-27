package de.chaoticva.ast

class BooleanNode(val value: Boolean, lineStart: Int, lineEnd: Int, line: Int): ASTNode(lineStart, lineEnd, line)