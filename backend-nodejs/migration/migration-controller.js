'use strict';
const path = require('path');

const _rootPath = path.resolve(__dirname) + "/..";
const _util = require(_rootPath + '/app/utils/util.js');
const _config = _util.parseJson(_rootPath + '/configs/setting.json');
const _db = new(require(_rootPath + '/app/services/mongo.js'))(_config.mongodb);

var builds = [
    require('./build2.5.1.js'),
    require('./build2.5.2.js'),
    require('./build2.5.5.js'),
    require('./build3.1.0.js'), // --stable
    require('./build3.1.1.js') // --alpha
]

// do migration corresponding to latest build
function migrate(builds, idx, oldVersion, next) {
    if (idx > builds.length - 1) {
        return next(builds[idx - 1]);
    }

    if (builds[idx].version().number > oldVersion) {
        // do migrate for this build.
        builds[idx].migrate(function() {
            // do migrate for next build
            migrate(builds, idx + 1, oldVersion, next);
        });
    } else {
        // do migrate for next build
        migrate(builds, idx + 1, oldVersion, next);
    }
}

_db.find('versions', {}, {}, function(err, docs) {
    if (err) {
        return console.log('[FAIL] can not find build version');
    }

    // do migrate
    migrate(builds, 0, parseInt(docs[0].number), function(latestBuild) {
        // update build version
        _db.update('versions', {}, { $set: latestBuild.version() }, function(err, result) {
            console.log('The End ---------> ');
        });
    });
});
