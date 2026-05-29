<template>
  <div>
    <h2>销售统计</h2>
    <router-link to="/" class="back-link">← 返回商城</router-link>
    <div ref="chartRef" class="chart-container"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'

const chartRef = ref<HTMLElement>()

onMounted(async () => {
  const res = await axios.get('http://localhost:8080/api/orders')
  const orders = res.data

  const dailyData: Record<string, number> = {}
  orders.forEach((order: any) => {
    const date = order.orderTime.split('T')[0]
    dailyData[date] = (dailyData[date] || 0) + order.totalPrice
  })

  const dates = Object.keys(dailyData).sort()
  const amounts = dates.map(d => dailyData[d])

  const chart = echarts.init(chartRef.value)
  chart.setOption({
    title: { text: '每日销售额统计', textStyle: { color: 'var(--text-primary)' } },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates, axisLabel: { color: 'var(--text-secondary)' } },
    yAxis: { type: 'value', name: '销售额（元）', nameTextStyle: { color: 'var(--text-secondary)' }, axisLabel: { color: 'var(--text-secondary)' } },
    series: [{ type: 'line', data: amounts, smooth: true, areaStyle: {}, lineStyle: { color: 'var(--primary-color)' } }],
    backgroundColor: 'transparent'
  })
})
</script>

<style scoped>
.back-link {
  display: inline-block;
  margin-bottom: 20px;
  color: var(--primary-color);
}

.chart-container {
  width: 100%;
  height: 400px;
  margin-top: 20px;
}
</style>