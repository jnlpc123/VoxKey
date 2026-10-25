# VoxKey 1.0 verification

Environment: Dangbei F5, Android 9/API28, armeabi-v7a; same device, same existing verified model cache. No reboot or original launcher change.

| Build / CPU session configuration | Cold load ms | Warm reuse ms | 1s synthetic decode ms |
|---|---:|---:|---:|
| 0.15 baseline, CPU defaults |6592|0|1170|
| 1.0 candidate, graph=0, no spinning |6817|0|1274|
| 1.0 no-prepacking, run 1 |5605|0|1384|
| 1.0 no-prepacking, run 2 |4596|0|1400|
| 1.0 no-prepacking, run 3 |4626|0|1606|
| Experimental spin during run, run 1 |4479|0|1577|
| Experimental spin during run, run 2 |4577|0|1396|
| Final APK, run 1 |5411|0|1381|
| Final APK, run 2 |4502|0|1403|
| Final APK, run 3 |4627|0|1727|

Final configuration: GraphOptimizationLevel=0; session.disable_prepacking=1; session.intra_op.allow_spinning=0; session.inter_op.allow_spinning=0. CPU threads=2. Same model/tokens; extended-conversion experiment was not adopted. No approximate arithmetic changes.
Final cold median 4627 ms versus single baseline 6592 ms: 29.81% lower. Final synthetic decode median1403 ms versus baseline1170 ms:19.91% higher. Different sample counts and uncontrolled background load limit generalization. All synthetic decodes succeeded. Warm result is ready-engine callback latency, not decoding latency.

64 unit checks passed:15 session/key ownership,9 trailing punctuation,22 toolbar navigation/cloud protocol/PCM copying,9 bounded audio buffer,9 queued/running cancellation ownership.
Both newly introduced buffer and pending-audio tests first failed because their production classes did not exist, then passed with implementations. Review found no release blocker; defensive finally ensures PCM clearing even if stream release fails.
Signed APK verified with v2/v3; installed APK transmission SHA256 matches local. Final APK SHA256 c774e15bba63385097eb04f6009714a8ec8c696724c9a7653c0d01ced7f0e6c9.
Model SHA256 d94222f3a655fe6c0690d31d261a3e1503aa56aa10d1aebe116f7b15ebec9358; tokens SHA256 f449eb28dc567533d7fa59be34e2abca8784f771850c78a47fb731a31429a1dc.
Two native libraries confirmed by ELF DT_NEEDED inspection and Android model initialization/decode; no unused C/C++ API wrapper shipped.
Installed label VoxKey and existing cloud mode confirmed via UI hierarchy. Default IME app.codex.f5voice/.VoiceIme, enabled accessibility still includes app.orchard.tv/.guard.HomeGuardService and app.codex.f5voice/app.codex.f5voice.KeyService. Temporary benchmark APK uninstalled after measurement.
Not repeated: human speech accuracy, physical remote, HOME/boot, actual cloud requests, QR credential submission. No secret values read or exported.

Runtime options references:
- https://github.com/k2-fsa/sherpa-onnx/blob/master/sherpa-onnx/csrc/session.cc
- https://github.com/microsoft/onnxruntime/blob/v1.23.2/include/onnxruntime/core/session/onnxruntime_session_options_config_keys.h
