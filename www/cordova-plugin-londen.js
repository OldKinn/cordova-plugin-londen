var exec = require('cordova/exec');

exports.hello = function(name, success, error) {
    exec(success, error, 'LondenValidator', 'hello', [name]);
};

exports.status = function(successCall, errorCall) {
    exec(successCall, errorCall, 'LondenValidator', 'status');
};

exports.initReader = function(successCall, errorCall) {
    exec(successCall, errorCall, 'LondenValidator', 'initReader');
};

exports.readCard = function(successCall, errorCall) {
    exec(successCall, errorCall, 'LondenValidator', 'readCard');
};