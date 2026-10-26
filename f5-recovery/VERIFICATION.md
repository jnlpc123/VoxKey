# F5 恢复助手验收

设备：当贝 F5，DBF5 / Android 9。日期：2026-10-26。

- 33 项纯 Java 合并/会话/手动选择检查通过。
- 独立代码审查完成；修复最新列表合并与 SCREEN_OFF/ON 事件遗漏后无 P1/P2 阻断。
- 主动切换搜狗，等待 7 秒仍保持搜狗；点击“立即恢复”后恢复 VoxKey。
- 用户使用实体遥控器正常关机，约 10 秒后再开机，确认画面恢复。
- 电脑重新连接后只读检查，没有执行 ime set、settings put 或助手手动恢复操作。
- 恢复助手进程 PID 前后保持不变，证实没有被本次当贝关机清理强停。
- 默认输入法自行恢复为 app.codex.f5voice/.VoiceIme。
- 已启用服务同时包含 VoxKey KeyService 和 Aerial HomeGuardService，两项均保留。
- dumpsys 确认 RecoveryService 在前台运行、VoiceIme 与 KeyService 已绑定，电源 Awake / ON。

本次为实体遥控器正常关机/唤醒验证，内核 uptime 连续；没有测试拔电冷启动或另一次 adb reboot。不要将其描述为断电重启实测。

安全范围：只安装独立恢复助手及授予所声明的 WRITE_SECURE_SETTINGS；没有改动原 VoxKey APK/签名、模型、账号配置、索尼电视或系统应用。未执行额外主页键/桌面测试。