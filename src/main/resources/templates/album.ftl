<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="icon" href="../../../../favicon.ico">
    <title>${comic.title}-第${album.index?c}话</title>
</head>
<body class="w-full h-full bg-[#ececec] text-[#777]">
    <div class="w-3/4 mx-auto mt-5 flex flex-col items-center">
    <#list album.photos as photo>
        <img src="${photo.index?c}.png" width="${photo.width?c}" height ="${photo.height?c}" />
    </#list>
    </div>
    <div id="nav-buttons" class="sticky bottom-5 mt-10 flex justify-center gap-6 transition-opacity duration-300 ease-in-out opacity-100 pointer-events-auto">
        <#-- 先如你之前一样计算 prevEnabled 和 nextEnabled -->
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
        <button
            class="px-6 py-2 rounded-none shadow-inner border border-[#ccc]
                <#if prevEnabled?has_content>
                    text-white bg-[#888] hover:bg-[#666] transition-all duration-300 shadow-md
                <#else>
                    text-[#aaa] bg-[#e0e0e0] cursor-not-allowed
                </#if>"
            <#if !prevEnabled?has_content>disabled</#if>
            <#if prevEnabled?has_content>onclick="window.location.href='../${prevEnabled}/index.html';"</#if>>
            ⬅ 上一话
        </button>

        <button
            class="px-6 py-2 rounded-none shadow-inner border border-[#ccc]
                <#if nextEnabled?has_content>
                    text-white bg-[#888] hover:bg-[#666] transition-all duration-300 shadow-md
                <#else>
                    text-[#aaa] bg-[#e0e0e0] cursor-not-allowed
                </#if>"
            <#if !nextEnabled?has_content>disabled</#if>
            <#if nextEnabled?has_content>onclick="window.location.href='../${nextEnabled}/index.html';"</#if>>
            下一话 ➡
        </button>
    </div>

    <script>
        const nav = document.getElementById('nav-buttons');
        let lastY = window.scrollY;
        window.addEventListener('scroll', () => {
            const y = window.scrollY;
            const atBottom = (y + window.innerHeight >= document.documentElement.scrollHeight - 2);

            if (atBottom || y < lastY) {
                nav.style.opacity = '1';
                nav.style.pointerEvents = 'auto';
            } else {
                nav.style.opacity = '0';
                nav.style.pointerEvents = 'none';
            }

            lastY = y;
        });
    </script>
</body>
</html>

