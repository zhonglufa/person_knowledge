import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store' // 引入 Vuex store

// Element UI 按需导入（性能优化）
import {
  Button,
  Input,
  Select,
  Option,
  Dialog,
  Form,
  FormItem,
  Table,
  TableColumn,
  Pagination,
  Tabs,
  TabPane,
  Tag,
  Card,
  Collapse,
  CollapseItem,
  Breadcrumb,
  BreadcrumbItem,
  Dropdown,
  DropdownMenu,
  DropdownItem,
  Menu,
  Submenu,
  MenuItem,
  MenuItemGroup,
  Message,
  MessageBox,
  Notification,
  Loading,
  Badge,
  Switch,
  Radio,
  RadioGroup,
  RadioButton,
  Checkbox,
  CheckboxGroup,
  Alert,
  Progress,
  Tree,
  DatePicker,
  TimePicker,
  Popover,
  Tooltip,
  Avatar,
  Divider,
  Empty,
  Skeleton,
  Result,
  Steps,
  Step,
  Carousel,
  CarouselItem,
  Cascader,
  ColorPicker,
  Transfer,
  Container,
  Header,
  Aside,
  Main,
  Footer,
  Upload,
  Rate,
  Slider,
  Link,
  Backtop
} from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import './styles/index.scss' // 引入样式系统入口文件
import './styles/design-system.css' // 引入设计系统
import '@/styles/utils/variables.css'
// 引入 Font Awesome 图标库
import '@fortawesome/fontawesome-free/css/all.css'

// 创建动态样式引入阿里巴巴普惠体字体
const link = document.createElement('link');
link.rel = 'stylesheet';
link.href = 'https://assets.mockplus.cn/ai/AlibabaPuHuiTi2.0.css';
document.head.appendChild(link);

Vue.config.productionTip = false //关闭生产模式下给出的提示

// 引入组件样式
import './styles/components/components.scss'

// 注册图片懒加载指令
import vLazy from '@/directives/lazyImage'
Vue.directive('lazy', vLazy)

// 按需注册Element UI组件
Vue.use(Button)
Vue.use(Input)
Vue.use(Select)
Vue.use(Option)
Vue.use(Dialog)
Vue.use(Form)
Vue.use(FormItem)
Vue.use(Table)
Vue.use(TableColumn)
Vue.use(Pagination)
Vue.use(Tabs)
Vue.use(TabPane)
Vue.use(Tag)
Vue.use(Card)
Vue.use(Collapse)
Vue.use(CollapseItem)
Vue.use(Breadcrumb)
Vue.use(BreadcrumbItem)
Vue.use(Dropdown)
Vue.use(DropdownMenu)
Vue.use(DropdownItem)
Vue.use(Menu)
Vue.use(Submenu)
Vue.use(MenuItem)
Vue.use(MenuItemGroup)
Vue.use(Badge)
Vue.use(Switch)
Vue.use(Radio)
Vue.use(RadioGroup)
Vue.use(RadioButton)
Vue.use(Checkbox)
Vue.use(CheckboxGroup)
Vue.use(Alert)
Vue.use(Progress)
Vue.use(Tree)
Vue.use(DatePicker)
Vue.use(TimePicker)
Vue.use(Popover)
Vue.use(Tooltip)
Vue.use(Avatar)
Vue.use(Divider)
Vue.use(Empty)
Vue.use(Skeleton)
Vue.use(Result)
Vue.use(Steps)
Vue.use(Step)
Vue.use(Carousel)
Vue.use(CarouselItem)
Vue.use(Cascader)
Vue.use(ColorPicker)
Vue.use(Transfer)
Vue.use(Container)
Vue.use(Header)
Vue.use(Aside)
Vue.use(Main)
Vue.use(Footer)
Vue.use(Upload)
Vue.use(Rate)
Vue.use(Slider)
Vue.use(Link)
Vue.use(Backtop)

// 注册v-loading指令
Vue.use(Loading, { directive: true })

// 注册全局方法
Vue.prototype.$message = Message
Vue.prototype.$msgbox = MessageBox
Vue.prototype.$alert = MessageBox.alert
Vue.prototype.$confirm = MessageBox.confirm
Vue.prototype.$prompt = MessageBox.prompt
Vue.prototype.$notify = Notification
Vue.prototype.$loading = Loading.service

new Vue({
    router,
    store, // 注入 store 到 Vue 实例
    render: h => h(App),
}).$mount('#app')