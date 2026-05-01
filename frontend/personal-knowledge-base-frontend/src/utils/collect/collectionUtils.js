// 工具函数
export const getCollectionTypeIcon = (type) => {
  const icons = {
    text: 'fas fa-file-alt',
    image: 'fas fa-image',
    link: 'fas fa-link',
    document: 'fas fa-file-pdf'
  };
  return icons[type] || 'fas fa-question-circle';
};

export const getCollectionTypeName = (type) => {
  const names = {
    text: '文本',
    image: '图片',
    link: '链接',
    document: '文档'
  };
  return names[type] || '未知';
};

export const truncateText = (text, length) => {
  if (!text) return '';
  return text.length > length ? text.substring(0, length) + '...' : text;
};

export const formatTime = (timeString) => {
  const time = new Date(timeString);
  const now = new Date();
  const diff = now - time;
  
  if (time.toDateString() === now.toDateString()) {
    return `今天 ${time.getHours().toString().padStart(2, '0')}:${time.getMinutes().toString().padStart(2, '0')}`;
  }
  
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (time.toDateString() === yesterday.toDateString()) {
    return '昨天';
  }
  
  if (diff < 3 * 24 * 60 * 60 * 1000) {
    const days = Math.floor(diff / (24 * 60 * 60 * 1000));
    return `${days}天前`;
  }
  
  return `${time.getFullYear()}-${(time.getMonth() + 1).toString().padStart(2, '0')}-${time.getDate().toString().padStart(2, '0')}`;
};