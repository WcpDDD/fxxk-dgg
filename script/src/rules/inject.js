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
        debug('开始监听签到按钮');
        setInterval(() => {
            const countDown = document.querySelector('.countdown-container,div[class^="countdown_container"]');
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

        // 检查进度条进度
        debug('开始监听进度条进度');
        let f = null;
        let status = 0;

        const nextVideo = () => {
            try {
                const videoList = document.querySelectorAll('div[class^="c_menu"] div[class^="c_tab_no_desc"] div[class^="c_catalog"] div[class^="c_item__"]');
                if (!!videoList) {
                    for (let i = videoList.length - 1; i >= 0; i --) {
                        // 找到当前页面激活的视频
                        const video = videoList[i];
                        const clazzNames = [...video.classList].filter(item => item.startsWith('active'));
                        // 找到处于激活状态的视频 如果不是最后一个视频 则切换到下一个视频
                        if (clazzNames.length > 0 && i < (videoList.length - 1)) {
                            videoList[i + 1].querySelector('div[class^="c_item_main"]>div').click();
                            break;
                        }
                    }
                }
            } catch (e) {
                debug(e.message)
            } finally {
                // 不管视频有没切换成功 均重置状态
                status = 0;
            }
        }

        const progressListener = () => {
            const progress = document.querySelector('.prism-progress-played');
            if (!!progress) {
                const width = parseInt(progress.style.width.replace('%', ''));
                if (width > 25 && status === 0) {
                    debug('进度条已超过四分之一');
                    status = 1;
                } else if (width > 50 && status === 1) {
                    debug('进度条已过半');
                    status = 2
                } else if (width > 75 && status === 2) {
                    debug('进度条已超过四分之三');
                    status = 3;
                } else if (width >= 99 && status === 3) {
                    debug('即将看完，切换至下一视频');
                    setTimeout(nextVideo, 4000);
                }
            }
        }

        f = setInterval(progressListener, 3000);
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