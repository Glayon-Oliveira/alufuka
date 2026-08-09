const { spawn, execFile } = require("child_process");
const fs = require("fs");
const path = require("path");
const net = require("net");

class JavaClient {

    constructor(javaServiceJar, javaServicePath) {
        this.javaServiceJar = javaServiceJar;
        this.javaServicePath = javaServicePath;
        this.process = null;
        this.port = 5130;
    }

    async start() {
        if (await this.isServiceRunning()) {
            return;
        }

        if (!fs.existsSync(this.javaServiceJar)) {
            await this.buildJavaService();

            const generatedJar = path.join(
                this.javaServicePath,
                "target",
                "alufuka.jar"
            );

            fs.mkdirSync(path.dirname(this.javaServiceJar), {recursive: true});

            fs.copyFileSync(generatedJar, this.javaServiceJar);
        }

        this.process = spawn("java", ["-jar", this.javaServiceJar], {
            stdio: ["ignore", "pipe", "pipe"]
        });

        this.capture_process_out(this.process, "java");

        console.log("Java process spawned, waiting for service...");

        await this.waitForService();
    }

    buildJavaService() {
        return new Promise((resolve, reject) => {
            const process = execFile(
                "mvn",
                ["install"],
                { cwd: this.javaServicePath }
            );

            this.capture_process_out(process, "maven", {resolve, reject});
        });
    }

    isServiceRunning() {
        return new Promise(resolve => {
            const socket = net.createConnection(this.port, "localhost");
            let buffer = "";

            socket.setTimeout(500);

            socket.on("connect", () => {
                socket.write("{}\n");
            });

            socket.on("data", data => {
                buffer += data.toString();

                if(!buffer.endsWith("\n")) {
                    return;
                }

                socket.destroy();

                try{
                    resolve(JSON.parse(buffer.trim()).status === "FAILURE");
                }catch{
                    resolve(false);
                }
            });

            socket.on("timeout", () => {
                socket.destroy();
                resolve(false);
            });

            socket.on("error", () => {
                resolve(false);
            });
        });
    }

    async waitForService() {
        while (!(await this.isServiceRunning())) {
            await new Promise(resolve => setTimeout(resolve, 100));
        }
    }

    send(data) {
        return new Promise((resolve, reject) => {
            const socket = net.createConnection(this.port, "localhost");
            let buffer = "";

            socket.on("connect", () => socket.write(data + "\n"));

            socket.on("data", chunk => {
                buffer += chunk.toString();

                if(buffer.endsWith("\n")) {
                    socket.destroy();

                    try{
                        resolve(JSON.parse(buffer.trim()));
                    }catch(error) {
                        reject(error);
                    }
                }
            });

            socket.on("error", reject);
        });
    }

    readJava(filePath) {
        return this.send(JSON.stringify({
            type: "READER",
            file_path: filePath
        }));
    }

    writeJavadoc(filePath, javaPath, content) {
        return this.send(JSON.stringify({
            type: "JAVADOC_WRITER",
            file_path: filePath,
            path: javaPath,
            content: content
        }));
    }

    stop() {
        if(this.process) {
            this.process.kill();
            this.process = null;
        }
    }

    capture_process_out(process, prefix, {resolve, reject} = {}) {
        process.stdout.on("data", data => {
            data.toString().split("\n").map((m) => {
                console.log(`${prefix ? "["+prefix+"] " : ""}${m}`);
            })            
        });

        process.stderr.on("data", data => {
            data.toString().split("\n").map((m) => {
                console.error(`${prefix ? "["+prefix+":error] " : ""}${m}`);
            })
        });

        process.on("error", error => {
            console.error(`${prefix ? "["+prefix+":process] " : ""}`, error);
            reject(error)
        });

        process.on("exit", (code, signal) => {
            console.log(`${prefix ? "["+prefix+":exit] " : ""} code=${code}, signal=${signal}`);

            if(code === 0) {
                resolve()
            }else {
                reject(new Error(`Maven exited with code=${code}, signal=${signal}`));
            }
        });
    }
}

module.exports = JavaClient;