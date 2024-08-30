<p align="center">
  <img src="https://user-images.githubusercontent.com/59665125/182234781-e60b3a3a-1b6f-44e6-84b2-d21a12caa023.png"/>
  <br>
</p>


# About
A crossplatform game on emini physics engine
- Endless gameplay and procedural world generation (random combination of predefined or custom structures)
- Supports touch and keyboard controls
- Adaptive to screen resolution.
- Custom structures: you can create create your custom structures using [the structure editor](https://github.com/vipaoL/mobapp-editor) (it is included into the android version of the game) and load them into the game by pressing the "Ext Structs" button in the game's main menu. The game will use your structures in world generation on a par with the built-in structures. Structures should be saved to a folder named "MGStructs" in the root of any drive (E:/ for example). You can also put MGStructs folder into "other", graphics or photos folder, if your phone doesn't allow creating folders in the root.
- Custom levels which can be created with emini world designer (emini engine is the physics engine the game use). Some example levels are pinned with the first release of the game. You should put the "Levels" folder into: [root] (e.g. c:/Levels), [root]/other and in your graphics and photos folders

![Gameplay](https://user-images.githubusercontent.com/59665125/170510578-16867f63-9968-4163-9282-d138356c4738.png)
![About screen](https://user-images.githubusercontent.com/59665125/170510649-ac57e0da-7374-4ada-bf7d-33956d262575.png)
![Debug menu](https://user-images.githubusercontent.com/59665125/170510660-60e9d6e1-d99e-4bc7-8931-ed5b952cd5ab.png)
![Main menu](https://user-images.githubusercontent.com/59665125/166309387-667bae51-8be3-45fe-a087-62cd5a91de9d.png)


# Build (Ubuntu 20.04)
Install dependencies:
```
sudo apt-get install gcc-multilib libxt6:i386
```
Build:
```
git clone --recurse-submodules https://github.com/vipaoL/mobap-game.git
cd mobap-game
git pull && chmod +x ./build.sh && ./build.sh
```
