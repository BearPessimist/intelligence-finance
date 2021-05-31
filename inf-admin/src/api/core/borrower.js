import request from'@/utils/request'

export default {
  /**
   *  分页后端接口
   * @param current 当前页
   * @param limit 每页限制多少条记录
   * @param keyword 查询关键字
   * @returns {request}
   */
  getPageList(current, limit, keyword) {
    return request({
      url: `/admin/core/borrower/list/${current}/${limit}`,
      method: 'get',
      params: {keyword}, // 查询关键字
    })
  },
  show(id) {
    return request({
      url: `/admin/core/borrower/show/${id}`,
      method: 'get'
    })
  },
  // 借款人申请审核
  approval(borrowerApproval) {
    return request({
      url: '/admin/core/borrower/approval',
      method: 'post',
      data: borrowerApproval
    })
  }
}
