
# Arify android app

Arify is an android app that can be used to build and view AR scenes in minutes. You can use the app in conjunction with the website to create a new scene, take photos of a target, edit them and choose which AR model to augment on detection of the image.

The app has been built using ViroAR core. The idea is to be build a POC of an app that enables users to build a basic AR scene and distribute it in minutes without having to download and learn how to use complex AR scene generation tools.

The features supported on the app are:-
 - Capturing and cropping an image and add it to a list of targets.
 - Adding multiple 3D models to load on image detection.
 - Configuring rotation, scale and position offset for each object with respect to the image target.
 - Viewing the built scene in AR by choosing the scene.

## Setup
The repository is an android project. If you wish to install the app without any changes, you can use the apk [here](https://choosealicense.com/licenses/mit/). You can open the respository in Android studio if you want to tweak the app.

The web server with which the app communicates is hosted at "**insert link here**". If you are using a different hosted server, you will have to update the file "config.properties", and set the url for your server for the key **web_base_url**






## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[MIT](https://choosealicense.com/licenses/mit/)

