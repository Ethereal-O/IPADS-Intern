# 导入 socket 模块
import socket
# 创建socket对象
s = socket.socket()
# 将socket绑定到本机IP和端口
s.bind(('localhost', 30000))
# 服务端开始监听来自客户端的连接
s.listen()
c, addr = s.accept()
while True:
    # 每当接收到客户端socket的请求时，该方法返回对应的socket和远程地址
    data = c.recv(1024)
    print(data.decode('utf-8'))
    print('连接地址：', addr)
    c.send('0.15'.encode('utf-8'))
    # 关闭连接
    # c.close()
