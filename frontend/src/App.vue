<script setup lang="ts">
import {
  NConfigProvider,
  NDialogProvider,
  NMessageProvider,
  dateZhCN,
  zhCN,
  type GlobalThemeOverrides,
} from 'naive-ui'
import AppShell from '@/components/AppShell.vue'

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#16a34a',
    primaryColorHover: '#15803d',
    primaryColorPressed: '#166534',
    primaryColorSuppl: '#16a34a',
    infoColor: '#2563a8',
    successColor: '#16a34a',
    warningColor: '#d97706',
    errorColor: '#e0413c',
    borderRadius: '10px',
    borderRadiusSmall: '8px',
    fontFamily:
      "'Outfit Variable', 'MiSans', 'HarmonyOS Sans SC', 'PingFang SC', 'Microsoft YaHei', 'Segoe UI', sans-serif",
    textColorBase: '#1f2329',
    textColor1: '#1f2329',
    textColor2: '#4b5563',
    textColor3: '#9aa1ab',
    borderColor: '#e8eaee',
    bodyColor: '#f6f7f9',
    cardColor: '#ffffff',
    tableHeaderColor: '#f2f4f6',
  },
  Button: {
    borderRadiusMedium: '10px',
    borderRadiusSmall: '8px',
    fontWeight: '500',
  },
  Input: {
    borderRadius: '10px',
  },
  DataTable: {
    borderRadius: '14px',
  },
}
</script>

<template>
  <n-config-provider
    :locale="zhCN"
    :date-locale="dateZhCN"
    :theme-overrides="themeOverrides"
    :style="{ minHeight: '100dvh' }"
  >
    <n-message-provider placement="top">
      <n-dialog-provider>
        <app-shell />
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>
