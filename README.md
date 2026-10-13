UMFST
Informatika – 2. év
Laborfeladat
Java Swing News & Events Manager
Java Swing • SQLite • RSS • HTTP • JSON • XML • Git
Cél: Egy hallgatói szintű desktop alkalmazás elkészítése, amely híreket és eseményeket kezel SQLite adatbázisban, RSS feedekből híreket tölt be, és Java Swing felületen teszi lehetővé az adatok megtekintését és kezelését.


Tartalom
1. Projekt célja és technológiai keret
2. Funkcionális követelmények
3. Adatmodellek: News és Event
4. SQLite adatbázis és összetett kulcsok
5. CRUD műveletek és DatabaseManager
6. RSS források és JSON konfiguráció
7. HTTP GET, státuszkódok és timeout
8. RSS feldolgozás és adatmentés
9. Java Swing felület – Gmail-szerű master-detail nézet
10. Keresés, Refresh és aszinkron betöltés
11. Cover image és default kép
12. Hibakezelés és Clear DB
13. Javasolt projektstruktúra
14. Git követelmények és commitok
15. Javasolt fejlesztési sorrend
16. Minimum elfogadási feltételek

1. Projekt célja és technológiai keret
A feladat egy Java Swing alapú desktop alkalmazás elkészítése, amely híreket és eseményeket kezel. A hangsúly azon van, hogy a hallgatók egyetlen projektben összekapcsolják az OOP, adatbázis-kezelés, hálózati kommunikáció, XML/JSON feldolgozás, grafikus felület és Git használat alapjait.
Java Swing grafikus felület
SQLite adatbázis a lokális adatok tárolására
RSS feedek mint jelenlegi külső hírforrás
HTTP GET kérések és státuszkódok oktatási célból
JSON resource fájl az RSS források konfigurálására
XML feldolgozás az RSS tartalmának parse-olására
Git használat minden elkészült feature után
RSS feeds     ↓ HTTP kapcsolat + státuszkód     ↓ RSS XML parsing     ↓ News objektumok     ↓ SQLite     ↓ Swing UI
Fontos: A REST/HTTP rész jelenleg nem külön adatforrás. Az alkalmazás valódi külső hírforrása az RSS. A HTTP GET használata azt mutatja be, hogyan kommunikál a Java alkalmazás egy távoli szerverrel.

2. Funkcionális követelmények
Hírek hozzáadása, megtekintése, módosítása és törlése.
Események hozzáadása, megtekintése, módosítása és törlése.
A hírek és események SQLite adatbázisban tárolása.
Hírek betöltése RSS feedekből.
Az RSS források konfigurálása külön JSON fájlban.
Hírek közötti keresés legalább cím és leírás alapján.
Gmail-szerű, 1/3–2/3 arányú master-detail Swing felület.
Refresh gomb az RSS adatok frissítésére.
Aszinkron betöltés és látható loading állapot a Refresh alatt.
Default cover image használata, ha nincs saját kép.
Clear DB gomb megerősítéssel.
A hálózati kéréseknél timeout és HTTP státuszkód-kezelés.
A forráskód releváns kommentjei angol nyelvűek legyenek.
3. Adatmodellek: News és Event
A modellek egyszerű Java osztályok (POJO-k). Nem primitív osztályokról van szó: a News és Event saját, összetett adattípusokat reprezentálnak.
3.1 News
Mező
Java típus
Megjegyzés
title
String
A hír címe
description
String
A hír leírása / tartalma
source
String
A hír forrása, például RSS forrás neve
author
String
Szerző, ha rendelkezésre áll
publisher
String
Kiadó / publikáló szervezet
publishDate
Timestamp
Publikálás időpontja (SQLite: TIMESTAMP)
url
String
Az eredeti hír URL-je
creationDate
Timestamp
Mikor került az adatbázisba (SQLite: TIMESTAMP)
lastModifiedDate
Timestamp
Utolsó adatbázis-módosítás időpontja (SQLite: TIMESTAMP)
language
String
RO vagy HU
coverImage
String
Lokális képútvonal

A dátummezőkhöz használjátok a java.sql.Timestamp típust; az SQLite oszlopok legyenek TIMESTAMP típusúak.
public class News {     private String title;     private String description;     private String source;     private String author;     private String publisher;     private Timestamp publishDate;     private String url;     private Timestamp creationDate;     private Timestamp lastModifiedDate;     private String language;     private String coverImage; }
3.2 Event
Az Event ugyanazokat az alapmezőket tartalmazza, mint a News, és további eseményspecifikus mezőkkel egészül ki:
További mező
Java típus
Megjegyzés
eventDateTime
Timestamp
Az esemény napja és kezdési időpontja egy mezőben (SQLite: TIMESTAMP)
location
String
Az esemény helyszíne

Dátum: a projektben a dátum- és időmezőket Timestamp típussal kezeljük. Az eventDateTime egyszerre tartalmazza az esemény napját és kezdési időpontját, például 2026-10-20 18:30:00. Külön eventDate mező nem szükséges.

String dateText = "2026-10-20 18:30:00"; Timestamp eventDateTime = Timestamp.valueOf(dateText);
4. SQLite adatbázis és összetett kulcsok
Az alkalmazás SQLite adatbázist használ. Legalább két tábla szükséges: news és events. A laborfeladat egyik adatbázis-kezelési követelménye az összetett (composite) primary key használata, vagyis a kulcs két vagy több attribútumból álljon.
4.1 News kulcs
Javasolt összetett kulcs: source + url. Ez egyben segít elkerülni, hogy ugyanaz az RSS hír többször kerüljön az adatbázisba.
CREATE TABLE news (     title TEXT NOT NULL,     description TEXT,     source TEXT NOT NULL,     author TEXT,     publisher TEXT,     publish_date TIMESTAMP,     url TEXT NOT NULL,     creation_date TIMESTAMP NOT NULL,     last_modified_date TIMESTAMP NOT NULL,     language TEXT NOT NULL,     cover_image TEXT,     PRIMARY KEY (source, url) );
4.2 Event kulcs
Javasolt összetett kulcs: title + event_datetime + location. Ez oktatási célból megfelelő, de vegyétek észre, hogy a kulcsot alkotó mezők módosítása a kulcs módosítását is jelenti.
PRIMARY KEY (title, event_datetime, location)
Megjegyzés: Valódi production rendszerben gyakran külön technikai id lenne a primary key és külön UNIQUE constraint védené a duplikációkat. Ebben a laborban viszont az összetett primary key használata tananyag.

5. CRUD műveletek és DatabaseManager
Implementáljátok az Add, Read, Update és Delete műveleteket hírekhez és eseményekhez.
addNews / getNews / getAllNews / updateNews / deleteNews
addEvent / getEvent / getAllEvents / updateEvent / deleteEvent
A feladatban használható egy DatabaseManager utility osztály statikus metódusokkal. Java-ban egy top-level osztály maga nem lehet static, ezért a statikus viselkedést a metódusokon valósítsátok meg.
public class DatabaseManager {      public static void insertNews(News news) { }     public static void updateNews(News news) { }     public static void deleteNews(String source, String url) { }      public static void insertEvent(Event event) { }     public static void updateEvent(Event event) { }     public static void deleteEvent(String title, Timestamp dateTime, String location) { } }
6. RSS források és JSON konfiguráció
Az RSS URL-ek ne legyenek hardcode-olva a Java forráskódban. Helyezzétek őket a res mappába JSON fájlként.
res/ ├── rss.json └── images/     ├── default.jpg     └── loading.gif
6.1 Példa rss.json
{   "rssSources": [     {"name":"Info Mures","url":"https://stiri.infomures.ro/feed/","language":"RO"},     {"name":"Zi de Zi","url":"https://www.zi-de-zi.ro/feed/","language":"RO"},     {"name":"Punctul","url":"https://punctul.ro/feed/","language":"RO"},     {"name":"Radio Mures","url":"https://www.radiomures.ro/feed/","language":"RO"},     {"name":"KPTV News","url":"https://news.kptv.ro/feed/","language":"RO"},     {"name":"laTarguMures","url":"https://latargumures.ro/feed/","language":"RO"},     {"name":"Vasarhely.ma","url":"https://www.vasarhely.ma/feed/","language":"HU"},     {"name":"Marosvasarhelyi.info","url":"https://marosvasarhelyi.info/feed/","language":"HU"},     {"name":"Marosvasarhelyi Radio","url":"https://www.marosvasarhelyiradio.ro/hirek_kategoria/a-nap-hirei/feed/","language":"HU"}   ] }
A JSON rekordokat például RSSSource objektumokká alakíthatjátok. JSON parsinghoz használható a laboron megadott / engedélyezett könyvtár, például Gson.
7. HTTP GET, státuszkódok és timeout
A HTTP GET rész oktatási célú. A cél annak bemutatása, hogyan ér el a Java alkalmazás egy URL-t, hogyan olvassa ki a szerver válaszkódját, és hogyan reagál hibákra.
Kód
Jelentés
Elvárt viselkedés
200
OK
A tartalom feldolgozható.
301 / 302
Redirect
Jelezzétek vagy kezeljétek az átirányítást.
400
Bad Request
Hibás kérés.
401
Unauthorized
A kéréshez hitelesítés szükséges.
403
Forbidden
A kérés megtagadva.
404
Not Found
Az URL / erőforrás nem található.
500
Internal Server Error
Szerveroldali hiba.

connection.setConnectTimeout(5000); connection.setReadTimeout(5000);
public static int getStatusCode(String urlAddress) {     try {         URL url = new URL(urlAddress);         HttpURLConnection connection = (HttpURLConnection) url.openConnection();         connection.setRequestMethod("GET");         connection.setConnectTimeout(5000);         connection.setReadTimeout(5000);         int statusCode = connection.getResponseCode();         connection.disconnect();         return statusCode;     } catch (Exception e) {         return -1;     } }
Terminológia: A külön internetteszt helyett célszerű magát a használni kívánt URL-t elérni. Sikeres HTTP válasz bizonyítja, hogy a hálózati kommunikáció működik; -1 vagy exception esetén a program kezelje a hibát.

8. RSS feldolgozás és adatmentés
Az RSS XML formátumú adat. A feedből legalább az item elemeket kell feldolgozni, majd News objektumokká alakítani.
<item>     <title>...</title>     <description>...</description>     <link>...</link>     <pubDate>...</pubDate>     <author>...</author> </item>
Olvassátok be az rss.json forráslistát.
Minden URL-nél kérjétek le a HTTP státuszkódot timeouttal.
Sikeres válasz esetén töltsétek le és parse-oljátok az XML-t.
Hozzatok létre News objektumokat.
Mentsétek az új híreket SQLite-ba.
A hiányzó RSS mezőket kezeljétek biztonságosan; egy hiányzó author ne állítsa le a programot.
8.1 Cache és duplikációk
Az első letöltés után a hírek maradjanak az adatbázisban. Következő indításkor a program először SQLite-ból jelenítse meg a már meglévő adatokat, ne várjon online letöltésre.
Program start → SQLite → show saved news Refresh → RSS → parse → save only new records → reload list
Az összetett news primary key (source, url) használható a duplikációk megakadályozására.
9. Java Swing felület – Gmail-szerű master-detail nézet
A főablak elrendezése legyen a Gmailhez hasonló master-detail felépítésű: bal oldalon lista, jobb oldalon a kiválasztott elem részletei.
Search:  [______________________________]                              [ Refresh ]
NEWS / EVENTS
News 1 – selected
News 2
News 3
Event 1
Event 2
≈ 1/3 of window
SELECTED ITEM DETAILS
[ Cover image ]
Title
Description / content
Source • Author • Publisher
Publish timestamp • Language
URL
Event: start timestamp • location
≈ 2/3 of window
[ Add ]   [ Edit ]   [ Delete ]   [ News ]   [ Events ]   [ Clear DB ]
A bal oldali rész körülbelül az ablak 1/3-a.
A jobb oldali rész körülbelül az ablak 2/3-a.
Amíg nincs kijelölt elem, a részletes panel lehet üres vagy információs üzenetet mutathat.
Kijelöléskor a jobb oldali panel frissüljön a kiválasztott News vagy Event adataival.
DefaultListModel<News> model = new DefaultListModel<>(); JList<News> newsList = new JList<>(model);
10. Keresés, Refresh és aszinkron betöltés
10.1 Keresés
A hírek között legalább title és description alapján lehessen keresni. A keresés történhet Search gombbal vagy gépelés közben.
SELECT * FROM news WHERE title LIKE ? OR description LIKE ?;
10.2 Refresh
Refresh    ↓ show loading    ↓ HTTP/RSS work in background    ↓ save new records    ↓ reload data from SQLite    ↓ hide loading
10.3 SwingWorker
A hálózati letöltést nem szabad az Event Dispatch Threaden futtatni, mert a Swing ablak lefagyna. Használjatok SwingWorker-t.
new SwingWorker<Void, Void>() {     @Override     protected Void doInBackground() throws Exception {         // Download and parse RSS in the background         return null;     }      @Override     protected void done() {         // Hide loading state and reload the UI     } }.execute();
11. Cover image és default kép
A News és Event objektumok coverImage mezője lokális fájlútvonalat tárol. A kép adatbázisban BLOB-ként történő tárolása nem része az alapfeladatnak.
res/images/default.jpg
if (coverImage == null || coverImage.isEmpty()) {     coverImage = "res/images/default.jpg"; }
12. Hibakezelés és Clear DB
12.1 Kezelendő hibák
nincs elérhető hálózati kapcsolat / connection exception
timeout
HTTP hiba
hibás vagy nem elérhető RSS feed
hibás XML
hiányzó RSS mező
hibás dátumformátum
SQLite hiba
hiányzó cover image
hibás vagy hiányos felhasználói input
12.2 Clear DB
A Clear DB gomb törölje a híreket és eseményeket, de csak felhasználói megerősítés után.
int result = JOptionPane.showConfirmDialog(     null,     "Are you sure?",     "Clear database",     JOptionPane.YES_NO_OPTION );
DELETE FROM news; DELETE FROM events;
13. Javasolt projektstruktúra
src/ ├── model/ │   ├── News.java │   └── Event.java ├── database/ │   ├── Database.java │   └── DatabaseManager.java ├── network/ │   └── HttpHelper.java ├── rss/ │   ├── RSSReader.java │   └── RSSSource.java ├── ui/ │   ├── MainWindow.java │   ├── NewsPanel.java │   └── EventPanel.java └── Main.java  res/ ├── rss.json └── images/     ├── default.jpg     └── loading.gif
Felelősségi körök: A model osztályok adatot reprezentálnak; a DatabaseManager az adatbázist kezeli; a HttpHelper a hálózati ellenőrzést; az RSSReader az RSS parse-olást; a Swing osztályok csak a felhasználói felületért felelnek.

14. Git követelmények és commitok
Minden elkészült, értelmes feature után külön Git commit szükséges. Nem minden HTTP request után kell commitolni; a követelmény a fejlesztési lépések követhetősége.
Nem megfelelő
Megfelelő
update
Add News model
changes
Add SQLite database connection
stuff
Add RSS parser
final
Add async refresh with SwingWorker
project done
Add clear database functionality

A kód kommentjei angolul legyenek, de csak ott, ahol a komment valóban hozzáad a megértéshez.
// Download RSS feeds in the background to keep the Swing UI responsive
15. Javasolt fejlesztési sorrend
News model elkészítése.
Event model elkészítése és eventDateTime Timestamp-ra alakítása.
SQLite kapcsolat és táblák létrehozása.
Összetett primary key-k beállítása.
News CRUD.
Event CRUD.
Alap Swing főablak.
News lista és részletes nézet.
Add / Edit / Delete UI.
Keresés.
rss.json és RSSSource beolvasása.
HTTP GET + státuszkód + timeout.
RSS XML parser.
RSS News objektumok SQLite-ba mentése.
Duplikációk kezelése.
Refresh gomb.
SwingWorker és loading állapot.
Default cover image.
Event UI.
Clear DB.
Hibakezelés, kódtisztítás, végső ellenőrzés.
16. Minimum elfogadási feltételek
☐ Működő News és Event modellek.
☐ Működő SQLite adatbázis és news/events táblák.
☐ Legalább két mezőből álló primary key mindkét releváns táblában.
☐ Működő Add, Read, Update, Delete hírekhez és eseményekhez.
☐ DatabaseManager vagy azzal egyenértékű, jól elkülönített adatbázis-kezelés.
☐ RSS források res/rss.json fájlban.
☐ Legalább egy RSS feed sikeres feldolgozása.
☐ HTTP státuszkód és timeout kezelése.
☐ RSS hírek SQLite-ba mentése és duplikációk elkerülése.
☐ Programindításkor a korábban mentett hírek SQLite-ból betöltődnek.
☐ Gmail-szerű 1/3–2/3 Swing master-detail felület.
☐ Hír kiválasztásakor a részletek megjelennek.
☐ Működő keresés.
☐ Működő Refresh gomb.
☐ A Refresh nem fagyasztja le a Swing UI-t.
☐ Látható loading állapot.
☐ Cover image vagy default kép.
☐ Működő Clear DB megerősítéssel.
☐ Releváns hibák kezelve vannak.
☐ A kód releváns kommentjei angolul vannak.
☐ A Git történet feature-önkénti commitokat mutat.
A laborfeladat lényege: Nem production szintű hírportál készítése a cél, hanem annak bemutatása, hogyan kapcsolódik össze egy Java desktop alkalmazásban az OOP, SQLite, HTTP, RSS/XML, JSON, Swing és Git.

