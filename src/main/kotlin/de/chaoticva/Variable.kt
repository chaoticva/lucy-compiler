package de.chaoticva

class Variable(val name: String, val sp: Int, var type: String) {
    override fun toString(): String {
        return "Variable(name='$name', sp=$sp)"
    }
}