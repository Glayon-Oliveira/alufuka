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
        return this.send("{}")
            .then((result) => result?.status)
            .catch(() => false)
    }

    async waitForService() {
        while (!(await this.isServiceRunning())) {
            await new Promise(resolve => setTimeout(resolve, 100));
        }
    }

    send(data) {
        return new Promise((resolve, reject) => {
            const socket = net.createConnection(this.port, "localhost");
            let payload = null;
            let buffer = Buffer.alloc(0)

            socket.on("connect", () => socket.write(data + "\n"));

            socket.on("data", chunk => {
                buffer = Buffer.concat([buffer, chunk])

                if(payload === null && buffer.length >= 4) {
                    payload = buffer.readInt32BE(0);
                }else if(payload == null) {
                    return
                }

                if(payload < 0) {
                    socket.destroy()
                    reject()
                    return
                }

                let expected = 4 + payload + 1;

                if(buffer.length < expected) {
                    return;
                }

                socket.destroy()

                if(buffer[expected-1] !== 0x0A) {
                    reject()
                    return
                }

                let content = buffer.subarray(4, payload+4);

                try{
                    resolve(JSON.parse(content.toString("utf8")));
                }catch(error) {
                    reject(error);
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

    writeJavadoc(filePath, javaPath, content, types = undefined) {
        return this.send(JSON.stringify({
            type: "JAVADOC_WRITER",
            file_path: filePath,
            path: javaPath,
            content: content,
            types: types
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