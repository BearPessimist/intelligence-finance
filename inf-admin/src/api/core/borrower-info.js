import request from '@/utils/request'

export default {
  getList() {
    return request({
      url: `/admin/core/borrowInfo/list`,
      method: 'get'
    })
  },
  show(id) {
    return request({
      url: `/admin/core/borrowInfo/show/${id}`,
      method: 'get'
    })
  },
  // 审批借款信息
  approval(borrowInfoApproval) {
    return request({
      url: '/admin/core/borrowInfo/approval',
      method: 'put',
      data: borrowInfoApproval
    })
  }
}
