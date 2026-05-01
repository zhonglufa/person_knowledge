// options.js - 选项页面脚本

document.addEventListener('DOMContentLoaded', function() {
  const apiBaseUrlInput = document.getElementById('apiBaseUrl');
  const userTokenInput = document.getElementById('userToken');
  const saveBtn = document.getElementById('saveBtn');
  const messageEl = document.getElementById('message');

  // 加载已保存的设置
  chrome.storage.local.get(['apiBaseUrl', 'userToken'], (result) => {
    if (result.apiBaseUrl) {
      apiBaseUrlInput.value = result.apiBaseUrl;
    }
    if (result.userToken) {
      userTokenInput.value = result.userToken;
    }
  });

  // 保存设置
  saveBtn.addEventListener('click', () => {
    const apiBaseUrl = apiBaseUrlInput.value.trim();
    const userToken = userTokenInput.value.trim();

    if (!apiBaseUrl || !userToken) {
      showMessage('请填写完整的API地址和用户Token', 'error');
      return;
    }

    // 保存到chrome.storage
    chrome.storage.local.set({
      apiBaseUrl: apiBaseUrl,
      userToken: userToken
    }, () => {
      showMessage('设置已保存', 'success');
    });
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
});