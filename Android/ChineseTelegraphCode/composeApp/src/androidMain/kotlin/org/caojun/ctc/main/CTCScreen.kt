package org.caojun.ctc.main

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 首页
 */
@Preview
@Composable
fun CTCScreen() {
    var tfvPlaintext by remember { mutableStateOf("") }
    var tfvCodetext by remember { mutableStateOf("") }
    var tfvCiphertext by remember { mutableStateOf("") }
    var debugInfo by remember { mutableStateOf("初始化中...") }
    var tfvKey by remember { mutableStateOf("0") }

    val debugLogger = remember { DebugLogger() }
    val context = LocalContext.current
    val ctcHelper = remember { CTCHelper(context) }

    LaunchedEffect(Unit) {
        debugLogger.clear()
        debugLogger.addSection("资源加载", "正在加载资源...")

        ctcHelper.loadExcelFromAssets("电报码.xlsx")
            .onSuccess {
                debugLogger.addSection("资源加载结果", """
                    加载行数: ${ctcHelper.getDataSize()}
                    首行数据: ${ctcHelper.getFirstRowData()}
                """.trimIndent())
            }
            .onFailure {
                debugLogger.addLog("加载失败: ${it.message}")
            }

        debugInfo = debugLogger.getLog()
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
        Text(debugInfo,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            debugLogger.clear()
                            debugInfo = "日志已清空"
                        }
                    )
                })

        OutlinedTextField(
            value = tfvKey,
            onValueChange = { tfvKey = it },
            label = { Text("密钥") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = tfvPlaintext,
            onValueChange = { tfvPlaintext = it },
            label = { Text("汉字") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            tfvCodetext = ctcHelper.plainToCipher(tfvPlaintext, tfvKey.toInt(), debugLogger)
            debugInfo = debugLogger.getLog()

            tfvCiphertext = ctcHelper.cipherToPlain(tfvCodetext, 0, debugLogger)
            debugInfo = debugLogger.getLog()
        }) {
            Text("汉字 -> 电报码 -> 密文")
        }

        OutlinedTextField(
            value = tfvCodetext,
            onValueChange = { tfvCodetext = it },
            label = { Text("电报码") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            tfvPlaintext = ctcHelper.cipherToPlain(tfvCodetext, tfvKey.toInt(), debugLogger)
            debugInfo = debugLogger.getLog()
        }) {
            Text("电报码 -> 汉字")
        }

        OutlinedTextField(
            value = tfvCiphertext,
            onValueChange = { tfvCiphertext = it },
            label = { Text("密文") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            tfvCodetext = ctcHelper.plainToCipher(tfvCiphertext, 0, debugLogger)
            debugInfo = debugLogger.getLog()

            tfvPlaintext = ctcHelper.cipherToPlain(tfvCodetext, tfvKey.toInt(), debugLogger)
            debugInfo = debugLogger.getLog()
        }) {
            Text("密文 -> 电报码 -> 汉字")
        }
    }
}