<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
    <link rel="icon" href="../../../../favicon.ico">
    <title>${comic.title}-第${album.index?c}话</title>
</head>
<body class="w-full h-full bg-[#ececec] text-[#777]">
    <div class="md:w-3/4 sm:w-full mx-auto flex flex-col items-center">
    <#list album.photos as photo>
        <img class="border-x-1 border-white" src="${photo.index?c}.png" width="${photo.width?c}" height ="${photo.height?c}" alt="${photo.index?c}.png" />
    </#list>
    </div>
    <div id="nav-buttons" class="sticky bottom-5 mt-10 flex flex-col items-center gap-2 transition-opacity duration-300 ease-in-out opacity-100 pointer-events-auto">
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
        <button aria-label="返回顶部" tabindex="-1" class="px-3 py-2 rounded shadow-inner border border-[#ccc] bg-[#d6d6d6] text-[#666] hover:bg-[#c8c8c8] hover:text-[#444] transition-all duration-300 shadow-md"
            onclick="window.scrollTo({ top: 0, behavior: 'smooth' });" >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M5 15l7-7 7 7" />
            </svg>
        </button>

        <div class="flex flex-row gap-4 mt-2">
            <button aria-label="返回主页" tabindex="-1"  class="px-3 py-2 rounded shadow-inner border border-[#ccc] 
                <#if prevEnabled != "">bg-[#ccc] text-[#555] hover:bg-[#bbb] hover:text-[#333] transition-all duration-300 shadow-md
                <#else>bg-[#e0e0e0] text-[#aaa] cursor-not-allowed</#if>"
                <#if prevEnabled != "">onclick="window.location.href='../${prevEnabled}/index.html';"
                <#else>disabled</#if>>
                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7" />
                </svg>
            </button>
            <button aria-label="上一话" tabindex="-1" class="px-3 py-2 rounded-none shadow-inner border border-[#ccc] bg-[#d6d6d6] text-[#666] hover:bg-[#c8c8c8] hover:text-[#444] transition-all duration-300 shadow-md"
                onclick="window.location.href='../../index.html';">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M3 12l9-9 9 9M4 10v10a1 1 0 001 1h4m10-11v10a1 1 0 01-1 1h-4m-6 0h6" />
                </svg>
            </button>
            <button aria-label="下一话" tabindex="-1" class="px-3 py-2 rounded shadow-inner border border-[#ccc] 
                <#if nextEnabled != "">bg-[#ccc] text-[#555] hover:bg-[#bbb] hover:text-[#333] transition-all duration-300 shadow-md
                <#else>bg-[#e0e0e0] text-[#aaa] cursor-not-allowed</#if>"
                <#if nextEnabled != "">onclick="window.location.href='../${nextEnabled}/index.html';"
                <#else>disabled</#if>>
                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
                </svg>
            </button>
        </div>
    </div>
    <script>
        const nav = document.getElementById('nav-buttons');
        let lastY = window.scrollY;

        const updateNavVisibility = () => {
            const y = window.scrollY;
            const atBottom = y + window.innerHeight >= document.documentElement.scrollHeight - 2;
            const shouldShow = atBottom || y < lastY;

            nav.style.opacity = shouldShow ? '1' : '0';
            nav.style.pointerEvents = shouldShow ? 'auto' : 'none';

            lastY = y;
        };

        window.addEventListener('scroll', updateNavVisibility);
        updateNavVisibility();
    </script>
</body>
</html>

