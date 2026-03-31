# Good Partner 💕

A personal daily habit tracker for being a better husband.
Built as an Android WebView app — colorful, gamified, with AI-powered task scheduling.

---

## 🚀 How to build your APK via GitHub Actions

Follow these steps **once** and then every push to `main` automatically builds a new APK.

---

### Step 1 — Create your keystore (Windows)

A keystore is a small file that signs your APK so Android trusts it.
You only do this once.

1. Make sure **Java** is installed on your PC
   - Download from https://adoptium.net/ if needed
   - After install, open a new Command Prompt and check: `java -version`

2. Double-click **`generate_keystore.bat`** in this folder
   - Enter a **store password** (remember it!)
   - Enter your name, org etc. (can be anything)
   - Enter a **key password** (can be the same as store password)
   - It will create `goodpartner.jks` and `goodpartner_base64.txt`

> 💾 **Back up `goodpartner.jks` somewhere safe** (Google Drive, etc.)
> If you lose it you can't update the app on your phone.

---

### Step 2 — Push this project to GitHub

1. Go to https://github.com/new and create a **private** repo called `GoodPartner`
2. In Command Prompt inside this folder:

```cmd
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/GoodPartner.git
git push -u origin main
```

---

### Step 3 — Add your 4 GitHub Secrets

1. Go to your repo on GitHub
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret** and add each of these:

| Secret name | Value |
|---|---|
| `KEYSTORE_BASE64` | Full contents of `goodpartner_base64.txt` |
| `KEYSTORE_PASSWORD` | The store password you set in Step 1 |
| `KEY_ALIAS` | `goodpartner` |
| `KEY_PASSWORD` | The key password you set in Step 1 |

---

### Step 4 — Trigger your first build

The build triggers automatically on every push to `main`.

To trigger it manually:
1. Go to your repo → **Actions** tab
2. Click **Build Signed APK** in the left sidebar
3. Click **Run workflow** → **Run workflow**

Build takes about 3–5 minutes.

---

### Step 5 — Download and install the APK

1. When the build finishes, click on the run to open it
2. Scroll to the bottom — click **Artifacts → GoodPartner-APK**
3. Download and unzip it — you'll get a `.apk` file
4. **Transfer to your phone** (AirDrop, Google Drive, USB, or email it to yourself)
5. On your Android phone:
   - Go to **Settings → Security → Install unknown apps**
   - Allow your **Files** app (or whatever you use to open the APK)
6. Tap the APK file → **Install** → **Open** 🎉

---

## 🔁 Updating the app

Whenever you want to update the app:

1. Edit `app/src/main/assets/index.html` (the whole app lives here)
2. Commit and push to `main`
3. GitHub Actions builds a new APK automatically
4. Download and install over the old version — your data is preserved

---

## 📁 Project structure

```
GoodPartner/
├── .github/workflows/build.yml   ← GitHub Actions build pipeline
├── app/
│   ├── src/main/
│   │   ├── assets/index.html     ← THE ENTIRE APP (edit this!)
│   │   ├── java/com/goodpartner/app/
│   │   │   ├── MainActivity.java      ← WebView host + storage bridge
│   │   │   ├── NotificationReceiver.java
│   │   │   └── BootReceiver.java
│   │   ├── res/                  ← Icons, themes, layouts
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── generate_keystore.bat         ← Run once to create signing key
└── README.md
```

---

## 🔔 Features

- **Daily check-in** with streak tracking
- **AI-powered task scheduling** — fits love tasks around your real schedule
- **Pinned daily tasks** (cat litter etc.) — earn half points
- **Task pool editor** — toggle which tasks the AI can pick
- **2-hour windows** — tasks show exact time slots, late = half points
- **Points & rewards** — redeem for gaming sessions, ordering food, etc.
- **Unlockable themes** — Berry Pop, Ocean Splash, Candy Shop
- **Egg hatching** — level up to hatch rare pets
- **Pet nursery** — customise with accessories, habitats, colour tints
- **Achievement book** — 15 achievements to unlock
- **Daily notification** at 8 AM to check in
