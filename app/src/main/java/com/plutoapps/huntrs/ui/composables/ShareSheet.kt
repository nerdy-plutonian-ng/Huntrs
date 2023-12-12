package com.plutoapps.huntrs.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.plutoapps.huntrs.data.models.HuntWithCheckpoints

@Composable
fun ShareSheet(huntWithCheckpoints: HuntWithCheckpoints, dismissSheet: () -> Unit) {
    Column {
        Text("Share hunt", style = MaterialTheme.typography.titleLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun ShareSheetPreview() {
    ShareSheet(huntWithCheckpoints = HuntWithCheckpoints(), dismissSheet = {  })
}