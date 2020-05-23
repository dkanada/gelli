## Gelli

This is a native music player for Android devices that connects to Jellyfin media servers. The code is based on a relatively recent version of Phonograph and was made for personal use, but contributions are welcome! Please open an issue to discuss larger changes before submitting a pull request. I am open to an improved icon if any graphic designers have a good suggestion.

## Features

* Basic library navigation
* Gapless playback
* Sort albums and songs by different fields
* Search media for partial matches
* Media service integration with notification
* Favorites and playlists
* Filter content by library

## Issues

Since this was a small project intended mainly for myself, there are some things I haven't resolved yet. I would appreciate pull requests to fix any of these issues!

* All lists are capped at 100 entries to avoid excessive load times
* Artist sorting isn't available through the API
* Playlists and favorites will not update automatically when changed
* Sorting also requires a reload to take effect
* Batch actions are not yet functional

## Future Plans

I don't currently have plans to add any large features. If I ever find the time, these are some of the items I would potentially include.

* Offline downloads
* Support for other media types
