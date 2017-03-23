var express = require('express')
var bodyParser = require('body-parser')
var app = express()
var sqlite3 = require('sqlite3')
var db = new sqlite3.Database('chatdb.sqlite');
var auth = require('basic-auth');


function guid() {
  function s4() {
    return Math.floor((1 + Math.random()) * 0x10000)
      .toString(16)
      .substring(1);
  }
  return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
    s4() + '-' + s4() + s4() + s4();
}

authenticate = function (req, res, done) {
    // http basic auth for requests, passwd is md5 of pass
    var credentials = auth(req);

    if (credentials) {
        var query = "select * from user where name = '" + credentials.name + "' and password = '" + credentials.pass + "'";
        db.all(query, function (err, rows) {
            var userAndPwdCorrect = rows.length > 0;
            if (userAndPwdCorrect) {
                var userGUID = rows[0].guid;
                req.userGUID = userGUID;
            }
            done();
        });
    }
    else {
            res.status(401);
            res.send('Unauthorized');
    }
}

getChannels = function (req, res) {
    authenticate(req, res, function () {
        var query = "select * from channel";
        var result = new Array();
        db.each(query, function (err, row) {
            result.push({ "guid": row.guid, "name": row.name });
        }, function () {
            res.send(JSON.stringify(result));
        });
    })
}

getChannelMessages = function (req, res) {
    authenticate(req, res, function () {
        var n = req.url.lastIndexOf('/');
        var channelGUID = req.url.substring(n + 1);
        var query = "select * from message where channel_guid = '" + channelGUID + "'";
        var result = new Array();
        db.each(query, function (err, row) {
            result.push({ "guid": row.guid, "channel_guid": row.channel_guid, "user_guid": row.user_guid, "content": row.content });
        }, function () {
            res.send(JSON.stringify(result));
        });
    });
}

publishMessage = function (req, res) {
    authenticate(req, res, function () {
        // insert new message
        var n = req.url.lastIndexOf('/');
        var channelGUID = req.url.substring(n + 1);
        var userGUID = req.userGUID;
        var content = req.body.content;
        db.run("INSERT into message(guid,channel_guid,user_guid, content) VALUES ('" + guid() + "','" + channelGUID + "','" + userGUID +  "','" + content +"')");
        // return back all messages for channel in response
        var query = "select * from message where channel_guid = '" + channelGUID + "'";
        var result = new Array();
        db.each(query, function (err, row) {
            result.push({ "guid": row.guid, "channel_guid": row.channel_guid, "user_guid": row.user_guid, "content": row.content });
        }, function () {
            res.send(JSON.stringify(result));
        });
    });
}

app.use(bodyParser.json())
app.use('/images', express.static('images'))

// routes
app.get('/chatserver/login', getChannels);
app.get('/chatserver/channels', getChannels);
app.get('/chatserver/messages/*', getChannelMessages);
app.post('/chatserver/publish/*', publishMessage);

app.listen(3000, function () {
    console.log('Chat server listening on port 3000!')
})