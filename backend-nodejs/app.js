// Require external/third-party scripts
var express = require('express');
var app = express();

var bodyParser = require('body-parser'); // parse body. Can not parse multipart body
var path = require('path');

// require internal scripts
const _middleware = require('./app/utils/middleware.js');

var helmet = require('helmet');

/* ===================== GLOBAL ==================== */

global.__rootPath = path.resolve(__dirname);
global.__util = require('./app/utils/util.js');
global.__config = __util.parseJson(__rootPath + '/configs/setting.json');
global.__logger = new(require(__rootPath + '/app/utils/logger.js'))(__rootPath + '/' + __config.dir.logger, __config.mode);
global.__db = new(require(__rootPath + '/app/services/mongo.js'))(__config.mongodb);
global.__httpStatus = require(__rootPath + '/app/utils/http-status-codes.js');

/* =================== MIDDLEWARE ================== */

// parse body in middleware
app.use(bodyParser.json()); // support json encoded bodies
app.use(bodyParser.urlencoded({ extended: true })); // support encoded bodies

// enable CORS
app.use(_middleware.cors);

// secure with helmet
// https://expressjs.com/en/advanced/best-practice-security.html
app.use(helmet());

/* ===================== ROUTER ==================== */

// serve static files
app.use("/public/temp", express.static(__config.dir.temp));
app.use("/public/share", express.static('shared'));
app.use("/public/images", express.static(__config.dir.image_data));

// User API
var user = require('./app/user')(); // don't need to provide index file b/c it is index file
app.use(user.login);
app.use('/api/users', user.api);

// Image API
var image = require('./app/image-data')();
app.use('/api/image-data', image.api);

// Resource data (configuration file, sample data ...)
var resource = require('./app/resource-data')();
app.use('/api/resource', resource.api);

// image caption bot
var ic_bot = require('./app/caption-bot')();
app.use('/api/caption-bot', ic_bot.api);

// log error - middleware
app.use(_middleware.errorHandler);

/* ================================================= */

// listen at port 1508
app.listen(__config.server.port);
console.log('listening at port ' + __config.server.port);
