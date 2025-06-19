/**
 * Excel 数据查询系统 - 完整实现
 * 动态从Excel读取列标题
 */
const ExcelQuerySystem = (function() {
    // 数据存储
    let excelData = [];
    let columnIndexes = [{}, {}, {}];
    let columnTitles = ['请先上传文件', '请先上传文件', '请先上传文件'];
    
    // DOM 元素缓存
    const dom = {
        fileInput: null,
        fileInfo: null,
        searchInputs: [],
        searchLabels: [],
        resultCount: null,
        resultsTable: null,
        tableHeaderRow: null
    };
    
    // 初始化应用
    function init() {
        console.log('Excel 查询系统初始化');
        cacheDOM();
        setupEventListeners();
        updateUIWithTitles();
    }
    
    // 缓存 DOM 元素
    function cacheDOM() {
        dom.fileInput = document.getElementById('excelFile');
        dom.fileInfo = document.getElementById('fileInfo');
        dom.resultCount = document.getElementById('resultCount');
        dom.resultsTable = document.getElementById('resultsTable');
        dom.tableHeaderRow = document.getElementById('tableHeaderRow');
        
        // 缓存搜索输入框和标签
        for (let i = 1; i <= 3; i++) {
            dom.searchInputs[i] = document.getElementById(`searchColumn${i}`);
            dom.searchLabels[i] = document.getElementById(`searchLabel${i}`);
        }
    }
    
    // 设置事件监听
    function setupEventListeners() {
        // 文件上传监听
        dom.fileInput.addEventListener('change', handleFileUpload);
        
        // 搜索框回车键监听
        dom.searchInputs.forEach((input, index) => {
            if (input) {
                input.addEventListener('keypress', function(e) {
                    if (e.key === 'Enter' && index >= 1) {
                        searchByColumn(index);
                    }
                });
            }
        });
        
        // 使用事件委托处理查询按钮点击
        document.addEventListener('click', function(e) {
            if (e.target.classList.contains('query-btn')) {
                const colNum = parseInt(e.target.dataset.colNum);
                if (colNum >= 1 && colNum <= 3) {
                    searchByColumn(colNum);
                }
            }
        });
    }
    
    // 处理文件上传
    function handleFileUpload(e) {
        const file = e.target.files[0];
        if (!file) return;
        
        updateFileInfo(`正在加载文件: ${file.name}...`, 'info');
        
        const reader = new FileReader();
        reader.onload = function(e) {
            try {
                const data = new Uint8Array(e.target.result);
                const workbook = XLSX.read(data, { type: 'array' });
                
                // 获取第一个工作表
                const firstSheetName = workbook.SheetNames[0];
                const worksheet = workbook.Sheets[firstSheetName];
                
                // 转换为 JSON 数组
                const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 });
                
                // 处理数据
                processExcelData(jsonData);
                
                // 更新文件信息
                updateFileInfo(
                    `已加载文件: <strong>${file.name}</strong><br>
                    总行数: <strong>${excelData.length}</strong> 行`,
                    'success'
                );
                
                // 更新UI中的标题
                updateUIWithTitles();
                
                // 启用搜索功能
                enableSearchInputs(true);
                
            } catch (error) {
                console.error('读取 Excel 文件出错:', error);
                updateFileInfo(
                    `错误: ${error.message}<br>请确保上传的是有效的 Excel 文件(.xlsx, .xls)`,
                    'error'
                );
                resetData();
            }
        };
        
        reader.onerror = function() {
            updateFileInfo('文件读取失败，请重试', 'error');
            console.error('文件读取错误:', reader.error);
        };
        
        reader.readAsArrayBuffer(file);
    }
    
    // 处理 Excel 数据
    function processExcelData(data) {
        resetData();
        
        console.log('开始处理 Excel 数据，原始行数:', data.length);
        
        // 提取标题行（第一行）
        if (data.length > 0 && data[0].length >= 3) {
            columnTitles = [
                data[0][0]?.toString()?.trim() || '未命名列1',
                data[0][1]?.toString()?.trim() || '未命名列2',
                data[0][2]?.toString()?.trim() || '未命名列3'
            ];
        }
        
        // 从第二行开始处理数据（第一行是标题）
        const startRow = 1;
        
        for (let i = startRow; i < data.length; i++) {
            const row = data[i];
            if (!row || row.length < 3) {
                console.warn(`跳过第${i+1}行: 数据不足`, row);
                continue;
            }
            
            const rowData = {
                column1: safeToString(row[0]),
                column2: safeToString(row[1]),
                column3: safeToString(row[2])
            };
            
            // 跳过全空的行
            if (!rowData.column1 && !rowData.column2 && !rowData.column3) {
                continue;
            }
            
            excelData.push(rowData);
            
            // 建立索引（不区分大小写）
            addToIndex(0, rowData.column1, rowData);
            addToIndex(1, rowData.column2, rowData);
            addToIndex(2, rowData.column3, rowData);
        }
        
        console.log('数据处理完成，有效行数:', excelData.length);
        console.log('列标题:', columnTitles);
    }
    
    // 更新UI中的标题显示
    function updateUIWithTitles() {
        // 更新表头
        if (dom.tableHeaderRow) {
            dom.tableHeaderRow.innerHTML = `
                <th>${escapeHtml(columnTitles[0])}</th>
                <th>${escapeHtml(columnTitles[1])}</th>
                <th>${escapeHtml(columnTitles[2])}</th>
            `;
        }
        
        // 更新查询标签
        for (let i = 1; i <= 3; i++) {
            if (dom.searchLabels[i]) {
                dom.searchLabels[i].textContent = columnTitles[i-1] + '查询';
            }
        }
    }
    
    // 添加到索引
    function addToIndex(indexNum, value, rowData) {
        if (!value) return;
        
        const key = value.toLowerCase();
        columnIndexes[indexNum][key] = columnIndexes[indexNum][key] || [];
        columnIndexes[indexNum][key].push(rowData);
    }
    
    // 按列查询
    function searchByColumn(columnNumber) {
        if (excelData.length === 0) {
            alert('请先上传 Excel 文件');
            return;
        }
        
        const searchInput = dom.searchInputs[columnNumber];
        const searchValue = searchInput.value.trim();
        const columnIndex = columnNumber - 1;
        
        console.log(`正在查询 ${columnTitles[columnIndex]}: "${searchValue}"`);
        
        let results = [];
        if (searchValue) {
            // 使用索引精确匹配（不区分大小写）
            const lowerSearchValue = searchValue.toLowerCase();
            results = columnIndexes[columnIndex][lowerSearchValue] || [];
        } else {
            // 如果搜索值为空，显示前100条数据
            results = excelData.slice(0, 100);
        }
        
        displayResults(results);
    }
    
    // 显示查询结果
    function displayResults(results) {
        dom.resultsTable.innerHTML = '';
        
        if (!results || results.length === 0) {
            dom.resultCount.innerHTML = `
                <span class="text-danger">没有找到匹配的记录</span>
                <button class="btn btn-sm btn-link" onclick="ExcelQuerySystem.showFirst100()">
                    显示全部数据(前100条)
                </button>
            `;
            
            dom.resultsTable.innerHTML = `
                <tr>
                    <td colspan="3" class="text-center text-muted">
                        没有找到匹配的记录，请尝试其他查询条件
                    </td>
                </tr>
            `;
            return;
        }
        
        dom.resultCount.textContent = `找到 ${results.length} 条记录`;
        
        // 限制显示最多500条结果
        const displayResults = results.length > 500 ? results.slice(0, 500) : results;
        if (results.length > 500) {
            dom.resultCount.textContent += ` (显示前500条)`;
        }
        
        displayResults.forEach(row => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${highlightSearchTerm(row.column1, 1)}</td>
                <td>${highlightSearchTerm(row.column2, 2)}</td>
                <td>${highlightSearchTerm(row.column3, 3)}</td>
            `;
            dom.resultsTable.appendChild(tr);
        });
    }
    
    // 高亮显示搜索词
    function highlightSearchTerm(text, columnNumber) {
        const searchInput = dom.searchInputs[columnNumber];
        const searchValue = searchInput.value.trim();
        
        if (!searchValue || !text) return escapeHtml(text);
        
        const lowerText = text.toLowerCase();
        const lowerSearchValue = searchValue.toLowerCase();
        const startIndex = lowerText.indexOf(lowerSearchValue);
        
        if (startIndex === -1) return escapeHtml(text);
        
        const endIndex = startIndex + searchValue.length;
        return [
            escapeHtml(text.substring(0, startIndex)),
            '<span class="highlight">',
            escapeHtml(text.substring(startIndex, endIndex)),
            '</span>',
            escapeHtml(text.substring(endIndex))
        ].join('');
    }
    
    // 辅助函数
    function safeToString(value) {
        if (value == null) return '';
        if (typeof value === 'string') return value.trim();
        if (typeof value === 'number') return String(value);
        if (value instanceof Date) return value.toLocaleString();
        return String(value).trim();
    }
    
    function escapeHtml(text) {
        if (!text) return '';
        return text.toString()
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }
    
    function updateFileInfo(message, type) {
        dom.fileInfo.innerHTML = message;
        dom.fileInfo.className = 'file-info ' + (type === 'error' ? 'text-danger' : type === 'success' ? 'text-success' : '');
    }
    
    function enableSearchInputs(enable) {
        dom.searchInputs.forEach((input, index) => {
            if (input) {
                input.disabled = !enable;
                input.placeholder = enable ? '输入查询值' : '请先上传 Excel 文件';
            }
        });
    }
    
    function resetData() {
        excelData = [];
        columnIndexes = [{}, {}, {}];
        columnTitles = ['请先上传文件', '请先上传文件', '请先上传文件'];
        enableSearchInputs(false);
        displayResults([]);
        updateUIWithTitles();
    }
    
    // 公有 API
    return {
        init: init,
        byColumn: searchByColumn,
        showFirst100: function() {
            displayResults(excelData.slice(0, 100));
        }
    };
})();

// 初始化应用
document.addEventListener('DOMContentLoaded', ExcelQuerySystem.init);