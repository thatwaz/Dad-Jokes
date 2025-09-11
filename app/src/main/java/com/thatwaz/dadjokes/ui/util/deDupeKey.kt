package com.thatwaz.dadjokes.ui.util

fun dedupeKey(setup: String, punch: String) =
    stableJokeId(setup, punch, "")
   // ignore API id on purpose
