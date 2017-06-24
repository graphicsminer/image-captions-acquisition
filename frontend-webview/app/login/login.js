$(document).ready(function() {

    $('#form-signin').submit(function(evt) {
        evt.preventDefault();
        $('#alert').hide();

        var username = $('#username').val().trim().toLocaleLowerCase(),
            // pwd = CryptoJS.SHA1($('#password').val().trim()).toString();
            pwd = $('#password').val().trim();

        // do login
        var settings = {
            async: true,
            crossDomain: true,
            url: _config.api_login,
            type: "POST",
            contentType: "application/x-www-form-urlencoded",
            dataType: 'json',
            data: {
                "username": username,
                "password": pwd
            }
        }
        $.ajax(settings).done(function(res) {
            if (res.token && res.token.trim() != "") {
                _helper.setToken(res.token);
                _helper.setLoggedUser(res.user._id);

                // redirect to editor page
                window.location.href = "../editor/editor.html";
            }
        }).fail(function(res){
            $('#alert').html("Login Fail. Please check username and password or Internet connection").show();
        });
    });

});
