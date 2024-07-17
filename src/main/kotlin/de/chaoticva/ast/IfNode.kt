package de.chaoticva.ast

class IfNode(val condition: ASTNode, val body: ScopeNode, val elseBody: ScopeNode?, val elseIf: IfNode?): ASTNode()