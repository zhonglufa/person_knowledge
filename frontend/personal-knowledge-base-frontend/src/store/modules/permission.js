import { permissionApi } from '@/api/admin'

let isFetchingPermissions = false
let pendingPermissionPromise = null

const state = {
  permissions: JSON.parse(localStorage.getItem('userPermissions') || '[]'),
  roles: JSON.parse(localStorage.getItem('userRoles') || '[]'),
  routesAdded: false
}

const getters = {
  getPermissions: (state) => state.permissions,
  getRoles: (state) => state.roles,
  hasPermission: (state) => (permissionCode) => {
    return state.permissions.includes(permissionCode)
  },
  hasAnyPermission: (state) => (permissionCodes) => {
    if (!Array.isArray(permissionCodes)) {
      return state.permissions.includes(permissionCodes)
    }
    return permissionCodes.some(code => state.permissions.includes(code))
  },
  hasAllPermissions: (state) => (permissionCodes) => {
    if (!Array.isArray(permissionCodes)) {
      return state.permissions.includes(permissionCodes)
    }
    return permissionCodes.every(code => state.permissions.includes(code))
  },
  isRoutesAdded: (state) => state.routesAdded
}

const mutations = {
  SET_PERMISSIONS(state, permissions) {
    state.permissions = permissions || []
    localStorage.setItem('userPermissions', JSON.stringify(state.permissions))
  },
  SET_ROLES(state, roles) {
    state.roles = roles || []
    localStorage.setItem('userRoles', JSON.stringify(state.roles))
  },
  SET_ROUTES_ADDED(state, added) {
    state.routesAdded = added
  },
  CLEAR_PERMISSION_STATE(state) {
    state.permissions = []
    state.roles = []
    state.routesAdded = false
    localStorage.removeItem('userPermissions')
    localStorage.removeItem('userRoles')
  }
}

const actions = {
  async fetchUserPermissions({ commit, state }) {
    if (state.permissions.length > 0 && state.routesAdded) {
      return state.permissions
    }

    if (isFetchingPermissions && pendingPermissionPromise) {
      return pendingPermissionPromise
    }

    isFetchingPermissions = true
    pendingPermissionPromise = (async () => {
      try {
        const response = await permissionApi.getCurrentUserPermissions()
        const permissions = response.data || response
        commit('SET_PERMISSIONS', permissions)
        return permissions
      } catch (error) {
        console.error('获取用户权限失败:', error)
        commit('SET_PERMISSIONS', [])
        throw error
      } finally {
        isFetchingPermissions = false
        pendingPermissionPromise = null
      }
    })()

    return pendingPermissionPromise
  },

  async fetchUserRoles({ commit }, userId) {
    try {
      if (!userId) {
        commit('SET_ROLES', [])
        return []
      }

      const response = await permissionApi.getUserRoles(userId)
      const roles = response.data || response
      commit('SET_ROLES', roles)
      return roles
    } catch (error) {
      console.error('获取用户角色失败:', error)
      commit('SET_ROLES', [])
      throw error
    }
  },

  clearPermissionState({ commit }) {
    commit('CLEAR_PERMISSION_STATE')
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
}
