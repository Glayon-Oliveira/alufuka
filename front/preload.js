const { contextBridge, ipcRenderer } = require("electron");


contextBridge.exposeInMainWorld("api", {

    chooseJavaFile: () => {
        return ipcRenderer.invoke("choose-java-file");
    },

    chooseDirectory: () => {
        return ipcRenderer.invoke("choose-directory");
    },

    readJava: (filePath) => {
        return ipcRenderer.invoke("read-java", filePath);
    },

    writeJavadoc: (filePath, path, content, types) => {
        return ipcRenderer.invoke("write-javadoc", filePath, path, content, types);
    }

});