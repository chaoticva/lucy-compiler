package de.chaoticva.ast

class ReassignmentNode(val name: String, val value: ASTNode, lineStart: Int, lineEnd: Int, line: Int) : ASTNode(lineStart, lineEnd, line)