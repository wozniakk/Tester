# Tester

####1. Instalacja

Po prostu rozpakowujemy archiwum na obydwu komputerach.
    
####2. Uruchamianie

Na obydwu komputerach uruchamiamy ``./Tester.sh <adres zdalny>``, przykładowo:
    
192.168.0.10 | 192.168.0.11
:------------: | :-------------:
./Tester.sh 192.168.0.11 | ./Tester.sh 192.168.0.10

####3. Używanie

Program oczekuje dwóch komend:
    
``run`` - wpisujemy na jednym z komputerów, inicjalizuje cały proces testowania, po pomyślnym zakończeniu widoczny komunikat DONE. Po drugiej stronie otrzymujemy wyniki analizy. Gdy cokolwiek doszło, wyniki procentowe są >= 0.

``exit`` - zamyka program.
