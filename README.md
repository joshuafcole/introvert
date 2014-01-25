Introvert
==============
Just a little library that enjoys introspecting into clojurescript.


                                          _   ___...___   _
     _____  __     __  _________  _____  | \ / ~.      \ / | _       _  _____  _____  _________
    |_   _||  \   |  ||___   ___||  __ \ |  \    \      /  || |     | ||  ___||  __ \|___   ___|
      |.|  | . \  |  |    |.|    |.|  \.\ \  \    \    /  / |.|     |.|| |    |.|  \.\   |.|
      | |  |    \ |  |    | |    | |__/ /  \  \    \  /  /  | \     / || |_   | |__/ /   | |
      | |  |  |\ \|  |    | |    | _  _/   /  /    /\ \  \   \ \   / / |  _|  | _  _/    | |
      | |  |  | \    |    | |    | |\ \   /  /    /  \ \  \   \ \ / /  | |    | |\ \     | |
     _|.|_ |  |  \ . |    |.|    |.| \.\ |  /    /    \ \  |   \ . /   | |___ |.| \.\    |.|
    |_____||__|   \__|    |_|    |_|  \_\|_/\  _/     | /\_|    \_/    |_____||_|  \_\   |_|
                                             \___...___/





## Functions
(**->js** _obj_ _[flatten]_) - Converts an arbitrary cljs data structure into a native JS approximation. Capable of handling atoms, circular references, and most other complications that naive implementations tend to barf on. Works best paired with intelligent dev tools (such as those provided by webkit) To serialize the object (e.g. to display it as a string), you will need to flatten it to remove circular references.

## Changelog
* 0.1.1 Adds support for flattening circular references.
* 0.1.0 Initial Release
