

# Serwis REST do obsługi kuponów rabatowych

## Opis projektu

Serwis REST odpowiedzialny za zarządzanie kuponami rabatowymi.

System udostępnia następujące funkcjonalności:

• rejestrację użycia kuponu przez użytkownika,    
• tworzenie nowego kuponu (bez obsługi uwierzytelniania)

Każdy kupon zawiera następujące informacje:

* unikalny kod kuponu (do 20 znaków alfanumerycznych),
* datę utworzenia,
* maksymalną liczbę możliwych użyć,
* bieżącą liczbę użyć,
* kod kraju, dla którego kupon jest przeznaczony (pole opcjonalne - brak kodu oznacza, że użytkownik z dowolnego kraju może zaaplikować ten kupon)
* dostępność dla tyko dla zarejestrowanych użytkowników - flaga true/false oznaczająca, że kupon jest przeznaczony wyłącznie do zarejestrowanych użytkowników aplikacji - ich identyfikatory muszą już istnieć w bazie danych

## Opis wymagań biznesowych:

* Kod kuponu powinien być unikalny, wielkość znaków nie ma znaczenia
* Wykorzystanie kuponu powinno być limitowane maksymalną liczbą użyć - kto pierwszy ten lepszy
* Kraj zdefiniowany w kuponie ogranicza użycie kuponu tylko do osób z danego kraju (na podstawie adresu IP HTTP requestu).
* Gdy kupon osiągnął maksymalną liczbę zużyć, próby użycia go kończą się błędem ze stosownym komunikatem. Tak samo, gdy podany kod kuponu nie istnieje, próba zużycia pochodzi z niedozwolonego kraju lub użytkownik zużył już dany kupon.
* Jeden użytkownik może wykorzystać kupon tylko raz – identyfikator użytkownika zdefiniowanygo już w bazie danych musi być przekazany w requescie

## Opis techniczny

Projekt składa się z trzech modułow:

**Moduł serwisu REST (service)** 

Moduł został napisany w języku Java z wykorzystaniem frameworku **spring boot**. Jest budowany z wykorzystaniem maven. Aplikacja wykorzystuje bazę Postgresql w celu przechowywania danych kuponów, ich wykorzystania i danych zarejestrowanych użytkowników. W celu identyfikacji kraju z jakiego pochodzą requesty HTTP dotyczące zaaplikowania kuponu wykorzystywany jest darmowy REST serwis [http://ip-api.com](http://ip-api.com). Istnieje również możliwość rekonfiguracji url służącego do pobrania kodu kraju na podstawie adresu ip z requestu HTTP poprzez parametr IPAPI_URL.

Budowanie aplikacji z wykorzystaniem maven komendą:

```bash
./mvnw clean package
```
Budowanie z uruchamieniem testów integracyjnych:
```bash 
./mvn clean verify 
```
*(Z uwagi na to, że w testach integracyjnych użyto fameworku [Testcontainers](https://testcontainers.com/) musi istnieć obraz docker o nazwie coupon-db, dla którego tworzony jest kontener na czas trwania testów. Dla tych testów konieczne jest działanie bazy danych ze zdefiniowanymi zarejestrowanymi użytkownikami. Więcej informacji o kontenerze coupon-db znajduje się w opisie modułu bazy danych)*

W celu uruchomienia aplikacji należy ustawić następujące zmienne środowiska:
* POSTGRES_DB - definiuje nazwą bazy danych Postgres
* POSTGRES_USER - definiuje użytkownika dostępowego do bazy danych
* POSTGRES_PASSWORD - definiuje hasło dla użytkownika do bazy danych
* POSTGRES_HOST - definiuje adres serwera bazy danych
* POSTGRES_PORT - definiuje port na którym nasłuchuje baza danych

Przykładowe wartości zmiennych dla środowiska lokalnego znajdują się w pliku [.env](.env).  
Do uruchomienia lokalnie aplikacji można wykorzystać przygotowany skrypt: [run-local-service.sh](./service/run-local-service.sh)

Opcjonalnie istnieje możliwość uruchomienia serwisu jako kontenera w Docker o nazwie **coupon-service**. W tym celu przygotowano plik [Dockerfile](./service/Dockerfile). Przygotowano również proste skrypty shell w celu budowania obrazu: [docker-build-service.sh](./service/docker-build-service.sh) oraz uruchomienia kontenera: [docker-run-service.sh](./service/docker-run-service.sh)  
W celu uruchomienia gotowego środowiska można również wykorzystać przygotowaną konfiguracje dla docker-compose [docker-compose](./docker-compose.yaml), która korzystana z predefiniowanych zmiennych środowiska z pliku [.env](.env). Komenda:
```bash  
docker-compose up
 ```
Serwis udostępnia następujące endpointy

* POST /coupons   -  do tworzenia kuponu
* GET /coupons/{code} - do pobrania danych kuponu z wybranym kodem
* GET /coupons   -  do pobrania listy utworzonych kuponów (możliwość stronnicowania wyników w wykorzystaniem opcjonalnych parametrów 'page' i 'size')
* POST /coupons/{code}/apply - zaaplikowanie kuponu z wybranym kodem przez użytkownika

Szczegółowa dokumentacja API z wykorzystaniem swagger-ui dostępna jest po uruchomieniu aplikacji pod url:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

Przygotowano przykłady wykorzystania powyższych endpointów w załączonej kolekcji Postman:

[coupon-challenge.postman_collection.json](./coupon-challenge.postman_collection.json)

**Moduł bazy danych (database)**

Zawiera konfigurację dla bazy Postgresql. Definicja schematu bazy oraz użytkowników aplikacji znajduje się w pliku: [init.sql](database/init.sql)

Stworzono definicję obrazu Docker bazy z wykorzystaniem pliku init.sql w pliku [Dockerfile](./database/Dockerfile).  
Przegotowano proste skrypty shell do tworzenia obrazu: [docker-build-coupon-db.sh](./database/docker-build-coupon-db.sh) oraz uruchomienia bazy Postgresql jako kontenera Docker o nazwie **coupon-db**: [docker-run-coupon-db.sh](./database/docker-run-coupon-db.sh)

**Moduł mocka serwisu IP API (wiremock)**  
Zawiera konfigurację frameworka  [Wiremock](https://wiremock.org) który może być używany w celu zastąpienia obsługi wywołań zewnętrznego serwisu ip-api.com. Serwis ip-api.com służy do pobrania kodu kraju na podstawie adresu ip.

Mock serwisu obsługuje endpoint:
```bash  
/ip-api/json/{ipaddr}?fields=countryCode,query
```
zwracając tę samą strukturę danych w odpowiedzi co właściwy serwis ([patrz dokumentacja api](https://ip-api.com/docs/api:json))

Mock zakłada, że dla adresów ip {ipaddr}:
* rozpoczynających się od cyfry 3 - countryCode nie jest zwracany w odpowiedzi (odpowiada to przypadkowi, kiedy nie można określić kraju pochodzenia po danym adresie IP)
* rozpoczynających się od cyfry 2 - zwraca countryCode: PL
* rozpoczynających się od innych cyfr - zwraca countryCode: UK

Przygotowano prosty skrypt shell do uruchomienia mocka jako kontener w Docker o nazwie **wiremock-ip-resolver**: [docker-run-wiremock-ip-resolver.sh](./wiremock/docker-run-wiremock-ip-resolver.sh)

Przykładowe testy API z wykorzystaniem mocka znajdują się w załączonej kolekcji Postman:

[coupon-challenge.postman_collection.json](./coupon-challenge.postman_collection.json)