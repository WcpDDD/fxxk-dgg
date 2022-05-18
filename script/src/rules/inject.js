setTimeout(() => {
    const body = document.querySelector('body');
    const div = document.createElement('div');
    div.style.width = '200px';
    div.style.height = '60vh';
    div.style.position = 'fixed';
    div.style.top = '100px';
    div.style.right = '18px';
    div.style.backgroundColor = 'rgba(0, 0, 0, 0.8)';
    div.style.color = 'white';
    div.style.overflowY = 'auto'
    div.style.zIndex = '9999';
    body.append(div);

    const btn = document.createElement('button');
    btn.style.position = 'fixed';
    btn.style.top = '100px';
    btn.style.left = '18px';
    btn.style.zIndex = '9999';
    btn.innerText = '复制html';

    btn.addEventListener('click',() => {
        const input = document.createElement('input');
        document.body.appendChild(input);
        input.setAttribute('value', body.innerHTML);
        input.select();
        if (document.execCommand('copy')) {
            document.execCommand('copy');
            debug('复制成功');
        }
        document.body.removeChild(input);
    });

    body.append(btn);

    const debug = (message) => {
        const node = document.createElement('p');
        node.innerText = message;
        div.append(node);
        div.scrollTop = div.scrollHeight;
    }

    debug('脚本注入成功...');

    const check = () => {
        const btn = document.querySelector('.prism-play-btn');
        return !!btn;
    }

    const start = () => {
        setInterval(() => {
            debug('开始监听签到按钮');
            const countDown = document.querySelector('.countdown-container');
            if (!!countDown) {
                debug('检测到签到按钮');
                countDown.click();
                const f = setInterval(() => {
                    const countDown = document.querySelector('.count-down');
                    if (!countDown) {
                        debug('签到成功!');
                        clearInterval(f);
                    }
                }, 3000);
            }
        }, 3000);
    }

    const f = setInterval(() => {
        debug('开始检测当前页面是否为课程页面...');
        if (!check()) {
            debug('当前页面并非课程页面,请进入课程页面');
            return;
        }
        debug('已经进入课程页面, 点击播放开始刷课');
        start();
        clearInterval(f);
    }, 3000);

}, 10 * 1000);