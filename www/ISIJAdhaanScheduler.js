var exec = require('cordova/exec');


exports.startService = function(enable_sound, success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "startService", [enable_sound]);
};

exports.stopService = function(success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "stopService", []);
};

exports.getServiceStatus = function(success, error) {
    exec(function(active) {success(active === 'true')}, error, "ISIJAdhaanScheduler", "getServiceStatus", []);
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
    exec(function(active) {success(active === 'true')}, error, "ISIJAdhaanScheduler", "getAdhaanStatus", []);
};

exports.skipUpcomingAdhaan = function(success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "skipUpcomingAdhaan", []);
};

exports.disableAdhaanSound = function(success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "disableAdhaanSound", []);
};

exports.enableAdhaanSound = function(success, error) {
    exec(success, error, "ISIJAdhaanScheduler", "enableAdhaanSound", []);
};

exports.getUpcomingAdhaanTime = function(success, error) {
    exec(function(timetampStr) {
        success(parseInt(timetampStr));
    }, error, "ISIJAdhaanScheduler", "getUpcomingAdhaanTime", []);
};
