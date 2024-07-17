package de.chaoticva

import de.chaoticva.token.Token
import de.chaoticva.token.TokenType
import java.io.File

class Lexer(val path: String) {
    private val content = File(path).readText()
    private var pos = 0
    private var current = if (content.isNotEmpty()) content[0] else '\u0000'

    private fun consume() {
        pos++
        current = if (pos < content.length) content[pos] else '\u0000'
    }

    private fun string(): Token {
        var result = ""
        val quote = current
        consume()

        while (current != quote) {
            result += current
            consume()
        }
        consume()

        return Token(TokenType.STRING, result)
    }

    private fun number(): Token {
        var result = ""

        while (current.isDigit()) {
            result += current
            consume()
        }

        return Token(TokenType.NUMBER, result)
    }

    private fun identifier(): Token {
        var result = ""

        while (current.isLetterOrDigit() || current == '_') {
            result += current
            consume()
        }

        if (result in listOf("true", "false")) return Token(TokenType.BOOLEAN, result)
        val type = TokenType.entries.find { it.seq == result }
        if (type == null) return Token(TokenType.IDENTIFIER, result)

        return Token(type, result)
    }

    fun tokenize(): ArrayList<Token> {
        val tokens = arrayListOf<Token>()

        while (current != '\u0000') {
            while (current.isWhitespace()) consume()

            val type = TokenType.entries.find { it.seq == current.toString() }
            if (type != null) {
                tokens.add(Token(type, current.toString()))
                consume()
            }

            if (current == '#') {
                while (pos < content.length && current !in listOf('\n', '\r')) consume()
            }
            if (current in listOf('"', '\'')) tokens.add(string())
            if (current.isDigit()) tokens.add(number())
            if (current.isLetter()) tokens.add(identifier())
        }

        tokens.add(Token(TokenType.EOF, ""))

        return tokens
    }
}