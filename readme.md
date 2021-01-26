# StarWars Search
This API works as an 'intermediate API'

All the data is based on the other API https://swapi.dev/

But, here you can search the Star Wars information by name and receive some similar suggestions about other things

Eg: If you search by Luke Skywalker, you also receive more 3 suggestions about similar people from Star Wars.

The API uses some Longest Common Subsequence algorithms to find similar elements

All the endpoints from https://swapi.dev/ are valid here.

The following are examples of SWAPI requests and their respective on *StarWars Search*:

    https://swapi.dev/api/people/1/
    http://localhost:8080/starwars/search/people?name=LukeSkywalker
    
    https://swapi.dev/api/planets/1/
    http://localhost:8080/starwars/search/planets?name=Tatooine

After the endpoint */starwar/search*, you can search from any element as on the original API

The *StarWars Search* also considers small orthography errors, so if you search something like 'Luki Skywalki', the result will be the same.

# Metrics
The following endpoints return all the terms that were searched on API with a counter

    http://localhost:8080/starwars/metrics?page=0

# How to run
    docker run -p 8080:8080 pacdouglas/starwars-search
    
You can also persist the database, mapping a directory in your PC:

    docker run -p 8080:8080 -v /path/to/directorydb:/starwarsdb pacdouglas/starwars-search

The */path/to/directorydb* must be an existing directory