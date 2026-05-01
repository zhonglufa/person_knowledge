// background.js - 浏览器扩展后台脚本

// 创建右键菜单
chrome.runtime.onInstalled.addListener(() => {
  chrome.contextMenus.create({
    id: "saveToKnowledgeBase",
    title: "收藏到个人知识库",
    contexts: ["page", "selection", "image", "link"]
  });
});

// 监听右键菜单点击事件
chrome.contextMenus.onClicked.addListener((info, tab) => {
  if (info.menuItemId === "saveToKnowledgeBase") {
    handleContextMenuClick(info, tab);
  }
});

// 处理右键菜单点击
function handleContextMenuClick(info, tab) {
  // 发送消息到内容脚本获取页面信息
  chrome.tabs.sendMessage(tab.id, {
    action: "getPageInfo",
    contextInfo: info
  }, (response) => {
    if (response && response.pageInfo) {
      sendToKnowledgeBase(response.pageInfo);
    }
  });
}

// 监听来自popup的消息
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.action === "saveCurrentPage") {
    // 获取当前标签页信息
    chrome.tabs.query({ active: true, currentWindow: true }, (tabs) => {
      const activeTab = tabs[0];
      console.log("当前活动标签页:", activeTab);
      
      // 发送消息到内容脚本获取页面信息
      chrome.tabs.sendMessage(activeTab.id, {
        action: "getPageInfo"
      }, (response) => {
        console.log("从content script收到响应:", response);
        
        if (chrome.runtime.lastError) {
          console.error("与content script通信失败:", chrome.runtime.lastError.message);
          sendResponse({ 
            success: false, 
            error: "无法与页面通信: " + chrome.runtime.lastError.message 
          });
          return;
        }
        
        if (response && response.pageInfo) {
          // 使用用户设置的收藏集ID和类型
          response.pageInfo.collectionId = request.collectionId || response.pageInfo.collectionId;
          response.pageInfo.sourceType = request.type || response.pageInfo.sourceType;

          sendToKnowledgeBase(response.pageInfo)
            .then(result => sendResponse({ success: true, data: result }))
            .catch(error => sendResponse({ success: false, error: error.message }));
          return true; // 保持消息通道开放以进行异步响应
        } else if (response && response.error) {
          sendResponse({ success: false, error: response.error });
        } else {
          sendResponse({ success: false, error: "无法获取页面信息" });
        }
      });
    });
    return true; // 保持消息通道开放
  } else if (request.action === "getUserCollections") {
    // 获取用户的收藏集列表
    getUserCollections()
      .then(collections => sendResponse({ success: true, collections: collections }))
      .catch(error => sendResponse({ success: false, error: error.message }));
    return true; // 保持消息通道开放
  }
});

// 发送到知识库后端
async function sendToKnowledgeBase(pageInfo) {
  try {
    // 获取后端API地址和用户token
    const settings = await new Promise(resolve => {
      chrome.storage.local.get(['apiBaseUrl', 'userToken'], resolve);
    });

    const { apiBaseUrl, userToken } = settings;

    if (!apiBaseUrl || !userToken) {
      throw new Error("请先在扩展选项中设置API地址和用户Token");
    }
    
    // 验证Token格式
    if (!userToken.startsWith('eyJ')) {
      throw new Error("Token格式不正确，请确保复制的是完整的JWT Token");
    }

    // 调试信息
    console.log("API配置信息:", {
      apiBaseUrl: apiBaseUrl,
      userToken: userToken ? "[已设置]" : "[未设置]",
      fullUrl: `${apiBaseUrl}/collect/add-collect`
    });

    // 构造请求头
    const headers = {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${userToken}`
    };

    // 首先检查URL是否已存在
    try {
      const checkUrlResponse = await fetch(`${apiBaseUrl}/collect/check-url?url=${encodeURIComponent(pageInfo.storageUrl || pageInfo.url)}`, {
        method: 'GET',
        headers: headers
      });

      if (checkUrlResponse.ok) {
        const checkResult = await checkUrlResponse.json();
        if (checkResult.code === 200 && checkResult.data && checkResult.data.exists) {
          throw new Error("该链接已被收藏");
        }
      }
      // 如果检查失败，继续执行收藏操作（不影响主要功能）
    } catch (checkError) {
      console.warn("检查URL是否存在时出错:", checkError.message);
      // 不阻止收藏操作继续执行
    }

    // 发送POST请求到后端，使用正确的API端点
    const fullUrl = `${apiBaseUrl}/collect/add-collect`;
    console.log("发送请求到:", fullUrl);
    console.log("请求头:", headers);
    console.log("请求体:", JSON.stringify(pageInfo, null, 2));
    
    const response = await fetch(fullUrl, {
      method: 'POST',
      headers: headers,
      body: JSON.stringify(pageInfo)
    });
    
    console.log("响应状态:", response.status);
    console.log("响应头:", [...response.headers.entries()]);

    if (!response.ok) {
      // 根据不同的HTTP状态码提供更具体的错误信息
      let errorMsg = `HTTP error! status: ${response.status}`;
      switch(response.status) {
        case 401:
          errorMsg = '认证失败，请检查Token设置';
          break;
        case 403:
          errorMsg = '权限不足，无法访问该资源';
          break;
        case 404:
          errorMsg = 'API端点不存在，请检查API地址配置';
          break;
        case 500:
          errorMsg = '服务器内部错误';
          break;
        case 0:
          errorMsg = '网络连接失败，请检查网络设置和API地址';
          break;
        default:
          if (response.status >= 500) {
            errorMsg = `服务器错误 (${response.status})`;
          } else if (response.status >= 400) {
            errorMsg = `客户端错误 (${response.status})`;
          }
      }
      throw new Error(errorMsg);
    }

    const result = await response.json();

    // 检查后端返回的结构是否符合预期 {code, message, data}
    if (result.code === 200) {
      console.log("收藏成功:", result);
      return result.data;
    } else {
      throw new Error(result.message || "收藏失败");
    }
  } catch (error) {
    console.error("收藏失败:", error);
    throw error;
  }
}

// 获取用户的收藏集列表
async function getUserCollections() {
  try {
    // 获取后端API地址和用户token
    const settings = await new Promise(resolve => {
      chrome.storage.local.get(['apiBaseUrl', 'userToken'], resolve);
    });

    const { apiBaseUrl, userToken } = settings;

    // 详细验证配置
    if (!apiBaseUrl) {
      throw new Error("未设置API基础地址，请在扩展设置中配置");
    }
    
    if (!userToken) {
      throw new Error("未设置用户Token，请在扩展设置中配置");
    }
    
    // 验证Token格式
    if (!userToken.startsWith('eyJ')) {
      throw new Error("Token格式不正确，请确保复制的是完整的JWT Token");
    }

    // 验证API地址格式
    try {
      new URL(apiBaseUrl);
    } catch (urlError) {
      throw new Error("API地址格式不正确，请输入完整的URL地址");
    }

    // 构造请求头
    const headers = {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${userToken}`
    };

    // 发送GET请求获取收藏集列表
    const collectionsUrl = `${apiBaseUrl}/collections/my-collection`;
    console.log("获取收藏集列表:", collectionsUrl);
    
    const response = await fetch(collectionsUrl, {
      method: 'GET',
      headers: headers
    });
    
    console.log("收藏集API响应状态:", response.status);
    
    // 详细的HTTP状态码处理
    if (!response.ok) {
      let errorMsg = `获取收藏集列表失败 (HTTP ${response.status})`;
      
      switch(response.status) {
        case 401:
          errorMsg = 'Token认证失败，请检查Token是否正确或已过期';
          break;
        case 403:
          errorMsg = '权限不足，无法访问收藏集列表';
          break;
        case 404:
          errorMsg = 'API端点不存在，请检查API地址配置是否正确';
          break;
        case 500:
          errorMsg = '服务器内部错误，请稍后重试';
          break;
        case 0:
          errorMsg = '网络连接失败，请检查网络设置和API地址';
          break;
        default:
          if (response.status >= 500) {
            errorMsg = `服务器错误 (${response.status})，请稍后重试`;
          } else if (response.status >= 400) {
            errorMsg = `客户端错误 (${response.status})，请检查配置`;
          }
      }
      
      // 尝试获取详细的错误信息
      try {
        const errorResult = await response.json();
        if (errorResult.message) {
          errorMsg += `: ${errorResult.message}`;
        }
      } catch (parseError) {
        // 解析错误信息失败，使用默认错误信息
      }
      
      throw new Error(errorMsg);
    }

    const result = await response.json();
    console.log("收藏集API响应数据:", result);
    
    // 根据后端API返回格式处理数据
    if (result.code === 200) {
      // 如果返回的是分页数据结构
      if (result.data && result.data.records) {
        return result.data.records;
      } 
      // 如果直接返回数组
      else if (Array.isArray(result.data)) {
        return result.data;
      }
      // 如果返回对象
      else if (result.data) {
        return [result.data];
      }
      // 空数据情况
      return [];
    } else {
      throw new Error(result.message || `API返回错误 (code: ${result.code})`);
    }
  } catch (error) {
    console.error("获取收藏集列表失败:", error);
    
    // 提供更有帮助的错误信息
    let userFriendlyError = error.message;
    
    // 网络相关错误的优化
    if (error instanceof TypeError && error.message.includes('fetch')) {
      userFriendlyError = '网络请求失败，请检查网络连接和API地址配置';
    }
    
    // CORS错误处理
    if (error.message.includes('CORS') || error.message.includes('blocked')) {
      userFriendlyError = '跨域请求被阻止，请检查API服务器的CORS配置';
    }
    
    throw new Error(userFriendlyError);
  }
}
