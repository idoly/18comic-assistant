<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="../">
    <link rel="icon" href="../../favicon.ico">
    <title>${comic.title}</title>
</head>
<body>
<#list albums as album>
    <a href="album/${album.id}/index.html">${"ep" + album.index}</a>
</#list>
</body>
</html>