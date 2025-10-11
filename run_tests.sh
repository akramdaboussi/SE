#!/bin/bash

# Script pour compiler le projet puis laisser l'utilisateur choisir
# quel cas de test exécuter.

#Complilation du projet
echo "---  Compilation du projet ---"
javac -d . src/main/BitPackingSimple.java


echo " Pour exécuter tous les tests, entrez '*', sinon tapez Entrée pour choisir un fichier spécifique."
read -r choice
# Si l'utilisateur a entré '*', on exécute tous les tests.
if [[ "$choice" == "*" ]]; then
    echo "-----------------------------------------------------"
    echo "  Lancement de tous les tests..."
    for test_file in src/tests/*.txt; do
        echo "-----------------------------------------------------"
        echo " Test pour : $test_file"
        java main.BitPackingSimple "$test_file"
        echo "-----------------------------------------------------"
    done
    echo "-----------------------------------------------------"
    echo "Tous les tests sont terminés."
    exit 0
fi

# Sinon, on propose à l'utilisateur de choisir un fichier spécifique.
if [[ "$choice" != "*" ]]; then
    echo -e "\n--- Sélection du fichier de test ---"

# On définit le dossier où se trouvent les fichiers de test.
# Note : vos fichiers sont dans 'test-data', pas dans 'src/tests'.
    TEST_DIR="src/tests"

# On crée un menu de sélection avec tous les fichiers .txt du dossier.
    PS3="Entrez le numéro du fichier que vous voulez tester (ou 'q' pour quitter) : "
    select test_file in "$TEST_DIR"/*.txt
    do
    # Si l'utilisateur a fait un choix valide...
    if [[ -n "$test_file" ]]; then
        echo "-----------------------------------------------------"
        echo " Lancement du test pour : $test_file"
        java main.BitPackingSimple "$test_file"
        echo "-----------------------------------------------------"
    else
        # Si le choix n'est pas valide (ex: l'utilisateur tape du texte).
        if [[ "$REPLY" == "q" ]]; then
            echo "Quitter le script."
            break
        fi
        echo "Choix invalide. Veuillez réessayer."
    fi
    # On repose la question. Pour quitter, l'utilisateur peut faire Ctrl+C
    # ou entrer 'q' (bien que la sortie par défaut soit Ctrl+C).
    done
fi

echo -e "\n--- Script terminé ---"

