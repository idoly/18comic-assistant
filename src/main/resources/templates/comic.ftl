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
    <!-- search-->
    <!-- comic-card 点击展开-显示目录，点击目录-跳转 -->
    <#list comics as comic>
        <a href="comic/${comic.id}">${comic.title}</a>
    </#list>
</body>
</html>