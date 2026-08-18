# MoboSafe Pocket Dashcam - Tier 1 Starter

Minimal Android (Kotlin) app that streams the back camera + microphone audio
to the MoboSafe RTMP server using the RootEncoder library. This covers **Tier 1**
(45 pts: 30 video + 15 audio). Get this working and submitted first, then
extend toward Tier 2/3 if time remains.

## How to open

1. Open Android Studio → **Open** → select this `DashcamApp` folder (not a file inside it).
2. Let Gradle sync. Android Studio will download the Gradle 8.4 distribution
   automatically (needs internet) - this can take a few minutes on first run.
3. Connect your Android phone via USB with USB Debugging enabled, select it
   as the run target, and hit Run (the green triangle).

## Before you run

- Type your roll number into the text field at the top of the app screen
  **exactly as printed on your ID, uppercase, no spaces** (the app
  auto-uppercases what you type, but double check it).
- Grant camera + microphone permission when prompted.
- Tap **Start Streaming**.
- Open `http://15.207.177.194:8081/web/player.html` on a laptop/phone browser,
  enter your roll number, and press **Unmute** (viewer starts muted).

## If the RootEncoder API doesn't match exactly

RootEncoder's method names have shifted slightly across versions (e.g. some
versions use `ConnectChecker` instead of `ConnectCheckerRtmp`, or
`RtmpCamera1` instead of `RtmpCamera2`). If you get a compile error:

1. Open the dependency line in `app/build.gradle` and check the latest
   release tag at https://github.com/pedroSG94/RootEncoder
2. Look at the "Rtmp example" in that repo's `app` module for the exact
   current method signatures - the *flow* (prepareAudio → prepareVideo →
   startStream) stays the same, only names shift slightly.

## Extending to Tier 2 (both cameras)

True concurrent dual-camera capture needs `Camera2` multi-camera API support
(not all phones have it) via two separate encoder/publisher pipelines, one
publishing to `_front` and one to `_back`. Fastest path under time pressure:
run two `RtmpCamera2`-like pipelines bound to two `OpenGlView`s stacked in
a split-screen layout, each prepared for a different physical camera ID.
If true concurrency isn't working, a simpler fallback for partial credit is
a manual toggle button that switches which camera is actively streaming to
`_front`, while composing both previews on screen.

## Extending to Tier 3 (stability)

- Wrap `startStream` in retry logic inside `onConnectionFailedRtmp` to
  auto-reconnect.
- Move streaming into a foreground `Service` with a persistent notification
  so it survives screen lock / backgrounding (plain Activity-based streaming
  will pause when the screen locks).

## Submission

RTMP push URL:
```
rtmp://15.207.177.194:1936/hackathon/{ROLLNO}_front
```

Announce your working run to the organisers as soon as Tier 1 is confirmed
live in the viewer - submission order affects ranking per the brief.
