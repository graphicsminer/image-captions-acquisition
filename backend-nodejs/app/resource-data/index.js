'use strict'

/* 
# Dependence modules: user
# Dependence services: mongo
*/

var express = require('express');

var _resource = require('./resource-data.js');
var _auth = require('../user/auth.js');

module.exports = function() {
    var router = express.Router();

    // Validate token key for all user api in middleware
    router.use(_auth.validateToken);

    router.get('/config/:filename', _resource.getConfigFile);

    return {
        api: router
    };
}
