import DOMPurify from 'dompurify'

export function sanitizeHtml(html) {
  if (!html) return ''
  return DOMPurify.sanitize(String(html), {
    ALLOWED_TAGS: ['mark', 'b', 'i', 'em', 'strong', 'br', 'p', 'span', 'a', 'ul', 'ol', 'li', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'img', 'div', 'u', 's', 'blockquote', 'code', 'pre', 'table', 'thead', 'tbody', 'tr', 'th', 'td'],
    ALLOWED_ATTR: ['href', 'title', 'target', 'rel', 'src', 'alt', 'class', 'id', 'style']
  })
}

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

export function stripHtml(html) {
  if (!html) return ''
  return String(html).replace(/<[^>]*>/g, '')
}

export function stripHtmlAndEscape(html) {
  if (!html) return ''
  const text = String(html).replace(/<[^>]*>/g, '')
  return escapeHtml(text)
}
