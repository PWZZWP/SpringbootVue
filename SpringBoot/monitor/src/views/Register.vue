<template>
  <div class="login-container">
   <div class="login-box">
     <div style="padding:50px 30px;background-color: #fff;margin-left: 180px;border-radius:5px;
     box-shadow: 0 0 1000px rgba(0,0,0,50%);">
       <el-form ref="formRef" :rules="data.rules" :model="data.form" style="width:350px">
         <div style="margin-bottom: 30px;font-size:24px;text-align:center;color: deepskyblue;
         font-weight: bold;">欢迎加入我们！</div>
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

         <el-form-item prop="confirmPassword" >
           <el-input show-password prefix-icon="Lock" size="large" v-model="data.form.confirmPassword"
                     placeholder="请确认密码" >
           </el-input>
         </el-form-item>
         <div style="margin-bottom: 20px">
           <el-button style="width:100%" size="large" type="primary" @click="register">注册</el-button>
         </div>
         <div style="text-align:right">已有账号！请<a style="color:deepskyblue;text-decoration:none" href="/login">登录</a></div>
       </el-form>
     </div>

   </div>
  </div>
</template>

<script setup>

import { reactive,ref } from 'vue'
import request from "@/utils/request.js";
import {ElMessage} from "element-plus";

const validatePass = (rule,value,callback) => {
  //rule:当前规则验证对象，value:当前字段的值，callback:用于返回验证结果得回调函数
  if (value === '') {
    callback(new Error('请再次确认密码'))
  } else if (value !== data.form.password) {
    callback(new Error("两次输入密码不一致!"))
  } else {
    callback()
  }
}

const data = reactive({
  form:{},//初始化为空对象，允许后续动态添加字段
  rules:{
    username:[{required:true,message:'请输入账号',trigger:'blur'}],
    password:[{required:true,message:'请输入密码',trigger:'blur'}],
    confirmPassword:[{validator:validatePass,trigger:'blur'}]
  }
})

const formRef=ref()




const register = () => {
  formRef.value.validate((valid)=>{
    if(valid){
      request.post('/register',data.form).then(res=>{//res是HTTP请求的响应对象，用于接收后端API返回的数据
        //URL：'/register'（注册接口路径）Data:data.form(包含username,password,confirmPassword的表单数据)
        if( res.code==='200') {//注册成功
          console.log(res.data)
          ElMessage.success('注册成功')
            setTimeout(()=>{location.href='/login'},500)
        }
        else{
          ElMessage.error(res.msg)
        }
      })
    }
  })
  //异步操作：代码启动后不阻塞主线程。回调函数：一个函数作为参数传递给另一个函数。
  //序列化：Http请求的Body只能传输字符串（Text,JSON,Binary等），而JavaScript对象data.form无法直接传输。
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