#!/bin/bash

# 'set -e' arrête le script si une commande échoue (ex: compilation)
set -e

# Compile tous les .java de src/main et place les .class à la racine
find src/main -name "*.java" -exec javac -d . {} +

# Définit le dossier des tests
TEST_DIR="src/tests"
JAVA_MAIN_CLASS="main.Main"

# --- Exécution d'un test ---
echo -e "\n--- Sélection du fichier de test ---"

# Configure le prompt du menu
PS3="Entrez le numéro du test : "

# On stocke les chemins complets dans un tableau
files_with_path=("$TEST_DIR"/*.txt)

# On crée un tableau d'options avec juste les noms de fichiers
options_display=()
for f in "${files_with_path[@]}"; do
    options_display+=("$(basename "$f")")
done

# On lance 'select' sur le tableau des noms (options_display)
select opt_display in "${options_display[@]}"; do

    case "$opt_display" in
        "Quitter")
            echo "Quitter le script."
            break
            ;;
        *)
            # On vérifie si l'option est valide
            if [[ -n "$opt_display" ]]; then
            
                # On récupère le chemin complet
                full_path="${files_with_path[$((REPLY - 1))]}"
                
                java "$JAVA_MAIN_CLASS" "$full_path"
                break 
                
            else
                echo "Choix invalide. Veuillez réessayer."
            fi
            ;;
    esac
    # Ré-affiche le prompt (seulement si choix invalide)
    echo "$PS3"
done

echo -e "\n--- Script terminé ---"