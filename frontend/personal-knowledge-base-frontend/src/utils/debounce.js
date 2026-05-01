/**
 * 防抖工具函数库
 * 限制函数在指定时间内只执行最后一次调用
 */

/**
 * 基础防抖函数
 * @param {Function} func - 需要防抖的函数
 * @param {number} wait - 等待时间（毫秒），默认300ms
 * @param {Object} options - 配置选项
 * @param {boolean} options.leading - 是否立即执行（首次调用时），默认false
 * @param {boolean} options.trailing - 是否在等待结束后执行，默认true
 * @param {Function} options.onBefore - 执行前回调
 * @param {Function} options.onAfter - 执行后回调
 * @returns {Function} 防抖后的函数
 */
export function debounce(func, wait = 300, options = {}) {
  const {
    leading = false,
    trailing = true,
    onBefore,
    onAfter
  } = typeof options === 'boolean' ? { leading: options } : options;

  let timeout = null;
  let lastArgs = null;
  let lastThis = null;
  let result = null;

  function invokeFunc() {
    if (onAfter) onAfter();
    result = func.apply(lastThis, lastArgs);
    timeout = null;
    return result;
  }

  function startTimer() {
    return setTimeout(() => {
      if (trailing) {
        invokeFunc();
      } else {
        timeout = null;
      }
    }, wait);
  }

  function leadingEdge() {
    timeout = startTimer();
    if (leading) {
      return invokeFunc();
    }
    return result;
  }

  function debounced(...args) {
    lastArgs = args;
    lastThis = this;

    if (onBefore) onBefore();

    if (timeout === null) {
      return leadingEdge();
    }

    if (trailing) {
      clearTimeout(timeout);
      timeout = startTimer();
    }

    return result;
  }

  // 取消待执行的调用
  debounced.cancel = function() {
    if (timeout) {
      clearTimeout(timeout);
      timeout = null;
    }
  };

  // 立即执行待执行的调用
  debounced.flush = function() {
    if (timeout) {
      clearTimeout(timeout);
      return invokeFunc();
    }
    return result;
  };

  return debounced;
}

/**
 * 节流函数
 * 限制函数在指定时间内最多执行一次
 * @param {Function} func - 需要节流的函数
 * @param {number} limit - 时间限制（毫秒），默认300ms
 * @param {Object} options - 配置选项
 * @param {boolean} options.noLeading - 是否禁止首次执行，默认false
 * @param {boolean} options.noTrailing - 是否禁止末尾执行，默认false
 * @returns {Function} 节流后的函数
 */
export function throttle(func, limit = 300, options = {}) {
  const { noLeading = false, noTrailing = false } = options;
  
  let inThrottle = false;
  let lastArgs = null;
  let lastThis = null;
  let result = null;
  let timeout = null;

  function executionTrailer() {
    if (!noTrailing && lastArgs) {
      result = func.apply(lastThis, lastArgs);
      lastArgs = null;
      lastThis = null;
    }
    inThrottle = false;
    timeout = null;
  }

  function throttled(...args) {
    lastArgs = args;
    lastThis = this;

    if (!inThrottle) {
      if (!noLeading) {
        result = func.apply(this, args);
      }
      inThrottle = true;
      timeout = setTimeout(executionTrailer, limit);
    } else if (noTrailing) {
      // 如果禁止末尾执行，则更新参数但不设置定时器
      lastArgs = args;
      lastThis = this;
    }

    return result;
  }

  throttled.cancel = function() {
    if (timeout) {
      clearTimeout(timeout);
      timeout = null;
      inThrottle = false;
    }
  };

  return throttled;
}

/**
 * 搜索防抖（专门用于搜索场景）
 * @param {Function} searchFunc - 搜索函数
 * @param {number} delay - 延迟时间（毫秒），默认500ms
 * @returns {Function} 搜索防抖函数
 */
export function debounceSearch(searchFunc, delay = 500) {
  return debounce(searchFunc, delay, {
    trailing: true,
    onBefore: () => {
      // 可以在这里添加loading状态
    },
    onAfter: () => {
      // 可以在这里结束loading状态
    }
  });
}

