package org.caojun.ctc.main

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun CTCScreen() {
    val context = LocalContext.current
    val ctcHelper = remember { CTCHelper(context) }

    var switchChecked by remember { mutableStateOf(true) }
    var tfvKey by remember { mutableStateOf("0") }
    var tfvPlaintext by remember { mutableStateOf("") }
    var tfvCodetext by remember { mutableStateOf("") }
    var tfvCiphertext by remember { mutableStateOf("") }

    // 初始化加载电报码表
    LaunchedEffect(Unit) {
        ctcHelper.loadExcelFromAssets("电报码.xlsx")
    }

    // 当明文变化且处于加密模式时，实时加密
    LaunchedEffect(tfvPlaintext, switchChecked, tfvKey) {
        if (switchChecked && tfvPlaintext.isNotEmpty()) {
            tfvCodetext = ctcHelper.plainToCipher(tfvPlaintext, tfvKey.toIntOrNull())
            tfvCiphertext = ctcHelper.cipherToPlain(tfvCodetext)
        }
    }

    // 当密文变化且处于解密模式时，实时解密
    LaunchedEffect(tfvCiphertext, switchChecked, tfvKey) {
        if (!switchChecked && tfvCiphertext.isNotEmpty()) {
            tfvCodetext = ctcHelper.plainToCipher(tfvCiphertext)
            tfvPlaintext = ctcHelper.cipherToPlain(tfvCodetext, tfvKey.toIntOrNull())
        }
    }

    val clipboardManager = remember { context.getSystemService(ClipboardManager::class.java) }
    // 监听生命周期
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) { // 应用退到后台时触发
                val textToCopy = if (switchChecked) tfvCiphertext else tfvPlaintext
                if (textToCopy.isNotEmpty()) {
                    clipboardManager?.setPrimaryClip(
                        ClipData.newPlainText(
                            "CTC Result",
                            textToCopy
                        )
                    )
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .imePadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("电报码", style = MaterialTheme.typography.displayLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = switchChecked,
                onCheckedChange = { switchChecked = it }
            )
            Text(if (switchChecked) "加密" else "解密", modifier = Modifier.padding(start = 8.dp))
            OutlinedTextField(
                value = tfvKey,
                onValueChange = { tfvKey = it },
                label = { Text("密钥") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        OutlinedTextField(
            enabled = switchChecked,
            value = tfvPlaintext,
            onValueChange = { tfvPlaintext = it },
            label = { Text(if (switchChecked) "明文" else "解密结果") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            enabled = !switchChecked,
            value = tfvCiphertext,
            onValueChange = { tfvCiphertext = it },
            label = { Text(if (switchChecked) "加密结果" else "密文") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}