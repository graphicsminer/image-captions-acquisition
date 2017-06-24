const path = require('path');

const _rootPath = path.resolve(__dirname);
const _util = require(_rootPath + '/app/utils/util.js');
const _config = _util.parseJson(_rootPath + '/configs/setting.json');
const _db = new(require(_rootPath + '/app/services/mongo.js'))(_config.mongodb);
const _shell = require('shelljs');

// Setup or reset environment using command
process.argv.forEach(function(val, idx) {
    switch (val) {
        case '--init':
            init();
            break;
        case '--reset':
            reset();
            break;
        default:
    }
});

// Initialize resource and configuration for project
function init() {

    // insert default admin user
    _db.insert('users', {
            username: 'admin',
            password: _util.sha1('admin'),
            fullname: 'admin',
            email: 'admin@gmail.com',
            roles: ['admin']
        },
        function(err, result) {
            callback(err, 'insert admin user');
        });

    // insert version
    _db.insert('versions', { number: 310, build: 'build 3.1.0-stable production 2017-06-06' }, function(err, result) {
        callback(err, 'insert versions');
    });

    let dirs = [];
    for (let key in _config.dir) {
        dirs.push(_config.dir[key]);
    }
    _shell.mkdir("-p", dirs);
    console.log("[DONE] Create directories")
}

// Reset environment
function reset() {
    // drop database
    _db.dropDatabase(function(err, result) { callback(err, 'drop database') });


    // delete directories
    _shell.rm("-rf", "data");
    _shell.rm("-rf", "logs");
    console.log("[DONE] Remove directories")
}

function callback(err, message) {
    if (err) return console.log('[FAILED] ' + message + ". Cause: " + err);
    return console.log('[DONE] ' + message);
}
