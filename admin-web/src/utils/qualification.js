/** 统一社会信用代码：18 位字母数字 */
export const CREDIT_CODE_PATTERN = /^[0-9A-HJ-NPQRTUWXY]{2}\d{6}[0-9A-HJ-NPQRTUWXY]{10}$/i

export const QUALIFICATION_STATUS = {
  0: { label: '待审核', tag: 'warning' },
  1: { label: '已通过', tag: 'success' },
  2: { label: '已驳回', tag: 'danger' }
}

export function qualificationStatusLabel(status) {
  return QUALIFICATION_STATUS[status]?.label ?? '未知'
}

export function qualificationStatusTag(status) {
  return QUALIFICATION_STATUS[status]?.tag ?? 'info'
}

/** 提交表单默认结构（字段与后端 EnterpriseQualification 一致） */
export function defaultQualificationForm() {
  return {
    companyName: '',
    creditCode: '',
    legalPerson: '',
    legalPersonIdCard: '',
    qualificationExpireDate: null,
    businessLicenseUrl: '',
    drugOperationPermitUrl: '',
    medicalDevicePermitUrl: '',
    gspCertificateUrl: ''
  }
}

export function mapQualificationToForm(row) {
  return {
    ...defaultQualificationForm(),
    companyName: row?.companyName || '',
    creditCode: row?.creditCode || '',
    legalPerson: row?.legalPerson || '',
    legalPersonIdCard: row?.legalPersonIdCard || '',
    qualificationExpireDate: row?.qualificationExpireDate || null,
    businessLicenseUrl: row?.businessLicenseUrl || '',
    drugOperationPermitUrl: row?.drugOperationPermitUrl || '',
    medicalDevicePermitUrl: row?.medicalDevicePermitUrl || '',
    gspCertificateUrl: row?.gspCertificateUrl || ''
  }
}

export const qualificationFormRules = {
  companyName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  creditCode: [
    { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
    { pattern: CREDIT_CODE_PATTERN, message: '统一社会信用代码格式不正确', trigger: 'blur' }
  ],
  legalPerson: [{ required: true, message: '请输入法定代表人', trigger: 'blur' }],
  businessLicenseUrl: [{ required: true, message: '请上传营业执照', trigger: 'change' }],
  qualificationExpireDate: [{ required: true, message: '请选择资质到期日期', trigger: 'change' }]
}
