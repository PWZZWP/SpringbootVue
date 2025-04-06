<template>
  <div>
    <div >
      <h1>实时流量监控和CART预测</h1>
      </div>
    <div>
      <el-input v-model="data.ip" placeholder="请输入网卡的IP地址" clearable style="width:400px;padding-right:10px;margin:0"/>
      <el-button @click="startCapture" :disabled="data.isCapturing" >开始捕获</el-button>
      <el-button @click="stopCapture">结束捕获</el-button>
      <el-button @click="saveTrafficPcapng" :disabled="!data.hasCaptured">保存流量包到本地</el-button>
<!--      <el-button @click="">格式转换</el-button>-->
<!--      <el-button @click="">上传文件到服务器</el-button>-->
<!--      <el-button @click="getTrafficData">获取流量数据</el-button>-->
    </div>

  <div style="width:100%;display: flex;margin:20px 0">
    <div id="main" style="height:400px;width:50%;margin-left: 0">
    </div>
    <div style="width:400px">
      <el-form>
        <el-form-item label="httpOpen">
          <el-select v-model="data.httpOpen" placeholder="选择Yes/No">
            <el-option label="Yes" value="Yes"></el-option>
            <el-option label="No" value="No"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="tcpWindowSize">
          <el-input v-model.number="data.tcpWindowSize" type="number" placeholder="请输入数值"></el-input>
        </el-form-item>
        <el-form-item label="zeroWindow">
          <el-input v-model.number="data.zeroWindow" type="number" placeholder="请输入数值"></el-input>
        </el-form-item>
        <el-form-item label="duration">
          <el-input v-model.number="data.duration" type="number" placeholder="请输入数值"></el-input>
        </el-form-item>
        <el-button @click="predict">调用预测接口</el-button>
      </el-form>
    </div>
    <div>
      <el-card v-if="data.predictionResult" title="预测结果">
        <p>当前输入的特征值预测结果为：{{ data.predictionResult }}</p>
      </el-card>
    </div>


  </div>

  </div>
</template>
<script setup>
import {reactive, onMounted, ref, onUnmounted} from 'vue'
import request from '../utils/request'
import * as echarts from 'echarts';
import {ElMessage} from "element-plus";
const data=reactive({
  ip:'',
  dataList:[],
  isCapturing:false,
  // 新增标志，判断是否有捕获数据
  hasCaptured: false,
  httpOpen: 'Yes', // 默认值
  tcpWindowSize: 0,
  zeroWindow: 0,
  duration: 0,
  predictionResult: null // 存储预测结果
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
    grid:{left:'3%'},
    //初始化X轴数据为空
    xAxis: {
      type: 'category',
      data: []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        data: data.dataList,
        type: 'line',
        smooth: true
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
})

onUnmounted(() => {
  // 组件卸载时清除定时器，防止内存泄漏
  if (intervalId) {
    clearInterval(intervalId);
  }
})

const predict = () => {
  request.get('/pmml/predict', {
    params: {
      httpOpen: data.httpOpen,
      tcpWindowSize: data.tcpWindowSize,
      zeroWindow: data.zeroWindow,
      duration: data.duration
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
}
</script>