Version: 2.0.0
- bugfix: audio files that are already ogg now get correctly converted to mono
- enhancement: added redstone compatibility
- enhancement: added a button to automatically retrieve the song duration of the set url
- enhancement: removed "No Song" hud showing when inserting a custom disc into jukebox
- enhancement: revamped how multiple discs with same url are handled. They now subscribe to the download event
- enhancement: download of a song will only be cancelled if all discs with this song url are extracted
- enhancement: hoppers now dont cancel the downloading process
- breaking change: removed playlist support in favor of vanilla automatic jukeboxes
- breaking change: API stop endpoint now expects a boolean whether to stop the download process