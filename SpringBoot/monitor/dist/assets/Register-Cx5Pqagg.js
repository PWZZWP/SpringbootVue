/* empty css               *//* empty css             *//* empty css                  *//* empty css              */import{r as c}from"./request-Cq_wt7Fs.js";import{a3 as _,r as x,a1 as w,c as b,a as t,d as r,w as l,a0 as v,o as y,g as m,Y as p,B as V,_ as E,A as h}from"./index-B_OMzdnw.js";const k={class:"login-container"},P={class:"login-box"},z={style:{padding:"50px 30px","background-color":"#fff","margin-left":"180px","border-radius":"5px","box-shadow":"0 0 1000px rgba(0,0,0,50%)"}},B={style:{"margin-bottom":"20px"}},R={__name:"Register",setup(U){const o=x({form:{},rules:{username:[{required:!0,message:"请输入账号",trigger:"blur"}],password:[{required:!0,message:"请输入密码",trigger:"blur"}],confirmPassword:[{validator:(i,e,s)=>{e===""?s(new Error("请再次确认密码")):e!==o.form.password?s(new Error("两次输入密码不一致!")):s()},trigger:"blur"}]}}),d=w(),f=()=>{d.value.validate(i=>{i&&c.post("/register",o.form).then(e=>{e.code==="200"?(console.log(e.data),p.success("注册成功"),setTimeout(()=>{location.href="/login"},500)):p.error(e.msg)})})};return(i,e)=>{const s=V,n=E,u=h,g=v;return y(),b("div",k,[t("div",P,[t("div",z,[r(g,{ref_key:"formRef",ref:d,rules:o.rules,model:o.form,style:{width:"350px"}},{default:l(()=>[e[4]||(e[4]=t("div",{style:{"margin-bottom":"30px","font-size":"24px","text-align":"center",color:"deepskyblue","font-weight":"bold"}},"欢迎加入我们！",-1)),r(n,{prop:"username"},{default:l(()=>[r(s,{"prefix-icon":"User",size:"large",modelValue:o.form.username,"onUpdate:modelValue":e[0]||(e[0]=a=>o.form.username=a),placeholder:"请输入账号"},null,8,["modelValue"])]),_:1}),r(n,{prop:"password"},{default:l(()=>[r(s,{"show-password":"","prefix-icon":"Lock",size:"large",modelValue:o.form.password,"onUpdate:modelValue":e[1]||(e[1]=a=>o.form.password=a),placeholder:"请输入密码"},null,8,["modelValue"])]),_:1}),r(n,{prop:"confirmPassword"},{default:l(()=>[r(s,{"show-password":"","prefix-icon":"Lock",size:"large",modelValue:o.form.confirmPassword,"onUpdate:modelValue":e[2]||(e[2]=a=>o.form.confirmPassword=a),placeholder:"请确认密码"},null,8,["modelValue"])]),_:1}),t("div",B,[r(u,{style:{width:"100%"},size:"large",type:"primary",onClick:f},{default:l(()=>e[3]||(e[3]=[m("注册")])),_:1})]),e[5]||(e[5]=t("div",{style:{"text-align":"right"}},[m("已有账号！请"),t("a",{style:{color:"deepskyblue","text-decoration":"none"},href:"/login"},"登录")],-1))]),_:1},8,["rules","model"])])])])}}},A=_(R,[["__scopeId","data-v-aa4fa948"]]);export{A as default};
