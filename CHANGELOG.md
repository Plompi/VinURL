Version: 1.7.0
- bugfix: when the url screen was resized, it didn't save the newly set parameters when closed
- bugfix: active download processes are now terminated when the corresponding music record is removed from the jukebox
- bugfix: starting the same download multiple times caused issues. Now if the same download is requested, it gets canceled
- enhancement: partially downloaded sounds now get deleted
- enhancement: switched to another ffmpeg/ffprobe [repository](https://github.com/eugeneware/ffmpeg-static) for better compatibility
- enhancement: restructured vinurl folder
- code: refactored code to comply with java conventions