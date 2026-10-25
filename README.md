# VoxKey

为当贝 F5 遥控器设计的 Android TV 语音输入法：按住语音键说话，松开填入当前输入框。

- **本地离线**：SenseVoice int8 ORT，在设备端识别。
- **可选云端**：百炼 Paraformer-realtime-v2；通过手机扫码配置 API Key。
- **编辑工具栏**：两行五列，支持删除、光标、空格、清空、提交、切换普通输入法。
- **限定接管范围**：仅在 VoxKey 输入面板可见且编辑器允许时处理 F5 实体语音键；密码框不录音。其他场景保留原系统语音。
- Aerial 风格蓝色卡片图标，原生 Java 实现，无 WebView。

## 下载

[**VoxKey 1.0.0 Release**](https://github.com/jnlpc123/VoxKey/releases/tag/v1.0.0)

`VoxKey-1.0.0-armv7.apk`：可安装包，包含离线模型。源码可直接克隆本仓库，离线模型单独提供为 Release 附件。

当贝 F5 1.0.0 已在 Android 9、ARMv7 实测。另有 [索尼 1.0.2 专用恢复版](sony/README.md)：保留原系统助手，辅助程序无运行时限，并支持电视本机 ADB 自动恢复；[下载 1.0.2](https://github.com/jnlpc123/VoxKey/releases/tag/v1.0.2)。根目录保留 F5 稳定版源码，索尼版源码见 `sony/`。其他机型未经验证。

## 使用

1. 安装 APK，在系统设置启用 **VoxKey 输入法**、授予麦克风权限。
2. 启用 **VoxKey 输入框语音键** 无障碍服务，用于只在输入框内接管实体语音键。
3. 打开输入框，选择 VoxKey，按住语音键说话，松开识别并填字。
4. 输入面板的“切换输入法”优先切回已启用的搜狗，否则选择其他普通输入法或打开系统选择器。

不会自动停用或卸载原桌面、原输入法、系统语音助手。安装后的系统权限需要自行按提示授权。本项目不执行 root 操作。

## 本地与云端

默认保留本地使用能力。本地失败不会自动上传到云端。云端仅在用户主动选择该模式并按住语音键时发送音频，账号额度及费用以百炼控制台为准。

在“识别设置”打开配对页，用连接同一可信 Wi-Fi 的手机扫码，填写北京地域 API Key、可选 Workspace ID，然后保存。配对码10分钟有效，刷新、保存或离开页面立即失效。投影仪使用 Android Keystore 加密存储密钥，不向手机回显已有密钥。

手机提交内容使用临时 RSA-OAEP/SHA256 + AES-GCM 加密。临时页面由投影仪通过局域网 HTTP 提供，因此仅应在可信网络使用；提交加密不能替代 HTTPS 对页面本身的认证。代码和发布包不包含任何用户 API Key。

## 构建

Windows PowerShell、JDK17、Android SDK Platform35、Build-tools35.0.0。

```powershell
$env:JAVA_HOME = 'C:\Java\jdk-17'
$env:ANDROID_HOME = 'C:\Android\sdk'
.\prepare-model.ps1
.\test.ps1
.\build.ps1
```

Git 仓库不保存约240MB的模型文件；`prepare-model.ps1` 从本项目 v1.0.0 Release 下载固定的模型附件，校验SHA256后只提取模型与词表，并分别复核其SHA256。可离线将同一附件路径传给 `-ArchivePath`。

构建产物：`build/F5Voice.apk`。依赖的 JAR 和两个 ARMv7 原生库已包含。没有 Gradle 或云端账号配置要求。

**发布签名私钥未公开。** 自行构建会生成新的本地签名，不能直接覆盖原发布签名安装的版本。内部包名保留 `app.codex.f5voice`，以维持现有输入法、无障碍及密钥身份。

## 1.0 性能与验证

- 清除旧 Zipformer 分支、历史热词路径和无用原生 API 包装库。
- 进程级共享模型，本地模式提前异步加载；加载回调去重并随界面销毁解除。
- 本地录音缓冲按需增长，云端采用复用的100ms块；取消的排队识别及时移除并清除PCM。
- 工具栏复用绘制对象，减少临时分配。
- F5 最终包三次进程冷加载 **5.411 / 4.502 / 4.627 秒**，中位数 **4.627秒**；旧版单次基线6.592秒。复用已就绪引擎的回调均为0毫秒。
- 加载优化有取舍：1秒合成音频解码中位数1.403秒，旧版单次1.170秒。后台负载、样本数不同，不代表所有场景均有相同比例的性能变化。
- 64项自动化检查通过。合成音频不等同于自然语音准确率评测；本轮未重做真人语音、实体遥控和桌面验收。

详见 [验证报告](docs/VERIFICATION.md)。初次解包模型的额外时间不包含在上述已缓存文件的进程冷加载计时中。

## 代码布局

- `src/`：输入法、按键服务、本地/云端识别及手机配对服务。
- `res/`：原生矢量图标与 Android 配置。
- `assets-fast/mobile/`：本机提供的手机配置页，无外部CDN。
- `deps/`：固定版本编译依赖及 ARMv7 JNI/ONNX Runtime。
- `tests/`：按键、编辑结果策略、导航、云端协议、PCM内存与取消所有权测试。
- `licenses/`：第三方许可与模型上游说明；模型原始名称及来源保持在说明中。

## 第三方来源

离线模型来自 [csukuangfj/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-2024-07-17](https://huggingface.co/csukuangfj/sherpa-onnx-sense-voice-zh-en-ja-ko-yue-2024-07-17)，上游 [SenseVoice](https://github.com/FunAudioLLM/SenseVoice)。原始模型转换为ORT格式，参数与词表不变。

运行库 [sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx)、[ONNX Runtime](https://github.com/microsoft/onnxruntime)，网络 [OkHttp](https://github.com/square/okhttp)，二维码 [ZXing](https://github.com/zxing/zxing)，手机加密 [node-forge](https://github.com/digitalbazaar/forge)。各组件及模型遵循 `licenses/` 中各自的许可，本仓库没有将第三方模型重新许可。

