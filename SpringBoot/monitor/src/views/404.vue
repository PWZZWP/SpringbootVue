<template>
  <div style="text-align:center;">
  <img src="@/assets/4041.png" alt="404 Not Found">
    <div><el-button type="primary" @click="goHome" style
        ="width:200px;height:50px;font-size:20px">
      返回主页
    </el-button>
    </div>
  </div>
</template>
<script setup>
const goHome=()=>{
  location.href="/manager/home"
}

</script>
<style>

</style>