var websocket = null;
var url = "ws://localhost:8989/mancala";
var gameId;



$('#login').click(function(){
    var username = $('#username').val();
    if(username == null || username== ""){
        alert("User Name can not be empty!");
        return;
    }

    websocket = new WebSocket(url + "/" +username);
    websocket.onopen = function(event){
        $('#show').append("<p>System Message：Connect Success!</p>");

        $('#startGame').removeAttr('disabled');
        $('#login').attr("disabled","disabled");
        $('#username').attr("disabled","disabled");
    }

    websocket.onmessage = function(event){
        let message = null;
        let rs = JSON.parse(event.data);
        if(rs.status =="START"){
                $('#sendMessage').removeAttr("disabled");
                $('#startGame').attr("disabled","disabled");
                gameId = rs.data.gameId;
                drawBoard(rs);
                addPitListener();
                message = "<p><font color='red'>"+rs.message+"</font></p>";
        }else if(rs.status=="END"){
                drawBoard(rs);
                 let winUsername = rs.message;
                 $('#player_turn').html("<p><font color='red'>Congratulation! the winner is "+winUsername+"</font></p>")
                 $('#show').append("<p><font color='red'>System Info：Congratulation! the winner is "+winUsername+"</font></p>");
                 message = "<p><font color='red'>System Message：Game is Over!</font></p>";
        }else if(rs.status == "SOW"){
                drawBoard(rs);
                message =  "<p>System Message：" + rs.message + "</p>";
        }else{
                message = "<p>System Message：" + rs.message + "</p>";
        }
        $('#show').append(message);

    }

    //
    websocket.onerror = function(event){
        alert("Connection Error!");
    }
});



//$('#sendMessage').click(function(){
//    var message = $('#message').val();
//    $('#message').val('');
//    var username = $('#username').val();
//
//    websocket.send(jsonMessage("CHAT",username,pitIdx,gameId));
//});


$('#startGame').click(function(){
    var username = $('#username').val();
    websocket.send(jsonMessage("START",username,null,null));
    $('#sendMessage').removeAttr("disabled");
    $('#startGame').attr("disabled","disabled");
     $('#username').attr("disabled","disabled");
});


var jsonMessage = function(playerAction,username,pitIdx,gameId){
    result.playerAction = playerAction;
    result.userName = username;
    result.pitIdx = pitIdx;
    result.gameId = gameId;
    var resultMessage = JSON.stringify(result);
    return resultMessage;
}

var drawBoard = function(response){
    var mancalaGame = response.data;
    var username = $('#username').val();
    var curSegment = mancalaGame.inactiveBoardSegmentVO;
    var nextSegment = mancalaGame.activeBoardSegmentVO;
    if(mancalaGame.activeBoardSegmentVO.player == username){
        curSegment = mancalaGame.activeBoardSegmentVO;
        nextSegment = mancalaGame.inactiveBoardSegmentVO;
    }
    $('#player_turn').html("Player " + mancalaGame.activeBoardSegmentVO.player+"'s Turn")
    drawOpponentSegment(nextSegment);
    drawSegment(curSegment);
}
//消息体
var result = {
    playerAction:null,
    userName:null,
    gameId:null,
    pitIdx:null

}


var drawOpponentSegment = function(segmentData){
     $('#opponent-house').html(segmentData.house);
     $('#opponent-pit-0').html(segmentData.pits[0]);
     $('#opponent-pit-1').html(segmentData.pits[1]);
     $('#opponent-pit-2').html(segmentData.pits[2]);
     $('#opponent-pit-3').html(segmentData.pits[3]);
     $('#opponent-pit-4').html(segmentData.pits[4]);
     $('#opponent-pit-5').html(segmentData.pits[5]);

}

var drawSegment = function(segmentData){
    $('#pit-0').html(segmentData.pits[0]);
    $('#pit-1').html(segmentData.pits[1]);
    $('#pit-2').html(segmentData.pits[2]);
    $('#pit-3').html(segmentData.pits[3]);
    $('#pit-4').html(segmentData.pits[4]);
    $('#pit-5').html(segmentData.pits[5]);
    $('#house').html(segmentData.house);

}

var addPitListener = function(){
    $('#pit-0').dblclick(function(){
        var username = $('#username').val();
        websocket.send(jsonMessage("SOW",username,0,gameId));
    });
    $('#pit-1').dblclick(function(){
        var username = $('#username').val();
        websocket.send(jsonMessage("SOW",username,1,gameId));
    });
    $('#pit-2').dblclick(function(){
        var username = $('#username').val();
        websocket.send(jsonMessage("SOW",username,2,gameId));
    });
    $('#pit-3').dblclick(function(){
        var username = $('#username').val();
         websocket.send(jsonMessage("SOW",username,3,gameId));
     });
    $('#pit-4').dblclick(function(){
        var username = $('#username').val();
        websocket.send(jsonMessage("SOW",username,4,gameId));
    });
    $('#pit-5').dblclick(function(){
        var username = $('#username').val();
        websocket.send(jsonMessage("SOW",username,5,gameId));
    });
}

