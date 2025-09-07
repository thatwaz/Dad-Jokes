package com.thatwaz.dadjokes.notification

import android.content.Context
import kotlin.random.Random

private const val PREFS = "notif_pool"
private const val USED = "used_indices"

object NotificationPool {
    /** Returns a joke that hasn't been used yet; resets when all are used. */
    fun next(context: Context): String {
        val pool = NotificationDadJokes.jokes
        if (pool.isEmpty()) return "Out of jokes… somehow. 😅"

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val used = prefs.getStringSet(USED, emptySet())!!.map { it.toInt() }.toMutableSet()

        val all = (pool.indices).toSet()
        val remaining = all - used

        val pickIndex = if (remaining.isEmpty()) {
            // reset the cycle
            used.clear()
            Random.nextInt(pool.size)
        } else {
            remaining.random()
        }

        used.add(pickIndex)
        prefs.edit().putStringSet(USED, used.map { it.toString() }.toSet()).apply()

        return pool[pickIndex]
    }

    /** Optional: call this from a Settings “Reset joke pool” button. */
    fun reset(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(USED).apply()
    }
}
