# bibleget-openoffice
BibleGet I/O Project plugin for Open Office

![SourceForge](https://img.shields.io/sourceforge/dt/aoo-extensions/18086?style=flat-square)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/BibleGet-I-O/bibleget-openoffice?style=flat-square)
![GitHub Release Date](https://img.shields.io/github/release-date/BibleGet-I-O/bibleget-openoffice?style=flat-square)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/BibleGet-I-O/bibleget-openoffice?style=flat-square)
![GitHub](https://img.shields.io/github/license/BibleGet-I-O/bibleget-openoffice?style=flat-square)
![GitHub top language](https://img.shields.io/github/languages/top/BibleGet-I-O/bibleget-openoffice?style=flat-square)
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/BibleGet-I-O/bibleget-openoffice?style=flat-square)


## Description

Insert Bible quotes into your document using standard notation for Bible Quotes (see https://en.wikipedia.org/wiki/Bible_citation). This plugin uses the BibleGet service endpoint (https://query.bibleget.io) to retrieve bible quotes. The user can choose the version or versions of the Bible to retrieve the quotes from based on their availability on the server. More versions in various languages are being added all the time. You can contribute to translating the user interface into other languages here: https://poeditor.com/join/project/5bkVxO5qsq

Inserisci citazioni della Bibbia nel tuo documento usando la notazione biblica standard. Questa estensione utilizza il servizio web di BibleGet (https://query.bibleget.io) per reperire le citazioni. L'utente può scegliere la versione biblica dalla quale trarre la citazione, in base alle versioni disponibili sul server. Nuove versioni in varie lingue vengono aggiunte in continuazione. Puoi contribuire a tradurre l'interfaccia in altre lingue qui: https://poeditor.com/join/project/5bkVxO5qsq

Introduzca citas de la Biblia en su documento utilizando la notación bíblica estándar. Esta extensión utiliza el servicio web BibleGet (https://query.bibleget.io) para obtener citas bíblicas. El usuario puede elegir la versión bíblica de la cual extraer la cita, de acuerdo a las versiones disponibles en el servidor. Nuevas versiones en varios idiomas se agregan constantemente. Puedes ayudar con la traduccion de la interfaz aqui: https://poeditor.com/join/project/5bkVxO5qsq

Entrez citations bibliques dans votre document en utilisant la notation biblique standard. Cette extension utilise le service Web BibleGet (https://query.bibleget.io) pour obtenir les citations. L'utilisateur peut choisir la version biblique dont tirer la citation, selon les versions disponibles sur le serveur. Nouvelles versions en différentes langues sont ajoutés en permanence. Pouvez aider avec la traduction de l'interface dans autres langues ici: https://poeditor.com/join/project/5bkVxO5qsq

Geben biblische Zitate in Ihrem Dokument mit dem biblischen Standard-Notation. Diese Erweiterung nutzt die Web-Service-BibleGet (https://query.bibleget.io), die Anführungszeichen zu bekommen. Der Benutzer kann die Version, die das biblische Zitat ziehen, abhängig von der auf dem Server verfügbaren Version. Neue Sprachversionen werden laufend hinzugefügt. Du kannst helfen, übersetzen Sie die Schnittstelle in anderen Sprachen Hier: https://poeditor.com/join/project/5bkVxO5qsq

## Installation
The latest release can be downloaded from the Open Office Extensions website at:
* https://extensions.openoffice.org/en/project/bibleget-io
* a Java Runtime Environment is required for this plugin to function correctly. A minimum of JRE 1.8 (32bit version) is required and can be obtained either from https://www.oracle.com/java/technologies/javase-jre8-downloads.html or from https://developers.redhat.com/products/openjdk/download (JDK includes JRE). Please note that a 64bit JRE will not work. You can install both a 64bit and a 32bit JRE, but OpenOffice can only handle a 32bit JRE (on Windows in any case).

## Developers
Anyone interested in contributing to the development of this plugin should first setup the environment for Apache Open Office plugin development. Please take a look at the [wiki page on this repository](https://github.com/BibleGet-I-O/bibleget-openoffice/wiki/Setting-up-the-Development-Environment).

## Changelog

### v2.8 (March 3rd, 2019)
This release of the plugin or extension for Open Office resynchronizes the plugin code with the 2.8 version of the plugin for LibreOffice. The preceding 2.6 release had been published first for OpenOffice, and then published as v2.7 for LibreOffice seeing that this last platform needed a few adaptations.

I had pushed the code for the extension to github when releasing the 2.6 version (year 2015) to keep it publicly accessible to the open source community. In the meantime, because of some technical difficulties with the computer I used for developing, I lost the local copy of the extension code. In order to proceed with this release, I retrieved the code from github (mainly the 2.7 release for LibreOffice). And I'm not entirely sure the code on github was actually 100% up to date with the actual 2.7 release because it seemed to have some bugs which didn't appear when testing for the 2.7 release; either the github code wasn't 100% up to date or there were some changes in some of the Java libraries used between releases which made for some bad bugs. In any case, these were all ironed out for the current 2.8 release.

After the recent BibleGet Project server updates which set HSTS headers enforcing the usage of the https protocol throughout the domain, including the query.bibleget.io service endpoint, it was necessary to use the https protocol in the extension web connections to the service endpoint. However the BibleGet.io domain uses Let's Encrypt certificates. Let's Encrypt certificates should be accepted in JRE 1.8 keystores which should trust the "DST Root CA X3" certificate used to sign Let's Encrypt certificates. However the keystore on my development machine didn't trust this certificate even though a Java 1.8 runtime was installed, and so the Let's Encrypt certificate was still causing an SSLHandShake exception to be thrown. In order to make sure untrusted certificates would not cause SSLHandShake exceptions which would have impeded the correct functioning of the extension, the DST Root CA X3 certificate was included in the extension resources and forcefully made to be trusted during plugin execution.

I'm guessing with there were some changes in updated Java libraries used for the extension, or perhaps in JRE itself, which changed the behaviour of javax.json . Not sure if this was because of code that was not pushed to github for the 2.7 release or because of updated libraries that acted differently since the 2.7 release, because the 2.7 release didn't have any problems with this as far as I can tell. In any case, the built json objects are now correctly passed into JsonObject variables which are then returned, instead of directly returning the JsonBuilder once it was built.

Another issue handled in the current release was that of UTF-8 encoded resource strings not being handled correctly, and the localized string translations were not being retrieved. I added a utf-8 control class to correctly read utf-8 encoded string resources.

* use https protocol for web connections
* the root CA certificate included and forcefully added to the trusted certificates on runtime to make sure https connections don't throw SSLHandShake exceptions
* fixed bugs with options not being correctly loaded, this was depending on how JsonBuilders were behaving
* JsonValues which are string values are now explicitly cast to JsonStrings and the string value is obtained with JsonString.getString
* new utf-8 control class to handle retrieval of utf-8 encoded strings from resources
* use Java Runtime 1.8 (x86) to build the extension, along with the 32bit Open Office 4.1.6 SDK. This requires using a 32 bit JRE rather than 64 bit. In any case a minimum JRE of 1.8 is now required
* some resource strings were updated

## v2.6 (September 17th, 2015)
This release contains a bugfix for setting the background colors for the inserted Scripture quotes (there seems to be a kind of bug in the OpenOffice software but we have provided a workaround for the time being).

It also contains a fix for the NABRE text's <speaker> tag which was not yet supported in the last release. For now the <speaker> tag has a fixed formatting of bold text with a space before and after and a background color of Light Gray. If anyone finds the fixed formatting not according to their needs please let me know and I can create more options for customizing the formatting of the speaker tag (examples of this tag can be found in <Song of Songs 2> for example, it represents the person currently speaking in the poetic dialog).

There is also an enhancement for the formatting of the poetic texts in the NABRE version, between correct newlines and correct indents... The indents can perhaps be enhanced even more, but for now it's acceptable.

## v2.5 (August 22nd, 2015)
This release contains a series of bugfixes and of enhancements. Must update.
* bugfix: quote was not being inserted at current cursor position
* bugfix: chapter limit and verse limit warning messages were not returning valid values
* bugfix: inserted text was not able to be formatted
* feature addition: added option to ovveride internal formatting for biblical texts that have internal formatting
* feature addition: support for New American Bible - Revised Edition with it's internal formatting
* bugfix: fix automatic refresh when renewing metadata from the BibleGet server
* bugfix: fix unicode font rendering for Asian languages
* feature addition: added German, French and Spanish translations of the interface (might need some fine-tuning)

## v2.0 (March 9th, 2015)
This is a major release. Much of the code has been updated to make it compatible with the second version of the BibleGet I/O engine. This means that the extension can now handle multiple versions of the Bible, as well as detect which versions are available on the BibleGet I/O server and which languages are supported by the BibleGet engine for the recognition of the books of the Bible. Also index information for each version of the Bible is handled, to check whether chapters and verses requested are valid.
The extension interface has also been internationalized with this release. The interface is translated into both English and Italian in full, and only partially into Spanish and French. Users can contribute translations for the extension interface at this project url: https://www.transifex.com/accounts/profile/bibleget.io/ . You can create an account and request access to the BibleGet project translations.

Questo aggiornamento di versione è un rilascio importante. Molto del codice è stato modificato e aggiornato per renderlo compatibile con la seconda versione del motore di BibleGet I/O. Questo significa che l'estensione ora può gestire multiple versioni della Bibbia, e può rilevare quali sono le versioni disponibili sul server BibleGet I/O e rilevare quali sono le lingue supportate dal motore BibleGet per il riconoscimento dei libri della Bibbia. Inoltre, informazioni di indice per ogni versione biblica vengono conservate in locale dal server BibleGet, per verifiche sulla validità dei capitoli e dei versetti nelle richieste di citazioni.
L'interfaccia dell'estensione è stata anche internazionalizzata con questo aggiornamento. L'interfaccia è disponibile per intero in Inglese e in Italiano, e solo parzialmente in Spagnolo e in Francese. Gli utenti possono contribuire a tradurre l'interfaccia in varie lingue a questo indirizzo: https://www.transifex.com/accounts/profile/bibleget.io/ . Puoi creare un account a questo indirizzo e richiedere accesso alle traduzione del progetto BibleGet.

Cette mise à jour est une version majeure. Une grande partie du code a été mis à jour pour le rendre compatible avec la deuxième version du moteur BibleGet I/O. Cela signifie que l'extension peut maintenant gérer de multiples versions de la Bible, ainsi que détecter les versions que sont disponibles sur le serveur et détecter quelles sont les langues supportées par le moteur BibleGet pour la reconnaissance des livres de la Bible. Également des informations d'index pour chaque version de la Bible est traitée, pour vérifier si les chapitres et les versets demandées sont valables.
L'interface de l'extension a également été internationalisé. L'interface est traduite en italien et en anglais dans son intégralité, et seulement partiellement en espagnol et en français. Les utilisateurs peuvent contribuer les traductions de l'interface de l'extension à ce url: https://www.transifex.com/accounts/profile/bibleget.io/. Vous pouvez créer un compte utilisateur et demander l'accès aux traductions de projets BibleGet.

Esta actualización es una versión principal. Gran parte del código se ha actualizado para ser compatible con la segunda versión del motor BibleGet I/O. Esto significa que la extensión puede ahora gestionar múltiples versiones de la Biblia, y puede identificar las versiones biblicas que están disponibles en el servidor y detectar cuales idiomas son apoyadas del motor BibleGet por el reconocimiento de los libros de la Biblia. También la información de índice para cada versión de la Biblia es descargada en local para comprobar si los capítulos y versículos requeridos son válidos.
La interfaz de l'extensión también se ha internacionalizado. La interfaz está traducida al italiano e al Inglés en su totalidad, y sólo parcialmente en español y francés. Los usuarios pueden contribuir traducciones de la interfaz de l'extensión a esta url: https://www.transifex.com/accounts/profile/bibleget.io/. Pueden crear una cuenta de usuario y solicitar el acceso a las traducciones de los proyectos BibleGet.

## v1.0 (December 7th, 2014)
First release, after a 3 month development process with testing and debugging.
