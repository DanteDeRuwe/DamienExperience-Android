# Damien experience androidapp 

The android application for the damien experience project.

## Installation
Clone this repository and import into **Android Studio**
```bash
git clone git@github.com:HoGent-Projecten3/projecten3-2021-android-d4.git
```
## Downloads
APK downloads for the application can be found [here](https://damienexperience.vankerkvoorde.me/)  
The app was also submitted to the Google Play Store (and approved). Currently it can only be downloaded if you are submitted to the Alpha testing phase.

### Credetials
```
email: google@test.com
password: P@ssword1
```

## Configuration
### Keystores:
Create `app/keystore.gradle` with the following info:
```gradle
ext.key_alias='...'
ext.key_password='...'
ext.store_password='...'
```
And place both keystores under `app/keystores/` directory:
- `playstore.keystore`
- `stage.keystore`


## Build variants
Use the Android Studio *Build Variants* button to choose between **production** and **staging** flavors combined with debug and release build types


## Generating signed APK
From Android Studio:
1. ***Build*** menu
2. ***Generate Signed APK...***
3. Fill in the keystore information *(you only need to do this once manually and then let Android Studio remember it)*

## Maintainers
This project is mantained by:
* [Simon Bettens](https://github.com/simonbettens)
* [Ruben Naudts](https://github.com/NaudtsRuben)
* [Dante De Ruwe](https://github.com/dantederuwe-hogent)
* [Jonas Haenebalcke](https://github.com/JonasHaenebalcke)
* [Lucas Van der Haegen](https://github.com/LucasVanderHaegen)
* [Jordy Van Kerkvoorde](https://github.com/JordyVanKerkvoorde)


## Contributing

1. Fork it
2. Create your feature branch (git checkout -b my-new-feature)
3. Commit your changes (git commit -m 'Add some feature')
4. Run the linter (ruby lint.rb').
5. Push your branch (git push origin my-new-feature)
6. Create a new Pull Request

