# VoxKey

Android TV 语音输入法：按住遥控器语音键说话，松开填入当前输入框。仅在输入框与 VoxKey 面板激活时接管，密码框不录音，其他场景保留系统语音助手。

## 当前版本

**本地开发构建：1.1.0-preview.2（尚未发布）**。新增当贝品牌／厂商识别，取消标准语音键接管的 F5 型号限制。首次打开会在缺少必要设置时显示引导，主界面也可进入“设置引导”。支持自动识别135／219／231及手动选择；厂商品牌未识别时可手动启用标准接管，索尼仍使用原专用后端。其他当贝固件未经实机验证，不能保证所有语音键均经过 Android 无障碍过滤。

设置顺序：麦克风 → 启用输入法 → 选择 VoxKey → 开启语音键无障碍 → 开启输入框接管 → 测试。引导动态显示状态，并提供系统设置入口、可选开机恢复授权说明和设备／按键诊断。普通输入不需要WRITE_SECURE_SETTINGS；当贝自动恢复仅限Android8及以上，非F5固件恢复为实验功能，需单独启用并检查厂商自启动／后台限制。Android7仍可使用标准输入。

现有统一包可保留数据覆盖升级。本地包含ARMv7识别库，需要设备支持32位ARM应用。新增机型、引导UI和最终preview.2尚未实机验收。见 [preview.2验证记录](docs/verification-1.1.0-preview.2.md)。

已发布版本：

[**1.1.0-preview.1 统一主线预览版**](https://github.com/jnlpc123/VoxKey/releases/tag/v1.1.0-preview.1)

一个安装包、一套界面与识别代码，按设备启用对应能力。主线已合并原 Sony 分支和 F5 恢复助手；不再维护独立的应用源码副本。平台按键驱动仍因固件能力不同而有所区别。

| 设备 | 启用能力 | 验证范围 |
| --- | --- | --- |
| 当贝 F5 / Android 9 / ARMv7 | 输入框语音键过滤、内置开机／唤醒恢复 | 前序一体版云端、本地输入和遥控器关机恢复通过；存储优化版模型加载通过；最终统一预览包尚未复测 |
| Sony BRAVIA 4K GB ATV3 / Android 9 / ARMv7 | 原有 shell/Binder 语音键辅助及本机 ADB 恢复 | 原 1.0.2 已实测，本轮统一预览包待复测 |
| 其他机型 | 普通输入面板可用性待验证 | 不承诺实体语音键或自动恢复 |

索尼辅助目前限制 Android 8–9（API26–28），Android10及以上不会启动此旧 Binder 接口驱动；其他索尼机型也未验证。本包只包含 ARMv7 原生识别库。索尼辅助仍依赖 shell 权限与本机调试，合并主线不会消除系统权限限制。

用户本轮要求跳过剩余实机测试，故发布为 **Prerelease**。旧版及独立助手的卸载应等最终安装包实测通过后再进行，不能把编译或单元检查当作设备验收。

## 主要功能

- SenseVoice int8 离线识别，或主动选择百炼 Paraformer-realtime-v2 云端识别。
- 手机扫码填写北京地域 API Key 与可选 Workspace ID；密钥通过 Android Keystore 加密保存。
- 两行五列遥控器工具栏：删除、空格、光标、清空、提交、切换普通输入法。
- 精简界面说明，保留操作、状态和必要错误提示。
- 直接从 APK 读取离线模型，避免额外保存约 **229 MiB** 的模型副本；已有副本仅在新版引擎成功初始化后清理。模型、识别精度设置保持不变。

本地失败不会自动上传。仅用户选择云端并发起输入时上传音频，账号费用与额度以百炼控制台为准。扫码配置仅在可信的同一 Wi-Fi 使用；手机提交内容加密，但局域网 HTTP 页面不等于 HTTPS 身份认证。保存或离开页面后配对码失效。

## 新安装

下载 `VoxKey-1.1.0-preview.1-armv7.apk`。已安装一体版时可直接覆盖更新；从旧包迁移请先看下一节。

```sh
adb install -r VoxKey-1.1.0-preview.1-armv7.apk
adb shell pm grant app.codex.f5voice.compat.com.android.integrated android.permission.RECORD_AUDIO
adb shell am start -n app.codex.f5voice.compat.com.android.integrated/.MainActivity
```

在系统设置启用并选择 VoxKey 输入法，启用“VoxKey 输入框语音键”无障碍服务。

F5 额外授予内置恢复权限，再进入“自动恢复”启用：

```sh
adb shell pm grant app.codex.f5voice.compat.com.android.integrated android.permission.WRITE_SECURE_SETTINGS
```

F5 在开机／唤醒时恢复默认输入法；同一次使用中主动切换普通输入法不会立即抢回。电脑无需常连。不会停用原输入法、桌面或系统助手。

索尼进入“语音键辅助”启用恢复；开发者选项中需开启本机 5555 调试端口，并在电视对 VoxKey 本机身份勾选“始终允许”。辅助无运行时限，输入框外释放接管，电脑无需常连。新包 UID 改变，需要重新授权一次；不迁移旧的 ADB 私钥。关闭调试端口、撤销授权或强行停止应用会影响恢复。

## 从旧版迁移

统一包名为 `app.codex.f5voice.compat.com.android.integrated`，保留已在 F5 验证的关机清理兼容身份；它是普通第三方应用，不具有系统签名或系统 UID。旧 F5 / Sony 版本的包名为 `app.codex.f5voice`。

1. 保留旧数据。**Sony 用户先在旧版“辅助状态”暂停辅助，松开所有按键后再迁移**，避免旧辅助进程与新版争用设备。F5 先停用旧独立助手：`adb shell am force-stop app.codex.f5voice.compat.com.android.recovery`。
2. 安装 `VoxKey-Legacy-Migration-1.0.4.apk` 覆盖旧包，再安装统一包。迁移桥与原发布包同签名，仅保留受保护的本机迁移接口，没有桌面入口、输入法、网络或录音功能；新版设置完成前旧语音输入会暂时不可用。全新安装不需要它。
3. 打开统一包，等待设置迁移并校验成功。签名权限、精确调用包名和签名校验保护 Binder 迁移；云配置在新 UID 下重新加密，回读确认后标记完成。迁移过程没有文件中转或网络导出，不显示现有密钥。
4. 配置新版输入法、语音键及所需权限；Sony 重新确认本机调试授权。F5 启用内置自动恢复。
5. 实体语音输入与正常关机／开机恢复均通过后，才卸载旧包 `app.codex.f5voice` 与旧独立助手 `app.codex.f5voice.compat.com.android.recovery`，最终保留一个 VoxKey。

迁移失败时不要清除旧数据。自编译签名无法读取原发布签名的配置，可通过手机重新配置。原签名私钥、API Key 和本机 ADB 身份不随源码发布。

## 构建

Windows PowerShell、JDK17、Android SDK Platform35、Build-tools35.0.0：

```powershell
$env:JAVA_HOME = 'C:\Java\jdk-17'
$env:ANDROID_HOME = 'C:\Android\sdk'
.\prepare-model.ps1
.\helper\build.ps1
Copy-Item .\helper\build\sony-helper.jar .\assets-fast\sony\sony-helper.jar -Force
.\test.ps1
.\build.ps1
```

模型不进入 Git；准备脚本从本仓库 v1.0.0 Release 下载固定附件并校验模型与词表 SHA256。仓库附带所需依赖、辅助资源和许可证，见 `deps/`、`licenses/`、`helper/`。输出 `build/F5Voice.apk`。默认生成本地开发签名，不能覆盖原发布签名的安装包。

迁移工具源码位于 `tools/legacy-migration/`，为冻结的升级桥，不是另一条产品开发线。历史独立版本可在 Git 历史及原 Release 获取。

详见 [本轮验证记录](docs/verification-1.1.0-preview.1.md)。
