# MineMeeting

Meeting rooms in Minecraft. you can claim your private rooms too.

No chat & ip logs, powered by minestom.

---

在 Minecraft 里开会, 支持自己认领房间.

没有聊天日志, 也不记录 IP. 使用 Minestom 编写.

[关于 Minestom 的博文](https://ib67.io/2022/12/19/Getting-Started-With-Minestom/)

Demo: lobby.sfclub.cc    
no SLA guarantee. Mojang auth is on  
demo 服务器只给了很少内存, 而且没有 SLA 保障, 正版验证开启.  

# Usage

`java -jar ./MineMeeting.jar`

`application.conf`:

```hocon
listen-addr="0.0.0.0"
listen-port=25565
show-motd=true // probe resistance maybe
online-mode=false
server-host="room.example.com" // join room by connect to ROOMID.room.example.com
night-vision=true
allow-flight=true
# -1 to disable
max-distance-from-spawn=-1

can-break-blocks = true
auto-restore-blocks = true

kept-meeting = {
    public = "A public meeting room for everyone." // these rooms won't be destroyed
}

max-players = 200
```
