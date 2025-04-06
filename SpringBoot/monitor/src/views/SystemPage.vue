<template>
  <div>
    <!--头部开始-->
    <div style="height:60px;background-color:#38b9e0;display:flex;align-items:center;">
    <div style="width:200px;display: flex;align-items:center;">
      <img src="@/assets/logo.png" alt="" style="width:40px;margin-left: 20px;"/>
      <span style="font-size:20px;color:white;margin-left:10px">
       <span v-if="data.user.role ==='ADMIN'">后台管理系统</span>
       <span v-if="data.user.role ==='USER'">流量监控系统</span>
      </span>
    </div>
      <div style="flex:1;display:flex;align-items:center;padding-left:20px">
        首页 / {{ router.currentRoute.value.meta.title }}
      </div>
    <div style="flex:1"></div>
      <div style="width:fit-content;display:flex;align-items:center;padding-right:20px">
        <div>
          <el-dropdown>
            <img :src="data.user.avatar" alt="" style="width:50px;height:50px;border-radius:50%"/>
            <template #dropdown>
              <el-dropdown-menu>
                <div @click="logout" style="cursor: pointer;">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </div>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <span style="margin-left:10px;color:mediumpurple;margin-right:10px">{{ data.user.name }}
        </span>
<!--        <el-button ><el-icon><SwitchButton /></el-icon>退出登录</el-button>-->
      </div>
    </div>
    <!--头部结束-->
    <!--下面部分开始-->
    <div style="display:flex;">
      <!--左侧导航菜单开始-->
      <div style="width:200px;border-right:1px solid #EBEBEB;min-height:calc(100vh - 60px);overflow:auto;">
        <el-menu router style="border:0;min-height:calc(100vh - 60px)" :default-active="router.currentRoute.value.path"
                 :default-openeds="[]">


          <el-menu-item index="/manager/modelManage" v-if="data.user.role ==='ADMIN'">
            <el-icon><DataAnalysis /></el-icon>
            模型管理</el-menu-item>

          <el-menu-item index="/manager/model" v-if="data.user.role ==='USER'">
            <el-icon><DataAnalysis /></el-icon>
            实时流量监控</el-menu-item>


          <el-menu-item index="/manager/data" >
            <el-icon><Histogram /></el-icon>
            <span v-if="data.user.role ==='USER'">历史流量</span>
            <span v-else>网络流量管理</span>
          </el-menu-item>

<!--          <el-sub-menu index="1" v-if = "data.user.role ==='ADMIN'" >-->
<!--            <template #title>-->
<!--              <el-icon><User /></el-icon>-->
<!--              <span>用户管理</span>-->
<!--            </template>-->
<!--          </el-sub-menu>-->
<!--            <el-menu-item index="/manager/admin">管理员信息</el-menu-item>-->
            <el-menu-item index="/manager/user" v-if="data.user.role ==='ADMIN'">
              <el-icon><User /></el-icon>
              用户信息</el-menu-item>


          <el-menu-item index="/manager/article">
            <el-icon><Document /></el-icon>
            <span v-if="data.user.role ==='ADMIN'">留言管理</span>
            <span v-else>留言区</span>
          </el-menu-item>

<!--          <el-sub-menu index="2">-->
<!--            <template #title>-->
<!--              <span>个人信息管理</span>-->
<!--            </template>-->
<!--          </el-sub-menu>-->
            <el-menu-item index="/manager/person">
              <el-icon><UserFilled /></el-icon>个人信息管理
            </el-menu-item>

<!--            <el-menu-item index="/manager/password">-->
<!--              <el-icon><Lock /></el-icon>修改密码-->
<!--            </el-menu-item>-->
<!--           <el-menu-item @click="logout">-->
<!--            <el-icon><SwitchButton /></el-icon>退出登录-->
<!--           </el-menu-item>-->
        </el-menu>
      </div>
      <!--左侧导航菜单结束-->

      <!--右侧主体内容开始-->
      <div style="flex:1;width:0;background-color: #f8f8ff">
        <RouterView @updateUser="updateUser"/>
      </div>
     <!--右侧主体内容结束-->
    </div>
    <!--下面部分结束-->
  </div>
</template>

<script setup>
import {DataAnalysis, Document, Histogram, House, SwitchButton, User, UserFilled} from "@element-plus/icons-vue";
import router from "@/router/index.js";
import {reactive} from "vue";
import Article from "@/views/ArticleInfo.vue";

const data=reactive({
  user: JSON.parse(localStorage.getItem('monitor-user'))
})

const logout=()=>{
  localStorage.removeItem('monitor-user')//清除当前用户信息
  location.href='/login'//退出到登陆页面
}

const updateUser=()=>{
  data.user=JSON.parse(localStorage.getItem('monitor-user'))
}
</script>
<style>
.el-menu {
  min-height: calc(100vh - 60px);
  background-color: #8c939d !important;
}

.el-sub-menu .is-active {
  background-color: white !important;

}
</style>