package org.caojun.ctc.main

import android.content.Context
import com.alibaba.excel.EasyExcel
import com.alibaba.excel.context.AnalysisContext
import com.alibaba.excel.event.AnalysisEventListener
import java.io.FileNotFoundException
import java.io.IOException

/**
 * 电报码转换工具类（支持明文/密文转换及数字偏移加密/解密）
 */
class CTCHelper(private val context: Context) {
    private val codes = mutableListOf<Array<String>>()

    /**
     * 从Assets加载Excel数据
     */
    fun loadExcelFromAssets(fileName: String): Result<Unit> {
        return try {
            context.assets.open(fileName).use { inputStream ->
                EasyExcel.read(inputStream)
                    .sheet(0)
                    .registerReadListener(object : AnalysisEventListener<Map<Int, String>>() {
                        override fun invoke(rowData: Map<Int, String>, context: AnalysisContext) {
                            codes.add(rowData.values.toTypedArray())
                        }
                        override fun doAfterAllAnalysed(context: AnalysisContext) {}
                    }).doRead()
            }
            Result.success(Unit)
        } catch (e: FileNotFoundException) {
            Result.failure(Exception("文件未找到: ${e.message}"))
        } catch (e: IOException) {
            Result.failure(Exception("IO异常: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("未知错误: ${e.message}"))
        }
    }

    /**
     * 明文转密文（兼容原功能，偏移量为0时直接转换）
     */
    fun plainToCipher(plaintext: String, offset: Int? = 0): String {
        return encrypt(plaintext, offset ?: 0)
    }
    fun plainToCipher(plaintext: String, debugLogger: DebugLogger? = null, offset: Int? = 0): String {
        return encrypt(plaintext, offset ?: 0, debugLogger)
    }

    /**
     * 密文转明文（兼容原功能，偏移量为0时直接转换）
     */
    fun cipherToPlain(ciphertext: String, offset: Int? = 0): String {
        return decrypt(ciphertext, offset ?: 0)
    }
    fun cipherToPlain(ciphertext: String, debugLogger: DebugLogger? = null, offset: Int? = 0): String {
        return decrypt(ciphertext, offset ?: 0, debugLogger)
    }

    /**
     * 加密明文（数字偏移）
     */
    private fun encrypt(plaintext: String, offset: Int = 0, debugLogger: DebugLogger? = null): String {
        debugLogger?.addSection("加密（偏移=$offset）", "开始转换...")
        debugLogger?.addLog("输入明文: $plaintext")

        if (codes.isEmpty()) {
            debugLogger?.addLog("错误: 电报码数据未加载")
            return "错误: 电报码数据未加载"
        }

        val result = StringBuilder()
        for (char in plaintext) {
            val found = codes.firstOrNull { row ->
                row.getOrNull(1)?.contains(char.toString()) == true
            }

            if (found != null && found.isNotEmpty()) {
                val originalCode = found[0].toIntOrNull() ?: continue
                val encryptedCode = (originalCode + offset).toString().padStart(4, '0')

                // 检查加密后的电报码是否存在
                val isValid = codes.any { it[0] == encryptedCode }
                if (isValid) {
                    debugLogger?.addLog("加密成功: '$char' ($originalCode) -> '$encryptedCode'")
                    result.append(encryptedCode)
                } else {
                    debugLogger?.addLog("加密后无效码: '$encryptedCode'，保留原文")
                    result.append(char)
                }
            } else {
                debugLogger?.addLog("未匹配: '$char'")
                result.append(char)
            }
        }

        val ciphertext = result.toString()
        debugLogger?.addLog("加密结果: $ciphertext")
        return ciphertext
    }

    /**
     * 解密密文（数字偏移）
     */
    private fun decrypt(ciphertext: String, offset: Int = 0, debugLogger: DebugLogger? = null): String {
        debugLogger?.addSection("解密（偏移=$offset）", "开始转换...")
        debugLogger?.addLog("输入密文: $ciphertext")

        if (codes.isEmpty()) {
            debugLogger?.addLog("错误: 电报码数据未加载")
            return "错误: 电报码数据未加载"
        }

        val result = StringBuilder()
        var i = 0
        while (i < ciphertext.length) {
            if (i + 4 <= ciphertext.length) {
                val potentialCode = ciphertext.substring(i, i + 4)
                val originalCode = potentialCode.toIntOrNull() ?: run {
                    debugLogger?.addLog("无效数字: '$potentialCode'")
                    result.append(ciphertext[i])
                    i++
                    continue
                }

                // 反向偏移计算
                val decryptedCode = (originalCode - offset).toString().padStart(4, '0')
                val found = codes.firstOrNull { row ->
                    row.getOrNull(0)?.equals(decryptedCode) == true
                }

                if (found != null && found.size >= 2) {
                    debugLogger?.addLog("解密成功: '$potentialCode' -> '${found[1]}'")
                    result.append(found[1])
                    i += 4
                    continue
                }
            }

            debugLogger?.addLog("未匹配: '${ciphertext[i]}'")
            result.append(ciphertext[i])
            i++
        }

        val plaintext = result.toString()
        debugLogger?.addLog("解密结果: $plaintext")
        return plaintext
    }

    /**
     * 获取加载的数据行数
     */
    fun getDataSize(): Int = codes.size

    /**
     * 获取首行数据
     */
    fun getFirstRowData(): String = codes.firstOrNull()?.joinToString() ?: "无数据"
}