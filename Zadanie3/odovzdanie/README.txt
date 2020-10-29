Možné argumenty programu:

-setl [String...security_levels] - Definuje bezpecnostne urovne programu.
	Vyhodnocovanie bezpecnostnych úrovní sa vykonáva od najnižšej (najmenej secure) po najvyššiu (najviac secure)
	Pri nedefinovani volitelneho argumentu sa pouziju default hodnoty: public, confidential, secret, top secret
	Pri definovaní nových bezpečnostných úrovní sa zmažú všetci definovaní užívatelia a súbory
	Pri definovaní nových bezpečnostných úrovní sa definuje jeden užívateľ s názvom "admin", ktorý má priradenú najvyššiu bezpečnostnú úroveň


-setu newUsername security_level changingUsername - Definuje noveho uzivatela s danou bezpecnostnou urovnou. Pri vytváraní je možné ako changingUsername použiť meno "admin"

-setf filename security_level username - Definuje bezpecnostnu uroven suboru. Uzivatel vsak moze definovat maximalne svoju bezpečnostnú uroven

-r filename username - Odtestuje, ci dany uzivatel ma pristup na citanie daneho suboru/priecinku

-w filename username - Odtestuje, ci dany uzivatel ma pristup na zapisovanie do daneho suboru/priecinku

-ls - vypíše vsetky bezpecnostne urovne, definovanych uzivatelov a subory
	výpis sa vykonáva podľa toho, ako  



Pravidlá prístupov a nastavení:

1. Vytváraný užívateľ môže mať maximálne takú bezpečnostnú úroveň, ako účet užívateľa, ktorý ho vytvára

2. Súbor/Priečinok, ktorý nemá nastavenú bezpečnostnú úroveň sa automaticky berie, že má najnižšiu bezpečnostnú úroveň

3. Užívateľ môže priečinku/súboru nastaviť len takú bezpečnostnú úroveň (alebo nižšiu), ako má on sám

4. Užívateľ môže nastaviť bezpečnostnú úroveň len takým priečinkom alebo súborom, ktoré majú súčasnú bezpečnostnú úroveň rovnakú, alebo nižšiu ako užívateľ

5. Keď sa nastaví bezpečnostná úroveň na priečinok, nastaví sa rekurzívne rovnaká bezpečnostná úroveň aj na všetky podadresáre a súbory v priečinku,
   avšak iba na tie podpriečinky a súbory, ktorých bezpečnostná úroveň je menšia alebo rovná bezpečnostej úrovni užívateľa, ktorý výkonáva zmenu.

6. Nie je možné nastaviť na súbor/priečinok nižšiu bezpečnostnú úroveň, než je bezpečnostná úroveň parent priečinku. 

7. Užívateľ môže čítať súbory a priečinky len rovnakej, alebo nižšej bezpečnostnej úrovne (Read Down Rule)

8. Užívateľ môže zapisovať do súborov a priečinkov iba rovnakej, alebo vyššej bezpečnostnej úrovne (Write Up Rule)

9. Užívateľ môže čítať a zároveň zapisovať len do súborov a priečinkov rovnakej úrovne (Vyplýva z bodov 7 a 8)


Technická špecifikácia:

Verzia Javy použitá pre vývoj: Java 11 LTS (ZuluFX)
OS: Windows 10