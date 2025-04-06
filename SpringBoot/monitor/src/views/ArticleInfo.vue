<template>
  <div>
    <div class="card" style="margin-bottom:8px">
      <el-input clearable prefix-icon="Search" style="width:240px;margin-right:10px;" v-model="data.title" placeholder="请输入标题查询" ></el-input>
      <el-button type="primary" @click="load">查 询</el-button>
      <el-button type="warning" @click="reset">重 置</el-button>
    </div>
    <div v-if="data.user.role ==='USER'" class="card" style="margin-top:8px;">
      <el-button  type="primary" @click="handleAdd">新 增</el-button>
      <el-button type="warning" @click="delBatch">批量删除</el-button>
      <!--      <el-button type="info">导 入</el-button>-->
      <!--      <el-button type="success">导 出</el-button>-->
    </div>

    <div class="card" style="margin-top:8px;margin-bottom:20px" >
      <el-table :data="data.dataList" stripe @selection-change="handleSelectionChange" >
        <el-table-column type="selection" width="55"/>
        <el-table-column label="标题" prop="title"  show-overflow-tooltip />
        <el-table-column v-if = "data.user.role ==='ADMIN'" label="发布者Id" prop="userId"/>

        <el-table-column v-if = "data.user.role ==='ADMIN'" label="角色" prop="role"/>

        <el-table-column label="封面">
          <template #default="scope">
            <el-image v-if="scope.row.img" :src="scope.row.img" :preview-src-list=[scope.row.img] preview-teleported
                      style="width:50px;height:50px;border-radius:50%;" />
          </template>
        </el-table-column>
<!--    <el-table-column label="简介" prop="description" show-overflow-tooltip />-->

        <el-table-column label="内容">
          <template #default="scope">
            <el-button type="primary" @click="view(scope.row.content)">查看内容</el-button>
          </template>
        </el-table-column>

        <el-table-column label="发布时间" prop="time" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button @click="handleUpdate(scope.row)" type="success" icon="Edit" circle></el-button>
            <el-button @click="del(scope.row.id)" type="danger" icon="Delete" circle></el-button>
          </template>
        </el-table-column>
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

    <el-dialog title="文章信息" v-model="data.formVisible"  width="50%" destroy-on-close>
      <el-form ref="formRef" :model="data.form" label-width="80px" style="padding-right:50px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="data.form.title" autocomplete="off" placeholder="请输入标题" />
        </el-form-item>

        <el-form-item label="封面" >
          <el-upload
              action="http://localhost:9090/files/upload"
              list-type="picture"
              :on-success="handleImgSuccess"
          >
            <el-button type="primary">上传封面</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="简介" prop="description">
          <el-input type="textarea" :rows="3" v-model="data.form.description" autocomplete="off" placeholder="请输入简介" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <div style="border: 1px solid #ccc; width: 100%">
            <Toolbar
                style="border-bottom: 1px solid #ccc"
                :editor="editorRef"
                :mode="mode"
            />
            <Editor
                style="height: 500px; overflow-y: hidden;"
                v-model="data.form.content"
                :mode="mode"
                :defaultConfig="editorConfig"
                @onCreated="handleCreated"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.formVisible = false">取消</el-button>
          <el-button  @click="save" type="primary">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="内容" v-model="data.viewVisible" width="50%" :close-on-click-modal="false" destroy-on-close>
      <div class="editor-content-view" style="padding: 20px" v-html="data.content"></div>
      <template #footer>
    <span class="dialog-footer">
      <el-button @click="data.viewVisible = false">关 闭</el-button>
    </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive} from 'vue'
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";

import '@wangeditor/editor/dist/css/style.css' // 引入 css
import {onBeforeUnmount,shallowRef} from "vue";
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

import '@/assets/view.css'

const data=reactive({
  user: JSON.parse(localStorage.getItem('monitor-user')),
  title: null,
  dataList: [],
  total: 0,
  pageNum: 1,
  pageSize: 10,
  formVisible: false,
  form: {},
  ids: [],
  viewVisible: false,
  content: null

})

const view = (content) => {
  data.content = content
  data.viewVisible = true
}

/* wangEditor5 初始化开始 */
const baseUrl = 'http://localhost:9090'
const editorRef = shallowRef()  // 编辑器实例，必须用 shallowRef
const mode = 'default'
const editorConfig = {MENU_CONF: {}}
// 图片上传配置
editorConfig.MENU_CONF['uploadImage'] = {
  server: baseUrl + '/files/wang/upload',  // 服务端图片上传接口
  fieldName: 'file'  // 服务端图片上传接口参数
}
// 组件销毁时，也及时销毁编辑器，否则可能会造成内存泄漏
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})
// 记录 editor 实例，重要！
const handleCreated = (editor) => {
  editorRef.value = editor
}
/* wangEditor5 初始化结束 */


const handleImgSuccess = (res) => {

  data.form.img = res.data
}

// 发起请求方法
const load = () => {
  request.get('/article/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      title: data.title,
      role:data.user.role
    }
  }).then(res => {
    console.log(res.data)
    data.dataList = res.data.list
    data.total = res.data.total
  })
}
load()

// 重置方法
const reset = () => {

  data.title = null;
  load()
}

// 新增方法
const handleAdd = () => {
  data.formVisible = true
  data.form = {}
}
// 保存方法
const save = () => {
  data.form.id ? update() : add()
}
const add = () => {//新增对象里面没有id
  request.post('/article/add', data.form).then(res => {
    console.log(res)
    if (res.code === "200") {
      ElMessage.success("操作成功")
      load() //新增完成重新加载最新数据
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {//更新对象里面有id
  request.put('/article/update', data.form).then(res => {
    if (res.code === "200") {
      ElMessage.success("操作成功")
      load() //更新完成重新加载最新数据
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}


// 更新方法
const handleUpdate = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

const del = (id) => {
  ElMessageBox.confirm('确认删除该文章吗？', '提示', {type: 'warning'}).then(() => {
    request.delete('/article/deleteById/' + id).then( res => {
      if (res.code === "200") {
        ElMessage.success("操作成功")
        load() //删除后完成重新加载最新数据
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch()
}


// 批量删除方法
const handleSelectionChange = (rows) => {//返回所有选中的行对象数组
  data.ids = rows.map(row => row.id)
  console.log(data.ids)
}
const delBatch = () => {
  if (data.ids.length === 0) {
    ElMessage.error("请先选择要删除的文章")
    return
  }
  ElMessageBox.confirm('确认删除该文章吗？', '提示', {type: 'warning'}).then(() => {
    request.delete('/article/deleteBatch/', {data: data.ids}).then(res => {
      if (res.code === "200") {
        ElMessage.success("操作成功")
        load() //删除后完成重新加载最新数据
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch()
}
</script>
