'use strict'

const path = require('path');
const fs = require("fs"); // handle file system

var controller = {

    getConfigFile: function(req, res) {
        var filename = req.params.filename,
            filePath = path.join(__rootPath, 'configs', filename);

        if (fs.existsSync(filePath)) {
            res.download(filePath);

        } else {
            res.status(__httpStatus.NOT_FOUND).json({ error: 'file not found' });
        }

    },

    /* ===================== METHOD ==================== */

}

module.exports = controller;
