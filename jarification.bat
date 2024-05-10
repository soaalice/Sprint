@echo off
setlocal

rem Définir le nom du projet
set "projet=Sprint"

rem Définir le chemin d'accès au répertoire des sources et au répertoire de destination des fichiers compilés
set "sourceDirectory=.\src"
set "destinationDirectory=.\bin"

rem Chemin vers le répertoire contenant les bibliothèques nécessaires
set "libDirectory=.\lib"

rem Initialiser la liste des fichiers Java à compiler
set "javaFiles="

rem Récupérer la liste de tous les fichiers Java dans les sous-dossiers de %sourceDirectory%
for /r "%sourceDirectory%" %%G in (*.java) do (
    rem Extraire la structure des packages à partir du chemin complet du fichier source
    set "javaFile=%%~fG"
    set "packagePath=!javaFile:%sourceDirectory%=!"
    set "packagePath=!packagePath:~0,-\%%~nG%%~xG!"

    rem Ajouter le fichier Java à la liste des fichiers à compiler
    set "javaFiles=!javaFiles! "%%G""
)

rem Construire le chemin de classe pour toutes les bibliothèques dans le dossier "lib"
set "classpath="
for %%I in ("%libDirectory%\*.jar") do (
    set "classpath=!classpath!;"%%I""
)

rem Compiler tous les fichiers Java en une seule commande avec les bibliothèques nécessaires
javac -cp "%classpath%" -d "%destinationDirectory%" !javaFiles!

rem Aller dans le répertoire de destination des fichiers compilés
cd "%destinationDirectory%"

rem Compresser dans un fichier jar
jar -cvf "../lib/%projet%.jar" *

echo Fichier .jar créé : %projet%.jar

endlocal
