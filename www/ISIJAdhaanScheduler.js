var exec = require('cordova/exec');


exports.startService = function(success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "startService", []);
};

exports.stopService = function(success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "stopService", []);
};

exports.getServiceStatus = function(success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "getServiceStatus", []);
};

exports.stopCurrentAdhaan = function(success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "stopCurrentAdhaan", []);
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

exports.skipUpcomingAdhaan = function(success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "skipUpcomingAdhaan", []);
};

exports.getUpcomingAdhaanTime = function(success, error) {
    exec(function(timetampStr) {
        success(parseInt(timetampStr));
    }, error, "ISIJAdhaanScheduler", "getUpcomingAdhaanTime", []);
};
