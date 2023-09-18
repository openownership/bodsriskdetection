package org.bodsrisk.utils

const val WORD_DELIMITERS = " ,.!?;(){}[]<>\"“”‘’'«»*%^&~`|:/\\-—\t\n\r"

fun String.tokenize(delimiters: String = WORD_DELIMITERS): List<String> {
    val chars = toCharArray()
    var crtIndex = 0
    val tokens = mutableListOf<String>()
    while (crtIndex < length) {
        val crtChar = chars[crtIndex]
        if (crtChar.isWordChar(delimiters)) {
            var end = crtIndex
            while (end < length && chars[end].isWordChar(delimiters)) {
                end++
            }

            val word = substring(crtIndex, end)
            tokens.add(word)
            crtIndex = end
        }
        crtIndex++
    }

    return tokens
}

private fun Char.isWordChar(delimiters: String): Boolean {
    return !isWhitespace() && !delimiters.contains(this)
}

fun Number.plural(singular: String, plural: String): String {
    val toInt = this.toInt()
    return if (toInt == 1) {
        "$toInt $singular"
    } else {
        "$toInt $plural"
    }
}