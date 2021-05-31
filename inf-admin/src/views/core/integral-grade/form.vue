<template xmlns:div="http://www.w3.org/1999/html">
  <div class="container">
    <!-- 输入表单 -->
    <el-form label-width="120px">
      <el-form-item label="借款额度">
        <el-input-number v-model="integralGrade.borrowAmount" :min="0" />
      </el-form-item>
      <el-form-item label="积分区间开始">
        <el-input-number v-model="integralGrade.integralStart" :min="0" />
      </el-form-item>
      <el-form-item label="积分区间结束">
        <el-input-number v-model="integralGrade.integralEnd" :min="0" />
      </el-form-item>
      <el-form-item>
        <el-button
          :disabled="saveBtnDisabled"
          type="primary"
          @click="saveOrUpdate()">
            保存
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import integralGradeApi from '@/api/core/integral-grade';

export default {
  name: "form",
  data() {
    return {
      saveBtnDisabled: false, // 保存按钮是否禁用，防止表单重复提交
      integralGrade: {}, // 初始化数据
      interId: null,
    }
  },
  created() {
    if (this.$route.params && this.$route.params.id) { // 如果有id值
      this.interId = this.$route.params.id;
      this.getById();
    }
  },
  methods: {
    getById() {
      integralGradeApi.getIntegralGradeById(this.interId).then(response => {
        this.integralGrade = response.data.record
        console.log("元素值：" + response.data.record.id);
      })
    },
    saveOrUpdate() {
      // 调用完saveOrUpdate方法后禁用保存按钮
      this.saveBtnDisabled = true
      // 没有id值是新增，有则是更新。
      !this.integralGrade.id ? this.saveData() : this.updateData();
    },

    // 新增数据
    saveData() {
      // debugger
      integralGradeApi.addIntegralGrade(this.integralGrade).then(response => {
        this.$message({
          type: 'success',
          message: response.message
        })
        this.$router.push('/core/integral-grade/list')
      })
    },
    // 修改数据
    updateData() {
      integralGradeApi.updateIntegralGrade(this.integralGrade).then(response => {
        this.$message({
          type: 'success',
          message: response.message,
        })
        this.$router.push('/core/integral-grade/list')
      })
    }
  }
}
</script>

<style scoped>

</style>
