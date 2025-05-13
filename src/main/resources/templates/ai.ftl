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

  let socket;

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

        if (result.response) {
          typeMsg(result.response);
        } else if (result.error) {
          typeMsg(result.error);
        } else if (result.data){
            showWebPreview('https://www.18comic.vip');
        } else {
          typeMsg('收到未知响应');
        }
      } catch (e) {
        console.error('解析失败:', e);
        addMsg('ai', 'AI 响应解析失败');
      }
    };

    socket.onclose = () => {
      addMsg('ai', "连接已关闭，尝试重连...");
      setTimeout(connectWebSocket, 3000);
    };
  }

  function showWebPreview(url) {
    chatCt.classList.replace('flex-col', 'flex-row');
    chatSide.classList.replace('w-7/12', 'w-1/2');
    webCt.classList.remove('hidden');
    webCt.classList.add('w-1/2', 'h-screen');
    webIf.src = url;
  }

  connectWebSocket();
</script>

</body>
</html>