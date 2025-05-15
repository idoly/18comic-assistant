<!DOCTYPE html>
<html lang="zh">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>18comic-assistant</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <style>
    #chat-box::-webkit-scrollbar { display: none; }
    #chat-box { -ms-overflow-style: none; scrollbar-width: none; }
  </style>
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
</head>
<body class="bg-gray-100">

  <div id="init-container" class="flex flex-col items-center justify-center h-screen text-center px-4 relative">
    <div class="absolute top-[40%] left-1/2 -translate-x-1/2 -translate-y-[45%] flex flex-col items-center">
      <img src="logo.png"/>
      <input
        id="init-input"
        type="text"
        placeholder="请输入你的问题…"
        class="w-full max-w-md px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring focus:border-blue-300"
      />
    </div>
  </div>

  <div id="chat-container" class="hidden">
    <div id="chat-side" class="flex flex-col h-screen">
      <div id="chat-box" class="flex-1 overflow-y-auto bg-white m-4 p-4 space-y-4 rounded-lg shadow"></div>
      <div class="sticky bottom-0 bg-gray-100 p-4">
        <input
          id="chat-input"
          type="text"
          placeholder="请输入你的问题…"
          class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring focus:border-blue-300"
        />
      </div>
    </div>

    <div id="web-content" class="hidden relative">
      <button
        id="close-web-btn"
        class="absolute top-4 right-4 bg-red-500 text-white px-3 py-1 rounded-lg hover:bg-red-600 z-10"
      >
        关闭
      </button>
      <button
        id="refresh-web-btn"
        class="absolute top-4 right-12 bg-green-500 text-white px-3 py-1 rounded-lg hover:bg-red-600 z-10"
      >
        刷新
      </button>
      <iframe
        id="web-iframe"
        src=""
        frameborder="0"
        width="100%"
        height="100%"
      ></iframe>
    </div>

  </div>
<script>
  const initCt   = document.getElementById('init-container');
  const initIn   = document.getElementById('init-input');
  const chatCt   = document.getElementById('chat-container');
  const chatSide = document.getElementById('chat-side');
  const chatBox  = document.getElementById('chat-box');
  const chatIn   = document.getElementById('chat-input');
  const webCt    = document.getElementById('web-content');
  const webIf    = document.getElementById('web-iframe');

  const refreshBtn = document.getElementById('refresh-web-btn');
  const closeBtn = document.getElementById('close-web-btn');

  function scrollDown() {
    chatBox.scrollTop = chatBox.scrollHeight;
  }

  function addMsg(role, txt) {
    const wrap = document.createElement('div');
    wrap.className = 'flex items-start space-x-2 ' + (role === 'user' ? 'justify-end' : '');

    const avatar = document.createElement('div');
    avatar.className = 'w-8 h-8 rounded-full bg-gray-400 flex items-center justify-center text-white';
    avatar.textContent = role === 'user' ? '我' : 'AI';

    const bubble = document.createElement('div');
    bubble.className = 'max-w-[75%] px-4 py-2 rounded-lg whitespace-pre-line ' + (role === 'user'? 'bg-blue-500 text-white' : 'bg-gray-200 text-black');
    bubble.textContent = txt;

    if (role === 'user') {
      wrap.append(bubble, avatar);
    } else {
      wrap.append(avatar, bubble);
    }

    chatBox.appendChild(wrap);
    scrollDown();
  }

  function typeMsg(text) {
    const wrap = document.createElement('div');
    wrap.className = 'flex items-start space-x-2';

    const avatar = document.createElement('div');
    avatar.className = 'w-8 h-8 rounded-full bg-gray-400 flex items-center justify-center text-white';
    avatar.textContent = 'AI';

    const bubble = document.createElement('div');
    bubble.className = 'max-w-[75%] px-4 py-2 rounded-lg whitespace-pre-line bg-gray-200 text-black';

    wrap.append(avatar, bubble);
    chatBox.appendChild(wrap);
    scrollDown();

    let i = 0;
    const timer = setInterval(() => {
      bubble.textContent += text[i++];
      scrollDown();
      if (i >= text.length) clearInterval(timer);
    }, 30);
  }

  function startChat(txt) {
    initCt.classList.add('hidden');
    chatCt.classList.remove('hidden');
    chatCt.classList.add('flex', 'flex-col');
    chatSide.classList.add('w-7/12', 'mx-auto');
    addMsg('user', txt);
    sendToAI(txt);
    chatIn.focus();
  }

  function sendToAI(text) {
    if (socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({ question: text }));
    } else {
      addMsg('ai', 'WebSocket 未连接，无法发送消息');
    }
  }

  initIn.addEventListener('keydown', e => {
    if (e.key === 'Enter' && initIn.value.trim()) {
      startChat(initIn.value.trim());
    }
  });

  chatIn.addEventListener('keydown', e => {
    if (e.key === 'Enter' && chatIn.value.trim()) {
      const t = chatIn.value.trim();
      addMsg('user', t);
      sendToAI(t);
      chatIn.value = '';
    }
  });

  closeBtn.addEventListener('click', () => {
    webCt.classList.add('hidden');
    webCt.classList.remove('w-1/2', 'h-screen');
    webIf.src = '';
    chatCt.classList.replace('flex-row', 'flex-col');
    chatSide.classList.replace('w-1/2', 'w-7/12');
    chatSide.classList.add('mx-auto');
  });

  refreshBtn.addEventListener('click', () => {
    showWebPreview(url);
  });

  let socket;

  let url = "";

  function connectWebSocket() {
    socket = new WebSocket("ws://localhost:8080/chat");

    socket.onmessage = event => {
      try {
        let result = event.data;
        if (typeof result === 'string') {
          result = JSON.parse(result);
          if (typeof result === 'string') {
              result = JSON.parse(result);
          }
        }

        console.log(result)

        // deepseek
        if (result.response) {
          return typeMsg(result.response);
        }
        
        if (result.error) {
          return typeMsg(result.error);
        }
        
        // app
        if (result.code !== 200 || !result.data) {
          return typeMsg(result.msg)
        } 

        if (result.type == 'url') {
            url = result.data
            showWebPreview(result.data);
        }

        if (result.type == 'comic') {
          if (result.data.albums) {
            updateComicCard(result.data)
          } else {
            comicCard(result.data)
          }
        }

        if (result.type == 'link') {

        }
        
      } catch (e) {
        console.error(e);
      }
    };

    socket.onclose = () => {
      setTimeout(connectWebSocket, 3000);
    };
  }

  function downloadFile(url, filename) {
    const a = document.createElement('a');
    a.href = url;
    a.download = filename || ''; // 可选：指定下载时的文件名
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  function showWebPreview(url) {
    chatCt.classList.replace('flex-col', 'flex-row');
    chatSide.classList.replace('w-7/12', 'w-1/2');
    webCt.classList.remove('hidden');
    webCt.classList.add('w-1/2','h-auto','m-4');
    webIf.src = window.location.origin + url;
  }

  function comicCard(comic) {
    const card = document.createElement('div');
    card.className = 'flex flex-col overflow-hidden rounded-lg shadow group p-4 bg-[#fffdf8] border border-[#e8e4d9] hover:border-[#d6cbaa] transition-colors duration-300 shadow-sm hover:shadow-md hover:bg-[#fefcf5] w-full max-w-md';

    // 第 1 行：封面图
    const cover = document.createElement('img');
    cover.src = comic.covers?.[1] || comic.covers?.[0] || '';
    cover.alt = comic.title;
    cover.className = 'w-full aspect-[3/4] object-cover rounded-md shadow';

    // 第 2 行：标题
    const titleWrap = document.createElement('div');
    titleWrap.className = 'overflow-hidden mt-2 text-center';

    const title = document.createElement('div');
    title.className = 'whitespace-nowrap inline-block title-text group-hover:animate-marquee';
    title.textContent = comic.title;

    // 检查是否需要滚动动画
    setTimeout(() => {
      const parent = title.parentElement;
      if (title.scrollWidth <= parent.clientWidth) {
        title.classList.remove('group-hover:animate-marquee');
      }
    }, 0);

    titleWrap.appendChild(title);

    // 第 3 行：简介（最多显示三行，鼠标悬停显示完整）
    const description = document.createElement('p');
    description.textContent = comic.description || '暂无简介';
    description.title = description.textContent; // 鼠标悬停显示
    description.className = 'text-sm text-gray-600 leading-relaxed mt-2 line-clamp-3';

    // 第 4 行：按钮
    const button = document.createElement('button');
    button.textContent = '下载阅读';
    button.className = 'mt-4 self-center bg-blue-500 hover:bg-blue-600 text-white px-5 py-2 rounded-full shadow transition';
    button.onclick = () => {
      url = comic.url;
      showWebPreview(url);
    };

    card.append(cover, titleWrap, description, button);

    // 包一层头像
    const wrap = document.createElement('div');
    wrap.className = 'flex items-start space-x-3 px-2 py-4';

    const avatar = document.createElement('div');
    avatar.className = 'w-10 h-10 rounded-full bg-gray-400 flex items-center justify-center text-white font-bold text-sm';
    avatar.textContent = 'AI';

    wrap.append(avatar, card);

    chatBox.appendChild(wrap);
    scrollDown();
  }

  function updateComicCard(comic) {
    const card = document.createElement('div');
    card.className = 'flex flex-col bg-white rounded-xl shadow p-4 w-full max-w-xl space-y-4';

    // 1. 漫画封面
    const cover = document.createElement('img');
    cover.src = comic.covers?.[0] || '';
    cover.alt = comic.title || '漫画封面';
    cover.className = 'w-full rounded-md aspect-[3/4] object-cover';

    // 2. 简介
    const description = document.createElement('div');
    description.textContent = comic.description || '暂无简介';
    description.className = 'text-sm text-gray-700 leading-relaxed max-h-24 overflow-y-auto';

    // 3. 操作按钮行
    const buttonRow = document.createElement('div');
    buttonRow.className = 'flex justify-between gap-3';

    const downloadBtn = document.createElement('button');
    downloadBtn.textContent = '下载';
    downloadBtn.className = baseBtnClass();
    downloadBtn.onclick = () => downloadComic(comic);

    const updateBtn = document.createElement('button');
    updateBtn.textContent = '更新';
    updateBtn.className = baseBtnClass('bg-blue-500 hover:bg-blue-600');
    updateBtn.onclick = () => updateComic(comic);

    const deleteBtn = document.createElement('button');
    deleteBtn.textContent = '删除';
    deleteBtn.className = baseBtnClass('bg-red-500 hover:bg-red-600');
    deleteBtn.onclick = () => deleteComic(comic);

    buttonRow.append(downloadBtn, updateBtn, deleteBtn);

    // 4. 章节目录（顺序/逆序切换 + 列表）
    const dirContainer = document.createElement('div');
    dirContainer.className = 'flex flex-col space-y-2';

    const dirHeader = document.createElement('div');
    dirHeader.className = 'flex justify-between items-center';

    const dirTitle = document.createElement('span');
    dirTitle.textContent = '目录';
    dirTitle.className = 'font-semibold text-gray-800';

    const toggleBtn = document.createElement('button');
    toggleBtn.textContent = '逆序';
    toggleBtn.className = 'text-blue-500 text-sm hover:underline cursor-pointer';

    let reversed = false;
    toggleBtn.onclick = () => {
      reversed = !reversed;
      toggleBtn.textContent = reversed ? '顺序' : '逆序';
      renderChapters();
    };

    dirHeader.append(dirTitle, toggleBtn);

    const chapterList = document.createElement('div');
    chapterList.className = 'max-h-40 overflow-y-auto flex flex-col gap-1 text-sm text-gray-600';

    function renderChapters() {
      chapterList.innerHTML = '';
      const chapters = reversed ? [...comic.albums].reverse() : comic.albums;
      chapters.forEach(album => {
        const link = document.createElement('a');
        link.textContent = "第" + album.index + "话";
        link.href = "#"; // 防止直接跳转
        link.target = '_self'; // 无需打开新标签
        link.className = 'hover:text-blue-500 truncate cursor-pointer';
        
        // 添加点击事件：调用 showWebPreview
        link.addEventListener('click', (e) => {
          e.preventDefault(); // 阻止默认跳转
          const previewUrl = '/comic/' + comic.id + "/album/" + album.id + "/index.html";
          showWebPreview(previewUrl);
        });

        chapterList.appendChild(link);
      });
    }

    function showWebPreview(url) {
      chatCt.classList.replace('flex-col', 'flex-row');
      chatSide.classList.replace('w-7/12', 'w-1/2');
      webCt.classList.remove('hidden');
      webCt.classList.add('w-1/2','h-auto','m-4');
      webIf.src = window.location.origin + url;
    }

    renderChapters();
    dirContainer.append(dirHeader, chapterList);

    // 组装
    card.append(cover, description, buttonRow, dirContainer);

    const wrap = document.createElement('div');
    wrap.className = 'flex justify-center p-4';
    wrap.appendChild(card);
    chatBox.appendChild(wrap);
    scrollDown();
  }

  // 公共按钮样式生成
  function baseBtnClass(extra = 'bg-gray-300 hover:bg-gray-400') {
    return 'flex-1 py-2 rounded font-semibold text-white transition ' + extra;
  }


  connectWebSocket();
</script>

</body>
</html>