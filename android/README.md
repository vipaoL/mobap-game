# Mobapp Game+Editor for Android
<p align="center">
  <img src="https://user-images.githubusercontent.com/59665125/182234781-e60b3a3a-1b6f-44e6-84b2-d21a12caa023.png"/>
  <img src="https://github.com/user-attachments/assets/28afb5f0-6e95-481c-acce-f1abfcdc3239"/>
  <br>
</p>

## Mobapp-Game
A crossplatform game on emini physics engine
- Endless gameplay and procedural world generation (random combination of predefined or custom structures)
- Supports touch and keyboard controls
- Adaptive to screen resolution.
- Custom structures: you can create create your custom structures using [the structure editor](https://github.com/vipaoL/mobap-game/tree/android#mobapp-editor) (it is included into the android version of the game) and load them into the game by pressing the "Ext Structs" button in the game's main menu. The game will use your structures in world generation on a par with the built-in structures. Structures should be saved to a folder named "MGStructs" in the root of any drive (E:/ for example). You can also put MGStructs folder into "other", graphics or photos folder, if your phone doesn't allow creating folders in the root.
- Custom levels which can be created with emini world designer (emini engine is the physics engine the game use). Some example levels are pinned with the first release of the game. You can put the "Levels" folder into Android/data/com.vipaol.mobapp.game.android/files/

![gameplay](https://github.com/user-attachments/assets/94910dee-de2f-48bd-bb28-acb48cc83cb4)
![game over](https://github.com/user-attachments/assets/a5bde1fc-9795-4451-a23a-b8ebece28ed0)

## Mobapp-Editor
The editor lets you to create structures for mobapp-game. The structures can be compared to parts of an endless puzzle, and the world of mobapp-game is just a random combination of them. You can create your own structures, load them into the game and the game will use them to generate the world on a par with the built-in ones.
![screenshot](https://github.com/user-attachments/assets/1acea31d-11a8-4459-85d2-48280e605d13)
![screenshot](https://github.com/user-attachments/assets/2f4bad35-7439-4759-a75e-a1b8e57170d8)
![screenshot](https://github.com/user-attachments/assets/cab8a687-f3a2-42e0-af7e-ef54bb26d33f)
![screenshot](https://github.com/user-attachments/assets/cc806607-d5c2-4d86-b7e1-1f225501afce)
![screenshot](https://github.com/user-attachments/assets/4eb04eee-ffd0-478a-a4de-4758df09687b)
![screenshot](https://github.com/user-attachments/assets/e2f6e624-3d68-4375-9735-d4b1aa24a627)

# Build
```
git clone https://github.com/vipaoL/mobap-game.git --branch android --recurse-submodules && cd mobap-game/android/
ANDROID_HOME=~/Android/Sdk/ ./gradlew build
```
