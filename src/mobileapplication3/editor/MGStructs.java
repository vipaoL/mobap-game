package mobileapplication3.editor;

import java.io.DataInputStream;
import java.io.IOException;

import mobileapplication3.editor.elements.Element;
import mobileapplication3.platform.FileUtils;
import mobileapplication3.platform.Logger;
import mobileapplication3.platform.Utils;

public class MGStructs {
	public static Element[] readMGStruct(String path) {
		Logger.log(path);
        return readMGStruct(FileUtils.fileToDataInputStream(path));
    }

    public static Element[] readMGStruct(DataInputStream dis) {
        try {
            short fileVer = dis.readShort();
            Logger.log("");
            Logger.log("Reading mgstruct v" + fileVer + " file");
            short elementsCount = dis.readShort();
            Logger.log("elements count: " + elementsCount);
            Element[] elements = new Element[elementsCount];
            for (int i = 0; i < elementsCount; i++) {
                elements[i] = readNextElement(dis);
                if (elements[i] == null) {
                    Logger.log("got null. stopping read");
                    break;
                }
            }
            return shrinkArray(elements);
        } catch (NullPointerException ex) {
        	Logger.log("nothing to read (null)");
        	return null;
        } catch (IOException ex) {
            Logger.log(ex);
            return null;
        }
    }

    private static Element[] shrinkArray(Element[] elements) {
        int l = 0;
        while (l < elements.length && elements[l] != null) {
            l++;
        }
        Element[] newArray = new Element[l];
        System.arraycopy(elements, 0, newArray, 0, l);
        return newArray;
    }

    public static Element readNextElement(DataInputStream is) {
        String logLine = "";
        Logger.log(logLine);
        try {
            short id = is.readShort();
            String prevLogLine = logLine;
            logLine += "id" + id;
            Logger.logReplaceLast(prevLogLine, logLine);
            prevLogLine = logLine;
            if (id == 0) {
                Logger.log("id0 is EOF mark. stopping");
                return null;
            }
            Element element = Element.createTypedInstance(id);
            if (element == null) {
                return null;
            }

            int argsCount = element.getArgsCount();
            short[] args = new short[argsCount];
            for (int i = 0; i < argsCount; i++) {
                args[i] = is.readShort();
            }

            logLine += Utils.shortArrayToString(args);
            Logger.logReplaceLast(prevLogLine, logLine);

            element.setArgs(args);
            return element;
        } catch (IOException ex) {
            Logger.log(ex);
            return null;
        }
    }
}
