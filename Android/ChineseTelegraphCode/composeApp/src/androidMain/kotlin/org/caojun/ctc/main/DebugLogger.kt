package org.caojun.ctc.main

/**
 * 记录调试日志的类
 */
class DebugLogger {
    private val logBuilder = StringBuilder()

    /**
     * 添加日志条目
     * @param message 要记录的日志信息
     */
    fun addLog(message: String) {
        logBuilder.appendLine(message)
    }

    /**
     * 添加带标题的日志部分
     * @param title 部分标题
     * @param content 日志内容
     */
    fun addSection(title: String, content: String) {
        logBuilder.appendLine("===== $title =====")
        logBuilder.appendLine(content)
    }

    /**
     * 获取完整日志
     */
    fun getLog(): String {
        return logBuilder.toString()
    }

    /**
     * 清空日志
     */
    fun clear() {
        logBuilder.clear()
    }
}