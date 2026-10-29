# Dangbei family and first-run setup

Request: allow other Dangbei models and guide installation permissions / input-scoped voice takeover. Keep the unified app identity, cloud configuration and Sony backend, no device tests unless requested again.

DevicePolicy: recognize Dangbei manufacturer/brand strings plus legacy DBF5; stop rejecting standard voice keys by exact F5 model. Unknown-brand firmware may explicitly enable the standard backend in setup. Sony never uses that fallback. Standard automatic mode accepts Android voice key codes135/219/231, with paired ownership and a manual code selector; navigation/confirm/Home remain untouched. It still requires a nonpassword editor, visible VoxKey IME and physical key.

Setup: first-open guide for missing requirements, permanent settings entry. Show/read microphone permission, enabled/default IME, accessibility registration/binding, standard takeover toggle and key choice. Use runtime microphone request, system IME settings/picker, accessibility settings with fallback intents. Include a test action, optional recovery permission instructions and app settings fallback. Never silently overwrite other accessibility services. Recovery may be explicitly enabled on recognized Dangbei devices with WRITE_SECURE_SETTINGS; behavior on other firmware is experimental and not guaranteed. Keep Sony routing unchanged.

Tests: expand paired key/session and device recognition checks, setup readiness/step ordering; run entire existing suite, build/sign version112 /1.1.0-preview.2 and inspect packaged manifest. Independent review before delivery. No remote TV/projector operations or old-app uninstall in this task. Deliver APK/source/checksums under outputs; publication separate from local build unless requested.
