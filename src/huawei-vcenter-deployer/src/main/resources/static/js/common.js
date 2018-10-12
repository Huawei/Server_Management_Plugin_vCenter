/**
 * 改变当前语言
 * @param {string} lang (zhCN,en)
 */
function changelang(lang) {
    if (lang == 'zhCN') {
        ELEMENT.locale(ELEMENT.lang.zhCN);
        localStorage.setItem('lang', 'zhCN');
        this.lang = ELEMENT.lang.zhCN.el.templateManage;
    } else {
        ELEMENT.locale(ELEMENT.lang.en);
        this.lang = ELEMENT.lang.en.el.templateManage;
        localStorage.setItem('lang', 'en');
    }
}
/**
 * 国际化
 **/
function getIn18() {
    var lang = localStorage.getItem('lang');
    if (lang) {
        if (lang == 'zhCN') {
            ELEMENT.locale(ELEMENT.lang.zhCN);
            return i18n_zh_CN;
        } else {
            ELEMENT.locale(ELEMENT.lang.en);
            return i18n_en;
        }
    } else {
        ELEMENT.locale(ELEMENT.lang.en);
        return i18n_en;
    }
}
var errorCode_zh_CN = {
    'E001': '安装包版本号读取错误',
    'E002': 'URL必须以https开始',
    'E003': 'vCenter IP不能为空',
    'E004': 'vCenter端口号不能为空',
    'E005': '获取证书指纹出错',
    'E006': 'tomcat.keystore证书文件不存在',
    'E007': '未找到更新包程序，请放入zip包然后刷新页面',
}

var errorCode_en = {
    'E001': 'Can not get version from zip package',
    'E002': 'URL must start with https',
    'E003': 'vCenter IP can not be empty',
    'E004': 'vCenter port can not be empty',
    'E005': 'Can not get key file thumbprint',
    'E006': "tomcat.keystore doesn't exist",
    'E007': ' Please put zip file and refresh the page',
}
/**
 * 获取错误代码
 * @param  errorCode 
 * @returns  errorMsg
 */
function getErrorMsg(errorCode) {
    var lang = localStorage.getItem('lang');
    if (lang === 'zhCN') {
        if (errorCode_zh_CN[errorCode]) {
            return errorCode_zh_CN[errorCode];
        }
        return errorCode;

    } else {
        if (errorCode_en[errorCode]) {
            return errorCode_en[errorCode];
        }
        return errorCode;
    }
};