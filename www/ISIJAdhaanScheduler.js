var exec = require('cordova/exec');

exports.coolMethod = function(arg0, success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "coolMethod", [arg0]);
};

exports.start = function(success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "start", []);
};

exports.stopCurrent = function(success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "stopCurrent", []);
};