package de.chaoticva

import de.chaoticva.token.Token
import de.chaoticva.token.TokenType
import java.io.File

class Lexer(val path: String) {
    private val content = File(path).readText()
    private var pos = 0
    private var column = 0
    private var line = 1
    private var current = if (content.isNotEmpty()) content[0] else '\u0000'

    private fun consume() {
        pos++
        current = if (pos < content.length) content[pos] else '\u0000'
    }

    private fun string(): Token {
        var result = ""
        val quote = current
        column++
        consume()
        var end = 0

        while (current != quote) {
            result += current
            end++
            consume()
        }
        column++
        val col = column
        column += end
        consume()

        return Token(TokenType.STRING, result, col, end, line)
    }

    private fun number(): Token {
        var result = ""
        var end = 0

        while (current.isDigit()) {
            result += current
            end++
            consume()
        }
        val col = column
        column += end

        return Token(TokenType.NUMBER, result, col, end, line)
    }

    private fun identifier(): Token {
        var result = ""
        var end = 0

        while (current.isLetterOrDigit() || current == '_') {
            result += current
            end++
            consume()
        }
        val col = column
        column += end

        if (result in listOf("true", "false")) return Token(TokenType.BOOLEAN, result, col, col + end, line)
        val type = TokenType.entries.find { it.seq == result }
        if (type == null) return Token(TokenType.IDENTIFIER, result, col, col + end, line)

        return Token(type, result, col, col + end, line)
    }

    fun tokenize(): ArrayList<Token> {
        val tokens = arrayListOf<Token>()

        while (current != '\u0000') {
            while (current.isWhitespace()) {
                column++
                if (current in listOf('\n', '\r')) {
                    column = 0
                    line++
                }
                consume()
            }

            val type = TokenType.entries.find { it.seq == current.toString() }
            if (type != null) {
                tokens.add(Token(type, current.toString(), column, column + 1, line))
                column++
                consume()
            }

            if (current == '#') {
                while (pos < content.length && current !in listOf('\n', '\r')) consume()
            }
            if (current in listOf('"', '\'')) tokens.add(string())
            if (current.isDigit()) tokens.add(number())
            if (current.isLetter()) tokens.add(identifier())
        }

        tokens.add(Token(TokenType.EOF, "", column, column, line))

        return tokens
    }
}