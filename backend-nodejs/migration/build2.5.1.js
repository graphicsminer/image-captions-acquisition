'use strict';

const path = require('path');

const _rootPath = path.resolve(__dirname) + "/..";
const _util = require(_rootPath + '/app/utils/util.js');
const _config = _util.parseJson(_rootPath + '/configs/setting.json');
const _db = new(require(_rootPath + '/app/services/mongo.js'))(_config.mongodb);

function Build251() {}

Build251.prototype = {
    // Handle migrate here. Must implement
    migrate: function(next) {
        console.log('Start build 2.5.1 -------->>>')

        // Change status from DONE -> ACCEPTED
        _db.connect(function(err, db) {
            if (err) {
                console.log("Connection fail!");
            }

            db.collection('images').updateMany({ status: "DONE" }, { $set: { status: "ACCEPTED" } }, function(err, result) {
                if (err) {
                    console.log('[Error] Change status from DONE -> ACCEPTED', err);
                    return db.close();
                }

                // Change status from FEEDBACK -> DONE
                db.collection('images').updateMany({ status: 'FEEDBACK' }, { $set: { status: 'DONE' } }, function(err, result) {
                    if (err) {
                        console.log('[Error] Change status from FEEDBACK -> DONE', err);
                        return db.close();
                    }

                    console.log('[Done] Migrate successfully!');
                    console.log('End build 2.5.1 -------->>>', '\n')
                    db.close();
                    next();
                });
            });
        })

    },

    // return build version. Must implement
    version: function() {
        return { number: 251, build: 'build 2.5.1-alpha production 2017-03-06' };
    }
}

module.exports = new Build251();
