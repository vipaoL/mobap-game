/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileapplication3.game;

import at.emini.physics2D.UserData;

/**
 *
 * @author vipaol
 */
public class MUserData implements UserData {
        public static final int TYPE_FALLING_PLATFORM = 10;
        public static final int TYPE_ACCELERATOR = 11;
        public static final int TYPE_LEVEL_FINISH = 12;
        public static final int TYPE_TRAMPOLINE = 13;
        public static final int TYPE_LAVA = 14;

        public String string;
        public int i = 1;
        public int bodyType = -1;
        public short[] data = null;
        public int color = 0x00ffff;
        
        public MUserData(int bodyType, short[] data) {
            this.bodyType = bodyType;
            this.data = data;
        }

        public UserData copy() {
            return this;
        }
        
        public UserData createNewUserData(String string, int i) {
            MUserData mUserData = new MUserData(bodyType, data);
            mUserData.i = i;
            mUserData.string = string;
            return mUserData;
        }
    }