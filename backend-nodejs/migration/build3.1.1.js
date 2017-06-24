'use strict';

const path = require('path');
const _shell = require('shelljs');

const _rootPath = path.resolve(__dirname) + "/..";
const _util = require(_rootPath + '/app/utils/util.js');
const _config = _util.parseJson(_rootPath + '/configs/setting.json');

function Build311() {}

Build311.prototype = {
    // Handle migrate here. Must implement
    migrate: function(next) {
        console.log('Start build 3.1.1-stable -------->>>')

        // create "ic-bot" folder
        _shell.mkdir("-p", _config.dir.ic_bot);

        console.log('[Done] Migrate successfully!');
        console.log('End build 3.1.1 -------->>>', '\n')
        next();
    },

    // return build version. Must implement
    version: function() {
        return { number: 311, build: 'build 3.1.1-alpha production 2017-06-13' };
    }
}

module.exports = new Build311();
