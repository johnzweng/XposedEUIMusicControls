# Xposed Module "EUI MusicControls"


### What's this:

This is a module for the [Xposed Framework](http://repo.xposed.info/). You need to have the Xposed framework (which is not made by me) installed on your phone. You may need to have root to be able to install the Xposed framework (but no root is needed for this Xposed module).


### What this module can do for you:

This module overrides the function of the three media control buttons in the EUI control center: **Previous, Play, Next**.

Normally these buttons only control the EUI Media Player. This Xposed module changes this behaviour, so that these buttons behave exactly like the Media Control buttons on any headset and broadcasts instead key events with these keycodes:

- `KEYCODE_MEDIA_PREVIOUS`
- `KEYCODE_MEDIA_PLAY_PAUSE`
- `KEYCODE_MEDIA_NEXT`

So any media app that supports the buttons on an external headset, should also be able to react to the button events from the EUI Control Center.


### Supported EUI Version:

This module was developed and tested on the following device:

- Device **LeEco LePro 3 (LEX720)**
- Firmware version: **5.8.018S**
- Build-ID: **WAXCNFN5801811012S**

It may or may not work on other EUI versions, so just give it a try and log into Xposed logs. If everything is working as expected you should see these lines in there:

```
EUI MusicControls: We are in com.android.systemui application. Will try to place method hooks for the music Play buttons in EUI Control Center. :)
EUI MusicControls: successfully replaced method handlePlayPause() :)
EUI MusicControls: successfully replaced method handleNext() :)
EUI MusicControls: successfully replaced method handlePrevious() :)
```

