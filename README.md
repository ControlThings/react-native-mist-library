
# react-native-mist-library

## Release instructions for new lib versions

Once you have made your changes, you make a release like this:

```sh
npm version patch
npm pack
```
Then copy the react-native-mist-library tgz to web site for distribution

## Getting started with react-native-mist-library in a new project

`$ npm install react-native-mist-library --save`

Actually, we hve used yarn with RN apps.

### Mostly automatic installation

`$ react-native link react-native-mist-library`

## Mist Api dependency

### iOS 

On iOS, the library is dependent on the libMistApi.a which is produced by the MistApi Xcode project. A pre-compiled binary is included in ios/lib. See README.md in MistApi for instructions on how to update libMistApi.a.

### Android

MistLibrary's dependencies are automatically downloaded from a maven repository called Artifactory running at foremost.cto.fi.

Required: Gradle configuration of Artifactory:

1. Log in to Artifactory http://foremost.controlthings.fi:8081
2. In the "Welcome, yourUserName" menu on the top right of the screen, find
   "Edit profile".
3. Write your password on the top of page to unlock...
4. Under Authentication settings you can click on the "eye" icon to see
   the Encrypted password
5. On your computer, create file: `~/.gradle/gradle.properties` and add
   the following:

    artifactory_username=<your Artifactory user name>
    artifactory_password=<your Encrypted Artifactory password>

#### Old information
In order for the rn mist-library to work under Android, you must include the Wish and MistApi aar libraries to the rn app project.


#### Updating WishCore and MistApi under development

When developing MistApi or WishCore you can update to newest versions via artifactory:

MistApi (publish updates):

```sh
.../mist-api-android $ ./gradlew --refresh-dependencies build assembleRelease artifactoryPublish
```

WishCore (publish update):

```sh
.../Wish $ ./gradlew build assembleRelease artifactoryPublish
```

MistLibrary (refresh dependencies, i.e. MistApi and WishCore)

```sh
.../MistLibrary/android $ ./gradlew --refresh-dependencies -x:lint build
```

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-mist-library` and add `RNMistLibrary.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNMistLibrary.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNMistLibraryPackage;` to the imports at the top of the file
  - Add `new RNMistLibraryPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-mist-library'
  	project(':react-native-mist-library').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-mist-library/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-mist-library')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNMistLibrary.sln` in `node_modules/react-native-mist-library/windows/RNMistLibrary.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Mist.Library.RNMistLibrary;` to the usings at the top of the file
  - Add `new RNMistLibraryPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNMistLibrary from 'react-native-mist-library';

// TODO: What to do with the module?
RNMistLibrary;
```
  
