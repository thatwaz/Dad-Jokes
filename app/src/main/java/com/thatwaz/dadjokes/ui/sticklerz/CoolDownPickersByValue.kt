package com.thatwaz.dadjokes.ui.sticklerz



class CooldownPickerByValue(private val window: Int = 10) {
    private val recentByKey = mutableMapOf<String, MutableList<String>>()

    fun pick(list: List<String>, key: String): String {
        if (list.isEmpty()) return ""
        val recent = recentByKey.getOrPut(key) { mutableListOf() }
        val candidates = list.filter { it !in recent }.ifEmpty { list }
        val choice = candidates.random()

        recent.add(choice)                 // append to end
        while (recent.size > window) {
            recent.removeAt(0)             // drop oldest
        }
        return choice
    }
}


