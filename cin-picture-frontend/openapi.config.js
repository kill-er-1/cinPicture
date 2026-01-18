import { generateService } from '@umijs/openapi'
/* 自动为请求编写代码 */
generateService({
  requestLibPath: "import request from '@/request'",
  schemaPath: 'http://localhost:8123/api/v2/api-docs',
  serversPath: './src',
})
