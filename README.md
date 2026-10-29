# VoxKey

Android TV 语音输入法：按住遥控器语音键说话，松开填入当前输入框。仅在非密码输入框与 VoxKey 面板激活时接管，面板外保留系统语音助手。

## 当前预览版

[**VoxKey 1.1.0-preview.2**](https://github.com/jnlpc123/VoxKey/releases/tag/v1.1.0-preview.2)

- 取消标准语音键接管的 F5 型号限制，按厂商／品牌识别其他当贝设备。品牌未识别时可手动开启标准接管。
- 首次缺少必要设置时显示引导，主界面可随时打开“设置引导”。
- 支持常见键码135、219、231的自动模式与手动选择；提供设备／按键诊断。
- 一个安装包、一套主线源码，保留Sony专用按键后端与当贝内置恢复。

**其他当贝型号尚未实机测试。** 新增的品牌识别、遥控器麦克风路由、厂商权限菜单、语音键分发和关机恢复均需在实际型号上验证。部分固件绕过Android无障碍过滤，不能只靠设置开关接管。最终preview.2及新引导UI也尚未装机验收，因此发布为 **Prerelease**，不表示所有当贝设备已适配成功。

| 设备 | 当前能力与验证范围 |
| --- | --- |
| 当贝F5 / Android9 / ARMv7 | 前序一体版迁移、云端／本地输入、遥控器正常关机恢复已通过；最终preview.2待复测 |
| 其他当贝型号 | 开放标准输入框语音键接管；自动恢复为实验功能；**尚未实测** |
| Sony BRAVIA 4K GB ATV3 / Android9 / ARMv7 | 保留原shell/Binder辅助与本机ADB恢复；原1.0.2实测通过，统一preview.2待复测 |
| 其他品牌或固件 | 可手动尝试标准接管，功能与路由未经验证；Sony不会使用这个回退后端 |

本包只包含ARMv7原生识别库，需要设备支持32位ARM应用。普通输入最低Android7（API24）；当贝自动恢复需要Android8及以上（API26+）。Sony辅助仅支持Android8–9（API26–28），仍依赖shell权限与本机调试。历史 [preview.1](https://github.com/jnlpc123/VoxKey/releases/tag/v1.1.0-preview.1) 与稳定版Release保留。

## 新设备安装

1. 下载 `VoxKey-1.1.0-preview.2-armv7.apk`，通过设备安装器或ADB安装，首次打开VoxKey。
2. 按“设置引导”依次完成：**麦克风授权 → 启用VoxKey输入法 → 选择VoxKey为当前输入法 → 开启“VoxKey 输入框语音键”无障碍服务 → 确认输入框接管已开启**。
3. 普通当贝设备先保留“语音键：自动”。若品牌信息未识别，可在引导中手动开启标准接管；若只对某个键码有效，可选择135／219／231之一。
4. 选择“测试语音输入”，在VoxKey面板显示时按住实体语音键说话，松开后核对文字。隐藏键盘后再按语音键，应保留原系统助手。无需停用当贝助手或更换默认系统语音助手。
5. 若无效，进入“设置引导 → 设备与按键诊断”，查看机型、服务是否连接、是否收到标准语音键事件。无事件不等于单纯缺少权限，也可能是厂商路由绕过过滤。

引导会显示当前状态，从系统设置返回后自动刷新。系统设置入口不可用时会回退到通用设置；麦克风被拒绝后可进入应用详情。普通语音输入**不需要**WRITE_SECURE_SETTINGS。只需云端识别时，在“识别设置”用同一可信Wi-Fi的手机扫码填写百炼北京地域API Key与可选Workspace ID。

ADB替代安装方式（以下所有命令均指向同一台目标设备；多设备连接请加 `-s 设备地址:端口`）：

```sh
adb install -r VoxKey-1.1.0-preview.2-armv7.apk
adb shell am start -n app.codex.f5voice.compat.com.android.integrated/.MainActivity
```

同目录的 `install-voxkey.ps1 -Device 设备IP:5555` 可安装、授予麦克风权限，并为识别出的Android8+当贝设备授予可选恢复权限；它不自动启用恢复、不删除旧应用，也不替代设备上的输入法／无障碍设置。

## 可选开机恢复

当贝在设置引导中选择“开机恢复”。如显示需要ADB授权，电脑连接目标设备后执行：

```sh
adb shell pm grant app.codex.f5voice.compat.com.android.integrated android.permission.WRITE_SECURE_SETTINGS
```

再返回应用手动启用。开机／唤醒时恢复VoxKey；同次使用中主动切换普通输入法不会立即抢回。其他当贝型号的固件清理／自启动机制未实测，应用会标记为实验功能；需要时在厂商设置允许VoxKey自启动和后台运行。Android7不启用恢复服务，不影响标准输入。电脑无需常连。

Sony用户进入“语音键辅助”启用恢复，开发者选项需开启本机5555调试端口，并对VoxKey本机身份勾选“始终允许”。输入框外释放接管，辅助无运行时限。关闭调试端口、撤销授权或强行停止应用会影响恢复。合并源码不会消除系统权限限制。

## 已安装preview.1或其他一体版：覆盖升级

统一包名为 `app.codex.f5voice.compat.com.android.integrated`。同一发布签名的preview.1／前序一体版可直接安装preview.2覆盖，**不要先卸载或清除数据，不需要迁移工具**。保留已有云端设置、密钥和Sony本机ADB身份；回到“设置引导”检查状态即可。此路径不因升级本身要求重新确认Sony本机身份。

如果更换为另一台投影仪，这是新设备安装：迁移工具不会跨设备搬运原设备的私有配置。请在新设备重新授权并通过手机扫码配置云端；不要复制旧电视的Keystore或ADB私钥。

## 旧F5 1.0.0／Sony 1.0.2包：同设备迁移

旧包名为 `app.codex.f5voice`，与统一包不同。保留旧数据，再使用Release提供的 `VoxKey-Legacy-Migration-1.0.4.apk`；全新安装与preview.1覆盖升级不需要此工具。

1. **Sony先在旧版“辅助状态”暂停辅助并松开所有按键**，避免两个辅助争用设备。F5如有旧独立助手，先停止它：`adb shell am force-stop app.codex.f5voice.compat.com.android.recovery`。
2. 安装迁移工具覆盖旧包，再安装preview.2。迁移工具与原发布包同签名，仅保留受保护的本机迁移接口；没有桌面入口、输入法、网络或录音功能。完成新版设置前旧语音输入会暂时不可用。
3. 打开preview.2，等待迁移并校验完成；可在“自动恢复”页查看迁移状态。新包通过签名权限、精确调用包名及签名校验读取旧云配置，在新UID下重新加密并回读确认；没有文件中转或网络导出。
4. 完成新版设置引导。Sony因从旧包迁至新UID，需要为新的VoxKey本机身份重新确认一次调试授权；旧ADB私钥不迁移。识别出的当贝设备可按需单独授权并启用内置恢复。
5. 验证实体语音输入、输入框外保留原助手及正常关机／开机后的所需恢复功能。**迁移校验和实机验收均通过后**，才卸载旧包 `app.codex.f5voice` 与旧独立助手 `app.codex.f5voice.compat.com.android.recovery`，最终保留一个VoxKey。

迁移失败时不要清除旧数据。自编译签名不能读取原发布签名的配置，可通过手机重新配置。统一包使用兼容命名空间，但仍是普通第三方应用，不具有系统签名或系统UID。本次发布不会远程安装、卸载或改动任何设备。

## 识别与存储

- SenseVoice int8离线识别，或主动选择百炼Paraformer-realtime-v2云端识别。
- 两行五列遥控器编辑工具栏，支持删除、空格、光标、清空、提交与切换普通输入法。
- 从APK直接读取同一个离线模型，避免约229MiB重复私有文件。已有副本仅在引擎成功初始化后清理，模型精度不变。
- 本地失败不自动上传；仅用户选择云端并发起输入时上传音频。账号费用与额度以百炼控制台为准。
- 手机提交内容加密，密钥使用Android Keystore存储；仅在可信的同一Wi-Fi配置，局域网HTTP页面不等于HTTPS身份认证。保存或离开后配对码失效。

## 构建与验证

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

模型不进入Git，准备脚本下载固定模型附件并校验SHA256；依赖、辅助资源和许可见 `deps/`、`helper/`、`licenses/`。输出 `build/F5Voice.apk`。签名私钥、用户API Key及本机ADB身份不公开，自编译默认开发签名不能覆盖原发布签名安装。

冻结的同设备旧包迁移工具源码见 `tools/legacy-migration/`，不是另一条产品开发线。历史独立版本可在Git历史及原Release获取。

178项逻辑检查、100次模拟ADB往返和RSA签名检查通过，APK编译及签名已校验。这些检查不能替代实机验收。详见 [preview.2验证记录](docs/verification-1.1.0-preview.2.md) 与 [前序实测范围](docs/verification-1.1.0-preview.1.md)。