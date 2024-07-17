package de.chaoticva.token

class Token(val type: TokenType, val value: String) {
    override fun toString(): String {
        return "Token(type=$type, value='$value')"
    }
}