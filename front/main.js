const { app, BrowserWindow, ipcMain, dialog } = require("electron");
const fs = require("fs");
const path = require("path");

const JavaClient = require("./java-client");
const { log } = require("console");
const { files } = require("jszip");
const { spawn } = require("child_process");

let javaClient;

function createWindow() {
    const window = new BrowserWindow({
        width: 900,
        height: 600,

        webPreferences: {
            preload: path.join(__dirname, "preload.js"),
            contextIsolation: true,
            nodeIntegration: false
        }
    });

    if(!app.isPackaged) {
        window.loadURL("http://localhost:5173");
    }else {
        window.loadFile(path.join(
            process.resourcesPath,
            "dist", 
            "index.html"
        ))
    }
}

function findJavaFiles(directory) {
    const result = []

    for(const entry of fs.readdirSync(directory, {withFileTypes: true})) {
        const fullPath = path.join(directory, entry.name);

        if(entry.isDirectory()) {
            result.push(...findJavaFiles(fullPath))
        }else if(entry.isFile() && entry.name.endsWith(".java")) {
            result.push(fullPath)
        }
    }

    return result
}

function findAlufukaExecutable(binPath) {
    const files = fs.readdirSync(binPath);

    const file = files.find(name => {
        const parsed = path.parse(name);
        return parsed.name === "alufuka";
    });

    if (!file) {
        throw new Error(`Not found alufuka executable in: ${binPath}`);
    }

    return path.join(binPath, file);
}

ipcMain.handle("choose-java-file", async () => {
    const result = await dialog.showOpenDialog({
        properties: ["openFile"],

        filters: [
            {
                name: "Java files",
                extensions: ["java"]
            }
        ]
    });

    if (result.canceled) {
        return null;
    }

    return result.filePaths[result.filePaths.length-1];
});


ipcMain.handle("choose-directory", async () => {
    const result = await dialog.showOpenDialog({
        properties: ["openDirectory"]
    });

    if (result.canceled) {
        return null;
    }

    return findJavaFiles(result.filePaths[0]);
});

app.whenReady().then(async () => {
    const javaServiceJar = path.join(
        __dirname,
        "java-service",
        "alufuka.jar"
    );

    let serviceProcess = null;

    if(app.isPackaged) {
        const binPath = path.join(process.resourcesPath, "alufuka", "bin")

        const executable = findAlufukaExecutable(binPath)

        serviceProcess = spawn(executable, {cwd: binPath})
    }

    javaClient = new JavaClient(
        javaServiceJar,
        serviceProcess
    );

    await javaClient.start();

    ipcMain.handle(
        "read-java",
        async (event, filePath) => {
            return await javaClient.readJava(filePath);
        }
    );


    ipcMain.handle(
        "write-javadoc",
        async (event, filePath, javaPath, content, types) => {
            return await javaClient.writeJavadoc(filePath, javaPath, content, types);
        }
    );


    createWindow();
});

app.on("window-all-closed", () => {
    javaClient?.stop();

    if (process.platform !== "darwin") {
        app.quit();
    }
});