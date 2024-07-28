package de.chaoticva.token

class Token(val type: TokenType, val value: String, val lineStart: Int, val lineEnd: Int, val line: Int) {
    override fun toString(): String {
        return "Token(type=$type, value='$value')"
    }
}