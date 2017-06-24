'use strict';

const path = require('path');

const _rootPath = path.resolve(__dirname) + "/..";
const _util = require(_rootPath + '/app/utils/util.js');
const _config = _util.parseJson(_rootPath + '/configs/setting.json');
const _db = new(require(_rootPath + '/app/services/mongo.js'))(_config.mongodb);

function Build252() {}

Build252.prototype = {
    // Handle migrate here. Must implement
    migrate: function(next) {
        console.log('Start build 2.5.2 -------->>>')

        _db.connect(function(err, db) {
            if (err) {
                console.log("Connection fail!");
            }

            db.collection('images').find({}, {}).toArray(function(err, docs) {
                if (err) return db.close();

                let promises = [];
                docs.forEach(function(img) {
                    let dateStr = img.updatedDate;
                    let d = new Date(dateStr);
                    if (isNaN(d.getMonth())) {
                        // fix date
                        dateStr = dateStr.slice(0, 5) + '0' + dateStr[5] + dateStr.slice(7);
                        promises.push(new Promise(function(resolve, reject) {
                            db.collection('images').updateOne({ hash: img.hash }, { $set: { updatedDate: dateStr } }, function(err, result) {
                                if (err) {
                                    return console.log('[Error] Cannot fix ' + dateStr, err);
                                }
                                resolve();
                            });
                        }));
                    }
                });


                Promise.all(promises).then(function(results) {
                    console.log('[Done] Migrate successfully!');
                    console.log('End build 2.5.2 -------->>>', '\n')
                    db.close();
                    next();
                });


            });
        })

    },

    // return build version. Must implement
    version: function() {
        return { number: 252, build: 'build 2.5.2-alpha production 2017-03-20' };
    }
}

module.exports = new Build252();
