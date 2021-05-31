<template>
  <div>
    <h1>用户信息</h1>

    <NuxtLink to="/user">
      用户信息
    </NuxtLink>

    <NuxtLink to="/user/account">
      用户账户
    </NuxtLink>
    <!-- user目录下的页面的路由出口 -->
    <NuxtChild />

  </div>

</template>

<script>
  export default {
    data() {
      return {
        ip: null,
      }
    },
    created() {

      this.$axios.$get('http://icanhazip.com').then( response => {
        console.log("响应信息" + response)
        this.ip = response
      })
    },
  }
</script>

<style scoped>

</style>
