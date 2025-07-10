package org.caojun.ctc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@Preview
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CompleteMaterial3ShowcaseWithLabels() {
    // 状态变量
    var checkedState by remember { mutableStateOf(true) }
    var sliderPosition by remember { mutableFloatStateOf(0.5f) }
    val radioOptions = listOf("选项1", "选项2", "选项3")
    var selectedOption by remember { mutableStateOf(radioOptions[0]) }
    var expanded by remember { mutableStateOf(false) }
    val items = listOf("项目1", "项目2", "项目3", "项目4")
    var selectedItem by remember { mutableStateOf(items[0]) }
    var textFieldValue by remember { mutableStateOf("") }
    var openDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var chipSelected by remember { mutableStateOf(false) }
    var switchChecked by remember { mutableStateOf(true) }
    var rangeSliderPosition by remember { mutableStateOf(0.25f..0.75f) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Material 3 组件大全") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("首页") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                    label = { Text("组件") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. 文本组件
            ComponentSection("文本组件 (Text)") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledComponent("DisplayLarge - 大标题") {
                        Text("大标题", style = MaterialTheme.typography.displayLarge)
                    }
                    LabeledComponent("HeadlineMedium - 中标题") {
                        Text("中标题", style = MaterialTheme.typography.headlineMedium)
                    }
                    LabeledComponent("BodyMedium - 正文") {
                        Text("这是正文内容", style = MaterialTheme.typography.bodyMedium)
                    }
                    LabeledComponent("LabelSmall - 标签") {
                        Text("标签文字", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // 2. 按钮组件
            ComponentSection("按钮组件 (Button)") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LabeledComponent("Button   - 按钮") {
                        Button(onClick = {}) { Text("确定") }
                    }
                    LabeledComponent("OutlinedButton - 轮廓按钮") {
                        OutlinedButton(onClick = {}) { Text("取消") }
                    }
                    LabeledComponent("TextButton - 文本按钮") {
                        TextButton(onClick = {}) { Text("了解更多") }
                    }
                    LabeledComponent("FloatingActionButton - 浮动按钮") {
                        FloatingActionButton(onClick = {}) {
                            Icon(Icons.Default.Add, "添加")
                        }
                    }
                }
            }

            // 3. 选择控件
            ComponentSection("选择控件 (Selection Controls)") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LabeledComponent("Checkbox - 复选框") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checkedState,
                                onCheckedChange = { checkedState = it }
                            )
                            Text("同意条款", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    LabeledComponent("Switch - 开关") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = switchChecked,
                                onCheckedChange = { switchChecked = it }
                            )
                            Text("夜间模式", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    LabeledComponent("RadioButton - 单选按钮") {
                        Column {
                            radioOptions.forEach { option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedOption = option }
                                ) {
                                    RadioButton(
                                        selected = (option == selectedOption),
                                        onClick = { selectedOption = option }
                                    )
                                    Text(option, modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 4. 滑块组件
            ComponentSection("滑块组件 (Slider)") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LabeledComponent("Slider - 单值滑块") {
                        Slider(
                            value = sliderPosition,
                            onValueChange = { sliderPosition = it },
                            valueRange = 0f..1f,
                            steps = 5
                        )
                    }
                    LabeledComponent("RangeSlider - 范围滑块") {
                        RangeSlider(
                            value = rangeSliderPosition,
                            onValueChange = { rangeSliderPosition = it },
                            valueRange = 0f..1f
                        )
                    }
                }
            }

            // 5. 输入组件
            ComponentSection("输入组件 (Text Fields)") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LabeledComponent("OutlinedTextField - 轮廓输入框") {
                        OutlinedTextField(
                            value = textFieldValue,
                            onValueChange = { textFieldValue = it },
                            label = { Text("用户名") }
                        )
                    }
                    LabeledComponent("TextField - 填充输入框") {
                        TextField(
                            value = textFieldValue,
                            onValueChange = { textFieldValue = it },
                            label = { Text("密码") },
                            visualTransformation = PasswordVisualTransformation()
                        )
                    }
                }
            }

            // 6. 卡片组件
            ComponentSection("卡片组件 (Card)") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LabeledComponent("ElevatedCard - 高架卡片") {
                        ElevatedCard(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("卡片标题", style = MaterialTheme.typography.titleLarge)
                                Text("这是高架卡片内容", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    LabeledComponent("OutlinedCard - 轮廓卡片") {
                        OutlinedCard(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("卡片标题", style = MaterialTheme.typography.titleLarge)
                                Text("这是轮廓卡片内容", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // 7. 对话框和菜单
            ComponentSection("对话框和菜单 (Dialogs & Menus)") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LabeledComponent("AlertDialog - 警告对话框") {
                        Button(onClick = { openDialog = true }) {
                            Text("显示对话框")
                        }
                        if (openDialog) {
                            AlertDialog(
                                onDismissRequest = { openDialog = false },
                                title = { Text("确认操作") },
                                text = { Text("您确定要执行此操作吗？") },
                                confirmButton = {
                                    TextButton(onClick = { openDialog = false }) {
                                        Text("确定")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { openDialog = false }) {
                                        Text("取消")
                                    }
                                }
                            )
                        }
                    }

                    LabeledComponent("DropdownMenu - 下拉菜单") {
                        Box {
                            OutlinedButton(onClick = { expanded = true }) {
                                Text("选择项目")
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                items.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
                                        onClick = {
                                            selectedItem = item
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 8. 进度指示器
            ComponentSection("进度指示器 (Progress Indicators)") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabeledComponent("LinearProgressIndicator - 线性进度条") {
                        LinearProgressIndicator(
                            progress = { sliderPosition },
                            modifier = Modifier.width(100.dp)
                        )
                    }
                    LabeledComponent("CircularProgressIndicator - 圆形进度条") {
                        CircularProgressIndicator(
                            progress = { sliderPosition },
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // 9. 芯片组件
            ComponentSection("芯片组件 (Chips)") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledComponent("FilterChip - 过滤芯片") {
                        FilterChip(
                            selected = chipSelected,
                            onClick = { chipSelected = !chipSelected },
                            label = { Text("过滤") }
                        )
                    }
                    LabeledComponent("AssistChip - 辅助芯片") {
                        AssistChip(
                            onClick = {},
                            label = { Text("操作") },
                            leadingIcon = {
                                Icon(Icons.Default.Send, null)
                            }
                        )
                    }
                }
            }

            // 10. 列表组件
            ComponentSection("列表组件 (Lists)") {
                LabeledComponent("ListItem - 列表项") {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            ListItem(
                                headlineContent = { Text("单行列表项") },
                                leadingContent = {
                                    Icon(Icons.Default.Email, null)
                                }
                            )
                            HorizontalDivider()
                            ListItem(
                                headlineContent = { Text("带图标的列表项") },
                                supportingContent = { Text("次要文本") },
                                leadingContent = {
                                    Icon(Icons.Default.Star, null)
                                },
                                trailingContent = {
                                    Icon(Icons.Default.MoreVert, null)
                                }
                            )
                        }
                    }
                }
            }

            // 11. 日期选择器
            ComponentSection("日期选择器 (DatePicker)") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showDatePicker = true }) {
                        Text(selectedDate.toString() ?: "选择日期")
                    }

                    if (showDatePicker) {
                        val datePickerState = rememberDatePickerState()
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        selectedDate = datePickerState.selectedDateMillis?.let {
                                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                                        }
                                        showDatePicker = false
                                    }
                                ) { Text("确定") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }
                }
            }
        }
    }
}

// 组件分组标题
@Composable
fun ComponentSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

// 带标签的组件
@Composable
fun LabeledComponent(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        content()
    }
}