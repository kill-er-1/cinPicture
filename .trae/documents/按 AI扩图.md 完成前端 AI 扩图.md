## 目标
- 严格按 [AI扩图.md](file:///Users/cin/code/project/cin-picture-backend/AI扩图.md) 的流程：新增“AI 扩图”弹窗组件，并在创建图片页加入入口按钮与回填逻辑。

## 代码现状对齐（基于仓库）
- 前端工程位于 [cin-picture-frontend](file:///Users/cin/code/project/cin-picture-backend/cin-picture-frontend)。
- 已存在裁剪弹窗组件 [ImageCropper.vue](file:///Users/cin/code/project/cin-picture-backend/cin-picture-frontend/src/components/ImageCropper.vue)。
- 已存在扩图 API：`createPictureOutPaintingTaskUsingPost` / `getPictureOutPaintingTaskUsingGet` / `uploadPictureByUrlUsingPost`（在 [pictureController.ts](file:///Users/cin/code/project/cin-picture-backend/cin-picture-frontend/src/api/pictureController.ts)）。
- 已有一个实现接近教程的扩图弹窗 [ImageOutPaiting.vue](file:///Users/cin/code/project/cin-picture-backend/cin-picture-frontend/src/components/ImageOutPaiting.vue)，但未被页面引用，且缺少教程要求的轮询清理（`onUnmounted`）等细节。

## 实现步骤（按文档逐条落地）
### 1) 新增/规范 AI 扩图弹窗组件
- 新建标准命名组件 `ImageOutPainting.vue`（按文档：`a-modal` + `v-model:visible` + 标题“AI 扩图” + `:footer="false"`）。
- 弹窗内容按文档采用“一行两列栅格”：左原图、右结果图；下方 2 个按钮“生成图片 / 应用结果”。
- 变量与状态按文档：`resultImageUrl`、`taskId`、`uploadLoading`。
- 创建任务函数按文档：调用 `createPictureOutPaintingTaskUsingPost({ pictureId, parameters: { xScale: 2, yScale: 2 } })`，成功后保存 `taskId` 并启动轮询。
- 轮询逻辑按文档：`setInterval` 每 3 秒调用 `getPictureOutPaintingTaskUsingGet({ taskId })`；成功拿到 `outputImageUrl` 后停止轮询；失败同样停止。
- 清理逻辑按文档：抽 `clearPolling()`，在成功/失败/异常及 `onUnmounted` 时清理定时器，并将 `pollingTimer=null`、`taskId=null`。
- “应用结果”按文档：仅当 `resultImageUrl` 存在时展示按钮，点击调用 `uploadPictureByUrlUsingPost({ fileUrl: resultImageUrl, spaceId, id: picture.id })`，成功后 `props.onSuccess?.(newPicture)` 并关闭弹窗。
- UI 优化按文档：
  - 生成图片按钮 `:loading="!!taskId"`，并补充 `:disabled="!!taskId"` 防重复提交。
  - 上传按钮 `:loading="uploadLoading"`。
- 处理遗留：将现有 [ImageOutPaiting.vue](file:///Users/cin/code/project/cin-picture-backend/cin-picture-frontend/src/components/ImageOutPaiting.vue) 作为参考来源，最终只保留与文档一致的标准组件文件（避免拼写混乱）。

### 2) 创建图片页面引入弹窗（AddPicturePage）
- 在 [AddPicturePage.vue](file:///Users/cin/code/project/cin-picture-backend/cin-picture-frontend/src/pages/AddPicturePage.vue) 的“编辑图片”按钮右侧增加“AI 扩图”按钮（`type="primary" ghost` + `FullscreenOutlined` 图标），并用 `a-space` 包裹与文档一致。
- 增加 `imageOutPaintingRef`、`doImagePainting()` 打开弹窗、`onImageOutPaintingSuccess()` 回填 `picture`。
- 在模板中挂载 `cImageOutPainting ref ... :picture :spaceId :onSuccess /e`。

### 3) 页面模板修正（确保按文档改造后可正常渲染）
- 修复当前 [AddPicturePage.vue](file:///Users/cin/code/project/cin-picture-backend/cin-picture-frontend/src/pages/AddPicturePage.vue) 内 `a-tabs` 起始标签存在的多余字符导致的模板语法问题（不改业务，仅保证页面可用）。

## 交付物
- 新增组件：`src/components/ImageOutPainting.vue`（完全按文档结构与交互）。
- 页面改造：`src/pages/AddPicturePage.vue` 增加入口与回填。
- 清理/统一：去除或重命名拼写错误的旧组件文件，避免后续维护混乱。