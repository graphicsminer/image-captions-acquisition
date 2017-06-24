/*
 * Compress javascript file then put to dist folder which then is used for deploying
 * 
 * Project structure
 * |- dist
 *      |- app
 *          |- chart
 *              |- chart.html
 *              |- chart.js
 *              |- index.html
 *          |- editor
 *              |- editor.html
 *              |- editor.js
 *              |- index.html
 *          |- login
 *              |- login.html
 *              |- login.js
 *              |- index.html
 *          |- models
 *              |- index.html
 *              |- model.js
 *          |- utils
 *              |- index.html
 *              |- helpers.js
 *          |- index.html
 *      |- assets
 *      |- configuration.js
 *      |- index.html
 *      |- index.js
 */
const _shell = require('shelljs');
const _util = require('util');

var helper = {
    compress: function(file1, file2) {
        let format = "java -jar yuicompressor-2.4.8.jar %s -o %s --charset utf-8"
        return _util.format(format, file1, file2);
    }
}

// Start
console.log("Start Building ...");

// remove dist directory
_shell.rm("-rf", "dist");
console.log("[Done] Remove dist directory");

// create necessary directories and copy fixed file to directory (*.html, assets file ...)
_shell.mkdir("-p", [
    "dist/app/chart",
    "dist/app/editor",
    "dist/app/login",
    "dist/app/models",
    "dist/app/utils",
    "dist/assets",
]);
_shell.cp("-R", "app/index.html", "dist/app");
_shell.cp("-R", "assets/*", "dist/assets");
_shell.cp(["index.html", "configuration.js"], "dist");
["chart", "editor", "login", "models", "utils"].forEach(function(dir) {
    _shell.cp("app/" + dir + "/*.html", "dist/app/" + dir + "/.");
});
console.log("[Done] Copy non js files");

// Compress javascript file then put to repective directory in dist directory
if (_shell.exec(helper.compress("index.js", "dist/index.js")).code !== 0) {
    _shell.echo('[Error]: Compress js file failed!');
    _shell.exit(1);
}

["chart/chart.js", "editor/editor.js", "login/login.js", "models/model.js", "utils/helpers.js"].forEach(function(file) {
    if (_shell.exec(helper.compress("app/" + file, "dist/app/" + file)).code !== 0) {
        _shell.echo('[Error]: Compress js file failed!');
        _shell.exit(1);
    }
});

console.log("[Done] Compress javascript file");

// DONE 
console.log("Finish!");
