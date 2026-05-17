# 医药订货系统管理后台 (Admin Web)

基于 Vue 3 + Vite + Element Plus 的医药订货系统B端管理后台。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite 5
- **UI库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios
- **图表**: ECharts 5

## 功能模块

- [x] 用户登录/登出
- [x] 数据概览Dashboard（销售趋势、分类占比）
- [x] 商品管理（发布、编辑、上下架）
- [x] 订单管理（查看、发货）
- [x] 处方审核（查看、通过/拒绝）

## 项目结构

```
admin-web/
├── src/
│   ├── api/              # API接口
│   │   ├── auth.js
│   │   ├── product.js
│   │   ├── order.js
│   │   └── prescription.js
│   ├── layout/           # 布局组件
│   │   └── index.vue
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── stores/           # Pinia状态管理
│   │   └── user.js
│   ├── utils/            # 工具函数
│   │   └── request.js
│   ├── views/            # 页面组件
│   │   ├── Login.vue
│   │   ├── Dashboard.vue
│   │   ├── ProductList.vue
│   │   ├── OrderList.vue
│   │   └── PrescriptionAudit.vue
│   ├── App.vue
│   └── main.js
├── index.html
├── package.json
└── vite.config.js
```

## 快速开始

### 安装依赖
```bash
cd admin-web
npm install
```

### 启动开发服务器
```bash
npm run dev
```
访问 http://localhost:3000

### 构建生产版本
```bash
npm run build
```

## 配置说明

### API代理
在 `vite.config.js` 中配置后端API地址：
```js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

### 默认登录账号
- 手机号: 13800000000
- 密码: admin123

## 页面截图说明

### 登录页
- 手机号+密码登录
- 表单验证

### Dashboard
- 今日订单数、营收、待审核处方、库存预警
- 销售趋势折线图
- 商品分类饼图

### 商品管理
- 商品列表（分页、搜索、筛选）
- 发布/编辑商品对话框
- 下架操作

### 订单管理
- 订单列表（按状态筛选）
- 订单详情查看
- 发货操作（选择物流公司、输入单号）

### 处方审核
- 待审核处方列表
- 处方详情查看（含图片预览）
- 审核通过/拒绝操作
