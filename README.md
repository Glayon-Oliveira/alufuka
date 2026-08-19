# Alufuka

Esse é um aplicativo para navegação e documentação Javadoc.

## Estrutura do projeto

### `java-service`

Em `java-service` está o código-fonte do serviço que realiza as operações da aplicação, via socket na porta fixa `5130`.

### `front`

Em `front` está o código-fonte da interface desktop, desenvolvida com Electron.

O `npm` em `front` fornece os seguintes scripts:

* `start`: executa tanto um servidor Vite para servir o React quanto o próprio Electron. Antes de utilizá-lo, gere o JAR de `java-service` e coloque-o em `front/java-service`.
* `build-vite`: compila os arquivos React.
* `build-java`: compila o `java-service` e coloca o resultado em `front/java-service`.
* `build`: executa ambos os builds.
* `package`: executa o build e o empacotamento.
* `make`: executa o build, o empacotamento e a criação dos artefatos para as plataformas suportadas:

  * Windows, por meio do `maker-squirrel`;
  * Linux (DEB), por meio do `maker-deb`;
  * Linux (RPM), por meio do `maker-rpm`;
  * ZIP, por meio do `maker-zip`.
