package de.chaoticva

import de.chaoticva.ast.ASTNode
import de.chaoticva.token.Token
import de.chaoticva.util.Color
import kotlin.system.exitProcess

class ErrorHighlighter {
    companion object {
        fun highlight(token: Token, previous: Token, next: Token, fileName: String, fix: String, error: String? = null) {
            val message = StringBuilder()
            message.appendLine("Error in file '$fileName'")
            message.appendLine(error)
            message.append("-> ${previous.value} ").append(Color.RED_UNDERLINED).append(token.value)
                .append(Color.RESET)
                .append(Color.RED_BOLD)
                .appendLine(" ${next.value}")
            message.append(" ".repeat(token.lineStart + 3)).appendLine("^".repeat(token.lineEnd - token.lineStart))
            message .appendLine("should be:")
            message.append("-> ").append(Color.RED_UNDERLINED).appendLine("${previous.value} $fix ${token.value} ${next.value}")
            print(Color.RED_BOLD)
            print(message)
            exitProcess(1)
        }

        fun highlight(node: ASTNode, fileName: String, error: String? = null) {
            val message = StringBuilder()
            message.appendLine("Error in file '$fileName'")
            message.appendLine(error)
            message.appendLine("-> $node")
            message.append(" ".repeat(node.lineStart + 3)).appendLine("^".repeat(node.lineEnd - node.lineStart - 1))
            print(Color.RED_BOLD)
            print(message)
            exitProcess(1)
        }
    }
}