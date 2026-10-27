# Unified mainline preview

User asks to merge Sony development into mainline, preserve F5/Sony behavior, shorten UI and reduce storage. They subsequently request skipping physical tests because projector is off, and explicitly choose publishing a prerelease with Sony marked pending verification.

One root application, one manifest/version/build, one shared UI/model/cloud stack. Retain integrated package identity to preserve the completed F5 configuration migration. DevicePolicy routes DBF5 to built-in settings recovery, Sony Android 8+ to existing shell/Binder input backend; unknown devices get neither automatic backend. Sony platform-specific code remains a driver, not a separate app fork.

Sony helper uses new fixed authority/descriptor, build number and unique deployment path/process name. Both IPC UID checks are retained. Sony local ADB identity is generated under the new UID and needs a new initial user authorization; do not export the old ADB private identity. Legacy cloud settings may migrate through same-signed exact-caller provider. Pause old Sony helper before installing migration bridge. Sony device remains untouched this round.

Tasks: merge backend and tests, build helper and app, review changed paths, replace duplicate root/Sony/F5 companion source trees with this single source and minimal legacy migration tooling, verify signatures/model hash/source package, publish v1.1.0-preview.1 as prerelease. No remaining device operations or obsolete app deletion this round. Original F5 cleanup requirement remains pending final physical acceptance; retain old apps for rollback until then.
