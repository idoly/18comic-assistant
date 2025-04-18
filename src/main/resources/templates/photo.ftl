<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="icon" href="https://18comic.vip/favicon.ico">
    <#--  <link rel="stylesheet" href="../../../../pico.css">  -->
    <#--  <link rel="icon" href="../../../../favicon.ico">  -->
    <title>${comic.title} - EP${album.index?c}</title>
</head>
<body>
    <div class="container mx-auto p-4">
        <!-- 图片列表 -->
        <div class="flex flex-col space-y-4">
            <#list album.photos as photo>
                <img class="w-full md:w-1/2 lg:w-1/3 xl:w-1/4 object-cover" src="${photo.index?c}.png" alt="Photo ${photo.index?c}" />
            </#list>
        </div>

        <!-- 上一页和下一页按钮 -->
        <div>
            <#assign prevEnabled = "">
            <#assign nextEnabled = "">
            <#list albums as target>
                <#if target.index == album.index - 1>
                    <#assign prevEnabled = target.id>
                </#if>
                <#if target.index == album.index + 1>
                    <#assign nextEnabled = target.id>
                </#if>
            </#list>
            <button <#if prevEnabled == "">disabled</#if> onclick="<#if prevEnabled != "">window.location.href='../${prevEnabled}/index.html';</#if>">pre</button>
            <button <#if nextEnabled == "">disabled</#if> onclick="<#if nextEnabled != "">window.location.href='../${nextEnabled}/index.html';</#if>">next</button>
        </div>
    </div>
</body>
</html>

