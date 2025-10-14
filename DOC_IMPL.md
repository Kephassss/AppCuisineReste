CuisineReste – Notes d’implémentation et design

Résumé
- Objectif: rapprocher l’UI du mockup fourni (Material 3, look “CooknRest”), intégrer la mascotte et préparer l’icône d’application.
- Langages: Java/Groovy uniquement (pas de Kotlin ajouté). Pas de scanner implémenté.

Palette et thème
- Fichier couleurs brand: app/src/main/res/values/colors_app.xml
  - Vert primaire `#22C55E`, secondaire `#10B981`, surfaces claires, accents.
- Thème Material 3: app/src/main/res/values/themes.xml
  - Couleurs brand branchées (colorPrimary, surface…), coins arrondis (shapes), barres système.
- Splash: app/src/main/res/drawable/splash_background.xml (dégradé vert, fond surface).

Libellés FR
- Fichier: app/src/main/res/values/strings.xml
  - Corrections d’accents/orthographe, nouveaux textes (CTA “Commencer la recette”, “Mes ingrédients”, etc.).

Intégration de la mascotte
- Drawable vectoriel simplifié: app/src/main/res/drawable/mascot_chef.xml
  - Sert de logo dans la toolbar et les headers d’écrans.
- Utilisation:
  - Toolbar: app/src/main/res/layout/activity_main.xml (attribut `app:logo`).
  - Accueil: app/src/main/res/layout/fragment_home.xml (ImageView dans l’en‑tête).
  - Détail recette: app/src/main/res/layout/fragment_recipe_detail.xml (ImageView dans le header).

Icône de l’application (launcher)
- Android 8+ (API 26+) utilise des Adaptive Icons (background + foreground).
- Actuellement configuré pour ressembler à l’image fournie:
  - Fond orange brand: `@color/cr_accent_orange`.
  - Premier plan: `@drawable/mascot_chef`.
  - Fichiers: 
    - app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
    - app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml

Utiliser exactement votre image “mascotte fond orange” comme logo
1) Préparer un PNG carré 432×432 px (recommandé par Google pour l’asset de premier plan des Adaptive Icons). Votre visuel inclut déjà le fond orange arrondi → gardez-le tel quel.
2) Méthode rapide (Android Studio): Tools → Asset Studio → Launcher Icons → Foreground Layer = votre PNG → Background = Transparent (ou couleur neutre). L’outil générera tous les mipmaps automatiquement et remplacera `ic_launcher`.
3) Méthode manuelle (si vous ne passez pas par Asset Studio):
   - Remplacez les fichiers existants `ic_launcher.webp` dans:
     - app/src/main/res/mipmap-mdpi/
     - app/src/main/res/mipmap-hdpi/
     - app/src/main/res/mipmap-xhdpi/
     - app/src/main/res/mipmap-xxhdpi/
     - app/src/main/res/mipmap-xxxhdpi/
     par votre PNG décliné aux tailles standard (48, 72, 96, 144, 192 px).
   - Pour Android 8+: placez aussi un PNG 432×432 nommé `ic_launcher_foreground.png` et configurez via Asset Studio, ou éditez les fichiers adaptive si vous préférez un autre nom.
4) Après remplacement, reconstruisez l’app; l’icône affichée dans le launcher sera exactement votre image.

Écrans refondus
- Accueil: app/src/main/res/layout/fragment_home.xml
  - Header dégradé + mascotte; compteur “%d recettes sauvegardées”; 2 gros boutons (“Mes ingrédients”, “Découvrir des recettes”).
  - Navigation branchée depuis HomeFragment: app/src/main/java/com/repasdelaflemme/app/ui/home/HomeFragment.java (MAJ du compteur + click listeners).
- Liste de recettes: app/src/main/res/layout/item_recipe.xml
  - Cartes Material, pastille durée (tag vert). Drawable: app/src/main/res/drawable/tag_time_bg.xml.
- Détail recette: app/src/main/res/layout/fragment_recipe_detail.xml
  - Header coloré + CTA “Commencer la recette”; sections ingrédients/étapes lisibles.
- Ingrédients: app/src/main/res/layout/fragment_pantry.xml
  - TextInput Material + header avec mascotte.

Navigation et activités
- Activité principale: app/src/main/java/com/repasdelaflemme/app/ui/main/MainActivity.java (Toolbar + BottomNav + NavHost).
- Graph de navigation: app/src/main/res/navigation/nav_graph.xml (Accueil, Placard, Recettes, Détail, Courses).

Ce qui n’a pas été fait (conformément à la demande)
- Pas de scanner d’ingrédients.
- Pas de Kotlin; tout reste en Java/Groovy.

Prochaines améliorations suggérées
- Pastilles supplémentaires (difficulté, coût) sur la liste et le détail.
- États vides/illustrations cohérents avec la mascotte.
- Écran “Courses” complet + suggestions intelligentes.
- Thème sombre peaufiné.

Questions fréquentes
- “Pourquoi garder un vector mascotte si je fournis un PNG ?” → le vector sert d’illustration interne (toolbar/header). Pour l’icône launcher, utilisez Asset Studio avec votre PNG afin d’optimiser toutes les tailles et masques.
- “Dois‑je supprimer l’ancien `ic_launcher` ?” → pas obligatoire si vous passez par Asset Studio; l’outil remplace proprement les ressources.

