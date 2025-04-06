<template>
  <div class="login-container">
   <div class="login-box">
     <div style="padding:50px 30px;background-color: #fff;margin-left: 180px;border-radius:5px;
     box-shadow: 0 0 10px rgba(0,0,0,0.1);">
       <el-form ref="formRef" :rules="data.rules" :model="data.form" style="width:350px">
         <div style="margin-bottom: 30px;font-size:24px;text-align:center;color: deepskyblue;
         font-weight: bold;">欢迎来到流量监控</div>


         <el-form-item prop="username">
           <el-input prefix-icon="User" size="large" v-model="data.form.username"
                     placeholder="请输入账号">
           </el-input>
         </el-form-item>


         <el-form-item prop="password" >
           <el-input show-password prefix-icon="Lock" size="large" v-model="data.form.password"
                     placeholder="请输入密码" >
           </el-input>
         </el-form-item>


         <el-form-item prop="role">
           <el-select v-model="data.form.role" style="width:100%" size="large">
             <el-option label="管理员" value="ADMIN"></el-option>
             <el-option label="用户" value="USER"></el-option>
           </el-select>
         </el-form-item>

         <div style="margin-bottom: 20px">
           <el-button style="width:100%" size="large" type="primary" @click="login">登录</el-button>
         </div>
         <div style="text-align:right">还没有账号？<a style="color:deepskyblue;text-decoration:none" href="/register">立即注册</a></div>
       </el-form>
     </div>

   </div>
  </div>
</template>

<script setup>

import { reactive,ref } from 'vue'
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";


const data = reactive({
  form:{role:'ADMIN'},
  rules:{
    username:[{required:true,message:'请输入账号',trigger:'blur'}],
    password:[{required:true,message:'请输入密码',trigger:'blur'}]
  }
})

const formRef=ref()

const login = () => {
  formRef.value.validate((valid)=>{
    if(valid){
      request.post('/login',data.form).then(res=>{
        if( res.code==='200') {//后端返回的code码，200表示登录成功
          // localStorage是浏览器的本地存储。setItem(key,value)方法可以将数据存储在本地，keys是存储的键，value是存储的值，value是字符串类型
          localStorage.setItem('monitor-user',JSON.stringify(res.data))
          console.log(res.data)
          ElMessage.success('登陆成功')//用户提交表单，后端验证通过，前端需要展示成功提示
            setTimeout(()=>{location.href='/manager/data'},500)
          //setTimeout()js内置函数，用于在指定延迟后执行代码块。
          //（）=>{}匿名箭头函数，可以将函数作为参数传入setTimeout()方法，并指定延迟时间。
          //location.href='/manager/home',修改当前页面。
        }
        else{
          ElMessage.error(res.msg)
        }
      })
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  overflow: hidden;
  background-image: url('@/assets/login2.jpg');
  background-size: cover;

}
.login-box {
  position: absolute;
  width:50%;
  right:0;
  height:100%;
  display:flex;
  align-items:center;

}
</style>