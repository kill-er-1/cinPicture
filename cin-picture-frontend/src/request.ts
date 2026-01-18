import axios from 'axios'
import { message } from 'ant-design-vue'

const myAxios = axios.create({
  baseURL: '',
  timeout: 60000, //60s
  withCredentials: true, //cookie
})

myAxios.interceptors.request.use(
  //在请求发送前修改 config（比如加 token）成功回调
  function (config) {
    return config
  },
  function (error) {
    // promise 异步调用时三种状态 进行 完成 失败 这里是失败
    return Promise.reject(error)
  },
)

myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    if (data.code === 40100) {
      if (
        !response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
        //？为了防止循环跳转，两种情况下不能跳转
        /*
        1.获取当前登录信息
        2.已经处于登录界面
         */
      ) {
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
        //？
        /*
        1.浏览器原生跳转（整页刷新到登录页）
        2.window.location.href
        window.location.href：当前页面完整 URL（比如 http://localhost:5173/about）。
        拼接后登录页 URL 变成 /user/login?redirect=http://localhost:5173/about。
        作用：登录成功后，后端或前端登录页面可以读取这个 redirect 参数，自动跳回原页面（用户体验好，不用手动返回）。
        */
      }
    }
    return response
  },
  function (error) {
    return Promise.reject(error)
  },
)

export default myAxios
