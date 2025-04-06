<template>
  <div>
    <div>
      <div class="card" style="margin-bottom:8px">
        <el-input
            clearable
            @clear="load"
            prefix-icon="Search"
            style="width:240px;margin-right:10px;"
            v-model="data.srcIp"
            placeholder="请输入ip查询"
        ></el-input>
        <el-button type="primary" color="#87CEFA" @click="load">查 询</el-button>
        <el-button type="warning" color="#FFFFE0" @click="reset">重 置</el-button>
      </div>
      <div class="card" style="margin-top:8px;">
        <el-button type="primary" color="#98F5FF" @click="handleAdd">新 增</el-button>
        <el-button type="warning" color="#97FFFF" @click="delBatch">批量删除</el-button>
        <el-upload
            style="margin-left:10px;display:inline-block;margin-right:10px;"
            action="http://localhost:9090/traffic/import"
            :show-file-list="false"
            :on-success="handleImportSuccess"
        >
          <el-button type="info" color="#9370DB">批量导入</el-button>
        </el-upload>
        <el-button type="success" color="#7FFF00" @click="exportData">批量导出</el-button>
      </div>
    </div>
    <div>
      <el-table
          height="250"
          :data="data.tableData"
          style="width: 100%"
          @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection"></el-table-column>
        <el-table-column prop="date" label="Date" />
        <el-table-column prop="userId" label="WhoPublished" v-if="data.user.role === 'ADMIN'" />
        <el-table-column prop="role" label="Role" v-if="data.user.role === 'ADMIN'" />
        <el-table-column prop="srcIp" label="Src_Ip" />
        <el-table-column prop="dstIp" label="Dst_Ip" />
        <el-table-column prop="packetSize" label="Packet_Size" />
        <el-table-column prop="requestFrequency" label="Request_Frequency" />
        <el-table-column prop="label" label="Label" />
        <el-table-column label="Operation">
          <template #default="scope">
            <el-button type="primary" icon="Edit" color="#7EC0EE" circle @click="handleEdit(scope.row)"></el-button>
            <el-button type="danger" icon="Delete" circle @click="handleDel(scope.row.id)"></el-button>
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
          layout="total, sizes, prev, pager, next, jumper"
          :total="data.total"
      />
    </div>
    <div style="margin-top:10px;">
      <div style="height:300px" id="pie"></div>
    </div>
    <div>
      <el-dialog v-model="data.FormVisible" title="新增流量数据" width="500" destroy-on-close>
        <el-form ref="formRef" :rules="data.rules" :model="data.form">
          <el-form-item label="Date" prop="date">
            <el-date-picker
                v-model="data.form.date"
                type="datetime"
                placeholder="Select date and time"
                value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          <el-form-item label="srcIp" prop="srcIp">
            <el-input v-model="data.form.srcIp" />
          </el-form-item>
          <el-form-item label="dstIP" prop="dstIp">
            <el-input v-model="data.form.dstIp" autocomplete="off" />
          </el-form-item>
          <el-form-item label="packetSize" prop="packetSize">
            <el-input v-model="data.form.packetSize" autocomplete="off" />
          </el-form-item>
          <el-form-item label="requestFrequency" prop="requestFrequency">
            <el-input v-model="data.form.requestFrequency" autocomplete="off" />
          </el-form-item>
          <el-form-item label="label" prop="label">
            <el-select v-model="data.form.label" placeholder="Please select a label">
              <el-option label="normal" value="normal" />
              <el-option label="abnormal" value="abnormal" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="data.FormVisible = false">Cancel</el-button>
            <el-button type="primary" @click="save">Save</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref, onMounted} from "vue";
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import * as echarts from "echarts";

const getCurrentUser = () => JSON.parse(localStorage.getItem("monitor-user"));

const handleImportSuccess = (res) => {
  if (res.code === "200") {
    ElMessage.success("导入成功！");
    load();
  } else {
    ElMessage.error(res.msg);
  }
};

const data = reactive({
  user: getCurrentUser(),
  pageNum: 1,
  pageSize: 5,
  total: 1,
  tableData: [],
  srcIp: null,
  form: {},
  FormVisible: false,
  rules: {
    date: [{required: true, message: "请选择日期", trigger: "blur"}],
    srcIp: [{required: true, message: "请输入ip", trigger: "blur"}],
    dstIp: [{required: true, message: "请输入dstIp", trigger: "blur"}],
    packetSize: [{required: true, message: "请输入packetSize", trigger: "blur"}],
    requestFrequency: [{required: true, message: "请输入requestFrequency", trigger: "blur"}],
    label: [{required: true, message: "请选择label", trigger: "blur"}],
  },
  rows: [],
  ids: [],
});

const load = () => {
  const params = {
    pageNum: data.pageNum,
    pageSize: data.pageSize,
    srcIp: data.srcIp,
  };

  if (data.user.role === "USER") {
    params.userId = data.user.id;
  }

  request.get("/traffic/selectPage", {params}).then((res) => {
    if (res.code === "200") {
      data.tableData = res.data.list;
      data.total = res.data.total;
      loadPie();
    } else {
      ElMessage.error(res.msg);
    }
  });
};

const loadPie = () => {
  const currentUser = getCurrentUser();
  const params = currentUser.role === "USER" ? {userId: currentUser.id} : {};

  request.get("/echarts/pie", {params}).then((res) => {
    if (res.code === "200") {
      const chartDom = document.getElementById("pie");
      const myChart = echarts.init(chartDom);

      const pieOptions = {
        title: {text: "正常异常流量统计", left: "center"},
        tooltip: {trigger: "item", formatter: "{a} <br/>{b} : {c} ({d}%)"},
        legend: {orient: "vertical", left: "left"},
        series: [{
          name: "数量占比",
          type: "pie",
          radius: "50%",
          center: ["50%", "60%"],
          data: res.data.map(item => ({name: item.label, value: item.count}))
        }]
      };

      myChart.setOption(pieOptions);
    } else {
      ElMessage.error("获取统计数据失败");
    }
  });
};

onMounted(() => {
  load();
});

const reset = () => {
  data.srcIp = null;
  load();
};

const handleAdd = () => {
  data.FormVisible = true;
  data.form = {};
};

const save = () => {
  const formRef = ref();
  formRef.value.validate((valid) => {
    if (valid) {
      const method = data.form.id ? request.put : request.post;
      method("/traffic/update", data.form).then((res) => {
        if (res.code === "200") {
          data.FormVisible = false;
          ElMessage.success(data.form.id ? "更新成功！" : "新增成功！");
          load();
        } else {
          ElMessage.error(res.msg);
        }
      });
    }
  });
};

const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row));
  data.FormVisible = true;
};

const handleDel = (id) => {
  ElMessageBox.confirm("确定删除该条数据吗？删除后不可恢复！", "删除确认", {type: "warning"})
      .then(() => {
        request.delete(`/traffic/delete/${id}`).then((res) => {
          if (res.code === "200") {
            ElMessage.success("删除成功！");
            load();
          } else {
            ElMessage.error(res.msg);
          }
        });
      });
};

const handleSelectionChange = (rows) => {
  data.rows = rows;
  data.ids = rows.map(v => v.id);
};

const delBatch = () => {
  if (data.rows.length === 0) {
    ElMessage.warning("请先选择要删除的数据！");
    return;
  }

  ElMessageBox.confirm("确定删除选中的数据吗？删除后不可恢复！", "删除确认", {type: "warning"})
      .then(() => {
        request.delete("/traffic/deleteBatch", {data: data.rows}).then((res) => {
          if (res.code === "200") {
            ElMessage.success("批量删除成功！");
            load();
          } else {
            ElMessage.error(res.msg);
          }
        });
      });
};

const exportData = () => {
  const idsStr = data.ids.join(",");
  const url = `http://localhost:9090/traffic/export?srcIp=${encodeURIComponent(data.srcIp || "")}&ids=${idsStr}`;
  window.open(url);
};
</script>

<style>
/* Add your custom styles here */
</style>