<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=<device-width>, initial-scale=1.0">
    <link rel="stylesheet" href="../../pico.css">
    <link rel="icon" href="../../favicon.ico">
    <title>photo</title>
</head>
<body>
<#list photos as photo>
    <img src="${photo.index}.png"/>
</#list>
</body>
</html>