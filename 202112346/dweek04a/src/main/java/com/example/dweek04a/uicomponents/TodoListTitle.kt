package com.example.dweek04a.uicomponents

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dweek04a.R

@Composable
fun TodoListTitle(modifier: Modifier = Modifier) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.todolist_title),
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
    )
}

@Preview
@Composable
private fun TodoListTitlePreview() {
    TodoListTitle()
}