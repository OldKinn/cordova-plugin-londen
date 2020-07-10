# 龙盾指纹核验仪插件

插件安装

```
cordova plugin add cordova-plugin-londen
```

初始化设备
```
document.getElementById('initBtn').addEventListener('click', function() {
    cordova.plugins.londen.initReader(function(data) {
        navigator.notification.alert(data, nullCall, '初始化成功');
    }, function(err) {
        navigator.notification.alert(err, nullCall, '初始化异常');
    });
});
```

读取身份证
```
document.getElementById('readBtn').addEventListener('click', function() {
    cordova.plugins.londen.readCard(function(data) {
        navigator.notification.alert(data.name, nullCall, '读取成功');
        document.getElementById('avatar').src = data.avatar;
        document.getElementById('frontImg').src = data.frontImg;
    }, function(err) {
        navigator.notification.alert(err, nullCall, '读取异常');
    });
});
```