import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 可以在这里添加token等认证信息
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 添加请求时间戳
    config.headers['X-Requested-With'] = 'XMLHttpRequest'
    config.headers['X-Timestamp'] = Date.now()
    
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果响应码不是0，则判断为错误
    if (res.code !== undefined && res.code !== 0) {
      ElMessage({
        message: res.message || '请求失败',
        type: 'error',
        duration: 5 * 1000
      })
      
      // 处理特定错误码
      if (res.code === 401) {
        // 未授权，跳转到登录页
        ElMessageBox.confirm(
          '登录状态已过期，请重新登录',
          '系统提示',
          {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          // 清除token并跳转到登录页
          localStorage.removeItem('token')
          window.location.reload()
        })
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    } else {
      return res
    }
  },
  error => {
    console.error('响应错误:', error)
    
    let message = '网络错误'
    
    if (error.response) {
      // 服务器返回了错误状态码
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '未授权，请登录'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = `请求失败，状态码: ${error.response.status}`
      }
    } else if (error.request) {
      // 请求已发出，但没有收到响应
      message = '网络连接失败，请检查网络设置'
    } else {
      // 其他错误
      message = error.message || '未知错误'
    }
    
    ElMessage({
      message,
      type: 'error',
      duration: 5 * 1000
    })
    
    return Promise.reject(error)
  }
)

// 封装GET请求
export function get(url, params = {}) {
  return service({
    url,
    method: 'GET',
    params
  })
}

// 封装POST请求
export function post(url, data = {}) {
  return service({
    url,
    method: 'POST',
    data
  })
}

// 封装PUT请求
export function put(url, data = {}) {
  return service({
    url,
    method: 'PUT',
    data
  })
}

// 封装DELETE请求
export function del(url, params = {}) {
  return service({
    url,
    method: 'DELETE',
    params
  })
}

// 封装文件上传
export function upload(url, file, onUploadProgress) {
  const formData = new FormData()
  formData.append('file', file)
  
  return service({
    url,
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress
  })
}

// 封装文件下载
export function download(url, params = {}) {
  return service({
    url,
    method: 'GET',
    params,
    responseType: 'blob'
  })
}

export default service