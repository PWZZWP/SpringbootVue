<template>
  <div class="model-management-container">
    <h1>模型管理</h1>
    <div class="operation-buttons">
      <el-upload
          action="http://localhost:9090/pmml/uploadModel"
          :show-file-list="false"
          :on-success="handleUploadSuccess"
          :before-upload="beforeUpload"
          name="modelFile"
      >
        <el-button type="primary" class="New_button1">导入模型</el-button>
      </el-upload>
      <el-button type="primary" class="New_button2" @click="fetchModels">查询模型</el-button>
    </div>

    <div style="margin-top: 50px;display:flex;justify-content: flex-end;width: 900px;padding: 10px;">
    <el-table :data="models" stripe >
      <el-table-column prop="name" label="模型名称"></el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag :type="scope.row.status === '启用' ? 'success' : 'info'">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button
              type="primary"
              @click="enableModel(scope.row.id)"
              :disabled="scope.row.status === '启用'"
          >
            启用
          </el-button>
          <el-button
              type="warning"
              @click="disableModel(scope.row.id)"
              :disabled="scope.row.status === '停用'"
          >
            禁用
          </el-button>
          <el-button
              type="danger"
              @click="confirmDelete(scope.row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import request from '../utils/request';
import { ElMessage, ElMessageBox } from 'element-plus';

const models = ref([]);
const data = reactive({
  user: JSON.parse(localStorage.getItem('monitor-user')) || {},
  token: JSON.parse(localStorage.getItem('monitor-user')).token
});

onMounted(() => {
  console.log(data.token);
  fetchModels();
});

const fetchModels = async () => {
  try {
    const response = await request.get('/pmml/getModels');
    models.value = response.data;
    ElMessage.success('模型查询成功');
  } catch (error) {
    ElMessage.error(`查询模型失败: ${error.message || '未知错误'}`);
  }
};

const beforeUpload = (file) => {
  const isPmml = file.name.endsWith('.pmml');
  if (!isPmml) {
    ElMessage.error('只允许上传 PMML 文件');
    return false;
  }
  return true;
};

const handleUploadSuccess = (res) => {
  console.log(res);
  if (res.code === '200') {
    ElMessage.success('模型导入成功');
    fetchModels();
  } else {
    ElMessage.error(`模型导入失败: ${res.msg || '未知错误'}`);
  }
};

const enableModel = async (modelId) => {
  try {
    await request.post(`/pmml/enableModel/${modelId}`);
    ElMessage.success('模型启用成功');
    await fetchModels();
  } catch (error) {
    ElMessage.error(`模型启用失败: ${error.message || '未知错误'}`);
  }
};

const disableModel = async (modelId) => {
  try {
    await request.post(`/pmml/disableModel/${modelId}`);
    ElMessage.success('模型禁用成功');
    await fetchModels();
  } catch (error) {
    ElMessage.error(`模型禁用失败: ${error.message || '未知错误'}`);
  }
};

const confirmDelete = async (modelId) => {
  try {
    await ElMessageBox.confirm('确定要删除该模型吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await deleteModel(modelId);
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`删除确认失败: ${error.message || '未知错误'}`);
    }
  }
};

const deleteModel = async (modelId) => {
  try {
    await request.delete(`/pmml/deleteModel/${modelId}`);
    ElMessage.success('模型删除成功');
    await fetchModels();
  } catch (error) {
    ElMessage.error(`模型删除失败: ${error.message || '未知错误'}`);
  }
};


</script>

<style scoped>
.model-management-container {
  padding: 10px;
  min-height: 80vh;
}

.operation-buttons {
  margin-bottom: 50px;
}

.New_button1 {
  margin-left: 10px;
  margin-bottom: 50px;
}

.New_button2 {
  margin-left: 10px;
}

.model-management-container {
  background-image: url("@/assets/wan(1).jpg"); /* 使用 CSS 变量设置背景图片 */
  background-size: cover; /* 让背景图片覆盖整个容器 */
  background-position: center; /* 背景图片居中显示 */
  min-height: 100vh; /* 设置最小高度为视口高度 */
  padding: 20px; /* 添加内边距 */
}
</style>    