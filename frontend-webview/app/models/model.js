/* ============================ User Model ============================ */
function User(data) {
    this.data = data;
}

User.prototype = {

    // Get data by key
    get: function(key) {
        return this.data[key];
    },

    // Set or add data
    set: function(key, value) {
        this.data[key] = value;
    },

    equals: function(user) {
        return this.get('_id') == user.get('_id');
    },

    // check whether use has specified roles or not
    hasRoles: function(roles) {
        for (let i = 0; i < roles.length; i++) {
            if (this.data.roles.indexOf(roles[i]) != -1) {
                return true;
            }
        }
        return false;
    },

    // Check whether user has comment permission or not
    hasCommentPermission: function(image) {
        if (this.hasRoles(['admin', 'manager']) ||
            (this.hasRoles(['reviewer']) && this.get('username') != image.get('contributor'))) {
            return true;
        }
        return false;
    },

    // Check whether user has permission to click 'accept' button or not
    hasAcceptPermission: function(image) {
        if (image != null) {
            if (this.hasRoles(['admin', 'manager']) ||
                (this.hasRoles(['reviewer']) && this.get('username') != image.get('contributor'))) {
                return true;
            }
        } else {
            if (this.hasRoles(['admin', 'manager', 'reviewer'])) {
                return true;
            }
        }

        return false;
    },

    // Check whether user has permission to click "Move to DONE" button or not
    hasDonePermission: function(image) {
        if (image != null) {
            if (this.hasRoles(['admin', 'manager']) ||
                (this.hasRoles(['contributor']) && this.get('username') === image.get('contributor'))) {
                return true;
            }
        } else {
            if (this.hasRoles(['admin', 'manager', 'contributor'])) {
                return true;
            }
        }

        return false;
    },

    // Check whether user has permission to click "Move to TODO" button or not
    hasTodoPermission: function(image) {
        if (image != null) {
            if (this.hasRoles(['admin', 'manager', 'reviewer']) ||
                (this.hasRoles(['contributor']) && this.get('username') === image.get('contributor'))) {
                return true;
            }
        }

        return false;
    },

    // check whether user has permission to upload image or not
    hasUploadPermission: function() {
        if (this.hasRoles(['admin', 'manager', 'contributor'])) {
            return true;
        }
        return false;
    }
}


/* =========================== Image Model ============================ */
function Image(data) {
    this.data = data;
}

Image.prototype = {

    // Get data by key
    get: function(key) {
        return this.data[key];
    },

    // Set or add data
    set: function(key, value) {
        this.data[key] = value;
    },

    equals: function(image) {
        return this.get('_id') == image.get('_id');
    },

    /**
     * Get languages using in categories
     * 
     * @return array of language. For example ['@vi_VN', '@en_US']
     */
    getCategoryLanguages: function() {
        var result = [],
            lang,
            re = /@(.+?)\(/ig;

        while ((lang = re.exec(this.get('categories'))) !== null) {
            result.push(lang[1]);
        }

        return result;
    },

    /**
     * Get languages using in captions
     * 
     * @return array of language. For example ['@vi_VN', '@en_US']
     */
    getCaptionLanguages: function() {
        var result = [],
            lang,
            re = /@(.+?)\(/ig;

        while ((lang = re.exec(this.get('captions'))) !== null) {
            result.push(lang[1]);
        }

        return result;
    },

    /**
     * Get categories and clasify by language.
     * Categories string has the format like "@en_US(#tree#animal)@vi_VN(#xyz)"
     *
     * @Return categories. For example: {@en_uS: ['tree', 'animal'], @vi_VN: ['vietnam']}
     */
    getCategories: function() {
        var result = {},
            group,
            re = /(@.+?)\(([^@:;,='"/\\<>%]+)\)/ig;

        while ((group = re.exec(this.get('categories'))) !== null) {
            let lang = group[1];
            if (result[lang] == undefined) {
                result[lang] = [];
            }
            group[2].split('#').forEach(function(cap) {
                if (cap !== '') result[lang].push(cap);
            });
        }

        return result;
    },

    /**
     * Get captions and clasify by language
     * Captions string has the format like "@en_US(tree.animal)@vi_VN(xyz)"
     *
     * @Return captions. For example: {@en_uS: ['abc', 'abc'], @vi_VN: ['zzz']}
     */
    getCaptions: function() {
        var result = {},
            group,
            re = /(@.+?)\(([^@:;,='"/\\<>%]+)\)/ig;

        while ((group = re.exec(this.get('captions'))) !== null) {
            let lang = group[1];
            if (result[lang] == undefined) {
                result[lang] = [];
            }
            group[2].split('.').forEach(function(cap) {
                if (cap !== '') result[lang].push(cap);
            });
        }

        return result;
    }
}
