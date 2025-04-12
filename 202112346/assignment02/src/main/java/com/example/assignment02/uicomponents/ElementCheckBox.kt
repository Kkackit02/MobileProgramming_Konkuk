package com.example.assignment02.uicomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ElementCheckBox(
    //202112346 정근녕
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    elementName : String,
    modifier: Modifier = Modifier)
{
    Row(
        modifier = modifier
            .size(300.dp, 50.dp)
            .padding(end = 16.dp)
            .padding(start = 30.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
                text = elementName,

        modifier = Modifier.padding(end = 12.dp)
        )
    }
}

@Preview
@Composable
private fun TodoSwitchPrev() {
    ElementCheckBox(
        checked = false,
        onCheckedChange = { it },
        "TEST"
    )
}