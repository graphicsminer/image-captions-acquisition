const path = require('path');
const request = require('request');
const fs = require('fs');
const shell = require('shelljs');

const _rootPath = path.resolve(__dirname);
const _util = require(_rootPath + '/app/utils/util.js');
const _config = _util.parseJson(_rootPath + '/configs/setting.json');
const _db = new(require(_rootPath + '/app/services/mongo.js'))(_config.mongodb);

function do_script(args) {
    switch (args[2]) {
        case '--create-user':
            createUser();
            break;
        case '--update-user':
            updateUser();
            break;
        case '--remove-user':
            // node script.js --remove-user username -p
            if (args[4] == '-p') {
                removeUser(args[3]);
            } else {
                activeUser(args[3], false);
            }
            break;
        case '--active-user':
            activeUser(args[3], true);
            break;
        case '--deactive-user':
            activeUser(args[3], false);
            break;
        case '--version':
            showVersion();
            break;
        case '--pack-data':
            packageDataForRelease(args[3]);
            break;
        default:
    }

}
do_script(process.argv);

/* ===================== METHOD ==================== */

// create user
// available roles = ['admin', 'manager', 'contributor', 'reviewer']
function createUser() {
    var newUser = {
        username: 'contributor'.toLocaleLowerCase(),
        password: _util.sha1('type password here'),
        fullname: "contributor",
        email: "admin@gmail.com",
        active: true,
        roles: ['admin', 'manager', 'contributor', 'reviewer']
    }
    _db.insert('users', newUser, function(err, result) {
        callback(err, 'insert user ' + newUser.username);
    });
}

// update user
function updateUser() {
    var updateUser = {
        username: 'contributor'.toLocaleLowerCase(),
        // password: _util.sha1('type password here'),
        // fullname: "I am contributor",
        // email: "admin@gmail.com",
        // active: true,
        // roles: ['admin', 'manager', 'contributor', 'reviewer']
    }
    _db.update('users', { username: updateUser.username }, { $set: updateUser },
        function(err, result) {
            callback(err, 'update user ' + updateUser.username);
        });
}

// remove user
function removeUser(username) {
    _db.delete('users', { username: username }, function(err, result) {
        callback(err, "remove user " + username);
    });
}

// active and deactive user
function activeUser(username, isActive) {
    _db.update('users', { username: username }, { $set: { active: isActive } },
        function(err, result) {
            callback(err, ((isActive) ? 'active' : 'deactive') + ' user ' + username);
        });
}

function callback(err, message) {
    if (err) return console.log('[FAILED] ' + message + ". Cause: " + err);
    return console.log('[DONE] ' + message);
}

// show version
function showVersion() {
    _db.find('versions', {}, {}, function(err, docs) {
        console.log(docs[0].build);
        console.log('version number: ' + docs[0].number);
    });
}

// package data for release
function packageDataForRelease(dir) {
    let url = 'http://caption-server.graphicsminer.com/api/image-data?token=umetnogtbdvtadmaaeitaeuiymis';
    let query = '&filters={"status":{"$in":["ACCEPTED"]}}&projections={"_id":0,"name":0,"comments":0,"sync":0}';

    // create directory for storing images
    let imageDir = path.join(dir, 'data');
    shell.mkdir("-p", imageDir);

    request(url + query, function(err, res, body) {
        let data = JSON.parse(body);
        downloadImage(imageDir, data, 0);

    }).pipe(fs.createWriteStream(path.join(dir, 'data.json')));
}

function downloadImage(dir, data, idx) {
    let staticImageUrl = 'http://localhost:1508/public/images/';

    if (idx > data.length - 1) {
        return console.log('\n Finish! .............. ')
    }

    let filename = data[idx].filename;
    request(staticImageUrl + filename).pipe(fs.createWriteStream(path.join(dir, filename)).on('close', function() {
        console.log('[done] ' + filename + ' - ' + idx);
        downloadImage(dir, data, idx + 1);
    }));
}
