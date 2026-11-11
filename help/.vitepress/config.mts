import { defineConfig } from 'vitepress'

export default defineConfig({
  lang: 'zh-CN',
  title: 'MoneyNote 帮助文档',
  description: 'MoneyNote 记账工具的帮助文档：核心概念、记账体系搭建指南与实际记账案例',
  cleanUrls: true,
  themeConfig: {
    nav: [
      { text: '简介', link: '/intro' },
      { text: '指南', link: '/build' },
      { text: '核心概念', link: '/conceptions/user' },
      { text: '记账案例', link: '/cases/reimbursement' },
      { text: '下载客户端', link: '/app' },
    ],
    sidebar: [
      { text: '简介', link: '/intro' },
      {
        text: '核心概念',
        items: [
          { text: '用户', link: '/conceptions/user' },
          { text: '账本', link: '/conceptions/book' },
          { text: '账户', link: '/conceptions/account' },
          { text: '记账要素', link: '/conceptions/elements' },
        ],
      },
      { text: '搭建完善的记账体系', link: '/build' },
      {
        text: '记账案例',
        items: [
          { text: '借还款/报销', link: '/cases/reimbursement' },
          { text: '贷款还贷', link: '/cases/loan' },
          { text: '房租分摊', link: '/cases/allocation' },
          { text: '充值卡', link: '/cases/charge_card' },
          { text: '其他案例', link: '/cases/other_cases' },
        ],
      },
      {
        text: '客户端',
        items: [{ text: '下载客户端', link: '/app' }],
      },
    ],
    search: {
      provider: 'local',
    },
    outline: {
      label: '本页目录',
    },
    docFooter: {
      prev: '上一页',
      next: '下一页',
    },
    returnToTopLabel: '回到顶部',
    sidebarMenuLabel: '菜单',
    darkModeSwitchLabel: '主题',
    lightModeSwitchTitle: '切换到浅色模式',
    darkModeSwitchTitle: '切换到深色模式',
  },
})
