<template>
  <div>
    <div class="card" style="margin-bottom:8px">
      <el-input clearable prefix-icon="Search" style="width:240px;margin-right:10px;" v-model="data.name" placeholder="请输入名称查询" ></el-input>
      <el-button type="primary" color="#87CEFA" @click="load">查 询</el-button>
      <el-button type="warning" color="#FFFFE0" @click="reset">重 置</el-button>
    </div>
<!--    <div  class="card" style="margin-top:8px;">-->
<!--      <el-button type="primary" @click="handleAdd" >新 增</el-button>-->
<!--      <el-button type="warning" @click="delBatch">批量删除</el-button>-->
<!--&lt;!&ndash;      <el-button type="info">导 入</el-button>&ndash;&gt;-->
<!--&lt;!&ndash;      <el-button type="success">导 出</el-button>&ndash;&gt;-->
<!--    </div>-->
    <div class="card" style="margin-top:8px;margin-bottom:20px" >
      <el-table :data="data.dataList" stripe @selection-change="handleSelectionChange" >
<!--        <el-table-column type="selection" width="55"/>-->
        <el-table-column label="账号" prop="username"></el-table-column>
        <el-table-column label="头像">
          <template #default="scope">
            <img v-if="scope.row.avatar" :src="scope.row.avatar" style="width:50px;height:50px;border-radius:50%;" />
          </template>
        </el-table-column>
        <el-table-column label="名称" prop="name"></el-table-column>
<!--        <el-table-column label="操作" width="120">-->
<!--          <template #default="scope">-->
<!--            <el-button @click="handleUpdate(scope.row)" type="success" :icon="Edit" circle></el-button>-->
<!--            <el-button @click="del(scope.row.id)" type="danger" :icon="Delete" circle></el-button>-->
<!--          </template>-->
<!--        </el-table-column>-->
      </el-table>
    </div>
    <div>
    <el-pagination
        @size-change="load"
        @current-change="load"
        v-model:current-page="data.pageNum"
        v-model:page-size="data.pageSize"
        :page-sizes="[5,10,15,20]"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="data.total"
    />
    </div>

    <el-dialog title="管理员信息" v-model="data.formVisible"  width="500" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.form" label-width="80px" style="padding-right:50px">
        <el-form-item label="账号" prop="username">
          <el-input :disabled="data.form.id" v-model="data.form.username" autocomplete="off" placeholder="请输入账号" />
        </el-form-item>

        <el-form-item label="头像" >
          <el-upload
              action="http://localhost:9090/files/upload"
              list-type="picture"
              :on-success="handleAvatarSuccess"
          >
            <el-button type="primary">上传头像</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" placeholder="请输入名称" />
        </el-form-item>

      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.formVisible = false">取消</el-button>
          <el-button  @click="save" type="primary">保存</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
  import {reactive,ref} from 'vue'
  import {Delete, Edit, Search} from "@element-plus/icons-vue";
  import request from "@/utils/request.js";
  import {ElMessage, ElMessageBox} from "element-plus";
  const data=reactive({
  name:null,
  dataList:[],
  total:0,
  pageNum:1,
  pageSize:10,
  formVisible:false,
  form:{},
  ids:[],
  rules:{
   username:[{required:true,message:"请输入账号",trigger:"blur"}],
    name:[{required:true,message:"请输入名称",trigger:"blur"}],
  }
})



  const formRef = ref()

  const handleAvatarSuccess= (res) =>{
    console.log(res.data)
    data.form.avatar = res.data
  }

  // 发起请求方法
  const load = () =>{
    request.get('/admin/selectPage',{
      params:{
        pageNum:data.pageNum,
        pageSize:data.pageSize,
        name:data.name
      }
        }).then(res=>{
      data.dataList=res.data.list
      data.total=res.data.total
    })
  }
load()

  // 重置方法
  const reset = () =>{
    data.name= null;
    load()
  }

  // 新增方法
  const handleAdd = () =>{
    data.formVisible=true
    data.form={}
  }
  // 保存方法
  const save = () =>{
    formRef.value.validate((valid)=>{
      if (valid){
        data.form.id ? update() : add()
      }
    })
  }
    const add=()=>{//新增对象里面没有id
           request.post('/admin/add',data.form ).then(res=>{
             if (res.code === "200"){
               ElMessage.success("操作成功")
               load() //新增完成重新加载最新数据
               data.formVisible=false
             }else{
               ElMessage.error(res.msg)
             }
           })
         }

    const update=()=>{//更新对象里面有id
      request.put('/admin/update',data.form ).then(res=>{
        if (res.code === "200"){
          ElMessage.success("操作成功")
          load() //更新完成重新加载最新数据
          data.formVisible=false
        }else{
          ElMessage.error(res.msg)
        }
      })
    }


    // 更新方法
  const handleUpdate =(row)=>{
    data.form=JSON.parse(JSON.stringify(row))
    data.formVisible=true
  }

  const del =(id)=>{
    ElMessageBox.confirm('确认删除该用户吗？', '提示', {type:'warning'}).then(()=>{
      request.delete('/admin/deleteById/' + id).then(res=>{
        if (res.code === "200"){
          ElMessage.success("操作成功")
          load() //删除后完成重新加载最新数据
        }else{
          ElMessage.error(res.msg)
        }
      })
    }).catch()
  }


  // 批量删除方法
  const handleSelectionChange =(rows)=>{//返回所有选中的行对象数组
   data.ids =  rows.map(row => row.id)
    console.log(data.ids)
  }
  const delBatch =()=>{
    if (data.ids.length === 0){
      ElMessage.error("请先选择要删除的用户")
      return
    }
    ElMessageBox.confirm('确认删除该用户吗？', '提示', {type:'warning'}).then(()=>{
      request.delete('/admin/deleteBatch/',{data:data.ids}).then(res=>{
        if (res.code === "200"){
          ElMessage.success("操作成功")
          load() //删除后完成重新加载最新数据
        }else{
          ElMessage.error(res.msg)
        }
      })
    }).catch()
  }
</script>
