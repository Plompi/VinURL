# About

VinURL is an exciting fork of the popular [URL Music Discs](https://modrinth.com/mod/url-music-discs) mod by [Hoofer](https://github.com/HooferDevelops). 
With the Custom Music Disc, players can download and play any cloud-hosted audio file directly in Minecraft or download
them from YouTube or other supported [Sites](https://github.com/yt-dlp/yt-dlp/blob/master/supportedsites.md), all via a URL, creating a personalized musical experience like never before.

# Usage

| **Enter URL**                            | **Use Disc**                                |
|------------------------------------------|---------------------------------------------|
| 1. Rightclick the Custom Music Disc      | 1. Insert the Disc into a Jukebox           |
| 2. Enter a valid URL                     | 2. Wait until the download completes        |
| 3. Press Enter or Escape to set the URL  | 3. Enjoy the music :)                       |
| ![disc_usage](docs/assets/enter_url.gif) | ![disc_insert](docs/assets/insert_disc.gif) |


# Commands

`/vinurl update` manually checks/installs updates for all the external executables that are required for the mod \
`/vinurl delete` deletes all downloaded audio files that are not being used at that moment for the player\
`/vinurl config` to open the config-screen \
`/vinurl set <URL>` writes the specified URL to the held Custom Music Disc (can be used if GUI doesnt work)

# Recipe

![DiscRecipe](https://cdn.modrinth.com/data/cached_images/92d30d4bd4cc1aa6a1294d50d2a0127b568380b5.png) \
it's shapeless, no need to align the colors with the shown image

# Config

various client-side settings can be made. The config-screen can be accessed through [Mod Menu](https://modrinth.com/mod/modmenu) or the slash command (see
Commands Section)

# Disclaimer

This mod automatically downloads the following external programs in the background to ensure proper functionality. By using this mod, you agree to the automated download and use of these external programs.

- [yt-dlp](https://github.com/yt-dlp/yt-dlp) ([Unlicense](https://github.com/yt-dlp/yt-dlp/blob/master/LICENSE)): Downloads media from supported providers.
- [FFmpeg binaries](https://github.com/Tyrrrz/FFmpegBin) ([MIT](https://github.com/Tyrrrz/FFmpegBin/blob/master/license.txt)): Extracts audio from media files.

Please note that it is the sole responsibility of each user to comply with applicable copyright laws and the terms of service of any music provider when using this mod. The developers of this mod do not assume any liability for unauthorized use or violations of such laws and regulations.