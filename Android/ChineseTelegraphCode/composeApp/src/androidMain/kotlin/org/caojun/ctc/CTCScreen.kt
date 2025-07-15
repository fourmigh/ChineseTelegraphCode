package org.caojun.ctc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 首页
 */
@Preview
@Composable
fun CTCScreen() {

    var tfvPlaintext by remember { mutableStateOf("") }
    var tfvCiphertext by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("电报码", style = MaterialTheme.typography.displayLarge)
        Text("初始化结果", style = MaterialTheme.typography.bodyMedium)

        LabeledComponent("请输入明文") {
            OutlinedTextField(
                value = tfvPlaintext,
                onValueChange = { tfvPlaintext = it },
                label = { Text("明文") }
            )
        }
        Button(onClick = {}) { Text("明文 -> 密文") }

        LabeledComponent("请输入密文") {
            OutlinedTextField(
                value = tfvCiphertext,
                onValueChange = { tfvCiphertext = it },
                label = { Text("密文") }
            )
        }
        Button(onClick = {}) { Text("密文 -> 明文") }
    }
}