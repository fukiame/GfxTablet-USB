What is GfxTablet-USB?
==================

GfxTablet-USB use USB instead of UDP, so it will be more user-friendly and will probably reduce the delays.

It consists of three components:

* the GfxTablet Android app
* the daemon for Android
* the input driver for your PC


License
-------

GfxTablet is licensed under The MIT License.

Initial contributor: Ricki Hirner / powered by [bitfire web engineering](https://www.bitfire.at) / [gimpusers.com](http://www.gimpusers.com) / Misaka 19465


Features
--------

* Pressure sensitivity supported
* Size of canvas will be detected and sent to the client
* Option for ignoring events that are not triggered by a stylus pen:
  so you can lay your hand on the tablet and draw with the pen.


Requirements
------------

* App: Any device with Android 4.0+ and touch screen
* Driver: Linux with uinput kernel module (included in modern versions of Fedora, Ubuntu etc.)

If you use Xorg (you probably do):

* Xorg-evdev module loaded and configured – probably on by default, but if it doesn't work, you may
  need to activate the module.


Installation
============

Part 1: uinput driver
---------------------

On your PC, either download the binary from [release](https://github.com/misaka19465/GfxTablet-USB/releases) (don't forget to `chmod a+x` it):

or compile it yourself (don't be afraid, it's only one file)

1. Clone the repository:
   `git clone git://github.com/misaka19465/GfxTablet-USB.git`
2. Install gcc, make and linux kernel header includes (`kernel-headers` on Fedora)
3. `cd GfxTablet-USB/driver-uinput; make`

Then, run the binary. The driver runs in user-mode, so it doesn't need any special privileges.
However, it needs access to `/dev/uinput`. If your distribution doesn't create a group for
uinput access, you'll need to do it yourself or just run the driver as root:

`sudo ./networktablet`

Then you should see a status message saying the driver is ready. If you do `xinput list` in a separate
terminal, should show a "Network Tablet" device.

You can start and stop (Ctrl+C) the Network Tablet at any time, but please be aware that applications
which use the device may be confused by that and could crash.

`networktablet` will display a status line for every touch/motion event it receives.


Part 2: App
-----------

You can either

1. compile the app from the source code in the Github repository
2. [download it from releases](https://github.com/misaka19465/GfxTablet-USB/releases)

After installing, run once first before running uinput driver.


Part 3: Use it
--------------

Now you can use your tablet as an input device in every Linux application (including X.org
applications). For instance, when networktablet is running, GIMP should have a "Network Tablet"
entry in "Edit / Input Devices". Set its mode to "Screen" and it's ready to use.


Frequently Asked Questions
==========================

Using with multiple monitors
----------------------------

If you're using multiple screens, you can assign the Network Tablet device to a specific screen
once it's running (thanks to @symbally and @Evi1M4chine, see https://forums.bitfire.at/topic/82/multi-monitor-problem):

1. Use `xrandr` to identify which monitor you would like to have the stylus picked up on. In this example, `DVI-I-1`
   is the display to assign.
2. Do `xinput map-to-output "$( xinput list --id-only "Network Tablet" )" DVI-I-1`.

Known problems
--------------

* With Gnome 3.16 (as shipped with Fedora 22), [Gnome Shell crashes when using GfxTablet](https://bugzilla.redhat.com/show_bug.cgi?id=1209008).


