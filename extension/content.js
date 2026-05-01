// content.js - 内容脚本，注入到网页中

// 监听来自background的消息
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  console.log("Content script收到消息:", request);

  if (request.action === "getPageInfo") {
    try {
      const pageInfo = getPageInfo(request.contextInfo);
      console.log("获取到的页面信息:", pageInfo);
      sendResponse({ pageInfo: pageInfo });
    } catch (error) {
      console.error("获取页面信息失败:", error);
      sendResponse({ error: "获取页面信息失败: " + error.message });
    }
    return true; // 保持消息通道开放
  }
});

// 获取页面信息
function getPageInfo(contextInfo) {
  const pageInfo = {
    sourceUrl: window.location.href,
    title: document.title,
    sourceType: 1, // 默认为网页类型，对应后端的sourceType字段
    collectionId: 1 // 默认收藏集ID，实际应该从设置中获取
  };

  // 根据上下文信息判断内容类型
  if (contextInfo) {
    if (contextInfo.selectionText) {
      // 选中文本
      pageInfo.sourceType = 3; // 文本类型
      pageInfo.content = contextInfo.selectionText;
      pageInfo.title = `选中文本: ${document.title}`;
    } else if (contextInfo.srcUrl && isImageUrl(contextInfo.srcUrl)) {
      // 图片
      pageInfo.sourceType = 2; // 图片类型
      pageInfo.storageUrl = contextInfo.srcUrl; // 使用storageUrl字段存储图片地址
      pageInfo.title = `图片: ${document.title}`;
    } else if (contextInfo.linkUrl && isVideoUrl(contextInfo.linkUrl)) {
      // 视频链接
      pageInfo.sourceType = 4; // 视频类型
      pageInfo.storageUrl = contextInfo.linkUrl; // 使用storageUrl字段存储视频地址
      pageInfo.title = getVideoTitle(contextInfo.linkUrl);
    } else if (contextInfo.linkUrl) {
      // 普通链接
      pageInfo.storageUrl = contextInfo.linkUrl; // 使用storageUrl字段存储链接地址
      pageInfo.title = `链接: ${getPageTitle(contextInfo.linkUrl)}`;
    }
  }

  return pageInfo;
}

// 判断是否为图片URL
function isImageUrl(url) {
  const imageExtensions = /\.(jpg|jpeg|png|gif|bmp|webp|svg)$/i;
  return imageExtensions.test(url) || url.includes('images') || url.includes('img');
}

// 判断是否为视频URL
function isVideoUrl(url) {
  const videoPlatforms = [
    'youtube.com',
    'youtu.be',
    'bilibili.com',
    'youku.com',
    'v.qq.com',
    'iqiyi.com'
  ];

  return videoPlatforms.some(platform => url.includes(platform));
}

// 获取视频标题
function getVideoTitle(url) {
  if (url.includes('bilibili.com')) {
    return 'B站视频';
  } else if (url.includes('youku.com')) {
    return '优酷视频';
  } else if (url.includes('youtube.com') || url.includes('youtu.be')) {
    return 'YouTube视频';
  } else if (url.includes('v.qq.com')) {
    return '腾讯视频';
  } else if (url.includes('iqiyi.com')) {
    return '爱奇艺视频';
  } else {
    return '视频链接';
  }
}

// 获取页面标题
function getPageTitle(url) {
  // 在实际应用中，可能需要通过其他方式获取链接页面的标题
  return '链接页面';
}
