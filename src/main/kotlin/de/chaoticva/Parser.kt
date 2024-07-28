package de.chaoticva

import de.chaoticva.ast.*
import de.chaoticva.token.TokenType

@Suppress("NAME_SHADOWING")
class Parser(val lexer: Lexer) {
    val tokens = lexer.tokenize()
    private var pos = 0
    private var current = tokens[pos]

    private fun consume() {
        pos++
        if (pos < tokens.size) current = tokens[pos]
    }

    private fun tryConsume(type: TokenType) {
        if (current.type == type) consume()
        else {
            ErrorHighlighter.highlight(current, tokens[pos - 1], tokens[pos + 1], lexer.path, type.seq, "Expected $type but found ${current.type} in line ${tokens[pos - 1].line}")
        }
    }

    private fun factor(): ASTNode {
        var node: ASTNode? = null

        when (current.type) {
            TokenType.NUMBER -> {
                node = NumberNode(current.value.toInt(), current.lineStart, current.lineEnd, current.line)
                consume()
            }

            TokenType.BOOLEAN -> {
                node = BooleanNode(current.value.toBoolean(), current.lineStart, current.lineEnd, current.line)
                consume()
            }

            TokenType.STRING -> {
                node = StringNode(current.value, current.lineStart, current.lineEnd, current.line)
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

        return node!!
    }

    private fun identifier(): ASTNode {
        val name = current.value
        val lineStart = current.lineStart
        val line = current.line
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
            val lineEnd = current.lineEnd

            return CallNode(name, args, lineStart, lineEnd, line)
        }
        val lineEnd = current.lineEnd

        if (current.type == TokenType.EQUALS) {
            consume()
            if (current.type == TokenType.EQUALS) {
                consume()
                val expr = expr()
                return BinOpNode("==", IdentifierNode(name, lineStart, lineEnd, line), expr, lineStart, expr.lineEnd, expr.line)
            }
            val value = expr()
            return ReassignmentNode(name, value, lineStart, value.lineEnd, value.line)
        }

        return IdentifierNode(name, lineStart, lineEnd, line)
    }

    private fun op(): ASTNode {
        var node = factor()

        while (current.type in listOf(TokenType.BANG, TokenType.EQUALS, TokenType.OPEN_ANGLE, TokenType.CLOSE_ANGLE)) {
            var operator = current.value
            consume()
            if (current.type == TokenType.EQUALS) {
                operator += "="
                consume()
            }
            val factor = factor()
            node = BinOpNode(operator, node, factor, node.lineStart, factor.lineEnd, factor.line)
        }

        return node
    }

    private fun term(): ASTNode {
        var node = op()

        while (current.type in listOf(TokenType.ASTERISK, TokenType.F_SLASH)) {
            val operator = current.value
            consume()
            val op = op()
            node = BinOpNode(operator, node, op, node.lineStart, op.lineEnd, op.line)
        }

        return node
    }

    private fun expr(): ASTNode {
        var node = term()

        while (current.type in listOf(TokenType.PLUS, TokenType.MINUS)) {
            val operator = current.value
            consume()
            val term = term()
            node = BinOpNode(operator, node, term, node.lineStart, term.lineEnd, term.line)
        }

        return node
    }

    private fun varNode(): VarNode {
        var const = false
        val lineStart = current.lineStart
        val line = current.line

        if (current.type == TokenType.CONST) {
            consume()
            const = true
        }

        tryConsume(TokenType.VAR)
        val type = current.value
        tryConsume(TokenType.IDENTIFIER)
        val name = current.value
        tryConsume(TokenType.IDENTIFIER)
        tryConsume(TokenType.EQUALS)
        val value = expr()
        val lineEnd = current.lineEnd
        tryConsume(TokenType.SEMICOLON)
        return VarNode(name, type, value, const, lineStart, lineEnd, line)
    }

    private fun ifNode(): IfNode {
        val lineStart = current.lineStart
        val line = current.line
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
                val ifNode = ifNode()
                return IfNode(condition, ScopeNode(thenBody, lineStart, thenBody.last().lineEnd, line), null, ifNode, lineStart, ifNode.lineEnd, ifNode.line)
            } else {
                val elseBody = arrayListOf<ASTNode>()
                tryConsume(TokenType.OPEN_BRACE)

                body(elseBody, TokenType.CLOSE_BRACE)

                val lineEnd = current.lineEnd
                tryConsume(TokenType.CLOSE_BRACE)
                return IfNode(condition, ScopeNode(thenBody, lineStart, thenBody.last().lineEnd, line), ScopeNode(elseBody, lineStart, elseBody.last().lineEnd, line), null, lineStart, lineEnd, line)
            }
        }
        val lineEnd = current.lineEnd

        return IfNode(condition, ScopeNode(thenBody, lineStart, thenBody.last().lineEnd, line), null, null, lineStart, lineEnd, line)
    }

    private fun scope(): ScopeNode {
        val lineStart = current.lineStart
        val line = current.line
        consume()
        val statements = arrayListOf<ASTNode>()

        body(statements, TokenType.CLOSE_BRACE)

        val lineEnd = current.lineEnd
        tryConsume(TokenType.CLOSE_BRACE)

        return ScopeNode(statements, lineStart, lineEnd, line)
    }

    private fun defNode(): DefNode {
        val lineStart = current.lineStart
        val line = current.line
        consume()
        val type = current.value
        consume()
        val name = current.value
        tryConsume(TokenType.IDENTIFIER)
        tryConsume(TokenType.OPEN_PAREN)

        val parameters = arrayListOf<ParameterNode>()
        if (current.type != TokenType.CLOSE_PAREN) {
            val lineStart = current.lineStart
            val lineEnd = current.lineEnd
            val type = current.value
            tryConsume(TokenType.IDENTIFIER)
            var paramName = current.value
            tryConsume(TokenType.IDENTIFIER)
            parameters.add(ParameterNode(paramName, type, lineStart, lineEnd, line))
            while (current.type == TokenType.COMMA) {
                consume()
                val lineStart = current.lineStart
                val lineEnd = current.lineEnd
                val type = current.value
                tryConsume(TokenType.IDENTIFIER)
                paramName = current.value
                tryConsume(TokenType.IDENTIFIER)
                parameters.add(ParameterNode(paramName, type, lineStart, lineEnd, line))
            }
        }

        tryConsume(TokenType.CLOSE_PAREN)
        tryConsume(TokenType.OPEN_BRACE)

        val body = arrayListOf<ASTNode>()
        val bodyLineStart = current.lineStart
        body(body, TokenType.CLOSE_BRACE)
        val lineEnd = current.lineEnd
        tryConsume(TokenType.CLOSE_BRACE)

        return DefNode(name, type, parameters, ScopeNode(body, bodyLineStart, if (body.isNotEmpty()) body.last().lineEnd else lineEnd, line), lineStart, lineEnd, line)
    }

    private fun body(statements: ArrayList<ASTNode>, type: TokenType) {
        while (current.type != type) {
            when (current.type) {
                TokenType.VAR, TokenType.CONST -> statements.add(varNode())
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