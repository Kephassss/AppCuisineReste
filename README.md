# CuisineReste — Documentation du projet

Ce dépôt contient une application Android native (Java majoritairement, avec une fine couche Kotlin côté Room) qui aide à cuisiner avec les ingrédients déjà disponibles à la maison. L’app permet de gérer un « placard » d’ingrédients, de découvrir des recettes correspondantes, d’affiner via de nombreux filtres (végétarien, budget, ustensiles, cuisines, allergènes…), de consulter le détail d’une recette, puis de cuisiner pas-à-pas avec guidage vocal (Text‑to‑Speech).

Cette documentation explique le fonctionnement concret, l’architecture, les principaux écrans, les modèles de données, et les choix techniques. Elle fournit aussi des instructions de build, d’extension (ajout d’ingrédients/recettes), et de dépannage.


**Sommaire**
- Présentation générale
- Architecture et organisation
- Modèles de données (assets JSON + modèles Java)
- Flux fonctionnels (écrans et navigation)
- Filtres de recettes (logique concrète)
- Persistance locale (SharedPreferences + Room)
- Réseau (Retrofit + TheMealDB, optionnel)
- UI, animations et images
- Construction, exécution et tâches Gradle utiles
- Ajouter des ingrédients/recettes et images
- Tests et qualité
- Dépannage courant
- Évolutions possibles


**Présentation Générale**
- Objectif: proposer des idées de plats en fonction de votre placard d’ingrédients et de préférences culinaires.
- Plateforme: Android (minSdk 24, target/compileSdk 35).
- Langages: Java pour l’UI, la logique et la data; Kotlin pour les entités/DAO Room.
- Données embarquées: ingrédients et jeux de recettes en JSON sous `app/src/main/assets/`.
- Offline-first: l’app fonctionne entièrement hors‑ligne avec les assets; une intégration réseau (TheMealDB) est prête pour enrichir.


**Architecture Et Organisation**
- Couche UI (Fragments/Activity)
  - Accueil: `app/src/main/java/com/repasdelaflemme/app/ui/home/HomeFragment.java:1`
  - Recettes: `app/src/main/java/com/repasdelaflemme/app/ui/recipes/RecipesFragment.java:1`
  - Placard: `app/src/main/java/com/repasdelaflemme/app/ui/pantry/PantryFragment.java:1`
  - Détail recette: `app/src/main/java/com/repasdelaflemme/app/ui/detail/RecipeDetailFragment.java:1`
  - Cuisine guidée: `app/src/main/java/com/repasdelaflemme/app/ui/detail/CookingFragment.java:1`
  - Activité principale + navigation bas: `app/src/main/java/com/repasdelaflemme/app/ui/main/MainActivity.java:1`
- Couche Data
  - Assets (JSON → modèles): `app/src/main/java/com/repasdelaflemme/app/data/AssetsRepository.java:1`
  - Stockage du placard (SharedPreferences): `app/src/main/java/com/repasdelaflemme/app/data/PrefPantryStore.java:1`
  - Statistiques d’usage: `app/src/main/java/com/repasdelaflemme/app/data/PrefPantryStats.java:1`
  - Favoris placard: `app/src/main/java/com/repasdelaflemme/app/data/PrefPantryFavorites.java:1`
  - Modèles de données: `app/src/main/java/com/repasdelaflemme/app/data/model/*.java`
  - Base Room (future extension): `app/src/main/java/com/repasdelaflemme/app/data/local/*`
- Réseau (optionnel)
  - Client Retrofit TheMealDB: `app/src/main/java/com/repasdelaflemme/app/data/remote/MealDbClient.java:1`
  - API: `app/src/main/java/com/repasdelaflemme/app/data/remote/MealDbApi.java:1`
- Navigation
  - Graph de nav Jetpack: `app/src/main/res/navigation/nav_graph.xml:1`
- Entrée d’application / Crashs
  - `Application` avec Crashlytics + log local: `app/src/main/java/com/kephas/appcuisinereste/App.java:1`
  - Logger de crash local: `app/src/main/java/com/repasdelaflemme/app/util/CrashLogger.java:1`

Structure des modules et dépendances Gradle:
- Projet: `build.gradle:1`
- Module app: `app/build.gradle:1` (ViewBinding, Navigation, Room, Gson, Glide, Retrofit/OkHttp, Coroutines, Material, Firebase Crashlytics conditionnel)


**Modèles De Données**
- Ingrédient: `app/src/main/java/com/repasdelaflemme/app/data/model/Ingredient.java:1`
  - Champs: `id`, `name`, `category`, `unit`.
- Recette: `app/src/main/java/com/repasdelaflemme/app/data/model/Recipe.java:1`
  - Champs clés: `id`, `title`, `minutes`, `servings`, `vegetarian`, `halal`, `budget (1..3)`, `utensils`, `cuisine`, `allergens`, `containsAlcohol`, `containsPork`, `image` ("res:photo_xxx" ou URL), `ingredients`, `steps`.
- Ingrédient dans une recette: `app/src/main/java/com/repasdelaflemme/app/data/model/RecipeIngredient.java:1`
  - Champs: `id`, `qty` (double), `unit`.
- Cartes UI (liste): `app/src/main/java/com/repasdelaflemme/app/ui/common/RecipeCard.java:1`
  - Calculées à partir de `Recipe` + placard, avec `matchScore` (% d’ingrédients disponibles) et `missingCount`.

Sources JSON (assets):
- Ingrédients: `app/src/main/assets/ingredients.json:1`
- Recettes (packs): `app/src/main/assets/recipes/set_01.json:1`, `app/src/main/assets/recipes/set_02.json:1`
- Chargement: `AssetsRepository.getRecipes/getIngredients` agrège et met en cache en mémoire (`AssetsRepository.java:23`).
- Génération synthétique (dev): `BuildConfig.GENERATE_SYNTHETIC_RECIPES` ajoute des recettes de démo pour tester des listes longues (`AssetsRepository.java:52`).

Images de recettes:
- Valeur `image` peut référencer un drawable embarqué via `res:photo_xxx` (ex: `res:photo_pasta`) ou une URL HTTP(S).
- Vérification build des images requises: tâche Gradle `verifyRecipeImages` (`app/build.gradle:127`).


**Flux Fonctionnels (Écrans Et Navigation)**
- Accueil
  - Vue: `HomeFragment.java:18`
  - Affiche des cartes de recettes avec pourcentage de correspondance à partir du placard (calcul dans `HomeViewModel.java:18`).
  - Boutons raccourcis vers Placard et Découvrir.
- Placard (gestion des ingrédients)
  - Vue: `PantryFragment.java:30`
  - Catalogue filtrable (recherche texte + catégories) et sélection/désélection de visuels (`PantryCatalogAdapter.java:17`).
  - Favoris d’ingrédients, sections rapides « Récents / Fréquents / Favoris » (stats et favoris en SharedPreferences).
  - Mise à jour en temps réel des « chips » sélectionnées et d’un compteur.
- Liste de recettes / Découverte
  - Vue: `RecipesFragment.java:35`
  - Grille 2 colonnes avec skeletons au chargement (`SkeletonAdapter.java:10`).
  - Filtrage avancé (voir section « Filtres de recettes »). Navigation vers le détail.
- Détail d’une recette
  - Vue: `RecipeDetailFragment.java:28`
  - Photo (Glide), durée/portions, tags (rapide, végé…), liste d’ingrédients marqués selon présence dans le placard, étapes.
  - Variante « light » automatique si un `id` suffixé `_light` existe (toggle de rendu dans `RecipeDetailFragment.java:51`).
  - CTA « Commencer la recette » vers Cuisine guidée.
- Cuisine guidée
  - Vue: `CookingFragment.java:23`
  - Pas-à-pas avec TTS (lecture/pause), boutons précédent/suivant, progression visuelle, swipe gauche/droite, écran toujours allumé.
- Navigation
  - Graph: `app/src/main/res/navigation/nav_graph.xml:1` (Accueil → Placard → Recettes → Détail → Cuisine).
  - Activité: `MainActivity.java:19` gère `NavHostFragment`, `BottomNavigationView` et un FAB contextuel.


**Filtres De Recettes (Logique Concrète)**
La logique de filtrage est centralisée dans `RecipesFragment.applyFilters()` (`app/src/main/java/com/repasdelaflemme/app/ui/recipes/RecipesFragment.java:267`). Les critères sont combinés côté client sur la liste `allRecipes` (provenant des assets) et produisent des `RecipeCard` triées/affichées.

Critères disponibles (chips et champs de recherche):
- Recherche texte (titre) en minuscule/normalisée.
- Végétarien; « Rapide » (< 20 min).
- Placard uniquement (toutes les recettes dont tous les ingrédients sont dans le placard) et « Tolérer 1 manquant ».
- Halal uniquement.
- Budget: 1, 2, 3.
- Temps: rapide/medium/long selon `minutes` (<20, 20–40, >40).
- Ustensiles: poêle, casserole, four, wok, cocotte, mixeur.
- Cuisines: français, italien, asiatique, indien, maghrébin, mexicain, US, méditerranéen, autres.
- Allergènes à éviter: gluten, lactose, œuf, arachide, fruits à coque, crustacé, soja, sésame.
- Sans alcool, sans porc.

Calcul d’adéquation au placard:
- Pour chaque recette, on compte les ingrédients présents vs. requis → `matchScore` (0–100%) et `missingCount`.
- Cette info est affichée sur les cartes et utilisée pour le tri dans un fallback (cf. `lateFallback()` en fin de `RecipesFragment.java:541`).


**Persistance Locale**
- Placard (SharedPreferences): `PrefPantryStore.java:12`
  - Stocke la liste d’`id` d’ingrédients sélectionnés sous forme de JSON.
  - Exposé en méthodes `getIngredientIds()`, `add(id)`, `remove(id)`, `setIngredientIds(list)`.
- Stats d’usage: `PrefPantryStats.java:14`
  - Compte d’usage et « last used » par ingrédient; permet « Récents » et « Fréquents ».
- Favoris: `PrefPantryFavorites.java:11`
  - Set d’`id` favoris JSON; callbacks de toggle dans `PantryCatalogAdapter`.
- Base de données Room (pré‑câblée, non branchée à l’UI actuelle):
  - Schéma: `AppDatabase.kt:9` (Ingredients, Recipes, CrossRef)
  - Entités: `IngredientEntity.kt:6`, `RecipeEntity.kt:6`, `RecipeIngredientCrossRef.kt:5`
  - DAO: `IngredientDao.kt:8`, `RecipeDao.kt:9`
  - Provider: `DatabaseProvider.kt:6`


**Réseau (Optionnel)**
- Client TheMealDB (Retrofit/OkHttp): `MealDbClient.java:15`, API `MealDbApi.java:7`.
- Aujourd’hui, le chargement principal reste sur les assets embarqués. Le client est prêt pour des enrichissements (recherche distante) et peut être appelé depuis l’écran Recettes.


**UI, Animations Et Images**
- Styles / thèmes Material 3: `app/src/main/res/values/themes.xml:1`, couleurs: `app/src/main/res/values/colors_app.xml:1`.
- Liste de recettes: `RecipeAdapter.java:18` affiche les chips (rapide, % dispo, manquants, végé, budget, cuisine, ustensiles) et charge l’image (Glide) depuis `imageUrl` ou `imageResId`.
- Squelettes et shimmer: `SkeletonAdapter.java:10`, `ShimmerDrawable.java:17` pour un effet de chargement fluide.
- Utilitaires d’animation: `AnimUtils.java:15` (slide‑in keyframes, press, fade‑in, bounce, pulse).
- Navigation bas + FAB: layout `app/src/main/res/layout/activity_main.xml:1` et contrôlé par `MainActivity.java:34` (icône/texte/action dynamiques selon destination).
- Icône launcher et mascotte: voir `DOC_IMPL.md:1` et ressources `app/src/main/res/mipmap-*` et `app/src/main/res/drawable/`.


**Construction, Exécution Et Tâches Gradle Utiles**
Prérequis:
- Android Studio récent (SDK 35 installé), JDK 17.
- Pas d’installation manuelle de dépendances: le wrapper Gradle gère tout.

Commandes courantes:
- Construire debug: `./gradlew :app:assembleDebug`
- Lancer depuis Android Studio: ouvrez le projet, sélectionnez un device, « Run ».

Plugins / BuildConfig:
- `BuildConfig.GENERATE_SYNTHETIC_RECIPES` (debug=true, release=false) pour générer un volume de recettes de test (`AssetsRepository.java:52`).
- Crashlytics (optionnel): activé si `google-services.json` est présent à la racine du module `app` (`app/build.gradle:7`).

Tâches utilitaires (module `app`):
- `verifyRecipeImages`: vérifie que toutes les images de recettes attendues existent dans `res/drawable` (`app/build.gradle:127`).
  - En CI/release, échoue si manquantes (sauf si `-PallowMissingRecipeImages`).
- `stripBom`: supprime les BOM UTF‑8 dans le code source pour éviter `illegal character: \ufeff` (`app/build.gradle:178`).
- `importRecipeImages`: copie des images depuis `app/recipe_images_input` vers `res/drawable` en mappant les noms (pasta/omelette/... → `photo_*`) (`app/build.gradle:204`).


**Ajouter Des Ingrédients/Recettes Et Images**
- Ingrédients: éditez `app/src/main/assets/ingredients.json`.
  - Respecter les champs `id`, `name`, `category`, `unit`.
- Recettes: ajoutez des fichiers JSON dans `app/src/main/assets/recipes/` (ex. `set_03.json`).
  - Respecter le schéma `Recipe` (voir section Modèles). Utiliser `image` au format `res:photo_xxx` si image locale, sinon URL.
- Images: placez des fichiers dans `app/src/main/res/drawable/` sous les noms attendus (ex. `photo_pasta.jpg`).
  - Utilisez `./gradlew :app:verifyRecipeImages` pour vérifier la complétude.
  - Aide à l’import: `./gradlew :app:importRecipeImages` (déposez vos images dans `app/recipe_images_input/`).


**Tests Et Qualité**
- Exemples fournis: `app/src/test/java/.../ExampleUnitTest.java:1`, `app/src/androidTest/java/.../ExampleInstrumentedTest.java:1`.
- Stratégies recommandées:
  - Tests unitaires sur la logique de filtrage (extraction des méthodes depuis `RecipesFragment`) et de conversion `Recipe` → `RecipeCard` (`HomeViewModel`/`RecipesFragment`).
  - Tests instrumentés UI pour la navigation (Navigation Component) et l’affichage des listes/écrans vides.


**Dépannage Courant**
- Images de recettes manquantes
  - Symptôme: avertissement/échec de build via `verifyRecipeImages`.
  - Solution: ajouter les fichiers sous `app/src/main/res/drawable/` ou passer `-PallowMissingRecipeImages` pour contourner (non recommandé en release).
- Encodage UTF‑8 / caractères accentués bizarres
  - Symptôme: diacritiques corrompus dans certaines chaînes.
  - Solution: exécuter `./gradlew :app:stripBom` et s’assurer que le projet est en UTF‑8.
- Crash au long‑press du FAB en debug
  - Comportement prévu pour vérifier la capture Crashlytics (`MainActivity.java:63–71`).
- Crashlytics/Google Services absents
  - L’app démarre sans; placez `app/google-services.json` pour activer Crashlytics/Analytics.
- Réseau TheMealDB
  - Si la recherche distante est activée ultérieurement, vérifier la connectivité et la latence; le client est configuré avec OkHttp + logging basique (`MealDbClient.java:18`).


**Évolutions Possibles**
- Brancher la base Room à l’UI pour persister recettes/ingrédients enrichis.
- Intégrer la recherche distante (TheMealDB) dans l’écran Recettes avec fusion locale/distante et cache.
- Ajouter un écran « Courses » pour lister les ingrédients manquants d’une recette.
- Améliorer l’accessibilité (TalkBack, contrastes, tailles de police dynamiques).
- Localisation multi‑langues complète (FR/EN) et corrections typographiques.


**Licence**
— Non spécifiée dans le dépôt. Par défaut, usage interne/projet. Ajoutez une licence si nécessaire.

