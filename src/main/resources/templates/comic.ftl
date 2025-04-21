<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="icon" href="../../favicon.ico">
    <title>${comic.title}</title>
</head>
<body class="w-full h-full bg-[#ececec] text-[#777]">
<#list albums as album>
    <a href="album/${album.id}/index.html">第${album.index?c}话</a>
</#list>
</body>
</html>