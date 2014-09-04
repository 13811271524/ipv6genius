#! /system/bin/sh

localIP=
server=210.76.114.13
prefix=2001:da8:202:107:0:5efe

ip tunnel add sit1 mode sit remote ${server} local ${localIP} ttl 255
ip -6 addr add ${prefix}:${localIP}/64 dev sit1
ip link set sit1 up
ip -6 route add ::/0 via ${prefix}:${server} dev sit1
