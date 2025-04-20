package com.vipaol.mobapp.android;

import static mobileapplication3.platform.Platform.SDK_INT;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsProvider;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsContract.Root;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import mobileapplication3.editor.EditorSettings;
import mobileapplication3.platform.Platform;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class MobappDocumentsProvider extends DocumentsProvider {
    private static final String[] DEFAULT_ROOT_PROJECTION = new String[]{
            Root.COLUMN_ROOT_ID,
            Root.COLUMN_FLAGS,
            Root.COLUMN_ICON,
            Root.COLUMN_TITLE,
            Root.COLUMN_SUMMARY,
            Root.COLUMN_DOCUMENT_ID,
            Root.COLUMN_AVAILABLE_BYTES
    };

    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_LAST_MODIFIED,
            Document.COLUMN_FLAGS,
            Document.COLUMN_SIZE
    };

    private static final String ROOT_NAME = "root";
    private File ROOT;

    @Override
    public boolean onCreate() {
        Platform.init(getContext());
        ROOT = new File(EditorSettings.getGameFolderPath());
        return true;
    }

    @Override
    public Cursor queryRoots(String[] projection) {
        MatrixCursor roots = new MatrixCursor(projection != null ? projection : DEFAULT_ROOT_PROJECTION);
        if (ROOT == null) {
            return roots;
        }

        MatrixCursor.RowBuilder row = roots.newRow();
        row.add(Root.COLUMN_ROOT_ID, ROOT_NAME);
        row.add(Root.COLUMN_SUMMARY, "Custom game content");
        row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_CREATE | Root.FLAG_SUPPORTS_SEARCH);
        row.add(Root.COLUMN_TITLE, "mobapp-game");
        row.add(Root.COLUMN_DOCUMENT_ID, getDocIdForFile(ROOT));
        row.add(Root.COLUMN_AVAILABLE_BYTES, ROOT.getFreeSpace());
        row.add(Root.COLUMN_ICON, R.mipmap.ic_launcher);

        return roots;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        if (projection == null) {
            projection = DEFAULT_DOCUMENT_PROJECTION;
        }
        final MatrixCursor result = new MatrixCursor(projection);
        includeFile(result, documentId, null);
        return result;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION);
        final File parent = getFileForDocId(parentDocumentId);
        for (File file : parent.listFiles()) {
            includeFile(result, null, file);
        }
        return result;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal) throws FileNotFoundException {
        return ParcelFileDescriptor.open(getFileForDocId(documentId), ParcelFileDescriptor.parseMode(mode));
    }

    @Override
    public String createDocument(String documentId, String mimeType, String displayName) throws FileNotFoundException {
        File parent = getFileForDocId(documentId);
        File file = new File(parent.getPath(), displayName);
        boolean success = false;
        try {
            if (file.createNewFile()) {
                if (file.setWritable(true) && file.setReadable(true)) {
                    success = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        }
        if (!success) {
            throw new FileNotFoundException("Failed to create document: \"" + displayName + "\" (id: \"" + documentId + "\")");
        }
        return getDocIdForFile(file);
    }

    @Override
    public void deleteDocument(String documentId) throws FileNotFoundException {
        File file = getFileForDocId(documentId);
        if (!file.delete()) {
            throw new FileNotFoundException("Failed to delete document (id: \"" + documentId + "\")");
        }
    }

    @Override
    public String renameDocument(String documentId, String displayName) throws FileNotFoundException {
        if (displayName == null) {
            throw new IllegalArgumentException("displayName is null");
        }

        // Create the destination file in the same directory as the source file
        File sourceFile = getFileForDocId(documentId);
        File sourceParentFile = sourceFile.getParentFile();
        if (sourceParentFile == null) {
            throw new FileNotFoundException("Failed to rename document. File has no parent.");
        }
        File destFile = new File(sourceParentFile.getPath(), displayName);

        // Try to do the rename
        try {
            boolean renameSucceeded = sourceFile.renameTo(destFile);
            if (!renameSucceeded) {
                throw new FileNotFoundException("Failed to rename document. Rename failed.");
            }
        } catch (Exception e) {
            throw new FileNotFoundException("Failed to rename document. Error: " + e.getMessage());
        }

        return getDocIdForFile(destFile);
    }

    private void includeFile(MatrixCursor result, String docId, File file) throws FileNotFoundException {
        if (file != null) {
            docId = getDocIdForFile(file);
        } else {
            file = getFileForDocId(docId);
        }

        int flags = 0;

        if (file.isDirectory()) {
            flags |= Document.FLAG_DIR_SUPPORTS_CREATE;
        } else {
            flags |= Document.FLAG_SUPPORTS_WRITE;
            flags |= Document.FLAG_SUPPORTS_DELETE;
            if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                flags |= Document.FLAG_SUPPORTS_RENAME;
            }
        }

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_DISPLAY_NAME, file.getName());
        row.add(Document.COLUMN_SIZE, file.length());
        row.add(Document.COLUMN_MIME_TYPE, getTypeForFile(file));
        row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified());
        row.add(Document.COLUMN_FLAGS, flags);
    }

    private String getDocIdForFile(File file) {
        String path = file.getAbsolutePath();

        final String rootPath = ROOT.getPath();
        if (rootPath.equals(path)) {
            path = "";
        } else if (rootPath.endsWith("/")) {
            path = path.substring(rootPath.length());
        } else {
            path = path.substring(rootPath.length() + 1);
        }

        return "root" + ':' + path;
    }

    private File getFileForDocId(String docId) throws FileNotFoundException {
        File target = ROOT;
        if (docId.equals(ROOT_NAME)) {
            return target;
        }
        final int splitIndex = docId.indexOf(':', 1);
        if (splitIndex < 0) {
            throw new FileNotFoundException("Missing root: " + docId);
        } else {
            final String path = docId.substring(splitIndex + 1);
            target = new File(target, path);
            if (!target.exists()) {
                throw new FileNotFoundException("File \"" + target + "\" not found (id: \"" + docId + "\")");
            }
            return target;
        }
    }

    private static String getTypeForFile(File file) {
        if (file.isDirectory()) {
            return Document.MIME_TYPE_DIR;
        } else {
            return getTypeForName(file.getName());
        }
    }

    private static String getTypeForName(String name) {
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = name.substring(lastDot + 1);
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }
        return "application/octet-stream";
    }
}
