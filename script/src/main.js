const AnyProxy = require('anyproxy');
const exec = require('child_process').exec;
const portfinder = require('portfinder');

if (!AnyProxy.utils.certMgr.ifRootCAFileExists()) {
    AnyProxy.utils.certMgr.generateRootCA((error, keyPath) => {
        // let users to trust this CA before using proxy
        if (!error) {
            const certDir = require('path').dirname(keyPath);
            console.log('The cert is generated at', certDir);
            const isWin = /^win/.test(process.platform);
            if (isWin) {
                exec('start .', {cwd: certDir});
            } else {
                exec('open .', {cwd: certDir});
            }
        } else {
            console.error('error when generating rootCA', error);
        }
    });
}

const options = {
    port: 8003,
    rule: {
        beforeSendResponse: require('./rules/response'),
        beforeDealHttpsRequest: require('./rules/https'),
    },
    webInterface: {
        enable: true,
        webPort: 8002
    },
    throttle: 10000,
    forceProxyHttps: false,
    wsIntercept: false, // 不开启websocket代理
    silent: false
};
const proxyServer = new AnyProxy.ProxyServer(options);

// 定时检查8001端口是否能够访问 不能则退出 每10秒检查一次
setInterval(async () => {
    const p = await portfinder.getPortPromise({
        port: 8001,
    });
    if (p === 8001) {
        console.log("主进程已关闭，退出注入进程");
        process.exit(1);
    }
}, 10 * 1000);

proxyServer.on('ready', () => {
    console.log("代理服务器启动成功");
    // AnyProxy.utils.systemProxyMgr.enableGlobalProxy('127.0.0.1', '8001');
});
proxyServer.on('error', (e) => { /* */
});
proxyServer.start();
