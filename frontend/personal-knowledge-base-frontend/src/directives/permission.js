import store from '@/store'

function checkPermission(el, binding) {
  const { value } = binding

  if (!value) {
    return
  }

  const permissions = store.state.permission.permissions || []

  let requiredPermissions = []
  if (Array.isArray(value)) {
    requiredPermissions = value
  } else if (typeof value === 'string') {
    requiredPermissions = [value]
  } else {
    console.warn('v-permission 指令值类型错误，应为字符串或数组')
    return
  }

  const hasPermission = requiredPermissions.some(code => permissions.includes(code))

  if (!hasPermission) {
    el.parentNode && el.parentNode.removeChild(el)
  }
}

export default {
  inserted(el, binding) {
    checkPermission(el, binding)
  },
  update(el, binding) {
    checkPermission(el, binding)
  }
}
