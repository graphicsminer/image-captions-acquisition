'use strict'

/*
# Author: VuThaiDuy
# Provide common middlewares
*/

const _httpStatus = require('../utils/http-status-codes.js');

// log error
exports.errorHandler = function(err, req, res, next) {
    err && __logger.log('error', err);
    res.status(__httpStatus.INTERNAL_SERVER_ERROR).json({ error: err.toString() });
}

// Allow cross-domain access
exports.cors = function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS');
    res.header('Access-Control-Allow-Headers', 'X-Requested-With, X-HTTP-Method-Override, Content-Type, Accept');
    next();
}
