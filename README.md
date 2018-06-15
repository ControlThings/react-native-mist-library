
# react-native-mist-library

## Getting started

`$ npm install react-native-mist-library --save`

Actually, we hve used yarn with RN apps.

### Mostly automatic installation

`$ react-native link react-native-mist-library`

## Mist Api dependency

### iOS 

On iOS, the library is dependent on the libMistApi.a which is produced by the MistApi Xcode project. A pre-compiled binary is included in ios/lib. See README.md in MistApi for instructions on how to update libMistApi.a.

### Android

In order for the rn mist-library to work under Android, you must include the Wish and MistApi aar libraries to the rn app project.

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
  
