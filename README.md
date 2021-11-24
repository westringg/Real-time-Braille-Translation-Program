# Real-time-Braille-Translation-Program
## Project Description
A real-time braille translation program which helps the blind using Keyless Screens(or reading any products with text without braille notation). They can get specific information at where they're currently pointing with their finger. The information can be displayed as braille on Dot Watch or spoken to the user via their smartphones.

## UML Diagram and Code Structure

![uml diagram](https://user-images.githubusercontent.com/68358806/142745130-bb669856-b11a-4d9e-9ae8-d2193b5e7685.jpg)

Thread is used to alleviate the delay of real-time translation due to the time consumption of interacting with OCR API and SMS API. (It was not able to communicate directly with Dot Watch at the moment-not accessible to its internal system of notification. Thus, I sent the text retrieved from OCR API to Dot Watch so that it appears on Dot Watch as the content of message it received.)
The main thread runs the OpencvOcrExample class, while the sub thread runs UpdateLatestImages class to continuously update the latest image taken from the camera.

This program has applied for a patent.
