var exec = require('cordova/exec');


exports.start = function(success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "start", []);
};

exports.schedule = function(timestamp,success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "schedule", [timestamp]);
};

exports.stopCurrent = function(success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "stopCurrent", []);
};

exports.onAdhaanStarted = function(success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "onAdhaanStarted", []);
};

exports.onAdhaanStopped = function(success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "onAdhaanStopped", []);
};

exports.getAdhaanStatus = function(success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "getAdhaanStatus", []);
};
