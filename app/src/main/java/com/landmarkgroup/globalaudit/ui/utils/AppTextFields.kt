package com.landmarkgroup.globalaudit.ui.utils

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.unit.dp

/**
 * App-wide keyboard defaults and a wrapper TextField that
 * enforces uppercase-by-default for all TEXT inputs.
 *
 * Usage:
 *  - Replace OutlinedTextField with AppOutlinedTextField for any text field
 *  - By default, capitalization is Characters and onValueChange coerces to uppercase
 *  - Provide filter = { it.replace(" ", "") } if you want to strip spaces
 *  - For numeric fields, pass keyboardOptions with KeyboardType.Number
 */
object AppKeyboardDefaults {
    val UppercaseText: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Characters,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Done
    )
}

@Composable
fun AppOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(12.dp),
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(),
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = AppKeyboardDefaults.UppercaseText,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    // Optional input sanitizer (e.g., remove spaces)
    filter: ((String) -> String)? = null
) {
    val applyFilter: (String) -> String = { input ->
        var s = filter?.invoke(input) ?: input
        // Enforce uppercase for TEXT inputs when capitalization is Characters
        if (keyboardOptions.keyboardType == KeyboardType.Text &&
            keyboardOptions.capitalization == KeyboardCapitalization.Characters
        ) {
            s = s.uppercase()
        }
        s
    }

    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(applyFilter(it)) },
        modifier = modifier,
        placeholder = placeholder,
        shape = shape,
        colors = colors,
        singleLine = singleLine,
        trailingIcon = trailingIcon,
        leadingIcon = leadingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}
