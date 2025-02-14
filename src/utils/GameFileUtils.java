/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import mobileapplication3.platform.FileUtils;

/**
 *
 * @author vipaol
 */
public class GameFileUtils {
    public static final short[] SUPPORTED_FILE_FORMAT_VERSIONS = {0, 1};

    private GameFileUtils() { }
    
    public static String[] listFilesInAllPlaces(String folderName) {
    	String[] places = FileUtils.getAllPlaces(folderName);
    	String[][] listTmp = new String[places.length][];
    	for (int i = 0; i < places.length; i++) {
			try {
				listTmp[i] = FileUtils.list(places[i]);
				for (int j = 0; j < listTmp[i].length; j++) {
					listTmp[i][j] = places[i] + listTmp[i][j];
				}
			} catch (Exception e) {
				e.printStackTrace();
				listTmp[i] = new String[0];
			}
		}
    	int l = 0;
    	for (int i = 0; i < listTmp.length; i++) {
			l += listTmp[i].length;
		}
    	String[] list = new String[l];
    	int c = 0;
    	for (int i = 0; i < listTmp.length; i++) {
			System.arraycopy(listTmp[i], 0, list, c, listTmp[i].length);
			c += listTmp[i].length;
		}
    	return list;
    }
}
