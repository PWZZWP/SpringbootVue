import { createRouter, createWebHistory } from 'vue-router'
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
      {path: '/', redirect:'/index'},
      {path: '/manager',meta:{title:'管理页'},component: ()=> import('../views/SystemPage.vue'), children:[
          {path: 'home', meta:{title:'家页'}, component: () => import('../views/Home.vue'),},
          {path: 'data',meta:{title:'用户流量数据页'}, component: () => import('../views/Traffic.vue'),},
          {path: 'user',meta:{title:'用户信息页'}, component: () => import('../views/UserInfo.vue'),},
          {path: 'admin',meta:{title:'管理员信息页'}, component: () => import('../views/AdminsInfo.vue'),},
          {path: 'person',meta:{title:'个人信息修改页'}, component: () => import('../views/Person.vue'),},
          {path: 'article',meta:{title:'留言页'}, component: () => import('../views/ArticleInfo.vue'),},
          {path: 'modelManage',meta:{title:'模型管理页'}, component: () => import('../views/ModelManage.vue'),},
          {path: 'model',meta:{title:'模型页'}, component: () => import('../views/Model.vue'),},

          ]},
      {path: '/login',meta:{title:'登录页'}, component: () => import('../views/Login.vue'),},
      {path: '/index',meta:{title:'首页'}, component: () => import('../views/Index.vue'),},
      {path: '/register',meta:{title:'注册页'}, component: () => import('../views/Register.vue'),},
      {path: '/404',meta:{title:'404页'}, component: () => import('../views/404.vue'),},
      {path: '/net',meta:{title:'网卡页'}, component: () => import('../views/RealTimeMonitor.vue'),},
      {path:'/:pathMatch(.*)',redirect:'/404'},


     ],
    })
router.beforeEach((to,from,next)=>{
    document.title=to.meta.title
    next()
})
export default router
