# Rick and Morty API - Application Android

## Vue d'ensemble

Cette application Android utilise l'API **Rick and Morty** pour afficher des informations sur les personnages et les localisations. Elle suit l'architecture **MVI (Model-View-Intent)**, qui garantit un flux de données stable dans une seule direction et une séparation claire des responsabilités. Dans cette application, elle est notamment mise en oeuvre par la séparation des différentes couches en modules distinct ainsi que par l'utilisation de l'injection de dépendances.

### Fonctionnalités principales

- Affichage des personnages et détails des personnages de l'univers **Rick and Morty**.
- Affichage des localisations et des personnages présents dans chaque localisation.
- Utilisation de **Ktor** pour gérer les appels API distants.
- Sauvegarde en local des données via **Realm** pour permettre l'accès hors ligne.

### Architecture

L'application suit l'architecture **MVI**, qui est constituée de trois éléments principaux :

- **Model (M)** : Représente le métier de l'application.
- **View (V)** : L'interface utilisateur qui réagit aux changements de l'état du modèle.
- **Intent (I)** : Capture les actions de l'utilisateur et les événements, qui sont transformés en mises à jour de l'état par le `ViewModel`. La `View` envoie des actions au `ViewModel`, qui lui va pouvoir y réagir et mettre à jour l'état.

### Structure du projet et respect de la Clean Architecture

Cette application est conçue selon les principes de la **Clean Architecture**, assurant une séparation claire des responsabilités entre les différentes couches de l'application. Elle est divisée en plusieurs couches :
- la **couche de domaine** (entités métier), qui est la couche centrale et qui ne connait qu'elle même
- la **couche de présentation** (ViewModels et View) qui connait la couche **domaine**
- la **couche de données** (répertoires, sources de données locales et distantes) qui connait également la couche **domaine**

Cette architecture permet une meilleure maintenabilité, testabilité, et évolutivité du projet. Les dépendances pointent toujours vers l'intérieur, garantissant que les détails d'implémentation (frameworks, API, bases de données) ne contaminent pas la logique métier.

Voici un aperçu de la structure des modules :

```
core/
  └── data/
      └── repositories/
      └── remote/
      └── local/
  └── domain/
      └── models/
      └── repositories/
  └── ui/
      └── composables/
      └── themes/
features/
  └── characters/
  └── locations/
```

#### **core**

Le module **core** contient les modules principaux :

- **data** : Gère la logique de récupération et de stockage des données, incluant les **API**, les bases de données locales (**Realm**) et l'implémentation des **répertoires**.
  - **repositories** : Implémente les interfaces des répertoires du domain.
  - **api** : Gère les appels à des services distants via **Ktor**.
  - **local** : Gère le stockage et la récupération des données locales à partir de **Realm**.

- **domain** : Définit la logique métier principale et les modèles de domaine.
  - **models** : Les modèles de données utilisés dans l'application, comme `Character` et `Location`.
  - **repositories** : Définit les interfaces de logique de récupération des données tels que `CharacterRepository` ou encore `LocationRepository`. Les sources de données peuvent différer : des sources distantes (API) ou locales (base de données Realm)

- **ui** : Contient des composants d'interface utilisateur réutilisables.
  - **composables** : Composants UI partagés comme des cards de personnages, de localisation, etc.

#### **features**

Le module **features** contient les modules spécifiques à chaque fonctionnalité.

- **characters** : Gestion des personnages.
  - **CharacterScreen.kt** : Écran affichant la liste des personnages.
  - **CharacterViewModel.kt** : Gère l'état et la logique de la page des personnages via le modèle MVI.
  - **CharacterDetailsScreen.kt** : Écran affichant les détails d'un personnage.
  - **CharacterDetailsViewModel.kt** : Gère l'état et la logique de la page des détails de personnage via le modèle MVI.

- **locations** : Gestion des localisations.
  - **LocationDetailsScreen.kt** : Écran affichant les détails d'une localisation.
  - **LocationDetailsViewModel.kt** : Gère l'état et la logique de la page des détails de localisation via le modèle MVI.

### Flux de données MVI

#### Intent

Les **Intents** représentent les actions de l'utilisateur (comme la sélection d'un personnage ou la navigation vers une localisation). Ces intents déclenchent des modifications dans le **State** via le `ViewModel`.

#### ViewModel et Model

Le `ViewModel` reçoit les **Intents** et met à jour le **State** en conséquence. Le **State** est un état immuable qui est mis à jour lorsqu'un changement se produit (par exemple, lors d'un appel API réussi). Ce **State** est ensuite observé par la vue pour refléter l'état mis à jour dans l'interface utilisateur.

#### View

Les **Views** sont construites avec **Jetpack Compose** et observent l'état à travers des `StateFlow` dans le `ViewModel`. Toute modification de l'état déclenche une mise à jour réactive de l'interface utilisateur.

### Gestion des données

#### API

L'application utilise **Ktor** pour gérer les appels réseau de manière asynchrone avec les coroutines. Les services API communiquent avec l'API Rick and Morty pour récupérer les personnages et les localisations.

- **CharacterApi** : Requêtes pour obtenir les informations des personnages.
- **LocationApi** : Requêtes pour obtenir les informations sur les localisations.

#### Stockage local

**Realm** est utilisé pour stocker les données localement. Cela permet à l'application de fonctionner hors ligne en mettant en cache les données récupérées via l'API.

- **CharacterLocal** : Gère la base de données pour stocker les personnages.
- **LocationPreviewLocal** : Gère la base de données pour stocker les prévisualisations des localisations.

#### Politique de fetch

L'application utilise une politique de fetch dite **local first** concernant la liste des personnages et les détails de leur localisation. Cela signifie que les données vont tout d'abord être recherchées dans le stockage local, et si rien n'est trouvé la source deviendra l'API distante. Cela est pertinent ici car ce sont des données d'un référentiel qui n'est pas amené a bouger souvent. Cela permet une utilisation hors ligne et fluide même dans des cas de mauvaise connexion internet.

### Injection de dépendances

L'application utilise **Koin** pour l'injection de dépendances. Cela simplifie la gestion des dépendances comme les `Repositories` ou les services API.

Voici un exemple de configuration du `DataModule` avec Koin:

```kotlin
val dataModule = module {

    single<HttpClient> {
        createHttpClient(
            baseUrl = RMAPI_URL
        )
    }

    single<RealmDatabase> { RMDatabase() }

    single { CharacterLocal(get()) }

    single { LocationPreviewLocal(get()) }

    single { CharacterApi(get()) }

    single<CharacterRepository> {
        CharacterRepositoryImpl(
            get(),
            get(),
            get(),
            get()
        )
    }

    single { LocationApi(get()) }

    single<LocationRepository> { LocationRepositoryImpl(get()) }
}
```