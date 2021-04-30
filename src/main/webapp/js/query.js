var classes=[];
var propertis=[];
var query=[];
var count=1;
var value=0;
// var result=[["mohit","studies","ds603"],["parth","teaches","ds603"]];
var api = "webapi/Rule/getNode";
$.get(api,function(create,status) {
    if (status === "success") {
        for (var i = 0; i<create[0].length; i++)
            classes.push(create[0][i]);
        for (var j = 0; j< create[1].length; j++)
            propertis.push(create[1][j]);
        newNode();
    }
    else
        alert("fail");
});
function togle() {
    count=0;
    value=!value;
    if(value){
        $("#result").hide();
        $("#backward").show();
        $("#plainquery").hide();
        row();
    }
    else{
        $("#result").hide();
        $("#backward").hide();
        $("#plainquery").show();
        newNode();
    }
}
//add single row in backward chaining
function row(){
    var str="";
    str+=`<div class="row mt-1"> <div class="col-lg-4 text-center"><select class="form-control subject1" style="border-radius: 50px" onchange="checkValues(this.value,6)">`;
    for(var i=0;i<classes.length;i++){
        str+=`<option value="`+classes[i]+`">`+classes[i]+`</option>`;
    }
    str+=`<option value="other">Other</option></select><input type="text" id="s6" placeholder="Enter Subject" class="form-control hide" style="border-radius: 50px"></div><div class="col-lg-4 text-center"><select class="form-control predicate1" style="border-radius: 50px"onchange=" checkValuep(this.value,6)">`;
    for(var i=0;i<propertis.length;i++){
        str+=`<option value="`+propertis[i]+`">`+propertis[i]+`</option>`;
    }
    str+=`<option value="other">Other</option></select><input type="text" id="p6" placeholder="Enter Subject" class="form-control hide" style="border-radius: 50px"></div><div class="col-lg-4 text-center"><select class="form-control object1" style="border-radius: 50px" onchange="checkValueo(this.value,6)">`;

    for(var i=0;i<classes.length;i++){
        str+=`<option value="`+classes[i]+`">`+classes[i]+`</option>`;
    }
    str+=`<option value="other">Other</option></select><input type="text" id="o6" placeholder="Enter Object" class="form-control hide" style="border-radius: 50px"></div></div>`;
    $('#backwordnode').html(str);

}
//add new row in query
function newNode() {
    var str="";
    str+=`<div class="row mt-1"> <div class="col-lg-3 text-center"><select class="form-control subject" style="border-radius: 50px" onchange="checkValues(this.value,`+count+`)">`;
    for(var i=0;i<classes.length;i++){
        str+=`<option value="`+classes[i]+`">`+classes[i]+`</option>`;
    }
    str+=`<option value="other">Other</option></select><input type="text" id="s`+count+`" placeholder="Enter Subject" class="form-control hide" style="border-radius: 50px"></div><div class="col-lg-3 text-center"><select class="form-control predicate" style="border-radius: 50px">`;
    for(var i=0;i<propertis.length;i++){
        str+=`<option value="`+propertis[i]+`">`+propertis[i]+`</option>`;
    }
    str+=`</select></div><div class="col-lg-3 text-center"><select class="form-control object" style="border-radius: 50px" onchange="checkValueo(this.value,`+count+`)">`;

    for(var i=0;i<classes.length;i++){
        str+=`<option value="`+classes[i]+`">`+classes[i]+`</option>`;
    }
    str+=`<option value="other">Other</option></select><input type="text" id="o`+count+`" placeholder="Enter Object" class="form-control hide" style="border-radius: 50px"></div><div class="col-lg-3 text-center"><select class="form-control and" style="border-radius: 50px"><option value="AND">AND</option><option value="OR">OR</option></select></div></div>`;
    if(count===0)
        $('#node').html(str);
    else
        $('#node').append(str);
    count++;
    $('.loader').hide();
}


function go(){
    query=[];
    var subject=$('.subject');
    var predicate=$('.predicate');
    var object=$('.object');
    var and=$('.and');

    //storing query value into array of string;
    for(var i=0;i<subject.length;i++){
        if(subject[i].value==="other") {
            query.push($("#s" + (i + 1)).val());
        }
        else {
            query.push(subject[i].value);
        }

        query.push(predicate[i].value);

        if(object[i].value==="other") {
            query.push($("#o" + (i + 1)).val());
        }
        else {
            query.push(object[i].value);
        }

        if(i<subject.length-1)
            query.push(and[i].value);
    }
    console.log(query);
    var data=JSON.stringify({rules:query});
    $.ajax({
        url: 'webapi/Rule/getQuery',
        type: "POST",
        data: data,
        processData: false,
        contentType: 'application/json',
        cache: false,
        async: true,
        timeout: 60000,
        success: function (result,status) {
            showResult(result);
        }

    });
}
function backwardgo(){
    query=[];
    var subject=$('.subject1');
    var predicate=$('.predicate1');
    var object=$('.object1');

    //storing query value into array of string;
    for(var i=0;i<subject.length;i++){
        if(subject[i].value==="other") {
            query.push($("#s6").val());
        }
        else {
            query.push(subject[i].value);
        }

        if(predicate[i].value==="other") {
            query.push($("#p6").val());
        }
        else {
            query.push(predicate[i].value);
        }

        if(object[i].value==="other") {
            query.push($("#o6").val());
        }
        else {
            query.push(object[i].value);
        }

    }
    console.log(query);
    var data=JSON.stringify({rules:query});
    $.ajax({
        url: 'webapi/chaining/backwardChaining',
        type: "POST",
        data: data,
        processData: false,
        contentType: 'application/json',
        cache: false,
        async: true,
        timeout: 60000,
        success: function (result,status) {
            alert(result);

        }

    });
}
function showResult(result){
    $("#result").show();
    var str="<table>";
    for(var i=0;i<result.length;i++){
        str+=`<tr>`;
        for(var j=0;j<result[i].length;j++){
            str+=`<td>`+result[i][j]+`</td>`;
        }
        str+=`</tr>`;
    }
    str+=`</table>`
    $("#showResult").html(str);

}
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
function checkValuep(val,id) {
    if(val==="other"){
        $("#p"+id).show();
    }
    else
        $("#p"+id).hide();
}