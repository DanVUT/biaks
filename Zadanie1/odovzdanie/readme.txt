Autor: Daniel Florek (df077nv)

Program bol pisany a testovany na Java 11 (LTS 11.0.8) na Jave 8 je nepreložiteľný

K spusteniu klienta je potrebná JavaFX.

Spustenie testované v rámci IntelliJIdea pri využití JetBrains Runtime a z commandline
pri využití ZuluFX (OpenJDK + OpenJFX)

K šifrovaniu správ sa využíva prúdový algoritmus RC4.

Klient argumenty:
"" = Defaultná IP adresa 127.0.0.1, Port 80
[IP servera] = Defaultný port 80
[IP servera] [Port Servera]

Argumenty musia byť oddelené medzerou

Server argumenty:
[Cesta k zdielanemu suboru] = povinný, default port 80
[Cesta k zdielanemu suboru] [port]

Argumenty musia byť oddelené medzerou