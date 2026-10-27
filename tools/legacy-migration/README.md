# 冻结的旧包迁移工具

这是一次性升级桥，不是独立维护的输入法产品。包名 `app.codex.f5voice`，版本码104，可覆盖原 F5 1.0.0 和 Sony 1.0.2 的同签名安装，保留旧 UID 下的偏好与 Keystore。

只有3个 Java 类和一个 provider。没有桌面入口、输入法、网络权限、录音权限、模型或第三方库。它读取旧 `voice-settings` 快照，用现有 `F5VoiceCloudKey` 解密云端配置，不生成密钥，不修改设置，不迁移 Sony 本机 ADB 身份。

接口受 signature 权限、精确目标包名和调用 UID 签名比对保护；不允许 ADB shell 或其他应用读取。请勿把配置 Bundle 打印到日志。

使用前先暂停 Sony 旧辅助或停用 F5 独立助手；安装桥后旧输入法入口会被移除，请紧接安装和配置新版。迁移回读校验及新版实机验收通过后，卸载旧包。完整流程见根 README。

构建：`.\build.ps1 -SdkDirectory ... -JdkDirectory ... -SigningKeyPath ...`。没有原发布签名不能读取原发布包配置。私钥不在源码中。
