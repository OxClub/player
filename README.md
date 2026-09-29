# OxPlayer — starter media player (libVLC + Jetpack Compose)

<p align="center">
  <img src="docs/images/oxplayer_icon.png" width="160" alt="OxPlayer app icon" />
</p>

Ye ek naya, alag Android project hai (OxFiles se separate), jisme ek advanced
local media player ka foundation hai.

## App icon
`docs/images/oxplayer_icon.png` — blue background wala version, isko seedha
`app/src/main/res/mipmap-*` folders mein `ic_launcher.png` ke roop mein daal
sakte ho (Android Studio ka Image Asset Studio use karke sab densities
auto-generate ho jayengi). Original (black background) version bhi
`docs/images/oxplayer_icon_original.png` mein rakha hai reference ke liye.

## Isme kya hai
- **PlayerEngine.kt** — libVLC MediaPlayer ka wrapper. Koi bhi format/codec
  chalata hai, aur "play as audio" mode (video track disable karke sirf audio
  chalana) support karta hai.
- **MediaScanner.kt** — Android ke MediaStore se device ke sab video/audio
  files fetch karta hai (scoped storage safe, koi manual file-walk nahi).
- **LibraryScreen.kt** — file list UI, runtime permission handling ke saath.
- **PlayerScreen.kt** — video surface + gesture controls:
  - Right-side vertical drag = volume
  - Left-side vertical drag = brightness
  - Horizontal drag = seek
  - Double-tap left/right = -10s / +10s
  - Pinch = zoom
  - Chip button = video/audio-only toggle
- **OxPlayerApp.kt** — ek hi LibVLC instance poore app ke liye (har screen
  par naya instance banana costly hai).

## GitHub Actions se build
Do workflows already add kiye hain (`.github/workflows/`):
- **build.yml** — har push/PR par debug APK banata hai, artifact ke roop mein upload hota hai
- **release.yml** — jab aap `v1.0.0` jaisa tag push karoge, signed release APK banega aur GitHub Release mein attach ho jayega

**Repo secrets set karne honge** (Settings → Secrets and variables → Actions), OxFiles jaisa hi pattern:
- `OX_KEYSTORE_BASE64` — `base64 keystore.jks | pbcopy` (ya Termux mein `base64 -w0 keystore.jks`) se banaya hua string
- `OX_KEYSTORE_PASSWORD`
- `OX_KEY_ALIAS`
- `OX_KEY_PASSWORD`

Agar aap OxFiles ka wahi keystore reuse karna chahte ho, to wahi secret values yahan bhi daal sakte ho — bas naya repo alag hai isliye secrets bhi alag se add karne padenge (secrets repo-specific hote hain, automatically share nahi hote).

**Note:** Is repo mein `gradlew` wrapper script/jar nahi hai (offline environment mein generate nahi ho saka). Workflows isliye seedha `gradle` command use karte hain Gradle ke official setup action ke through — kaam karega bina wrapper ke bhi. Jab aap Android Studio mein project khologe, wo khud wrapper generate/fix kar dega local development ke liye.

## Android 16 (API 36) support
- `compileSdk`/`targetSdk` ab **36** hain, aur usske liye AGP **8.9.1+** aur
  Gradle **8.11.1+** use ho raha hai (Google ka apna minimum requirement —
  CI workflows mein bhi Gradle 8.11.1 pin kiya hua hai).
- **Zaroori caveat — libVLC ka 16 KB page size issue:** Android 15+ se
  Google Play ko native libraries (`.so` files) **16 KB memory page size**
  ke liye aligned chahiye, warna kuch naye devices par app crash ya install
  fail ho sakta hai. `libvlc-all:3.6.0` (jo hum use kar rahe hain) is fix ke
  saath release nahi hua tha — VideoLAN team par ye known open issue hai.
  Build/publish se pehle:
  1. [Maven Central par libvlc-all ke latest versions check karo](https://central.sonatype.com/artifact/org.videolan.android/libvlc-all) —
     agar koi naya patch ya stable 4.x release 16 KB-aligned mila to
     `app/build.gradle.kts` mein version bump kar do.
  2. Google ka apna script (`check_elf_alignment.sh`, developer.android.com/guide/practices/page-sizes)
     release APK par chala kar verify karo ki `libvlc.so`/`libvlcjni.so`
     ALIGNED dikha rahe hain.
  3. Jab tak fix wala version na mile, app aaj ke zyadatar (4 KB page size
     wale) devices par bilkul normal chalega — sirf naye 16 KB page size
     wale devices par risk hai.

## Setup
1. Android Studio mein naya "Empty Compose Activity" project banao (ya is
   folder ko root leke Gradle wrapper generate karo: `gradle wrapper`).
2. Is `app/` folder ke andar ke files apne project ke `app/` folder mein
   copy karo, package structure match karte hue.
3. `settings.gradle.kts` mein libVLC ke liye Maven Central already default
   hota hai, extra repo add karne ki zarurat nahi.
4. Sync Gradle, run karo. Emulator par video codec kabhi slow chal sakta
   hai — real device par test karna better hai.

## Jo abhi missing hai (aapne scope se hata diya)
- Cloud sync, AI subtitles, cast/streaming — jaan-bujh kar chhoda gaya hai.

## Next steps jab aap ready ho
- **Playlist/queue:** ek simple list + "next/previous" MediaPlayer par.
- **Background playback:** foreground `MediaSessionService` add karna, taaki
  audio-only mode screen off hone par bhi chale.
- **Subtitle loading:** libVLC `.srt`/`.ass` files khud detect kar leta hai
  agar wo video file ke sath same folder mein ho, same naam se.
- **Thumbnails:** MediaStore se `MediaStore.Video.Thumbnails` ya Coil/Glide
  se video frame thumbnail nikal sakte ho library grid ke liye.
- **Custom UI polish:** ye starter functional hai lekin visually minimal —
  isko VLC se "advance" dikhane ke liye apna design system (colors, icons,
  animations) is par layer karna hoga.

Batao jab in mein se koi next step lena ho, main uska code bana dunga.
