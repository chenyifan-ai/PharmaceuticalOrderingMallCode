import request from '@/utils/request'

// 管理员发票列表
export function getAdminInvoices(params) {
  return request({
    url: '/admin/invoice/list',
    method: 'get',
    params
  })
}

export function getAdminInvoiceDetail(id) {
  return request({
    url: `/admin/invoice/${id}`,
    method: 'get'
  })
}

export function issueInvoice(id) {
  return request({
    url: `/admin/invoice/issue/${id}`,
    method: 'post'
  })
}

export function sendInvoice(id, data) {
  return request({
    url: `/admin/invoice/send/${id}`,
    method: 'post',
    data
  })
}
