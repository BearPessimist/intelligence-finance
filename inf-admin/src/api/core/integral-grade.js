// @ 符号在vue.config.js 中配置， 表示 'src' 路径的别名
import request from '@/utils/request'

export default {
  getAllIntegralGrade() {
    return request({
      url: '/admin/core/integralGrade/list',
      method: 'get'
    })
  },
  getIntegralGradeByPage(current,limit) {
    return request({
      url: `/admin/core/integralGrade/page/${current}/${limit}`,
      method: 'get'
    })
  },
  removeIntegralGradeById(id) {
    return request({
      url: `/admin/core/integralGrade/remove/${id}`,
      method: 'delete'
    })
  },
  addIntegralGrade(integralGrade) {
    return request({
      url: '/admin/core/integralGrade/add',
      method: 'post',
      data: integralGrade
    })
  },
  getIntegralGradeById(id) {
    return request({
      url: `/admin/core/integralGrade/get/${id}`,
      method: 'get',
    })
  },
  updateIntegralGrade(integralGrade) {
    return request({
      url: '/admin/core/integralGrade/modify',
      method: 'put',
      data: integralGrade
    })
  }
}
