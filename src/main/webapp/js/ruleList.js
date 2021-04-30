var if_rules=[];
var existing_rules=[];
var classes=[];
var propertis=[];
var then_rules=[];
var count=1;
var api = "webapi/Rule";
//Ajax function for fetch rules from server
$.get(api,function(rule,status){

    if(status === "success"){
        var str="";
        var l = rule.length;
        for(var i=0;i<l;i++){
            str+=`<div class="form-group"><label class="form-check-label"><input type="checkbox" class="form-check-input existing" value="${i}">${rule[i]}</label></div>`;
        }
        $('#existingrules').html(str);
        $('.loader').hide();

    }
});
// toggle to create rule page
function createRule(){
    $('#existSection').hide();
    $('.loader').show();
    var api = "webapi/Rule/getNode";
    $.get(api,function(create,status) {
        if (status === "success") {
            for (var i = 0; i<create[0].length; i++)
                classes.push(create[0][i]);
            for (var j = 0; j< create[1].length; j++)
                propertis.push(create[1][j]);
            $('.loader').hide();
        }
        else
            alert("fail");
    });

    $('#ifSection').show();
    //addRule();
}
//function for adding new row in if rule
function addRule(){
    var str="";
    str+=`<div class="row mt-1"> <div class="col-sm-3 col-lg-3 text-center"><select class="form-control subject" onchange="checkValues(this.value,`+count+`)">`;
    for(var i=0;i<classes.length;i++){
        str+=`<option value="`+classes[i]+`">`+classes[i]+`</option>`;
    }
    str+=`<option value="other">Other</option></select><input type="text" id="s`+count+`" placeholder="Enter Subject" class="form-control hide"></div><div class="col-sm-3 col-lg-3 text-center"><select class="form-control predicate">`;
    for(var i=0;i<propertis.length;i++){
        str+=`<option value="`+propertis[i]+`">`+propertis[i]+`</option>`;
    }
    str+=`</select></div><div class=" col-sm-3 col-lg-3 text-center"><select class="form-control object" onchange="checkValueo(this.value,`+count+`)">`;

    for(var i=0;i<classes.length;i++){
        str+=`<option value="`+classes[i]+`">`+classes[i]+`</option>`;
    }
    str+=`<option value="other">Other</option></select><input type="text" id="o`+count+`" placeholder="Enter Object" class="form-control hide"></div><div class="col-sm-3 col-lg-3 text-center"><select class="form-control and"><option value="AND">AND</option><option value="OR">OR</option> </select></div></div>`;
    $('#ifrules').append(str);
    count++;
}
// function for done if rule and get then rule div....
function done(){
    $('#thenSection').show();
    if_rules=[];
    var subject=$('.subject');
    var predicate=$('.predicate');
    var object=$('.object');
    var and=$('.and');
    var s=[];
    var o=[];
    //storing if rules value into array of string;
    for(var i=0;i<subject.length;i++){
        if(subject[i].value==="other") {
            if_rules.push($("#s" + (i + 1)).val());
            s.push($("#s" + (i + 1)).val());
        }
        else {
            if_rules.push(subject[i].value);
            s.push(subject[i].value);
        }

        if_rules.push(predicate[i].value);

        if(object[i].value==="other") {
            if_rules.push($("#o" + (i + 1)).val());
            o.push($("#o" + (i + 1)).val());
        }
        else {
            if_rules.push(object[i].value);
            o.push(object[i].value);
        }

        if(i<subject.length-1)
            if_rules.push(and[i].value);
    }
    var union= [...new Set([...s, ...o])];
    var str="";
    str+=`<div class="row"><div class="col-sm-4 col-lg-4 text-center"><select class="form-control" id="then_subject">`;
    for(var i=0;i<union.length;i++){
        str+=`<option value="`+union[i]+`">`+union[i]+`</option>`;
    }
    str+=`</select></div><div class="col-sm-4 col-lg-4 text-center"><input type="text" id="then_predicate" placeholder="Enter Value" class="form-control"></div> <div class=" col-sm-4 col-lg-4 text-center"><select class="form-control" id="then_object">`;
    for(var i=0;i<union.length;i++){
        str+=`<option value="`+union[i]+`">`+union[i]+`</option>`;
    }
    str+=`</select></div> </div>`;
    $('#thenrules').html(str);

    console.log(if_rules);

}
//function for submit exitsiting rule and send to server
function submitE(){
    var existing=$('.existing');
    $('.loader').show();
    for(var i=0;i<existing.length;i++){
        if(existing[i].checked==true)
            existing_rules.push(existing[i].value);
    }
    console.log(JSON.stringify(existing_rules));
    var data=JSON.stringify({rules:existing_rules});
    $.ajax({
        url: 'webapi/chaining/forwardChaining',
        type: "POST",
        data: data,
        //enctype:'multipart/form-data',
        processData: false,
        contentType: 'application/json',
        cache: false,
        async: true,
        timeout: 1200000,
        success: function (rule,status) {
            $('.loader').hide();
            window.location.assign("/SWRE_war_exploded/workon.html");
        },
        error: function () {
            alert("timeout");
            $('.loader').hide();
        }

    });
}
//submit new rule and send to server
function submit(){
    then_rules=[];
    $('.loader').show();
    then_rules.push($('#then_subject').val());
    then_rules.push($('#then_predicate').val());
    then_rules.push($('#then_object').val());
    console.log(then_rules);
    var data=JSON.stringify({antecedent:if_rules,consequent:then_rules});
    $.ajax({
        url: 'webapi/Rule/newRule',
        type: "POST",
        data: data,
        processData: false,
        contentType: 'application/json',
        cache: false,
        async: true,
        timeout: 6000,
        success: function (rule,status) {
            $('.loader').hide();
            location.reload(true);
        }
    });
}
//function for show input field for other option
function checkValues(val,id) {
    if(val==="other"){
        $("#s"+id).show();
    }
    else
        $("#s"+id).hide();
}
function checkValueo(val,id) {
    if(val==="other"){
        $("#o"+id).show();
    }
    else
        $("#o"+id).hide();
}

