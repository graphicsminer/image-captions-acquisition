'use strict'

/* 
# Dependence modules: user
# Dependence services: mongo
*/

var express = require('express');

var _image = require('./image-data.js');
var _auth = require('../user/auth.js');

module.exports = function() {
    var router = express.Router();

    // Validate token key for all user api in middleware
    router.use(_auth.validateToken);

    // Get all users
    router.get('/', _image.get);
    router.post('/', _image.create);
    router.put('/:hash', _image.update);
    router.delete('/:hash', _image.delete);
    router.post('/upload', _image.upload);

    return {
        api: router
    };
}
