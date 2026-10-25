# VoxKey 1.0.2 — Sony recovery edition

索尼 Android TV 语音键适配与自动恢复版。已在 BRAVIA 4K GB ATV3 / Android 9 / ARMv7 实测。其他索尼机型的按键路由、蓝牙设备名和调试端口可能不同，尚未验证。

[下载 1.0.2](https://github.com/jnlpc123/VoxKey/releases/tag/v1.0.2) · [当贝 F5 1.0.0](https://github.com/jnlpc123/VoxKey/releases/tag/v1.0.0)

## 安装包

- `VoxKey-1.0.2-Sony-armv7.apk`：完整安装包，包含本地 SenseVoice 模型，适合首次安装。
- `VoxKey-1.0.2-Sony-cache-update.apk`：小体积覆盖更新包，依赖先前版本已提取且校验成功的本地模型缓存；不要卸载或清除数据后用这个小包首次安装。
- `VoxKey-1.0.2-Sony-source.zip`：索尼版源码、编译依赖、开源许可，不含模型和签名私钥。

包名与 1.0.0 保持 `app.codex.f5voice`，支持保留数据覆盖升级。根目录仍保留当贝 F5 1.0.0 源码；此目录是索尼专用分支，请勿将其视为所有设备通用的适配。

## 工作方式

索尼遥控器语音键绕过普通无障碍按键过滤。本版保留 shell 身份的辅助程序，仅当 VoxKey 面板显示、存在可编辑输入连接且非密码框时，接管名为 `SONY TV RC MIC 001` 的蓝牙设备的语音键。输入框外释放接管，保留索尼原助手。应用不修改默认助手、不停用系统应用，不需要 root。

辅助程序取消一小时运行期限，使用经过 UID 检查的 Binder 双向通信。后台前台服务检查连接，失效时通过 `127.0.0.1:5555` 本机 ADB 安全部署和拉起，电脑无需保持运行，IP 变化不影响本机连接。辅助构建号用于更新替换，文件锁防止重复运行。

开机、用户解锁、应用更新后恢复；持久 JobScheduler 任务作为兜底。健康时只查 Binder，不保持 ADB 连接；失败使用 5/15/30/60 秒退避。前台通知和“辅助状态”显示结果，可重新连接或暂停。暂停等待已按下的键释放后退出，并阻止迟到的连接恢复接管。

## 首次设置

1. 安装完整 APK，授予麦克风权限，启用并选择 VoxKey 输入法；按系统提示启用 VoxKey 无障碍服务。
2. 在电视开发者选项开启 USB/网络调试，确保本机 5555 端口可用。各厂商菜单不同，此应用不能自行获取系统权限来打开关闭的调试端口。
3. 打开 VoxKey → 辅助状态 → 启用并自动恢复。
4. 系统会为 `VoxKey@localhost` 的独立本机调试身份显示一次授权弹窗，勾选“始终允许”并确认。该身份的私钥使用 Android Keystore AES-GCM 加密保存，应用不复制电脑的 ADB 私钥。
5. 状态变为“辅助已连接”后，在输入框内按住语音键说话、松开填字。

正常系统重启一般无需重新授权；撤销调试授权、清除应用数据或卸载重装后需重新授权。调试端口关闭或厂商限制后台运行时，恢复可能无法完成；用户强行停止应用后需重新打开 VoxKey。ADB 授权本身授予调试能力，请仅允许自己认可的身份。应用没有提供任意远程命令接口。

旧 protocol1 限时预览版首次迁移：先隐藏键盘、不按语音键，再结束旧辅助进程；旧版不支持新退出命令。本次实测设备已完成迁移，protocol2 构建可自动安全替换。

## 验证

- 64 项语音输入、导航、云端协议与音频取消回归检查通过。
- 12 项恢复退避/SYNC 测试、100 次快速 ADB OPEN/数据/CLOSE 往返及 RSA 签名检查通过。
- 原辅助程序的 10 项转发、5 项释放、3 项 Binder 重连检查已通过；新增单实例锁与恢复代码完成编译和实机验证。
- 暂停后进程和恢复服务退出；重新启用后自行启动。
- 模拟辅助进程退出后约 8 秒自行恢复；软件更新后按构建号替换旧进程。
- 系统重启后，在电脑未执行辅助启动命令的情况下，电视自行启动辅助、保留本机授权，调试端口仍为 5555。
- 用户确认重启后实体语音键能填字，未弹出索尼助手；收起键盘后回到非接管状态。

本次“重启”测试使用 `adb reboot`，不等同于断电冷启动；没有宣称所有索尼固件和断电场景都能自启。

## 构建

Windows PowerShell、JDK 17、Android SDK Platform 35、Build-tools 35.0.0。请设置 `JAVA_HOME` 和 `ANDROID_HOME`，或在每个构建脚本传 `-JdkDirectory`、`-SdkDirectory`。

```powershell
# 在 sony 目录下
.\helper\build.ps1
Copy-Item .\helper\build\sony-helper.jar .\app\assets-fast\sony\sony-helper.jar -Force
.\app\prepare-model.ps1
.\app\test.ps1
.\app\build.ps1
```

`prepare-model.ps1` 复用 v1.0.0 的固定模型附件并校验 SHA256。Git 不包含模型。完整 APK 输出到 `app/build/F5Voice.apk`。

`app/assets-fast/sony/` 附带对应辅助 jar、JNA 原生库和固定启动脚本。修改 helper 后请同步更新 `SonyInputBridge.BUILD` 和 `SonyImeBridge.ready()` 的预期构建号，再重新编译、复制辅助 jar。

**签名私钥不发布。** 没有原始私钥时，构建脚本生成自己的开发签名，不能覆盖当前发布签名的安装版本。

AdbLib 来源：[cgutman/AdbLib](https://github.com/cgutman/AdbLib)，本版修复流尾部读取、快速 OPEN 响应和写入同步等问题；保留 BSD 许可。JNA 来源：[java-native-access/jna 5.17.0](https://github.com/java-native-access/jna/tree/5.17.0)，保留双许可说明与 Apache 2.0 文本。其余模型和依赖的许可均在 `app/licenses/`。
