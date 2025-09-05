package com.thatwaz.dadjokes.ui.util

fun stableJokeId(setup: String, punchline: String, apiId: Any? = null): String {
    val key = apiId?.toString() ?: "$setup||$punchline"
    val md = java.security.MessageDigest.getInstance("SHA-1")
    val bytes = md.digest(key.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

