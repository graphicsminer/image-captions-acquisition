'use strict'

/**
 * Create Logger class which extends method of winston.Logger class.
 * Ref: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Introduction_to_Object-Oriented_JavaScript#Inheritance
 *
 * Using:
 * var _logger = new(require('./logger.js'))(dir, mode);
 *
 * # print error to console or save to file
 * _logger.log('error', 'error message') 
 * _logger.log('infor', 'message');
 *
 * # Both response client and print or write log message
 * _logger.sendError(res, status, error-message, options);
 * _logger.send(res, status, data, options)
 */
const winston = require('winston');
const rotateFile = require('winston-daily-rotate-file');
const pathUtil = require('path');

// level of logging
const levels = ['error', 'warn', 'info', 'verbose', 'debug', 'silly']

// Create transport based on file name and level
// https://github.com/winstonjs/winston/pull/205
function newTransportOpt(dir, filename, level) {
    return {
        name: level + "-file",
        filename: pathUtil.join(dir, filename),
        datePattern: '.yyyy-MM-dd',
        maxsize: 20000000, // ~20MB
        level: level
    }
}

function GMLogger(dir, mode) {
    // Call the parent constructor, making sure (using Function#call)
    // that "this" is set correctly during the call
    winston.Logger.call(this);

    // add transport
    if (mode === 'production') {
        this.add(rotateFile, newTransportOpt(dir, 'error.log', 'error'));
        this.add(rotateFile, newTransportOpt(dir, 'infor.log', 'info'));

    } else /*development || test*/ {
        this.add(winston.transports.Console);
    }
}

// Create a Logger.prototype object that inherits from winston.Logger.prototype.
GMLogger.prototype = Object.create(winston.Logger.prototype);
// Set the "constructor" property to refer to Logger
GMLogger.prototype.constructor = GMLogger;

// Add extra method here. For example
// GMLogger.prototype.send = function() {}

GMLogger.prototype.logError = function() {
    // error && this.log('error', error.toString());
    let logs = {};
    for (let i = 0; i < arguments.length; i++) {
        for (let key in arguments[i]) {
            logs[key] = arguments[i][key];
        }
    }
    this.log('error', logs);
}

GMLogger.prototype.logInfo = function() {
    let logs = {};
    for (let i = 0; i < arguments.length; i++) {
        for (let key in arguments[i]) {
            logs[key] = arguments[i][key];
        }
    }
    this.log('info', logs);
}

module.exports = GMLogger;
