package io.github.orioneee.extentions

fun Int.formate(): String{
    // put space every 3 digits
    return this.toString().reversed().chunked(3).joinToString(" ").reversed()
}