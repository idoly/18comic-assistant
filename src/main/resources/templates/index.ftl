<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
  <link rel="icon" href="favicon.ico" />
  <style type="text/tailwindcss">
    @theme {
        --animate-marquee: marquee 10s linear infinite;
        @keyframes marquee {
            0% {
            transform: translateX(0%);
            }
            100% {
            transform: translateX(-100%);
            }
        }
    }
  </style>
  <title>18comic-assistant</title>
</head>
<body class="w-full h-full bg-[#ececec] text-[#777]">

  <div class="w-full max-w-md mx-auto mt-4">
    <input type="text" id="search" placeholder="标题或编号" class="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring focus:border-blue-300"/>
  </div>

  <div class="mx-auto mt-5 grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-4">
  <#list comics as comic>
  <#assign id = comic.id()>
  <#assign title = comic.title()>
    <a data-id="${id}" data-title="${title?lower_case}" href="comic/${id}/index.html" class="w-full max-w-sm flex flex-col overflow-hidden rounded-lg shadow group p-2 bg-[#fffdf8] border border-[#e8e4d9] hover:border-[#d6cbaa] transition-colors duration-300 shadow-sm hover:shadow-md hover:bg-[#fefcf5]">
      <img src="comic/${id}/0.png" class="w-full aspect-[3/4] object-cover rounded-md shadow" />
      <div class="flex flex-col justify-between flex-1 overflow-hidden mt-2">
          <div class="flex-1 overflow-hidden hide-scrollbar text-center">
              <div class="whitespace-nowrap inline-block title-text group-hover:animate-marquee">
                <span class="text-sm overflow-hidden">
                    ${title}
                </span>
              </div>
          </div>
      </div>
    </a>
  </#list>
  </div>
  <script>
    document.addEventListener("DOMContentLoaded", () => {
      document.querySelectorAll(".title-text").forEach(el => {
        const parent = el.parentElement;
        if (el.scrollWidth <= parent.clientWidth) {
          el.classList.remove("group-hover:animate-marquee");
        }
      });

      const searchInput = document.getElementById("search");
      const cards = document.querySelectorAll("a[data-id][data-title]");

      searchInput.addEventListener("input", () => {
        const keyword = searchInput.value.trim().toLowerCase();
        cards.forEach(card => {
          const id = card.dataset.id.toLowerCase();
          const title = card.dataset.title;
          const match = id.includes(keyword) || title.includes(keyword);
          card.style.display = match ? "" : "none";
        });
      });
    });
  </script>
</body>
</html>
