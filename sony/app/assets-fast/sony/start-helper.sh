#!/system/bin/sh
case "$1" in ''|*[!0-9]*) exit 2;; esac
cd /data/local/tmp/voxkey-probe || exit 3
existing=$(pidof voxkey-sony)
if [ -z "$existing" ]; then
 CLASSPATH=/data/local/tmp/voxkey-probe/sony-helper.jar setsid app_process /system/bin --nice-name=voxkey-sony SonyInputBridge "$1" </dev/null >sony-helper.log 2>&1 &
fi
n=0
while [ "$n" -lt 30 ]; do
 existing=$(pidof voxkey-sony)
 if [ -n "$existing" ]; then break; fi
 sleep 0.1
 n=$((n+1))
done
[ -n "$existing" ] && echo "$existing"
