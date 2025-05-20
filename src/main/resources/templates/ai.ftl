<!DOCTYPE html>
<html lang="zh">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
  <link rel="icon" href="favicon.ico" />
  <title>18comic-assistant</title>
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
<body class="w-screen h-screen bg-gray-100">
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

  <div id="chat-container" class="w-full h-full hidden">
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
      <button id="close-web-btn" class="absolute top-4 right-6 p-2 rounded-full bg-red-500 hover:bg-red-600 z-10 text-white">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><!-- Icon from Google Material Icons by Material Design Authors - https://github.com/material-icons/material-icons/blob/master/LICENSE --><path fill="currentColor" d="M12 2C6.47 2 2 6.47 2 12s4.47 10 10 10s10-4.47 10-10S17.53 2 12 2m4.3 14.3a.996.996 0 0 1-1.41 0L12 13.41L9.11 16.3a.996.996 0 1 1-1.41-1.41L10.59 12L7.7 9.11A.996.996 0 1 1 9.11 7.7L12 10.59l2.89-2.89a.996.996 0 1 1 1.41 1.41L13.41 12l2.89 2.89c.38.38.38 1.02 0 1.41"/></svg>
      </button>
      <button id="refresh-web-btn" class="absolute top-4 right-18 p-2 rounded-full bg-green-500 hover:bg-green-600 z-10 text-white">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><!-- Icon from Google Material Icons by Material Design Authors - https://github.com/material-icons/material-icons/blob/master/LICENSE --><path fill="currentColor" d="M17.65 6.35a7.95 7.95 0 0 0-6.48-2.31c-3.67.37-6.69 3.35-7.1 7.02C3.52 15.91 7.27 20 12 20a7.98 7.98 0 0 0 7.21-4.56c.32-.67-.16-1.44-.9-1.44c-.37 0-.72.2-.88.53a5.994 5.994 0 0 1-6.8 3.31c-2.22-.49-4.01-2.3-4.48-4.52A6.002 6.002 0 0 1 12 6c1.66 0 3.14.69 4.22 1.78l-1.51 1.51c-.63.63-.19 1.71.7 1.71H19c.55 0 1-.45 1-1V6.41c0-.89-1.08-1.34-1.71-.71z"/></svg>
      </button>
      <button id="toggle-size-btn" class="absolute top-4 right-30 p-2 rounded-full bg-blue-500 hover:bg-blue-600 z-10 text-white">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><!-- Icon from Google Material Icons by Material Design Authors - https://github.com/material-icons/material-icons/blob/master/LICENSE --><path fill="currentColor" d="M6 14c-.55 0-1 .45-1 1v3c0 .55.45 1 1 1h3c.55 0 1-.45 1-1s-.45-1-1-1H7v-2c0-.55-.45-1-1-1m0-4c.55 0 1-.45 1-1V7h2c.55 0 1-.45 1-1s-.45-1-1-1H6c-.55 0-1 .45-1 1v3c0 .55.45 1 1 1m11 7h-2c-.55 0-1 .45-1 1s.45 1 1 1h3c.55 0 1-.45 1-1v-3c0-.55-.45-1-1-1s-1 .45-1 1zM14 6c0 .55.45 1 1 1h2v2c0 .55.45 1 1 1s1-.45 1-1V6c0-.55-.45-1-1-1h-3c-.55 0-1 .45-1 1"/></svg>
      </button>
      <iframe id="web-iframe" src="" frameborder="0" width="100%" height="100%"></iframe>
    </div>

  </div>
  <script>

    const socket = connect();

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
    const toggleBtn = document.getElementById('toggle-size-btn');

    function scrollDown() {
      chatBox.scrollTop = chatBox.scrollHeight;
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
      webIf.contentWindow?.location.reload();
    });


    toggleBtn.addEventListener('click', () => {
      const isFull = webCt.classList.contains('fixed');


      document.body.classList.toggle('overflow-hidden', isFull);

      if (!isFull) {
        // 切换到全屏
        chatSide.classList.add('hidden');

        chatCt.classList.add('flex-col');
        chatCt.classList.remove('flex-row');
      
        webCt.classList.remove('w-1/2', 'm-4')
        webCt.classList.add('fixed', 'top-0', 'left-0', 'w-full', 'h-full', 'z-50', 'bg-white');

        webIf.classList.add('w-full', 'h-full');
      } else {
        // 恢复半屏
        chatSide.classList.remove('hidden');

        chatCt.classList.remove('flex-col');
        chatCt.classList.add('flex-row');

        webCt.classList.add('w-1/2', 'm-4')
        webCt.classList.remove('fixed', 'top-0', 'left-0', 'w-full', 'h-full', 'z-50', 'bg-white');

        webIf.classList.remove('w-full', 'h-full');
      }
    });


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

    function connect() {
      let socket = new WebSocket("ws://localhost:8080/chat");
      socket.onmessage = ({data}) => {
        try {
          const result = deepParseJson(data);
          console.log(result);
          if (result.response) return typeMsg(result.response);
          if (result.error) return typeMsg(result.error);
          if (result.code !== 200 || !result.data) return typeMsg(result.msg);
          if (result.type === 'url') return open(result.data);
          if (result.type === 'comic') return render(result.data);
          if (result.type === 'file') return download(result.data);
        } catch (e) {
          console.error(e);
        }
      };
      socket.onclose = () => setTimeout(connect, 3000);

      return socket;
    }

    function deepParseJson(data) {
      let parsed = JSON.parse(data);
      return typeof parsed === 'string' ? deepParseJson(parsed) : parsed;
    }

    function open(url) {
      chatCt.classList.replace('flex-col', 'flex-row');
      chatSide.classList.replace('w-7/12', 'w-1/2');
      webCt.classList.remove('hidden');
      webCt.classList.add('w-1/2','h-auto','m-4');
      webIf.src = window.location.origin + url;
    }

    function render(comic) {
      const card = document.createElement('div');
      card.className = 'flex flex-col max-w-xs bg-white rounded-lg border border-gray-200 shadow-lg overflow-hidden';

      const img = document.createElement('img');
      img.src = comic.covers && comic.covers.length > 0 ? comic.covers[0] : '';
      img.alt = comic.title || '';
      img.className = 'w-full aspect-[3/4] object-cover';
      card.appendChild(img);

      const titleDiv = document.createElement('div');
      titleDiv.className = 'p-2 text-sm font-semibold text-gray-800 truncate';
      titleDiv.textContent = comic.title || '无标题';
      card.appendChild(titleDiv);

      const descDiv = document.createElement('div');
      descDiv.className = 'p-2 text-xs text-gray-600 border-t border-gray-100';
      descDiv.textContent = comic.description || '暂无简介';
      card.appendChild(descDiv);

      const actionsDiv = document.createElement('div');
      actionsDiv.className = 'flex justify-between p-2 border-t border-gray-100 space-x-2';
      ['下载', '同步', '删除'].forEach(function(label) {
        const btn = document.createElement('button');
        let colorClasses = 'bg-blue-500 hover:bg-blue-600';
        if (label === '同步') colorClasses = 'bg-yellow-500 hover:bg-yellow-600';
        if (label === '删除') colorClasses = 'bg-red-500 hover:bg-red-600';

        btn.className = 'flex-1 px-2 py-1 text-xs text-white rounded ' + colorClasses + ' transition transform hover:scale-105';
        btn.textContent = label;
        btn.onclick = function() {
          if (label === '下载') socket.send("下载动漫" + comic.id);
          if (label === '同步') socket.send("同步动漫" + comic.id);
          if (label === '删除') socket.send("删除动漫" + comic.id);
        };
        actionsDiv.appendChild(btn);
      });
      card.appendChild(actionsDiv);

      if (comic.albums && Array.isArray(comic.albums)) {
        const ctrlDiv = document.createElement('div');
        ctrlDiv.className = 'flex justify-between items-center p-2 border-t border-gray-100';

        const labelEl = document.createElement('label');
        labelEl.htmlFor = 'order-' + comic.id;
        labelEl.className = 'text-xs text-gray-700';
        labelEl.textContent = '排序：';

        const select = document.createElement('select');
        select.id = 'order-' + comic.id;
        select.className = 'text-xs border border-gray-300 rounded px-1 py-0.5 focus:outline-none focus:ring focus:border-blue-300';

        [['asc', '正序'], ['desc', '倒序']].forEach(function(optArr) {
          const opt = document.createElement('option');
          opt.value = optArr[0];
          opt.textContent = optArr[1];
          select.appendChild(opt);
        });

        ctrlDiv.appendChild(labelEl);
        ctrlDiv.appendChild(select);
        card.appendChild(ctrlDiv);

        const chaptersDiv = document.createElement('div');
        chaptersDiv.className = 'h-24 overflow-y-auto p-2 bg-gray-50';

        var ul = document.createElement('ul');
        ul.className = 'list-none';
        chaptersDiv.appendChild(ul);
        card.appendChild(chaptersDiv);

        var renderChapters = function() {
          ul.innerHTML = '';
          var list = select.value === 'desc' ? comic.albums.slice().reverse() : comic.albums;
          list.forEach(function(a) {
            var li = document.createElement('li');
            li.className = 'px-2 py-1 text-xs text-gray-700 truncate hover:bg-gray-100 cursor-pointer';
            li.textContent = '第' + a.index + '话';
            li.onclick = function() {
              li.classList.add('text-blue-600', 'font-semibold', 'bg-blue-50');
              open('/comic/' + comic.id + '/album/' + a.id + '/index.html');
            };
            ul.appendChild(li);
          });
        };

        select.addEventListener('change', renderChapters);
        renderChapters();
    }

    const wrap = document.createElement('div');
    wrap.className = 'flex items-start space-x-2 p-2';

    const avatar = document.createElement('div');
    avatar.className = 'w-6 h-6 rounded-full bg-gray-400 flex items-center justify-center text-white text-xs';
    avatar.textContent = 'AI';

    wrap.appendChild(avatar);
    wrap.appendChild(card);
    chatBox.appendChild(wrap);
    scrollDown();
  }


    async function download(filename) {
      addMsg('ai', '开始下载：' + filename);
      try {
        const res = await fetch("/download?filename=" + encodeURIComponent(filename));
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const blob = await res.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        addMsg('ai', '下载完成：' + filename);
      } catch (err) {
        addMsg('ai', '下载失败：' + err.message);
      }
    }
  </script>
</body>
</html>