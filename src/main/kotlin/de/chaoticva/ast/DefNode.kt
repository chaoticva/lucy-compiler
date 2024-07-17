package de.chaoticva.ast

class DefNode(val name: String, val parameters: ArrayList<ParameterNode>, val body: ScopeNode): ASTNode()