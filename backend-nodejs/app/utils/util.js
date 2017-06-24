'use strict'

const fs = require('fs');
const crypto = require('crypto');
const exec = require('child_process').exec;

function GMUtils() {}
GMUtils.prototype = {

    /** 
     * Convert content of JSON file to JSON object
     * @param path: path of JSON file 
     */
    parseJson: function(path) {
        return JSON.parse(fs.readFileSync(path));
    },

    /*
     * Generate hash string using MD5 agorithm.
     * @return: somthing like "9f6290f4436e5a2351f12e03b6433c3c"
     */
    md5: function(str) {
        return crypto.createHash('md5').update(str).digest("hex");
    },

    /*
     * Generate hash string using MD5 agorithm.
     * @return: somthing like "476432a3e85a0aa21c23f5abd2975a89b6820d63"
     */
    sha1: function(str) {
        return crypto.createHash('sha1').update(str).digest("hex");
    },

    /*
     * Generate hash string using MD5 agorithm.
     * @return: somthing like "476432a3e85a0aa21c23f5abd2975a89b6820d63"
     */
    sha256: function(str) {
        return crypto.createHash('sha256').update(str).digest("hex");
    },

    /**
     * Remove directory or file
     *
     * @param path: file/dir path
     * @param callback: function(err). nullable
     */
    removeFileOrDir: function(path, callback) {
        // success -> delete folder which contain image of above property
        exec("rm -rf " + path, function(error, stdout, stderr) {
            callback && callback(error);
        });
    },

    /**
     * Create directory
     *
     * @param path directory path
     * @param callback(err) function. nullable
     */
    createDir: function(path, callback) {
        fs.access(path, function(err) {
            if (err && err.code === 'ENOENT') {
                // directory has not created -> create
                fs.mkdir(path, function(err) {
                    if (err && err.code != 'EEXIST') {
                        callback && callback(err);
                    }
                    callback && callback();
                });
            }
        });
    }

}

// export model for global use
module.exports = new GMUtils();
