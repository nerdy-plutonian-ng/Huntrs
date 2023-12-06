package com.plutoapps.huntrs.ui.composables

import android.icu.util.Freezable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TextBox(
    modifier: Modifier = Modifier,
    text: String,
    onchange: (String) -> Unit,
    label: String,
    error: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = text,
            onValueChange = onchange,
            label = { Text(text = label) },
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = trailing,
            placeholder = placeholder,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
        )
        if (error != null)
            Text(
                text = error,
                modifier = modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error)
            )
    }
}

@Preview(showBackground = true)
@Composable
fun TextBoxPreview() {
    TextBox(
        text = "John Doe",
        onchange = {},
        label = "Name",
        trailing = { Icon(Icons.Default.LocationOn, null) })
}
