# VoxKey 当贝 F5 恢复助手 1.0

修复当贝 F5 正常关机清理后，VoxKey 被强停、默认输入法回退搜狗、语音键无障碍服务关闭的问题。独立小程序，不替换 VoxKey 本体，不修改模型与云端配置。

[下载安装包与源码](https://github.com/jnlpc123/VoxKey/releases/tag/f5-recovery-v1.0.0)

## 行为

- 开机、解锁或从待机唤醒后，将默认输入法恢复为 VoxKey，并恢复其语音键服务。
- 保留搜狗和其他输入法；保留 Aerial 等已有无障碍服务，只合并固定 VoxKey 登记。
- 每次唤醒只恢复一次默认输入法；本次使用中主动切到普通输入法不会被持续抢回。下次唤醒再恢复 VoxKey。
- 可在“VoxKey 恢复助手”界面停用，停用后停止服务及恢复任务，当前输入法不变。
- 没有网络、录音或屏幕读取权限。不依赖电脑常开或 ADB 常驻进程。

## 固件适配

仅在 Build.MODEL=DBF5 时运行，针对 Android 9 的当贝 F5 实测。该固件关机时 doRamClear() 使用硬编码包名例外，而不读取 HOME 清理保护名单。助手包名 app.codex.f5voice.compat.com.android.recovery 在自己的命名空间内明确匹配该固件的 com.android 子串例外，避免被同一关机流程强停。界面和通知均显示“VoxKey 恢复助手”；没有系统签名、系统 UID、root 或系统 APK 修改。固件升级改变清理规则后应重新验证。

## 权限与安装

首次安装需要经 ADB 授予 WRITE_SECURE_SETTINGS，用于以下四个固定字段：enabled_input_methods、default_input_method、enabled_accessibility_services、accessibility_enabled。该权限本身范围较广，但本程序没有配置任意目标、网络入口或执行任意命令的接口。另声明开机广播与前台服务权限。停用/卸载助手可停止恢复。

使用 Install-F5-Recovery.ps1，传入 -Device 和 -Adb，安装后在助手界面选择“启用自动恢复”。设备现有 VoxKey 需已安装，不能单独用本助手进行语音识别。

## 验证

33 项组件合并、启动/唤醒状态、手动输入法选择测试通过。恢复助手已安装并获授权，主动切换搜狗保持 7 秒没有被抢回，“立即恢复”可切回 VoxKey；Aerial 登记保持。

真实遥控器关机/开机验证结果见 [验证记录](VERIFICATION.md)。断电冷启动若未另行记录，不应视为已实测。

## 构建

JDK 17、Android SDK Platform 35、Build-tools 35.0.0，PowerShell：

```powershell
.\test.ps1 -JdkDirectory 'C:\Java\jdk-17'
.\build.ps1 -SdkDirectory 'C:\Android\sdk' -JdkDirectory 'C:\Java\jdk-17'
```

没有签名私钥时生成自己的本地开发签名；发布签名私钥不在源码包内。原签名的设备更新必须由相同签名构建。

服务使用前台通知、START_STICKY、开机/解锁/应用更新接收器及持久周期任务。正常关机时不持有唤醒锁、不录音、不阻止休眠。设置列表采用最新快照合并、读回校验和有限重试；Android Settings 未提供跨进程 CAS，因此极端并发写入仍需关注。