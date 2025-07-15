package org.caojun.ctc

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alibaba.excel.EasyExcel
import com.alibaba.excel.context.AnalysisContext
import com.alibaba.excel.event.AnalysisEventListener
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.FileNotFoundException
import java.io.IOException

/**
 * 首页
 */
@Preview
@Composable
fun CTCScreen() {
    var tfvPlaintext by remember { mutableStateOf("") }
    var tfvCiphertext by remember { mutableStateOf("") }
    var debugInfo by remember { mutableStateOf("初始化中...") }

    // 在组合中加载Excel数据
    val codes = remember { mutableStateOf<List<Array<String>>>(emptyList()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        debugInfo = "正在加载资源..."
        val (loadedData, resourceDebug) = loadExcelFromAssetsWithDebug(context, "电报码.xlsx")
        codes.value = loadedData
        debugInfo = """
            资源加载结果:
            $resourceDebug
            加载行数: ${loadedData.size}
            首行数据: ${loadedData.firstOrNull()?.joinToString() ?: "无数据"}
        """.trimIndent()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("电报码", style = MaterialTheme.typography.displayLarge)
        Text(debugInfo, style = MaterialTheme.typography.bodyMedium)

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

/**
 * 从 Android assets 目录读取 Excel 文件（带调试信息）
 * @param context Android Context
 * @param fileName assets 目录下的文件名（如 "电报码.xlsx"）
 */
fun loadExcelFromAssetsWithDebug(
    context: Context,
    fileName: String
): Pair<List<Array<String>>, String> {
    val debugInfo = StringBuilder().apply {
        appendLine("===== Assets 调试信息 =====")
        appendLine("文件路径: assets/$fileName")
    }
    val dataList = mutableListOf<Array<String>>()

    try {
        // 1. 检查文件是否存在
        val assetManager = context.assets
        debugInfo.appendLine("Assets 文件列表:")
        assetManager.list("")?.forEach { file ->
            debugInfo.appendLine("- $file")
        }

        // 2. 打开文件流
        assetManager.open(fileName).use { inputStream ->
            debugInfo.appendLine("成功打开文件流")

            // 3. 使用 EasyExcel 解析
            EasyExcel.read(inputStream)
                .sheet(0)
                .registerReadListener(object : AnalysisEventListener<Map<Int, String>>() {
                    override fun invoke(rowData: Map<Int, String>, context: AnalysisContext) {
                        dataList.add(rowData.values.toTypedArray())
                    }

                    override fun doAfterAllAnalysed(context: AnalysisContext) {
                        debugInfo.appendLine("Excel 解析完成")
                    }
                }).doRead()
        }
    } catch (e: FileNotFoundException) {
        debugInfo.appendLine("错误: 文件未找到 - ${e.message}")
    } catch (e: IOException) {
        debugInfo.appendLine("错误: IO 异常 - ${e.stackTraceToString()}")
    } catch (e: Exception) {
        debugInfo.appendLine("错误: 未知异常 - ${e.stackTraceToString()}")
    }

    debugInfo.appendLine("===== 解析结果 =====")
    debugInfo.appendLine("读取行数: ${dataList.size}")
    if (dataList.isNotEmpty()) {
        debugInfo.appendLine("首行数据: ${dataList.first().joinToString()}")
    }

    return dataList to debugInfo.toString()
}