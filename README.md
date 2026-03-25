# AudioOggTranscriberApp

Aplicação desktop Java com interface Swing para **transcrever arquivos `.ogg` em texto `.txt`** usando **Vosk** e **FFmpeg**.

## Visão geral

O projeto foi estruturado para separar responsabilidades entre:

- **UI Swing** para interação com o usuário
- **Service layer** para orquestração da transcrição
- **Repository** para persistência do histórico em JSON
- **Integração com FFmpeg** para conversão de áudio
- **Integração com Vosk** para reconhecimento de fala

## Funcionalidades

- Seleção de arquivo de entrada `.ogg`
- Seleção do arquivo de saída `.txt`
- Seleção da pasta do modelo Vosk
- Conversão automática de `.ogg` para `.wav` PCM mono 16kHz
- Transcrição do áudio com Vosk
- Salvamento do resultado em `.txt`
- Exibição do texto transcrito na interface
- Histórico de execuções persistido em arquivo JSON
- Barra de progresso
- Validação de dependências no startup
- Cancelamento do processamento

## Tecnologias utilizadas

- **Java 21**
- **Maven**
- **Swing**
- **Vosk**
- **FFmpeg**
- **Jackson**

## Estrutura do projeto

```text
src/main/java/br/techservice/audiooggtranscriberapp/
├── Main.java
├── model/
│   ├── HistoryEntry.java
│   ├── TranscriptionRequest.java
│   └── TranscriptionResult.java
├── repository/
│   ├── HistoryRepository.java
│   └── JsonHistoryRepository.java
├── service/
│   ├── AudioConverterService.java
│   ├── EnvironmentService.java
│   ├── TranscriptFileService.java
│   ├── TranscriptionService.java
│   └── VoskRecognitionService.java
├── ui/
│   └── AudioOggTranscriberFrame.java
└── util/
    └── DurationUtils.java
```

## Pré-requisitos

Antes de executar o projeto, você precisa ter:

- **JDK 21**
- **Apache NetBeans IDE 26** ou outro ambiente compatível com Maven
- **FFmpeg** instalado
- **Modelo Vosk** baixado localmente

## Instalação do FFmpeg

### Windows

1. Baixe o FFmpeg
2. Extraia para uma pasta, por exemplo:

```text
C:\ffmpeg
```

3. Confirme se o executável existe em:

```text
C:\ffmpeg\bin\ffmpeg.exe
```

4. Adicione `C:\ffmpeg\bin` ao `PATH` do Windows

Para testar:

```bat
ffmpeg -version
```

Se esse comando funcionar no terminal, a aplicação conseguirá localizar o FFmpeg.

### Alternativa com variável de ambiente

Você também pode definir uma variável de ambiente para o executável:

```bat
setx FFMPEG_PATH "C:\ffmpeg\bin\ffmpeg.exe"
```

Depois disso, reinicie o NetBeans.

## Download do modelo Vosk

Baixe um modelo oficial em:

- https://alphacephei.com/vosk/models

Para **português**, uma opção leve para desktop é:

- `vosk-model-small-pt-0.3`

Exemplo de diretório:

```text
C:\vosk\vosk-model-small-pt-0.3
```

Você também pode definir a variável de ambiente:

```bat
setx VOSK_MODEL_PATH "C:\vosk\vosk-model-small-pt-0.3"
```

## Como executar no NetBeans 26

1. Abra o projeto Maven no NetBeans
2. Aguarde o download das dependências
3. Execute `Clean and Build`
4. Rode a classe principal:

```text
br.techservice.audiooggtranscriberapp.Main
```

## Como executar via Maven

No diretório do projeto:

```bash
mvn -U clean install
mvn exec:java -Dexec.mainClass=br.techservice.audiooggtranscriberapp.Main
```

## Exemplo de uso

1. Clique em **Selecionar .ogg**
2. Escolha o arquivo de áudio
3. Clique em **Salvar .txt em...**
4. Escolha o destino do arquivo de transcrição
5. Informe a pasta do modelo Vosk
6. Clique em **Transcrever**
7. Aguarde a conversão e o reconhecimento
8. O texto será exibido na tela e salvo no arquivo `.txt`

## Persistência do histórico

O histórico é salvo em JSON no diretório do usuário:

```text
%USERPROFILE%\.audiooggtranscriber\history.json
```

## Dependências Maven principais

```xml
<dependencies>
    <dependency>
        <groupId>com.alphacephei</groupId>
        <artifactId>vosk</artifactId>
        <version>0.3.45</version>
    </dependency>

    <dependency>
        <groupId>net.java.dev.jna</groupId>
        <artifactId>jna</artifactId>
        <version>5.7.0</version>
    </dependency>

    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.21.2</version>
    </dependency>

    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
        <version>2.21.2</version>
    </dependency>
</dependencies>
```

## Possíveis problemas

### FFmpeg não encontrado

Mensagem típica:

```text
Cannot run program "ffmpeg": CreateProcess error=2
```

Solução:
- instalar o FFmpeg
- adicionar ao `PATH`
- ou definir `FFMPEG_PATH`

### Modelo Vosk inválido

Verifique se o caminho informado aponta para a **pasta raiz do modelo descompactado**.

### Erro ao executar pelo Run do NetBeans

Se o NetBeans tentar executar `${packageClassName}`, ajuste a configuração de execução do projeto para usar:

```text
br.techservice.audiooggtranscriberapp.Main
```

## Melhorias futuras

- suporte a mais formatos de áudio
- processamento em lote
- configuração persistente de caminhos
- logs estruturados
- exportação do histórico
- empacotamento nativo para Windows

## Licença

Defina aqui a licença do projeto, por exemplo:

```text
MIT
```

## Autor

Projeto mantido por **Felipe Nunes**.
