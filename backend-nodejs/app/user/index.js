'use strict'

/*
# Dependence services: mongo
*/

var express = require('express');

var _user = require('./user.js');
var _auth = require('./auth.js');

module.exports = function() {
    var app = express();
    var router = express.Router();

    // Do login here
    app.post("/login", _auth.login);

    // Validate token key for all user api in middleware
    router.use(_auth.validateToken);

    // Get all users
    router.get('/', _user.get);
    router.post('/', _user.create);
    router.put('/:id', _user.update);
    router.delete('/:id', _user.delete);

    return {
        login: app,
        api: router
    };
}
