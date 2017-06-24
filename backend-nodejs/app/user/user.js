'use strict';

/**
User format
{
    _id: '575b01dcd98cdbadc9bd6bf6',
    username : 'admin',
    password: '3fa23035f758b1479c2f6d92b60b4a84',
    fullname: 'admin',
    email: 'admin@gmail.com',
    active: true,
    roles: ['admin', 'manager', 'contributor', 'reviewer'], // and a ghost role: "viewer"
    photo: 'admin.jpg'
}
*/
var controller = {
    get: function(req, res) {
        delete req.query.token; // remove token key which not used for filter
        controller.responseData(res, req.query);
    },

    create: function(req, res) {
        var roles = req.decoded.roles;
        var log = { account: req.decoded.username, action: 'create user' };

        // Only admin can create new user
        if (!controller.isAdmin(roles)) {
            return controller.info(res, 'no permission', __httpStatus.UNAUTHORIZED, log);
        }

        var newUser = JSON.parse(req.body.data);
        delete newUser._id;
        // check if user exist or not
        __db.find('users', { username: newUser.username }, { password: 0 }, function(err, docs) {
            if (err) return controller.error(res, err, __httpStatus.UNPROCESSABLE_ENTITY, log);

            if (docs.length > 0) {
                // user already exist
                return res.status(__httpStatus.CONFLICT).json({ error: "User already exist" });

            } else {
                __db.insert('users', newUser, function(err, result) {
                    if (err) return controller.error(res, err, __httpStatus.UNPROCESSABLE_ENTITY, log);

                    if (result.result.ok == 1 && result.insertedCount > 0) {
                        delete newUser._id;
                        __logger.logInfo(log, { status: __httpStatus.OK, inserted: newUser });
                        return controller.responseData(res, { _id: result.insertedId });
                    }

                    return controller.error(res, "Insert user failed", __httpStatus.NOT_MODIFIED, log);
                });
            }
        });
    },

    update: function(req, res) {
        var updatedUser = JSON.parse(req.body.data);
        var id = req.params.id;
        delete updatedUser._id;
        var log = { account: req.decoded.username, action: 'update user' };
        var roles = req.decoded.roles;

        // only admin can change username and role
        if (!controller.isAdmin(roles)) {
            return controller.info(res, 'no permission', __httpStatus.UNAUTHORIZED, log);
        }

        __db.update('users', { _id: __db.toObjectId(id) }, { $set: updatedUser }, function(err, result) {
            if (err) return controller.error(res, err, __httpStatus.UNPROCESSABLE_ENTITY, log);

            if (result.matchedCount == 0) {
                return res.status(__httpStatus.NOT_FOUND).json({ error: "User not found" });
            }

            if (result.result.ok == 1) {
                __logger.logInfo(log, { status: __httpStatus.OK, updated: updatedUser });
                return controller.responseData(res, { _id: __db.toObjectId(id) });
            }

            return controller.error(res, "Update user failed", __httpStatus.NOT_MODIFIED, log);
        });
    },

    delete: function(req, res) {
        var id = req.params.id;
        var roles = req.decoded.roles;
        var log = { account: req.decoded.username, action: 'delete user' };

        // Only admin can delete new user
        if (!controller.isAdmin(roles)) {
            return controller.info(res, 'no permission', __httpStatus.UNAUTHORIZED, log);
        }

        __db.find('users', { _id: __db.toObjectId(id) }, {}, function(err, docs) {
            if (err) return controller.error(res, err, __httpStatus.UNPROCESSABLE_ENTITY, log);

            __db.delete('users', { _id: __db.toObjectId(id) }, function(err, result) {
                if (err) return controller.error(res, err, __httpStatus.UNPROCESSABLE_ENTITY, log);

                if (result.result.ok == 1) {
                    if (result.deletedCount == 0) {
                        return res.status(__httpStatus.NOT_FOUND).json({ error: "User has already been deleted" });
                    }
                    __logger.logInfo(log, { status: __httpStatus.OK, deleted: { username: docs[0].username } });
                    return res.status(__httpStatus.OK).json(result.result);
                }

                return controller.error(res, "Delete user failed", __httpStatus.NOT_MODIFIED, log);
            });

        });

    },

    /* ===================== METHOD ==================== */

    /**
     * Get data by specified query then send to client
     *
     * @param res response object
     * @query query in json format
     */
    responseData: function(res, query) {
        let log = { account: "", action: 'query image data' };

        // suport complex mongo filter and search condition
        // Example: GET /users?filters={"username":{"$in":["admin", "contributor"]}}
        var filters = {};
        for (let key in query) {
            if (key === 'projections') continue;
            
            if (key === 'filters') {
                let ft = JSON.parse(query.filters);
                for (let prop in ft) {
                    filters[prop] = ft[prop];
                }

            } else {
                filters[key] = controller.parse(key, query[key]);
            }
        }
        if (filters._id != null && typeof(filters._id) == 'string') {
            filters._id = __db.toObjectId(filters._id);
        }

        // support excluded fields
        // Example: GET /users?projections={"username":1, "password": 0} -> include username and exclude password
        var projections = {};
        if (query.projections != null) {
            projections = JSON.parse(query.projections);
        }

        __db.find('users', filters, projections, function(err, docs) {
            if (err) return controller.error(res, err, __httpStatus.UNPROCESSABLE_ENTITY, log);
            res.status(__httpStatus.OK).json(docs);
        });
    },

    // reponse and log an error
    error(res, err, status, extra) {
        __logger.logError(extra, { status: status, message: err.toString() });
        return res.status(status).json({ error: err.toString() });
    },

    info(res, msg, status, extra) {
        __logger.logInfo(extra, { status: status, message: msg.toString() });
        return res.status(status).json({ message: msg.toString() });
    },

    // parse value base on key type
    parse: function(key, value) {
        // boolean type
        if (key === 'active') {
            return value.toLowerCase() === 'true';
        }

        // number type
        // if (key === 'hits') return parseInt(value);

        return value;
    },

    /* ===================== Authroize and Permission ==================== */

    isAdmin: function(roles) {
        return roles.indexOf('admin') !== 1;
    }
}

module.exports = controller;
