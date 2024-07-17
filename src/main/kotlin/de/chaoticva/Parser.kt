package de.chaoticva

import de.chaoticva.ast.*
import de.chaoticva.token.TokenType

class Parser(val lexer: Lexer) {
    private val tokens = lexer.tokenize()
    private var pos = 0
    private var current = tokens[pos]

    private fun consume() {
        pos++
        if (pos < tokens.size) current = tokens[pos]
    }

    private fun tryConsume(type: TokenType) {
        if (current.type == type) consume()
        else error("Expected $type but found ${current.type}")
    }

    private fun factor(): ASTNode {
        var node = ASTNode()

        when (current.type) {
            TokenType.NUMBER -> {
                node = NumberNode(current.value.toInt())
                consume()
            }

            TokenType.BOOLEAN -> {
                node = BooleanNode(current.value.toBoolean())
                consume()
            }

            TokenType.STRING -> {
                node = StringNode(current.value)
                consume()
            }

            TokenType.OPEN_PAREN -> {
                consume()
                node = expr()
                tryConsume(TokenType.CLOSE_PAREN)
            }

            TokenType.IDENTIFIER -> {
                node = identifier()
            }

            else -> {}
        }

        return node
    }

    private fun identifier(): ASTNode {
        val name = current.value
        tryConsume(TokenType.IDENTIFIER)

        if (current.type == TokenType.OPEN_PAREN) {
            consume()
            val args = arrayListOf<ASTNode>()
            if (current.type != TokenType.CLOSE_PAREN) {
                args.add(expr())

                while (current.type == TokenType.COMMA) {
                    consume()
                    args.add(expr())
                }
            }
            tryConsume(TokenType.CLOSE_PAREN)

            return CallNode(name, args)
        }

        if (current.type == TokenType.EQUALS) {
            consume()
            if (current.type == TokenType.EQUALS) {
                consume()
                return BinOpNode("==", IdentifierNode(name), expr())
            }
            val value = expr()
            return ReassignmentNode(name, value)
        }

        return IdentifierNode(name)
    }

    private fun op(): ASTNode {
        var node = factor()

        while (current.type in listOf(TokenType.BANG, TokenType.EQUALS, TokenType.OPEN_ANGLE, TokenType.CLOSE_ANGLE)) {
            var operator = current.value
            consume()
            if (current.type == TokenType.EQUALS){
                operator += "="
                consume()
            }
            node = BinOpNode(operator, node, factor())
        }

        return node
    }

    private fun term(): ASTNode {
        var node = op()

        while (current.type in listOf(TokenType.ASTERISK, TokenType.F_SLASH)) {
            val operator = current.value
            consume()
            node = BinOpNode(operator, node, op())
        }

        return node
    }

    private fun expr(): ASTNode {
        var node = term()

        while (current.type in listOf(TokenType.PLUS, TokenType.MINUS)) {
            val operator = current.value
            consume()
            node = BinOpNode(operator, node, term())
        }

        return node
    }

    private fun varNode(): VarNode {
        tryConsume(TokenType.VAR)
        val name = current.value
        tryConsume(TokenType.IDENTIFIER)
        tryConsume(TokenType.EQUALS)
        val value = expr()
        tryConsume(TokenType.SEMICOLON)
        return VarNode(name, value)
    }

    private fun ifNode(): IfNode {
        tryConsume(TokenType.IF)
        tryConsume(TokenType.OPEN_PAREN)
        val condition = expr()
        tryConsume(TokenType.CLOSE_PAREN)
        val thenBody = arrayListOf<ASTNode>()

        tryConsume(TokenType.OPEN_BRACE)

        body(thenBody, TokenType.CLOSE_BRACE)

        tryConsume(TokenType.CLOSE_BRACE)

        if (current.type == TokenType.ELSE) {
            consume()
            if (current.type == TokenType.IF) {
                return IfNode(condition, ScopeNode(thenBody), null, ifNode())
            } else {
                val elseBody = arrayListOf<ASTNode>()
                tryConsume(TokenType.OPEN_BRACE)

                body(elseBody, TokenType.CLOSE_BRACE)

                tryConsume(TokenType.CLOSE_BRACE)
                return IfNode(condition, ScopeNode(thenBody), ScopeNode(elseBody), null)
            }
        }

        return IfNode(condition, ScopeNode(thenBody), null, null)
    }

    private fun scope(): ScopeNode {
        consume()
        val statements = arrayListOf<ASTNode>()

        body(statements, TokenType.CLOSE_BRACE)

        tryConsume(TokenType.CLOSE_BRACE)

        return ScopeNode(statements)
    }

    private fun defNode(): DefNode {
        consume()
        val name = current.value
        tryConsume(TokenType.IDENTIFIER)
        tryConsume(TokenType.OPEN_PAREN)

        val parameters = arrayListOf<ParameterNode>()
        if (current.type != TokenType.CLOSE_PAREN) {
            var paramName = current.value
            tryConsume(TokenType.IDENTIFIER)
            if (current.type == TokenType.COLON) {
                consume()
                val type = current.value
                tryConsume(TokenType.IDENTIFIER)
                parameters.add(ParameterNode(paramName, type))
            } else {
                parameters.add(ParameterNode(paramName, "any"))
            }
            while (current.type == TokenType.COMMA) {
                consume()
                paramName = current.value
                tryConsume(TokenType.IDENTIFIER)
                if (current.type == TokenType.COLON) {
                    consume()
                    val type = current.value
                    tryConsume(TokenType.IDENTIFIER)
                    parameters.add(ParameterNode(paramName, type))
                } else {
                    parameters.add(ParameterNode(paramName, "any"))
                }
            }
        }

        tryConsume(TokenType.CLOSE_PAREN)
        tryConsume(TokenType.OPEN_BRACE)

        val body = arrayListOf<ASTNode>()
        body(body, TokenType.CLOSE_BRACE)
        tryConsume(TokenType.CLOSE_BRACE)

        return DefNode(name, parameters, ScopeNode(body))
    }

    private fun body(statements: ArrayList<ASTNode>, type: TokenType) {
        while (current.type != type) {
            when (current.type) {
                TokenType.VAR -> statements.add(varNode())
                TokenType.DEF -> statements.add(defNode())
                TokenType.IDENTIFIER -> {
                    statements.add(identifier())
                    tryConsume(TokenType.SEMICOLON)
                }
                TokenType.IF -> statements.add(ifNode())
                TokenType.OPEN_BRACE -> statements.add(scope())
                else -> {}
            }
        }
    }

    fun parse(): FileNode {
        val statements = arrayListOf<ASTNode>()

        body(statements, TokenType.EOF)

        return FileNode(statements)
    }
}