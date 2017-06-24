function Helpers() {}

Helpers.prototype = {
    getToken: function() {
        return localStorage.getItem('token');
    },

    setToken: function(token) {
        localStorage.setItem('token', token);
    },

    getLoggedUser: function() {
        return localStorage.getItem('logged-user');
    },

    setLoggedUser: function(userid) {
        localStorage.setItem('logged-user', userid);
    },

    getLanguage: function() {
        var lang = localStorage.getItem('language');
        if (lang == null) {
            // set default language is English
            lang = '@en_US';
        }
        return lang;
    },

    setLanguage: function(language) {
        localStorage.setItem('language', language);
    },

    toggleLanguage: function() {
        if (this.getLanguage() === '@vi_VN') {
            this.setLanguage('@en_US');
            return 'English';
        } else {
            this.setLanguage('@vi_VN');
            return 'Vietnamese';
        }
    },

    getDefaultCategoryThreshold: function() {
        var thresh = localStorage.getItem('category-threshold');
        thresh = thresh ? parseInt(thresh) : 5
        return thresh;
    },

    setDefaultCategoryThreshold: function(thresh) {
        localStorage.setItem('category-threshold', thresh);
    },

    isCategoryGroupped: function() {
        var isGroupped = localStorage.getItem('category-grouped');
        isGroupped = isGroupped && isGroupped == 'true' ? true : false;
        return isGroupped;
    },

    setCategoryGrouped: function(isGroupped) {
        localStorage.setItem('category-grouped', isGroupped);
    },

    getCategorySortedType: function() {
        // available types: ['ASC', 'DESC', 'NONE']
        var sortType = localStorage.getItem('category-sort-type');
        sortType = sortType ? sortType : 'NONE'
        return sortType;
    },

    setCategorySortedType: function(sortType) {
        localStorage.setItem('category-sort-type', sortType);
    },

    // Notify error message
    error: function(message, alertId) {
        var alert = (alertId != undefined) ? $('#' + alertId) : $('#alert-error');
        var alertMsg = (message != undefined) ? message : 'Error occur!. Please contact administrator';
        alert && alert.html(alertMsg).show().css('opacity', '1').fadeTo(5000, 0);
    },

    // Notify success message
    success: function(message, alertId) {
        var alert = (alertId != undefined) ? $('#' + alertId) : $('#alert-success');
        var alertMsg = (message != undefined) ? message : 'Successfully!';
        alert && alert.html(alertMsg).show().css('opacity', '1').fadeTo(5000, 0);
    },

    // Get current date time as string
    // format: yyyy-mm-dd hh:mm:ss
    // example: "2016-12-02 15:31:21"
    getDateTimeString: function(date) {
        var year = '' + date.getFullYear(),
            month = '' + (date.getMonth() + 1),
            day = '' + date.getDate(),
            hour = '' + date.getHours(),
            seconds = '' + date.getSeconds(),
            minutes = '' + date.getMinutes();

        month = (month.length < 2) ? '0' + month : month;
        day = (day.length < 2) ? '0' + day : day;
        hour = (hour.length < 2) ? '0' + hour : hour;
        minutes = (minutes.length < 2) ? '0' + minutes : minutes;
        seconds = (seconds.length < 2) ? '0' + seconds : seconds;

        return [year, month, day].join('-') + ' ' + [hour, minutes, seconds].join(':');
    },

    // escape HTML string
    // Thank to http://stackoverflow.com/questions/6020714/escape-html-using-jquery
    escapeHtml: function(str) {
        // List of HTML entities for escaping.
        var htmlEscapes = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#x27;',
            '/': '&#x2F;'
        };

        // Regex containing the keys listed immediately above.
        var htmlEscaper = /[&<>"'\/]/g;

        return ('' + str).replace(htmlEscaper, function(match) {
            return htmlEscapes[match];
        });
    },

    // check whether text contain special characters or not
    containSpecialCharacter: function(text, specials) {
        for (let i = 0; i < specials.length; i++) {
            if (text.indexOf(specials[i]) != -1) {
                return true;
            }
        }
        return false;
    },

    /**
     * Get file extension (.jpg) and name without extension
     * @param filename filename with extension
     */
    splitFileNameAndExtension: function(filename) {
        var extIdx = filename.lastIndexOf(".");
        name = (extIdx > 0) ? filename.substring(0, extIdx) : filename;
        ext = (extIdx > 0) ? filename.substring(extIdx) : "";
        return [name, ext];
    },

    // get colors
    getColors: function() {
        return ["#E3D761", "#2fe4fd",
            "#fd482f", "#5d3954", "#856088", "#ffb35d", "#e1e001", "#637c96",
            "#ed6464", "#9cbc7c", "#93e1b3", "#fd482f", "#262f38", "#ff5722",
            "#34866f", "#f68d2e", "#007FFF", "#98777B", "#2E5894", "#8A2BE2",
            "#FFA500", "#FAEBD7", "#00FFFF", "#7FFFD4", "#808000", "#F5F5DC",
            "#FFE4C4", "#FFEBCD", "#FFFF00", "#0000FF", "#8A2BE2", "#B5A642",
            "#A52A2A", "#DEB887", "#5F9EA0", "#7FFF00", "#D2691E", "#D98719",
            "#BF00DF", "#FF7F50", "#BFEFDF", "#6495ED", "#FFF8DC", "#DC143C",
            "#00FFFF", "#00008B", "#DA0B00", "#008B8B", "#B8860B", "#A9A9A9",
            "#006400", "#BDB76B", "#8B008B", "#556B2F", "#FF8C00", "#9932CC",
            "#8B0000", "#E9967A", "#8FBC8F", "#483D8B", "#2F4F4F", "#00CED1",
            "#9400D3", "#FF1493", "#00BFFF", "#696969", "#1E90FF", "#FED0E0",
            "#B22222", "#FFFAF0", "#228B22", "#FF00FF", "#DCDCDC", "#F8F8FF",
            "#FFD700", "#DAA520", "#808080", "#008000", "#ADFF2F", "#F0FFF0",
            "#FF69B4", "#CD5C5C", "#4B0082", "#FFFFF0", "#F0E68C", "#E6E6FA",
            "#FFF0F5", "#7CFC00", "#FFFACD", "#ADD8E6", "#F08080", "#E0FFFF",
            "#FAFAD2", "#90EE90", "#D3D3D3", "#FFB6C1", "#FFA07A", "#20B2AA",
            "#87CEFA", "#778899", "#B0C4DE", "#FFFFE0", "#00FF00", "#32CD32",
            "#FAF0E6", "#FF00FF", "#800000", "#66CDAA", "#0000CD", "#BA55D3",
            "#9370DB", "#3CB371", "#7B68EE", "#00FA9A", "#48D1CC", "#C71585",
            "#191970", "#F5FFFA", "#FFE4E1", "#FFE4B5", "#FFDEAD", "#000080",
            "#FDF5E6", "#808000", "#6B8E23", "#FFA500", "#FF4500", "#DA70D6",
            "#EEE8AA", "#98FB98", "#AFEEEE", "#DB7093", "#FFEFD5", "#FFDAB9",
            "#CD853F", "#FFC0CB", "#DDA0DD", "#B0E0E6", "#800080", "#FF0000",
            "#0CB0E0", "#BC8F8F", "#4169E1", "#8B4513", "#FA8072", "#F4A460",
            "#2E8B57", "#FFF5EE", "#A0522D", "#C0C0C0", "#87CEEB", "#6A5ACD",
            "#708090", "#FFFAFA", "#00FF7F", "#4682B4", "#D2B48C", "#008080",
            "#D8BFD8", "#FF6347", "#40E0D0", "#EE82EE", "#FFFFFF", "#000000",
            "#F5F5F5", "#FFFF00", "#9ACD32"
        ]
    },

    // wrap svg text element by symbol
    wrap: function(textSvg, symbol) {
        var words = textSvg.text().trim().split(symbol).reverse();
        var word = "",
            line = new Array(),
            lineNumber = 0,
            lineHeight = 1.3; // ems
        var y = textSvg.attr("y");
        var x = textSvg.attr("x");
        var dy = parseFloat(textSvg.attr("dy"));
        if (isNaN(dy)) {
            dy = 0;
        }

        textSvg.text(null).append("tspan").attr("x", x).attr("y", y).attr("dy", dy + "em");
        while (word = words.pop()) {
            textSvg.append("tspan").attr("x", x).attr("y", y).attr("dy", ++lineNumber * lineHeight + dy + "em").text(word.trim());
        }
    }


}

var _helper = new Helpers();
