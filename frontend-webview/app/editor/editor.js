$(document).ready(function() {
    var token = _helper.getToken();
    var loggedUser, images = [];
    var uploadedFiles = [];
    var dirtyChange = false;
    var cancelUpload = false;
    var overlayWaiting = $('#overlayWaiting');
    overlayWaiting.hide();

    var imageSliderShow = $('#imageSliderShow');
    var searchInput = $('#inputBox');
    var categoriesInput = $('#categoriesInfo');
    var captionsInput = $('#captionsInfo');


    // check whether user is exit or not. If not -> redirect to login page
    $.ajax({
        async: true,
        crossDomain: true,
        url: _config.api_user_get + '?_id=' + _helper.getLoggedUser() + '&token=' + token,
        type: "GET"

    }).done(function(res) {
        if (!(res instanceof Array) || res[0] == null) {
            // Notify error message
            return _helper.error();
        }

        // OK -> 
        loggedUser = new User(res[0]);

        // init and load some data
        init();

        // auto upload data to server after 3'
        setInterval(function() {
            try {
                if (dirtyChange) {
                    var imageIdx = imageSliderShow.slick('slickCurrentSlide'),
                        id = $("[data-slick-index='" + imageIdx + "']").attr('id');
                    var image = getImageById(id);

                    image && updateImage(image, function() {
                        // notify success
                        dirtyChange = false;
                        _helper.success('Updating is successful');
                    });
                }

            } catch (ex) {
                dirtyChange = false;
            }

        }, 180000);

        // handle button event
        imageSliderShow.on('afterChange reInit', onAfterImageChange);
        $('#searchBtn').click(onSearchHandler);
        $('#languageBtn').click(onChangeLanguage);
        $('#moveToDoBtn').click(onMoveToDoHandler);
        if (loggedUser.hasDonePermission()) {
            $('#doneBtn').click(onMoveToDoneHandler);
        } else {
            $('#doneBtn').remove();
            categoriesInput.attr('disabled', true);
            captionsInput.attr('disabled', true);
        }
        if (loggedUser.hasRoles(['admin', 'manager'])) {
            $('#deleteBtn').click(onDeleteConfirmationOpen);
            $('#deleteConfirmDiag #okBtn').click(onDeleteHandler);
        } else {
            $('#deleteBtn').remove();
        }
        if (loggedUser.hasAcceptPermission()) {
            $('#acceptBtn').click(onAcceptHandler);
        } else {
            $('#acceptBtn').remove();
        }

        // Upload event
        if (loggedUser.hasUploadPermission()) {
            $('#uploadBtn').click(function(e) {
                $('#uploadFileBtn').click();
            });
            $('#uploadFileBtn').change(onUploadImages);
        } else {
            $('#uploadBtn').remove();
            $('#uploadFileBtn').remove();
        }

        // display chart link if user is admin or manager -> allow all users to view chart
        // if (!loggedUser.hasRoles(['admin', 'manager'])) {
        //     $('#chartArea').remove();
        // }

        // handle short key
        handleShortKey();

    }).fail(function(res) {
        // redirect to login page
        window.location.href = "../login/login.html";
    });

    /**
     * Initialize
     */
    function init() {
        /* ----------> Display stored language */
        if (_helper.getLanguage() === '@vi_VN') {
            $('#languageBtn').html('Vietnamese');
        } else {
            $('#languageBtn').html('English');
        }

        /* ----------> Get sample categories from server then add to inuput as suggesstion */
        getSampleCategories(function(results) {
            var categories = [];
            results.forEach(function(item) {
                item.data.split('#').slice(1).forEach(function(cat) {
                    if (cat.trim() != '') {
                        categories.push({ language: item.language, text: cat.trim() });
                    }
                });
            });

            var blood = new Bloodhound({
                datumTokenizer: Bloodhound.tokenizers.obj.whitespace('text'),
                queryTokenizer: Bloodhound.tokenizers.whitespace,
                local: categories
            });
            blood.initialize();

            /* ----------> init boostrap tag input and typeahead for category search input */
            searchInput.tagsinput({
                typeaheadjs: {
                    name: 'categories-search',
                    displayKey: 'text',
                    valueKey: 'text',
                    limit: 50,
                    source: blood.ttAdapter()
                },
                // Because typeahead not support suggestion as object.
                // So we should override typeahead selected handler then add input manually
                // See #43-note2 for hack code
                onTypeaheadSelected: function(item, input) {
                    searchInput.tagsinput('add', { language: item.language, text: '#' + item.text.toLowerCase().trim() });
                },
                tagClass: function(item) {
                    switch (item.language) {
                        case '@en_US':
                            return 'label label-primary';
                        case '@vi_VN':
                            return 'label label-danger label-important';
                    }
                },
                itemValue: 'text',
                itemText: 'text',
                allowDuplicates: false,
                trimValue: true
            });
            // Becase currently, we input model is object (JSON) not string to support coloring tags
            // So when type a string to input field, we should catch enter event than add tags manually
            // See #43-note2 for hack code            
            searchInput.on('beforeHandleItem', function(e, input) {
                // allow search by category which does not exist on typeahead
                $(input).val('');
                if (!_helper.containSpecialCharacter(e.item, '@:;,=\'"/\\<>%') && e.item.trim() != '') {
                    let cat = e.item.startsWith('#') ? e.item : '#' + e.item;
                    searchInput.tagsinput('add', { language: _helper.getLanguage(), text: cat });
                }
            });

            /* ----------> init boostrap tag input and typeahead for image categories input */
            categoriesInput.tagsinput({
                typeaheadjs: {
                    name: 'categories',
                    displayKey: 'text',
                    valueKey: 'text',
                    limit: 50,
                    source: blood.ttAdapter()
                },
                // Because typeahead not support suggestion as object.
                // So we should override typeahead selected handler then add input manually
                // See #43-note2 for hack code
                onTypeaheadSelected: function(item, input) {
                    categoriesInput.tagsinput('add', { language: item.language, text: '#' + item.text.toLowerCase().trim() });
                    dirtyChange = true;
                },
                tagClass: function(item) {
                    switch (item.language) {
                        case '@en_US':
                            return 'label label-primary';
                        case '@vi_VN':
                            return 'label label-danger label-important';
                    }
                },
                itemValue: 'text',
                itemText: 'text',
                allowDuplicates: false,
                trimValue: true
            });
            // Becase currently, we input model is object (JSON) not string to support coloring tags
            // So when type a string to input field, we should catch enter event than add tags manually
            // See #43-note2 for hack code
            categoriesInput.on('beforeHandleItem', function(e, input) {
                // add new category based on chosen language
                $(input).val('');
                if (!_helper.containSpecialCharacter(e.item, '@:;,=\'"/\\<>%') && e.item.trim() != '') {
                    let cat = e.item.startsWith('#') ? e.item : '#' + e.item;
                    categoriesInput.tagsinput('add', { language: _helper.getLanguage(), text: cat });
                    dirtyChange = true;
                }
            });
        });

        /* ----------> init boostrap tag input and typeahead for image captions input */
        captionsInput.tagsinput({
            // use css to color tags by language
            tagClass: function(item) {
                switch (item.language) {
                    case '@en_US':
                        return 'label label-primary';
                    case '@vi_VN':
                        return 'label label-danger label-important';
                }
            },
            itemValue: 'text',
            itemText: 'text',
            allowDuplicates: false,
            trimValue: true
        });
        captionsInput.on('beforeHandleItem', function(e, input) {
            $(input).val('');
            if (!_helper.containSpecialCharacter(e.item, '@:;,=\'"/\\<>%')) {
                captionsInput.tagsinput('add', { language: _helper.getLanguage(), text: e.item.toLowerCase().trim() });
                dirtyChange = true;
            }
        });

        /* ----------> load contributor, reviewer and status into select box */
        loadContributors();
        loadStatuses();
        if (loggedUser.hasRoles(['admin', 'manager', 'reviewer'])) {
            // only admin, manager and reviewer can see reviewer select box
            loadReviewer();
        } else {
            $('#reviewerSBoxContainer').remove();
        }
        // set dirty change when comment is changed
        $('#commentInput').change(function(e) {
            dirtyChange = true;
        });
    }

    /**
     * Load contributors into select box
     */
    function loadContributors() {
        var selectBox = $('#contributorSBox');
        if (loggedUser.hasRoles(['admin', 'manager', 'reviewer', 'contributor'])) {

            // load all contributor except
            $.ajax({
                async: true,
                crossDomain: true,
                url: _config.api_user_get + '?token=' + token,
                type: "GET"

            }).done(function(res) {
                if (!(res instanceof Array)) {
                    // Notify error message
                    return _helper.error();
                }

                // OK -> load contributor into select box
                res.sort(function(a, b) {
                    if (a.username < b.username) return -1;
                    if (a.username > b.username) return 1;
                    return 0;
                }).forEach(function(data) {
                    let user = new User(data);
                    let opt = $("<option></option>").attr("value", user.get('username')).text(user.get('fullname'));
                    if (user.get('active') == false) {
                        opt.css('background', '#FFF68F').text(opt.text() + " (inactive)");
                    }

                    selectBox.append(opt);
                });
                // set selected option and refresh select box
                selectBox.selectpicker('val', loggedUser.get('username')).selectpicker('refresh');

            }).fail(function(res) {
                return _helper.error(res.error);
            });


        } else {
            // load current logged user only
            selectBox.append($("<option></option>").attr("value", loggedUser.get('username')).text(loggedUser.get('fullname')));
            selectBox.selectpicker('val', loggedUser.get('username')).selectpicker('refresh');
        }

    }

    /**
     * Load reviewer into select box
     */
    function loadReviewer() {
        var selectBox = $('#reviewerSBox');

        if (loggedUser.hasRoles(['admin', 'manager'])) {
            $.ajax({
                async: true,
                crossDomain: true,
                url: _config.api_user_get + '?token=' + token,
                type: "GET"

            }).done(function(res) {
                if (!(res instanceof Array)) {
                    // Notify error message
                    return _helper.error();
                }

                // OK -> load contributor into select box
                res.sort(function(a, b) {
                    if (a.username < b.username) return -1;
                    if (a.username > b.username) return 1;
                    return 0;
                }).forEach(function(data) {
                    let user = new User(data);
                    if (user.hasRoles(['admin', 'manager', 'reviewer'])) {
                        let opt = $("<option></option>").attr("value", user.get('username')).text(user.get('fullname'));
                        selectBox.append(opt);
                    }
                });
                // set selected option and refresh select box
                selectBox.selectpicker('refresh');

            }).fail(function(res) {
                return _helper.error(res.error);
            });

        } else if (loggedUser.hasRoles(['reviewer'])) {
            // load current logged user only
            selectBox.append($("<option></option>").attr("value", loggedUser.get('username')).text(loggedUser.get('fullname')));
            selectBox.selectpicker('refresh');
        }

    }

    /**
     * Load status into selected box
     */
    function loadStatuses() {
        var selectBox = $('#statusSBox');
        selectBox.append($("<option></option>").attr("value", 'TODO').text('TODO'));
        selectBox.append($("<option></option>").attr("value", 'DONE').text('DONE'));
        selectBox.append($("<option></option>").attr("value", 'ACCEPTED').text('ACCEPTED'));
        selectBox.selectpicker('refresh');
    }

    /**
     * Load images into image container by filtering and searching condition
     * 
     * @param searchCondition search condition
     */
    function loadImages(searchCondition) {
        // reset slide show
        if (imageSliderShow.hasClass('slick-initialized')) {
            imageSliderShow.slick('unslick');
        }
        imageSliderShow.empty();

        $.ajax({
            async: true,
            crossDomain: true,
            url: _config.api_image_get + '?token=' + token + '&' + searchCondition,
            type: "GET"

        }).done(function(res) {
            if (!(res instanceof Array)) {
                // Notify error message
                return _helper.error();
            }

            // OK -> load image into image container
            $('#result').html(res.length);
            res.sort(function(a, b) {
                // sort images by updated date
                let d1 = new Date(a['updatedDate']);
                let d2 = new Date(b['updatedDate']);
                return d2.getTime() - d1.getTime();

            }).forEach(function(data) {
                let image = new Image(data);
                images.push(image);
                let imgTag = $("<img></img>").attr("src", _config.static_image_url + image.get('filename')).attr('id', image.get('_id'));
                imageSliderShow.append(imgTag);
            });

            // enable slide show by using slick
            imageSliderShow.slick({
                arrows: true,
                adaptiveHeight: true
            });

            var id = $("[data-slick-index='" + imageSliderShow.slick('slickCurrentSlide') + "']").attr('id');
            loadImageInfo(getImageById(id));

        }).fail(function(res) {
            return _helper.error(res.error);
        });
    }

    /**
     * Load displayed information into image info region
     * 
     * @param image image model data
     */
    function loadImageInfo(image) {
        var commentInput = $('#commentInput'),
            contributorInfo = $('#contributorInfo'),
            reviewerInfo = $('#reviewerInfo'),
            statusInfo = $('#statusInfo'),
            locationInfo = $('#locationInfo'),
            createdDateInfo = $('#createdDateInfo'),
            updatedDateInfo = $('#updatedDateInfo');

        // clear old view
        categoriesInput.tagsinput('removeAll');
        captionsInput.tagsinput('removeAll');
        contributorInfo.html('');
        statusInfo.html('');
        locationInfo.html('');
        createdDateInfo.html('');
        updatedDateInfo.html('');

        // clear data if image is null
        if (image == null) {
            return;
        }

        // load categories into input        
        var categories = image.getCategories();
        for (let lang in categories) {
            categories[lang].forEach(function(cat) {
                categoriesInput.tagsinput('add', { language: lang, text: '#' + cat });
            });
        }

        // load captions into input        
        var captions = image.getCaptions();
        for (let lang in captions) {
            captions[lang].forEach(function(cap) {
                captionsInput.tagsinput('add', { language: lang, text: cap });
            });
        }

        // load comment
        // Reviewers can comment on image is not their own
        commentInput.val(image.get('comments'));
        if (loggedUser.hasCommentPermission(image)) {
            commentInput.attr('disabled', false);
        } else {
            commentInput.attr('disabled', true);
        }

        /* ------ Load other information ------- */
        contributorInfo.html(image.get('contributor'));
        let reviewer = image.get('reviewer') ? image.get('reviewer') : 'None';
        reviewerInfo.html(reviewer)
        statusInfo.html(image.get('status'));

        // load location
        var location = image.get('longitude') ? image.get('longitude') + ' longitude x ' : '0 longitude x';
        location += image.get('latitude') ? image.get('latitude') + ' latitude x ' : '0 latitude x';
        location += image.get('altitude') ? image.get('altitude') + ' altitude' : '0 altitude';
        locationInfo.html(location)

        // load date
        createdDateInfo.html(image.get('createdDate'));
        updatedDateInfo.html(image.get('updatedDate'));

        // Hide "Move to DONE" button if logged user is not image owner
        if (loggedUser.hasDonePermission(image)) {
            $('#doneBtn').show();
            categoriesInput.attr('disabled', false);
            captionsInput.attr('disabled', false);
        } else {
            $('#doneBtn').hide();
            categoriesInput.attr('disabled', true);
            captionsInput.attr('disabled', true);
        }

        // Hide "Move to TODO" button if logged user is contributor and image owner
        if (loggedUser.hasTodoPermission(image)) {
            $('#moveToDoBtn').show();
        } else {
            $('#moveToDoBtn').hide();
        }

        // Hide Accept button if logged user is image owner
        if (loggedUser.hasAcceptPermission(image)) {
            $('#acceptBtn').show();
        } else {
            $('#acceptBtn').hide();
        }
    }

    /**
     * Create condition for searching and filter image data
     */
    function createSearchCondition() {
        let searchVals = searchInput.tagsinput('items');
        // support some hacks for searching images by special conditions.
        if (searchVals && searchVals.length > 0) {
            if (searchVals[0].text.startsWith('#_id_')) {
                // search by image hash
                return 'hash=' + searchVals[0].text.substring('#_id_'.length);

            } else if (searchVals[0].text.startsWith('#_notag_')) {
                // search images which has no categories;
                return 'filters={\"status\":{\"$in\":[\"ACCEPTED\", \"DONE\"]},\"categories\":{\"$exists\":\"true\"},\"$where\":\"this.categories.length==0\"}';
            }
        }

        var cdt = 'filters={';
        var cdtList = [];
        var vals = [];

        // filter by contributors
        var contributors = $('#contributorSBox option:selected');
        vals = [];
        contributors.each(function(idx, item) {
            vals.push("\"" + $(item).attr('value') + "\"");
        });
        if (vals.length > 0) {
            cdtList.push("\"contributor\":{\"$in\":[" + vals.join(',') + "]}");
        }

        // filter by reviewers
        var reviewers = $('#reviewerSBox option:selected');
        if (reviewers && reviewers.length > 0) {
            vals = [];
            reviewers.each(function(idx, item) {
                vals.push("\"" + $(item).attr('value') + "\"");
            });
            if (vals.length > 0) {
                cdtList.push("\"reviewer\":{\"$in\":[" + vals.join(',') + "]}");
            }
        }

        // filter by status
        var statuses = $('#statusSBox option:selected');
        vals = [];
        statuses.each(function(idx, item) {
            vals.push("\"" + $(item).attr('value') + "\"");
        });
        if (vals.length > 0) {
            cdtList.push("\"status\":{\"$in\":[" + vals.join(',') + "]}");
        }

        // search by categories
        // %23 -> #
        // categores is stored in format: "@en_US(#tree#animal)@vi_VN(#xyz)"
        // -> condition shoulde be categories.indexOf('#tree#') || categories.indexOf('#tree)')
        // we use two condition for each category to avoid case of a category is substring of other category
        // For example: with categories like "@en_US(#tree#tree pot)"
        // we see that "tree" is sub string of "tree pot" therefore when searching by "tree" 
        // result will return "tree pot" too
        vals = []
        searchInput.tagsinput('items').forEach(function(item) {
            vals.push("this.categories.indexOf('%23" + item.text.slice(1) + "%23') != -1");
            vals.push("this.categories.indexOf('%23" + item.text.slice(1) + ")') != -1");
        });
        if (vals.length > 0) {
            cdtList.push("\"categories\": {\"$exists\": \"true\"},\"$where\":\"" + vals.join("||") + "\"");
        }

        return cdt + cdtList.join(',') + '}';
    }

    // ============================ Handle event here ============================ //

    /**
     * Handle search button
     */
    function onSearchHandler(e) {
        var cdt = createSearchCondition();
        loadImages(cdt);
    }

    /**
     * Handle Move to TODO button
     */
    function onMoveToDoHandler(e, callback) {
        overlayWaiting.show();
        try {
            var imageIdx = imageSliderShow.slick('slickCurrentSlide'),
                id = $("[data-slick-index='" + imageIdx + "']").attr('id');
            var image = getImageById(id);

            image.set('status', 'TODO');
            if (loggedUser.get('username') != image.get('contributor')) {
                image.set('reviewer', loggedUser.get('username'));
            }
            updateImage(image, function() {
                // move to next image and notify success
                imageSliderShow.slick('slickNext');
                overlayWaiting.hide();
                _helper.success('Updating is successful');

                callback && callback();
            });

        } catch (err) {
            overlayWaiting.hide();
            _helper.error(err.toString());
        }
    }

    /**
     * Handle update button 
     */
    function onMoveToDoneHandler(e, callback) {
        overlayWaiting.show();
        try {
            var imageIdx = imageSliderShow.slick('slickCurrentSlide'),
                id = $("[data-slick-index='" + imageIdx + "']").attr('id');
            var image = getImageById(id);

            // only image owner can click 'Move to DONE' button and change status to DONE
            image.set('status', 'DONE');
            updateImage(image, function() {
                // move to next image and notify success
                imageSliderShow.slick('slickNext');
                overlayWaiting.hide();
                _helper.success('Updating is successful');

                callback && callback();
            });

        } catch (err) {
            overlayWaiting.hide();
            _helper.error(err.toString());
        }
    }

    /**
     * Handle accept button
     */
    function onAcceptHandler(e) {
        overlayWaiting.show();
        try {
            var imageIdx = imageSliderShow.slick('slickCurrentSlide'),
                id = $("[data-slick-index='" + imageIdx + "']").attr('id');
            var image = getImageById(id);

            // Change status to ACCEPTED
            image.set('status', 'ACCEPTED');
            image.set('reviewer', loggedUser.get('username'));
            updateImage(image, function() {
                // move to next image and notify success
                imageSliderShow.slick('slickNext');
                overlayWaiting.hide();
                _helper.success('Updating is successful');
            });

        } catch (err) {
            overlayWaiting.hide();
            _helper.error(err.toString());
        }
    }

    /**
     * Open confirmation dialog when click delete button
     */
    function onDeleteConfirmationOpen(e) {
        $('#deleteConfirmDiag').modal('show');
    }

    /**
     * Handle delete button
     */
    function onDeleteHandler(e) {
        try {
            var activeSlide = imageSliderShow.find('img.slick-active');
            var image = getImageById(activeSlide.attr('id'));

            $.ajax({
                async: true,
                crossDomain: true,
                url: _config.api_image_delete + '/' + image.get('hash') + '?token=' + token,
                type: "DELETE"

            }).done(function(res) {

                // open connfirmation dialog

                // remove image from slide container and from image array
                imageSliderShow.slick('slickRemove', activeSlide.attr("data-slick-index"));
                removeImageById(image.get('_id'));

                // re-order data-slick-index
                let i = 0;
                imageSliderShow.find("img.slick-slide").not('.slick-cloned').each(function() {
                    $(this).attr("data-slick-index", i);
                    i++;
                });

            }).fail(function(res) {
                return _helper.error(res.error);
            });

        } catch (err) {
            _helper.error(err.toString());
        }
    }

    /**
     * On image change
     */
    function onAfterImageChange(e, slide, currentIdx) {
        // reload image info
        var activeSlide = $('#imageSliderShow img.slick-active');
        var id = activeSlide.attr("id");
        loadImageInfo(getImageById(id));
        dirtyChange = false;

        $('#currentSlide').html(activeSlide.attr('data-slick-index'));
    }

    /**_helpe.getLanguage();
     * On change language
     */
    function onChangeLanguage(e) {
        $('#languageBtn').html(_helper.toggleLanguage());
    }

    /**
     * On upload images
     */
    function onUploadImages(e) {
        // Display uploading dialog
        var dialog = $('#uploadModalDialog');
        var progressBar = dialog.find('#uploadingProgress');
        var terminal = dialog.find('#terninal');

        // get available images
        uploadedFiles = [];
        for (let i = 0; i < this.files.length; i++) {
            let file = this.files[i];
            if (file.type.indexOf('image') !== -1) {
                uploadedFiles.push(file);
            }
        }

        if (uploadedFiles.length == 0) {
            return;
        }

        // set progress bar max value
        progressBar.attr('aria-valuemax', uploadedFiles.length);
        progressBar.attr('aria-valuemin', 0);
        progressBar.attr('aria-valuenow', 0);
        progressBar.css('width', "0%");
        dialog.modal('show');
    }

    $('#uploadModalDialog').on('shown.bs.modal', function(e) {
        // uploading image
        cancelUpload = false;
        uploadNextImage(0);
    });

    $('#uploadModalDialog').on('hide.bs.modal', function(e) {
        // cancel upload
        cancelUpload = true;
    });

    // recursive uploading image
    function uploadNextImage(idx) {
        var dialog = $('#uploadModalDialog');
        var progressBar = dialog.find('#uploadingProgress');
        var numberLabel = dialog.find('#uploadedNumber');
        var terminal = dialog.find('#terminal');

        let image = new Image({});
        let file = uploadedFiles[idx];
        var now = _helper.getDateTimeString(new Date());
        let hash = CryptoJS.MD5(file.name + now).toString();

        image.set('name', file.name);
        image.set('hash', hash);
        image.set('filename', hash + _helper.splitFileNameAndExtension(file.name)[1]);
        image.set('contributor', loggedUser.get('username'));
        image.set('status', 'TODO');
        image.set('createdDate', now);
        image.set('updatedDate', now);

        // upload image file first
        uploadImage(image, file, function(err, res) {
            if (err) {
                // add log to terminal
                terminal.html('[FAIL] uploading ' + image.get('name') + '\n' + res.statusText + '\n' + terminal.html());

                idx++;
                if (cancelUpload == false && idx < uploadedFiles.length) {
                    uploadNextImage(idx);
                }
                return;
            }

            // DONE -> insert image data
            insertImageData(image, function(err, res) {
                if (err) {
                    // add log to terminal
                    terminal.html('[FAIL] insert data repective to ' + image.get('name') + '\n' + res.statusText + '\n' + terminal.html());

                    idx++;
                    if (idx < uploadedFiles.length) {
                        uploadNextImage(idx);
                    }
                    return;
                }

                // update progress dialog and close if all images have been uploaded
                let pValue = parseInt(progressBar.attr('aria-valuenow')) + 1;
                let pPercentage = (pValue / uploadedFiles.length) * 100;

                progressBar.attr('aria-valuenow', pValue);
                progressBar.css('width', pPercentage + "%");
                progressBar.html(parseInt(pPercentage) + "%");
                numberLabel.html(parseInt(numberLabel.html()) + 1);

                // add log to terminal
                terminal.html('[DONE] uploading ' + image.get('name') + '\n' + terminal.html());

                idx++;
                if (cancelUpload == false && idx < uploadedFiles.length) {
                    uploadNextImage(idx);
                }
            });
        });
    }

    // ================================= Method ================================== //

    /**
     * Get image by id
     */
    function getImageById(id) {
        for (let i = 0; i < images.length; i++) {
            if (images[i].get('_id') === id) {
                return images[i];
            }
        }
        return null;
    }

    // remove image from list
    function removeImageById(id) {
        for (let i = 0; i < images.length; i++) {
            if (images[i].get('_id') === id) {
                images.splice(i, 1);
            }
        }
    }

    /**
     * Get sample categories from server
     *
     * @param callback callback function
     */
    function getSampleCategories(callback) {
        var promises = [];
        // currently, load Vietnamese categories only
        // ['vi_VN', 'en_US'].forEach(function(lang) {
        ['vi_VN'].forEach(function(lang) {
            promises.push(new Promise(function(resolve, reject) {
                let url = _config.api_resource_config + 'categories-' + lang + '.spl?token=' + token;
                $.get(url).done(function(res) {
                    resolve({ language: '@' + lang, data: res });
                }).fail(function(res) {
                    _helper.error(res.error);
                });
            }));
        });

        Promise.all(promises).then(function(results) {
            callback(results);
        });
    }

    /**
     * Update image
     *
     * @param image image data model
     * @param callback callback function
     */
    function updateImage(image, callback) {
        var map = {};

        // set date
        image.set('updatedDate', _helper.getDateTimeString(new Date()));

        if (loggedUser.hasDonePermission(image)) {
            // update categories
            // categories string should have format: "@en_US(#tree#animal)@vi_VN(#xyz)"
            var categories = '';
            map = {}
            categoriesInput.tagsinput('items').forEach(function(item) {
                if (map[item.language] == undefined) map[item.language] = [];
                map[item.language].push(item.text.toLowerCase().trim());
            });
            for (let key in map) {
                categories += key + '(' + map[key].join('') + ')';
            }
            image.set('categories', categories);

            // update captions
            // captions string should have format: "@en_US(tree.animal)@vi_VN(xyz)"
            map = {}
            var captions = '';
            captionsInput.tagsinput('items').forEach(function(item) {
                if (map[item.language] == undefined) map[item.language] = [];
                map[item.language].push(item.text.toLowerCase().trim());
            });
            for (let key in map) {
                captions += key + '(' + map[key].join('.') + ')';
            }
            image.set('captions', captions);
        }

        // update comment
        if (loggedUser.hasCommentPermission(image)) {
            image.set('comments', $('#commentInput').val());
        }

        $.ajax({
            async: true,
            crossDomain: true,
            url: _config.api_image_update + '/' + image.get('hash') + '?token=' + token,
            type: "PUT",
            contentType: "application/x-www-form-urlencoded",
            dataType: 'json',
            data: {
                data: JSON.stringify(image.data)
            }

        }).done(function(res) {
            dirtyChange = false;
            callback(res);

        }).fail(function(res) {
            return _helper.error(res.error);
        });
    }

    /**
     * Upload image file to server
     * 
     * @param image: image model data
     * @param image file
     * @param callback callback function
     */
    function uploadImage(image, file, callback) {
        // upload image
        var formData = new FormData();
        formData.append('file', file);
        formData.append('filename', image.get('filename'));

        $.ajax({
            async: true,
            crossDomain: true,
            url: _config.api_image_upload + '?token=' + token,
            type: "POST",
            contentType: false,
            processData: false,
            mimeType: "multipart/form-data",
            data: formData,
            timeout: 30000 // 30s

        }).done(function(res) {
            callback(null, res);

        }).fail(function(res) {
            callback('error', res);
        });
    }

    /**
     * Insert image data which store information like name, categories, captions, status, createdData ...
     *
     * @param image image data model
     * @param callback callback function
     */
    function insertImageData(image, callback) {
        $.ajax({
            async: true,
            crossDomain: true,
            url: _config.api_image_create + '?token=' + token,
            type: "POST",
            contentType: "application/x-www-form-urlencoded",
            dataType: 'json',
            data: {
                data: JSON.stringify(image.data)
            },
            timeout: 30000 // 30s

        }).done(function(res) {
            callback(null, res);

        }).fail(function(res) {
            callback('error', res);
        });
    }

    function saveJob(callback) {
        try {
            var imageIdx = imageSliderShow.slick('slickCurrentSlide'),
                id = $("[data-slick-index='" + imageIdx + "']").attr('id');
            var image = getImageById(id);

            updateImage(image, function() {
                callback && callback()
            });

        } catch (err) {
            _helper.error(err.toString());
        }
    }

    function handleShortKey() {
        let isCtrlPress = false;
        let isShiftPress = false;
        let keyCodes = [];

        $('body').keydown(function(e) {
            isCtrlPress = e.ctrlKey;
            isShiftPress = e.shiftKey;

            if (e.keyCode && keyCodes.indexOf(e.keyCode) === -1) {
                keyCodes.push(e.keyCode);
            }
        });

        $('body').keyup(function(e) {

            if (isCtrlPress && keyCodes.length === 2 && keyCodes[1] === 188) /*CRTL + <*/ {
                // save and back to previous image
                saveJob(function() {
                    imageSliderShow.slick('slickPrev');
                    categoriesInput.tagsinput('focus');
                });

            } else if (isCtrlPress && keyCodes.length === 2 && keyCodes[1] === 190) /*CRTL + >*/ {
                // save and move to next image
                // save and back to previous image
                saveJob(function() {
                    imageSliderShow.slick('slickNext');
                    categoriesInput.tagsinput('focus');
                });

            } else if (isCtrlPress && keyCodes.length === 2 && keyCodes[1] === 38) /*CRTL+ArrowUp*/ {
                categoriesInput.tagsinput('focus');

            } else if (isCtrlPress && isShiftPress && keyCodes.length === 3 && keyCodes[2] === 13) /*CTRL+SHIFT+ENTER*/ {
                // move to DONE
                onMoveToDoneHandler(e);
                categoriesInput.tagsinput('focus');

            } else if (isCtrlPress && isShiftPress && keyCodes.length === 3 && keyCodes[2] === 83) /*CTRL+SHIFT+S*/ {
                // move to TODO
                onMoveToDoHandler(e);
                categoriesInput.tagsinput('focus');

            } else if (isCtrlPress && isShiftPress && keyCodes.length === 3 && keyCodes[2] === 65) /*CTRL+SHIFT+A*/ {
                // move to ACCEPTED
                onAcceptHandler(e);
                categoriesInput.tagsinput('focus');

            } else if (isCtrlPress && isShiftPress && keyCodes.length === 2) {
                // change language when press CTRL + SHIFT
                $('#languageBtn').html(_helper.toggleLanguage());
            }

            isCtrlPress = false;
            isShiftPress = false;
            keyCodes = [];
        });
    }

});
