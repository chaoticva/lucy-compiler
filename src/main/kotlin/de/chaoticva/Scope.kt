package de.chaoticva

class Scope(val parent: Scope? = null) {
    val variables = mutableListOf<Variable>()
    val _variables = hashMapOf<String, String>()

    fun findVariable(name: String): Variable? {
        if (variables.any { it.name == name })
            return variables.find { it.name == name }
        else {
            if (parent!= null)
                return parent.findVariable(name)
            return null
        }
    }

    fun getType(name: String): String? {
        if (_variables.any { it.key == name })
            return _variables[name]
        else {
            if (parent!= null)
                return parent.getType(name)
            return null
        }
    }
}