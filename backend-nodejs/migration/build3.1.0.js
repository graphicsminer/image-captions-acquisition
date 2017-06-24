'use strict';

function Build310() {}

Build310.prototype = {
    // Handle migrate here. Must implement
    migrate: function(next) {
        console.log('Start build 3.1.0-stable -------->>>')
        console.log('[Done] Migrate successfully!');
        console.log('End build 3.1.0 -------->>>', '\n')
        next();
    },

    // return build version. Must implement
    version: function() {
        return { number: 310, build: 'build 3.1.0-stable production 2017-06-06' };
    }
}

module.exports = new Build310();
