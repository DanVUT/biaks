Format povolenych argumentov:
[] - symbolizuje voliteľné argumenty
-g [RESULT_DIRECTORY] - vygeneruje par klucov. V pripade, ze sa nezada RESULT_DIRECTORY, tak sa kluce ulozia do aktualneho priecinka z kontextu vykonavania programu

-s INPUT_FILE [PRIVATE_KEY] - podpise zadany subor. V pripade, ze sa nezada PRIVATE_KEY, tak sa vygeneruje novy par klucov v priecinku kde sa nachadza INPUT_FILE

-v SIGNED_FILE PUBLIC_KEY - verifikuje sa podpisany subor voci zadanemu PUBLIC_KEY




Pri podpisovaní súboru, sa vytvorí nový, podpísaný súbor s názvom pôvodného súboru, ktorý v názve naviac obsahuje "_signed"

napr: priklad.txt -> priklad_signed.txt

K hashovaniu obsahu priečinka sa využíva MD5. K podpisovaniu súborov sa využíva RSA-304.

Ku generovaniu a práci s veľkými číslami bola využitá vstavaná Java knižnica BigInteger.

Program bol písaný v Java 11 a testovaný na Intellij JDK 11 a Zulu Java 11. Vývoj a test prebiehal na Windows 10