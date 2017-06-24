'use strict';

var jwt = require('jsonwebtoken'); // create and verify token key

var auth = {

    // Middleware to verify token
    validateToken: function(req, res, next) {
        try {
            // check header or url parameters or post parameters for token
            let token = req.query.token || req.headers['x-access-token'];

            /* decode token */
            if (token) {
                // special token for viewer user. This is token which never die
                if (token == "bbdf1bf56b15f6b15d6s1b5d1b56dfb1fs") {
                    req.decoded = {
                        username: 'viewer',
                        roles: ['viewer']
                    };
                    return next();
                }

                // verifies secret and checks exp            
                jwt.verify(token, __config.server.secretKey, function(err, decoded) {
                    if (err) {
                        return res.status(__httpStatus.NOT_ACCEPTABLE).json({ error: err.toString() });
                    } else {
                        // if everything is good, save to request for use in other routes
                        req.decoded = decoded;
                        next();
                    }
                });
            } else {
                // if there is no token return an error
                return res.status(__httpStatus.NETWORK_AUTHENTICATION_REQUIRED).json({ error: 'No token key' });
            }

        } catch (ex) {
            __logger.logError({
                action: 'validate token key',
                status: __httpStatus.INTERNAL_SERVER_ERROR,
                message: ex.toString()
            });
            return res.status(__httpStatus.INTERNAL_SERVER_ERROR).json({ error: ex.toString() });
        }
    },

    /**
     * Generate token key by user information and secret key
     */
    genToken: function(user) {
        return jwt.sign({
            username: user.username,
            roles: user.roles
        }, __config.server.secretKey, { expiresIn: '5h' });
    },


    /*
     * Validate user
     */
    login: function(req, res) {
        var name = req.body.username; // user name should be unique
        var pwd = req.body.password;

        var log = {
            account: name,
            action: 'login',
            remoteAddress: req.connection.remoteAddress || req.headers["x-forwarded-for"],
            userAgent: req.headers['user-agent']
        };

        __db.find('users', { username: name, password: __util.sha1(pwd) }, {}, function(err, users) {
            try {
                if (err) {
                    __logger.logError(log, {
                        status: __httpStatus.UNPROCESSABLE_ENTITY,
                        message: err.toString()
                    });
                    return res.status(__httpStatus.UNPROCESSABLE_ENTITY).json({ error: err.toString() });
                }

                if (users.length == 0) {
                    return res.status(__httpStatus.NOT_FOUND).json({ error: "User not found" });
                }

                if (users[0].active == false) {
                    return res.status(__httpStatus.FORBIDDEN).json({ error: "User is deactived" });
                }

                // user and password is valid -> create token key
                let token = auth.genToken(users[0]);
                res.status(__httpStatus.OK).json({
                    token: token,
                    user: users[0]
                });
                __logger.logInfo(log, { status: __httpStatus.OK });

            } catch (ex) {
                __logger.logError(log, {
                    status: __httpStatus.INTERNAL_SERVER_ERROR,
                    message: ex.toString()
                });
                return res.status(__httpStatus.INTERNAL_SERVER_ERROR).json({ error: ex.toString() });
            }
        });
    }
}


module.exports = auth;
