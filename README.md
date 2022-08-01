<p align="center">
  <img src="https://user-images.githubusercontent.com/59665125/182234781-e60b3a3a-1b6f-44e6-84b2-d21a12caa023.png"/>
  <br>
</p>


# About
J2ME game on emini physics engine
- Endless gameplay and procedural generation (random combination of predefined and custom structures)
- Custom structures: you can create create your custom structures using [this](https://github.com/vipaoL/mobap-game-editor-android) editor and load them into the game. Folder for structures is named 'MGStructs' and can be in root, in graphics folder, in photos folder or in [root]/other
- Custom levels (can be created through emini world designer. Some examples were pinned with first release. Game tries to find "Levels" folder with your levels in: [root] (e.g. c:/Levels), [root]/other and in your graphics and photos folders
- Debug menu: Here i'm adding new unstable features before i can say that they are stable. But there are some things that may be interesting
- Touch interface and screen rotation are supported
- Can adapt to any (or almost any) screen size

tested on: default emulator; Nokia 6303c, n8, e6, 308; Samsung c3200, b320; SE w580i, z550i; j2me loader.

![Gameplay](https://user-images.githubusercontent.com/59665125/170510578-16867f63-9968-4163-9282-d138356c4738.png)
![About screen](https://user-images.githubusercontent.com/59665125/170510649-ac57e0da-7374-4ada-bf7d-33956d262575.png)
![Debug menu](https://user-images.githubusercontent.com/59665125/170510660-60e9d6e1-d99e-4bc7-8931-ed5b952cd5ab.png)
![Main menu](https://user-images.githubusercontent.com/59665125/166309387-667bae51-8be3-45fe-a087-62cd5a91de9d.png)


# Build (linux)
Install dependencies:
```
sudo apt-get install gcc-multilib libxt6:i386
```
```
git clone https://github.com/vipaoL/mobap-game.git  
cd mobap-game  
git pull && chmod +x ./build.sh && ./build.sh
```
