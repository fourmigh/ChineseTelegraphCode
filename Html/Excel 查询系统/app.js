/**
 * Excel 数据查询系统 - 动态列数实现
 */
const ExcelQuerySystem = (function() {
    // 数据存储
    let excelData = [];
    let columnIndexes = [];
    let columnTitles = [];
    let columnCount = 0;
    
    // DOM 元素缓存
    const dom = {
        fileInput: null,
        fileInfo: null,
        queryInputsContainer: null,
        noFileUploadedHint: null,
        resultCount: null,
        resultsTable: null,
        tableHeaderRow: null
    };
    
    // 初始化应用
    function init() {
        console.log('Excel 查询系统初始化');
        cacheDOM();
        setupEventListeners();
    }
    
    // 缓存 DOM 元素
    function cacheDOM() {
        dom.fileInput = document.getElementById('excelFile');
        dom.fileInfo = document.getElementById('fileInfo');
        dom.queryInputsContainer = document.getElementById('queryInputsContainer');
        dom.noFileUploadedHint = document.getElementById('noFileUploadedHint');
        dom.resultCount = document.getElementById('resultCount');
        dom.resultsTable = document.getElementById('resultsTable');
        dom.tableHeaderRow = document.getElementById('tableHeaderRow');
    }
    
    // 设置事件监听
    function setupEventListeners() {
        // 文件上传监听
        dom.fileInput.addEventListener('change', handleFileUpload);
        
        // 使用事件委托处理查询按钮点击
        document.addEventListener('click', function(e) {
            if (e.target.classList.contains('query-btn')) {
                const colNum = parseInt(e.target.dataset.colNum);
                if (colNum >= 0 && colNum < columnCount) {
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
                    总行数: <strong>${excelData.length}</strong> 行<br>
                    列数: <strong>${columnCount}</strong> 列`,
                    'success'
                );
                
                // 更新UI
                updateQueryInputs();
                updateTableHeader();
                
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
        
        console.log('开始处理 Excel 数据:', data);
        
        if (!data || data.length === 0) {
            throw new Error('Excel 文件为空或格式不正确');
        }
        
        // 确定列数（取第一行的长度）
        columnCount = data[0].length;
        if (columnCount === 0) {
            throw new Error('Excel 文件中没有数据列');
        }
        
        // 初始化列索引
        columnIndexes = new Array(columnCount);
        for (let i = 0; i < columnCount; i++) {
            columnIndexes[i] = {};
        }
        
        // 提取标题行（第一行）
        columnTitles = [];
        for (let i = 0; i < columnCount; i++) {
            columnTitles.push(data[0][i]?.toString()?.trim() || `列${i+1}`);
        }
        
        // 从第二行开始处理数据（第一行是标题）
        const startRow = 1;
        
        for (let i = startRow; i < data.length; i++) {
            const row = data[i];
            if (!row || row.length === 0) {
                console.warn(`跳过第${i+1}行: 数据不足`, row);
                continue;
            }
            
            const rowData = {};
            let isEmptyRow = true;
            
            // 处理每一列数据
            for (let col = 0; col < columnCount; col++) {
                const value = col < row.length ? safeToString(row[col]) : '';
                rowData[`column${col}`] = value;
                
                if (value) {
                    isEmptyRow = false;
                }
            }
            
            // 跳过全空的行
            if (isEmptyRow) {
                continue;
            }
            
            excelData.push(rowData);
            
            // 为每一列建立索引
            for (let col = 0; col < columnCount; col++) {
                const value = rowData[`column${col}`];
                if (value) {
                    const key = value.toLowerCase();
                    columnIndexes[col][key] = columnIndexes[col][key] || [];
                    columnIndexes[col][key].push(rowData);
                }
            }
        }
        
        console.log('数据处理完成:', {
            rowCount: excelData.length,
            columnCount: columnCount,
            columnTitles: columnTitles
        });
    }
    
    // 更新查询输入框
    function updateQueryInputs() {
        dom.noFileUploadedHint.style.display = 'none';
        dom.queryInputsContainer.innerHTML = '';
        
        // 根据列数动态生成查询输入框
        for (let col = 0; col < columnCount; col++) {
            const colDiv = document.createElement('div');
            colDiv.className = `col-md-${Math.min(12, Math.floor(12 / columnCount))} query-col`;
            
            colDiv.innerHTML = `
                <div class="search-label">${escapeHtml(columnTitles[col])}查询</div>
                <div class="input-group">
                    <input type="text" id="searchColumn${col}" class="form-control" placeholder="输入查询值">
                    <button class="btn btn-primary query-btn" data-col-num="${col}">查询</button>
                </div>
            `;
            
            // 添加回车键监听
            const input = colDiv.querySelector('input');
            input.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    searchByColumn(col);
                }
            });
            
            dom.queryInputsContainer.appendChild(colDiv);
        }
    }
    
    // 更新表头
    function updateTableHeader() {
        dom.tableHeaderRow.innerHTML = '';
        
        for (let col = 0; col < columnCount; col++) {
            const th = document.createElement('th');
            th.textContent = columnTitles[col];
            dom.tableHeaderRow.appendChild(th);
        }
    }
    
    // 按列查询
    function searchByColumn(columnNumber) {
        if (excelData.length === 0) {
            alert('请先上传 Excel 文件');
            return;
        }
        
        const searchInput = document.getElementById(`searchColumn${columnNumber}`);
        const searchValue = searchInput?.value.trim() || '';
        
        console.log(`正在查询 ${columnTitles[columnNumber]}: "${searchValue}"`);
        
        let results = [];
        if (searchValue) {
            // 使用索引精确匹配（不区分大小写）
            const lowerSearchValue = searchValue.toLowerCase();
            results = columnIndexes[columnNumber][lowerSearchValue] || [];
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
            
            const tr = document.createElement('tr');
            tr.innerHTML = `<td colspan="${columnCount}" class="text-center text-muted">没有找到匹配的记录，请尝试其他查询条件</td>`;
            dom.resultsTable.appendChild(tr);
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
            
            for (let col = 0; col < columnCount; col++) {
                const td = document.createElement('td');
                td.innerHTML = highlightSearchTerm(row[`column${col}`], col);
                tr.appendChild(td);
            }
            
            dom.resultsTable.appendChild(tr);
        });
    }
    
    // 高亮显示搜索词
    function highlightSearchTerm(text, columnNumber) {
        const searchInput = document.getElementById(`searchColumn${columnNumber}`);
        const searchValue = searchInput?.value.trim() || '';
        
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
    
    function resetData() {
        excelData = [];
        columnIndexes = [];
        columnTitles = [];
        columnCount = 0;
        
        dom.noFileUploadedHint.style.display = 'block';
        dom.queryInputsContainer.innerHTML = '';
        dom.resultCount.textContent = '';
        dom.resultsTable.innerHTML = '';
        dom.tableHeaderRow.innerHTML = '<td colspan="3" class="text-center text-muted">请先上传Excel文件</td>';
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