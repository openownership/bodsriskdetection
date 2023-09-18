var $ = require('jquery-browserify')

exports.get = function (url, onSuccess) {
    $.ajax({
        type: "GET",
        url: url,
        success: function (response) {
            onSuccess(response);
        },
        error: function (request) {
            console.log(`Error loading ${url}: ` + request.responseText);
        }
    });
}

exports.post = function (url, data, onSuccess) {
    $.ajax({
        type: "POST",
        url: url,
        data: JSON.stringify(data),
        contentType: "application/json; charset=utf-8",
        success: function (response) {
            onSuccess(response);
        },
        error: function (request) {
            console.log(`Error loading ${url}: ` + request.responseText);
        }
    });
}
