# F5 integrated release polish

User explicitly requests concise in-app wording and lower disk usage before publishing. Keep recognizer/model accuracy, toolbar, local/cloud choice, credentials and launcher behavior intact.

1. Replace model extraction with AssetManager-backed native model loading. Verify pinned model/token length and SHA256 once per APK update, and fail closed on mismatch. Only after successful native initialization, delete the old known private model-copy files; do not remove settings, keystore entries or unrelated files. Add pure Java stream-integrity regression checks, preserving the existing tests.
2. Shorten main, cloud configuration and recovery status/help text while retaining meaningful actions and actionable errors. Keep trusted-network guidance and configuration fields.
3. Fix late duplicate SCREEN_ON broadcasts overwriting a completed recovery status. Existing real-device wake verified settings restored but status was stale. Add duplicate/pending policy checks and expose safe pending state diagnostics.
4. Rebuild, install over integrated package preserving config. Verify model integrity, direct asset initialization, local input and migrated cloud config. Measure only known model-copy disk use before/after; record APK size separately. Repeat normal remote off/on acceptance with final binary.
5. On success remove old app/helper, verify only one VoxKey, default IME/key service and Aerial service preserved. Package safe source/APKs/checksums and publish requested GitHub release after final checks. Sony remains untouched.
