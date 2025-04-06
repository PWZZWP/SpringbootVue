<template>
  <div class="Index-container">
    <!--头部开始-->
    <div style="height:80px;background-color:#38b9e0;display:flex;align-items:center;">
      <span style="width:200px;display: flex;align-items:center;">
        <img src="@/assets/logo.png" alt="" style="width:40px;margin-left: 20px;"/>
        <span style="font-size:15px;color:black;margin-left:10px">
       想试试流量监控？欢迎加入我们！</span>

      </span>
      <span>
          <el-image style="width: 80px; height: 80px" :src="url" />
        </span>
      <div style="flex:1;display:flex;align-items:center;padding-left:20px">

      </div>
      <div style="flex:1"></div>
      <div style="width:fit-content;display:flex;align-items:center;padding-right:20px">
        <div @click="router.push('/register')"  style="font-weight:bold;margin-right:10px;cursor:pointer">注册</div>
        <div  @click="router.push('/login')"  style="font-weight:bold;margin-right:10px;cursor:pointer">登录</div>
        <div  @click="handleOpen"  style="font-weight:bold;cursor:pointer">关于</div>
        <el-dialog title="关于我们的信息" v-model="data.formVisible"  width="500" destroy-on-close>
          <el-form :model="data.text" label-width="80px" style="padding-right:50px">
            <p>{{ data.text }}</p>
          </el-form>
          <template #footer>
            <div class="dialog-footer">
              <el-button @click="data.formVisible = false">关闭</el-button>
            </div>
          </template>
        </el-dialog>
      </div>
    </div>
    <!--头部结束-->
    <!--下面部分开始-->

    <!--下面部分结束-->
  </div>
  <div class="footer" >
    <div>
      本工具由面向现代化开发，提供使用！
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import router from "@/router/index.js";
import url from "@/assets/img2.png"
import {DataAnalysis, Document, Histogram, House, SwitchButton, User, UserFilled} from "@element-plus/icons-vue";

const data = reactive({
  text:"",
  formVisible:false,
})
const handleOpen=()=>{
  data.formVisible=true
  data.text="流量监控系统是基于Spring Boot开发的，主要用于电脑网卡流量监控，目前功能还比较简单，后续会逐步完善。"
  +"作者：平凡的世界，QQ：1194458225，欢迎交流。"
}

</script>

<style scoped>
.Index-container {
  height: 93vh;
  overflow: hidden;
  background-image: url('@/assets/index.jpeg');
  background-size: cover;
  body{margin:0}
}
.footer {
  background-color: #333;
  color: white;
  text-align: center;
  padding: 10px;
}

.New_button{
  background:
  linear-gradient(90deg,#03a9f4,#f441a5,#ffeb3b,#03a9f4);
}
</style>


