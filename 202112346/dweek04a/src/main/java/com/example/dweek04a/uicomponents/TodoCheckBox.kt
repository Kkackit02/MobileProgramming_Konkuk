package com.example.dweek04a.uicomponents

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TodoCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit // 가장 마지막에 있는 매개변수는 괄호 밖으로 뺄 수 있음.
) {
    Checkbox(checked = checked,
        onCheckedChange = { isChecked -> onCheckedChange(isChecked)
    })
}

@Preview
@Composable
private fun TodoCheckboxPreview() {
    TodoCheckbox(checked = true) {
        
    }

}

