Version: 2.2.0
- enhancement: mod is now automatically being published to curseforge
- api: added 2 new api endpoints playFor and stopFor for starting and stopping global custom sounds for a specified user via uuid. Especially useful for portable jukeboxes
- breaking change: changed the 2 existing api endpoints play -> playAt and stop -> stopAt 
- code: moved from yarn to mojang mappings
- code: a lot of refactoring and code improvements
- docs: updated the docs to reflect the new api