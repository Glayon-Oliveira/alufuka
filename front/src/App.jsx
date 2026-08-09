import { useEffect, useId, useState } from "react";

export function FailView({file, message, name = undefined, cause = undefined}) {
    const id = useId();

    return (
        <div className="card shadow-sm overflow-hidden">
            <div className="card-header p-0" data-bs-toggle="collapse" data-bs-target={`#failview-${id}`}
                role="button" style={{ cursor: "pointer" }}>

                <div className="d-flex align-items-center justify-content-between px-3 py-3">
                    <div>
                        <div className="fw-semibold text-danger">
                            {file ?? name ?? "Unknown"}
                        </div>
                    </div>
                </div>
            </div>

            <div className="collapse" id={`jtype-${id}`}>
                <div className="card-body">
                    <p className="text-body-secondary">
                        {message ?? "No message"}
                    </p>

                    {cause && 
                        <p className="text-body-danger">
                            {cause}
                        </p>
                    }
                </div>
            </div>
        </div>
    )
}

export function Javadoc({ file, path, javadoc }) {
    const [editing, setEditing] = useState(false);
    const [value, setValue] = useState(javadoc ?? "");

    async function save() {
        await window.api.writeJavadoc(file, path, value);

        setEditing(false);
    }

    return (
        <div className="mt-3">
            <div className="d-flex align-items-center justify-content-between mb-2">
                <div className="d-flex align-items-center gap-2">
                    <span className="badge text-bg-secondary">
                        Javadoc
                    </span>

                    <small className="text-body-secondary font-monospace">
                        {path}
                    </small>
                </div>

                <button
                    type="button"
                    className={`btn btn-sm ${
                        editing
                            ? "btn-success"
                            : "btn-outline-secondary"
                    }`}
                    onClick={editing ? save : () => setEditing(true)}
                >
                    {editing ? "Salvar" : "Editar"}
                </button>
            </div>

            {editing ? (
                <textarea
                    className="form-control font-monospace"
                    rows="6"
                    value={value}
                    onChange={(e) => setValue(e.target.value)}
                />
            ) : (
                <div className="p-3 rounded-3 border bg-body-tertiary">
                    {value || (
                        <span className="text-body-secondary fst-italic">
                            Sem documentação.
                        </span>
                    )}
                </div>
            )}
        </div>
    );
}

export function JavaMemberView({ member, path, file }) {
    const id = useId();

    const currentPath = `${path}.${member.name}`;

    return (
        <div className="ms-3">
            <div className="border-start border-2 ps-3 py-1" style={{ borderColor: "var(--bs-border-color)" }}>
                <div className="d-flex align-items-center justify-content-between gap-3 p-2 rounded-3" data-bs-toggle="collapse" data-bs-target={`#j-${id}`}
                    role="button" style={{ cursor: "pointer" }}>
                    <div>
                        <div className="font-monospace fw-semibold text-primary">
                            {member.name}
                        </div>

                        <div className="small text-body-secondary">
                            {member.definition}
                        </div>
                    </div>

                    <span className="text-body-secondary"> › </span>
                </div>

                <div className="collapse" id={`j-${id}`}>
                    <div className="px-2 pb-3 pt-2">
                        {member.annotations.map((a, i) => (
                            <div className="font-monospace small text-body-secondary" key={i}>
                                {a}
                            </div>
                        ))}

                        <div className="p-3 rounded-3 border bg-body-tertiary mt-2">
                            <code>{member.definition}</code>
                        </div>

                        <Javadoc file={file} path={currentPath} javadoc={member.javadoc}/>
                    </div>
                </div>
            </div>

            {member.members?.map((m) => (
                <JavaMemberView key={m.definition} member={m} path={currentPath} file={file}/>
            ))}
        </div>
    );
}

export function JavaView({ file }) {
    const [result, setResult] = useState();
    const id = useId();   

    useEffect(() => {
        async function read() {
            setResult(await window.api.readJava(file));
        }

        read();
    }, [file]);

    if (!result) {
        return (
            <div className="card shadow-sm">
                <div className="card-body">
                    Carregando...
                </div>
            </div>
        );
    }

    const javaType = result.body?.javaType;
    const packageName =  result.body?.packageName;
    const name = javaType?.name;

    if(result.status == "FAILURE") {
        return (
            <FailView file={file} message={result?.message} cause={result?.cause}></FailView>
        )
    }

    return (
        <div className="card shadow-sm overflow-hidden">
            <div className="card-header p-0" data-bs-toggle="collapse" data-bs-target={`#jtype-${id}`}
                role="button" style={{ cursor: "pointer" }}>

                <div className="d-flex align-items-center justify-content-between px-3 py-3">
                    <div>
                        <div className="fw-semibold">
                            {name}
                        </div>

                        <div className="small text-body-secondary">
                            {packageName ?? file}
                        </div>
                    </div>

                    <span className="badge rounded-pill text-bg-light border">
                        {javaType.members.length} membros
                    </span>
                </div>
            </div>

            <div className="collapse" id={`jtype-${id}`}>
                <div className="card-body">
                    {javaType.annotations.map((a, i) => (
                        <div className="font-monospace small text-body-secondary" key={i}>
                            {a}
                        </div>
                    ))}

                    <div className="p-3 rounded-3 border bg-body-tertiary mt-2">
                        <code>{javaType.definition}</code>
                    </div>

                    <Javadoc file={file} path={name} javadoc={javaType.javadoc}/>

                    <div className="mt-4">
                        {javaType.members.map((member) => (
                            <JavaMemberView key={member.definition} member={member} path={name} file={file}/>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default function App() {
    const [files, setFiles] = useState([]);

    async function chooseFiles() {
        const selectedFiles = await window.api.chooseDirectory();

        if (!selectedFiles) {
            return;
        }

        setFiles(selectedFiles);
    }

    async function chooseOneFile() {
        const selectedFile = await window.api.chooseJavaFile();

        if (!selectedFile) {
            return;
        }

        setFiles([selectedFile]);
    }

    return (
        <div className="min-vh-100 bg-body-tertiary">
            <header className="border-bottom bg-body">
                <div className="container-fluid px-4 py-3">
                    <div className="d-flex align-items-center justify-content-between">
                        <div className="d-flex align-items-center gap-3">
                            <div className="d-flex align-items-center justify-content-center rounded-3 bg-primary text-white fw-bold"
                                style={{
                                    width: "42px",
                                    height: "42px",
                                }}>
                                AK
                            </div>

                            <div>
                                <h5 className="mb-0">Alufuka</h5>
                                <small className="text-body-secondary">
                                    Java documentation explorer
                                </small>
                            </div>
                        </div>

                        <div className="btn-group">
                            <button className="btn btn-primary" onClick={chooseFiles}>
                                Selecionar pasta
                            </button>

                            <button className="btn btn-outline-secondary" onClick={chooseOneFile}>
                                Arquivo
                            </button>
                        </div>
                    </div>
                </div>
            </header>
            
            <main className="container-fluid px-4 py-4">
                {files.length === 0 ? (
                    <div className="d-flex flex-column align-items-center justify-content-center text-center border rounded-4 bg-body"
                        style={{
                            minHeight: "420px",
                            borderStyle: "dashed",
                        }}>
                        <div className="d-flex align-items-center justify-content-center rounded-circle bg-body-tertiary border mb-3"
                            style={{
                                width: "72px",
                                height: "72px",
                                fontSize: "28px",
                            }}>
                            ☕
                        </div>

                        <h5>Nenhum código carregado</h5>

                        <p className="text-body-secondary mb-4" style={{ maxWidth: "420px" }}>
                            Selecione uma pasta ou arquivo Java para começar
                            a explorar as classes, métodos e documentação.
                        </p>

                        <button className="btn btn-primary px-4" onClick={chooseFiles}>
                            Selecionar pasta
                        </button>
                    </div>
                ) : (
                    <>
                        <div className="d-flex align-items-end justify-content-between mb-3">
                            <div>
                                <h4 className="mb-1">Arquivos Java</h4>
                                <div className="text-body-secondary small">
                                    {files.length} arquivo
                                    {files.length !== 1 ? "s" : ""} carregado
                                    {files.length !== 1 ? "s" : ""}
                                </div>
                            </div>
                        </div>

                        <div className="d-flex flex-column gap-3">
                            {files.map((f) => (
                                <JavaView file={f} key={f} />
                            ))}
                        </div>
                    </>
                )}
            </main>
        </div>
    );
}
