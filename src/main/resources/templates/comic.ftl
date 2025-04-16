<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="pico.css">
    <link rel="icon" href="favicon.ico">
    <title>18comic-assistant</title>
</head>
<body>
<#list comics as comic>
    <a href="comic/${comic.id()}/index.html" title= ${comic.title()}" style="display: inline-flex; align-items: center; gap: 4px;">
        <img src="${comic.cover()}" alt="">
        <span>${comic.title()}</span>
    </a>
</#list>
</body>
</html>