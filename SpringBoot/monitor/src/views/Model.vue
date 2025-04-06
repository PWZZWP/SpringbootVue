<template>
  <!-- 外层容器，使用 flex 布局，宽度占满父元素，上下外边距 20px -->
  <div class="container">

    <!-- 左侧 div，flex 为 1 表示占据剩余空间的一部分，添加背景色 -->
    <div class="left">
    <div style="margin-left:10px;display:flex">
      <el-input v-model="data.ip" placeholder="请输入网卡的IP地址" clearable style="width:400px;padding-right:10px;margin:0"/>
      <el-button class="New_button1" @click="startCapture" :disabled="data.isCapturing" type="info" >开始捕获</el-button>
      <el-button class="New_button2" @click="stopCapture" type="warning">结束捕获</el-button>
    </div>
      <div style="margin-left:10px;margin-top:10px;display:flex">
      <el-button class="New_button3" @click="saveTrafficPcapng" :disabled="!data.hasCaptured">保存流量包到本地</el-button>
        <el-upload
            ref="upload"
            action="http://localhost:9090/traffic/uploadAndConvertPcapng"
            :auto-upload="false"
            :on-change="handleChange"
            :before-upload="beforeUpload1"
            :http-request="customRequest"
            accept=".pcapng"
        >
          <template #trigger>
            <el-button slot="trigger" type="primary" style="margin-right:10px;margin-left:10px">选取文件</el-button>
          </template>
          <el-button type="success" @click="submitUpload">上传到服务器</el-button>
          <el-button v-if="tableData.length > 0" type="primary" @click="downloadCSV" style="">下载 CSV 文件</el-button>
          <template #tip>
            <div class="el-upload__tip">只能上传 .pcapng 文件</div>
          </template>
        </el-upload>
      </div>
      <div id="main" style="width:100%;height:calc(100vh - 60px)"> </div>
    </div>
    <!-- 右侧容器，使用 flex 布局，flex 为 1 占据剩余空间一部分，子元素垂直排列 -->
    <div class="right">
      <div class="right-top">
        <div style="display:flex;">
        <el-button @click="data.showFormDialog = true" class="New_button1">预测参数设置</el-button>
        <el-button @click="handleExport"  class="New_button3">导入单条数据</el-button>
        <el-dialog
            title="预测参数表单"
            v-model="data.showFormDialog"
            width="80%"
            destroy-on-close
        >
            <el-form :inline="true">
              <el-form-item label="SrcIp" style="width:300px">
                <el-input v-model="data.srcIp" placeholder="请输入源 IP">
                </el-input>
              </el-form-item>
              <el-form-item label="DstIp" style="width:300px">
                <el-input v-model="data.dstIp" placeholder="请输入目的 IP">
                </el-input>
              </el-form-item>
              <el-form-item label="SrcPort" style="width:300px">
                <el-input v-model.number="data.srcPort" type="number" placeholder="请输入源端口">
                </el-input>
              </el-form-item>
              <el-form-item label="DstPort" style="width:300px">
                <el-input v-model.number="data.dstPort" type="number" placeholder="请输入目的端口">
                </el-input>
              </el-form-item>
              <el-form-item label="HttpOpen" style="width:300px">
                <el-select v-model="data.httpOpen" placeholder="选择 Yes/No">
                  <el-option label="Yes" value="Yes"></el-option>
                  <el-option label="No" value="No"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="TcpWindowSize" style="width:300px">
                <el-input v-model.number="data.tcpWindowSize" type="number" placeholder="请输入数值">
                  <template #append>Bytes</template>
                </el-input>
              </el-form-item>
              <el-form-item label="ZeroWindow" style="width:300px">
                <el-input v-model.number="data.zeroWindow" type="number" placeholder="请输入数值">
                  <template #append>次/分钟</template>
                </el-input>
              </el-form-item>
              <el-form-item label="Duration" style="width:300px">
                <el-input v-model.number="data.duration" type="number" placeholder="请输入数值">
                  <template #append>秒</template>
                </el-input>
              </el-form-item>

                <el-form-item label="ResPacketSplit" style="width:300px">
                  <el-input v-model.number="data.resPacketSplit" type="number" placeholder="请输入数值">
                    <template #append>分片数</template>
                  </el-input>
                </el-form-item>
                <el-form-item>
                  <el-button class="New_button3" @click="predict">调用预测接口</el-button>
                </el-form-item>
            </el-form>
          <template #footer>
            <el-button @click="data.showFormDialog = false">关闭</el-button>
          </template>
        </el-dialog>
        </div>

      <div style="margin-top:20px;display:flex;">
        <el-upload
            style=";display:inline-block;margin-right:10px;"
            action="http://localhost:9090/pmml/fileUpload"
            :show-file-list="true"
            :on-success="handleImportSuccess"
            :before-upload="beforeUpload"
            :on-change="handleFileChange"
            name="fileName"
        >
          <el-button class="New_button1">CSV文件导入</el-button>
        </el-upload>
        <el-input v-model="data.fileName" placeholder="请输入CSV文件名" style="height: 30px;margin-right:10px;width:150px " clearable/>
        <el-button @click="predict2" class="New_button5" >调用文件预测接口</el-button>

      </div>
        <div style=";display:flex;">
          <el-button @click="data.showTop10Dialog = true" class="New_button3">查看前 10 条预测结果</el-button>
          <el-button @click="exportCSV" class="New_button2">导出预测结果CSV文件</el-button>
        </div>
        <div style="display: flex; justify-content: flex-end;">
          <div id="pieChart" style="width: 300px; height: 300px;margin-top: 10px"></div>
        </div>
      </div>

      <div class="right-bottom">
        <div>
          <el-dialog title="预测信息和结果" v-model="data.dialogVisible"  width="500" destroy-on-close>
            <el-form :model="data" label-width="80px" style="padding-right:50px">
              <p>{{ data.predictionResult }}</p>
            </el-form>
          </el-dialog>
       </div>
        <div>

          <el-dialog
              v-model="data.showTop10Dialog"
              title="前 10 条预测结果"
              width="900px"
          >
              <el-table :data="data.predictionResults.slice(0, 10)" stripe>
                <el-table-column prop="input.srcIp" label="源 IP"></el-table-column>
                <el-table-column prop="input.dstIp" label="目的 IP"></el-table-column>
                <el-table-column prop="input.srcPort" label="源端口"></el-table-column>
                <el-table-column prop="input.dstPort" label="目的端口"></el-table-column>
                <el-table-column prop="input.httpOpen" label="HttpOpen"></el-table-column>
                <el-table-column prop="input.tcpWindowSize" label="TcpWindowSize"></el-table-column>
                <el-table-column prop="input.zeroWindow" label="ZeroWindow"></el-table-column>
                <el-table-column prop="input.duration" label="Duration"></el-table-column>
                <el-table-column prop="input.resPacketSplit" label="resPacketSplit"></el-table-column>
                <el-table-column prop="prediction" label="预测结果"></el-table-column>
              </el-table>
          </el-dialog>
        </div>

      </div>

    </div>

  </div>
</template>
<script setup>
import {onMounted, onUnmounted, reactive, ref, watch} from "vue";
import request from '../utils/request'
import * as echarts from 'echarts';
import {ElMessage, ElMessageBox} from "element-plus";
import router from "@/router/index.js";

const data=reactive({
  showFormDialog: false, // 新增：显示预测参数表单的弹窗
  selectedFile: null, // 新增：存储选中的文件
  showTop10Dialog: false, // 新增：显示前10条预测结果的弹窗
  predictionResults:[],
  fileName:null,
  dialogVisible: false,
  ip:'',
  dataList:[],
  isCapturing:false,
  // 新增标志，判断是否有捕获数据
  hasCaptured: false,
  httpOpen: 'Yes', // 默认值
  tcpWindowSize: 0,
  zeroWindow: 0,
  duration: 0,
  predictionResult: null , // 存储预测结果
  resPacketSplit:null,
  srcIp:null,
  dstIp:null,
  srcPort:null,
  dstPort:null,
})


//用于存储Echarts实例
const myChart = ref(null);

//用于存储定时器ID
let intervalId = null;

// 记录开始捕获的时间
let startTime = null;
const startCapture=()=>{
  if(!data.ip){
    ElMessage.warning('请输入网卡的IP地址')
  }
  else{
    ElMessage.success('开始捕获流量数据')
    data.isCapturing = true
    data.hasCaptured = false; // 开始捕获时重置标志
    startTime = new Date();
    request.get('/traffic/startCapture',{
      params:{
        ip:data.ip
      }
    }).then(res=>{
      console.log(res);
      //开启定时器，每隔2秒调用一次getTrafficData方法
      intervalId = setInterval(()=>{
        getTrafficData();
      },2000);
    });
  }
}

const stopCapture=()=>{
  if(data.isCapturing) {
    request.get('/traffic/stopCapture').then(res => {
      data.hasCaptured = true;
      console.log(res)
    })
    //清除定时器
    if(intervalId){
      clearInterval(intervalId);
    }
    ElMessage.success('结束捕获流量数据')
    data.isCapturing = false
  }else{
    ElMessage.warning('请先开始捕获流量数据')
  }

}

//新增保存流量包方法
// 新增保存流量包方法
const saveTrafficPcapng = () => {
  if (data.hasCaptured) {
    const link = document.createElement('a');
    // 确保路径与后端接口一致

    link.href = 'http://localhost:9090/traffic/downloadPcapng';
    link.download = 'traffic.pcapng';
    link.click();
  } else {
    ElMessage.warning('请先进行流量捕获');
  }
}

const getTrafficData=()=>{
  request.get('/traffic/trafficData').then(res=>{
    data.dataList=res
    console.log(data.dataList)
    for(let i=0;i<data.dataList.length;i++){
      if(data.dataList[i]>200){
        ElMessage.error('流量数据异常,请停止捕获并交由CART模块处理')
      }
    }
    //更新Echarts图表
    updateChart();
  });
}


const initChart=()=>{
  let chartDom = document.getElementById('main');
  myChart.value = echarts.init(chartDom);
  let option;
  option = {
    title:{
      text:'流量数据折线图',
      left:'center',
      textStyle:{
        color:'#f441a5'
      }
    },
    grid:{left:'3%',bottom:'40%'},
    //初始化X轴数据为空
    xAxis: {
      type: 'category',
      data: [],
      axisLine:{
        lineStyle:{color:'black'}
    },
      axisLabel:{
        color:'red'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {color: 'black'}
      },
      axisLabel: {
        color:'red'
      },
      splitLine: {
        show: false
      }
    },

    series: [
      {
        data: data.dataList,
        type: 'line',
        smooth: true,
        lineStyle: {
          color: '#f441a5' // 这里可以将颜色修改为你想要的，比如'red'、'green'等
        }
      }
    ]
  };
  option && myChart.value.setOption(option);
}

//新增更新图表的函数
const updateChart=()=>{
  if(myChart.value){
    //动态生成X轴数据，以展示每隔时间间隔2秒
    const xAxisData = []
    for(let i=0;i<data.dataList.length;i++){
      //计算当前时间点，从开始时间加上i*2秒
      const currentTime = new Date(startTime.getTime()+i*2000);
      //格式化时间为HH:mm:ss格式
      const timeString = currentTime.toLocaleTimeString([],{hour:'2-digit',minute:'2-digit',second:'2-digit'});
      xAxisData.push(timeString)
    }
    const option={
      xAxis:{data:xAxisData},
      series:[{data:data.dataList}]
    }
    myChart.value.setOption(option);
  }
}

onMounted(()=>{
  initChart()
  initPieChart();
})

onUnmounted(() => {
  // 组件卸载时清除定时器，防止内存泄漏
  if (intervalId) {
    clearInterval(intervalId);
  }
})

const predict = () => {
  if(data.tcpWindowSize ==null || data.zeroWindow ==null ||data.duration ==null ||
      data.resPacketSplit ==null ||data.srcIp ==null ||data.dstIp ==null ||data.srcPort ==null ||data.dstPort ==null){
    ElMessage.warning('请先填写完整的预测参数')
  }
  else{
    request.get('/pmml/predict', {
      params: {
        httpOpen: data.httpOpen,
        tcpWindowSize: data.tcpWindowSize,
        zeroWindow: data.zeroWindow,
        duration: data.duration,
        resPacketSplit:data.resPacketSplit,
        srcIp:data.srcIp,
        dstIp:data.dstIp,
        srcPort:data.srcPort,
        dstPort:data.dstPort,
      }
    }).then(res => {
      if(res.code ==="200"){
        data.predictionResult = res.data;
        ElMessage.success('预测成功');
      }else{
        ElMessage.error(res.msg);
      }
      console.log(res);
    })
    data.dialogVisible = true;
  }
}

const handleImportSuccess = (res) => {
  console.log(res);
  let message;
  if (res.code === '200') {
    data.fileName = res.data.fileName;
    message = `导入成功！\n文件名: ${res.data.fileName}  \n文件大小: ${res.data.fileSize} 字节`;
    ElMessageBox.alert(message, '导入结果', {
      confirmButtonText: '关闭',
      type: 'success',
    });
  } else {
    message = `导入失败！\n错误信息: ${res.msg}`;
    ElMessageBox.alert(message, '导入结果', {
      confirmButtonText: '关闭',
      type: 'error',
    });
  }
};

const  beforeUpload=(file)=> {
  const isCSV = file.name.endsWith('.csv');
  if (!isCSV) {
    ElMessage.error('只允许上传 CSV 文件');
    return false;
  }
  const maxSize = 10 * 1024 * 1024;
  if (file.size > maxSize) {
    ElMessage.error('文件大小超过限制（最大 10MB）');
    return false;
  }
  return true;
}


// 修改：新增文件选择事件处理函数
const handleFileChange = (file) => {
  if (!file.raw.type.endsWith('text/csv')) {
    ElMessage.warning('只允许上传 csv 文件');
    return;
  }
  data.selectedFile = file.raw; // 存储选中的文件
  data.fileName = file.name; // 更新文件名显示
};




// 修改：新增文件预测接口调用逻辑
const predict2 = () => {
  if (!data.selectedFile) {
    ElMessage.warning('请先选择一个 CSV 文件');
    return;
  }

  const formData = new FormData();
  formData.append('filePath', data.selectedFile);

  request
      .post('/pmml/predict2', formData, {
        headers: {
          // 手动设置请求头
          'Content-Type': 'multipart/form-data'
        }
      }).then(res => {
        if (res.code === '200') {
          data.predictionResults = res.data;
          ElMessage.success('预测成功');
        } else {
          ElMessage.error(`预测失败：${res.msg}`);
        }
      })
      .catch(error => {
        console.error('请求出错：', error);
        ElMessage.error('请求失败，请稍后再试');
      });
};

// 新增饼图实例引用
const pieChart = ref(null);

// 新增计算正常和异常标签数量的函数
const calculateLabels = () => {
  let normalCount = 0;
  let abnormalCount = 0;
  data.predictionResults.forEach(item => {
    if (item.prediction === 'NORMAL') {
      normalCount++;
    } else {
      abnormalCount++;
    }
  });
  return [
    { value: normalCount, name: '正常' },
    { value: abnormalCount, name: '异常' }
  ];
};

// 新增初始化饼图的函数
const initPieChart = () => {
  let chartDom = document.getElementById('pieChart');
  pieChart.value = echarts.init(chartDom);
  const option = {
    title: {
      text: '正常与异常标签数量分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    series: [
      {
        name: '标签数量',
        type: 'pie',
        radius: '50%',
        data: calculateLabels()
      }
    ]
  };
  option && pieChart.value.setOption(option);
};

// 当 predictionResults 更新时，更新饼图
watch(() => data.predictionResults, () => {
  if (pieChart.value) {
    const option = {
      series: [
        {
          data: calculateLabels()
        }
      ]
    };
    pieChart.value.setOption(option);
  }
});

// 导出 CSV 文件的方法
const exportCSV = () => {
  if (data.predictionResults.length === 0) {
    ElMessage.warning('没有预测结果可供导出');
    return;
  }
  // 定义 CSV 表头
  const headers = [
    'SrcIp', 'DstIp', 'SrcPort', 'DstPort', 'HttpOpen', 'TcpWindowSize', 'ZeroWindow', 'Duration', 'ResPacketSplit', 'Prediction'
  ];
  // 生成 CSV 内容
  let csvContent = headers.join(',') + '\n';
  data.predictionResults.forEach(item => {
    const row = [
      item.input.srcIp,
      item.input.dstIp,
      item.input.srcPort,
      item.input.dstPort,
      item.input.httpOpen,
      item.input.tcpWindowSize,
      item.input.zeroWindow,
      item.input.duration,
      item.input.resPacketSplit,
      item.prediction
    ];
    csvContent += row.join(',') + '\n';
  });
  // 创建 Blob 对象
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  // 创建下载链接
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);
  link.setAttribute('href', url);
  link.setAttribute('download', 'prediction_results.csv');
  link.style.visibility = 'hidden';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

const handleExport = () => {
  router.push('/manager/data');
}

const upload = ref(null);

const tableData = ref([]);
let csvContent = '';
let hasParsedData = ref(false);

const handleChange = (file, fileList) => {
  if (fileList.length > 1) {
    fileList.shift();
  }
};

const beforeUpload1 = (file) => {
  return file.type === 'application/vnd.tcpdump.pcap' || file.name.endsWith('.pcapng');
};

const customRequest = async ({ file }) => {
  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await request.post('/traffic/uploadAndConvertPcapng', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    if (response.code === "200") {
      const base64Data = response.data;
      const decodedData = atob(base64Data); // 解码 Base64 字符串
      tableData.value = parseCSV(decodedData);
      csvContent = decodedData; // 存储 CSV 内容以便下载
      hasParsedData.value = true;
      ElMessage.success('文件上传成功并解析为 CSV 数据');
    } else {
      ElMessage.error('文件上传失败');
    }
  } catch (error) {
    console.error(error);
    ElMessage.error('文件上传失败');
  }
};

const submitUpload = () => {
  upload.value.submit();
};

const parseCSV = (csvString) => {
  if (!csvString) return [];
  const lines = csvString.split('\n').filter(line => line.trim() !== '');
  const headers = lines[0].split(',');
  return lines.slice(1).map(line => {
    const values = line.split(',');
    return headers.reduce((obj, header, index) => ({
      ...obj,
      [header]: values[index],
    }), {});
  });
};


const downloadCSV = () => {
  if (!hasParsedData.value) {
    ElMessage.warning('没有可下载的数据');
    return;
  }
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement("a");
  if (link.download !== undefined) {
    const url = URL.createObjectURL(blob);
    link.setAttribute("href", url);
    link.setAttribute("download", "converted_data.csv");
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
};
</script>
<style scoped>
body {
  margin: 0;
  padding: 0;
}

.container {
  display: flex;
  height:calc(100vh - 60px); /* 设置容器高度为视口高度 */
  background-image: url('@/assets/img.jpg');
  background-size: cover;
}

.left,
.right {
  flex: 1; /* 左右两侧各占一半空间 */
  padding: 20px;
  box-sizing: border-box;
}

.left {
  //background-image:url('@/assets/img1-2.jpg');
  //background-size: cover;
}

.right {
  display: flex;
  flex-direction: column;
}
.right-top,
.right-bottom {
  flex: 1; /* 上下两部分各占一半空间 */
  box-sizing: border-box;
}

::v-deep .el-form-item__label {
  color: #f441a5;
}

.el-upload__tip {
  font-weight: bold;
  color: #ff4d4f !important;
}
</style>