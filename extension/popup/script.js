// popup/script.js - 弹出窗口脚本

document.addEventListener('DOMContentLoaded', function() {
  const saveBtn = document.getElementById('saveBtn');
  const collectionSelect = document.getElementById('collection');
  const typeSelect = document.getElementById('type');
  const messageEl = document.getElementById('message');
  const settingsLink = document.getElementById('settingsLink');
  const collectionStatus = document.getElementById('collectionStatus');

  // 页面加载时获取用户的收藏集列表
  loadUserCollections();

  // 保存当前页面
  saveBtn.addEventListener('click', () => {
    // 显示加载状态
    saveBtn.textContent = '收藏中...';
    saveBtn.disabled = true;

    // 获取用户选择的设置
    const collectionId = parseInt(collectionSelect.value);
    const sourceType = parseInt(typeSelect.value); // 改名为sourceType以匹配后端字段

    // 发送消息到background script
    chrome.runtime.sendMessage({
      action: "saveCurrentPage",
      collectionId: collectionId,
      type: sourceType // 传递sourceType参数
    }, (response) => {
      console.log("从background收到响应:", response);
      
      // 恢复按钮状态
      saveBtn.textContent = '收藏当前页面';
      saveBtn.disabled = false;

      // 显示结果
      if (response && response.success) {
        showMessage('收藏成功！', 'success');
        // 2秒后关闭弹窗
        setTimeout(() => {
          window.close();
        }, 2000);
      } else {
        // 根据不同的错误类型显示不同的消息
        let errorMessage = response ? response.error : '未知错误';
        
        // 精确匹配特定错误
        if (errorMessage.includes('已被收藏')) {
          showMessage('该链接已被收藏', 'warning');
        } else if (errorMessage.includes('无法与页面通信')) {
          showMessage('无法与当前页面通信，请刷新页面后重试', 'error');
        } else if (errorMessage.includes('获取页面信息失败')) {
          showMessage('获取页面信息失败：' + errorMessage, 'error');
        } else if (errorMessage.includes('请先设置API地址') || errorMessage.includes('Token')) {
          showMessage('请先在设置中配置API地址和Token', 'error');
        } else if (errorMessage.includes('网络连接失败') || errorMessage.includes('HTTP error! status: 0')) {
          showMessage('网络连接失败，请检查网络设置和API地址配置', 'error');
        } else if (errorMessage.includes('认证失败') || errorMessage.includes('401')) {
          showMessage('认证失败，请检查Token设置是否正确', 'error');
        } else if (errorMessage.includes('API端点不存在') || errorMessage.includes('404')) {
          showMessage('API端点不存在，请检查API地址配置是否正确', 'error');
        } else if (errorMessage.includes('服务器内部错误') || errorMessage.includes('500')) {
          showMessage('服务器内部错误，请稍后重试', 'error');
        } else {
          // 显示原始错误信息
          showMessage('收藏失败: ' + errorMessage, 'error');
        }
      }
    });
  });

  // 设置链接
  settingsLink.addEventListener('click', (e) => {
    e.preventDefault();
    // 打开设置页面（这里可以打开一个选项页）
    chrome.runtime.openOptionsPage();
  });

  // 显示消息
  function showMessage(text, type) {
    messageEl.textContent = text;
    messageEl.className = 'message ' + type;
    messageEl.style.display = 'block';

    // 3秒后隐藏消息
    setTimeout(() => {
      messageEl.style.display = 'none';
    }, 3000);
  }

  // 加载用户的收藏集列表
  async function loadUserCollections() {
    try {
      // 显示加载状态
      collectionSelect.innerHTML = '<option value="">正在加载收藏集...</option>';
      collectionSelect.disabled = true;
      collectionStatus.textContent = '加载中...';
      collectionStatus.style.color = '#666';

      // 发送消息到background script获取收藏集列表
      chrome.runtime.sendMessage({
        action: "getUserCollections"
      }, (response) => {
        collectionSelect.disabled = false;
        
        if (response && response.success) {
          // 清空现有选项
          collectionSelect.innerHTML = '';
          
          // 检查是否有收藏集数据
          if (response.collections && response.collections.length > 0) {
            // 添加获取到的收藏集
            response.collections.forEach(collection => {
              const option = document.createElement('option');
              option.value = collection.id;
              option.textContent = `${collection.name} (${collection.itemCount || 0}项)`;
              collectionSelect.appendChild(option);
            });
            
            // 默认选中第一个收藏集
            if (collectionSelect.options.length > 0) {
              collectionSelect.selectedIndex = 0;
            }
            
            // 更新状态显示
            collectionStatus.textContent = `(${response.collections.length}个)`;
            collectionStatus.style.color = '#4CAF50';
            
            showMessage(`成功加载 ${response.collections.length} 个收藏集`, 'success');
          } else {
            // 没有收藏集的情况
            const option = document.createElement('option');
            option.value = "1";
            option.textContent = "默认收藏集 (空)";
            collectionSelect.appendChild(option);
            
            // 更新状态显示
            collectionStatus.textContent = '(空)';
            collectionStatus.style.color = '#FF9800';
            
            showMessage('暂无收藏集，将使用默认收藏集', 'warning');
          }
        } else {
          // 获取失败时的处理
          collectionSelect.innerHTML = `
            <option value="1">默认收藏集</option>
            <option value="2">学习资料</option>
            <option value="3">工作文档</option>
          `;
          
          // 更新状态显示
          collectionStatus.textContent = '(加载失败)';
          collectionStatus.style.color = '#F44336';
          
          // 显示具体错误信息
          const errorMessage = response ? response.error : '未知错误';
          let displayMessage = '无法获取收藏集列表';
          
          if (errorMessage.includes('Token')) {
            displayMessage += '：请检查Token设置是否正确';
          } else if (errorMessage.includes('401') || errorMessage.includes('认证')) {
            displayMessage += '：Token认证失败，请重新登录获取Token';
          } else if (errorMessage.includes('404') || errorMessage.includes('API端点不存在')) {
            displayMessage += '：API地址配置错误';
          } else if (errorMessage.includes('网络') || errorMessage.includes('HTTP error! status: 0')) {
            displayMessage += '：网络连接失败，请检查网络和API地址';
          } else {
            displayMessage += `：${errorMessage}`;
          }
          
          showMessage(displayMessage, 'error');
        }
      });
    } catch (error) {
      console.error('加载收藏集列表失败:', error);
      // 出错时使用默认选项
      collectionSelect.innerHTML = `
        <option value="1">默认收藏集</option>
        <option value="2">学习资料</option>
        <option value="3">工作文档</option>
      `;
      collectionSelect.disabled = false;
      collectionStatus.textContent = '(错误)';
      collectionStatus.style.color = '#F44336';
      
      showMessage(`加载收藏集失败：${error.message}`, 'error');
    }
  }
});