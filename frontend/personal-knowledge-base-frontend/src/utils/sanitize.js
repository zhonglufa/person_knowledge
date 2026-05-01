import DOMPurify from 'dompurify'

/**
 * HTML消毒工具
 * 过滤用户输入内容，防止XSS攻击
 */
export function sanitizeHtml(html) {
  if (!html) return ''
  return DOMPurify.sanitize(String(html), {
    ALLOWED_TAGS: ['mark', 'b', 'i', 'em', 'strong', 'br', 'p', 'span', 'a', 'ul', 'ol', 'li', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'img', 'div', 'u', 's', 'blockquote', 'code', 'pre', 'table', 'thead', 'tbody', 'tr', 'th', 'td'],
    ALLOWED_ATTR: ['href', 'title', 'target', 'rel', 'src', 'alt', 'class', 'id', 'style']
  })
}

/**
 * HTML转义（纯文本输出）
 */
export function escapeHtml(text) {
  if (!text) return ''
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  }
  return String(text).replace(/[&<>"']/g, m => map[m])
}
