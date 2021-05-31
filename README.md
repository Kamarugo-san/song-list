# Virtual Song List
This project aims to create an expansive song list so the user can store and read their lyrics at any given time.

## Requirements
- The project must be written in Java
- The app must:
  * [x]  Support a default song set
  * [x]  Show the songs in a list
  * [x]  Allow the user to add songs manually
  * [x]  Allow the user to add songs via zip files
  * [x]  Allow the user to touch an item of the list to open it
  * [x]  Allow the user to select multiple items to export as a zip file that can be imported
  * [x]  Allow the user to select multiple items to delete
  * [x]  Allow the user to filter through the list
  * [x]  Allow the user to edit an added or imported song
  * [x]  Allow the user to mark a text in the lyrics
  * [x]  Check for duplicated songs using MD5 hashes based on their content and let user choose between keeping or deleting them

## Implementation
[Jetpack Navigation Component](https://developer.android.com/guide/navigation) is being used alongside [ViewModel](https://developer.android.com/reference/androidx/lifecycle/ViewModel) and [LiveData](https://developer.android.com/reference/androidx/lifecycle/LiveData).

## Team
[Matheus Camargo Gomes da Silva](https://github.com/Kamarugo-san)

## Note
This project is a university assignment for [Universidade São Francisco](https://www.usf.edu.br/) and has Brazilian Portuguese as its default UI language.

## License
```
Copyright 2021 Matheus Camargo Gomes da Silva

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```