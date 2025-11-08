# CookNReste — Application Android

CookNReste est une application Android native qui aide à cuisiner avec les ingrédients déjà disponibles à la maison. L’app permet de gérer un « placard » d’ingrédients, de découvrir des recettes correspondantes, d’affiner via des filtres (végétarien, budget, ustensiles, cuisines, allergènes…), de consulter le détail d’une recette, puis de cuisiner pas à pas avec guidage vocal (Text‑to‑Speech).

Pour des notes d’implémentation détaillées (UI, icône, mascotte, thèmes…), voir `DOC_IMPL.md`.

## Aperçu
- Plateforme: Android (minSdk 24, target/compileSdk 35)
- Langages: Java (UI, logique, data) + Kotlin limité (entités/DAO Room)
- Données embarquées: ingrédients et recettes en JSON sous `app/src/main/assets/`
- Mode hors‑ligne en priorité, option réseau (TheMealDB) prête pour enrichissement

## Fonctionnalités
- Gestion d’un placard d’ingrédients (ajout, sélection rapide, favoris)
- Découverte et filtrage de recettes (temps, budget, régimes, ustensiles, cuisines, allergènes)
- Détail de recette clair (ingrédients, étapes, temps, portions)
- Cuisine guidée pas à pas avec commandes et TTS
- UI Material 3 avec animations et états de chargement

## Structure du projet
- UI (Fragments/Activity)
  - Accueil: `app/src/main/java/com/repasdelaflemme/app/ui/home/HomeFragment.java`
  - Recettes: `app/src/main/java/com/repasdelaflemme/app/ui/recipes/RecipesFragment.java`
  - Placard: `app/src/main/java/com/repasdelaflemme/app/ui/pantry/PantryFragment.java`
  - Détail: `app/src/main/java/com/repasdelaflemme/app/ui/detail/RecipeDetailFragment.java`
  - Cuisine guidée: `app/src/main/java/com/repasdelaflemme/app/ui/detail/CookingFragment.java`
  - Activité principale: `app/src/main/java/com/repasdelaflemme/app/ui/main/MainActivity.java`
- Data
  - Assets (JSON → modèles): `app/src/main/java/com/repasdelaflemme/app/data/AssetsRepository.java`
  - Placard (SharedPreferences): `app/src/main/java/com/repasdelaflemme/app/data/PrefPantryStore.java`
  - Stats d’usage: `app/src/main/java/com/repasdelaflemme/app/data/PrefPantryStats.java`
  - Favoris placard: `app/src/main/java/com/repasdelaflemme/app/data/PrefPantryFavorites.java`
  - Modèles: `app/src/main/java/com/repasdelaflemme/app/data/model/`
  - Base Room (extension): `app/src/main/java/com/repasdelaflemme/app/data/local/`
- Réseau (optionnel)
  - Client Retrofit: `app/src/main/java/com/repasdelaflemme/app/data/remote/MealDbClient.java`
  - API: `app/src/main/java/com/repasdelaflemme/app/data/remote/MealDbApi.java`
- Application / Crashs
  - `Application` + Crashlytics + log local: `app/src/main/java/com/repasdelaflemme/app/App.java`
  - Logger local: `app/src/main/java/com/repasdelaflemme/app/util/CrashLogger.java`

## Prérequis
- Android Studio récent (SDK 35 installé)
- JDK 17
- Aucun install manuel: utiliser le wrapper Gradle fourni

## Démarrage rapide
1) Ouvrir le projet dans Android Studio
2) Laisser Gradle synchroniser les dépendances
3) Lancer sur un émulateur ou un appareil depuis Android Studio

Ligne de commande:
- macOS/Linux: `./gradlew :app:assembleDebug`
- Windows: `./gradlew.bat :app:assembleDebug`

## Tâches Gradle utiles (module `app`)
- `verifyRecipeImages`: vérifie que toutes les images attendues existent dans `app/src/main/res/drawable/`
  - Échoue en CI/release si manquantes (sauf `-PallowMissingRecipeImages`)
- `stripBom`: supprime des BOM UTF‑8 pour éviter `illegal character: \ufeff`
- `importRecipeImages`: importe des images depuis `app/recipe_images_input` vers `res/drawable` en mappant les noms (`photo_*`)

## Données (assets)
- Ingrédients: `app/src/main/assets/ingredients.json`
- Recettes: `app/src/main/assets/recipes/`
  - Exemples: `set_01.json`, `set_02.json`

## Ajouter ingrédients, recettes et images
- Éditer `app/src/main/assets/ingredients.json` (champs: `id`, `name`, `category`, `unit`)
- Ajouter des recettes sous `app/src/main/assets/recipes/` (ex. `set_03.json`)
  - Champ `image`: `res:photo_xxx` pour ressource locale, ou URL
- Placer les images sous `app/src/main/res/drawable/` (ex. `photo_pasta.jpg`)
  - Vérifier la complétude: `:app:verifyRecipeImages`
  - Voir `DOC_IMPL.md` pour les détails d’icône/mascotte

## Dépannage
- Images de recettes manquantes → compléter `res/drawable/` ou utiliser `-PallowMissingRecipeImages` (à éviter en release)
- Encodage/accents → exécuter `:app:stripBom` et vérifier l’UTF‑8
- Crash volontaire en debug au long‑press du FAB → vérifie la capture Crashlytics
- Crashlytics/Analytics optionnels → ajouter `app/google-services.json` pour activer

## Licence
Non spécifiée dans le dépôt. Ajoutez une licence si nécessaire.
