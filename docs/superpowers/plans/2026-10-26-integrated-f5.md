# VoxKey F5 integrated 1.1 implementation plan

Goal: one installed VoxKey application providing voice input and the proven F5 power-cycle recovery, preserving the existing model choice and cloud configuration.

Design: new own-namespace package app.codex.f5voice.compat.com.android.integrated matches the F5 vendor cleanup exception already validated by the independent helper. Merge the existing recovery classes into the input-method app, with one launcher activity and a recovery settings page. Keep the original user-facing icon/theme/toolbars. Recovery targets itself and is opt-in, defaults to restoring only at boot/wake; manual ordinary IME switching remains usable within a session.

Migration: update the old app in place with a temporary read-only configuration provider, guarded by a signature permission PLUS exact caller-package and signature verification. It exposes one fixed Bundle schema through Binder, no network/log/file export. New app imports only when it has no existing cloud key and migration is not completed, validates the schema and key-presence flag, re-encrypts using its own Android Keystore, and verifies the saved values before marking migration complete. Full APK contains the exact pinned public model/tokens and verifies them using existing ModelStore; no private app file or signing-key copying. Never remove old app/helper until new app setup and physical tests pass. Before enabling new recovery, stop old helper to avoid opposing targets.

Constraints: F5 only, Sony untouched, Aerial and stock launcher untouched, no Home/desktop tests. Keep original API key off tools/logs/chat/files. Existing signing key is read only for build, never delivered. No automatic GitHub publication in this task.

Tasks:
1. Clone the F5 source into an isolated work directory. Rename Java/manifest identity and migrate the already-reviewed recovery classes, target the new self component IDs, add recovery settings entry and a proper notification icon.
2. Add failing tests for migration ACL and overwrite/schema decisions, adapt existing 64 input tests and 33 recovery tests to new identity. Implement the minimal configuration provider/client and make tests pass.
3. Build bridge update and integrated full APK. Verify cert/model hashes, manifest permissions, caller denial from shell. Review new migration path independently.
4. Install temporary old-app update and new app; verify migration status without printing credentials. Grant only microphone/WSS, configure new IME/key service while preserving Aerial and stock IME. Stop old recovery/helper and old app once migration done. Test voice input and true remote power cycle.
5. On success uninstall obsolete old app/helper (keep local original APK for rollback), read back exact installed package list and active settings. Deliver one full APK, safe source and validation report. If testing fails, restore old app/service settings and helper; preserve all original data.