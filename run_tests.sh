#!/bin/bash

# 'set -e' arrête le script si une commande échoue (ex: compilation)
set -e

# Compile tous les .java de src/main et place les .class à la racine
find src/main -name "*.java" -exec javac -d . {} +

# --- 2. Sélection du type de compression ---
echo -e "\n--- Choix du type de compression ---"
PS3="Quel type de compresseur utiliser ? : "
options_compression=("no_overlap" "overlap" "Quitter")

# On stocke le choix dans la variable $comp_type
select comp_type in "${options_compression[@]}"; do
    case "$comp_type" in
        "no_overlap" | "overlap")
            echo "Mode sélectionné : $comp_type"
            break
            ;;
        "Quitter")
            echo "Quitter le script."
            exit 0
            ;;
        *)
            echo "Choix invalide. Veuillez réessayer."
            ;;
    esac
done

# Si $comp_type est vide (l'utilisateur a fait Ctrl+D), on quitte
if [[ -z "$comp_type" ]]; then
    exit 1
fi

# Définit le dossier des tests
TEST_DIR="src/tests"
JAVA_MAIN_CLASS="main.app.Main"

# --- 3. Exécution d'un test ---
echo -e "\n--- Sélection du fichier de test ---"
PS3="Entrez le numéro du test (ou 'q' pour quitter) : "

files_with_path=("$TEST_DIR"/*.txt)
options_display=()
for f in "${files_with_path[@]}"; do
    options_display+=("$(basename "$f")")
done

select opt_display in "${options_display[@]}"; do
    if [[ "$REPLY" == "q" ]]; then
        echo "Quitter le script."
        break
    fi

    case "$opt_display" in
        "Quitter")
            echo "Quitter le script."
            break
            ;;
        *)
            if [[ -n "$opt_display" ]]; then
                full_path="${files_with_path[$((REPLY - 1))]}"
                
                # On passe le $full_path ET le $comp_type au Main
                java "$JAVA_MAIN_CLASS" "$full_path" "$comp_type"
                
                echo "-----------------------------------------------------"
                break 
            else
                echo "Choix invalide. Veuillez réessayer."
            fi
            ;;
    esac
    echo "$PS3"
done

echo -e "\n--- Script terminé ---"