<template>
  <div class="card" style="width:50%;padding:40px 20px;margin:auto;">
    <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="100px" style="padding-right:50px">
      <el-form-item label="头像">
        <div>
          <el-upload
              class="avatar-uploader"
              action="http://localhost:9090/files/upload"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
          >
            <img v-if="data.form.avatar" :src="data.form.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </div>
      </el-form-item>

      <el-form-item label="账号" prop="username">
        <el-input disabled v-model="data.form.username" autocomplete="off" placeholder="请输入账号" />
      </el-form-item>

      <el-form-item label="名称" prop="name">
        <el-input v-model="data.form.name" autocomplete="off" placeholder="请输入名称" />
      </el-form-item>
      <div v-if="data.user.role==='USER'">
        <el-form-item label="性别" >
          <el-radio-group v-model="data.form.sex">
            <el-radio value="男" label="男"></el-radio>
            <el-radio value="女" label="女"></el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="描述" >
          <el-input :rows="3" type="textarea" v-model="data.form.description" autocomplete="off" placeholder="请输入个人描述" />
        </el-form-item>
      </div>

      <el-form-item label="原密码" prop="password">
        <el-input show-password v-model="data.form.password" autocomplete="off" placeholder="请输入原密码" />
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input show-password v-model="data.form.newPassword" autocomplete="off" placeholder="请输入新密码" />
      </el-form-item>

      <el-form-item label="确认新密码" prop="confirmPassword" required>
        <el-input show-password v-model="data.form.confirmPassword" autocomplete="off" placeholder="请再次输入新密码" />
      </el-form-item>

      <div style="text-align:center">
      <el-button @click="updateUser" type="primary" style="padding:20px 30px">更新个人信息</el-button>
    </div>
    </el-form>
  </div>
</template>


<script setup>
import {reactive,ref} from 'vue';
import {ElMessage} from "element-plus";
import request from "@/utils/request.js";
import {Plus} from "@element-plus/icons-vue";
//注意该request是自己定义的，不是axios

const handleAvatarSuccess= (res) =>{
  console.log(res)
  data.form.avatar = res.data
}

const emit=defineEmits(['updateUser'])

const formRef=ref()

const validatePass = (rule,value,callback) => {
  if (value === '') {
    callback(new Error('请再次确认新密码'))
  } else if (value !== data.form.newPassword) {
    callback(new Error("两次输入密码不一致!"))
  } else {
    callback()
  }
}

const data=reactive({
  user: JSON.parse(localStorage.getItem('monitor-user')),
  form:{},//空对象可以灵活的添加属性
  rules:{
    username:[{required:true,message:"请输入账号",trigger:"blur"}],
    name:[{required:true,message:"请输入名称",trigger:"blur"}],
    password:[{required:true,message:"请输入原密码",trigger:"blur"}],
    newPassword:[{required:true,message:"请输入新密码",trigger:"blur"}],
    confirmPassword:[{validator:validatePass,trigger:"blur"}],
  }
})


if(data.user.role==="USER"){
  request.get('/user/selectById/' + data.user.id).then(res=>{
    data.form=res.data
  })
}else{
  data.form=data.user
}

const updateUser=()=>{
  if(data.user.role==="USER"){
    request.put('/user/update',data.form).then(res=>{
      updatePassword()
      if(res.code==='200'){
        ElMessage.success('更新成功！')
        //更新缓存数据
        localStorage.setItem('monitor-user',JSON.stringify(data.form))
        //触发父级从缓存里面获取最新数据
        emit('updateUser')
      }else{
        ElMessage.error(res.msg)
      }
    })
  } else{
    request.put('/admin/update',data.form).then(res=>{
      updatePassword()
      if(res.code==='200'){
        ElMessage.success('更新成功！')
        //更新缓存数据
        localStorage.setItem('monitor-user',JSON.stringify(data.form))
        //触发父级从缓存里面获取最新数据
        emit('updateUser')
      }else{
        ElMessage.error(res.msg)
      }
    })
  }
}

const updatePassword=()=>{
  data.form.id = data.user.id
  data.form.role = data.user.role
  formRef.value.validate((valid)=>{
    if(valid){
      request.put('/updatePassword',data.form).then(res=>{
        if(res.code==='200'){
          ElMessage.success('修改密码成功')
          localStorage.removeItem('monitor-user')//从浏览器的本地存储中LocalStorage删除指定键名monitor-user的数据。
          setTimeout(()=>{
            location.href='/login'
          },1000)
        }else{
          ElMessage.error(res.msg)
        }
      })
    }
  })}
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
</style>