'use strict';

function Build255() {}

Build255.prototype = {
    // Handle migrate here. Must implement
    migrate: function(next) {
        console.log('Start build 2.5.5-stable -------->>>')
        console.log('[Done] Migrate successfully!');
        console.log('End build 2.5.5 -------->>>', '\n')
        next();
    },

    // return build version. Must implement
    version: function() {
        return { number: 255, build: 'build 2.5.5-stable production 2017-03-20' };
    }
}

module.exports = new Build255();
