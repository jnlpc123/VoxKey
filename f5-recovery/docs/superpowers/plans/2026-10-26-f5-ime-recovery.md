# F5 input method recovery implementation plan

Goal: restore VoxKey IME and its own accessibility service after Dangbei power cleanup, without changing Aerial or stock apps and without overriding manual IME switches during an active session.

Architecture: a visible, opt-in, lightweight companion with foreground service and boot/unlock/update receivers. F5's doRamClear uses a substring exception for com.android; our honest own-namespace package app.codex.f5voice.compat.com.android.recovery intentionally matches this vendor compatibility rule, without claiming system identity or privileges. Grant WRITE_SECURE_SETTINGS over authorized ADB for the three IME/accessibility settings. No internet/audio permission, no arbitrary command or configurable target. Device-protected enabled/wake state, START_STICKY and persisted periodic job. Keep stock IME enabled, preserve every other accessibility component. Restore IME once per wake; bounded retries for incomplete writes. Manual switching remains effective until next wake. Disable stops service/job.

Constraints: DBF5-only at runtime; preserve installed package app.codex.f5voice, original cloud/model data, Aerial and original launcher. No root/system APK modification. Sony untouched. No desktop/Home regression tests.

1. Test pure decision/merge policy: exact canonical service matching, null/empty/duplicates, preserve unrelated services, boot/wake/session and retry ownership, disabled state, no continuous IME enforcement.
2. Implement policy, WSS adapter with checked read-back, foreground service, receivers/job, status/enable/disable UI. Do not mark wake complete if the target/permission is unavailable. App enabled-input-method entry is preserved/added only as needed.
3. Build/sign standalone helper; run policy tests. Independent review focused on vendor shutdown survival, permission scope, user-switch preservation and merge races.
4. Install and grant requested permission; simulate VoxKey force-stop then restore. Test the actual firmware power-off/on path and observe that the helper survives, IME and key service return, Aerial registration is preserved. User only needs physical power cycle when prepared. Validate normal IME switching is not fought; restore VoxKey at finish.
5. Deliver APK, source, instructions and verified scope. No GitHub publication unless requested.