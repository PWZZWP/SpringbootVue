import axios from "axios";
import {ElMessage} from "element-plus";

const request = axios.create({
    baseURL: 'http://localhost:9090',
    timeout: 5000  // 后台接口超时时间
})

// request 拦截器
// 可以在请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    //根据请求数据类型动态设置Content-Type
    if(config.data instanceof FormData){
        delete config.headers['Content-Type']
    }else{
        config.headers['Content-Type'] = 'application/json;charset=utf-8';
    }
    let user = JSON.parse(localStorage.getItem('monitor-user') || '{}')
    config.headers['token'] = user.token
    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res//当res为假值时，返回res.res为真值：如"data","123"
        }
        return res;//JSON.parse()将JSON字符串转换为JavaScript对象；JSON.stringify()将JavaScript对象转换为JSON字符串
    },
    error => {
        if (error.response.status === 404) {
            ElMessage.error('未找到请求接口')
        } else if (error.response.status === 500) {
            ElMessage.error('系统异常，请查看后端控制台报错')
        } else {
            console.error(error.message)
        }
        return Promise.reject(error)
    }
)

export default request