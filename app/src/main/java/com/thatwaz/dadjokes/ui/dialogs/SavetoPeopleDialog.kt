@file:OptIn(ExperimentalMaterial3Api::class)

package com.thatwaz.dadjokes.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp


@Composable
fun SaveToPeopleDialog(
    existingPeople: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    // Always include "My Favorites" as the first option
    val canonicalFavorites = "My Favorites"
    val basePeople = remember(existingPeople) {
        buildList {
            add(canonicalFavorites)
            addAll(existingPeople.filter { !it.equals(canonicalFavorites, ignoreCase = true) })
        }
    }

    // Selected set; default includes "My Favorites"
    val selected = remember { mutableStateListOf<String>() }.apply {
        if (isEmpty()) add(canonicalFavorites)
    }

    var menuOpen by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    // Keyboard + focus helpers
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val nameFocus = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = {
            focusManager.clearFocus()
            keyboard?.hide()
            onDismiss()
        },
        title = { Text("Save joke to…") },
        text = {
            Column(Modifier.fillMaxWidth()) {

                // Multi-select dropdown (checkbox style)
                ExposedDropdownMenuBox(
                    expanded = menuOpen,
                    onExpandedChange = { menuOpen = it }
                ) {
                    OutlinedTextField(
                        value = if (selected.isEmpty()) "None" else selected.joinToString(),
                        onValueChange = { /* read-only */ },
                        label = { Text("Lists / People") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOpen) }
                    )

                    ExposedDropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false }
                    ) {
                        basePeople.forEach { name ->
                            val checked = selected.any { it.equals(name, ignoreCase = true) }
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(name)
                                        Checkbox(checked = checked, onCheckedChange = null)
                                    }
                                },
                                onClick = {
                                    if (checked) {
                                        selected.removeAll { it.equals(name, true) }
                                    } else {
                                        selected.add(name)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Add new name (outside the menu so keyboard works reliably)
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Add new name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameFocus),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words, // Capitalize first letter of each word
                        autoCorrect = true,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newName.isNotBlank()) {
                                addNameToSelection(newName, basePeople, selected)
                                newName = ""
                            }
                            keyboard?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    addNameToSelection(newName, basePeople, selected)
                                    newName = ""
                                }
                                keyboard?.hide()
                                focusManager.clearFocus()
                            },
                            enabled = newName.isNotBlank()
                        ) { Text("Add") }
                    }
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    "Tip: You can pick multiple names. “My Favorites” is selected by default.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // (Optional) Auto-focus the new-name field when dialog opens:
                LaunchedEffect(Unit) {
                    // comment out if you don't want auto-open
                    // nameFocus.requestFocus()
                    // keyboard?.show()
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val final = mergeWithExisting(selected, basePeople)
                    if (final.isNotEmpty()) onSave(final)
                    keyboard?.hide()
                    focusManager.clearFocus()
                    onDismiss()
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    keyboard?.hide()
                    focusManager.clearFocus()
                    onDismiss()
                }
            ) { Text("Cancel") }
        }
    )
}

/* ---------------- Helpers ---------------- */

private fun normalizeKey(name: String): String =
    name.trim().replace(Regex("\\s+"), " ").lowercase()

private fun displayCase(name: String): String =
    name.trim()
        .replace(Regex("\\s+"), " ")
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { part -> part.lowercase().replaceFirstChar { c -> c.titlecase() } }

/** Dedup + reuse existing spelling; returns canonicalized list */
private fun mergeWithExisting(
    names: List<String>,
    existing: List<String>
): List<String> {
    val existingMap = existing.associateBy { normalizeKey(it) }
    val result = LinkedHashSet<String>()
    for (n in names) {
        val key = normalizeKey(n)
        val chosen = existingMap[key] ?: displayCase(n)
        if (chosen.isNotBlank()) result.add(chosen)
    }
    return result.toList()
}

/** Adds a (new or normalized) name to the selected list with proper casing and de-dupe. */
private fun addNameToSelection(
    raw: String,
    existing: List<String>,
    selected: MutableList<String>
) {
    val merged = mergeWithExisting(listOf(raw), existing)
    val toAdd = merged.firstOrNull() ?: return
    if (selected.none { normalizeKey(it) == normalizeKey(toAdd) }) {
        selected.add(toAdd)
    }
}



