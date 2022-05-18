const fs = require('fs');

const listPageFilter = async (url, request, response) => {
    if (url.indexOf('dingtalk/pc/detail.jhtml') > 0) {
        const {
            response: newResponse
        } = response;
        const content = fs.readFileSync('./script/src/rules/inject.js').toString();
        const myBodyStr = newResponse.body.toString().replace(`</html>`, `
        <script type="text/javascript">
            ${content}
        </script>
        </html>
        `);
        newResponse.body = Buffer.from(myBodyStr);
        return {
            flag: true,
            response: {
                ...response,
                response: newResponse,
            },
        }
    }
    return {
        flag: false,
    }
}

const filters = [listPageFilter];


/**
 * 响应处理规则
 */
module.exports = async (request, response) => {
    const {
        requestOptions: {
            hostname,
        },
        url
    } = request;
    const res = response;

    if (hostname !== 'saas.daxue.dingtalk.com') {
        return null;
    }

    for (let filter of filters) {
        const {
            flag,
            response
        } = await filter(url, request, res);
        if (flag) {
            return response;
        }
    }

    return response;
}
