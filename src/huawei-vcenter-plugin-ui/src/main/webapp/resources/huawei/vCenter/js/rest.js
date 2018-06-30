var deviceInfo = {
    getUnits: function(param, callback) {
        var serverDeviceDetailUrl = com_huawei_vcenterpluginui.webContextPath + "/rest/services/server/device/detail?ip="+param.ip+"&dn="+param.dn+"&s=" + Math.random();
        //通过API获取数据
        $.get(serverDeviceDetailUrl, function(response){
        	console.log(response);
            if (typeof callback === "function") {
            	callback(response);
            }
          },"json");
    }
}
var vCenterManage = {
    //获取eSight配置列表
    getList: function (param, callback) {
        var keyword = param.param.hostIP;
        var listData = [];
        var eSightListUrl = com_huawei_vcenterpluginui.webContextPath + "/rest/services/esight?ip="+keyword+"&pageNo="+param.param.pageNo+"&pageSize="+param.param.pageSize + "&s=" + Math.random();
        //通过API获取数据
        $.get(eSightListUrl, function(response){
            console.log(response);
            if (typeof callback === "function") {
                var dataCode = response.code;
                listData = response.data;
                var ret = {code: dataCode, msg: response.description, data:listData}
                callback(ret);
            }
        },"json");
    },
    get: function (callback) {
      var url = com_huawei_vcenterpluginui.webContextPath + "/rest/services/vcenter?s=" + Math.random();
      $.get(url, function(response){
        console.log(response);
        if (typeof callback === "function") {
          callback(response);
        }
      },"json");
    },
    getIps: function (callback) {
      var url = com_huawei_vcenterpluginui.webContextPath + "/rest/services/vcenter/ips?s=" + Math.random();
      $.get(url, function(response){
        console.log(response);
        if (typeof callback === "function") {
          callback(response);
        }
      },"json");
    },
    save: function (param,callback) {
      var url = com_huawei_vcenterpluginui.webContextPath + "/rest/services/vcenter?s=" + Math.random();
      $.ajax({
        type: 'POST',
        contentType : 'application/json;charset=utf-8',
        url: url,
        data: JSON.stringify(param),
        dataType: "json",
        success: function(response){
            if (typeof callback === "function") {
            var ret = {code: response.code,data: response.data,msg: response.description}
            callback(ret);
          }
        }
      });
    }
}