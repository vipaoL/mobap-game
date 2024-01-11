<p align="center">
  <img src="https://user-images.githubusercontent.com/59665125/182234781-e60b3a3a-1b6f-44e6-84b2-d21a12caa023.png"/>
  <br>
</p>


# About
J2ME game on emini physics engine
- Endless gameplay and procedural generation (random combination of predefined and custom structures)
- Supports touch screen and screen rotation, is playable on almost any screen size
- Custom structures: you can create create your custom structures using [this](https://github.com/vipaoL/mobapp-game-editor-j2me) editor and load them into the game ("Ext Structs" button in the main menu). World generator will use them for generating world. Structures should be saved to a folder named as "MGStructs" in the root of any drive (E:/ for example). You can also put MGStructs folder into "other", graphics or photos folder, if your phone doesn't allow creating folders in the root.
- Custom levels (can be created through emini world designer. Some examples are pinned with first release. Game tries to find "Levels" folder with your levels in: [root] (e.g. c:/Levels), [root]/other and in your graphics and photos folders
- Debug menu: Some features for developing, unstable features, features which are added just for a joke.

Tested on: emulator from Sun WTK 2.5.2; Nokia 6303c, N8, E6, 308, phoneME/N900; Samsung C3200, B320; SE W580i, Z550i; J2ME Loader.

![Gameplay](https://user-images.githubusercontent.com/59665125/170510578-16867f63-9968-4163-9282-d138356c4738.png)
![About screen](https://user-images.githubusercontent.com/59665125/170510649-ac57e0da-7374-4ada-bf7d-33956d262575.png)
![Debug menu](https://user-images.githubusercontent.com/59665125/170510660-60e9d6e1-d99e-4bc7-8931-ed5b952cd5ab.png)
![Main menu](https://user-images.githubusercontent.com/59665125/166309387-667bae51-8be3-45fe-a087-62cd5a91de9d.png)


# Build (linux)
Install dependencies:
```
sudo apt-get install gcc-multilib libxt6:i386
```
Build:
```
git clone https://github.com/vipaoL/mobap-game.git  
cd mobap-game  
git pull && chmod +x ./build.sh && ./build.sh
```
