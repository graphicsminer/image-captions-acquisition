'use strict'

/**
Image format
{
    _id: "5841316bd8fe5b18b9c2b4f3",
    hash: "a48cba7f215e4e06aabc92b71c2f77a2",      
    name: "20161202_153035_P_20160903_080013.jpg",
    sync: "NEW",
    status: "DONE",
    contributor: "nothing",
    reviewer: "nothing",
    categories: "@vi_VN(#tree#animal)@vi_vn(#abc#xyz)",
    captions: "@en_us(What the hell?)@vi_vn(Nothing to go.Just keep going)",
    filename: "a48cba7f215e4e06aabc92b71c2f77a2.jpg",
    longitude: 10.1545,
    altitude: 0.465626,
    latitude: 3.15112,
    updatedDate: "2016-12-02 15:31:21",
    createdDate: "2016-12-02 15:30:37",
}
*/

const multiparty = require('multiparty'); // parse multipart body and upload file
const path = require('path');
const fs = require("fs"); // handle file system

var controller = {
    get: function(req, res) {
        delete req.query.token; // remove token key which not used for filter
        controller.responseData(res, req.query);
    },

    create: function(req, res) {
        let newImage = JSON.parse(req.body.data);
        delete newImage._id;

        let roles = req.decoded.roles;
        let log = { account: req.decoded.username, action: 'create image data' };

        if (!controller.hasCreatePermission(roles)) {
            return controller.info(res, 'no permission', __httpStatus.UNAUTHORIZED, log);
        }

        // check if user exist or not
        __db.find('images', { hash: newImage.hash }, {}, function(err, docs) {
            if (err) return controller.error(res, err, __httpStatus.UNPROCESSABLE_ENTITY, log);

            if (docs.length > 0) {
                // image already exist
                return res.status(__httpStatus.CONFLICT).json({ error: "Image already exist" });

            } else {
                __db.insert('images', newImage, function(err, result) {
                    if (err) return controller.error(res, err, __httpStatus.UNPROCESSABLE_ENTITY, log);

                    if (result.result.ok == 1 && result.insertedCount > 0) {
                        __logger.logInfo(log, {
                            status: __httpStatus.OK,
                            inserted: { hash: newImage.hash }
                        });
                        return controller.responseData(res, { _id: result.insertedId });
                    }

                    return controller.error(res, "Insert image data failed", __httpStatus.NOT_MODIFIED, log);
                });
            }
        });
    },

    update: function(req, res) {
        let image = JSON.parse(req.body.data);
        let hash = req.params.hash;
        delete image._id;

        let roles = req.decoded.roles;
        let log = { account: req.decoded.username, action: 'update image data' };

        if (!controller.hasUpdatePermission(roles)) {
            return controller.info(res, 'no permission', __httpStatus.UNAUTHORIZED, log);
        }

        __db.update('images', { hash: hash }, { $set: image }, function(err, result) {
            if (err) return controller.error(res, err, __httpStatus.UNPROCESSABLE_ENTITY, log);

            if (result.matchedCount == 0) {
                return res.status(__httpStatus.NOT_FOUND).json({ error: "Image not found" });
            }

            if (result.result.ok == 1) {
                __logger.logInfo(log, {
                    status: __httpStatus.OK,
                    updated: { hash: hash }
                });
                return controller.responseData(res, { hash: hash });
            }

            return controller.error(res, "Update image data failed", __httpStatus.NOT_MODIFIED, log);
        });
    },

    delete: function(req, res) {
        let hash = req.params.hash;
        let roles = req.decoded.roles;
        let log = { account: req.decoded.username, action: 'delete image data' };

        if (!controller.hasDeletePermission(roles)) {
            return controller.info(res, 'no permission', __httpStatus.UNAUTHORIZED, log);
        }

        __db.delete('images', { hash: hash }, function(err, result) {
            if (err) return controller.error(res, err, __httpStatus.UNPROCESSABLE_ENTITY, log);

            if (result.result.ok == 1) {
                if (result.deletedCount == 0) {
                    return res.status(__httpStatus.NOT_FOUND).json({ error: "Image has already been deleted" });
                }
                __logger.logInfo(log, { status: __httpStatus.OK, deleted: { hash: hash } });
                res.status(__httpStatus.OK).json(result.result);

                // delete image file
                __util.removeFileOrDir(controller.getAbsolutePath(hash) + '*');
                return;
            }

            return controller.error(res, "Delete image data failed", __httpStatus.NOT_MODIFIED, log);

        });
    },

    upload: function(req, res) {
        let roles = req.decoded.roles;
        let log = { account: req.decoded.username, action: 'upload image' };

        if (!controller.hasCreatePermission(roles)) {
            return controller.info(res, 'no permission', __httpStatus.UNAUTHORIZED, log);
        }

        // property is available -> save image to temp folder before is renamed and save to property folder.
        var form = new multiparty.Form({ uploadDir: __config.dir.image_data }),
            tmpImagePath = null,
            filename = '';

        form.on('file', function(name, file) {
            tmpImagePath = file.path;
        });

        form.on('field', function(key, value) {
            // Retrieve other field here
            if (key == 'filename') {
                filename = value;
            }
        });

        form.on("close", function() {
            // Rename image then save to property directory.
            if (tmpImagePath == null || tmpImagePath == '' || filename == '') {
                if (err) return controller.error(res, "Upload image failed", __httpStatus.NOT_ACCEPTABLE, log);

            } else {
                fs.rename(tmpImagePath, controller.getAbsolutePath(filename), function(err) {
                    if (err) return controller.error(res, err, __httpStatus.UNPROCESSABLE_ENTITY, log);

                    __logger.logInfo(log, { status: __httpStatus.OK, filename: filename });
                    return res.status(__httpStatus.OK).json({ filename: filename });

                });
            }

        });

        form.parse(req, function(err, fields, files) {
            if (err) return controller.error(res, err, __httpStatus.INTERNAL_SERVER_ERROR, log);
        });
    },

    /* ===================== METHOD ==================== */

    // reponse and log an error
    error(res, err, status, extra) {
        __logger.logError(extra, { status: status, message: err.toString() });
        return res.status(status).json({ error: err.toString() });
    },

    info(res, msg, status, extra) {
        __logger.logInfo(extra, { status: status, message: msg.toString() });
        return res.status(status).json({ message: msg.toString() });
    },

    /**
     * Get relative path of image file
     *
     * @param filename image file name
     */
    getRelativePath: function(filename) {
        return path.join(__config.dir.image_data, filename);
    },

    /**
     * Get absolute path of image file
     *
     * @param filename image file name
     */
    getAbsolutePath: function(filename) {
        return path.join(__rootPath, controller.getRelativePath(filename));
    },

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

        __db.find('images', filters, projections, function(err, docs) {
            if (err)
                if (err) return controller.error(res, err, __httpStatus.UNPROCESSABLE_ENTITY, log);
            res.status(__httpStatus.OK).json(docs);
        });
    },

    // parse value base on key type
    parse: function(key, value) {
        // boolean type
        // if (key === 'active') {
        //     return value.toLowerCase() === 'true';
        // }
        // number type
        // if (key === 'hits') return parseInt(value);

        return value;
    },

    /* ===================== Authroize and Permission ==================== */
    // roles: ['admin', 'manager', 'contributor', 'reviewer'], // and a ghost role: "viewer"

    roleMatch: function(roles1, roles2) {
        for (let i = 0; i < roles2.length; i++) {
            if (roles1.indexOf(roles2[i]) !== -1) {
                return true;
            }
        }
        return false;
    },

    hasCreatePermission: function(roles) {
        return controller.roleMatch(roles, ['admin', 'manager', 'contributor']);
    },

    hasUpdatePermission: function(roles) {
        return controller.roleMatch(roles, ['admin', 'manager', 'contributor', 'reviewer']);
    },

    hasDeletePermission: function(roles) {
        return controller.roleMatch(roles, ['admin', 'manager']);
    }

}

module.exports = controller;
