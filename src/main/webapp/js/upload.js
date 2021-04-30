function uploadfile() {
    var form = $('#ontologyfile')[0];
    var data = new FormData(form);
    $.ajax({
        type: "POST",
        url: 'webapi/DataLoader/NewOntology',
        enctype: 'multipart/form-data',
        data: data,
        processData: false,
        contentType:false,
        cache: false,
        async: true,
        timeout: 60000,
        success: function (status) {
            sessionStorage.setItem("content",document.getElementById("ontologyfile").elements.namedItem("dbname").value);
            window.location.assign("/SWRE_war_exploded/workon.html");
        }
    });
}

function triggerUniversityOntology() {

    api = "webapi/DataLoader/University";
    $.get(api,function(status) {
        sessionStorage.setItem("content","University");
        window.location.assign("/SWRE_war_exploded/workon.html");
    });
}
