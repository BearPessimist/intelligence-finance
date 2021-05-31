<template>
  <div class="container">
    <!-- 表格 -->
    <el-table :data="integralGradeList" border stripe>
      <el-table-column type="index" width="50" />
      <el-table-column prop="borrowAmount" label="借款额度" />
      <el-table-column prop="integralStart" label="积分区间开始" />
      <el-table-column prop="integralEnd" label="积分区间结束" />
      <el-table-column label="操作" width="200" align="center">
        <template slot-scope="scope">
          <router-link :to="'/core/integral-grade/edit/' + scope.row.id" >
            <el-button type="primary" size="mini" icon="el-icon-edit">
              修改
            </el-button>
          </router-link>

          <el-button
            type="danger"
            size="mini"
            icon="el-icon-delete"
            @click="removeById(scope.row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="padding-top: 10px"
      align="center"
      layout="prev, pager, next"
      :page-size="limit"
      :current-page="currentPage"
      :total="total"
      @current-change="getIntegrateByPage"
    >
    </el-pagination>
  </div>
</template>

<script>
import integralGradeApi from '@/api/core/integral-grade';


export default {
  name: "list",
  data() {
    return {
      integralGradeList: [],
      currentPage: 1, // 当前是第几页
      limit: 6, // 每页限制多少条
      total: 0, // 总页数
    }
  },
  created() {
    // this.queryAll();
    this.getIntegrateByPage();
  },
  methods: {

    getIntegrateByPage(page = 1) {
      this.currentPage = page;
      integralGradeApi.getIntegralGradeByPage(this.currentPage,this.limit).then(response => {
        this.total = response.data.total;
        this.integralGradeList = response.data.records;

      })
    },
    // queryAll() {
    //   integralGradeApi.getAllIntegralGrade().then(response => {
    //     this.integralGradeList = response.data.integralGrade;
    //   });
    // },
    // 根据id删除数据
    removeById(id) {
      this.$confirm('此操作将永久删除该记录, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
          return integralGradeApi.removeIntegralGradeById(id) // 返回这个对象
        }).then(response => { // 在第一个then回调函数后面调用接口。
        this.$message.success(response.message) // 调用后端的自定义返回信息。
        this.getIntegrateByPage(); // 调用查询所有方法刷新页面。

        }).catch(error => {
          if (error === `cancel`) {
            this.$message.warning('取消删除')
          }
        })
    }
  }
}
</script>


<style scoped>

</style>
